
package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JCheckBoxMenuItem;

public class LawenaView extends JFrame {

    private static final Logger log = Logger.getLogger("lawena");

    private class MntmRenderingTutorialActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            new SwingWorker<Void, Void>() {
                protected Void doInBackground() throws Exception {
                    try {
                        String url = "http://code.google.com/p/lawenarecordingtool/wiki/RenderingTutorial";
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (IOException | URISyntaxException e1) {
                        log.log(Level.INFO, "Could not open URL", e1);
                    }
                    return null;
                }
            }.execute();
        }
    }

    private class MntmPatchNotesActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            new SwingWorker<Void, Void>() {
                protected Void doInBackground() throws Exception {
                    try {
                        String url = "https://github.com/iabarca/lawena-recording-tool/commits/master";
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (IOException | URISyntaxException e1) {
                        log.log(Level.INFO, "Could not open URL", e1);
                    }
                    return null;
                }
            }.execute();
        }
    }

    private class MntmProjectPageActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            new SwingWorker<Void, Void>() {
                protected Void doInBackground() throws Exception {
                    try {
                        String url = "http://code.google.com/p/lawenarecordingtool/";
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (IOException | URISyntaxException e1) {
                        log.log(Level.INFO, "Could not open URL", e1);
                    }
                    return null;
                }
            }.execute();
        }
    }

    private class MntmVdmTutorialActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            new SwingWorker<Void, Void>() {
                protected Void doInBackground() throws Exception {
                    try {
                        String url = "http://code.google.com/p/lawenarecordingtool/wiki/VDMtutorial";
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (IOException | URISyntaxException e1) {
                        log.log(Level.INFO, "Could not open URL", e1);
                    }
                    return null;
                }
            }.execute();
        }
    }

    private class MntmInstructionsActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            new SwingWorker<Void, Void>() {
                protected Void doInBackground() throws Exception {
                    try {
                        String url = "http://code.google.com/p/lawenarecordingtool/wiki/Instructions";
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (IOException | URISyntaxException e1) {
                        log.log(Level.INFO, "Could not open URL", e1);
                    }
                    return null;
                }
            }.execute();
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private JComboBox<String> cmbSkybox;
    private JComboBox<String> cmbResolution;
    private JComboBox<String> cmbHud;
    private JComboBox<String> cmbFramerate;
    private JComboBox<String> cmbQuality;
    private JSpinner spinnerViewmodelFov;
    private JCheckBox enableMotionBlur;
    private JCheckBox disableCrosshair;
    private JCheckBox disableCrosshairSwitch;
    private JCheckBox disableCombatText;
    private JCheckBox disableHitSounds;
    private JCheckBox disableVoiceChat;
    private JButton btnStartTf;
    private JLabel lblResolution;
    private JLabel lblFrameRate;
    private JButton btnClearMovieFolder;
    private JPanel panelBottomLeft;
    private JLabel lblSkyboxPreview;
    private JTable tableCustomContent;
    private JScrollPane scrollPane;
    private JScrollPane scrollPane_2;
    private JTextArea textAreaLog;
    private JTabbedPane tabbedPane;
    private JMenuBar menuBar;
    private JMenu mnFile;
    private JMenuItem mntmChangeTfDirectory;
    private JMenuItem mntmChangeMovieDirectory;
    private JPanel panelStatusbar;
    private JLabel lblStatus;
    private JPanel panelCheckboxes;
    private JPanel panelCustomContent;
    private JComboBox<String> cmbViewmodel;
    private Component verticalStrut;
    private JLabel lblPreview;
    private Component horizontalStrut;
    private JPanel panelBottomRight;
    private JProgressBar progressBar;
    private JSeparator separator;
    private JMenuItem mntmRevertToDefault;
    private JSeparator separator_1;
    private JMenuItem mntmExit;
    private JMenu mnHelp;
    private JMenuItem mntmInstructions;
    private JMenuItem mntmVdmTutorial;
    private JMenuItem mntmProjectPage;
    private JMenuItem mntmPatchNotes;
    private JSeparator separator_2;
    private JMenuItem mntmAbout;
    private JMenuItem mntmRenderingTutorial;
    private JMenuItem mntmSaveSettings;
    private JCheckBox useHudMinmode;
    private JMenu mnAdvanced;
    private JMenuItem mntmSelectEnhancedParticles;
    private JCheckBoxMenuItem chckbxmntmInsecure;
    private JMenuItem mntmChangeTfLaunch;
    private JSeparator separator_3;
    private JMenuItem mntmOpenCustomFolder;
    private JMenuItem mntmOpenMovieFolder;
    private JSeparator separator_4;

    /**
     * Create the frame.
     */
    public LawenaView() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        mnFile = new JMenu(" File ");
        menuBar.add(mnFile);

        mntmChangeTfDirectory = new JMenuItem("Change TF2 Folder...");
        mnFile.add(mntmChangeTfDirectory);

        mntmChangeMovieDirectory = new JMenuItem("Change Movie Folder...");
        mnFile.add(mntmChangeMovieDirectory);

        separator_4 = new JSeparator();
        mnFile.add(separator_4);

        mntmOpenMovieFolder = new JMenuItem("Open Movie Folder");
        mnFile.add(mntmOpenMovieFolder);

        mntmOpenCustomFolder = new JMenuItem("Open Custom Folder");
        mnFile.add(mntmOpenCustomFolder);

        separator = new JSeparator();
        mnFile.add(separator);

        mntmSaveSettings = new JMenuItem("Save Settings");
        mntmSaveSettings
                .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        mnFile.add(mntmSaveSettings);

        mntmRevertToDefault = new JMenuItem("Revert to Default Settings");
        mnFile.add(mntmRevertToDefault);

        separator_1 = new JSeparator();
        mnFile.add(separator_1);

        mntmExit = new JMenuItem("Exit");
        mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
        mnFile.add(mntmExit);

        mnAdvanced = new JMenu(" Advanced ");
        menuBar.add(mnAdvanced);

        mntmSelectEnhancedParticles = new JMenuItem("Select Enhanced Particles...");
        mnAdvanced.add(mntmSelectEnhancedParticles);

        separator_3 = new JSeparator();
        mnAdvanced.add(separator_3);

        chckbxmntmInsecure = new JCheckBoxMenuItem("Use -insecure in launch options");
        mnAdvanced.add(chckbxmntmInsecure);

        mntmChangeTfLaunch = new JMenuItem("Change TF2 launch timeout...");
        mnAdvanced.add(mntmChangeTfLaunch);

        mnHelp = new JMenu(" Help ");
        menuBar.add(mnHelp);

        mntmInstructions = new JMenuItem("Instructions and FAQ");
        mntmInstructions.addActionListener(new MntmInstructionsActionListener());
        mnHelp.add(mntmInstructions);

        mntmVdmTutorial = new JMenuItem("VDM Tutorial");
        mntmVdmTutorial.addActionListener(new MntmVdmTutorialActionListener());

        mntmRenderingTutorial = new JMenuItem("Rendering Tutorial");
        mntmRenderingTutorial.addActionListener(new MntmRenderingTutorialActionListener());
        mnHelp.add(mntmRenderingTutorial);
        mnHelp.add(mntmVdmTutorial);

        mntmProjectPage = new JMenuItem("Project Page");
        mntmProjectPage.addActionListener(new MntmProjectPageActionListener());
        mnHelp.add(mntmProjectPage);

        mntmPatchNotes = new JMenuItem("Patch Notes");
        mntmPatchNotes.addActionListener(new MntmPatchNotesActionListener());
        mnHelp.add(mntmPatchNotes);

        separator_2 = new JSeparator();
        mnHelp.add(separator_2);

        mntmAbout = new JMenuItem("About");
        mnHelp.add(mntmAbout);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(5, 5));
        contentPane.setPreferredSize(new Dimension(650, 400));
        setContentPane(contentPane);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        contentPane.add(tabbedPane, BorderLayout.CENTER);

        JPanel panelSettings = new JPanel();
        tabbedPane.addTab("Settings", null, panelSettings, null);
        GridBagLayout gbl_panelSettings = new GridBagLayout();
        gbl_panelSettings.columnWidths = new int[] {
                0, 1, 0, 1, 1, 0
        };
        gbl_panelSettings.rowHeights = new int[] {
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        gbl_panelSettings.columnWeights = new double[] {
                0.0, 1.0, 0.0, 1.0, 10.0
        };
        gbl_panelSettings.rowWeights = new double[] {
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE
        };
        panelSettings.setLayout(gbl_panelSettings);

        lblResolution = new JLabel("Resolution:");
        GridBagConstraints gbc_lblResolution = new GridBagConstraints();
        gbc_lblResolution.insets = new Insets(5, 5, 5, 5);
        gbc_lblResolution.anchor = GridBagConstraints.EAST;
        gbc_lblResolution.gridx = 0;
        gbc_lblResolution.gridy = 0;
        panelSettings.add(lblResolution, gbc_lblResolution);

        cmbResolution = new JComboBox<>();
        cmbResolution
                .setToolTipText("<html>Set the resolution for TF2, you can choose<br>an option or input a custom one.");
        cmbResolution.setModel(new DefaultComboBoxModel<String>(new String[] {
                "640x360", "854x480", "1280x720", "1920x1080"
        }));
        cmbResolution.setEditable(true);
        GridBagConstraints gbc_cmbResolution = new GridBagConstraints();
        gbc_cmbResolution.fill = GridBagConstraints.HORIZONTAL;
        gbc_cmbResolution.insets = new Insets(5, 0, 5, 5);
        gbc_cmbResolution.gridx = 1;
        gbc_cmbResolution.gridy = 0;
        panelSettings.add(cmbResolution, gbc_cmbResolution);

        lblFrameRate = new JLabel("FPS:");
        GridBagConstraints gbc_lblFrameRate = new GridBagConstraints();
        gbc_lblFrameRate.anchor = GridBagConstraints.EAST;
        gbc_lblFrameRate.insets = new Insets(5, 0, 5, 5);
        gbc_lblFrameRate.gridx = 2;
        gbc_lblFrameRate.gridy = 0;
        panelSettings.add(lblFrameRate, gbc_lblFrameRate);

        cmbFramerate = new JComboBox<>();
        cmbFramerate
                .setToolTipText("<html>Set the frames per second of the recording. This value can be<br>changed in-game with the up and down arrow keys. You can<br>also set a custom FPS value here.");
        cmbFramerate.setModel(new DefaultComboBoxModel<String>(new String[] {
                "60", "120", "240", "480", "960", "1920", "3840"
        }));
        cmbFramerate.setEditable(true);
        GridBagConstraints gbc_cmbFramerate = new GridBagConstraints();
        gbc_cmbFramerate.fill = GridBagConstraints.HORIZONTAL;
        gbc_cmbFramerate.insets = new Insets(5, 0, 5, 5);
        gbc_cmbFramerate.gridx = 3;
        gbc_cmbFramerate.gridy = 0;
        panelSettings.add(cmbFramerate, gbc_cmbFramerate);

        panelCustomContent = new JPanel();
        GridBagConstraints gbc_panelCustomContent = new GridBagConstraints();
        gbc_panelCustomContent.insets = new Insets(5, 0, 8, 5);
        gbc_panelCustomContent.gridwidth = 2;
        gbc_panelCustomContent.gridheight = 10;
        gbc_panelCustomContent.fill = GridBagConstraints.BOTH;
        gbc_panelCustomContent.gridx = 4;
        gbc_panelCustomContent.gridy = 0;
        panelSettings.add(panelCustomContent, gbc_panelCustomContent);
        GridBagLayout gbl_panelCustomContent = new GridBagLayout();
        gbl_panelCustomContent.columnWidths = new int[] {
                0, 0
        };
        gbl_panelCustomContent.rowHeights = new int[] {
                0, 0
        };
        gbl_panelCustomContent.columnWeights = new double[] {
                1.0, Double.MIN_VALUE
        };
        gbl_panelCustomContent.rowWeights = new double[] {
                1.0, Double.MIN_VALUE
        };
        panelCustomContent.setLayout(gbl_panelCustomContent);

        scrollPane = new JScrollPane();
        scrollPane
                .setToolTipText("<html>Drag custom folders to this table or copy them<br>\r\nto lawena 'custom' folder to make them appear here.");
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 0;
        gbc_scrollPane.gridy = 0;
        panelCustomContent.add(scrollPane, gbc_scrollPane);

        tableCustomContent = new JTable();
        tableCustomContent.setShowVerticalLines(false);
        tableCustomContent.setGridColor(new Color(0, 0, 0, 30));
        tableCustomContent.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        tableCustomContent.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCustomContent.getTableHeader().setReorderingAllowed(false);
        scrollPane.setViewportView(tableCustomContent);

        JLabel lblHud = new JLabel("HUD:");
        GridBagConstraints gbc_lblHud = new GridBagConstraints();
        gbc_lblHud.anchor = GridBagConstraints.EAST;
        gbc_lblHud.insets = new Insets(0, 5, 5, 5);
        gbc_lblHud.gridx = 0;
        gbc_lblHud.gridy = 1;
        panelSettings.add(lblHud, gbc_lblHud);

        cmbHud = new JComboBox<>();
        cmbHud.setToolTipText("<html>Select your preferred HUD here. You can also you use a custom HUD,<br>in which case you should put the HUD folder into lawena/custom folder<br>and then mark it in the Custom Resources sidebar.");
        cmbHud.setModel(new DefaultComboBoxModel<String>(new String[] {
                "Minimal (kill notices)", "Medic (hp, ubercharge, cp)", "Full", "Default", "Custom"
        }));
        GridBagConstraints gbc_cmbHud = new GridBagConstraints();
        gbc_cmbHud.fill = GridBagConstraints.HORIZONTAL;
        gbc_cmbHud.insets = new Insets(0, 0, 5, 5);
        gbc_cmbHud.gridx = 1;
        gbc_cmbHud.gridy = 1;
        panelSettings.add(cmbHud, gbc_cmbHud);

        JLabel lblDxLevel = new JLabel("Quality:");
        GridBagConstraints gbc_lblDxLevel = new GridBagConstraints();
        gbc_lblDxLevel.anchor = GridBagConstraints.EAST;
        gbc_lblDxLevel.insets = new Insets(0, 5, 5, 5);
        gbc_lblDxLevel.gridx = 2;
        gbc_lblDxLevel.gridy = 1;
        panelSettings.add(lblDxLevel, gbc_lblDxLevel);

        cmbQuality = new JComboBox<>();
        cmbQuality.setModel(new DefaultComboBoxModel<String>(new String[] {
                "DirectX 8.0 (Lowest)", "DirectX 8.1 (Low)", "DirectX 9.0 (Medium)",
                "DirectX 9.5 (High)", "DirectX 9.8 (Highest)"
        }));
        GridBagConstraints gbc_cmbQuality = new GridBagConstraints();
        gbc_cmbQuality.fill = GridBagConstraints.HORIZONTAL;
        gbc_cmbQuality.insets = new Insets(0, 0, 5, 5);
        gbc_cmbQuality.gridx = 3;
        gbc_cmbQuality.gridy = 1;
        panelSettings.add(cmbQuality, gbc_cmbQuality);

        JLabel lblSkybox = new JLabel("Skybox:");
        GridBagConstraints gbc_lblSkybox = new GridBagConstraints();
        gbc_lblSkybox.anchor = GridBagConstraints.EAST;
        gbc_lblSkybox.insets = new Insets(0, 5, 5, 5);
        gbc_lblSkybox.gridx = 0;
        gbc_lblSkybox.gridy = 2;
        panelSettings.add(lblSkybox, gbc_lblSkybox);

        cmbSkybox = new JComboBox<>();
        GridBagConstraints gbc_cmbSkybox = new GridBagConstraints();
        gbc_cmbSkybox.fill = GridBagConstraints.HORIZONTAL;
        gbc_cmbSkybox.insets = new Insets(0, 0, 5, 5);
        gbc_cmbSkybox.gridx = 1;
        gbc_cmbSkybox.gridy = 2;
        panelSettings.add(cmbSkybox, gbc_cmbSkybox);

        lblPreview = new JLabel("");
        GridBagConstraints gbc_lblPreview = new GridBagConstraints();
        gbc_lblPreview.anchor = GridBagConstraints.EAST;
        gbc_lblPreview.insets = new Insets(0, 0, 5, 5);
        gbc_lblPreview.gridx = 2;
        gbc_lblPreview.gridy = 2;
        panelSettings.add(lblPreview, gbc_lblPreview);

        lblSkyboxPreview = new JLabel("");
        GridBagConstraints gbc_lblSkyboxPreview = new GridBagConstraints();
        gbc_lblSkyboxPreview.anchor = GridBagConstraints.NORTHWEST;
        gbc_lblSkyboxPreview.gridheight = 4;
        gbc_lblSkyboxPreview.insets = new Insets(0, 0, 5, 5);
        gbc_lblSkyboxPreview.gridx = 3;
        gbc_lblSkyboxPreview.gridy = 2;
        panelSettings.add(lblSkyboxPreview, gbc_lblSkyboxPreview);

        JLabel lblViewmodels = new JLabel("Viewmodels:");
        GridBagConstraints gbc_lblViewmodels = new GridBagConstraints();
        gbc_lblViewmodels.anchor = GridBagConstraints.EAST;
        gbc_lblViewmodels.insets = new Insets(0, 5, 5, 5);
        gbc_lblViewmodels.gridx = 0;
        gbc_lblViewmodels.gridy = 3;
        panelSettings.add(lblViewmodels, gbc_lblViewmodels);

        cmbViewmodel = new JComboBox<>();
        cmbViewmodel.setModel(new DefaultComboBoxModel<String>(new String[] {
                "On", "Off", "Default"
        }));
        GridBagConstraints gbc_cmbViewmodel = new GridBagConstraints();
        gbc_cmbViewmodel.insets = new Insets(0, 0, 5, 5);
        gbc_cmbViewmodel.fill = GridBagConstraints.HORIZONTAL;
        gbc_cmbViewmodel.gridx = 1;
        gbc_cmbViewmodel.gridy = 3;
        panelSettings.add(cmbViewmodel, gbc_cmbViewmodel);

        horizontalStrut = Box.createHorizontalStrut(24);
        GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
        gbc_horizontalStrut.insets = new Insets(0, 0, 5, 5);
        gbc_horizontalStrut.gridx = 2;
        gbc_horizontalStrut.gridy = 3;
        panelSettings.add(horizontalStrut, gbc_horizontalStrut);

        JLabel lblViewmodelFov = new JLabel("Viewmodel FOV:");
        GridBagConstraints gbc_lblViewmodelFov = new GridBagConstraints();
        gbc_lblViewmodelFov.anchor = GridBagConstraints.EAST;
        gbc_lblViewmodelFov.insets = new Insets(0, 5, 5, 5);
        gbc_lblViewmodelFov.gridx = 0;
        gbc_lblViewmodelFov.gridy = 4;
        panelSettings.add(lblViewmodelFov, gbc_lblViewmodelFov);

        spinnerViewmodelFov = new JSpinner();
        GridBagConstraints gbc_spinnerViewmodelFov = new GridBagConstraints();
        gbc_spinnerViewmodelFov.anchor = GridBagConstraints.WEST;
        gbc_spinnerViewmodelFov.insets = new Insets(0, 0, 5, 5);
        gbc_spinnerViewmodelFov.gridx = 1;
        gbc_spinnerViewmodelFov.gridy = 4;
        panelSettings.add(spinnerViewmodelFov, gbc_spinnerViewmodelFov);
        spinnerViewmodelFov.setModel(new SpinnerNumberModel(new Integer(70), null, null,
                new Integer(1)));

        verticalStrut = Box.createVerticalStrut(22);
        GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
        gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
        gbc_verticalStrut.gridx = 0;
        gbc_verticalStrut.gridy = 5;
        panelSettings.add(verticalStrut, gbc_verticalStrut);

        panelCheckboxes = new JPanel();
        panelCheckboxes.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
                "Additional Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        GridBagConstraints gbc_panelCheckboxes = new GridBagConstraints();
        gbc_panelCheckboxes.insets = new Insets(0, 0, 5, 5);
        gbc_panelCheckboxes.gridheight = 4;
        gbc_panelCheckboxes.gridwidth = 4;
        gbc_panelCheckboxes.fill = GridBagConstraints.BOTH;
        gbc_panelCheckboxes.gridx = 0;
        gbc_panelCheckboxes.gridy = 6;
        panelSettings.add(panelCheckboxes, gbc_panelCheckboxes);
        GridBagLayout gbl_panelCheckboxes = new GridBagLayout();
        gbl_panelCheckboxes.columnWidths = new int[] {
                0, 0, 0
        };
        gbl_panelCheckboxes.rowHeights = new int[] {
                0, 0, 0, 0, 0
        };
        gbl_panelCheckboxes.columnWeights = new double[] {
                1.0, 1.0, Double.MIN_VALUE
        };
        gbl_panelCheckboxes.rowWeights = new double[] {
                0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE
        };
        panelCheckboxes.setLayout(gbl_panelCheckboxes);

        enableMotionBlur = new JCheckBox("Enable Motion Blur");
        GridBagConstraints gbc_enableMotionBlur = new GridBagConstraints();
        gbc_enableMotionBlur.insets = new Insets(0, 0, 0, 5);
        gbc_enableMotionBlur.anchor = GridBagConstraints.WEST;
        gbc_enableMotionBlur.gridx = 0;
        gbc_enableMotionBlur.gridy = 0;
        panelCheckboxes.add(enableMotionBlur, gbc_enableMotionBlur);

        disableCombatText = new JCheckBox("Disable Combat Text");
        GridBagConstraints gbc_disableCombatText = new GridBagConstraints();
        gbc_disableCombatText.anchor = GridBagConstraints.WEST;
        gbc_disableCombatText.gridx = 1;
        gbc_disableCombatText.gridy = 0;
        panelCheckboxes.add(disableCombatText, gbc_disableCombatText);

        disableHitSounds = new JCheckBox("Disable Hit Sounds");
        GridBagConstraints gbc_disableHitSounds = new GridBagConstraints();
        gbc_disableHitSounds.insets = new Insets(0, 0, 0, 5);
        gbc_disableHitSounds.anchor = GridBagConstraints.WEST;
        gbc_disableHitSounds.gridx = 0;
        gbc_disableHitSounds.gridy = 1;
        panelCheckboxes.add(disableHitSounds, gbc_disableHitSounds);

        disableCrosshair = new JCheckBox("Disable Crosshair");
        GridBagConstraints gbc_disableCrosshair = new GridBagConstraints();
        gbc_disableCrosshair.anchor = GridBagConstraints.WEST;
        gbc_disableCrosshair.gridx = 1;
        gbc_disableCrosshair.gridy = 1;
        panelCheckboxes.add(disableCrosshair, gbc_disableCrosshair);

        disableVoiceChat = new JCheckBox("Disable Voice Chat");
        GridBagConstraints gbc_disableVoiceChat = new GridBagConstraints();
        gbc_disableVoiceChat.insets = new Insets(0, 0, 0, 5);
        gbc_disableVoiceChat.anchor = GridBagConstraints.WEST;
        gbc_disableVoiceChat.gridx = 0;
        gbc_disableVoiceChat.gridy = 2;
        panelCheckboxes.add(disableVoiceChat, gbc_disableVoiceChat);

        disableCrosshairSwitch = new JCheckBox("Disable Crosshair Switching");
        GridBagConstraints gbc_disableCrosshairSwitch = new GridBagConstraints();
        gbc_disableCrosshairSwitch.anchor = GridBagConstraints.WEST;
        gbc_disableCrosshairSwitch.gridx = 1;
        gbc_disableCrosshairSwitch.gridy = 2;
        panelCheckboxes.add(disableCrosshairSwitch, gbc_disableCrosshairSwitch);

        useHudMinmode = new JCheckBox("Use HUD Minmode");
        useHudMinmode
                .setToolTipText("<html>The minmode version of a HUD primarily reduces the size of the<br>health and ammo displays, moving them closer to the centre of<br>the screen. Ticking this option will add \"cl_hud_minmode 1\" to<br>the config.");
        GridBagConstraints gbc_chckbxUseHudMin = new GridBagConstraints();
        gbc_chckbxUseHudMin.anchor = GridBagConstraints.WEST;
        gbc_chckbxUseHudMin.insets = new Insets(0, 0, 0, 5);
        gbc_chckbxUseHudMin.gridx = 0;
        gbc_chckbxUseHudMin.gridy = 3;
        panelCheckboxes.add(useHudMinmode, gbc_chckbxUseHudMin);

        panelBottomLeft = new JPanel();
        FlowLayout fl_panelBottomLeft = (FlowLayout) panelBottomLeft.getLayout();
        fl_panelBottomLeft.setVgap(0);
        fl_panelBottomLeft.setHgap(0);
        GridBagConstraints gbc_panelBottomLeft = new GridBagConstraints();
        gbc_panelBottomLeft.anchor = GridBagConstraints.WEST;
        gbc_panelBottomLeft.gridwidth = 4;
        gbc_panelBottomLeft.insets = new Insets(0, 5, 5, 5);
        gbc_panelBottomLeft.fill = GridBagConstraints.VERTICAL;
        gbc_panelBottomLeft.gridx = 0;
        gbc_panelBottomLeft.gridy = 10;
        panelSettings.add(panelBottomLeft, gbc_panelBottomLeft);

        btnClearMovieFolder = new JButton("Clear Movie Files...");
        panelBottomLeft.add(btnClearMovieFolder);

        panelBottomRight = new JPanel();
        FlowLayout fl_panelBottomRight = (FlowLayout) panelBottomRight.getLayout();
        fl_panelBottomRight.setVgap(0);
        fl_panelBottomRight.setHgap(0);
        GridBagConstraints gbc_panelBottomRight = new GridBagConstraints();
        gbc_panelBottomRight.gridwidth = 2;
        gbc_panelBottomRight.anchor = GridBagConstraints.EAST;
        gbc_panelBottomRight.insets = new Insets(0, 0, 5, 5);
        gbc_panelBottomRight.fill = GridBagConstraints.VERTICAL;
        gbc_panelBottomRight.gridx = 4;
        gbc_panelBottomRight.gridy = 10;
        panelSettings.add(panelBottomRight, gbc_panelBottomRight);

        btnStartTf = new JButton("Start Team Fortress 2");
        panelBottomRight.add(btnStartTf);

        JPanel panelLog = new JPanel();
        tabbedPane.addTab("Log", null, panelLog, null);
        GridBagLayout gbl_panelLog = new GridBagLayout();
        gbl_panelLog.columnWidths = new int[] {
                719, 0
        };
        gbl_panelLog.rowHeights = new int[] {
                320, 0
        };
        gbl_panelLog.columnWeights = new double[] {
                1.0, Double.MIN_VALUE
        };
        gbl_panelLog.rowWeights = new double[] {
                1.0, Double.MIN_VALUE
        };
        panelLog.setLayout(gbl_panelLog);

        scrollPane_2 = new JScrollPane();
        GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
        gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
        gbc_scrollPane_2.insets = new Insets(5, 5, 5, 5);
        gbc_scrollPane_2.gridx = 0;
        gbc_scrollPane_2.gridy = 0;
        panelLog.add(scrollPane_2, gbc_scrollPane_2);

        textAreaLog = new JTextArea();
        textAreaLog.setFont(new Font("Tahoma", Font.PLAIN, 10));
        textAreaLog.setEditable(false);
        scrollPane_2.setViewportView(textAreaLog);

        panelStatusbar = new JPanel();
        contentPane.add(panelStatusbar, BorderLayout.SOUTH);
        GridBagLayout gbl_panelStatusbar = new GridBagLayout();
        gbl_panelStatusbar.columnWidths = new int[] {
                31, 0, 0, 0
        };
        gbl_panelStatusbar.rowHeights = new int[] {
                12, 0
        };
        gbl_panelStatusbar.columnWeights = new double[] {
                0.0, 1.0, 0.0, Double.MIN_VALUE
        };
        gbl_panelStatusbar.rowWeights = new double[] {
                0.0, Double.MIN_VALUE
        };
        panelStatusbar.setLayout(gbl_panelStatusbar);

        lblStatus = new JLabel("Status");
        GridBagConstraints gbc_lblStatus = new GridBagConstraints();
        gbc_lblStatus.insets = new Insets(0, 5, 0, 5);
        gbc_lblStatus.anchor = GridBagConstraints.NORTHWEST;
        gbc_lblStatus.gridx = 0;
        gbc_lblStatus.gridy = 0;
        panelStatusbar.add(lblStatus, gbc_lblStatus);

        progressBar = new JProgressBar();
        GridBagConstraints gbc_progressBar = new GridBagConstraints();
        gbc_progressBar.anchor = GridBagConstraints.EAST;
        gbc_progressBar.gridx = 2;
        gbc_progressBar.gridy = 0;
        panelStatusbar.add(progressBar, gbc_progressBar);

        pack();
        setMinimumSize(new Dimension(750, 420));
        setLocationByPlatform(true);
    }

    public JComboBox<String> getCmbResolution() {
        return cmbResolution;
    }

    public JComboBox<String> getCmbSkybox() {
        return cmbSkybox;
    }

    public JComboBox<String> getCmbHud() {
        return cmbHud;
    }

    public JComboBox<String> getCmbFramerate() {
        return cmbFramerate;
    }

    public JComboBox<String> getCmbQuality() {
        return cmbQuality;
    }

    public JSpinner getSpinnerViewmodelFov() {
        return spinnerViewmodelFov;
    }

    public JCheckBox getEnableMotionBlur() {
        return enableMotionBlur;
    }

    public JCheckBox getDisableCrosshair() {
        return disableCrosshair;
    }

    public JCheckBox getDisableCrosshairSwitch() {
        return disableCrosshairSwitch;
    }

    public JCheckBox getDisableCombatText() {
        return disableCombatText;
    }

    public JCheckBox getDisableHitSounds() {
        return disableHitSounds;
    }

    public JCheckBox getDisableVoiceChat() {
        return disableVoiceChat;
    }

    public JButton getBtnStartTf() {
        return btnStartTf;
    }

    public JLabel getLblResolution() {
        return lblResolution;
    }

    public JLabel getLblFrameRate() {
        return lblFrameRate;
    }

    public JLabel getLblSkyboxPreview() {
        return lblSkyboxPreview;
    }

    public JButton getBtnClearMovieFolder() {
        return btnClearMovieFolder;
    }

    public JTextArea getTextAreaLog() {
        return textAreaLog;
    }

    public JTable getTableCustomContent() {
        return tableCustomContent;
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public JMenuItem getMntmChangeTfDirectory() {
        return mntmChangeTfDirectory;
    }

    public JMenuItem getMntmChangeMovieDirectory() {
        return mntmChangeMovieDirectory;
    }

    public JLabel getLblStatus() {
        return lblStatus;
    }

    public JComboBox<String> getCmbViewmodel() {
        return cmbViewmodel;
    }

    public JLabel getLblPreview() {
        return lblPreview;
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JMenuItem getMntmRevertToDefault() {
        return mntmRevertToDefault;
    }

    public JMenuItem getMntmExit() {
        return mntmExit;
    }

    public JMenuItem getMntmAbout() {
        return mntmAbout;
    }

    public JMenuItem getMntmSaveSettings() {
        return mntmSaveSettings;
    }

    public JCheckBox getUseHudMinmode() {
        return useHudMinmode;
    }

    public JMenuItem getMntmSelectEnhancedParticles() {
        return mntmSelectEnhancedParticles;
    }

    public JMenuItem getMntmOpenMovieFolder() {
        return mntmOpenMovieFolder;
    }

    public JMenuItem getMntmOpenCustomFolder() {
        return mntmOpenCustomFolder;
    }

    public JMenuItem getMntmLaunchTimeout() {
        return mntmChangeTfLaunch;
    }

    public JCheckBoxMenuItem getChckbxmntmInsecure() {
        return chckbxmntmInsecure;
    }
}
