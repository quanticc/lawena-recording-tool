package com.github.lawena.app;

import static com.github.lawena.util.Util.toPath;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.RowFilter;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

import com.github.lawena.Messages;
import com.github.lawena.app.model.ConfigWriter;
import com.github.lawena.app.model.MainModel;
import com.github.lawena.app.model.Resource;
import com.github.lawena.app.model.Resources;
import com.github.lawena.app.model.Settings;
import com.github.lawena.app.task.FileOpener;
import com.github.lawena.profile.Key;
import com.github.lawena.profile.ProfileListener;
import com.github.lawena.profile.ValidationResult;
import com.github.lawena.profile.StringValidator;
import com.github.lawena.ui.AboutDialog;
import com.github.lawena.ui.LaunchOptionsDialog;
import com.github.lawena.ui.LawenaView;
import com.github.lawena.ui.LogView;
import com.github.lawena.ui.SkyboxListRenderer;
import com.github.lawena.ui.TooltipRenderer;
import com.github.lawena.update.Build;
import com.github.lawena.update.Updater;
import com.github.lawena.util.LoggingAppender;
import com.github.lawena.util.StatusAppender;
import com.github.lawena.util.Util;
import com.github.lawena.vdm.DemoEditor;

/**
 * Main presenter to manage the UI components decoupled from configuration values and settings. Game
 * implementations should extend this class to account for particular UI components the game may
 * need.
 * 
 * @author Ivan
 *
 */
public abstract class Lawena implements ProfileListener {

  static final Logger log = LoggerFactory.getLogger(Lawena.class);

  protected MainModel model;
  protected LawenaView view;

  protected Settings settings;
  protected DemoEditor demos;
  protected Resources resources;

  protected Tasks tasks;
  protected Segments segments;
  protected Branches branches;

  protected LogView logView;

  protected AboutDialog dialog;
  protected JTextArea customSettingsTextArea;
  protected JScrollPane customSettingsScrollPane;
  protected Object lastHud;
  protected LaunchOptionsDialog launchOptionsDialog;
  protected LoggingAppender appender;

  public Lawena(MainModel mainModel) {
    model = mainModel;
  }

  /**
   * Initialize the particular {@link LawenaView} that will represent the View layer.
   */
  protected abstract void setupView();

  /**
   * Retrieves the icon for the user interface main window.
   * 
   * @return
   */
  protected abstract Image getIconImage();

  public abstract Component viewAsComponent();

  protected abstract Frame viewAsFrame();

  /**
   * Initialize all UI components and displays them.
   */
  public final void start() {
    setupView();
    if (view == null)
      throw new IllegalStateException("View must be configured by this step"); //$NON-NLS-1$
    logView = new LogView();
    tasks = new Tasks(this);
    segments = new Segments(this);
    branches = new Branches(this);

    settings = model.getSettings();
    demos = model.getDemos();
    resources = model.getResources();

    // setup ui loggers: log tab and status bar
    appender.setScroll(logView.getLogScroll());
    appender.setPane(logView.getLogPane());
    ch.qos.logback.classic.Logger statusLog =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("status"); //$NON-NLS-1$
    statusLog.setAdditive(false);
    statusLog.addAppender(new StatusAppender(view.getLblStatus(), statusLog.getLoggerContext()));

    model.getUpdater().fileCleanup();
    tasks.checkForUpdates();

    view.setTitle("Lawena Recording Tool " + model.getShortVersion()); //$NON-NLS-1$
    try {
      view.setIconImage(getIconImage());
    } catch (Exception e) {
      log.warn("Window icon missing / could not be set"); //$NON-NLS-1$
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
          dialog = new AboutDialog(model.getFullVersion(), model.getBuildTime());
          dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
          dialog.setModalityType(ModalityType.APPLICATION_MODAL);
        }
        dialog.setVisible(true);
      }
    });
    view.getMntmAddCustomSettings().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        JTextArea custom = getCustomSettingsTextArea();
        String previous = custom.getText();
        int result =
            JOptionPane.showConfirmDialog(viewAsComponent(), getCustomSettingsScrollPane(),
                Messages.getString("Lawena.configureCustomSettingsTitle"), //$NON-NLS-1$
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
          log.info("Saving custom settings change: " + custom.getText()); //$NON-NLS-1$
          saveSettings();
        } else {
          custom.setText(previous);
        }
      }
    });
    view.getCheckForUpdatesMenuItem().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        tasks.checkForUpdates();
      }
    });
    view.getSwitchUpdaterBranchMenuItem().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        startBranchesDialog();
      }
    });
    view.getShowLogMenuItem().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        logView.setLocation(view.getX() + view.getWidth() + 5, view.getY());
        logView.setVisible(true);
      }
    });
    logView.getOpenLogButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        new FileOpener(Paths.get("logs", "lawena.log")).execute(); //$NON-NLS-1$ //$NON-NLS-2$
      }
    });
    logView.getCopyLogButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        logView.getLogPane().selectAll();
        logView.getLogPane().copy();
        logView.getLogPane().select(0, 0);
        log.info("Log contents copied to clipboard"); //$NON-NLS-1$
      }
    });
    logView.getLevelComboBox().setSelectedItem(appender.getLevel().toString());
    logView.getLevelComboBox().addItemListener(new ItemListener() {

      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          ch.qos.logback.classic.Logger rootLog =
              (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("root"); //$NON-NLS-1$
          Level level = Level.valueOf((String) e.getItem());
          log.info("Changing log level to {}", level); //$NON-NLS-1$
          appender.setLevel(level);
          if (!Level.INFO.isGreaterOrEqual(level)) {
            rootLog.setLevel(Level.INFO);
          } else if (rootLog.getLevel().isGreaterOrEqual(level)) {
            rootLog.setLevel(level);
          }
        }
      }
    });

    final JTable table = view.getTableCustomContent();
    table.setModel(resources);
    table.getColumnModel().getColumn(0).setMaxWidth(20);
    table.getColumnModel().getColumn(2).setMaxWidth(50);
    table.setDefaultRenderer(Resource.class, new TooltipRenderer(settings));
    table.getModel().addTableModelListener(new TableModelListener() {

      @Override
      public void tableChanged(TableModelEvent e) {
        if (e.getColumn() == Resources.Column.ENABLED.ordinal()) {
          int row = e.getFirstRow();
          TableModel model1 = (TableModel) e.getSource();
          Resource cp = (Resource) model1.getValueAt(row, Resources.Column.NAME.ordinal());
          checkCustomHud(cp);
        }
      }
    });
    TableRowSorter<Resources> sorter = new TableRowSorter<>(resources);
    table.setRowSorter(sorter);
    RowFilter<Resources, Object> filter = new RowFilter<Resources, Object>() {
      @Override
      public boolean include(Entry<? extends Resources, ? extends Object> entry) {
        Resource resource = (Resource) entry.getValue(Resources.Column.NAME.ordinal());
        return !resources.isParentForcefullyLoaded(resource);
      }
    };
    sorter.setRowFilter(filter);

    // ui validators
    Util.registerValidation(view.getCmbResolution(), "[1-9][0-9]*x[1-9][0-9]*", //$NON-NLS-1$
        view.getLblResolution());
    Util.registerValidation(view.getCmbFramerate(), "[1-9][0-9]*", view.getLblFrameRate()); //$NON-NLS-1$

    view.getCmbSkybox().addItem("Default"); //$NON-NLS-1$

    // trigger profile select
    onProfileSelected();

    // TODO: add change steam directory presenter action

    view.getMntmChangeGameDirectory().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (tasks.getCurrentLaunchTask() == null) {
          Path newpath = validateGamePath(null);
          if (newpath != null) {
            Key.gamePath.setValueEx(settings, newpath.toString());
            JOptionPane.showMessageDialog(viewAsComponent(),
                String.format(Messages.getString("Lawena.newGamePath"), newpath, //$NON-NLS-1$
                    Messages.getString("Lawena.newGamePathTitle"), //$NON-NLS-1$
                    JOptionPane.INFORMATION_MESSAGE));
          }
        } else {
          JOptionPane.showMessageDialog(viewAsComponent(),
              Messages.getString("Lawena.dirChangeWhileGameRunning")); //$NON-NLS-1$
        }
      }
    });
    view.getMntmChangeMovieDirectory().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (tasks.getCurrentLaunchTask() == null) {
          Path newpath = validateRecordingPath(null);
          if (newpath != null) {
            Key.recordingPath.setValueEx(settings, newpath.toString());
            JOptionPane.showMessageDialog(viewAsComponent(),
                String.format(Messages.getString("Lawena.newRecordingPath"), newpath, //$NON-NLS-1$
                    Messages.getString("Lawena.newRecordingPathTitle"), //$NON-NLS-1$
                    JOptionPane.INFORMATION_MESSAGE));
          }
        } else {
          JOptionPane.showMessageDialog(viewAsComponent(),
              Messages.getString("Lawena.dirChangeWhileGameRunning")); //$NON-NLS-1$
        }
      }
    });
    view.getMntmRevertToDefault().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        int answer =
            JOptionPane.showConfirmDialog(viewAsComponent(),
                Messages.getString("Lawena.resetProfileToDefault"), //$NON-NLS-1$
                Messages.getString("Lawena.resetProfileToDefaultTitle"), //$NON-NLS-1$
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
          Path game = toPath(Key.gamePath.getValue(settings));
          Path movies = toPath(Key.recordingPath.getValue(settings));
          settings.loadDefaultValues();
          Key.gamePath.setValueEx(settings, game.toString());
          Key.recordingPath.setValueEx(settings, movies.toString());
          onProfileSelected();
        }
      }
    });
    view.getMntmExit().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        saveAndExit();
      }
    });
    view.getMntmSaveSettings().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        saveSettings();
      }
    });
    view.getBtnStartGame().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        tasks.launch();
      }
    });
    view.getBtnClearMovieFolder().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        startSegmentsDialog();
      }
    });
    view.getMntmOpenMovieFolder().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        Tasks.openFile(new File(Key.recordingPath.getValue(settings)).getAbsoluteFile());
      }
    });
    view.getMntmOpenCustomFolder().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        Path data = settings.getParentDataPath();
        Tasks.openFile(data.resolve("custom").toFile()); //$NON-NLS-1$
      }
    });
    view.getChckbxmntmInsecure().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        Key.insecure.setValueEx(settings, view.getChckbxmntmInsecure().isSelected());
      }
    });
    view.getChckbxmntmBackupMode().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        Key.deleteUnneededBackups.setValueEx(settings, view.getChckbxmntmBackupMode().isSelected());
      }
    });
    view.getCheckRememberGame().setSelected(model.getGames().isRememberChoice());
    view.getCheckRememberGame().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        model.getGames().setRememberChoice(view.getCheckRememberGame().isSelected());
        model.getGames().save();
      }
    });
    view.getMntmLaunchTimeout().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        Object answer =
            JOptionPane.showInputDialog(viewAsComponent(),
                Messages.getString("Lawena.gameLaunchTimeout"), //$NON-NLS-1$
                Messages.getString("Lawena.gameLaunchTimeoutTitle"), //$NON-NLS-1$
                JOptionPane.PLAIN_MESSAGE, null, null, Key.launchTimeout.getValue(settings));
        if (answer != null) {
          try {
            int value = Integer.parseInt(answer.toString());
            Key.launchTimeout.setValueEx(settings, value);
          } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(viewAsComponent(),
                Messages.getString("Lawena.invalidGameLaunchTimeout"), //$NON-NLS-1$
                Messages.getString("Lawena.gameLaunchTimeoutOptions"), //$NON-NLS-1$
                JOptionPane.WARNING_MESSAGE);
          }
        }
      }
    });
    view.getCustomLaunchOptionsMenuItem().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (launchOptionsDialog == null) {
          launchOptionsDialog = new LaunchOptionsDialog();
        }
        launchOptionsDialog.getOptionsTextField().setText(Key.launchOptions.getValue(settings));
        int result = launchOptionsDialog.showDialog();
        if (result == JOptionPane.YES_OPTION) {
          String launchOptions = launchOptionsDialog.getOptionsTextField().getText();
          Key.launchOptions.setValueEx(settings, launchOptions);
        } else if (result == 1) {
          Key.launchOptions.revertToDefault(settings);
        }
      }
    });
    view.getCmbViewmodel().addItemListener(new ItemListener() {

      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          checkViewmodelState();
        }
      }
    });
    view.getCmbSourceVideoFormat().addItemListener(new ItemListener() {

      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          checkFrameFormatState();
        }
      }
    });

    view.getTabbedPane().addTab(
        Messages.getString("Lawena.valveDemoMetafiles"), null, demos.start()); //$NON-NLS-1$
    view.setVisible(true);

    // Basic path validation task
    /*
     * Makes the user choose the folder if invalid but if dialog is closed no action is done, just
     * notice through log.
     */
    new Timer().schedule(new TimerTask() {

      private String safeToString(Path path) {
        return path == null ? null : path.toString();
      }

      @Override
      public void run() {
        ValidationResult rs, rg, rr;
        Path steamPath = toPath(Key.steamPath.getValue(settings));
        Path gamePath = toPath(Key.gamePath.getValue(settings));
        Path recPath = toPath(Key.recordingPath.getValue(settings));
        Path newSteamPath = validateSteamPath(steamPath);
        rs = Key.steamPath.setValue(settings, safeToString(newSteamPath));
        Path newGamePath = validateGamePath(gamePath);
        rg = Key.gamePath.setValue(settings, safeToString(newGamePath));
        Path newRecPath = validateRecordingPath(recPath);
        rr = Key.recordingPath.setValue(settings, safeToString(newRecPath));
        if (!rs.isValid()) {
          log.warn("No Steam path was defined"); //$NON-NLS-1$
        } else if (!steamPath.equals(newSteamPath)) {
          log.info("New Steam path: {}", newSteamPath); //$NON-NLS-1$
        }
        if (!rg.isValid()) {
          log.warn("No game path was defined"); //$NON-NLS-1$
        } else if (!gamePath.equals(newGamePath)) {
          log.debug("New game path: {}", newGamePath); //$NON-NLS-1$
        }
        if (!rr.isValid()) {
          log.warn("No segments path was defined"); //$NON-NLS-1$
        } else if (!recPath.equals(newRecPath)) {
          log.debug("New segments path: {}", newRecPath); //$NON-NLS-1$
        }
        settings.save();
      }
    }, 1000);
  }

  void checkViewmodelState() {
    boolean e = view.getCmbViewmodel().getSelectedIndex() != 1;
    view.getLblViewmodelFov().setEnabled(e);
    view.getSpinnerViewmodelFov().setEnabled(e);
  }

  void checkFrameFormatState() {
    boolean e = view.getCmbSourceVideoFormat().getSelectedIndex() != 0;
    view.getLblJpegQuality().setEnabled(e);
    view.getSpinnerJpegQuality().setEnabled(e);
  }

  JTextArea getCustomSettingsTextArea() {
    if (customSettingsTextArea == null) {
      customSettingsTextArea = new JTextArea(10, 40);
      customSettingsTextArea.setEditable(true);
      customSettingsTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
    }
    return customSettingsTextArea;
  }

  JScrollPane getCustomSettingsScrollPane() {
    if (customSettingsScrollPane == null) {
      customSettingsScrollPane = new JScrollPane(getCustomSettingsTextArea());
    }
    return customSettingsScrollPane;
  }

  void startSegmentsDialog() {
    segments.start();
  }

  void startBranchesDialog() {
    branches.start();
  }

  boolean checkCustomHud(Resource resource) {
    Set<String> set = resource.getTags();
    if (resource.isEnabled()) {
      if (set.contains(Resource.HUD)) {
        lastHud = view.getCmbHud().getSelectedItem();
        view.getCmbHud().setSelectedItem("Custom"); //$NON-NLS-1$
        view.getCmbHud().setEnabled(false);
        return true;
      }
    } else {
      if (set.contains(Resource.HUD)) {
        if (lastHud != null) {
          view.getCmbHud().setSelectedItem(lastHud);
        }
        view.getCmbHud().setEnabled(true);
        return false;
      }
    }
    return false;
  }

  void loadHudComboState() {
    boolean detected = false;
    for (Resource resource : resources.getResourceList()) {
      detected = checkCustomHud(resource);
      if (detected) {
        break;
      }
    }
  }

  private void loadSettings() {
    Util.selectComboItem(view.getCmbHud(), Key.hud.getValue(settings),
        ((StringValidator) Key.hud.getValidator()).getAllowedValues());
    Util.selectComboItem(view.getCmbViewmodel(), Key.viewmodelSwitch.getValue(settings),
        ((StringValidator) Key.viewmodelSwitch.getValidator()).getAllowedValues());
    selectSkyboxFromSettings();
    view.getCmbResolution().setSelectedItem(
        Key.width.getValue(settings).intValue() + "x" + Key.height.getValue(settings).intValue()); //$NON-NLS-1$
    view.getCmbFramerate().setSelectedItem(Key.framerate.getValue(settings).intValue() + ""); //$NON-NLS-1$
    try {
      view.getSpinnerViewmodelFov().setValue(Key.viewmodelFov.getValue(settings));
    } catch (IllegalArgumentException e) {
      // ignore
    }
    view.getChckbxmntmInsecure().setSelected(Key.insecure.getValue(settings));
    getCustomSettingsTextArea().setText(Key.extConVars.getValue(settings));
    view.getCmbSourceVideoFormat().setSelectedItem(Key.recorderVideoFormat.getValue(settings));
    view.getSpinnerJpegQuality().setValue(Key.recorderJpegQuality.getValue(settings));
    logView.getLevelComboBox().setSelectedItem(Key.loglevel.getValue(settings));
    view.getChckbxmntmBackupMode().setSelected(Key.deleteUnneededBackups.getValue(settings));
    checkViewmodelState();
    checkFrameFormatState();
    loadDependentSettings();
  }

  protected abstract void loadDependentSettings();

  public void saveSettings() {
    String[] resolution = ((String) view.getCmbResolution().getSelectedItem()).split("x"); //$NON-NLS-1$
    if (resolution.length == 2) {
      Key.width.setValueEx(settings, Integer.parseInt(resolution[0]));
      Key.height.setValueEx(settings, Integer.parseInt(resolution[1]));
    } else {
      log.warn("Bad resolution format, reverting to previously saved"); //$NON-NLS-1$
      view.getCmbResolution().setSelectedItem(
          Key.width.getValue(settings) + "x" + Key.height.getValue(settings)); //$NON-NLS-1$
    }
    Key.hud.setValueEx(
        settings,
        ((StringValidator) Key.hud.getValidator()).getAllowedValues().get(
            view.getCmbHud().getSelectedIndex()));
    Key.framerate.setValueEx(settings,
        Integer.parseInt((String) view.getCmbFramerate().getSelectedItem()));
    Key.viewmodelSwitch.setValueEx(settings, ((StringValidator) Key.viewmodelSwitch.getValidator())
        .getAllowedValues().get(view.getCmbViewmodel().getSelectedIndex()));
    Key.viewmodelFov.setValueEx(settings, (Integer) view.getSpinnerViewmodelFov().getValue());
    Key.skybox.setValueEx(settings, (String) view.getCmbSkybox().getSelectedItem());
    Key.resources.setValueEx(settings, resources.getEnabledStringList());
    Key.insecure.setValueEx(settings, view.getChckbxmntmInsecure().isSelected());
    Key.extConVars.setValueEx(settings, getCustomSettingsTextArea().getText());
    Key.recorderVideoFormat.setValueEx(settings, view.getCmbSourceVideoFormat().getSelectedItem()
        .toString());
    Key.recorderJpegQuality.setValueEx(settings, (Integer) view.getSpinnerJpegQuality().getValue());
    Key.loglevel.setValueEx(settings, (String) logView.getLevelComboBox().getSelectedItem());
    Key.deleteUnneededBackups.setValueEx(settings, view.getChckbxmntmBackupMode().isSelected());

    saveDependentSettings();

    settings.save();
    log.info("Settings saved"); //$NON-NLS-1$
  }

  protected abstract void saveDependentSettings();

  void saveAndExit() {
    saveSettings();
    view.setVisible(false);
    if (!model.getOsInterface().isGameRunning()) {
      model.getLinker().unlink();
    }
    System.exit(0);
  }

  @SuppressWarnings("nls")
  void configureSkyboxes(final JComboBox<String> combo) {
    final Vector<String> data = new Vector<>();
    Path dir = Paths.get("lwrt", Key.gameFolderName.getValue(settings), "skybox/vtf");
    if (Files.exists(dir)) {
      log.info("Loading skybox folder");
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*up.vtf")) {
        for (Path path : stream) {
          log.trace("Skybox found at: " + path);
          String skybox = path.toFile().getName();
          skybox = skybox.substring(0, skybox.indexOf("up.vtf"));
          data.add(skybox);
        }
      } catch (IOException e) {
        log.warn("Problem while loading skybox folder", e);
      }
    }
    tasks.generateSkyboxPreviews(new ArrayList<>(data));
    data.add(0, Key.skybox.getDefaultValue());
    combo.setModel(new DefaultComboBoxModel<>(data));
    combo.setRenderer(new SkyboxListRenderer(model.getSkyboxPreviewStore().getMap()));
  }

  public void selectSkyboxFromSettings() {
    view.getCmbSkybox().setSelectedItem(Key.skybox.getValue(settings));
  }

  MainModel getModel() {
    return model;
  }

  LawenaView getView() {
    return view;
  }

  void clearSegmentFiles(List<String> selected) {
    tasks.cleanSegments(selected);
  }

  public void upgrade(Build details) {
    if (details.equals(Build.LATEST)) {
      String notice = Messages.getString("Lawena.updateReadyPleaseRestart"); //$NON-NLS-1$
      log.info(notice);
      JOptionPane.showMessageDialog(viewAsComponent(), notice,
          Messages.getString("Lawena.updateReadyPleaseRestartTitle"), //$NON-NLS-1$
          JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    if (Updater.upgradeApplication(details)) {
      log.info("Upgrade in progress.."); //$NON-NLS-1$
      saveAndExit();
    } else {
      log.debug("Attempting to update version marker file again"); //$NON-NLS-1$
      if (Updater.createVersionFile(details.getName())) {
        String notice = Messages.getString("Lawena.updateReadyPleaseRestart"); //$NON-NLS-1$
        log.info(notice);
        JOptionPane.showMessageDialog(viewAsComponent(), notice,
            Messages.getString("Lawena.updateReadyPleaseRestartTitle"), //$NON-NLS-1$
            JOptionPane.INFORMATION_MESSAGE);
      } else {
        String notice = Messages.getString("Lawena.updateFailedPleaseReport"); //$NON-NLS-1$
        log.info(notice);
        JOptionPane.showMessageDialog(viewAsComponent(), notice,
            Messages.getString("Lawena.updateFailedPleaseReportTitle"), //$NON-NLS-1$
            JOptionPane.WARNING_MESSAGE);
      }
    }
  }

  public LoggingAppender getAppender() {
    return appender;
  }

  public void setAppender(LoggingAppender appender) {
    this.appender = appender;
  }

  public Path validateRecordingPath(Path initial) {
    Path selected = initial;
    String folder =
        initial != null ? initial.toAbsolutePath().toString() : Paths
            .get(Key.recordingPath.getValue(settings)).toAbsolutePath().toString();
    int ret = 0;
    while ((selected == null && ret == 0)
        || (selected != null && (!Files.exists(selected) || Paths.get("").equals(selected)))) { //$NON-NLS-1$
      log.debug("Validating current recording folder: {}", //$NON-NLS-1$
          (selected != null ? selected.toAbsolutePath() : "<None>")); //$NON-NLS-1$
      String dir =
          model.getOsInterface().chooseSingleFolder(viewAsFrame(),
              Messages.getString("Lawena.segmentPathSelectionPrompt"), //$NON-NLS-1$
              folder);
      if (dir != null) {
        selected = Paths.get(dir);
      } else {
        ret = 1;
        selected = null;
      }
      log.debug("Selected recording folder: " + selected); //$NON-NLS-1$
    }
    return selected;
  }

  public Path validateGamePath(Path initial) {
    Path selected = initial;
    String folder =
        initial != null ? initial.toAbsolutePath().toString() : Paths
            .get(Key.gamePath.getValue(settings)).toAbsolutePath().toString();
    int ret = 0;
    String dirName = Key.gameFolderName.getValue(settings);
    while ((selected == null && ret == 0)
        || (selected != null && (!Files.exists(selected) || !selected.toAbsolutePath().toFile()
            .getName().toString().equals(dirName)))) {
      log.debug("Validating current game folder: {}", (selected != null ? selected.toAbsolutePath() //$NON-NLS-1$
          : "<None>")); //$NON-NLS-1$
      String dir =
          model.getOsInterface().chooseSingleFolder(viewAsFrame(),
              String.format(Messages.getString("Lawena.gamePathSelectionPrompt"), dirName), //$NON-NLS-1$
              folder);
      if (dir != null) {
        selected = Paths.get(dir);
      } else {
        ret = 1;
        selected = null;
      }
      log.debug("Selected game folder: " + selected); //$NON-NLS-1$
    }
    return selected;
  }

  public Path validateSteamPath(Path initial) {
    Path selected = initial;
    String folder =
        initial != null ? initial.toAbsolutePath().toString() : Paths
            .get(Key.steamPath.getValue(settings)).toAbsolutePath().toString();
    int ret = 0;
    String fileName = "Steam"; //$NON-NLS-1$
    while ((selected == null && ret == 0)
        || (selected != null && (!Files.exists(selected) || !selected.toAbsolutePath()
            .getFileName().toString().equalsIgnoreCase(fileName)))) {
      log.debug("Validating current Steam folder: {}", //$NON-NLS-1$
          (selected != null ? selected.toAbsolutePath() : "<None>")); //$NON-NLS-1$
      String dir =
          model.getOsInterface().chooseSingleFolder(viewAsFrame(),
              String.format(Messages.getString("Lawena.steamPathSelectionPrompt"), fileName), //$NON-NLS-1$
              folder);
      if (dir != null) {
        selected = Paths.get(dir);
      } else {
        ret = 1;
        selected = null;
      }
      log.debug("Selected Steam folder: " + selected); //$NON-NLS-1$
    }
    return selected;
  }

  @Override
  public void onProfileSelected() {
    // update the combo box
    // view.getProfilesComboBox().setSelectedItem(settings.getSelectedName());
    tasks.scanResources();
    tasks.loadSkyboxes();
    loadSettings();
    // TODO: update VDM demos path as well
    saveSettings();
  }

  @Override
  public void onProfileListUpdated() {
    // TODO Auto-generated method stub

  }

  public abstract ConfigWriter newConfigWriter();

}
