package com.github.lawena.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.github.lawena.Constants;
import com.github.lawena.Messages;
import com.github.lawena.app.task.LinkRunner;

public class LawenaViewTf extends JFrame implements LawenaView {

  private static final long serialVersionUID = 1L;

  private static class MntmRenderingTutorialActionListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      new LinkRunner(renderingTutorialURL).execute();
    }
  }

  private static class MntmPatchNotesActionListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      new LinkRunner(releasesURL).execute();
    }
  }

  private static class MntmProjectPageActionListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      new LinkRunner(projectPageURL).execute();
    }
  }

  private static class MntmVdmTutorialActionListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      new LinkRunner(vdmTutorialURL).execute();
    }
  }

  private static class MntmInstructionsActionListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      new LinkRunner(instructionsURL).execute();
    }
  }

  private JComboBox<String> cmbHud;
  private JComboBox<String> cmbQuality;
  private JComboBox<String> cmbSkybox;
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
  private JLabel lblStatus;
  private JComboBox<String> cmbViewmodel;
  private JProgressBar progressBar;
  private JMenuItem mntmExit;
  private JMenuItem mntmAbout;
  private JCheckBox useHudMinmode;
  private JCheckBox usePlayerModel;
  private JComboBox<String> cmbSourceVideoFormat;
  private JSpinner spinnerJpegQuality;
  private JLabel lblViewmodelFov;
  private JLabel lblJpegQuality;
  private JComboBox<String> cmbResolution;
  private JComboBox<String> cmbFramerate;
  private JTable tableCustomContent;
  private JTabbedPane tabbedPane;
  private JMenuItem mntmChangeTfDirectory;
  private JMenuItem mntmChangeMovieDirectory;
  private JMenuItem mntmSaveSettings;
  private JMenuItem mntmAddCustomSettings;
  private JMenuItem mntmOpenMovieFolder;
  private JMenuItem mntmOpenCustomFolder;
  private JMenuItem mntmChangeTfLaunch;
  private JCheckBoxMenuItem chckbxmntmInsecure;
  private JMenuItem mntmRevertToDefault;
  private JCheckBoxMenuItem chckbxmntmBackupMode;
  private JMenuItem checkForUpdatesMenuItem;
  private JMenuItem switchUpdaterBranchMenuItem;
  private JMenuItem showLogMenuItem;
  private JMenuItem customLaunchOptionsMenuItem;
  private JCheckBoxMenuItem checkRememberGame;

  public LawenaViewTf() {
    super();
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);

    JMenu mnFile = new JMenu(Messages.getString("LawenaView.fileMenu")); //$NON-NLS-1$
    menuBar.add(mnFile);

    JMenuItem mntmChangeTfDirectory =
        new JMenuItem(Messages.getString("LawenaView.changeGameFolder")); //$NON-NLS-1$
    mnFile.add(mntmChangeTfDirectory);

    JMenuItem mntmChangeMovieDirectory =
        new JMenuItem(Messages.getString("LawenaView.changeFramesFolder")); //$NON-NLS-1$
    mnFile.add(mntmChangeMovieDirectory);

    JSeparator separator_4 = new JSeparator();
    mnFile.add(separator_4);

    JMenuItem mntmOpenMovieFolder =
        new JMenuItem(Messages.getString("LawenaView.openFramesFolder")); //$NON-NLS-1$
    mnFile.add(mntmOpenMovieFolder);

    JMenuItem mntmOpenCustomFolder =
        new JMenuItem(Messages.getString("LawenaView.openCustomFolder")); //$NON-NLS-1$
    mnFile.add(mntmOpenCustomFolder);

    JSeparator separator = new JSeparator();
    mnFile.add(separator);

    JMenuItem mntmSaveSettings = new JMenuItem(Messages.getString("LawenaView.saveSettings")); //$NON-NLS-1$
    mntmSaveSettings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
    mnFile.add(mntmSaveSettings);

    JMenuItem mntmRevertToDefault =
        new JMenuItem(Messages.getString("LawenaView.revertToDefaultSettings")); //$NON-NLS-1$
    mnFile.add(mntmRevertToDefault);

    JSeparator separator_1 = new JSeparator();
    mnFile.add(separator_1);

    JMenuItem mntmExit = new JMenuItem(Messages.getString("LawenaView.exit")); //$NON-NLS-1$
    mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
    mnFile.add(mntmExit);

    JMenu mnAdvanced = new JMenu(Messages.getString("LawenaView.advancedMenu")); //$NON-NLS-1$
    menuBar.add(mnAdvanced);

    JMenuItem mntmAddCustomSettings =
        new JMenuItem(Messages.getString("LawenaView.customSettings")); //$NON-NLS-1$
    mnAdvanced.add(mntmAddCustomSettings);

    JMenuItem customLaunchOptionsMenuItem =
        new JMenuItem(Messages.getString("LawenaView.customLaunchOptions")); //$NON-NLS-1$
    mnAdvanced.add(customLaunchOptionsMenuItem);

    JSeparator separator_3 = new JSeparator();
    mnAdvanced.add(separator_3);

    JCheckBoxMenuItem chckbxmntmInsecure =
        new JCheckBoxMenuItem(Messages.getString("LawenaView.useInsecure")); //$NON-NLS-1$
    mnAdvanced.add(chckbxmntmInsecure);

    JCheckBoxMenuItem chckbxmntmBackupMode =
        new JCheckBoxMenuItem(Messages.getString("LawenaView.deleteBackupFilesOnRestoreSuccess")); //$NON-NLS-1$
    mnAdvanced.add(chckbxmntmBackupMode);

    JCheckBoxMenuItem checkRememberGame =
        new JCheckBoxMenuItem(Messages.getString("LawenaView.rememberSelectedGame")); //$NON-NLS-1$
    mnAdvanced.add(checkRememberGame);

    JSeparator separator_5 = new JSeparator();
    mnAdvanced.add(separator_5);

    JMenuItem mntmChangeTfLaunch =
        new JMenuItem(Messages.getString("LawenaView.changeGameLaunchTimeout")); //$NON-NLS-1$
    mnAdvanced.add(mntmChangeTfLaunch);

    JMenu mnHelp = new JMenu(Messages.getString("LawenaView.helpMenu")); //$NON-NLS-1$
    menuBar.add(mnHelp);

    JMenuItem showLogMenuItem = new JMenuItem(Messages.getString("LawenaView.showLog")); //$NON-NLS-1$
    showLogMenuItem.setIcon(new ImageIcon(LawenaViewTf.class
        .getResource("/com/github/lawena/ui/fugue/clock.png"))); //$NON-NLS-1$
    mnHelp.add(showLogMenuItem);

    JSeparator separator_7 = new JSeparator();
    mnHelp.add(separator_7);

    JMenuItem mntmInstructions = new JMenuItem(Messages.getString("LawenaView.instructions")); //$NON-NLS-1$
    mntmInstructions.addActionListener(new MntmInstructionsActionListener());
    mnHelp.add(mntmInstructions);

    JMenuItem mntmVdmTutorial = new JMenuItem(Messages.getString("LawenaView.vdmTutorial")); //$NON-NLS-1$
    mntmVdmTutorial.addActionListener(new MntmVdmTutorialActionListener());
    mnHelp.add(mntmVdmTutorial);

    JMenuItem mntmRenderingTutorial =
        new JMenuItem(Messages.getString("LawenaView.renderingTutorial")); //$NON-NLS-1$
    mntmRenderingTutorial.addActionListener(new MntmRenderingTutorialActionListener());
    mnHelp.add(mntmRenderingTutorial);

    JSeparator separator_2 = new JSeparator();
    mnHelp.add(separator_2);

    JMenuItem checkForUpdatesMenuItem =
        new JMenuItem(Messages.getString("LawenaView.checkForUpdates")); //$NON-NLS-1$
    mnHelp.add(checkForUpdatesMenuItem);

    JMenuItem switchUpdaterBranchMenuItem =
        new JMenuItem(Messages.getString("LawenaView.switchUpdaterBranch")); //$NON-NLS-1$
    mnHelp.add(switchUpdaterBranchMenuItem);

    JMenuItem mntmPatchNotes = new JMenuItem(Messages.getString("LawenaView.releaseNotes")); //$NON-NLS-1$
    mntmPatchNotes.addActionListener(new MntmPatchNotesActionListener());
    mnHelp.add(mntmPatchNotes);

    JMenuItem mntmProjectPage = new JMenuItem(Messages.getString("LawenaView.projectPage")); //$NON-NLS-1$
    mntmProjectPage.addActionListener(new MntmProjectPageActionListener());
    mnHelp.add(mntmProjectPage);

    JSeparator separator_6 = new JSeparator();
    mnHelp.add(separator_6);

    JMenuItem mntmAbout = new JMenuItem(Messages.getString("LawenaView.about")); //$NON-NLS-1$
    mnHelp.add(mntmAbout);
    JPanel contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    contentPane.setLayout(new BorderLayout(5, 5));
    contentPane.setPreferredSize(new Dimension(650, 400));
    setContentPane(contentPane);

    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    contentPane.add(tabbedPane, BorderLayout.CENTER);

    JPanel panelSettings = new JPanel();
    tabbedPane.addTab(Messages.getString("LawenaView.settings"), null, panelSettings, null); //$NON-NLS-1$
    GridBagLayout gbl_panelSettings = new GridBagLayout();
    gbl_panelSettings.columnWidths = new int[] {0, 1, 1, 1, 0, 1, 1, 0};
    gbl_panelSettings.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    gbl_panelSettings.columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 10.0};
    gbl_panelSettings.rowWeights =
        new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
    panelSettings.setLayout(gbl_panelSettings);

    JLabel lblResolution = new JLabel(Messages.getString("LawenaView.resolution")); //$NON-NLS-1$
    GridBagConstraints gbc_lblResolution = new GridBagConstraints();
    gbc_lblResolution.insets = new Insets(5, 5, 5, 5);
    gbc_lblResolution.anchor = GridBagConstraints.EAST;
    gbc_lblResolution.gridx = 0;
    gbc_lblResolution.gridy = 0;
    panelSettings.add(lblResolution, gbc_lblResolution);

    JComboBox<String> cmbResolution = new JComboBox<>();
    cmbResolution.setToolTipText(Messages.getString("LawenaView.resolutionTooltip")); //$NON-NLS-1$
    cmbResolution.setModel(new DefaultComboBoxModel<>(Constants.RESOLUTIONS));
    cmbResolution.setEditable(true);
    GridBagConstraints gbc_cmbResolution = new GridBagConstraints();
    gbc_cmbResolution.gridwidth = 3;
    gbc_cmbResolution.fill = GridBagConstraints.HORIZONTAL;
    gbc_cmbResolution.insets = new Insets(5, 0, 5, 5);
    gbc_cmbResolution.gridx = 1;
    gbc_cmbResolution.gridy = 0;
    panelSettings.add(cmbResolution, gbc_cmbResolution);

    JLabel lblFrameRate = new JLabel(Messages.getString("LawenaView.fps")); //$NON-NLS-1$
    GridBagConstraints gbc_lblFrameRate = new GridBagConstraints();
    gbc_lblFrameRate.anchor = GridBagConstraints.EAST;
    gbc_lblFrameRate.insets = new Insets(5, 0, 5, 5);
    gbc_lblFrameRate.gridx = 4;
    gbc_lblFrameRate.gridy = 0;
    panelSettings.add(lblFrameRate, gbc_lblFrameRate);

    JComboBox<String> cmbFramerate = new JComboBox<>();
    cmbFramerate.setToolTipText(Messages.getString("LawenaView.fpsTooltip")); //$NON-NLS-1$
    cmbFramerate.setModel(new DefaultComboBoxModel<>(Constants.FPS));
    cmbFramerate.setEditable(true);
    GridBagConstraints gbc_cmbFramerate = new GridBagConstraints();
    gbc_cmbFramerate.fill = GridBagConstraints.HORIZONTAL;
    gbc_cmbFramerate.insets = new Insets(5, 0, 5, 5);
    gbc_cmbFramerate.gridx = 5;
    gbc_cmbFramerate.gridy = 0;
    panelSettings.add(cmbFramerate, gbc_cmbFramerate);

    JPanel panelCustomContent = new JPanel();
    GridBagConstraints gbc_panelCustomContent = new GridBagConstraints();
    gbc_panelCustomContent.insets = new Insets(5, 0, 3, 0);
    gbc_panelCustomContent.gridwidth = 2;
    gbc_panelCustomContent.gridheight = 10;
    gbc_panelCustomContent.fill = GridBagConstraints.BOTH;
    gbc_panelCustomContent.gridx = 6;
    gbc_panelCustomContent.gridy = 0;
    panelSettings.add(panelCustomContent, gbc_panelCustomContent);
    GridBagLayout gbl_panelCustomContent = new GridBagLayout();
    gbl_panelCustomContent.columnWidths = new int[] {0, 0};
    gbl_panelCustomContent.rowHeights = new int[] {0, 0};
    gbl_panelCustomContent.columnWeights = new double[] {1.0, Double.MIN_VALUE};
    gbl_panelCustomContent.rowWeights = new double[] {1.0, Double.MIN_VALUE};
    panelCustomContent.setLayout(gbl_panelCustomContent);

    JScrollPane scrollPane = new JScrollPane();
    GridBagConstraints gbc_scrollPane = new GridBagConstraints();
    gbc_scrollPane.fill = GridBagConstraints.BOTH;
    gbc_scrollPane.gridx = 0;
    gbc_scrollPane.gridy = 0;
    panelCustomContent.add(scrollPane, gbc_scrollPane);

    JTable tableCustomContent = new JTable();
    tableCustomContent.setShowVerticalLines(false);
    tableCustomContent.setGridColor(new Color(0, 0, 0, 30));
    tableCustomContent.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    tableCustomContent.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tableCustomContent.getTableHeader().setReorderingAllowed(false);
    scrollPane.setViewportView(tableCustomContent);

    JLabel lblHud = new JLabel(Messages.getString("LawenaView.hud")); //$NON-NLS-1$
    GridBagConstraints gbc_lblHud = new GridBagConstraints();
    gbc_lblHud.anchor = GridBagConstraints.EAST;
    gbc_lblHud.insets = new Insets(0, 5, 5, 5);
    gbc_lblHud.gridx = 0;
    gbc_lblHud.gridy = 1;
    panelSettings.add(lblHud, gbc_lblHud);

    JComboBox<String> cmbHud = new JComboBox<>();
    cmbHud.setToolTipText(Messages.getString("LawenaView.hudTooltip")); //$NON-NLS-1$
    cmbHud.setModel(new DefaultComboBoxModel<>(Constants.TF_HUD_OPTIONS));
    GridBagConstraints gbc_cmbHud = new GridBagConstraints();
    gbc_cmbHud.gridwidth = 3;
    gbc_cmbHud.fill = GridBagConstraints.HORIZONTAL;
    gbc_cmbHud.insets = new Insets(0, 0, 5, 5);
    gbc_cmbHud.gridx = 1;
    gbc_cmbHud.gridy = 1;
    panelSettings.add(cmbHud, gbc_cmbHud);

    JLabel lblDxLevel = new JLabel(Messages.getString("LawenaView.dxlevel")); //$NON-NLS-1$
    GridBagConstraints gbc_lblDxLevel = new GridBagConstraints();
    gbc_lblDxLevel.anchor = GridBagConstraints.EAST;
    gbc_lblDxLevel.insets = new Insets(0, 5, 5, 5);
    gbc_lblDxLevel.gridx = 4;
    gbc_lblDxLevel.gridy = 1;
    panelSettings.add(lblDxLevel, gbc_lblDxLevel);

    JComboBox<String> cmbQuality = new JComboBox<>();
    cmbQuality.setModel(new DefaultComboBoxModel<>(Constants.DXLEVELS));
    GridBagConstraints gbc_cmbQuality = new GridBagConstraints();
    gbc_cmbQuality.fill = GridBagConstraints.HORIZONTAL;
    gbc_cmbQuality.insets = new Insets(0, 0, 5, 5);
    gbc_cmbQuality.gridx = 5;
    gbc_cmbQuality.gridy = 1;
    panelSettings.add(cmbQuality, gbc_cmbQuality);

    JLabel lblSkybox = new JLabel(Messages.getString("LawenaView.skybox")); //$NON-NLS-1$
    GridBagConstraints gbc_lblSkybox = new GridBagConstraints();
    gbc_lblSkybox.anchor = GridBagConstraints.EAST;
    gbc_lblSkybox.insets = new Insets(0, 5, 5, 5);
    gbc_lblSkybox.gridx = 0;
    gbc_lblSkybox.gridy = 2;
    panelSettings.add(lblSkybox, gbc_lblSkybox);

    JComboBox<String> cmbSkybox = new JComboBox<>();
    GridBagConstraints gbc_cmbSkybox = new GridBagConstraints();
    gbc_cmbSkybox.gridwidth = 5;
    gbc_cmbSkybox.fill = GridBagConstraints.HORIZONTAL;
    gbc_cmbSkybox.insets = new Insets(0, 0, 5, 5);
    gbc_cmbSkybox.gridx = 1;
    gbc_cmbSkybox.gridy = 2;
    panelSettings.add(cmbSkybox, gbc_cmbSkybox);

    JLabel lblViewmodels = new JLabel(Messages.getString("LawenaView.viewmodels")); //$NON-NLS-1$
    GridBagConstraints gbc_lblViewmodels = new GridBagConstraints();
    gbc_lblViewmodels.anchor = GridBagConstraints.EAST;
    gbc_lblViewmodels.insets = new Insets(0, 5, 5, 5);
    gbc_lblViewmodels.gridx = 0;
    gbc_lblViewmodels.gridy = 3;
    panelSettings.add(lblViewmodels, gbc_lblViewmodels);

    JComboBox<String> cmbViewmodel = new JComboBox<>();
    cmbViewmodel.setModel(new DefaultComboBoxModel<>(Constants.VIEWMODELS));
    GridBagConstraints gbc_cmbViewmodel = new GridBagConstraints();
    gbc_cmbViewmodel.insets = new Insets(0, 0, 5, 5);
    gbc_cmbViewmodel.fill = GridBagConstraints.HORIZONTAL;
    gbc_cmbViewmodel.gridx = 1;
    gbc_cmbViewmodel.gridy = 3;
    panelSettings.add(cmbViewmodel, gbc_cmbViewmodel);

    JLabel lblViewmodelFov = new JLabel(Messages.getString("LawenaView.viewmodelFov")); //$NON-NLS-1$
    GridBagConstraints gbc_lblViewmodelFov = new GridBagConstraints();
    gbc_lblViewmodelFov.anchor = GridBagConstraints.EAST;
    gbc_lblViewmodelFov.insets = new Insets(0, 5, 5, 5);
    gbc_lblViewmodelFov.gridx = 2;
    gbc_lblViewmodelFov.gridy = 3;
    panelSettings.add(lblViewmodelFov, gbc_lblViewmodelFov);

    JSpinner spinnerViewmodelFov = new JSpinner();
    GridBagConstraints gbc_spinnerViewmodelFov = new GridBagConstraints();
    gbc_spinnerViewmodelFov.fill = GridBagConstraints.HORIZONTAL;
    gbc_spinnerViewmodelFov.insets = new Insets(0, 0, 5, 5);
    gbc_spinnerViewmodelFov.gridx = 3;
    gbc_spinnerViewmodelFov.gridy = 3;
    panelSettings.add(spinnerViewmodelFov, gbc_spinnerViewmodelFov);
    spinnerViewmodelFov.setModel(new SpinnerNumberModel(70, 1, 179, 1));

    Component horizontalStrut = Box.createHorizontalStrut(24);
    GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
    gbc_horizontalStrut.insets = new Insets(0, 0, 5, 5);
    gbc_horizontalStrut.gridx = 4;
    gbc_horizontalStrut.gridy = 3;
    panelSettings.add(horizontalStrut, gbc_horizontalStrut);

    JLabel lblOutput = new JLabel(Messages.getString("LawenaView.frameOutputFormat")); //$NON-NLS-1$
    GridBagConstraints gbc_lblOutput = new GridBagConstraints();
    gbc_lblOutput.anchor = GridBagConstraints.EAST;
    gbc_lblOutput.insets = new Insets(0, 5, 5, 5);
    gbc_lblOutput.gridx = 0;
    gbc_lblOutput.gridy = 4;
    panelSettings.add(lblOutput, gbc_lblOutput);

    JComboBox<String> cmbSourceVideoFormat = new JComboBox<>();
    cmbSourceVideoFormat.setModel(new DefaultComboBoxModel<>(Constants.FRAME_OUTPUT_FORMATS));
    GridBagConstraints gbc_cmbSourceVideoFormat = new GridBagConstraints();
    gbc_cmbSourceVideoFormat.insets = new Insets(0, 0, 5, 5);
    gbc_cmbSourceVideoFormat.fill = GridBagConstraints.HORIZONTAL;
    gbc_cmbSourceVideoFormat.gridx = 1;
    gbc_cmbSourceVideoFormat.gridy = 4;
    panelSettings.add(cmbSourceVideoFormat, gbc_cmbSourceVideoFormat);

    JLabel lblJpegQuality = new JLabel(Messages.getString("LawenaView.jpegQuality")); //$NON-NLS-1$
    GridBagConstraints gbc_lblJpegQuality = new GridBagConstraints();
    gbc_lblJpegQuality.anchor = GridBagConstraints.EAST;
    gbc_lblJpegQuality.insets = new Insets(0, 0, 5, 5);
    gbc_lblJpegQuality.gridx = 2;
    gbc_lblJpegQuality.gridy = 4;
    panelSettings.add(lblJpegQuality, gbc_lblJpegQuality);

    JSpinner spinnerJpegQuality = new JSpinner();
    spinnerJpegQuality.setModel(new SpinnerNumberModel(50, 1, 100, 1));
    GridBagConstraints gbc_spinnerJpegQuality = new GridBagConstraints();
    gbc_spinnerJpegQuality.fill = GridBagConstraints.HORIZONTAL;
    gbc_spinnerJpegQuality.insets = new Insets(0, 0, 5, 5);
    gbc_spinnerJpegQuality.gridx = 3;
    gbc_spinnerJpegQuality.gridy = 4;
    panelSettings.add(spinnerJpegQuality, gbc_spinnerJpegQuality);

    Component verticalStrut = Box.createVerticalStrut(22);
    GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
    gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
    gbc_verticalStrut.gridx = 0;
    gbc_verticalStrut.gridy = 5;
    panelSettings.add(verticalStrut, gbc_verticalStrut);

    JPanel panelCheckboxes = new JPanel();
    panelCheckboxes
        .setBorder(new TitledBorder(
            UIManager.getBorder("TitledBorder.border"), //$NON-NLS-1$
            Messages.getString("LawenaView.additionalSettings"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    GridBagConstraints gbc_panelCheckboxes = new GridBagConstraints();
    gbc_panelCheckboxes.insets = new Insets(0, 0, 0, 5);
    gbc_panelCheckboxes.gridheight = 4;
    gbc_panelCheckboxes.gridwidth = 6;
    gbc_panelCheckboxes.fill = GridBagConstraints.BOTH;
    gbc_panelCheckboxes.gridx = 0;
    gbc_panelCheckboxes.gridy = 6;
    panelSettings.add(panelCheckboxes, gbc_panelCheckboxes);
    GridBagLayout gbl_panelCheckboxes = new GridBagLayout();
    gbl_panelCheckboxes.columnWidths = new int[] {0, 0, 0};
    gbl_panelCheckboxes.rowHeights = new int[] {0, 0, 0, 0, 0};
    gbl_panelCheckboxes.columnWeights = new double[] {1.0, 1.0, Double.MIN_VALUE};
    gbl_panelCheckboxes.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
    panelCheckboxes.setLayout(gbl_panelCheckboxes);

    JCheckBox enableMotionBlur = new JCheckBox(Messages.getString("LawenaView.enableMotionBlur")); //$NON-NLS-1$
    GridBagConstraints gbc_enableMotionBlur = new GridBagConstraints();
    gbc_enableMotionBlur.insets = new Insets(0, 0, 0, 5);
    gbc_enableMotionBlur.anchor = GridBagConstraints.WEST;
    gbc_enableMotionBlur.gridx = 0;
    gbc_enableMotionBlur.gridy = 0;
    panelCheckboxes.add(enableMotionBlur, gbc_enableMotionBlur);

    JCheckBox disableCombatText = new JCheckBox(Messages.getString("LawenaView.disableCombatText")); //$NON-NLS-1$
    GridBagConstraints gbc_disableCombatText = new GridBagConstraints();
    gbc_disableCombatText.anchor = GridBagConstraints.WEST;
    gbc_disableCombatText.gridx = 1;
    gbc_disableCombatText.gridy = 0;
    panelCheckboxes.add(disableCombatText, gbc_disableCombatText);

    JCheckBox disableHitSounds = new JCheckBox(Messages.getString("LawenaView.disableHitSounds")); //$NON-NLS-1$
    GridBagConstraints gbc_disableHitSounds = new GridBagConstraints();
    gbc_disableHitSounds.insets = new Insets(0, 0, 0, 5);
    gbc_disableHitSounds.anchor = GridBagConstraints.WEST;
    gbc_disableHitSounds.gridx = 0;
    gbc_disableHitSounds.gridy = 1;
    panelCheckboxes.add(disableHitSounds, gbc_disableHitSounds);

    JCheckBox disableCrosshair = new JCheckBox(Messages.getString("LawenaView.disableCrosshair")); //$NON-NLS-1$
    GridBagConstraints gbc_disableCrosshair = new GridBagConstraints();
    gbc_disableCrosshair.anchor = GridBagConstraints.WEST;
    gbc_disableCrosshair.gridx = 1;
    gbc_disableCrosshair.gridy = 1;
    panelCheckboxes.add(disableCrosshair, gbc_disableCrosshair);

    JCheckBox disableVoiceChat = new JCheckBox(Messages.getString("LawenaView.disableVoiceChat")); //$NON-NLS-1$
    GridBagConstraints gbc_disableVoiceChat = new GridBagConstraints();
    gbc_disableVoiceChat.insets = new Insets(0, 0, 0, 5);
    gbc_disableVoiceChat.anchor = GridBagConstraints.WEST;
    gbc_disableVoiceChat.gridx = 0;
    gbc_disableVoiceChat.gridy = 2;
    panelCheckboxes.add(disableVoiceChat, gbc_disableVoiceChat);

    JCheckBox disableCrosshairSwitch =
        new JCheckBox(Messages.getString("LawenaView.disableCrosshairSwitching")); //$NON-NLS-1$
    GridBagConstraints gbc_disableCrosshairSwitch = new GridBagConstraints();
    gbc_disableCrosshairSwitch.anchor = GridBagConstraints.WEST;
    gbc_disableCrosshairSwitch.gridx = 1;
    gbc_disableCrosshairSwitch.gridy = 2;
    panelCheckboxes.add(disableCrosshairSwitch, gbc_disableCrosshairSwitch);

    JCheckBox useHudMinmode = new JCheckBox(Messages.getString("LawenaView.useMinimalHud")); //$NON-NLS-1$
    useHudMinmode.setToolTipText(Messages.getString("LawenaView.useMinimalHudTooltip")); //$NON-NLS-1$
    GridBagConstraints gbc_chckbxUseHudMin = new GridBagConstraints();
    gbc_chckbxUseHudMin.anchor = GridBagConstraints.WEST;
    gbc_chckbxUseHudMin.insets = new Insets(0, 0, 0, 5);
    gbc_chckbxUseHudMin.gridx = 0;
    gbc_chckbxUseHudMin.gridy = 3;
    panelCheckboxes.add(useHudMinmode, gbc_chckbxUseHudMin);

    JCheckBox usePlayerModel = new JCheckBox(Messages.getString("LawenaView.usePlayerModel")); //$NON-NLS-1$
    usePlayerModel.setToolTipText(Messages.getString("LawenaView.usePlayerModelTooltip")); //$NON-NLS-1$
    GridBagConstraints gbc_usePlayerModel = new GridBagConstraints();
    gbc_usePlayerModel.anchor = GridBagConstraints.WEST;
    gbc_usePlayerModel.gridx = 1;
    gbc_usePlayerModel.gridy = 3;
    panelCheckboxes.add(usePlayerModel, gbc_usePlayerModel);

    JPanel panelBottomLeft = new JPanel();
    FlowLayout fl_panelBottomLeft = (FlowLayout) panelBottomLeft.getLayout();
    fl_panelBottomLeft.setVgap(0);
    fl_panelBottomLeft.setHgap(0);
    GridBagConstraints gbc_panelBottomLeft = new GridBagConstraints();
    gbc_panelBottomLeft.anchor = GridBagConstraints.WEST;
    gbc_panelBottomLeft.gridwidth = 6;
    gbc_panelBottomLeft.fill = GridBagConstraints.VERTICAL;
    gbc_panelBottomLeft.gridx = 0;
    gbc_panelBottomLeft.gridy = 10;
    panelSettings.add(panelBottomLeft, gbc_panelBottomLeft);

    JButton btnClearMovieFolder = new JButton(Messages.getString("LawenaView.deleteMovieSegments")); //$NON-NLS-1$
    panelBottomLeft.add(btnClearMovieFolder);

    JPanel panelBottomRight = new JPanel();
    FlowLayout fl_panelBottomRight = (FlowLayout) panelBottomRight.getLayout();
    fl_panelBottomRight.setVgap(0);
    fl_panelBottomRight.setHgap(0);
    GridBagConstraints gbc_panelBottomRight = new GridBagConstraints();
    gbc_panelBottomRight.gridwidth = 2;
    gbc_panelBottomRight.anchor = GridBagConstraints.EAST;
    gbc_panelBottomRight.fill = GridBagConstraints.VERTICAL;
    gbc_panelBottomRight.gridx = 6;
    gbc_panelBottomRight.gridy = 10;
    panelSettings.add(panelBottomRight, gbc_panelBottomRight);

    JButton btnStartTf = new JButton(Messages.getString("LawenaView.launchGame")); //$NON-NLS-1$
    panelBottomRight.add(btnStartTf);

    JPanel panelStatusbar = new JPanel();
    contentPane.add(panelStatusbar, BorderLayout.SOUTH);
    GridBagLayout gbl_panelStatusbar = new GridBagLayout();
    gbl_panelStatusbar.columnWidths = new int[] {31, 0, 0, 0};
    gbl_panelStatusbar.rowHeights = new int[] {12, 0};
    gbl_panelStatusbar.columnWeights = new double[] {0.0, 1.0, 0.0, Double.MIN_VALUE};
    gbl_panelStatusbar.rowWeights = new double[] {0.0, Double.MIN_VALUE};
    panelStatusbar.setLayout(gbl_panelStatusbar);

    JLabel lblStatus = new JLabel(""); //$NON-NLS-1$
    GridBagConstraints gbc_lblStatus = new GridBagConstraints();
    gbc_lblStatus.insets = new Insets(0, 5, 0, 5);
    gbc_lblStatus.anchor = GridBagConstraints.NORTHWEST;
    gbc_lblStatus.gridx = 0;
    gbc_lblStatus.gridy = 0;
    panelStatusbar.add(lblStatus, gbc_lblStatus);

    JProgressBar progressBar = new JProgressBar();
    progressBar.setVisible(false);
    GridBagConstraints gbc_progressBar = new GridBagConstraints();
    gbc_progressBar.anchor = GridBagConstraints.EAST;
    gbc_progressBar.gridx = 2;
    gbc_progressBar.gridy = 0;
    panelStatusbar.add(progressBar, gbc_progressBar);

    this.cmbResolution = cmbResolution;
    this.cmbSkybox = cmbSkybox;
    this.cmbHud = cmbHud;
    this.cmbFramerate = cmbFramerate;
    this.cmbQuality = cmbQuality;
    this.spinnerViewmodelFov = spinnerViewmodelFov;
    this.enableMotionBlur = enableMotionBlur;
    this.disableCrosshair = disableCrosshair;
    this.disableCrosshairSwitch = disableCrosshairSwitch;
    this.disableCombatText = disableCombatText;
    this.disableHitSounds = disableHitSounds;
    this.disableVoiceChat = disableVoiceChat;
    this.btnStartTf = btnStartTf;
    this.lblResolution = lblResolution;
    this.lblFrameRate = lblFrameRate;
    this.btnClearMovieFolder = btnClearMovieFolder;
    this.tableCustomContent = tableCustomContent;
    this.tabbedPane = tabbedPane;
    this.mntmChangeTfDirectory = mntmChangeTfDirectory;
    this.mntmChangeMovieDirectory = mntmChangeMovieDirectory;
    this.lblStatus = lblStatus;
    this.cmbViewmodel = cmbViewmodel;
    this.progressBar = progressBar;
    this.mntmRevertToDefault = mntmRevertToDefault;
    this.mntmExit = mntmExit;
    this.mntmAbout = mntmAbout;
    this.mntmSaveSettings = mntmSaveSettings;
    this.useHudMinmode = useHudMinmode;
    this.mntmAddCustomSettings = mntmAddCustomSettings;
    this.mntmOpenMovieFolder = mntmOpenMovieFolder;
    this.mntmOpenCustomFolder = mntmOpenCustomFolder;
    this.mntmChangeTfLaunch = mntmChangeTfLaunch;
    this.chckbxmntmInsecure = chckbxmntmInsecure;
    this.usePlayerModel = usePlayerModel;
    this.cmbSourceVideoFormat = cmbSourceVideoFormat;
    this.spinnerJpegQuality = spinnerJpegQuality;
    this.lblViewmodelFov = lblViewmodelFov;
    this.lblJpegQuality = lblJpegQuality;
    this.chckbxmntmBackupMode = chckbxmntmBackupMode;
    this.checkForUpdatesMenuItem = checkForUpdatesMenuItem;
    this.switchUpdaterBranchMenuItem = switchUpdaterBranchMenuItem;
    this.showLogMenuItem = showLogMenuItem;
    this.customLaunchOptionsMenuItem = customLaunchOptionsMenuItem;
    this.checkRememberGame = checkRememberGame;

    pack();
    setMinimumSize(new Dimension(750, 480));
    setLocationByPlatform(true);
  }

  @Override
  public JComboBox<String> getCmbResolution() {
    return cmbResolution;
  }

  @Override
  public JComboBox<String> getCmbSkybox() {
    return cmbSkybox;
  }

  @Override
  public JComboBox<String> getCmbHud() {
    return cmbHud;
  }

  @Override
  public JComboBox<String> getCmbFramerate() {
    return cmbFramerate;
  }

  public JComboBox<String> getCmbQuality() {
    return cmbQuality;
  }

  @Override
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

  @Override
  public JButton getBtnStartGame() {
    return btnStartTf;
  }

  @Override
  public JLabel getLblResolution() {
    return lblResolution;
  }

  @Override
  public JLabel getLblFrameRate() {
    return lblFrameRate;
  }

  @Override
  public JButton getBtnClearMovieFolder() {
    return btnClearMovieFolder;
  }

  @Override
  public JTable getTableCustomContent() {
    return tableCustomContent;
  }

  @Override
  public JTabbedPane getTabbedPane() {
    return tabbedPane;
  }

  @Override
  public JMenuItem getMntmChangeGameDirectory() {
    return mntmChangeTfDirectory;
  }

  @Override
  public JMenuItem getMntmChangeMovieDirectory() {
    return mntmChangeMovieDirectory;
  }

  @Override
  public JLabel getLblStatus() {
    return lblStatus;
  }

  @Override
  public JComboBox<String> getCmbViewmodel() {
    return cmbViewmodel;
  }

  @Override
  public JProgressBar getProgressBar() {
    return progressBar;
  }

  @Override
  public JMenuItem getMntmRevertToDefault() {
    return mntmRevertToDefault;
  }

  @Override
  public JMenuItem getMntmExit() {
    return mntmExit;
  }

  @Override
  public JMenuItem getMntmAbout() {
    return mntmAbout;
  }

  @Override
  public JMenuItem getMntmSaveSettings() {
    return mntmSaveSettings;
  }

  public JCheckBox getUseHudMinmode() {
    return useHudMinmode;
  }

  @Override
  public JMenuItem getMntmAddCustomSettings() {
    return mntmAddCustomSettings;
  }

  @Override
  public JMenuItem getMntmOpenMovieFolder() {
    return mntmOpenMovieFolder;
  }

  @Override
  public JMenuItem getMntmOpenCustomFolder() {
    return mntmOpenCustomFolder;
  }

  @Override
  public JMenuItem getMntmLaunchTimeout() {
    return mntmChangeTfLaunch;
  }

  @Override
  public JCheckBoxMenuItem getChckbxmntmInsecure() {
    return chckbxmntmInsecure;
  }

  public JCheckBox getUsePlayerModel() {
    return usePlayerModel;
  }

  @Override
  public JComboBox<String> getCmbSourceVideoFormat() {
    return cmbSourceVideoFormat;
  }

  @Override
  public JSpinner getSpinnerJpegQuality() {
    return spinnerJpegQuality;
  }

  @Override
  public JLabel getLblViewmodelFov() {
    return lblViewmodelFov;
  }

  @Override
  public JLabel getLblJpegQuality() {
    return lblJpegQuality;
  }

  @Override
  public JCheckBoxMenuItem getChckbxmntmBackupMode() {
    return chckbxmntmBackupMode;
  }

  @Override
  public JMenuItem getCheckForUpdatesMenuItem() {
    return checkForUpdatesMenuItem;
  }

  @Override
  public JMenuItem getSwitchUpdaterBranchMenuItem() {
    return switchUpdaterBranchMenuItem;
  }

  @Override
  public JMenuItem getShowLogMenuItem() {
    return showLogMenuItem;
  }

  @Override
  public JMenuItem getCustomLaunchOptionsMenuItem() {
    return customLaunchOptionsMenuItem;
  }

  @Override
  public AbstractButton getCheckRememberGame() {
    return checkRememberGame;
  }

}
