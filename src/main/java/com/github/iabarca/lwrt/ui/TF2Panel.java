package com.github.iabarca.lwrt.ui;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import java.awt.FlowLayout;
import javax.swing.JSpinner;

public class TF2Panel extends JPanel {
    private JTextField txtHeight;
    private JTextField txtWidth;

    /**
     * Create the panel.
     */
    public TF2Panel() {
        setLayout(new BorderLayout(0, 0));
        
        JPanel centerPanel = new JPanel();
        add(centerPanel, BorderLayout.CENTER);
        centerPanel.setLayout(new BorderLayout(0, 0));
        
        JTabbedPane customTabbedPane = new JTabbedPane(JTabbedPane.TOP);
        centerPanel.add(customTabbedPane);
        
        JPanel settingsPanel = new JPanel();
        customTabbedPane.addTab("Settings", null, settingsPanel, null);
        GridBagLayout gbl_settingsPanel = new GridBagLayout();
        gbl_settingsPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
        gbl_settingsPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
        gbl_settingsPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
        gbl_settingsPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        settingsPanel.setLayout(gbl_settingsPanel);
        
        JLabel lblResolution = new JLabel("Resolution:");
        GridBagConstraints gbc_lblResolution = new GridBagConstraints();
        gbc_lblResolution.anchor = GridBagConstraints.EAST;
        gbc_lblResolution.insets = new Insets(5, 5, 5, 5);
        gbc_lblResolution.gridx = 0;
        gbc_lblResolution.gridy = 0;
        settingsPanel.add(lblResolution, gbc_lblResolution);
        
        txtWidth = new JTextField();
        GridBagConstraints gbc_txtWidth = new GridBagConstraints();
        gbc_txtWidth.insets = new Insets(5, 0, 5, 5);
        gbc_txtWidth.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtWidth.gridx = 1;
        gbc_txtWidth.gridy = 0;
        settingsPanel.add(txtWidth, gbc_txtWidth);
        txtWidth.setColumns(10);
        
        JLabel lblX = new JLabel("x");
        GridBagConstraints gbc_lblX = new GridBagConstraints();
        gbc_lblX.insets = new Insets(5, 0, 5, 5);
        gbc_lblX.gridx = 2;
        gbc_lblX.gridy = 0;
        settingsPanel.add(lblX, gbc_lblX);
        
        txtHeight = new JTextField();
        GridBagConstraints gbc_txtHeight = new GridBagConstraints();
        gbc_txtHeight.insets = new Insets(5, 0, 5, 5);
        gbc_txtHeight.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtHeight.gridx = 3;
        gbc_txtHeight.gridy = 0;
        settingsPanel.add(txtHeight, gbc_txtHeight);
        txtHeight.setColumns(10);
        
        JLabel lblFramerate = new JLabel("Frame rate:");
        GridBagConstraints gbc_lblFramerate = new GridBagConstraints();
        gbc_lblFramerate.anchor = GridBagConstraints.EAST;
        gbc_lblFramerate.insets = new Insets(0, 5, 5, 5);
        gbc_lblFramerate.gridx = 0;
        gbc_lblFramerate.gridy = 1;
        settingsPanel.add(lblFramerate, gbc_lblFramerate);
        
        JComboBox framerateComboBox = new JComboBox();
        framerateComboBox.setEditable(true);
        GridBagConstraints gbc_framerateComboBox = new GridBagConstraints();
        gbc_framerateComboBox.gridwidth = 3;
        gbc_framerateComboBox.insets = new Insets(0, 0, 5, 5);
        gbc_framerateComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_framerateComboBox.gridx = 1;
        gbc_framerateComboBox.gridy = 1;
        settingsPanel.add(framerateComboBox, gbc_framerateComboBox);
        
        JLabel lblQuality = new JLabel("Quality:");
        GridBagConstraints gbc_lblQuality = new GridBagConstraints();
        gbc_lblQuality.anchor = GridBagConstraints.EAST;
        gbc_lblQuality.insets = new Insets(0, 5, 5, 5);
        gbc_lblQuality.gridx = 0;
        gbc_lblQuality.gridy = 2;
        settingsPanel.add(lblQuality, gbc_lblQuality);
        
        JComboBox dxlevelComboBox = new JComboBox();
        GridBagConstraints gbc_dxlevelComboBox = new GridBagConstraints();
        gbc_dxlevelComboBox.insets = new Insets(0, 0, 5, 5);
        gbc_dxlevelComboBox.gridwidth = 3;
        gbc_dxlevelComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_dxlevelComboBox.gridx = 1;
        gbc_dxlevelComboBox.gridy = 2;
        settingsPanel.add(dxlevelComboBox, gbc_dxlevelComboBox);
        
        JLabel lblHud = new JLabel("HUD:");
        GridBagConstraints gbc_lblHud = new GridBagConstraints();
        gbc_lblHud.anchor = GridBagConstraints.EAST;
        gbc_lblHud.insets = new Insets(0, 0, 5, 5);
        gbc_lblHud.gridx = 0;
        gbc_lblHud.gridy = 3;
        settingsPanel.add(lblHud, gbc_lblHud);
        
        JComboBox hudComboBox = new JComboBox();
        GridBagConstraints gbc_hudComboBox = new GridBagConstraints();
        gbc_hudComboBox.insets = new Insets(0, 0, 5, 5);
        gbc_hudComboBox.gridwidth = 3;
        gbc_hudComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_hudComboBox.gridx = 1;
        gbc_hudComboBox.gridy = 3;
        settingsPanel.add(hudComboBox, gbc_hudComboBox);
        
        JLabel lblViewmodels = new JLabel("Viewmodels:");
        GridBagConstraints gbc_lblViewmodels = new GridBagConstraints();
        gbc_lblViewmodels.anchor = GridBagConstraints.EAST;
        gbc_lblViewmodels.insets = new Insets(0, 0, 5, 5);
        gbc_lblViewmodels.gridx = 0;
        gbc_lblViewmodels.gridy = 4;
        settingsPanel.add(lblViewmodels, gbc_lblViewmodels);
        
        JComboBox comboBox = new JComboBox();
        GridBagConstraints gbc_comboBox = new GridBagConstraints();
        gbc_comboBox.insets = new Insets(0, 0, 5, 5);
        gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_comboBox.gridx = 1;
        gbc_comboBox.gridy = 4;
        settingsPanel.add(comboBox, gbc_comboBox);
        
        JLabel lblFov = new JLabel("FOV:");
        GridBagConstraints gbc_lblFov = new GridBagConstraints();
        gbc_lblFov.insets = new Insets(0, 0, 5, 5);
        gbc_lblFov.gridx = 2;
        gbc_lblFov.gridy = 4;
        settingsPanel.add(lblFov, gbc_lblFov);
        
        JSpinner spinner = new JSpinner();
        GridBagConstraints gbc_spinner = new GridBagConstraints();
        gbc_spinner.insets = new Insets(0, 0, 5, 5);
        gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
        gbc_spinner.gridx = 3;
        gbc_spinner.gridy = 4;
        settingsPanel.add(spinner, gbc_spinner);
        
        JPanel panel_2 = new JPanel();
        GridBagConstraints gbc_panel_2 = new GridBagConstraints();
        gbc_panel_2.gridwidth = 5;
        gbc_panel_2.insets = new Insets(0, 5, 5, 5);
        gbc_panel_2.fill = GridBagConstraints.BOTH;
        gbc_panel_2.gridx = 0;
        gbc_panel_2.gridy = 5;
        settingsPanel.add(panel_2, gbc_panel_2);
        
        JPanel customPanel = new JPanel();
        customTabbedPane.addTab("Custom", null, customPanel, null);
        
        JPanel skyboxPanel = new JPanel();
        customTabbedPane.addTab("Skybox", null, skyboxPanel, null);
        
        JPanel particlesPanel = new JPanel();
        customTabbedPane.addTab("Particles", null, particlesPanel, null);
        
        JPanel sidePanel = new JPanel();
        add(sidePanel, BorderLayout.EAST);
        sidePanel.setLayout(new BorderLayout(0, 0));
        
        JPanel northPanel = new JPanel();
        sidePanel.add(northPanel, BorderLayout.NORTH);
        northPanel.setBorder(new TitledBorder(null, "Profiles", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        GridBagLayout gbl_northPanel = new GridBagLayout();
        gbl_northPanel.columnWidths = new int[]{34, 28, 0};
        gbl_northPanel.rowHeights = new int[]{23, 0, 0};
        gbl_northPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gbl_northPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        northPanel.setLayout(gbl_northPanel);
        
        JLabel lblProfile = new JLabel("Profile:");
        GridBagConstraints gbc_lblProfile = new GridBagConstraints();
        gbc_lblProfile.anchor = GridBagConstraints.WEST;
        gbc_lblProfile.insets = new Insets(0, 5, 5, 5);
        gbc_lblProfile.gridx = 0;
        gbc_lblProfile.gridy = 0;
        northPanel.add(lblProfile, gbc_lblProfile);
        
        JComboBox profiles = new JComboBox();
        GridBagConstraints gbc_profiles = new GridBagConstraints();
        gbc_profiles.fill = GridBagConstraints.HORIZONTAL;
        gbc_profiles.insets = new Insets(0, 0, 5, 5);
        gbc_profiles.gridx = 1;
        gbc_profiles.gridy = 0;
        northPanel.add(profiles, gbc_profiles);
        
        JPanel panel_1 = new JPanel();
        GridBagConstraints gbc_panel_1 = new GridBagConstraints();
        gbc_panel_1.gridwidth = 2;
        gbc_panel_1.fill = GridBagConstraints.BOTH;
        gbc_panel_1.insets = new Insets(0, 5, 5, 5);
        gbc_panel_1.gridx = 0;
        gbc_panel_1.gridy = 1;
        northPanel.add(panel_1, gbc_panel_1);
        GridBagLayout gbl_panel_1 = new GridBagLayout();
        gbl_panel_1.columnWidths = new int[]{87, 83, 0};
        gbl_panel_1.rowHeights = new int[]{23, 0};
        gbl_panel_1.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        panel_1.setLayout(gbl_panel_1);
        
        JButton btnNewProfile = new JButton("New Profile");
        GridBagConstraints gbc_btnNewProfile = new GridBagConstraints();
        gbc_btnNewProfile.anchor = GridBagConstraints.NORTHWEST;
        gbc_btnNewProfile.insets = new Insets(0, 0, 0, 5);
        gbc_btnNewProfile.gridx = 0;
        gbc_btnNewProfile.gridy = 0;
        panel_1.add(btnNewProfile, gbc_btnNewProfile);
        
        JButton btnEditProfile = new JButton("Edit Profile");
        GridBagConstraints gbc_btnEditProfile = new GridBagConstraints();
        gbc_btnEditProfile.anchor = GridBagConstraints.NORTHWEST;
        gbc_btnEditProfile.gridx = 1;
        gbc_btnEditProfile.gridy = 0;
        panel_1.add(btnEditProfile, gbc_btnEditProfile);
        
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "Start Game", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        sidePanel.add(panel, BorderLayout.SOUTH);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[]{0, 0, 0};
        gbl_panel.rowHeights = new int[]{0, 0, 0};
        gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_panel);
        
        JLabel lblStatus = new JLabel("Status:");
        GridBagConstraints gbc_lblStatus = new GridBagConstraints();
        gbc_lblStatus.insets = new Insets(0, 5, 5, 5);
        gbc_lblStatus.gridx = 0;
        gbc_lblStatus.gridy = 0;
        panel.add(lblStatus, gbc_lblStatus);
        
        JLabel lblStatus_1 = new JLabel("Status");
        GridBagConstraints gbc_lblStatus_1 = new GridBagConstraints();
        gbc_lblStatus_1.anchor = GridBagConstraints.WEST;
        gbc_lblStatus_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblStatus_1.gridx = 1;
        gbc_lblStatus_1.gridy = 0;
        panel.add(lblStatus_1, gbc_lblStatus_1);
        
        JButton btnLaunchGame = new JButton("Launch Game");
        GridBagConstraints gbc_btnLaunchGame = new GridBagConstraints();
        gbc_btnLaunchGame.fill = GridBagConstraints.HORIZONTAL;
        gbc_btnLaunchGame.gridwidth = 2;
        gbc_btnLaunchGame.insets = new Insets(0, 5, 5, 5);
        gbc_btnLaunchGame.gridx = 0;
        gbc_btnLaunchGame.gridy = 1;
        panel.add(btnLaunchGame, gbc_btnLaunchGame);

    }

}
