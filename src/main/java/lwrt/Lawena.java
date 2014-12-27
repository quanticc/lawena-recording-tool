package lwrt;

import ui.AboutDialog;
import ui.LaunchOptionsDialog;
import ui.LawenaView;
import ui.ParticlesDialog;
import ui.SegmentsDialog;
import ui.TooltipRenderer;
import util.LawenaException;
import util.StartLogger;
import util.UpdateHelper;
import util.Util;
import util.WatchDir;
import vdm.DemoEditor;

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
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
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
    private List<String> segmentsToDelete;

    public ClearMoviesTask() {}

    public ClearMoviesTask(List<String> segmentsToDelete) {
      this.segmentsToDelete = segmentsToDelete;
    }

    @Override
    protected Void doInBackground() throws Exception {
      SwingUtilities.invokeAndWait(new Runnable() {

        @Override
        public void run() {
          view.getBtnClearMovieFolder().setEnabled(false);
        }
      });
      if (clearMoviesTask == null) {
        String segmentsGlob = "";
        if (segmentsToDelete != null && !segmentsToDelete.isEmpty()) {
          segmentsGlob =
              segmentsToDelete.toString().replace("[", "{").replace("]", "}").replace(" ", "");
          log.info("Deleting segments: " + segmentsGlob);
        } else {
          int answer =
              JOptionPane.showConfirmDialog(view,
                  "Are you sure you want to clear ALL movie files?", "Clearing Movie Files",
                  JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
          if (answer != JOptionPane.YES_NO_OPTION) {
            return null;
          }
        }
        try (DirectoryStream<Path> stream =
            Files.newDirectoryStream(settings.getMoviePath(), segmentsGlob + "*.{tga,wav}")) {

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
            try {
              Files.delete(path);
            } catch (IOException e) {
              log.log(Level.INFO, "Could not delete a file", e);
            }
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

  public class StartTfTask extends SwingWorker<Boolean, Void> {

    @Override
    protected Boolean doInBackground() throws Exception {
      try {
        return doStuff();
      } catch (Exception e) {
        log.log(Level.WARNING, "Start operation was interrupted or failed", e);
        return false;
      }
    }

    private Boolean doStuff() throws InvocationTargetException, InterruptedException {
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

        // Checking if the user selects "Custom" HUD in the dropdown,
        // he or she also selects a "hud" in the sidebar
        if (!verifyCustomHud()) {
          JOptionPane.showMessageDialog(view,
              "Please select a custom HUD in the\nCustom Resources table and retry", "Custom HUD",
              JOptionPane.INFORMATION_MESSAGE);
          log.info("Launch aborted because the custom HUD to use was not specified");
          return false;
        }

        setProgress(10);

        // Check for big custom folders, mitigate OOM errors with custom folder > 2 GB
        Path tfpath = settings.getTfPath();
        Path customPath = tfpath.resolve("custom");
        Path configPath = tfpath.resolve("cfg");
        try {
          long bytes = Util.sizeOfPath(configPath) + Util.sizeOfPath(customPath);
          String size = Util.humanReadableByteCount(bytes, true);
          log.info("Config and Custom folders size: " + size);
          if (bytes / 1024 / 1024 > settings.getInt(Key.BigFolderMBThreshold)) {
            int answer =
                JOptionPane
                    .showConfirmDialog(
                        null,
                        "Your cfg and custom folders are "
                            + size
                            + " in size."
                            + "\nPlease consider moving unnecesary custom files like maps to tf/download folder."
                            + "\nDo you still want to proceed?", "Copying Folders before Launch",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (answer == JOptionPane.NO_OPTION || answer == JOptionPane.CLOSED_OPTION) {
              log.info("Launch aborted by the user");
              status.info("Launch aborted by the user");
              return false;
            }
          }
        } catch (IOException e) {
          log.info("Could not determine folder size: " + e);
        }

        setProgress(20);
        closeTf2Handles();

        // Restoring user files
        status.info("Restoring your files");
        files.restoreAll();
        setProgress(40);

        // Saving ui settings to cfg files
        status.info("Saving settings and generating cfg files");
        try {
          saveSettings();
          settings.saveToCfg();
          movies.createMovienameCfgs();
        } catch (IOException e) {
          log.log(Level.WARNING, "Problem while saving settings to file", e);
          status.info("Failed to save lawena settings to file");
          return false;
        }
        // Allow failing this without cancelling launch, notify user
        // See https://github.com/iabarca/lawena-recording-tool/issues/36
        try {
          movies.movieOffset();
        } catch (IOException e) {
          log.info("Could not detect current movie slot");
        }

        setProgress(60);

        // Backing up user files and copying lawena files
        status.info("Copying lawena files to cfg and custom...");
        try {
          files.replaceAll();
        } catch (LawenaException e) {
          status.info(e.getMessage());
          return false;
        }
        setProgress(80);

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

        int timeout = 0;
        int cfgtimeout = settings.getLaunchTimeout();
        int millis = 5000;
        int maxtimeout = cfgtimeout / (millis / 1000);
        setProgress(0);
        status.info("Waiting for TF2 to start...");
        if (cfgtimeout > 0) {
          log.fine("TF2 launch timeout: around " + cfgtimeout + " seconds");
        } else {
          log.fine("TF2 launch timeout disabled");
        }
        while (!cl.isRunningTF2() && (cfgtimeout == 0 || timeout < maxtimeout)) {
          ++timeout;
          if (cfgtimeout > 0) {
            setProgress((int) ((double) timeout / maxtimeout * 100));
          }
          Thread.sleep(millis);
        }

        if (cfgtimeout > 0 && timeout >= maxtimeout) {
          int s = timeout * (millis / 1000);
          log.info("TF2 launch timed out after " + s + " seconds");
          status.info("TF2 did not start after " + s + " seconds");
          return false;
        }

        log.fine("TF2 has started running");
        status.info("Waiting for TF2 to finish running...");
        SwingUtilities.invokeLater(new Runnable() {

          @Override
          public void run() {
            view.getProgressBar().setIndeterminate(true);
          }
        });
        while (cl.isRunningTF2()) {
          Thread.sleep(millis);
        }

        Thread.sleep(5000);
        closeTf2Handles();

      } else {
        if (cl.isRunningTF2()) {
          status.info("Attempting to finish TF2 process...");
          cl.killTf2Process();
          Thread.sleep(5000);
          if (!cl.isRunningTF2()) {
            startTfTask.cancel(true);
          }
          closeTf2Handles();
        } else {
          status.info("TF2 was not running, cancelling");
        }
      }

      return true;
    }

    private void closeTf2Handles() {
      status.info("Closing open handles in TF2 'cfg' folder...");
      cl.closeHandles(settings.getTfPath().resolve("cfg"));
      status.info("Closing open handles in TF2 'custom' folder...");
      cl.closeHandles(settings.getTfPath().resolve("custom"));
    }

    private boolean verifyCustomHud() {
      if (view.getCmbHud().getSelectedItem().equals("Custom")) {
        for (CustomPath cp : customPaths.getList()) {
          customPaths.update(cp);
          EnumSet<PathContents> set = cp.getContents();
          if (cp.isSelected() && set.contains(PathContents.HUD)) {
            return true;
          }
        }
        return false;
      } else {
        return true;
      }
    }

    @Override
    protected void done() {
      if (!isCancelled()) {
        startTfTask = null;
        setCurrentWorker(null, false);
        view.getBtnStartTf().setEnabled(false);
        boolean ranTf2Correctly = false;
        try {
          ranTf2Correctly = get();
        } catch (InterruptedException | ExecutionException e) {
        }
        boolean restoredAllFiles = files.restoreAll();
        if (ranTf2Correctly) {
          if (restoredAllFiles) {
            status.info("TF2 has finished running. All files restored");
          } else {
            status.info("Your files could not be restored correctly. Check log for details");
          }
        }
        cl.setSystemDxLevel(oDxlevel);
        view.getBtnStartTf().setText("Start Team Fortress 2");
        view.getBtnStartTf().setEnabled(true);
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
        log.log(Level.INFO, "Problem while scanning custom paths", e);
      }
      return null;
    }

    private void scan() {
      customPaths.clear();
      customPaths.addPaths(Paths.get("custom"));
      customPaths.addPaths(settings.getTfPath().resolve("custom"));
      customPaths.validateRequired();
    }

    @Override
    protected void done() {
      customPaths.loadResourceSettings();
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
        log.log(Level.FINE, "Custom path copy task was cancelled", e);
      }
      if (!result) {
        try {
          customPaths.addPath(from);
          log.info(from + " added to custom resource list");
        } catch (IOException e) {
          log.log(Level.FINE, "Problem while loading a custom path", e);
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
            cl.extractIfNeeded(settings.getTfPath(), "custom/skybox.vpk", Paths.get("skybox"),
                Arrays.asList(filename));
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
  private DemoEditor vdm;
  private CommandLine cl;
  private CustomPathList customPaths;

  private AboutDialog dialog;
  private ParticlesDialog particles;
  private SegmentsDialog segments;
  private JTextArea customSettingsTextArea;
  private JScrollPane customSettingsScrollPane;

  private HashMap<String, ImageIcon> skyboxMap;
  private JFileChooser chooser;
  private String oDxlevel;
  private Thread watcher;
  private Object lastHud;

  private String version = "4.1";
  private String build;
  private UpdateHelper updater;

  private LaunchOptionsDialog launchOptionsDialog;

  public Lawena(SettingsManager cfg) {
    String impl = this.getClass().getPackage().getImplementationVersion();
    if (impl != null) {
      version = impl;
    }
    build = getManifestString("Implementation-Build", Util.now("yyyyMMddHHmmss"));
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

    // Perform after-update checks
    updater = new UpdateHelper();
    updater.updateLauncher();
    updater.cleanupUnusedFiles();
    updater.loadChannels();

    settings = cfg;
    log.fine("Retrieving current dxlevel and Steam path");
    oDxlevel = cl.getSystemDxLevel();

    // retrieve SteamPath, attempt 2 registry queries, user input if all else fails
    Path steampath = cl.getSteamPath();
    if (steampath.toString().isEmpty()) {
      steampath = Paths.get(settings.getString(Key.AltSteamDir));
    }
    log.fine("Checking for Steam path at " + steampath);
    if (!steampath.getFileName().toString().equalsIgnoreCase("Steam") || !Files.exists(steampath)) {
      log.warning("IMPORTANT: SteamPath from registry is invalid. Check your Steam installation");
      log.warning("Value should be at HKEY_CURRENT_USER\\Software\\Valve\\Steam on key SteamPath");
      log.info("Asking for manual SteamPath");
      steampath = getChosenSteamPath();
      if (steampath == null) {
        log.info("No Steam directory specified, exiting.");
        JOptionPane.showMessageDialog(null, "No Steam directory specified, program will exit.",
            "Invalid SteamPath", JOptionPane.WARNING_MESSAGE);
        throw new IllegalArgumentException("Steam directory must be specified");
      }
      settings.setString(Key.AltSteamDir, steampath.toString());
    }
    settings.setString(Key.SteamDir, steampath.toString());

    // retrieve GamePath, attempt resolving via SteamPath, otherwise ask user for it
    Path tfpath = settings.getTfPath();
    if (tfpath == null || tfpath.toString().isEmpty()) {
      tfpath = steampath.resolve("SteamApps/common/Team Fortress 2/tf");
    }
    log.fine("Checking for game path at " + tfpath);
    if (!tfpath.getFileName().toString().equalsIgnoreCase("tf") || !Files.exists(tfpath)) {
      tfpath = getChosenTfPath();
      if (tfpath == null) {
        log.info("No game directory specified, exiting.");
        JOptionPane.showMessageDialog(null, "No game directory specified, program will exit.",
            "Invalid GamePath", JOptionPane.WARNING_MESSAGE);
        throw new IllegalArgumentException("A game directory must be specified");
      }
    }
    settings.setTfPath(tfpath);
    files = new FileManager(settings, cl);

    // retrieve MoviePath, always ask user
    Path moviepath = settings.getMoviePath();
    log.info("Checking for movie path at " + moviepath);
    if (moviepath == null || moviepath.toString().isEmpty() || !Files.exists(moviepath)) {
      moviepath = getChosenMoviePath();
      if (moviepath == null) {
        log.info("No movie directory specified, exiting.");
        JOptionPane.showMessageDialog(null, "No movie directory specified, program will exit.",
            "Invalid MoviePath", JOptionPane.WARNING_MESSAGE);
        throw new IllegalArgumentException("A segment directory must be specified");
      }
    }
    movies = new MovieManager(settings);
    settings.setMoviePath(moviepath);

    log.fine("Saving settings to file");
    settings.save();
    log.fine("Restoring TF2 user files if needed");
    files.restoreAll();

    customPaths = new CustomPathList(settings, cl);
    files.setCustomPathList(customPaths);

    watcher = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          WatchDir w = new WatchDir(Paths.get("custom"), false) {
            @Override
            public void entryCreated(Path child) {
              try {
                customPaths.addPath(child);
              } catch (IOException e) {
                log.log(Level.FINE, "Could not add custom path", e);
              }
            }

            @Override
            public void entryModified(Path child) {
              customPaths.updatePath(child);
            };

            @Override
            public void entryDeleted(Path child) {
              customPaths.removePath(child);
            }
          };
          w.processEvents();
        } catch (IOException e) {
          log.log(Level.FINE, "Problem while watching directory", e);
        }
      }
    }, "FolderWatcher");
    watcher.setDaemon(true);

    vdm = new DemoEditor(settings, cl);

    log.fine("Init complete - Ready to display GUI");
  }

  private String getManifestString(String key, String defaultValue) {
    try (JarFile jar =
        new JarFile(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation()
            .toURI()))) {
      String value = jar.getManifest().getMainAttributes().getValue(key);
      return (value == null ? "bat." + defaultValue : value);
    } catch (IOException | URISyntaxException e) {
    }
    return "custom." + defaultValue;
  }

  private String shortver() {
    String[] arr = version.split("-");
    return arr[0] + (arr.length > 1 ? "-" + arr[1] : "");
  }

  public void start() {
    view = new LawenaView();

    new StartLogger("lawena").toTextComponent(settings.getLogUiLevel(), view.getTextAreaLog());
    new StartLogger("status").toLabel(Level.FINE, view.getLblStatus());
    log.fine("Lawena Recording Tool " + version + " build " + build);
    log.fine("TF2 path: " + settings.getTfPath());
    log.fine("Steam path: " + cl.getSteamPath());
    log.fine("Movie path: " + settings.getMoviePath());
    log.fine("Lawena path: " + Paths.get("").toAbsolutePath());

    view.setTitle("Lawena Recording Tool " + shortver());
    try {
      view.setIconImage(new ImageIcon(Lawena.class.getClassLoader().getResource("ui/tf2.png"))
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
          dialog.getBtnUpdater().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
              updater.showSwitchUpdateChannelDialog();
            }
          });
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
    table.setModel(customPaths);
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
            log.log(Level.FINE, "Drag and drop operation failed", e);
          } catch (IOException e) {
            log.log(Level.FINE, "Drag and drop operation failed", e);
          }
        } else {
          dtde.rejectDrop();
        }
      }
    });
    TableRowSorter<CustomPathList> sorter = new TableRowSorter<>(customPaths);
    table.setRowSorter(sorter);
    RowFilter<CustomPathList, Object> filter = new RowFilter<CustomPathList, Object>() {
      public boolean include(Entry<? extends CustomPathList, ? extends Object> entry) {
        CustomPath cp = (CustomPath) entry.getValue(CustomPathList.Column.PATH.ordinal());
        return !cp.getContents().contains(PathContents.READONLY);
      }
    };
    sorter.setRowFilter(filter);

    SwingWorker<Void, Void> scannerTask = new PathScanTask();
    SwingWorker<Void, Void> skySetupTask = new SwingWorker<Void, Void>() {
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
    };
    scannerTask.execute();
    skySetupTask.execute();
    waitForTask(scannerTask);
    waitForTask(skySetupTask);

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
        customPaths.loadResourceSettings();
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
        new StartTfTask().execute();
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
              log.log(Level.FINE, "Could not open custom folder", ex);
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
              log.log(Level.FINE, "Could not open custom folder", ex);
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
    view.getCustomLaunchOptionsMenuItem().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (launchOptionsDialog == null) {
          launchOptionsDialog = new LaunchOptionsDialog();
        }
        launchOptionsDialog.getOptionsTextField().setText(settings.getString(Key.LaunchOptions));
        int result = launchOptionsDialog.showDialog();
        if (result == JOptionPane.YES_OPTION) {
          String launchOptions = launchOptionsDialog.getOptionsTextField().getText();
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

    view.getTabbedPane().addTab("VDM", null, vdm.start());
    view.setVisible(true);
  }

  private void waitForTask(SwingWorker<?, ?> worker) {
    try {
      worker.get();
    } catch (InterruptedException | ExecutionException e) {
      log.warning("Task was interrupted or cancelled: " + e.toString());
    }
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
          log.finer("Particles: " + settings.getParticles());
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
          log.finer("Particles: " + selected);
          particles.setVisible(false);
        }
      });
      tableParticles.setModel(dtm);
      tableParticles.getColumnModel().getColumn(0).setMaxWidth(20);
      List<String> selected = settings.getParticles();
      boolean selectAll = selected.contains("*");
      for (String particle : cl.getVpkContents(settings.getTfPath(),
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
          new ClearMoviesTask(selected).execute();
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
      log.log(Level.INFO, "Problem while scanning movie folder", e);
    }
    return existingSegments;
  }

  private boolean checkCustomHud(CustomPath cp) {
    EnumSet<PathContents> set = cp.getContents();
    if (cp.isSelected()) {
      if (set.contains(PathContents.HUD)) {
        lastHud = view.getCmbHud().getSelectedItem();
        view.getCmbHud().setSelectedItem("Custom");
        log.finer("HUD combobox disabled");
        view.getCmbHud().setEnabled(false);
        return true;
      }
    } else {
      if (set.contains(PathContents.HUD)) {
        if (lastHud != null) {
          view.getCmbHud().setSelectedItem(lastHud);
        }
        log.finer("HUD combobox enabled");
        view.getCmbHud().setEnabled(true);
        return false;
      }
    }
    return false;
  }

  private void loadHudComboState() {
    boolean detected = false;
    for (CustomPath cp : customPaths.getList()) {
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

  private void saveSettings() {
    String[] resolution = ((String) view.getCmbResolution().getSelectedItem()).split("x");
    if (resolution.length == 2) {
      settings.setWidth(Integer.parseInt(resolution[0]));
      settings.setHeight(Integer.parseInt(resolution[1]));
    } else {
      log.fine("Bad resolution format, reverting to previously saved");
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
    for (CustomPath cp : customPaths.getList()) {
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
    log.fine("Settings saved");
  }

  private void saveAndExit() {
    saveSettings();
    view.setVisible(false);
    if (!cl.isRunningTF2()) {
      files.restoreAll();
    }
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
      chooser = new JFileChooser();
      chooser.setDialogTitle("Choose a directory to store your movie files");
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      ret = chooser.showOpenDialog(null);
      if (ret == JFileChooser.APPROVE_OPTION) {
        selected = chooser.getSelectedFile().toPath();
      } else {
        selected = null;
      }
      log.finer("Selected MoviePath: " + selected);
    }
    return selected;
  }

  private Path getChosenTfPath() {
    Path selected = null;
    int ret = 0;
    while ((selected == null && ret == 0)
        || (selected != null && (!Files.exists(selected) || !selected.getFileName().toString()
            .equals("tf")))) {
      chooser = new JFileChooser();
      chooser.setDialogTitle("Choose your \"tf\" directory");
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      chooser.setFileHidingEnabled(false);
      ret = chooser.showOpenDialog(null);
      if (ret == JFileChooser.APPROVE_OPTION) {
        selected = chooser.getSelectedFile().toPath();
      } else {
        selected = null;
      }
      log.finer("Selected GamePath: " + selected);
    }
    return selected;
  }

  private Path getChosenSteamPath() {
    Path selected = null;
    int ret = 0;
    while ((selected == null && ret == 0)
        || (selected != null && (!Files.exists(selected.resolve("steam.exe")) || !selected.toFile()
            .getName().toString().equals("Steam")))) {
      chooser = new JFileChooser();
      chooser.setDialogTitle("Choose your \"Steam\" directory (where steam.exe is)");
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      chooser.setFileHidingEnabled(false);
      ret = chooser.showOpenDialog(null);
      if (ret == JFileChooser.APPROVE_OPTION) {
        selected = chooser.getSelectedFile().toPath();
      } else {
        selected = null;
      }
      log.finer("Selected SteamPath: " + selected);
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
