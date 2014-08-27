package com.github.lawena.lwrt;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.app.LaunchTask;
import com.github.lawena.app.MainModel;
import com.github.lawena.app.Tasks;
import com.github.lawena.lwrt.CustomPath.PathContents;
import com.github.lawena.lwrt.SettingsManager.Key;
import com.github.lawena.ui.AboutDialog;
import com.github.lawena.ui.LawenaView;
import com.github.lawena.ui.ParticlesDialog;
import com.github.lawena.ui.SegmentsDialog;
import com.github.lawena.ui.TooltipRenderer;
import com.github.lawena.util.LoggingAppender;
import com.github.lawena.util.StatusAppender;
import com.github.lawena.vdm.DemoEditor;

public class Lawena {

  private static final Logger log = LoggerFactory.getLogger(Lawena.class);
  private static final java.util.logging.Logger status = java.util.logging.Logger
      .getLogger("status");

  public class MovieFolderChange implements ActionListener {

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

  }

  public class PathScanTask extends SwingWorker<Void, Void> {
    @Override
    protected Void doInBackground() throws Exception {
      try {
        scan();
        watcher.start();
      } catch (Exception e) {
        log.warn("Problem while scanning custom paths", e);
      }
      return null;
    }

    private void scan() {
      resources.clear();
      resources.addPaths(Paths.get("custom"));
      resources.addPaths(settings.getTfPath().resolve("custom"));
      resources.validateRequired();
    }

    @Override
    protected void done() {
      resources.loadResourceSettings();
      loadHudComboState();
    }
  }

  public class PathCopyTask extends SwingWorker<Boolean, Void> {

    private Path from;

    public PathCopyTask(Path from) {
      this.from = from;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
      status.info("Copying " + from + " into lawena custom folder...");
      return files.copyToCustom(from);
    }

    @Override
    protected void done() {
      boolean result = false;
      try {
        result = get();
      } catch (CancellationException | InterruptedException | ExecutionException e) {
        log.warn("Custom path copy task was cancelled", e);
      }
      if (!result) {
        try {
          resources.addPath(from);
          log.info(from + " added to custom resource list");
        } catch (IOException e) {
          log.warn("Problem while loading a custom path", e);
        }
      } else {
        log.info(from + " copied to custom resource folder");
      }
      status.info(from.getFileName() + " was added"
          + (result ? " to lawena custom folder" : " to custom resource list"));
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
            os.generatePreview(filename);
          }
          ImageIcon icon = createPreviewIcon(img);
          map.put(skybox, icon);
          i++;
        }
      } catch (Exception e) {
        log.warn("Problem while loading skyboxes", e);
      }
      return map;
    }

    @Override
    protected void done() {
      try {
        skyboxMap.putAll(get());
        selectSkyboxFromSettings();
        log.debug("Skybox loading and preview generation complete");
      } catch (CancellationException | InterruptedException | ExecutionException e) {
        log.warn("Skybox preview generator task was cancelled", e);
      }
      status.info("Ready");
      if (!isCancelled()) {
        setCurrentWorker(null, false);
      }
    }

  }

  public class Tf2FolderChange implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      if (tasks.getCurrentLaunchTask() == null) {
        Path newpath = model.getChosenTfPath();
        if (newpath != null) {
          settings.setTfPath(newpath);
          new PathScanTask().execute();
        }
      } else {
        JOptionPane.showMessageDialog(view, "Please wait until TF2 has stopped running");
      }
    }

  }

  private static ImageIcon createPreviewIcon(String imageName) throws IOException {
    int size = 96;
    BufferedImage image;
    File input = new File(imageName);
    image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
    image.createGraphics().drawImage(
        ImageIO.read(input).getScaledInstance(size, size, Image.SCALE_SMOOTH), 0, 0, null);
    return new ImageIcon(image);
  }

  private static void registerValidation(JComboBox<String> combo, final String validationRegex,
      final JLabel label) {
    final JTextComponent tc = (JTextComponent) combo.getEditor().getEditorComponent();
    tc.getDocument().addDocumentListener(new DocumentListener() {

      @Override
      public void changedUpdate(DocumentEvent e) {}

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
  private DemoEditor demos;
  private CustomPathList resources;
  private CommandLine os;

  private AboutDialog dialog;
  private ParticlesDialog particles;
  private SegmentsDialog segments;
  private JTextArea customSettingsTextArea;
  private JScrollPane customSettingsScrollPane;

  private HashMap<String, ImageIcon> skyboxMap;
  private Thread watcher;
  private Object lastHud;

  private MainModel model;
  private Tasks tasks;

  public Lawena(MainModel mainModel) {
    model = mainModel;
  }

  public void start() {
    view = new LawenaView();
    tasks = new Tasks(this);

    os = model.getOsInterface();
    settings = model.getSettings();
    movies = model.getMovies();
    files = model.getFiles();
    demos = model.getDemos();
    resources = model.getResources();
    watcher = model.getWatcher();

    // setup ui loggers
    ch.qos.logback.classic.Logger rootLog =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("root");
    rootLog.addAppender(new LoggingAppender(view.getLogPane(), rootLog.getLoggerContext()));
    ch.qos.logback.classic.Logger statusLog =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("status");
    statusLog.setAdditive(false);
    statusLog.addAppender(new StatusAppender(view.getLblStatus(), statusLog.getLoggerContext()));

    tasks.new UpdaterTask().execute();

    view.setTitle("Lawena Recording Tool " + model.getShortVersion());
    try {
      view.setIconImage(new ImageIcon(getClass().getResource("tf2.png")).getImage());
    } catch (Exception e) {
      log.warn("Could not set icon to frame", e);
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
          dialog.getBtnUpdater().setEnabled(false);
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

    final JTable table = view.getTableCustomContent();
    table.setModel(resources);
    table.getColumnModel().getColumn(0).setMaxWidth(20);
    table.getColumnModel().getColumn(2).setMaxWidth(50);
    table.setDefaultRenderer(CustomPath.class, new TooltipRenderer(settings));
    table.getModel().addTableModelListener(new TableModelListener() {

      @Override
      public void tableChanged(TableModelEvent e) {
        if (e.getColumn() == CustomPathList.Column.SELECTED.ordinal()) {
          int row = e.getFirstRow();
          TableModel model = (TableModel) e.getSource();
          CustomPath cp = (CustomPath) model.getValueAt(row, CustomPathList.Column.PATH.ordinal());
          checkCustomHud(cp);
          if (cp == CustomPathList.particles && cp.isSelected()) {
            startParticlesDialog();
          }
        }
      }
    });
    table.setDropTarget(new DropTarget() {

      private static final long serialVersionUID = 1L;

      @Override
      public synchronized void dragOver(DropTargetDragEvent dtde) {
        Point point = dtde.getLocation();
        int row = table.rowAtPoint(point);
        if (row < 0) {
          table.clearSelection();
        } else {
          table.setRowSelectionInterval(row, row);
        }
        dtde.acceptDrag(DnDConstants.ACTION_COPY);
      }

      @Override
      public synchronized void drop(DropTargetDropEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
          dtde.acceptDrop(DnDConstants.ACTION_COPY);
          Transferable t = dtde.getTransferable();
          List<?> fileList = null;
          try {
            fileList = (List<?>) t.getTransferData(DataFlavor.javaFileListFlavor);
            if (fileList.size() > 0) {
              table.clearSelection();
              for (Object value : fileList) {
                if (value instanceof File) {
                  File f = (File) value;
                  log.info("Attempting to copy " + f.toPath());
                  new PathCopyTask(f.toPath()).execute();
                }
              }
            }
          } catch (UnsupportedFlavorException e) {
            log.warn("Drag and drop operation failed", e);
          } catch (IOException e) {
            log.warn("Drag and drop operation failed", e);
          }
        } else {
          dtde.rejectDrop();
        }
      }
    });
    TableRowSorter<CustomPathList> sorter = new TableRowSorter<>(resources);
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
          log.warn("Problem while configuring skyboxes", e);
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
        new LaunchTask(tasks).execute();
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
        new SwingWorker<Void, Void>() {
          protected Void doInBackground() throws Exception {
            try {
              Desktop.getDesktop().open(settings.getMoviePath().toFile());
            } catch (IOException ex) {
              log.warn("Could not open custom folder", ex);
            }
            return null;
          }
        }.execute();
      }
    });
    view.getMntmOpenCustomFolder().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        new SwingWorker<Void, Void>() {
          protected Void doInBackground() throws Exception {
            try {
              Desktop.getDesktop().open(Paths.get("custom").toFile());
            } catch (IOException ex) {
              log.warn("Could not open custom folder", ex);
            }
            return null;
          }
        }.execute();
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
    if (particles == null) {
      particles = new ParticlesDialog();
      particles.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      particles.setModalityType(ModalityType.APPLICATION_MODAL);
      DefaultTableModel dtm = new DefaultTableModel(0, 2) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int row, int column) {
          return column == 0;
        }

        @Override
        public java.lang.Class<?> getColumnClass(int columnIndex) {
          return columnIndex == 0 ? Boolean.class : String.class;
        };

        @Override
        public String getColumnName(int column) {
          return column == 0 ? "" : "Particle filename";
        }
      };
      final JTable tableParticles = particles.getTableParticles();
      particles.getOkButton().addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          List<String> selected = new ArrayList<>();
          int selectCount = 0;
          for (int i = 0; i < tableParticles.getRowCount(); i++) {
            if ((boolean) tableParticles.getValueAt(i, 0)) {
              selectCount++;
              selected.add((String) tableParticles.getValueAt(i, 1));
            }
          }
          if (selectCount == 0) {
            settings.setParticles(Arrays.asList(""));
          } else if (selectCount == tableParticles.getRowCount()) {
            settings.setParticles(Arrays.asList("*"));
          } else {
            settings.setParticles(selected);
          }
          log.debug("Particles: " + settings.getParticles());
          particles.setVisible(false);
        }
      });
      particles.getCancelButton().addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          List<String> selected = settings.getParticles();
          boolean selectAll = selected.contains("*");
          for (int i = 0; i < tableParticles.getRowCount(); i++) {
            tableParticles.setValueAt(
                selectAll || selected.contains(tableParticles.getValueAt(i, 1)), i, 0);
          }
          log.debug("Particles: " + selected);
          particles.setVisible(false);
        }
      });
      tableParticles.setModel(dtm);
      tableParticles.getColumnModel().getColumn(0).setMaxWidth(20);
      List<String> selected = settings.getParticles();
      boolean selectAll = selected.contains("*");
      for (String particle : os.getVpkContents(settings.getTfPath(),
          CustomPathList.particles.getPath())) {
        dtm.addRow(new Object[] {selectAll || selected.contains(particle), particle});
      }
    }
    particles.setVisible(true);
  }

  private DefaultTableModel newSegmentsModel() {
    return new DefaultTableModel(0, 2) {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean isCellEditable(int row, int column) {
        return column == 0;
      }

      @Override
      public java.lang.Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 0 ? Boolean.class : String.class;
      };

      @Override
      public String getColumnName(int column) {
        return column == 0 ? "" : "Segment";
      }
    };
  }

  private SegmentsDialog newSegmentsDialog() {
    final SegmentsDialog d = new SegmentsDialog();
    d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    d.setModalityType(ModalityType.APPLICATION_MODAL);
    DefaultTableModel dtm = newSegmentsModel();
    final JTable tableSegments = d.getTableSegments();
    d.getOkButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        List<String> selected = new ArrayList<>();
        int selectCount = 0;
        for (int i = 0; i < tableSegments.getRowCount(); i++) {
          if ((boolean) tableSegments.getValueAt(i, 0)) {
            selectCount++;
            selected.add((String) tableSegments.getValueAt(i, 1));
          }
        }
        if (selectCount > 0) {
          tasks.new ClearMoviesTask(selected).execute();
        } else {
          log.info("No segments selected to remove");
        }
        d.setVisible(false);
      }
    });
    d.getCancelButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        // for (int i = 0; i < tableSegments.getRowCount(); i++) {
        // tableSegments.setValueAt(false, i, 0);
        // }
        d.setVisible(false);
      }
    });
    tableSegments.setModel(dtm);
    tableSegments.getColumnModel().getColumn(0).setMaxWidth(20);
    return d;
  }

  private void startSegmentsDialog() {
    if (segments == null) {
      segments = newSegmentsDialog();
    }
    DefaultTableModel tmodel = (DefaultTableModel) segments.getTableSegments().getModel();
    tmodel.setRowCount(0);
    List<String> segs = getExistingSegments();
    if (segs.isEmpty()) {
      JOptionPane.showMessageDialog(view, "There are no segments to delete", "Delete Segments",
          JOptionPane.INFORMATION_MESSAGE);
    } else {
      for (String seg : segs) {
        tmodel.addRow(new Object[] {false, seg});
      }
      segments.setVisible(true);
    }
  }

  private List<String> getExistingSegments() {
    List<String> existingSegments = new ArrayList<>();
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(settings.getMoviePath(), "*.wav")) {
      for (Path path : stream) {
        String segname = path.getFileName().toString();
        String key = segname.substring(0, segname.indexOf("_"));
        if (!existingSegments.contains(key))
          existingSegments.add(key);
      }
    } catch (NoSuchFileException e) {
      // TODO: add a check for the reparse point (junction) to confirm it's SrcDemo2
      log.info("Could not scan for existing segments. Is SrcDemo2 running?");
    } catch (IOException e) {
      log.warn("Problem while scanning movie folder", e);
    }
    return existingSegments;
  }

  private boolean checkCustomHud(CustomPath cp) {
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

  private void loadHudComboState() {
    boolean detected = false;
    for (CustomPath cp : resources.getList()) {
      if (detected) {
        break;
      }
      detected = checkCustomHud(cp);
    }
  }

  private void loadSettings() {
    registerValidation(view.getCmbResolution(), "[1-9][0-9]*x[1-9][0-9]*", view.getLblResolution());
    registerValidation(view.getCmbFramerate(), "[1-9][0-9]*", view.getLblFrameRate());
    selectComboItem(view.getCmbHud(), settings.getHud(), Key.Hud.getAllowedValues());
    selectComboItem(view.getCmbQuality(), settings.getDxlevel(), Key.DxLevel.getAllowedValues());
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
    for (CustomPath cp : resources.getList()) {
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

  private void saveAndExit() {
    saveSettings();
    view.setVisible(false);
    if (!os.isRunningTF2()) {
      files.restoreAll();
    }
    System.exit(0);
  }

  private void configureSkyboxes(final JComboBox<String> combo) {
    final Vector<String> data = new Vector<>();
    Path dir = Paths.get("skybox");
    if (Files.exists(dir)) {
      log.info("Loading skyboxes from folder");
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*up.vtf")) {
        for (Path path : stream) {
          log.debug("Skybox found at: " + path);
          String skybox = path.toFile().getName();
          skybox = skybox.substring(0, skybox.indexOf("up.vtf"));
          data.add(skybox);
        }
      } catch (IOException e) {
        log.warn("Problem while loading skyboxes", e);
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

  public MainModel getModel() {
    return model;
  }

  public LawenaView getView() {
    return view;
  }

}
