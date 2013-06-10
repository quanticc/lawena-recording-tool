
package ui;

import config.CLWindows;
import config.CommandLine;
import config.CustomPath;
import config.CustomPathList;
import config.FileManager;
import config.MovieManager;
import config.SettingsManager;
import config.SettingsManager.Key;
import config.StartLogger;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

public class Lawena {

    public class MovieFolderChange implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            log.fine("MovieFolderChange: Not yet implemented");
        }

    }

    public class MovieFolderClear implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".tga") || name.endsWith(".wav");
                }
            };

            String[] moviefiles = new File(settings.getMovieDir()).list(filter);

            for (int i = 0; i < moviefiles.length; ++i) {
                File tgafile = new File(settings.getMovieDir() + "\\" + moviefiles[i]);
                tgafile.setWritable(true);
                if (!tgafile.delete())
                    throw new IllegalArgumentException("Cannot delete file '" + moviefiles[i] + "'");
            }
        }

    }

    public class SaveSettings implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                setSettings();
                settings.save();
                settings.saveToCfg();
            } catch (Exception e1) {
                log.warning("A problem occurred while saving settings: " + e1);
            }
        }

        public void setSettings() {
            String[] resolution = ((String) view.getCmbResolution().getSelectedItem()).split("x");
            if (resolution.length == 2) {
                settings.setWidth(Integer.parseInt(resolution[0]));
                settings.setHeight(Integer.parseInt(resolution[1]));
            } else {
                throw new IllegalArgumentException("Bad resolution format");
            }
            String framerate = (String) view.getCmbFramerate().getSelectedItem();
            settings.setFramerate(Integer.parseInt(framerate));
            settings.setHud(Key.Hud.getAllowedValues().get(view.getCmbHud().getSelectedIndex()));
            settings.setViewmodelSwitch(Key.ViewmodelSwitch.getAllowedValues().get(
                    view.getCmbViewmodel().getSelectedIndex()));
            settings.setViewmodelFov((int) view.getSpinnerViewmodelFov().getValue());
            settings.setDxlevel(Key.DxLevel.getAllowedValues().get(
                    view.getCmbQuality().getSelectedIndex()));
            settings.setMotionBlur(view.getEnableMotionBlur().isSelected());
            settings.setCombattext(!view.getDisableCombatText().isSelected());
            settings.setCrosshair(!view.getDisableCrosshair().isSelected());
            settings.setCrosshairSwitch(!view.getDisableCrosshairSwitch().isSelected());
            settings.setHitsounds(!view.getDisableHitSounds().isSelected());
            settings.setVoice(!view.getDisableVoiceChat().isSelected());
        }
    }

    public class StartTfTask extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    view.getBtnStartTf().setEnabled(false);
                }
            });
            if (currentTask == null) {
                currentTask = this;

                // Restoring user files
                status.info("Restoring your files");
                files.restoreAll();

                // Saving ui settings to cfg files
                status.info("Saving settings and generating cfg files");
                try {
                    new SaveSettings().setSettings();
                    settings.saveToCfg();
                    movies.createMovienameCfgs();
                    movies.movieOffset();
                } catch (IOException e) {
                    log.log(Level.INFO, "A problem occurred while saving settings", e);
                    status.info("Problem while saving to settings file, check log for details");
                    return null;
                }

                // Backing up user files and copying lawena files
                status.info("Copying lawena files to cfg and custom");
                if (view.getCmbSkybox().getSelectedIndex() != 0) {
                    files.setSkyboxFilename((String) view.getCmbSkybox().getSelectedItem());
                }
                files.setHudName(settings.getHud());
                files.replaceAll();

                // Launching process
                status.info("Launching TF2 process");
                cl.startTf(settings.getWidth(), settings.getHeight(), settings.getDxlevel());

                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        view.getBtnStartTf().setEnabled(true);
                        view.getBtnStartTf().setText("Stop Team Fortress 2");
                    }
                });

                // Waiting up to 2 minutes for TF2 to start
                int timeout = 0;
                int maxtimeout = 40;
                int millis = 3000;
                status.info("Waiting for TF2 to start...");
                while (!cl.isTf2Running() && timeout < maxtimeout) {
                    Thread.sleep(millis);
                    ++timeout;
                }

                if (timeout >= maxtimeout) {
                    int s = timeout * (millis / 1000);
                    log.info("TF2 launch timed out after " + s + " seconds");
                    status.info("TF2 launch timed out, aborting");
                    return null;
                }

                log.fine("TF2 has started running");
                status.info("Waiting for TF2 to finish running...");
                while (cl.isTf2Running()) {
                    Thread.sleep(millis);
                }

            } else {
                if (cl.isTf2Running()) {
                    status.info("Attempting to finish TF2 process");
                    cl.killTf2Process();
                    if (!cl.isTf2Running()) {
                        currentTask.cancel(true);
                    }
                } else {
                    status.info("TF2 was not running, cancelling");
                }
            }

            return null;
        }

        @Override
        protected void done() {
            if (!isCancelled()) {
                currentTask = null;
                view.getBtnStartTf().setEnabled(false);
                files.restoreAll();
                cl.setSystemDxLevel(oDxlevel);
                view.getBtnStartTf().setText("Start Team Fortress 2");
                view.getBtnStartTf().setEnabled(true);
                status.info("");
            }
        }

    }

    public class Tf2FolderChange implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            log.fine("Tf2FolderChange: Not yet implemented");
        }

    }

    private static final Logger log = Logger.getLogger("lawena");
    private static final Logger status = Logger.getLogger("status");

    private static StartTfTask currentTask = null;

    private static ImageIcon createPreviewIcon(String imageName) {
        int size = 96;
        BufferedImage image;
        File input = new File(imageName);
        image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        try {
            image.createGraphics()
                    .drawImage(
                            ImageIO.read(input).getScaledInstance(size, size, Image.SCALE_SMOOTH),
                            0, 0, null);
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void registerValidation(JComboBox<String> combo, final String validationRegex,
            final JLabel label) {
        final JTextComponent tc = (JTextComponent) combo.getEditor().getEditorComponent();
        tc.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                validateInput();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validateInput();
            }

            private void validateInput() {
                if (tc.getText().matches(validationRegex)) {
                    label.setForeground(Color.BLACK);
                } else {
                    label.setForeground(Color.RED);
                }
            }
        });
    }

    private static void selectComboItem(JComboBox<String> combo, String selectedValue,
            List<String> possibleValues) {
        if (possibleValues == null || possibleValues.isEmpty()) {
            combo.setSelectedItem(selectedValue);
        } else {
            int i = possibleValues.indexOf(selectedValue);
            if (i >= 0) {
                combo.setSelectedIndex(i);
            }
        }
    }

    private LawenaView view;

    private SettingsManager settings;
    private MovieManager movies;
    private FileManager files;
    private DemoEditor vdm;
    private CommandLine cl;
    private CustomPathList customPaths;

    private HashMap<String, ImageIcon> skyboxMap;
    private JFileChooser choosemovie;
    private JFileChooser choosedir;
    private String steampath;
    private String oDxlevel;

    private String version;
    private String build;

    public Lawena() {
        try {
            version = this.getClass().getPackage().getImplementationVersion().split("-")[0];
        } catch (Exception e) {
            version = "4.0";
        }
        build = getManifestString("Implementation-Build", "0");
        String osname = System.getProperty("os.name");
        if (osname.contains("Windows")) {
            cl = new CLWindows();
            // } else if (osname.contains("Linux")) {
            // cl = new CLLinux();
            // } else if (osname.contains("OS X")) {
            // cl = new CLOSX();
        } else {
            throw new UnsupportedOperationException("OS not supported");
        }
        settings = new SettingsManager("settings.lwf");
        oDxlevel = cl.getSystemDxLevel();

        steampath = cl.getSteamPath();
        String tfdir = settings.getTfDir();
        Path tfpath;
        if (tfdir == null || tfdir.isEmpty()) {
            tfpath = Paths.get(steampath, "steamapps/common/team fortress 2/tf");
        } else {
            tfpath = Paths.get(tfdir);
        }
        if (!Files.exists(tfpath)) {
            tfpath = getTfPath();
            if (tfpath == null) {
                log.info("No tf directory specified, exiting.");
                System.exit(1);
            }
        }
        tfdir = tfpath.toString();
        settings.setTfDir(tfdir);
        files = new FileManager(tfdir, cl);

        String moviedir = settings.getMovieDir();
        Path moviepath = null;
        if (moviedir != null && !moviedir.isEmpty()) {
            moviepath = Paths.get(moviedir);
        }
        if (moviepath == null || !Files.exists(moviepath)) {
            moviepath = getMoviePath();
            if (moviepath == null) {
                log.info("No movie directory specified, exiting.");
                System.exit(1);
            }
        }
        moviedir = moviepath.toString();
        movies = new MovieManager(moviedir);
        settings.setMovieDir(moviedir);

        settings.save();
        files.restoreAll();

        customPaths = new CustomPathList();
        files.setCustomPathList(customPaths);

        vdm = new DemoEditor(settings);
    }

    private String getManifestString(String key, String defaultValue) {
        try {
            return new JarFile(new File(this.getClass().getProtectionDomain().getCodeSource()
                    .getLocation().toURI()))
                    .getManifest().getMainAttributes().getValue(key);
        } catch (IOException | URISyntaxException e) {
        }
        return defaultValue;
    }

    private SwingWorker<Map<String, ImageIcon>, Void> getSkyboxLoader(final List<String> data) {
        return new SwingWorker<Map<String, ImageIcon>, Void>() {

            @Override
            protected Map<String, ImageIcon> doInBackground() throws Exception {
                final Map<String, ImageIcon> map = new HashMap<>();
                try {
                    int i = 1;
                    for (String skybox : data) {
                        setProgress((int) (100 * ((double) i / data.size())));
                        status.fine("[" + getProgress() + "%] Generating skybox preview: " + skybox);
                        String img = "skybox/" + skybox + "up.png";
                        if (!Files.exists(Paths.get(img))) {
                            String filename = skybox + "up.vtf";
                            cl.extractIfNeeded(settings.getTfPath(), "custom/skybox.vpk",
                                    Paths.get("skybox"), filename);
                            cl.generatePreview(filename);
                        }
                        ImageIcon icon = createPreviewIcon(img);
                        map.put(skybox, icon);
                        i++;
                    }
                } catch (Exception e) {
                    log.log(Level.INFO, "Problem while loading skyboxes", e);
                }
                return map;
            }

            @Override
            protected void done() {
                try {
                    skyboxMap.putAll(get());
                    log.fine("Skybox loading and preview generation complete");
                } catch (CancellationException | InterruptedException | ExecutionException e) {
                    log.info("Skybox preview generator task was cancelled");
                }
                status.info("");
            }

        };
    }

    private void configureSkyboxes(final JComboBox<String> combo) {
        Vector<String> data = new Vector<>();
        Path dir = Paths.get("skybox");
        if (Files.exists(dir)) {
            log.fine("Loading skyboxes from folder");
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*up.vtf")) {
                for (Path path : stream) {
                    log.finer("Skybox found at: " + path);
                    String skybox = path.toFile().getName();
                    skybox = skybox.substring(0, skybox.indexOf("up.vtf"));
                    data.add(skybox);
                }
            } catch (IOException e) {
                log.log(Level.INFO, "Problem while loading skyboxes", e);
            }
        }
        Path vpk = Paths.get("custom/skybox.vpk");
        if (Files.exists(vpk)) {
            log.fine("Searching for skyboxes in " + vpk);
            for (String file : cl.getVpkContents(settings.getTfPath(), vpk)) {
                if (file.endsWith("up.vtf")) {
                    log.finer("[skybox.vpk] Skybox found at: " + file);
                    String skybox = file;
                    skybox = skybox.substring(0, skybox.indexOf("up.vtf"));
                    if (!data.contains(skybox)) {
                        data.add(skybox);
                    } else {
                        log.finer("Not adding because it already exists: " + skybox);
                    }
                }
            }
        }
        skyboxMap = new HashMap<>(data.size());
        getSkyboxLoader(new ArrayList<>(data)).execute();
        data.add(0, "Default");
        combo.setModel(new DefaultComboBoxModel<String>(data));
        combo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ImageIcon preview = skyboxMap.get(combo.getSelectedItem());
                view.getLblPreview().setText(preview == null ? "" : "Preview:");
                view.getLblSkyboxPreview().setIcon(preview);
            }
        });

    }

    private Path getMoviePath() {
        Path selected = null;
        int ret = 0;
        while ((selected == null && ret == 0) || (selected != null && !Files.exists(selected))) {
            choosemovie = new JFileChooser();
            choosemovie.setDialogTitle("Choose a directory to store your movie files");
            choosemovie.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            ret = choosemovie.showOpenDialog(null);
            if (ret == JFileChooser.APPROVE_OPTION) {
                selected = choosemovie.getSelectedFile().toPath();
            } else {
                selected = null;
            }
        }
        return selected;
    }

    private Path getTfPath() {
        Path selected = null;
        int ret = 0;
        while ((selected == null && ret == 0)
                || (selected != null && (!Files.exists(selected) || !selected.getFileName().equals(
                        "tf")))) {
            choosedir = new JFileChooser();
            choosedir.setDialogTitle("Choose your \"tf\" directory");
            choosedir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            choosedir.setCurrentDirectory(new File(steampath));
            ret = choosedir.showOpenDialog(null);
            if (ret == JFileChooser.APPROVE_OPTION) {
                selected = choosedir.getSelectedFile().toPath();
            } else {
                selected = null;
            }
        }
        return selected;
    }

    public void start() {
        view = new LawenaView();

        new StartLogger("lawena").toTextComponent(Level.FINE, view.getTextAreaLog());
        new StartLogger("status").toLabel(Level.FINE, view.getLblStatus());
        log.fine("Started lawena Recording Tool " + version + " build " + build);
        log.fine("TF2 path: " + settings.getTfPath());
        log.fine("Movie path: " + settings.getMovieDir());

        view.setTitle("lawena Recording Tool");
        try {
            view.setIconImage(new ImageIcon(Lawena.class.getClassLoader()
                    .getResource("ui/tf2.png"))
                    .getImage());
        } catch (Exception e) {
        }
        view.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                log.fine("Exiting");
                files.restoreAll();
                System.exit(0);
            }

        });

        registerValidation(view.getCmbResolution(), "[1-9][0-9]*x[1-9][0-9]*",
                view.getLblResolution());
        registerValidation(view.getCmbFramerate(), "[1-9][0-9]*",
                view.getLblFrameRate());
        selectComboItem(view.getCmbHud(), settings.getHud(),
                Arrays.asList("killnotices", "medic", "full", "custom"));
        selectComboItem(view.getCmbQuality(), settings.getDxlevel(),
                Arrays.asList("80", "81", "90", "95", "98"));
        selectComboItem(view.getCmbViewmodel(), settings.getViewmodelSwitch(),
                Arrays.asList("on", "off", "default"));

        view.getCmbResolution().setSelectedItem(settings.getWidth() + "x" + settings.getHeight());
        view.getCmbFramerate().setSelectedItem(settings.getFramerate() + "");
        try {
            view.getSpinnerViewmodelFov().setValue(settings.getViewmodelFov());
        } catch (IllegalArgumentException e) {
        }
        configureSkyboxes(view.getCmbSkybox());

        JTable table = view.getTableCustomContent();
        table.setModel(customPaths);
        table.getColumnModel().getColumn(0).setMaxWidth(20);
        table.setDefaultRenderer(CustomPath.class, new TooltipRenderer());

        // set remaining values
        view.getEnableMotionBlur().setSelected(settings.getMotionBlur());
        view.getDisableCombatText().setSelected(!settings.getCombattext());
        view.getDisableCrosshair().setSelected(!settings.getCrosshair());
        view.getDisableCrosshairSwitch().setSelected(!settings.getCrosshairSwitch());
        view.getDisableHitSounds().setSelected(!settings.getHitsounds());
        // view.getDisableSteamCloud().setSelected(!settings.getSteamCloud());
        view.getDisableVoiceChat().setSelected(!settings.getVoice());

        // register actions
        view.getMntmChangeTfDirectory().addActionListener(new Tf2FolderChange());
        view.getMntmChangeMovieDirectory().addActionListener(new MovieFolderChange());
        view.getBtnClearMovieFolder().addActionListener(new MovieFolderClear());
        view.getBtnSaveSettings().addActionListener(new SaveSettings());
        view.getBtnStartTf().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new StartTfTask().execute();
            }
        });

        DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {

            @Override
            public boolean accept(Path entry) throws IOException {
                return (Files.isDirectory(entry) || entry.toString().endsWith(".vpk"))
                        && !entry.getFileName().toString().equals("skybox.vpk");
            }
        };

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("custom"), filter)) {
            for (Path path : stream) {
                CustomPath cp = new CustomPath(path);
                cp.setSelected(true);
                cp.setContents(getDirectoryContents(path));
                customPaths.addPath(cp);
            }
        } catch (IOException e) {
            log.log(Level.INFO, "", e);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(settings.getTfDir(),
                "custom"), filter)) {
            for (Path path : stream) {
                CustomPath cp = new CustomPath(path);
                cp.setContents(getDirectoryContents(path));
                customPaths.addPath(cp);
            }
        } catch (IOException e) {
            log.log(Level.INFO, "", e);
        }

        view.getTabbedPane().addTab("VDM", null, vdm.start());

        view.setVisible(true);
    }

    private List<String> getDirectoryContents(Path start) {
        if (Files.isDirectory(start)) {
            final List<String> files = new ArrayList<>();
            try {
                Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        files.add(file.toString());
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException e)
                            throws IOException {
                        if (e == null) {
                            return FileVisitResult.CONTINUE;
                        } else {
                            throw e;
                        }
                    }
                });
            } catch (IOException e) {
                log.info("Problem while retrieving folder contents");
            }
            return files;
        } else {
            return cl.getVpkContents(settings.getTfPath(), start);
        }
    }
}
