
package ui;

import config.CLInterface;
import config.FileManager;
import config.MovieManager;
import config.SettingsManager;
import config.SimpleLog;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class LwrtGUI extends JFrame implements ActionListener {

    private static final Logger log = Logger.getLogger("lwrt");

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private JComboBox<String> resolution;
    private JComboBox<String> framerate;
    private JComboBox<String> hud;
    private JComboBox<String> viewmodelswitch;
    private JComboBox<String> dxlevel;
    private JComboBox<String> skybox;
    private JSlider viewmodelfov;
    private JCheckBox motionblur;
    private JCheckBox crosshairswitch;
    private JCheckBox crosshair;
    private JCheckBox combattext;
    private JCheckBox announcer;
    private JCheckBox domination;
    private JCheckBox hitsounds;
    private JCheckBox voice;
    private JCheckBox steamcloud;
    private JTextField height;
    private JTextField width;
    private JTextField fps;
    private JTextField selecteddemo;
    private JTextField tick1;
    private JTextField tick2;
    private JTable ticks;
    private DefaultTableModel model;
    private JButton deletemovies;
    private JButton start;
    private JButton save;
    private JButton addticks;
    private JButton browsedemos;
    private JButton createvdm;
    private JButton clearticks;
    private JButton clearvdm;
    private JButton skyboxpreview;

    private JTabbedPane tabbedpane;

    private JScrollPane ticksscrollpane;

    private JPanel settingspanel;
    private JPanel vdmpanel;
    private JPanel respanel;
    private JPanel respanel2;
    private JPanel fpspanel;
    private JPanel fpspanel2;
    private JPanel hudpanel;
    private JPanel viewmodelfovpanel;
    private JPanel savedeletepanel;
    private JPanel tickpanel;
    private JPanel demopanel;
    private JPanel createvdmpanel;
    private JPanel viewmodelswitchpanel;
    private JPanel dxlevelpanel;
    private JPanel skyboxpanel;

    private JFileChooser choosedir;
    private JFileChooser choosedemo;
    private JFileChooser choosemovie;

    private PreviewWindow previewwindow;
    private SettingsManager settings;
    private MovieManager movies;
    private FileManager files;
    private CLInterface cl;
    private TickList ticklist;
    private VDMGenerator vdmgenerator;

    private String oDxlevel;
    private String tfdir;
    private String moviedir;
    private String steampath;
    private String currentdemo;

    public void init() throws Exception {

        choosedir = new JFileChooser("C:\\");
        choosedemo = new JFileChooser("C:\\");
        choosemovie = new JFileChooser("C:\\");
        cl = new CLInterface();

        File dirfile = new File("tfdir.lwf");

        tfdir = "";
        moviedir = "";
        currentdemo = "";

        steampath = cl.regQuery("HKEY_CURRENT_USER\\Software\\Valve\\Steam", "SteamPath", 1);

        choosedir.setDialogTitle("Choose your \"tf\" directory");
        choosedir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        choosedir.setCurrentDirectory(new File(steampath));
        choosemovie.setDialogTitle("Choose a directory to store your movie files");
        choosemovie.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (dirfile.exists()) {
            BufferedReader br = new BufferedReader(new FileReader(dirfile));
            tfdir = br.readLine();
            moviedir = br.readLine();
            br.close();
        }

        while (!dirfile.exists() || !Files.exists(Paths.get(tfdir)) || tfdir.indexOf("tf") < 0) {
            int returnVal = choosedir.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                tfdir = choosedir.getSelectedFile().getPath();
                PrintWriter pw = new PrintWriter(new FileWriter("tfdir.lwf"));
                pw.println(tfdir);
                pw.close();
            }
            else {
                dispose();
                System.exit(0);
            }
        }

        while (moviedir == null || moviedir.isEmpty() || !Files.exists(Paths.get(moviedir))) {
            int returnVal = choosemovie.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                moviedir = choosemovie.getSelectedFile().getPath();
                PrintWriter pw = new PrintWriter(new FileWriter("tfdir.lwf", true));
                pw.println(moviedir);
                pw.close();
            }
            else {
                dispose();
                System.exit(0);
            }
        }

        choosedemo.setDialogTitle("Choose a demo file");
        choosedemo.setFileSelectionMode(JFileChooser.FILES_ONLY);
        choosedemo.setFileFilter(new FileNameExtensionFilter("Demo files", new String[] {
                "DEM"
        }));
        choosedemo.setCurrentDirectory(new File(tfdir));

        settings = new SettingsManager("settings.lwf");
        movies = new MovieManager(moviedir);
        files = new FileManager(tfdir);

        files.restoreAll();

        oDxlevel = cl.regQuery("HKEY_CURRENT_USER\\Software\\Valve\\Source\\tf\\Settings",
                "DXLevel_V1", 0);

        tabbedpane = new JTabbedPane();
        settingspanel = new JPanel();
        vdmpanel = new JPanel();

        settingspanel.setLayout(new GridLayout(10, 2));
        vdmpanel.setLayout(new GridLayout(4, 1));

        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith("up.vtf");
            }
        };

        String[] skyboxfiles = new File("Skybox").list(filter);
        String[] skyboxes = new String[skyboxfiles.length + 1];

        for (int i = 1; i < skyboxfiles.length + 1; ++i) {
            cl.generatePreview(skyboxfiles[i - 1]);
            skyboxes[i] = skyboxfiles[i - 1].substring(0, skyboxfiles[i - 1].indexOf("up.vtf"));
        }

        skyboxes[0] = "Default";

        String[] resolutions = {
                "640x360", "854x480", "1280x720", "1920x1080", "Custom Resolution"
        };
        String[] framerates = {
                "60", "120", "240", "480", "960", "1920", "3840", "Custom Frame Rate"
        };
        String[] huds = {
                "None (kill notices)", "Medic (hitpoints, ubercharge & cp)", "Full", "Custom HUD"
        };
        String[] viewmodeloptions = {
                "Always on", "Always off", "Default"
        };
        String[] dxleveloptions = {
                "Very low", "Low", "Medium", "High", "Very high"
        };
        String[] columnames = {
                "Demo name", "Starting Tick", "Ending Tick"
        };
        Object[][] tickdata = {};
        model = new DefaultTableModel(tickdata, columnames);

        height = new JTextField(4);
        width = new JTextField(4);
        fps = new JTextField(4);
        selecteddemo = new JTextField(40);
        tick1 = new JTextField(6);
        tick2 = new JTextField(6);
        ticks = new JTable(model);
        ticks.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int vColIndex = 0;
        TableColumn col = ticks.getColumnModel().getColumn(vColIndex);
        int columnwidth = 400;
        col.setPreferredWidth(columnwidth);
        ticks.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        ticks.setFillsViewportHeight(true);
        resolution = new JComboBox<>(resolutions);
        framerate = new JComboBox<>(framerates);
        viewmodelswitch = new JComboBox<>(viewmodeloptions);
        dxlevel = new JComboBox<>(dxleveloptions);
        skybox = new JComboBox<>(skyboxes);
        hud = new JComboBox<>(huds);
        resolution.setSelectedIndex(resChoice(settings.getWidth(), settings.getHeight()));

        ticksscrollpane = new JScrollPane(ticks);

        if (resChoice(settings.getWidth(), settings.getHeight()) == 4) {
            width.setText("" + settings.getWidth());
            height.setText("" + settings.getHeight());
        }

        framerate.setSelectedIndex(frameChoice(settings.getFramerate()));

        if (frameChoice(settings.getFramerate()) == 7) {
            fps.setText("" + settings.getFramerate());
        }

        hud.setSelectedIndex(hudChoice(settings.getHud()));
        viewmodelswitch.setSelectedIndex(viewmodelChoice(settings.getViewmodelSwitch()));
        dxlevel.setSelectedIndex(dxlevelChoice(settings.getDxlevel()));
        viewmodelfov = new JSlider(JSlider.HORIZONTAL, 55, 70, settings.getViewmodelFov());
        viewmodelfov.setMajorTickSpacing(5);
        viewmodelfov.setMinorTickSpacing(1);
        viewmodelfov.setPaintTicks(true);
        viewmodelfov.setPaintLabels(true);
        motionblur = new JCheckBox("Enable Motion Blur", settings.getMotionBlur());
        crosshairswitch = new JCheckBox("Disable Crosshair Switching in demos",
                !settings.getCrosshairSwitch());
        crosshair = new JCheckBox("Disable Crosshair", !settings.getCrosshair());
        combattext = new JCheckBox("Disable Combat Text", !settings.getCombattext());
        announcer = new JCheckBox("Disable Announcer Voice", !settings.getAnnouncer());
        domination = new JCheckBox("Disable Domination/Revenge Sounds", !settings.getDomination());
        hitsounds = new JCheckBox("Disable Hit Sounds", !settings.getHitsounds());
        voice = new JCheckBox("Disable Voice Chat", !settings.getVoice());
        steamcloud = new JCheckBox("Disable Steam Cloud (recommended)", !settings.getSteamCloud());
        save = new JButton("Save Settings");
        start = new JButton("Start Team Fortress 2");
        browsedemos = new JButton("Browse...");
        addticks = new JButton("Add");
        createvdm = new JButton("Create VDM Files");
        deletemovies = new JButton("Clear Movie Folder");
        clearticks = new JButton("Clear Tick List");
        clearvdm = new JButton("Delete VDM files");
        skyboxpreview = new JButton("Preview Skybox");
        height.setEditable(resolution.getSelectedIndex() == 4);
        width.setEditable(resolution.getSelectedIndex() == 4);
        fps.setEditable(framerate.getSelectedIndex() == 7);

        save.addActionListener(this);
        start.addActionListener(this);
        deletemovies.addActionListener(this);
        resolution.addActionListener(this);
        framerate.addActionListener(this);
        browsedemos.addActionListener(this);
        addticks.addActionListener(this);
        clearticks.addActionListener(this);
        createvdm.addActionListener(this);
        clearvdm.addActionListener(this);
        skyboxpreview.addActionListener(this);

        addWindowListener(new LwrtWindowListener());

        respanel = new JPanel();
        respanel2 = new JPanel();
        fpspanel = new JPanel();
        fpspanel2 = new JPanel();
        hudpanel = new JPanel();
        skyboxpanel = new JPanel();
        viewmodelfovpanel = new JPanel();
        viewmodelswitchpanel = new JPanel();
        dxlevelpanel = new JPanel();
        savedeletepanel = new JPanel();
        tickpanel = new JPanel();
        demopanel = new JPanel();
        createvdmpanel = new JPanel();

        respanel.add(new JLabel("Resolution: "));
        respanel.add(resolution);
        respanel2.add(new JLabel("Width: "));
        respanel2.add(width);
        respanel2.add(new JLabel("Height: "));
        respanel2.add(height);
        fpspanel.add(new JLabel("Frame Rate: "));
        fpspanel.add(framerate);
        fpspanel2.add(new JLabel("Custom Frame Rate: "));
        fpspanel2.add(fps);
        hudpanel.add(new JLabel("HUD: "));
        hudpanel.add(hud);
        viewmodelfovpanel.add(new JLabel("Viewmodel FOV: "));
        viewmodelfovpanel.add(viewmodelfov);
        savedeletepanel.add(save);
        savedeletepanel.add(deletemovies);
        tickpanel.add(new JLabel("Starting Tick:"));
        tickpanel.add(tick1);
        tickpanel.add(new JLabel("      "));
        tickpanel.add(new JLabel("Ending Tick:"));
        tickpanel.add(tick2);
        tickpanel.add(new JLabel("        "));
        tickpanel.add(addticks);
        demopanel.add(new JLabel("Select Demo File:"));
        demopanel.add(selecteddemo);
        demopanel.add(browsedemos);
        createvdmpanel.add(clearticks);
        createvdmpanel.add(createvdm);
        createvdmpanel.add(clearvdm);
        viewmodelswitchpanel.add(new JLabel("Viewmodels:"));
        viewmodelswitchpanel.add(viewmodelswitch);
        dxlevelpanel.add(new JLabel("Quality:"));
        dxlevelpanel.add(dxlevel);
        skyboxpanel.add(new JLabel("Skybox:"));
        skyboxpanel.add(skybox);
        skyboxpanel.add(skyboxpreview);

        settingspanel.add(respanel);
        settingspanel.add(respanel2);
        settingspanel.add(fpspanel);
        settingspanel.add(fpspanel2);
        settingspanel.add(dxlevelpanel);
        settingspanel.add(hudpanel);
        settingspanel.add(viewmodelfovpanel);
        settingspanel.add(skyboxpanel);
        settingspanel.add(motionblur);
        settingspanel.add(viewmodelswitchpanel);
        settingspanel.add(crosshairswitch);
        settingspanel.add(crosshair);
        settingspanel.add(combattext);
        settingspanel.add(announcer);
        settingspanel.add(domination);
        settingspanel.add(hitsounds);
        settingspanel.add(voice);
        settingspanel.add(steamcloud);
        settingspanel.add(savedeletepanel);
        settingspanel.add(start);
        vdmpanel.add(demopanel);
        vdmpanel.add(tickpanel);
        vdmpanel.add(createvdmpanel);
        vdmpanel.add(ticksscrollpane);
        // add(new JLabel("Made by Montz"));

        tabbedpane.addTab("Settings", settingspanel);
        tabbedpane.addTab("VDM", vdmpanel);

        add(tabbedpane);

        setTitle("lawena Recording Tool v3.1");
        pack();

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        setSize(dim.width / 2, dim.height / 2);
        int w = getSize().width;
        int h = getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;

        setLocation(x, y);
        setResizable(false);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == resolution) {
            height.setEditable(resolution.getSelectedIndex() == 4);
            width.setEditable(resolution.getSelectedIndex() == 4);
            return;
        }

        if (e.getSource() == framerate) {
            fps.setEditable(framerate.getSelectedIndex() == 7);
            return;
        }

        if (e.getSource() == deletemovies) {
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".tga") || name.endsWith(".wav");
                }
            };

            String[] moviefiles = new File(moviedir).list(filter);

            for (int i = 0; i < moviefiles.length; ++i) {
                File tgafile = new File(moviedir + "\\" + moviefiles[i]);
                tgafile.setWritable(true);
                if (!tgafile.delete())
                    throw new IllegalArgumentException("Cannot delete file '" + moviefiles[i] + "'");
            }
        }

        if (e.getSource() == save) {

            setSettings();

            try {
                settings.save();
                settings.saveToCfg();
            } catch (Exception e1) {
                log.log(Level.INFO, "Problem saving settings", e1);
            }
        }

        if (e.getSource() == browsedemos) {
            int returnVal = choosedemo.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                currentdemo = choosedemo.getSelectedFile().getName();
                selecteddemo.setText(currentdemo);
            }
        }

        if (e.getSource() == addticks) {
            if (!Files.exists(Paths.get(tfdir, selecteddemo.getText()))) {
                JOptionPane.showMessageDialog(this,
                        "Please fill the required demo file field with a valid demo file", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            else
                currentdemo = selecteddemo.getText();

            if (!checkIfNumber(tick1.getText()) || !checkIfNumber(tick2.getText())
                    || Integer.parseInt(tick1.getText()) >= Integer.parseInt(tick2.getText())) {
                JOptionPane.showMessageDialog(this,
                        "Please fill the required tick fields with valid numbers", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Object[] row = {
                    currentdemo, tick1.getText(), tick2.getText()
            };

            model.insertRow(ticks.getRowCount(), row);
        }

        if (e.getSource() == createvdm) {
            ticklist = generateTickList(0);
            vdmgenerator = new VDMGenerator(ticklist, tfdir);

            try {
                vdmgenerator.generate();
            } catch (IOException e1) {
                log.log(Level.INFO, "", e1);
            }
        }

        if (e.getSource() == clearticks) {
            while (model.getRowCount() > 0) {
                model.removeRow(0);
            }
        }

        if (e.getSource() == clearvdm) {
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".vdm");
                }
            };

            String[] vdmfiles = new File(tfdir).list(filter);

            System.gc();

            for (int i = 0; i < vdmfiles.length; ++i) {
                File vdmfile = new File(tfdir + "\\" + vdmfiles[i]);
                if (!vdmfile.delete())
                    throw new IllegalArgumentException("Cannot delete file '" + vdmfiles[i] + "'");
            }
        }

        if (e.getSource() == skyboxpreview) {
            if (previewwindow != null && previewwindow.isShowing())
                previewwindow.destroy();
            if (skybox.getSelectedIndex() == 0)
                return;
            previewwindow = new PreviewWindow("Skybox\\" + (String) skybox.getSelectedItem()
                    + "up.png");
            previewwindow.show();
        }

        if (e.getSource() == start) {

            if (!setSettings())
                return;

            try {
                settings.saveToCfg();
            } catch (Exception e1) {
                log.log(Level.INFO, "", e1);
            }

            setEnabled(false);
            setTitle("Replacing Config...");

            try {
                movies.createMovienameCfgs();
                movies.movieOffset();
            } catch (IOException e2) {
                log.log(Level.INFO, "", e2);
            }

            // backup entire custom folder and then replace with lawena stuff
            files.setReplaceVo(!settings.getAnnouncer());
            if (skybox.getSelectedIndex() != 0) {
                files.setSkyboxFilename((String) skybox.getSelectedItem());
            }
            files.setReplaceAnnouncer(!settings.getAnnouncer());
            files.setReplaceDomination(!settings.getDomination());
            files.setHudName(settings.getHud());
            files.replaceAll();

            setTitle("Starting TF2...");
            cl.startTf(settings.getWidth(), settings.getHeight(), steampath, settings.getDxlevel());
            int timeout = 0;
            while (!cl.isRunning("hl2.exe") && timeout < 40) {
                try {
                    Thread.sleep(3000);
                    ++timeout;
                } catch (Exception e1) {
                    log.log(Level.INFO, "", e1);
                }
            }
            
            setTitle("Running TF2...");
            while (cl.isRunning("hl2.exe")) {
                try {
                    Thread.sleep(3000);
                } catch (Exception e1) {
                    log.log(Level.INFO, "", e1);
                }
            }

            setTitle("Restoring Config...");
            files.restoreAll();
            cl.regedit("HKEY_CURRENT_USER\\Software\\Valve\\Source\\tf\\Settings", "DXLevel_V1",
                    oDxlevel);
            
            setEnabled(true);
            setTitle("lawena Recording Tool v3.1");
        }
    }

    private int resChoice(int width, int height) {
        switch (width) {
            case 640:
                if (height == 360)
                    return 0;
            case 854:
                if (height == 480)
                    return 1;
            case 1280:
                if (height == 720)
                    return 2;
            case 1920:
                if (height == 1080)
                    return 3;
            default:
                return 4;
        }
    }

    private int frameChoice(int fps) {
        switch (fps) {
            case 60:
                return 0;
            case 120:
                return 1;
            case 240:
                return 2;
            case 480:
                return 3;
            case 960:
                return 4;
            case 1920:
                return 5;
            case 3840:
                return 6;
            default:
                return 7;
        }
    }

    private int hudChoice(String hud) {
        if (hud.equals("killnotices"))
            return 0;
        if (hud.equals("medic"))
            return 1;
        if (hud.equals("full"))
            return 2;
        else
            return 3;
    }

    private int viewmodelChoice(String viewmodelswitch) {
        if (viewmodelswitch.equals("on"))
            return 0;
        if (viewmodelswitch.equals("off"))
            return 1;
        if (viewmodelswitch.equals("default"))
            return 2;
        else
            return 4;
    }

    public int dxlevelChoice(int dxlevel) {
        switch (dxlevel) {
            case 80:
                return 0;
            case 81:
                return 1;
            case 90:
                return 2;
            case 95:
                return 3;
            case 98:
                return 4;
            default:
                return 5;
        }
    }

    private boolean checkIfNumber(String in) {

        try {
            Integer.parseInt(in);
        } catch (NumberFormatException ex) {
            return false;
        }

        return true;
    }

    private boolean setSettings() {
        int index = resolution.getSelectedIndex();
        switch (index) {
            case 0:
                settings.setWidth(640);
                settings.setHeight(360);
                break;
            case 1:
                settings.setWidth(854);
                settings.setHeight(480);
                break;
            case 2:
                settings.setWidth(1280);
                settings.setHeight(720);
                break;
            case 3:
                settings.setWidth(1920);
                settings.setHeight(1080);
                break;
            case 4:
                if (!checkIfNumber(height.getText()) || !checkIfNumber(width.getText())) {
                    JOptionPane.showMessageDialog(this,
                            "Please fill the required resolution fields", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                settings.setWidth(Integer.parseInt(width.getText()));
                settings.setHeight(Integer.parseInt(height.getText()));
        }

        index = framerate.getSelectedIndex();
        switch (index) {
            case 0:
                settings.setFramerate(60);
                break;
            case 1:
                settings.setFramerate(120);
                break;
            case 2:
                settings.setFramerate(240);
                break;
            case 3:
                settings.setFramerate(480);
                break;
            case 4:
                settings.setFramerate(960);
                break;
            case 5:
                settings.setFramerate(1920);
                break;
            case 6:
                settings.setFramerate(3840);
                break;
            case 7:
                if (!checkIfNumber(fps.getText())) {
                    JOptionPane.showMessageDialog(this,
                            "Please fill the required frame rate field", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                settings.setFramerate(Integer.parseInt(fps.getText()));
        }

        index = hud.getSelectedIndex();
        switch (index) {
            case 0:
                settings.setHud("killnotices");
                break;
            case 1:
                settings.setHud("medic");
                break;
            case 2:
                settings.setHud("full");
                break;
            case 3:
                settings.setHud("custom");
                break;
        }

        index = viewmodelswitch.getSelectedIndex();
        switch (index) {
            case 0:
                settings.setViewmodelSwitch("on");
                break;
            case 1:
                settings.setViewmodelSwitch("off");
                break;
            case 2:
                settings.setViewmodelSwitch("default");
                break;
        }

        index = dxlevel.getSelectedIndex();
        switch (index) {
            case 0:
                settings.setDxlevel(80);
                break;
            case 1:
                settings.setDxlevel(81);
                break;
            case 2:
                settings.setDxlevel(90);
                break;
            case 3:
                settings.setDxlevel(95);
                break;
            case 4:
                settings.setDxlevel(98);
                break;
        }

        settings.setViewmodelFov(viewmodelfov.getValue());
        settings.setMotionBlur(motionblur.isSelected());
        settings.setCrosshairSwitch(!crosshairswitch.isSelected());
        settings.setCrosshair(!crosshair.isSelected());
        settings.setCombattext(!combattext.isSelected());
        settings.setAnnouncer(!announcer.isSelected());
        settings.setDomination(!domination.isSelected());
        settings.setHitsounds(!hitsounds.isSelected());
        settings.setVoice(!voice.isSelected());
        settings.setSteamCloud(!steamcloud.isSelected());

        return true;
    }

    public TickList generateTickList(int i) {
        TickList current;

        current = new TickList((String) model.getValueAt(i, 0), Integer.parseInt((String) model
                .getValueAt(i, 1)), Integer.parseInt((String) model.getValueAt(i, 2)));

        if (i + 1 == ticks.getRowCount())
            return current;

        current.setNext(generateTickList(i + 1));

        return current;
    }

    static public void main(String[] args) throws Exception {

        SimpleLog sl = new SimpleLog("lwrt");
        sl.startConsoleLog();
        sl.startShortLogfileOutput();
        log.info("Starting lawena Recording Tool v3.1");

        try {
            // Set System L&F
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error setting look and feel!");
            System.exit(1);
        }

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                LwrtGUI frame = new LwrtGUI();
                try {
                    frame.init();
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                frame.setVisible(true);
            }
        });
    }
}
