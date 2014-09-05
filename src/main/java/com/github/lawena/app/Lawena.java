package com.github.lawena.app;

import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.model.LwrtFiles;
import com.github.lawena.model.LwrtResource;
import com.github.lawena.model.LwrtResource.PathContents;
import com.github.lawena.model.LwrtResources;
import com.github.lawena.model.LwrtSettings;
import com.github.lawena.model.LwrtSettings.Key;
import com.github.lawena.model.MainModel;
import com.github.lawena.os.OSInterface;
import com.github.lawena.ui.AboutDialog;
import com.github.lawena.ui.LawenaView;
import com.github.lawena.ui.LogView;
import com.github.lawena.ui.SkyboxListRenderer;
import com.github.lawena.ui.SwingLink;
import com.github.lawena.ui.TooltipRenderer;
import com.github.lawena.update.Build;
import com.github.lawena.update.Updater;
import com.github.lawena.util.LoggingAppender;
import com.github.lawena.util.StatusAppender;
import com.github.lawena.util.Util;
import com.github.lawena.vdm.DemoEditor;

public class Lawena {

  private static final Logger log = LoggerFactory.getLogger(Lawena.class);

  private MainModel model;
  private LawenaView view;

  private LwrtSettings settings;
  private LwrtFiles files;
  private DemoEditor demos;
  private LwrtResources resources;
  private OSInterface os;

  private Tasks tasks;
  private Particles particles;
  private Segments segments;
  private Branches branches;

  private LogView logView;

  private AboutDialog dialog;
  private JTextArea customSettingsTextArea;
  private JScrollPane customSettingsScrollPane;
  private Object lastHud;

  public Lawena(MainModel mainModel) {
    model = mainModel;
  }

  public void start() {
    view = new LawenaView();
    logView = new LogView();
    tasks = new Tasks(this);
    particles = new Particles(this);
    segments = new Segments(this);
    branches = new Branches(this);

    os = model.getOsInterface();
    settings = model.getSettings();
    files = model.getFiles();
    demos = model.getDemos();
    resources = model.getResources();

    // setup ui loggers: log tab and status bar
    ch.qos.logback.classic.Logger rootLog =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("root");
    rootLog.addAppender(new LoggingAppender(logView.getLogPane(), rootLog.getLoggerContext()));
    ch.qos.logback.classic.Logger statusLog =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("status");
    statusLog.setAdditive(false);
    statusLog.addAppender(new StatusAppender(view.getLblStatus(), statusLog.getLoggerContext()));

    model.getUpdater().fileCleanup();
    tasks.checkForUpdates();

    view.setTitle("Lawena Recording Tool " + model.getShortVersion());
    try {
      view.setIconImage(new ImageIcon(getClass().getResource("tf2.png")).getImage());
    } catch (Exception e) {
      log.warn("Window icon missing / could not be set");
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
          dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
          dialog.setModalityType(ModalityType.APPLICATION_MODAL);
        }
        dialog.setVisible(true);
      }
    });
    view.getMntmSelectEnhancedParticles().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        startParticlesDialog();
      }
    });
    view.getMntmAddCustomSettings().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        JTextArea custom = getCustomSettingsTextArea();
        String previous = custom.getText();
        int result =
            JOptionPane.showConfirmDialog(view, getCustomSettingsScrollPane(),
                "Configure Custom Settings", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
          log.info("Saving custom settings change: " + custom.getText());
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
        logView.setLocation(view.getX() + view.getWidth() + 10, view.getY());
        logView.setVisible(true);
      }
    });

    final JTable table = view.getTableCustomContent();
    table.setModel(resources);
    table.getColumnModel().getColumn(0).setMaxWidth(20);
    table.getColumnModel().getColumn(2).setMaxWidth(50);
    table.setDefaultRenderer(LwrtResource.class, new TooltipRenderer(settings));
    table.getModel().addTableModelListener(new TableModelListener() {

      @Override
      public void tableChanged(TableModelEvent e) {
        if (e.getColumn() == LwrtResources.Column.SELECTED.ordinal()) {
          int row = e.getFirstRow();
          TableModel model = (TableModel) e.getSource();
          LwrtResource cp =
              (LwrtResource) model.getValueAt(row, LwrtResources.Column.PATH.ordinal());
          checkCustomHud(cp);
          if (cp == LwrtResources.particles && cp.isSelected()) {
            startParticlesDialog();
          }
        }
      }
    });
    TableRowSorter<LwrtResources> sorter = new TableRowSorter<>(resources);
    table.setRowSorter(sorter);
    RowFilter<LwrtResources, Object> filter = new RowFilter<LwrtResources, Object>() {
      public boolean include(Entry<? extends LwrtResources, ? extends Object> entry) {
        LwrtResource cp = (LwrtResource) entry.getValue(LwrtResources.Column.PATH.ordinal());
        return !cp.getContents().contains(PathContents.READONLY);
      }
    };
    sorter.setRowFilter(filter);
    tasks.scanResources();
    tasks.loadSkyboxes();

    loadSettings();

    view.getMntmChangeTfDirectory().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (tasks.getCurrentLaunchTask() == null) {
          Path newpath = model.getChosenTfPath();
          if (newpath != null) {
            settings.setTfPath(newpath);
            tasks.scanResources();
          }
        } else {
          JOptionPane.showMessageDialog(view, "Please wait until TF2 has stopped running");
        }
      }
    });
    view.getMntmChangeMovieDirectory().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (tasks.getCurrentLaunchTask() == null) {
          Path newpath = model.getChosenMoviePath();
          if (newpath != null) {
            settings.setMoviePath(newpath);
          }
        } else {
          JOptionPane.showMessageDialog(view, "Please wait until TF2 has stopped running");
        }
      }
    });
    view.getMntmRevertToDefault().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        Path movies = settings.getMoviePath();
        settings.loadDefaults();
        settings.setMoviePath(movies);
        loadSettings();
        resources.loadResourceSettings();
        loadHudComboState();
        saveSettings();
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
    view.getBtnStartTf().addActionListener(new ActionListener() {

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
        tasks.openFile(settings.getMoviePath().toFile());
      }
    });
    view.getMntmOpenCustomFolder().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        tasks.openFile(Paths.get("custom").toFile());
      }
    });
    view.getChckbxmntmInsecure().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        settings.setInsecure(view.getChckbxmntmInsecure().isSelected());
      }
    });
    view.getMntmLaunchTimeout().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        Object answer =
            JOptionPane.showInputDialog(view, "Enter the number of seconds to wait\n"
                + "before interrupting TF2 launch.\n" + "Enter 0 to disable timeout.",
                "Launch Timeout", JOptionPane.PLAIN_MESSAGE, null, null,
                settings.getLaunchTimeout());
        if (answer != null) {
          try {
            int value = Integer.parseInt(answer.toString());
            settings.setLaunchTimeout(value);
          } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(view, "Invalid value, must be 0 or higher integer.",
                "Launch Options", JOptionPane.WARNING_MESSAGE);
          }
        }
      }
    });
    view.getCustomLaunchOptionsMenuItem().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        Object[] options = {"OK", "Set defaults", "Cancel"};
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBounds(0, 0, 81, 140);
        panel
            .add(new JLabel(
                "<html>Enter the list of custom launch options to use on game launch."
                    + "<br>Steam AppID, Resolution and DxLevel are provided by Lawena in case they are not defined here."
                    + "<br>These options are for advanced users only and can override settings from the main window."));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(new SwingLink("Launch Options Valve Developer Wiki",
            "https://developer.valvesoftware.com/wiki/Launch_options"));
        JTextField textField = new JTextField(settings.getString(Key.LaunchOptions));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(textField);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel
            .add(new JLabel(
                "Press OK to use above values or Set Defaults to use the standard launch options used by Lawena."));
        int result =
            JOptionPane.showOptionDialog(null, panel, "Custom Launch Options",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options,
                null);
        if (result == JOptionPane.YES_OPTION) {
          String launchOptions = textField.getText();
          settings.setString(Key.LaunchOptions, launchOptions);
        } else if (result == 1) {
          settings.setString(Key.LaunchOptions, (String) Key.LaunchOptions.defValue());
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

    view.getTabbedPane().addTab("VDM", null, demos.start());
    view.setVisible(true);
  }

  private void checkViewmodelState() {
    boolean e = view.getCmbViewmodel().getSelectedIndex() != 1;
    view.getLblViewmodelFov().setEnabled(e);
    view.getSpinnerViewmodelFov().setEnabled(e);
  }

  private void checkFrameFormatState() {
    boolean e = view.getCmbSourceVideoFormat().getSelectedIndex() != 0;
    view.getLblJpegQuality().setEnabled(e);
    view.getSpinnerJpegQuality().setEnabled(e);
  }

  private JTextArea getCustomSettingsTextArea() {
    if (customSettingsTextArea == null) {
      customSettingsTextArea = new JTextArea(10, 40);
      customSettingsTextArea.setEditable(true);
      customSettingsTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
    }
    return customSettingsTextArea;
  }

  private JScrollPane getCustomSettingsScrollPane() {
    if (customSettingsScrollPane == null) {
      customSettingsScrollPane = new JScrollPane(getCustomSettingsTextArea());
    }
    return customSettingsScrollPane;
  }

  private void startParticlesDialog() {
    particles.start();
  }

  private void startSegmentsDialog() {
    segments.start();
  }

  private void startBranchesDialog() {
    branches.start();
  }

  private boolean checkCustomHud(LwrtResource cp) {
    EnumSet<PathContents> set = cp.getContents();
    if (cp.isSelected()) {
      if (set.contains(PathContents.HUD)) {
        lastHud = view.getCmbHud().getSelectedItem();
        view.getCmbHud().setSelectedItem("Custom");
        view.getCmbHud().setEnabled(false);
        return true;
      }
    } else {
      if (set.contains(PathContents.HUD)) {
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
    for (LwrtResource cp : resources.getList()) {
      if (detected) {
        break;
      }
      detected = checkCustomHud(cp);
    }
  }

  private void loadSettings() {
    Util.registerValidation(view.getCmbResolution(), "[1-9][0-9]*x[1-9][0-9]*",
        view.getLblResolution());
    Util.registerValidation(view.getCmbFramerate(), "[1-9][0-9]*", view.getLblFrameRate());
    Util.selectComboItem(view.getCmbHud(), settings.getHud(), Key.Hud.getAllowedValues());
    Util.selectComboItem(view.getCmbQuality(), settings.getDxlevel(),
        Key.DxLevel.getAllowedValues());
    Util.selectComboItem(view.getCmbViewmodel(), settings.getViewmodelSwitch(),
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
    view.getUseHudMinmode().setSelected(settings.getHudMinmode());
    view.getChckbxmntmInsecure().setSelected(settings.getInsecure());
    view.getChckbxmntmBackupMode().setSelected(settings.getBoolean(Key.DeleteBackupsWhenRestoring));
    view.getUsePlayerModel().setSelected(settings.getHudPlayerModel());
    getCustomSettingsTextArea().setText(settings.getCustomSettings());
    view.getCmbSourceVideoFormat().setSelectedItem(
        settings.getString(Key.SourceRecorderVideoFormat).toUpperCase());
    view.getSpinnerJpegQuality().setValue(settings.getInt(Key.SourceRecorderJpegQuality));
    checkViewmodelState();
    checkFrameFormatState();
  }

  public void saveSettings() {
    String[] resolution = ((String) view.getCmbResolution().getSelectedItem()).split("x");
    if (resolution.length == 2) {
      settings.setWidth(Integer.parseInt(resolution[0]));
      settings.setHeight(Integer.parseInt(resolution[1]));
    } else {
      log.warn("Bad resolution format, reverting to previously saved");
      view.getCmbResolution().setSelectedItem(settings.getWidth() + "x" + settings.getHeight());
    }
    String framerate = (String) view.getCmbFramerate().getSelectedItem();
    settings.setFramerate(Integer.parseInt(framerate));
    settings.setHud(Key.Hud.getAllowedValues().get(view.getCmbHud().getSelectedIndex()));
    settings.setViewmodelSwitch(Key.ViewmodelSwitch.getAllowedValues().get(
        view.getCmbViewmodel().getSelectedIndex()));
    settings.setViewmodelFov((int) view.getSpinnerViewmodelFov().getValue());
    settings
        .setDxlevel(Key.DxLevel.getAllowedValues().get(view.getCmbQuality().getSelectedIndex()));
    settings.setMotionBlur(view.getEnableMotionBlur().isSelected());
    settings.setCombattext(!view.getDisableCombatText().isSelected());
    settings.setCrosshair(!view.getDisableCrosshair().isSelected());
    settings.setCrosshairSwitch(!view.getDisableCrosshairSwitch().isSelected());
    settings.setHitsounds(!view.getDisableHitSounds().isSelected());
    settings.setVoice(!view.getDisableVoiceChat().isSelected());
    settings.setSkybox((String) view.getCmbSkybox().getSelectedItem());
    Path tfpath = settings.getTfPath();
    List<String> selected = new ArrayList<>();
    for (LwrtResource cp : resources.getList()) {
      Path path = cp.getPath();
      if (!cp.getContents().contains(PathContents.READONLY) && cp.isSelected()) {
        String key = (path.startsWith(tfpath) ? "tf*" : "");
        key += path.getFileName().toString();
        selected.add(key);
      }
    }
    settings.setCustomResources(selected);
    settings.setHudMinmode(view.getUseHudMinmode().isSelected());
    settings.setInsecure(view.getChckbxmntmInsecure().isSelected());
    settings
        .setBoolean(Key.DeleteBackupsWhenRestoring, view.getChckbxmntmBackupMode().isSelected());
    settings.setHudPlayerModel(view.getUsePlayerModel().isSelected());
    settings.setCustomSettings(getCustomSettingsTextArea().getText());
    settings.setString(Key.SourceRecorderVideoFormat, view.getCmbSourceVideoFormat()
        .getSelectedItem().toString().toLowerCase());
    settings.setInt(Key.SourceRecorderJpegQuality, (int) view.getSpinnerJpegQuality().getValue());
    settings.save();
    log.info("Settings saved");
  }

  void saveAndExit() {
    saveSettings();
    view.setVisible(false);
    if (!os.isRunningTF2()) {
      files.restoreAll();
    }
    System.exit(0);
  }

  void configureSkyboxes(final JComboBox<String> combo) {
    final Vector<String> data = new Vector<>();
    Path dir = Paths.get("skybox");
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
    data.add(0, (String) Key.Skybox.defValue());
    combo.setModel(new DefaultComboBoxModel<String>(data));
    combo.setRenderer(new SkyboxListRenderer(model.getSkyboxes().getMap()));
  }

  public void selectSkyboxFromSettings() {
    view.getCmbSkybox().setSelectedItem(settings.getSkybox());
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
      String notice = "Update is ready. Please restart Lawena to use it";
      log.info(notice);
      JOptionPane.showMessageDialog(view, notice, "Update Ready", JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    Updater updater = model.getUpdater();
    if (updater.upgradeApplication(details)) {
      log.info("Upgrade in progress..");
      saveAndExit();
    } else {
      log.debug("Attempting to update version marker file again");
      if (updater.createVersionFile(details.getName())) {
        String notice = "Update is ready. Please restart Lawena to use it";
        log.info(notice);
        JOptionPane
            .showMessageDialog(view, notice, "Update Ready", JOptionPane.INFORMATION_MESSAGE);
      } else {
        String notice = "Update could not be completed, please report this issue";
        log.info(notice);
        JOptionPane.showMessageDialog(view, notice, "Updater Error", JOptionPane.WARNING_MESSAGE);
      }
    }
  }
}
