
package ui;

import config.CLInterface;
import config.FileManager;
import config.MovieManager;
import config.SettingsManager;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;

public class Lawena {

    private static final Logger log = Logger.getLogger("lwrt");

    private LawenaView view;
    private CLInterface cl;
    private JFileChooser choosedir;
    private JFileChooser choosedemo;
    private JFileChooser choosemovie;
    private String tfdir;
    private String moviedir;
    private String currentdemo;
    private String version;
    private String steampath;
    private SettingsManager settings;
    private MovieManager movies;
    private FileManager files;
    private String oDxlevel;
    private String osname = System.getProperty("os.name");

    private HashMap<String, ImageIcon> skyboxMap;

    public Lawena() {
        cl = new CLInterface();
        settings = new SettingsManager("settings.lwf");

        choosedir = new JFileChooser("C:\\");
        choosedemo = new JFileChooser("C:\\");
        choosemovie = new JFileChooser("C:\\");

        File dirfile = new File("tfdir.lwf");

        tfdir = settings.getTfDir();
        moviedir = settings.getMovieDir();
        currentdemo = "";

        try {
            version = this.getClass().getPackage().getImplementationVersion().split("-")[0];
        } catch (Exception e) {
            version = "";
        }

        steampath = cl.regQuery("HKEY_CURRENT_USER\\Software\\Valve\\Steam", "SteamPath", 1);

        choosedir.setDialogTitle("Choose your \"tf\" directory");
        choosedir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        choosedir.setCurrentDirectory(new File(steampath));
        choosemovie.setDialogTitle("Choose a directory to store your movie files");
        choosemovie.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        try {
            if (dirfile.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(dirfile));
                tfdir = br.readLine();
                moviedir = br.readLine();
                br.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (!dirfile.exists() || !Files.exists(Paths.get(tfdir))
                || !Paths.get(tfdir).toFile().getName().toString().equals("tf")) {
            int returnVal = choosedir.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                tfdir = choosedir.getSelectedFile().getPath();
                try {
                    PrintWriter pw = new PrintWriter(new FileWriter("tfdir.lwf"));
                    pw.println(tfdir);
                    pw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.exit(0);
            }
        }

        while (moviedir == null || moviedir.isEmpty() || !Files.exists(Paths.get(moviedir))) {
            int returnVal = choosemovie.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                moviedir = choosemovie.getSelectedFile().getPath();
                try {
                    PrintWriter pw = new PrintWriter(new FileWriter("tfdir.lwf", true));
                    pw.println(moviedir);
                    pw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.exit(0);
            }
        }

        // VDM
        choosedemo.setDialogTitle("Choose a demo file");
        choosedemo.setFileSelectionMode(JFileChooser.FILES_ONLY);
        choosedemo.setFileFilter(new FileNameExtensionFilter("Demo files", new String[] {
                "DEM"
        }));
        choosedemo.setCurrentDirectory(new File(tfdir));

        movies = new MovieManager(moviedir);
        files = new FileManager(tfdir);
        settings.setTfDir(tfdir);
        settings.setMovieDir(moviedir);
        settings.save();
        files.restoreAll();
        if (osname.contains("Windows")) {
            oDxlevel = cl.regQuery("HKEY_CURRENT_USER\\Software\\Valve\\Source\\tf\\Settings",
                    "DXLevel_V1", 0);
        }
    }

    public void start() {
        view = new LawenaView();
        registerValidation(view.getCmbResolution(), "[1-9][0-9]*x[1-9][0-9]*",
                view.getLblResolution());
        view.getCmbResolution().setSelectedItem(settings.getWidth() + "x" + settings.getHeight());
        registerValidation(view.getCmbFramerate(), "[1-9][0-9]*x[1-9][0-9]*",
                view.getLblFrameRate());
        view.getCmbFramerate().setSelectedItem(settings.getFramerate() + "");
        selectComboItem(view.getCmbHud(), settings.getHud(),
                Arrays.asList("killnotices", "medic", "full", "custom"));
        selectComboItem(view.getCmbQuality(), settings.getDxlevel(),
                Arrays.asList("80", "81", "90", "95", "98"));
        selectRadioItem(view.getButtonGroupViewmodels(), settings.getViewmodelSwitch(), Arrays.asList("on", "off", "default"));        
        try {
            view.getSpinnerViewmodelFov().setValue(settings.getViewmodelFov());
        } catch (IllegalArgumentException e) {
        }
        configureSkyboxes(view.getCmbSkybox());
        
        view.getEnableMotionBlur().setSelected(settings.getEnableMotionBlur());
        //view.getEnableParticles().setSelected(settings.getEnableParticles());
        view.getDisableAnnouncer().setSelected(settings.getDisableAnnouncer());
        view.getDisableCombatText().setSelected(settings.getDisableCombattext());
        view.getDisableCrosshair().setSelected(settings.getDisableCrosshair());
        view.getDisableCrosshairSwitch().setSelected(settings.getDisableCrosshairSwitch());
        view.getDisableDominationSounds().setSelected(settings.getDisableDomination());
        view.getDisableHitSounds().setSelected(settings.getDisableHitsounds());
        view.getDisableSteamCloud().setSelected(settings.getDisableSteamCloud());
        view.getDisableVoiceChat().setSelected(settings.getDisableVoice());
        
        view.setVisible(true);
    }
    
    private static void selectRadioItem(ButtonGroup group, String selectedValue,
            List<String> possibleValues) {
        List<AbstractButton> elements = Collections.list(group.getElements());
        if (possibleValues != null && !possibleValues.isEmpty()) {
            int i = possibleValues.indexOf(selectedValue);
            if (i >= 0) {
                group.setSelected(elements.get(i).getModel(), true);
            }
        }
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

    private static void registerValidation(JComboBox<String> combo, final String validationRegex,
            final JLabel label) {
        final JTextComponent tc = (JTextComponent) combo.getEditor().getEditorComponent();
        tc.getDocument().addDocumentListener(new DocumentListener() {

            private void validateInput() {
                if (tc.getText().matches(validationRegex)) {
                    label.setForeground(Color.BLACK);
                } else {
                    label.setForeground(Color.RED);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validateInput();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                validateInput();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
    }

    private void configureSkyboxes(final JComboBox<String> combo) {
        skyboxMap = new HashMap<>();
        Vector<String> data = new Vector<>();
        // add default skybox option
        data.add("Default");
        skyboxMap.put("Default", null);
        // load skyboxes from folder
        Path dir = Paths.get("skybox");
        if (Files.exists(dir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*up.vtf")) {
                for (Path path : stream) {
                    String skybox = path.toFile().getName();
                    cl.generatePreview(skybox);
                    skybox = skybox.substring(0, skybox.indexOf("up.vtf"));
                    data.add(skybox);
                    ImageIcon icon = createPreviewIcon("skybox\\" + skybox + "up.png");
                    skyboxMap.put(skybox, icon);
                }
            } catch (IOException e) {
                log.log(Level.INFO, "Problem while loading skyboxes", e);
            }
        }

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

    private static ImageIcon createPreviewIcon(String imageName) {
        int size = 64;
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

}
