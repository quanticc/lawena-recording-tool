package lawena;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

public class LawenaView extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private JPanel contentPane;
    private JTextField txtTfdir;
    private JTextField txtMoviedir;
    private JComboBox<String> cmbSkybox;

    /**
     * Create the frame.
     */
    public LawenaView() {
        setTitle("lawena Recording Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 791, 450);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(dim.width / 2, dim.height / 2);
        int w = getSize().width;
        int h = getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;
        setLocation(x, y);
        setResizable(false);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        contentPane.add(tabbedPane, BorderLayout.CENTER);
        
        JPanel panelSettings = new JPanel();
        tabbedPane.addTab("Settings", null, panelSettings, null);
        GridBagLayout gbl_panelSettings = new GridBagLayout();
        gbl_panelSettings.columnWidths = new int[]{0, 0, 0};
        gbl_panelSettings.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_panelSettings.columnWeights = new double[]{0.0, 1.0, 0.0};
        gbl_panelSettings.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
        panelSettings.setLayout(gbl_panelSettings);
        
        JLabel lblResolution = new JLabel("Resolution:");
        GridBagConstraints gbc_lblResolution = new GridBagConstraints();
        gbc_lblResolution.insets = new Insets(0, 0, 5, 5);
        gbc_lblResolution.anchor = GridBagConstraints.EAST;
        gbc_lblResolution.gridx = 0;
        gbc_lblResolution.gridy = 0;
        panelSettings.add(lblResolution, gbc_lblResolution);
        
        JComboBox cmbResolution = new JComboBox();
        cmbResolution.setModel(new DefaultComboBoxModel(new String[] {"640x360", "854x480", "1280x720", "1920x1080"}));
        cmbResolution.setEditable(true);
        GridBagConstraints gbc_cmbResolution = new GridBagConstraints();
        gbc_cmbResolution.fill = GridBagConstraints.HORIZONTAL;
        gbc_cmbResolution.insets = new Insets(0, 0, 5, 5);
        gbc_cmbResolution.gridx = 1;
        gbc_cmbResolution.gridy = 0;
        panelSettings.add(cmbResolution, gbc_cmbResolution);
        
        JLabel lblFrameRate = new JLabel("Frame Rate:");
        GridBagConstraints gbc_lblFrameRate = new GridBagConstraints();
        gbc_lblFrameRate.anchor = GridBagConstraints.EAST;
        gbc_lblFrameRate.insets = new Insets(0, 0, 5, 5);
        gbc_lblFrameRate.gridx = 2;
        gbc_lblFrameRate.gridy = 0;
        panelSettings.add(lblFrameRate, gbc_lblFrameRate);
        
        JComboBox cmbFramerate = new JComboBox();
        cmbFramerate.setModel(new DefaultComboBoxModel(new String[] {"60", "120", "240", "480", "960", "1920", "3840"}));
        cmbFramerate.setEditable(true);
        GridBagConstraints gbc_cmbFramerate = new GridBagConstraints();
        gbc_cmbFramerate.anchor = GridBagConstraints.WEST;
        gbc_cmbFramerate.insets = new Insets(0, 0, 5, 5);
        gbc_cmbFramerate.gridx = 3;
        gbc_cmbFramerate.gridy = 0;
        panelSettings.add(cmbFramerate, gbc_cmbFramerate);
        
        JCheckBox enableMotionBlur = new JCheckBox("Enable Motion Blur");
        GridBagConstraints gbc_enableMotionBlur = new GridBagConstraints();
        gbc_enableMotionBlur.anchor = GridBagConstraints.WEST;
        gbc_enableMotionBlur.insets = new Insets(0, 0, 5, 0);
        gbc_enableMotionBlur.gridx = 4;
        gbc_enableMotionBlur.gridy = 0;
        panelSettings.add(enableMotionBlur, gbc_enableMotionBlur);
        
        JLabel lblHud = new JLabel("HUD:");
        GridBagConstraints gbc_lblHud = new GridBagConstraints();
        gbc_lblHud.anchor = GridBagConstraints.EAST;
        gbc_lblHud.insets = new Insets(0, 0, 5, 5);
        gbc_lblHud.gridx = 0;
        gbc_lblHud.gridy = 1;
        panelSettings.add(lblHud, gbc_lblHud);
        
        JComboBox cmbHud = new JComboBox();
        cmbHud.setModel(new DefaultComboBoxModel(new String[] {"Minimal (kill notices)", "Medic (hp, ubercharge, cp)", "Full", "Custom"}));
        GridBagConstraints gbc_cmbHud = new GridBagConstraints();
        gbc_cmbHud.fill = GridBagConstraints.HORIZONTAL;
        gbc_cmbHud.insets = new Insets(0, 0, 5, 5);
        gbc_cmbHud.gridx = 1;
        gbc_cmbHud.gridy = 1;
        panelSettings.add(cmbHud, gbc_cmbHud);
        
        JLabel lblDxLevel = new JLabel("DirectX Level:");
        GridBagConstraints gbc_lblDxLevel = new GridBagConstraints();
        gbc_lblDxLevel.anchor = GridBagConstraints.EAST;
        gbc_lblDxLevel.insets = new Insets(0, 0, 5, 5);
        gbc_lblDxLevel.gridx = 2;
        gbc_lblDxLevel.gridy = 1;
        panelSettings.add(lblDxLevel, gbc_lblDxLevel);
        
        JComboBox cmbQuality = new JComboBox();
        cmbQuality.setModel(new DefaultComboBoxModel(new String[] {"80", "81", "90", "95", "98"}));
        GridBagConstraints gbc_cmbQuality = new GridBagConstraints();
        gbc_cmbQuality.fill = GridBagConstraints.HORIZONTAL;
        gbc_cmbQuality.insets = new Insets(0, 0, 5, 5);
        gbc_cmbQuality.gridx = 3;
        gbc_cmbQuality.gridy = 1;
        panelSettings.add(cmbQuality, gbc_cmbQuality);
        
        JCheckBox disableCrosshair = new JCheckBox("Disable Crosshair");
        GridBagConstraints gbc_disableCrosshair = new GridBagConstraints();
        gbc_disableCrosshair.anchor = GridBagConstraints.WEST;
        gbc_disableCrosshair.insets = new Insets(0, 0, 5, 0);
        gbc_disableCrosshair.gridx = 4;
        gbc_disableCrosshair.gridy = 2;
        panelSettings.add(disableCrosshair, gbc_disableCrosshair);
        
        JLabel lblViewmodels = new JLabel("Viewmodels:");
        GridBagConstraints gbc_lblViewmodels = new GridBagConstraints();
        gbc_lblViewmodels.anchor = GridBagConstraints.EAST;
        gbc_lblViewmodels.insets = new Insets(0, 0, 5, 5);
        gbc_lblViewmodels.gridx = 0;
        gbc_lblViewmodels.gridy = 2;
        panelSettings.add(lblViewmodels, gbc_lblViewmodels);
        
        JComboBox cmbViewmodels = new JComboBox();
        cmbViewmodels.setModel(new DefaultComboBoxModel(new String[] {"Always on", "Always off", "Default"}));
        GridBagConstraints gbc_cmbViewmodels = new GridBagConstraints();
        gbc_cmbViewmodels.fill = GridBagConstraints.HORIZONTAL;
        gbc_cmbViewmodels.insets = new Insets(0, 0, 5, 5);
        gbc_cmbViewmodels.gridx = 1;
        gbc_cmbViewmodels.gridy = 2;
        panelSettings.add(cmbViewmodels, gbc_cmbViewmodels);
        
        JLabel lblViewmodelFov = new JLabel("Viewmodel FOV:");
        GridBagConstraints gbc_lblViewmodelFov = new GridBagConstraints();
        gbc_lblViewmodelFov.anchor = GridBagConstraints.EAST;
        gbc_lblViewmodelFov.insets = new Insets(0, 0, 5, 5);
        gbc_lblViewmodelFov.gridx = 2;
        gbc_lblViewmodelFov.gridy = 2;
        panelSettings.add(lblViewmodelFov, gbc_lblViewmodelFov);
        
        JSpinner spinner = new JSpinner();
        spinner.setModel(new SpinnerNumberModel(70, 55, 70, 1));
        GridBagConstraints gbc_spinner = new GridBagConstraints();
        gbc_spinner.anchor = GridBagConstraints.WEST;
        gbc_spinner.insets = new Insets(0, 0, 5, 5);
        gbc_spinner.gridx = 3;
        gbc_spinner.gridy = 2;
        panelSettings.add(spinner, gbc_spinner);
        
        JCheckBox disableCrosshairSwitch = new JCheckBox("Disable Crosshair Switching in demos");
        GridBagConstraints gbc_disableCrosshairSwitch = new GridBagConstraints();
        gbc_disableCrosshairSwitch.anchor = GridBagConstraints.WEST;
        gbc_disableCrosshairSwitch.insets = new Insets(0, 0, 5, 0);
        gbc_disableCrosshairSwitch.gridx = 4;
        gbc_disableCrosshairSwitch.gridy = 3;
        panelSettings.add(disableCrosshairSwitch, gbc_disableCrosshairSwitch);
        
        JLabel lblSkybox = new JLabel("Skybox:");
        GridBagConstraints gbc_lblSkybox = new GridBagConstraints();
        gbc_lblSkybox.gridheight = 3;
        gbc_lblSkybox.anchor = GridBagConstraints.EAST;
        gbc_lblSkybox.insets = new Insets(0, 0, 5, 5);
        gbc_lblSkybox.gridx = 0;
        gbc_lblSkybox.gridy = 3;
        panelSettings.add(lblSkybox, gbc_lblSkybox);
        
        cmbSkybox = new JComboBox();
        GridBagConstraints gbc_cmbSkybox = new GridBagConstraints();
        gbc_cmbSkybox.gridheight = 3;
        gbc_cmbSkybox.fill = GridBagConstraints.HORIZONTAL;
        gbc_cmbSkybox.insets = new Insets(0, 0, 5, 5);
        gbc_cmbSkybox.gridx = 1;
        gbc_cmbSkybox.gridy = 3;
        panelSettings.add(cmbSkybox, gbc_cmbSkybox);
        
        JCheckBox disableCombatText = new JCheckBox("Disable Combat Text");
        GridBagConstraints gbc_disableCombatText = new GridBagConstraints();
        gbc_disableCombatText.anchor = GridBagConstraints.WEST;
        gbc_disableCombatText.insets = new Insets(0, 0, 5, 0);
        gbc_disableCombatText.gridx = 4;
        gbc_disableCombatText.gridy = 4;
        panelSettings.add(disableCombatText, gbc_disableCombatText);
        
        JCheckBox chckbxEnableParticles = new JCheckBox("Enable Custom Particle Effects");
        GridBagConstraints gbc_chckbxEnableParticles = new GridBagConstraints();
        gbc_chckbxEnableParticles.anchor = GridBagConstraints.WEST;
        gbc_chckbxEnableParticles.insets = new Insets(0, 0, 5, 0);
        gbc_chckbxEnableParticles.gridx = 4;
        gbc_chckbxEnableParticles.gridy = 1;
        panelSettings.add(chckbxEnableParticles, gbc_chckbxEnableParticles);
        
        JCheckBox disableHitSounds = new JCheckBox("Disable Hit Sounds");
        GridBagConstraints gbc_disableHitSounds = new GridBagConstraints();
        gbc_disableHitSounds.weightx = 2.0;
        gbc_disableHitSounds.anchor = GridBagConstraints.WEST;
        gbc_disableHitSounds.insets = new Insets(0, 0, 5, 0);
        gbc_disableHitSounds.gridx = 4;
        gbc_disableHitSounds.gridy = 5;
        panelSettings.add(disableHitSounds, gbc_disableHitSounds);
        
        JLabel lblTfDirectory = new JLabel("TF2 Folder:");
        GridBagConstraints gbc_lblTfDirectory = new GridBagConstraints();
        gbc_lblTfDirectory.anchor = GridBagConstraints.EAST;
        gbc_lblTfDirectory.insets = new Insets(0, 0, 5, 5);
        gbc_lblTfDirectory.gridx = 0;
        gbc_lblTfDirectory.gridy = 6;
        panelSettings.add(lblTfDirectory, gbc_lblTfDirectory);
        
        txtTfdir = new JTextField();
        txtTfdir.setText("tfdir");
        GridBagConstraints gbc_txtTfdir = new GridBagConstraints();
        gbc_txtTfdir.gridwidth = 2;
        gbc_txtTfdir.insets = new Insets(0, 0, 5, 5);
        gbc_txtTfdir.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtTfdir.gridx = 1;
        gbc_txtTfdir.gridy = 6;
        panelSettings.add(txtTfdir, gbc_txtTfdir);
        txtTfdir.setColumns(10);
        
        JPanel panel_2 = new JPanel();
        FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        flowLayout.setVgap(0);
        flowLayout.setHgap(0);
        GridBagConstraints gbc_panel_2 = new GridBagConstraints();
        gbc_panel_2.insets = new Insets(0, 0, 5, 5);
        gbc_panel_2.fill = GridBagConstraints.BOTH;
        gbc_panel_2.gridx = 3;
        gbc_panel_2.gridy = 6;
        panelSettings.add(panel_2, gbc_panel_2);
        
        JButton button_2 = new JButton("...");
        button_2.setMargin(new Insets(1, 2, 1, 2));
        panel_2.add(button_2);
        
        JButton button_3 = new JButton("");
        button_3.setIcon(new ImageIcon(LawenaView.class.getResource("/ui/folder-horizontal-open.png")));
        button_3.setMargin(new Insets(0, 0, 0, 0));
        panel_2.add(button_3);
        
        JCheckBox disableVoiceChat = new JCheckBox("Disable Voice Chat");
        GridBagConstraints gbc_disableVoiceChat = new GridBagConstraints();
        gbc_disableVoiceChat.anchor = GridBagConstraints.WEST;
        gbc_disableVoiceChat.insets = new Insets(0, 0, 5, 0);
        gbc_disableVoiceChat.gridx = 4;
        gbc_disableVoiceChat.gridy = 6;
        panelSettings.add(disableVoiceChat, gbc_disableVoiceChat);
        
        JLabel lblMovieFolder = new JLabel("Movie Folder:");
        GridBagConstraints gbc_lblMovieFolder = new GridBagConstraints();
        gbc_lblMovieFolder.anchor = GridBagConstraints.EAST;
        gbc_lblMovieFolder.insets = new Insets(0, 0, 5, 5);
        gbc_lblMovieFolder.gridx = 0;
        gbc_lblMovieFolder.gridy = 7;
        panelSettings.add(lblMovieFolder, gbc_lblMovieFolder);
        
        txtMoviedir = new JTextField();
        txtMoviedir.setText("moviedir");
        GridBagConstraints gbc_txtMoviedir = new GridBagConstraints();
        gbc_txtMoviedir.gridwidth = 2;
        gbc_txtMoviedir.insets = new Insets(0, 0, 5, 5);
        gbc_txtMoviedir.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtMoviedir.gridx = 1;
        gbc_txtMoviedir.gridy = 7;
        panelSettings.add(txtMoviedir, gbc_txtMoviedir);
        txtMoviedir.setColumns(10);
        
        JPanel panel_1 = new JPanel();
        GridBagConstraints gbc_panel_1 = new GridBagConstraints();
        gbc_panel_1.insets = new Insets(0, 0, 5, 5);
        gbc_panel_1.fill = GridBagConstraints.BOTH;
        gbc_panel_1.gridx = 3;
        gbc_panel_1.gridy = 7;
        panelSettings.add(panel_1, gbc_panel_1);
        FlowLayout fl_panel_1 = new FlowLayout(FlowLayout.LEFT, 0, 0);
        panel_1.setLayout(fl_panel_1);
        
        JButton btnOpenTfDir = new JButton("...");
        btnOpenTfDir.setMargin(new Insets(1, 2, 1, 2));
        panel_1.add(btnOpenTfDir);
        
        JButton button = new JButton("");
        button.setIcon(new ImageIcon(LawenaView.class.getResource("/ui/folder-horizontal-open.png")));
        button.setMargin(new Insets(0, 0, 0, 0));
        panel_1.add(button);
        
        JButton button_1 = new JButton("");
        button_1.setIcon(new ImageIcon(LawenaView.class.getResource("/ui/cross.png")));
        button_1.setMargin(new Insets(0, 0, 0, 0));
        panel_1.add(button_1);
        
        JCheckBox disableAnnouncer = new JCheckBox("Disable Announcer");
        GridBagConstraints gbc_disableAnnouncer = new GridBagConstraints();
        gbc_disableAnnouncer.anchor = GridBagConstraints.WEST;
        gbc_disableAnnouncer.insets = new Insets(0, 0, 5, 0);
        gbc_disableAnnouncer.gridx = 4;
        gbc_disableAnnouncer.gridy = 7;
        panelSettings.add(disableAnnouncer, gbc_disableAnnouncer);
        
        JCheckBox disableDominationSounds = new JCheckBox("Disable Domination and Revenge Sounds");
        GridBagConstraints gbc_disableDominationSounds = new GridBagConstraints();
        gbc_disableDominationSounds.anchor = GridBagConstraints.WEST;
        gbc_disableDominationSounds.insets = new Insets(0, 0, 5, 0);
        gbc_disableDominationSounds.gridx = 4;
        gbc_disableDominationSounds.gridy = 8;
        panelSettings.add(disableDominationSounds, gbc_disableDominationSounds);
        
        JCheckBox disableSteamCloud = new JCheckBox("Disable Steam Cloud (recommended)");
        GridBagConstraints gbc_disableSteamCloud = new GridBagConstraints();
        gbc_disableSteamCloud.insets = new Insets(0, 0, 5, 0);
        gbc_disableSteamCloud.anchor = GridBagConstraints.WEST;
        gbc_disableSteamCloud.gridx = 4;
        gbc_disableSteamCloud.gridy = 9;
        panelSettings.add(disableSteamCloud, gbc_disableSteamCloud);
        
        JButton btnStartTf = new JButton("Start Team Fortress 2");
        btnStartTf.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnStartTf.setIconTextGap(10);
        btnStartTf.setIcon(new ImageIcon(LawenaView.class.getResource("/ui/tf2full.png")));
        GridBagConstraints gbc_btnStartTf = new GridBagConstraints();
        gbc_btnStartTf.fill = GridBagConstraints.HORIZONTAL;
        gbc_btnStartTf.gridx = 4;
        gbc_btnStartTf.gridy = 11;
        panelSettings.add(btnStartTf, gbc_btnStartTf);
        
        JPanel panelVdm = new JPanel();
        tabbedPane.addTab("VDM", null, panelVdm, null);
        
        JPanel panel = new JPanel();
        tabbedPane.addTab("About", null, panel, null);
    }

    public JComboBox<String> getCmbSkybox() {
        return cmbSkybox;
    }
}
