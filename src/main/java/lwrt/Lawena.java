
package lwrt;

import ui.AboutDialog;
import ui.LawenaView;
import ui.TooltipRenderer;
import util.ListFilesVisitor;
import util.StartLogger;
import vdm.DemoEditor;

import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;

import lwrt.CustomPath.PathContents;
import lwrt.SettingsManager.Key;

public class Lawena {

    public class MovieFolderChange implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (startTfTask == null) {
                Path newpath = getChosenMoviePath();
                if (newpath != null) {
                    settings.setMoviePath(newpath);
                }
            } else {
                JOptionPane.showMessageDialog(view, "Please wait until TF2 has stopped running");
            }
        }

    }

    public class ClearMoviesTask extends SwingWorker<Void, Path> {

        private int count = 0;

        @Override
        protected Void doInBackground() throws Exception {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    view.getBtnClearMovieFolder().setEnabled(false);
                }
            });
            if (clearMoviesTask == null) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                        settings.getMoviePath(),
                        "*.{tga,wav}")) {

                    clearMoviesTask = this;
                    setCurrentWorker(this, true);
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            view.getBtnClearMovieFolder().setEnabled(true);
                            view.getBtnClearMovieFolder().setText("Stop Clearing");
                        }
                    });

                    for (Path path : stream) {
                        if (isCancelled()) {
                            break;
                        }
                        path.toFile().setWritable(true);
                        Files.delete(path);
                        publish(path);
                    }

                } catch (IOException ex) {
                    log.log(Level.INFO, "Problem while clearing movie folder", ex);
                }
            } else {
                log.fine("Cancelling movie folder clearing task");
                status.info("Cancelling task");
                clearMoviesTask.cancel(true);
            }

            return null;
        }

        @Override
        protected void process(List<Path> chunks) {
            count += chunks.size();
            status.info("Deleting " + count + " files from movie folder...");
        };

        @Override
        protected void done() {
            if (!isCancelled()) {
                clearMoviesTask = null;
                setCurrentWorker(null, false);
                if (count > 0) {
                    log.fine("Movie folder cleared: " + count + " files deleted");
                } else {
                    log.fine("Movie folder already clean, no files deleted");
                }
                view.getBtnClearMovieFolder().setEnabled(true);
                view.getBtnClearMovieFolder().setText("Clear Movie Files");
                status.info("");
            }
        };

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
            if (startTfTask == null) {
                startTfTask = this;
                setCurrentWorker(this, false);
                setProgress(0);

                // Restoring user files
                status.info("Restoring your files");
                files.restoreAll();
                setProgress(25);

                // Saving ui settings to cfg files
                status.info("Saving settings and generating cfg files");
                try {
                    saveSettings();
                    settings.saveToCfg();
                    movies.createMovienameCfgs();
                    movies.movieOffset();
                } catch (IOException e) {
                    log.log(Level.INFO, "Problem while saving settings to file", e);
                    status.info("Launch aborted: Problem while saving settings to file");
                    return null;
                }
                setProgress(50);

                // Backing up user files and copying lawena files
                status.info("Copying lawena files to cfg and custom");
                files.replaceAll();
                setProgress(75);

                // Launching process
                status.info("Launching TF2 process");
                cl.startTf(settings);

                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        view.getBtnStartTf().setEnabled(true);
                        view.getBtnStartTf().setText("Stop Team Fortress 2");
                    }
                });
                setProgress(100);

                // Waiting up to 2 minutes for TF2 to start
                int timeout = 0;
                int maxtimeout = 40;
                int millis = 3000;
                setProgress(0);
                status.info("Waiting for TF2 to start...");
                while (!cl.isRunningTF2() && timeout < maxtimeout) {
                    setProgress((int) ((double) timeout / maxtimeout * 100));
                    Thread.sleep(millis);
                    ++timeout;
                }

                if (timeout >= maxtimeout) {
                    int s = timeout * (millis / 1000);
                    log.info("TF2 launch timed out after " + s + " seconds");
                    status.info("Launch aborted: TF2 did not start after " + s + " seconds");
                    return null;
                }

                log.fine("TF2 has started running");
                status.info("Waiting for TF2 to finish running...");
                view.getProgressBar().setIndeterminate(true);
                while (cl.isRunningTF2()) {
                    Thread.sleep(millis);
                }

            } else {
                if (cl.isRunningTF2()) {
                    status.info("Attempting to finish TF2 process");
                    cl.killTf2Process();
                    if (!cl.isRunningTF2()) {
                        startTfTask.cancel(true);
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
                startTfTask = null;
                setCurrentWorker(null, false);
                view.getBtnStartTf().setEnabled(false);
                files.restoreAll();
                cl.setSystemDxLevel(oDxlevel);
                view.getBtnStartTf().setText("Start Team Fortress 2");
                view.getBtnStartTf().setEnabled(true);
                status.info("");
            }
        }

    }

    public class PathScanTask extends SwingWorker<Void, Void> {
        @Override
        protected Void doInBackground() throws Exception {
            try {
                scan();
            } catch (Exception e) {
                log.log(Level.INFO, "Problem while scanning custom paths", e);
            }
            return null;
        }

        private void scan() {
            customPaths.clear();
            customPaths.addPaths(Paths.get("custom"), settings.getTfPath().resolve("custom"));
            customPaths.validateRequired();
            int i = 0;
            for (CustomPath cp : customPaths.getList()) {
                EnumSet<PathContents> c = cp.getContents();
                Path path = cp.getPath();
                if (!c.contains(PathContents.READONLY)) {
                    List<String> files = getContentsList(path);
                    boolean hud = false;
                    boolean cfg = false;
                    boolean sky = false;
                    for (String file : files) {
                        if (hud && cfg & sky) {
                            break;
                        }
                        if (!hud && (file.startsWith("resource/") || file.startsWith("scripts/"))) {
                            c.add(PathContents.HUD);
                            hud = true;
                        } else if (!cfg && file.startsWith("cfg/") && file.endsWith(".cfg")) {
                            c.add(PathContents.CONFIG);
                            cfg = true;
                        } else if (!sky && file.startsWith("materials/skybox/")) {
                            c.add(PathContents.SKYBOX);
                            sky = true;
                        }
                    }
                    if (hud || cfg || sky) {
                        customPaths.fireTableRowsUpdated(i, i);
                    }
                }
                i++;
            }
        }

        private List<String> getContentsList(Path path) {
            if (path.toString().endsWith(".vpk")) {
                return cl.getVpkContents(settings.getTfPath(), path);
            } else if (Files.isDirectory(path)) {
                ListFilesVisitor visitor = new ListFilesVisitor(path);
                try {
                    Set<FileVisitOption> set = new HashSet<>();
                    set.add(FileVisitOption.FOLLOW_LINKS);
                    Files.walkFileTree(path, set, 3, visitor);
                    return visitor.getFiles();
                } catch (IllegalArgumentException | IOException e) {
                    log.log(Level.INFO, "Could not walk through this directory: " + path, e);
                }
            }
            return Collections.emptyList();
        }

        @Override
        protected void done() {
            loadResourceSettings();
        }
    }

    public class SkyboxPreviewTask extends SwingWorker<Map<String, ImageIcon>, Void> {

        private List<String> data;

        public SkyboxPreviewTask(List<String> data) {
            this.data = data;
        }

        @Override
        protected Map<String, ImageIcon> doInBackground() throws Exception {
            setCurrentWorker(this, false);
            setProgress(0);
            final Map<String, ImageIcon> map = new HashMap<>();
            try {
                int i = 1;
                for (String skybox : data) {
                    setProgress((int) (100 * ((double) i / data.size())));
                    status.fine("Generating skybox preview: " + skybox);
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
                selectSkyboxFromSettings();
                log.fine("Skybox loading and preview generation complete");
            } catch (CancellationException | InterruptedException | ExecutionException e) {
                log.info("Skybox preview generator task was cancelled");
            }
            status.info("");
            if (!isCancelled()) {
                setCurrentWorker(null, false);
            }
        }

    }

    public class Tf2FolderChange implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (startTfTask == null) {
                Path newpath = getChosenTfPath();
                if (newpath != null) {
                    settings.setTfPath(newpath);
                    new PathScanTask().execute();
                }
            } else {
                JOptionPane.showMessageDialog(view, "Please wait until TF2 has stopped running");
            }
        }

    }

    private static final Logger log = Logger.getLogger("lawena");
    private static final Logger status = Logger.getLogger("status");

    private static StartTfTask startTfTask = null;
    private static ClearMoviesTask clearMoviesTask = null;

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

    private static String now(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(Calendar.getInstance().getTime());
    }

    private LawenaView view;

    private SettingsManager settings;
    private MovieManager movies;
    private FileManager files;
    private DemoEditor vdm;
    private CommandLine cl;
    private CustomPathList customPaths;
    private AboutDialog dialog;

    private HashMap<String, ImageIcon> skyboxMap;
    private JFileChooser choosemovie;
    private JFileChooser choosedir;
    private Path steampath;
    private String oDxlevel;

    private String version = "4.0-SNAPSHOT";
    private String build;

    public Lawena() {
        String impl = this.getClass().getPackage().getImplementationVersion();
        if (impl != null) {
            version = impl;
        }
        build = getManifestString("Implementation-Build", now("yyyyMMddHHmmss"));
        String osname = System.getProperty("os.name");
        if (osname.contains("Windows")) {
            cl = new CLWindows();
        } else if (osname.contains("Linux")) {
            cl = new CLLinux();
        } else if (osname.contains("OS X")) {
            cl = new CLOSX();
        } else {
            throw new UnsupportedOperationException("OS not supported");
        }
        cl.setLookAndFeel();

        settings = new SettingsManager("settings.lwf");
        oDxlevel = cl.getSystemDxLevel();
        steampath = cl.getSteamPath();
        Path tfpath = settings.getTfPath();
        if (tfpath == null || tfpath.toString().isEmpty()) {
            tfpath = steampath.resolve("SteamApps/common/Team Fortress 2/tf");
        }
        if (!Files.exists(tfpath)) {
            tfpath = getChosenTfPath();
            if (tfpath == null) {
                log.info("No tf directory specified, exiting.");
                System.exit(1);
            }
        }
        settings.setTfPath(tfpath);
        files = new FileManager(settings, cl);

        Path moviepath = settings.getMoviePath();
        if (moviepath == null || moviepath.toString().isEmpty() || !Files.exists(moviepath)) {
            moviepath = getChosenMoviePath();
            if (moviepath == null) {
                log.info("No movie directory specified, exiting.");
                System.exit(1);
            }
        }
        movies = new MovieManager(moviepath.toString());
        settings.setMoviePath(moviepath);

        settings.save();
        files.restoreAll();

        customPaths = new CustomPathList();
        files.setCustomPathList(customPaths);

        vdm = new DemoEditor(settings);
    }

    private String getManifestString(String key, String defaultValue) {
        try (JarFile jar = new JarFile(new File(this.getClass().getProtectionDomain()
                .getCodeSource().getLocation().toURI()))) {
            String value = jar.getManifest().getMainAttributes()
                    .getValue(key);
            return (value == null ? "bat." + defaultValue : value);
        } catch (IOException | URISyntaxException e) {
        }
        return "custom." + defaultValue;
    }

    public void start() {
        view = new LawenaView();

        new StartLogger("lawena").toTextComponent(Level.FINE, view.getTextAreaLog());
        new StartLogger("status").toLabel(Level.FINE, view.getLblStatus());
        log.fine("Started lawena Recording Tool " + version + " build " + build);
        log.fine("TF2 path: " + settings.getTfPath());
        log.fine("Movie path: " + settings.getMoviePath());

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
                saveAndExit();
            }

        });
        view.getMntmAbout().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (dialog == null) {
                    dialog = new AboutDialog(version, build);
                    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    dialog.setModalityType(ModalityType.APPLICATION_MODAL);
                }
                dialog.setVisible(true);
            }
        });

        JTable table = view.getTableCustomContent();
        table.setModel(customPaths);
        table.getColumnModel().getColumn(0).setMaxWidth(20);
        table.setDefaultRenderer(CustomPath.class, new TooltipRenderer(settings));
        TableRowSorter<CustomPathList> sorter = new TableRowSorter<>(customPaths);
        table.setRowSorter(sorter);
        RowFilter<CustomPathList, Object> filter = new RowFilter<CustomPathList, Object>() {
            public boolean include(Entry<? extends CustomPathList, ? extends Object> entry) {
                CustomPath cp = (CustomPath) entry.getValue(CustomPathList.Column.PATH.ordinal());
                return !cp.getContents().contains(PathContents.READONLY);
            }
        };
        sorter.setRowFilter(filter);
        new PathScanTask().execute();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    configureSkyboxes(view.getCmbSkybox());
                } catch (Exception e) {
                    log.log(Level.INFO, "Problem while configuring skyboxes", e);
                }
                return null;
            }

            @Override
            protected void done() {
                selectSkyboxFromSettings();
            };
        }.execute();

        loadSettings();

        view.getMntmChangeTfDirectory().addActionListener(new Tf2FolderChange());
        view.getMntmChangeMovieDirectory().addActionListener(new MovieFolderChange());
        view.getMntmRevertToDefault().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                settings.loadDefaults();
                loadSettings();
                loadResourceSettings();
                saveSettings();
            }
        });
        view.getMntmExit().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                saveAndExit();
            }
        });
        view.getBtnSaveSettings().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                saveSettings();
            }
        });
        view.getBtnStartTf().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new StartTfTask().execute();
            }
        });
        view.getBtnClearMovieFolder().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new ClearMoviesTask().execute();
            }
        });

        view.getTabbedPane().addTab("VDM", null, vdm.start());
        view.setVisible(true);
    }

    private void loadSettings() {
        registerValidation(view.getCmbResolution(), "[1-9][0-9]*x[1-9][0-9]*",
                view.getLblResolution());
        registerValidation(view.getCmbFramerate(), "[1-9][0-9]*",
                view.getLblFrameRate());
        selectComboItem(view.getCmbHud(), settings.getHud(),
                Key.Hud.getAllowedValues());
        selectComboItem(view.getCmbQuality(), settings.getDxlevel(),
                Key.DxLevel.getAllowedValues());
        selectComboItem(view.getCmbViewmodel(), settings.getViewmodelSwitch(),
                Key.ViewmodelSwitch.getAllowedValues());
        selectSkyboxFromSettings();
        view.getCmbResolution().setSelectedItem(settings.getWidth() + "x" + settings.getHeight());
        view.getCmbFramerate().setSelectedItem(settings.getFramerate() + "");
        try {
            view.getSpinnerViewmodelFov().setValue(settings.getViewmodelFov());
        } catch (IllegalArgumentException e) {
        }
        view.getEnableMotionBlur().setSelected(settings.getMotionBlur());
        view.getDisableCombatText().setSelected(!settings.getCombattext());
        view.getDisableCrosshair().setSelected(!settings.getCrosshair());
        view.getDisableCrosshairSwitch().setSelected(!settings.getCrosshairSwitch());
        view.getDisableHitSounds().setSelected(!settings.getHitsounds());
        view.getDisableVoiceChat().setSelected(!settings.getVoice());
        view.getChckbxUsecondebug().setSelected(settings.getCondebug());
    }

    private void loadResourceSettings() {
        List<String> selected = settings.getCustomResources();
        Path tfpath = settings.getTfPath();
        int i = 0;
        for (CustomPath cp : customPaths.getList()) {
            Path path = cp.getPath();
            if (!cp.getContents().contains(PathContents.READONLY) && !selected.isEmpty()) {
                String key = (path.startsWith(tfpath) ? "tf*" : "");
                key += path.getFileName().toString();
                cp.setSelected(selected.contains(key));
                customPaths.fireTableRowsUpdated(i, i);
            }
            i++;
        }
    }

    private void saveSettings() {
        String[] resolution = ((String) view.getCmbResolution().getSelectedItem()).split("x");
        if (resolution.length == 2) {
            settings.setWidth(Integer.parseInt(resolution[0]));
            settings.setHeight(Integer.parseInt(resolution[1]));
        } else {
            log.fine("Bad resolution format, reverting to previously saved");
            view.getCmbResolution().setSelectedItem(
                    settings.getWidth() + "x" + settings.getHeight());
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
        settings.setSkybox((String) view.getCmbSkybox().getSelectedItem());
        settings.setCondebug(view.getChckbxUsecondebug().isSelected());
        Path tfpath = settings.getTfPath();
        List<String> selected = new ArrayList<>();
        for (CustomPath cp : customPaths.getList()) {
            Path path = cp.getPath();
            if (!cp.getContents().contains(PathContents.READONLY) && cp.isSelected()) {
                String key = (path.startsWith(tfpath) ? "tf*" : "");
                key += path.getFileName().toString();
                selected.add(key);
            }
        }
        settings.setCustomResources(selected);
        settings.save();
        log.fine("Settings saved");
    }

    private void saveAndExit() {
        saveSettings();
        files.restoreAll();
        System.exit(0);
    }

    private void configureSkyboxes(final JComboBox<String> combo) {
        final Vector<String> data = new Vector<>();
        Path dir = Paths.get("skybox");
        if (Files.exists(dir)) {
            log.finer("Loading skyboxes from folder");
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
            log.finer("Searching for skyboxes in " + vpk);
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
        new SkyboxPreviewTask(new ArrayList<>(data)).execute();
        data.add(0, (String) Key.Skybox.defValue());
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

    private void selectSkyboxFromSettings() {
        view.getCmbSkybox().setSelectedItem(settings.getSkybox());
    }

    private Path getChosenMoviePath() {
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

    private Path getChosenTfPath() {
        Path selected = null;
        int ret = 0;
        while ((selected == null && ret == 0)
                || (selected != null && (!Files.exists(selected) || !selected.toFile().getName()
                        .toString().equals("tf")))) {
            choosedir = new JFileChooser();
            choosedir.setDialogTitle("Choose your \"tf\" directory");
            choosedir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            choosedir.setCurrentDirectory(steampath.toFile());
            choosedir.setFileHidingEnabled(false);
            ret = choosedir.showOpenDialog(null);
            if (ret == JFileChooser.APPROVE_OPTION) {
                selected = choosedir.getSelectedFile().toPath();
            } else {
                selected = null;
            }
            log.finer("Selected path: " + selected);
        }
        return selected;
    }

    private void setCurrentWorker(final SwingWorker<?, ?> worker, final boolean indeterminate) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (worker != null) {
                    view.getProgressBar().setVisible(true);
                    view.getProgressBar().setIndeterminate(indeterminate);
                    view.getProgressBar().setValue(0);
                    worker.addPropertyChangeListener(new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            if ("progress".equals(evt.getPropertyName())) {
                                view.getProgressBar().setValue((Integer) evt.getNewValue());
                            }
                        }
                    });
                } else {
                    view.getProgressBar().setVisible(false);
                    view.getProgressBar().setIndeterminate(indeterminate);
                    view.getProgressBar().setValue(0);
                }
            }
        });

    }

}
