package vdm;

import ui.DemoEditorView;
import util.DemoPreview;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumn;

import lwrt.CommandLine;
import lwrt.SettingsManager;
import lwrt.SettingsManager.Key;

public class DemoEditor {

  private static final Logger log = Logger.getLogger("lawena");
  private static final Logger status = Logger.getLogger("status");

  public class VdmAddTick implements ActionListener {

    private final String type;
    private final String template;

    public VdmAddTick(String type, String template) {
      this.type = type;
      this.template = template;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (currentDemoFile == null || !Files.exists(currentDemoFile.toPath())) {
        JOptionPane.showMessageDialog(view,
            "Please fill the required demo file field with a valid demo file", "Error",
            JOptionPane.ERROR_MESSAGE);
        return;
      }

      try {
        int tick1 = Integer.parseInt(view.getTxtStarttick().getText());
        int tick2 = Integer.parseInt(view.getTxtEndtick().getText());
        if (tick1 >= tick2) {
          throw new NumberFormatException();
        }
        Tick segment =
            new Tick(currentDemoFile, settings.getTfPath().relativize(currentDemoFile.toPath())
                .toString(), tick1, tick2);
        segment.setType(type);
        segment.setTemplate(template);
        model.addTick(segment);
        log.info("Adding segment: " + segment);
      } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(view,
            "Please fill the required tick fields with valid numbers", "Error",
            JOptionPane.ERROR_MESSAGE);
      }
    }

  }

  public class VdmBrowseDemo implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      int returnVal = choosedemo.showOpenDialog(view);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        currentDemoFile = choosedemo.getSelectedFile();
        if (Files.exists(currentDemoFile.toPath())) {
          log.info("Selected demo file: " + currentDemoFile);
          view.getTxtDemofile().setText(currentDemoFile.getName());
          updateDemoDetails();
        } else {
          JOptionPane.showMessageDialog(view, "The selected file does not exist.", "Browse",
              JOptionPane.INFORMATION_MESSAGE);
        }
      }
    }

  }

  public class VdmClearTicks implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      model.clear();
    }

  }

  public class VdmCreateFile implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      if (model.getRowCount() > 0) {
        vdmgenerator = new VDMGenerator(model.getTickList(), settings);
        try {
          final List<Path> paths = vdmgenerator.generate();
          status.info("Created " + paths.size() + (paths.size() == 1 ? " new file" : " new files"));
          new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
              // open each parent of the generated files, removing duplicates
              paths.stream().map(p -> p.toAbsolutePath().getParent()).filter(p -> p != null)
                  .distinct().forEach(p -> cl.open(p));
              return null;
            }
          }.execute();

        } catch (IOException e1) {
          log.warning("A problem occurred while generating the VDM: " + e1);
          status.info("Problem occurred while generating the VDM files");
        }
      }
    }
  }

  public class ClearVdmFilesTask extends SwingWorker<Void, Path> {

    private int count = 0;

    @Override
    protected Void doInBackground() throws Exception {
      SwingUtilities.invokeAndWait(new Runnable() {
        @Override
        public void run() {
          view.getBtnDeleteVdmFiles().setEnabled(false);
        }
      });
      deleteVdmFromFolder(settings.getTfPath());
      if (currentDemoFile != null) {
        deleteVdmFromFolder(currentDemoFile.getParentFile().toPath());
      }
      return null;
    }

    private void deleteVdmFromFolder(Path dir) {
      log.info("Deleting VDM files from " + dir);
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.vdm")) {
        for (Path path : stream) {
          if (isCancelled()) {
            break;
          }
          path.toFile().setWritable(true);
          Files.delete(path);
          publish(path);
        }
      } catch (IOException ex) {
        log.log(Level.INFO, "Problem while deleting VDM files", ex);
      }
    }

    @Override
    protected void process(List<Path> chunks) {
      count += chunks.size();
      status.info("Deleting " + count + (count == 1 ? " VDM file " : " VDM files "));
    }

    @Override
    protected void done() {
      if (!isCancelled()) {
        if (count > 0) {
          String str =
              "VDM files cleared: " + count + (count == 1 ? " file " : " files ") + "deleted";
          log.fine(str);
          status.info(str);
        } else {
          log.fine("No VDM files were deleted");
          status.info("");
        }
        view.getBtnDeleteVdmFiles().setEnabled(true);
      }
    }

  }

  private DemoEditorView view;
  private JFileChooser choosedemo = new JFileChooser();
  private TickTableModel model;
  private SettingsManager settings;
  private CommandLine cl;
  private VDMGenerator vdmgenerator;
  private File currentDemoFile;

  public DemoEditor(SettingsManager settings, CommandLine cl) {
    this.settings = settings;
    this.cl = cl;
    choosedemo.setDialogTitle("Choose a demo file");
    choosedemo.setFileSelectionMode(JFileChooser.FILES_ONLY);
    choosedemo.setFileFilter(new FileNameExtensionFilter("Demo files", new String[] {"DEM"}));
    choosedemo.setCurrentDirectory(settings.getTfPath().toFile());

    model = new TickTableModel();
  }

  public void updateDemoDetails() {
    new SwingWorker<String, Void>() {

      @Override
      protected String doInBackground() throws Exception {
        try (DemoPreview dp = new DemoPreview(currentDemoFile.toPath())) {
          return dp.toString();
        }
      }

      @Override
      protected void done() {
        try {
          view.getTxtrDemodetails().setText(get());
        } catch (InterruptedException | ExecutionException e) {
          view.getTxtrDemodetails().setText("Could not retrieve demo details");
        }
      }

    }.execute();
  }

  public Component start() {
    view = new DemoEditorView();

    view.getTableTicks().setModel(model);
    view.getTableTicks().setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    view.getTableTicks().setFillsViewportHeight(true);

    TableColumn typeColumn =
        view.getTableTicks().getColumnModel().getColumn(TickTableModel.Column.TYPE.ordinal());
    JComboBox<String> segmentTypes = new JComboBox<>();
    segmentTypes.setEditable(false);
    segmentTypes.addItem(Tick.RECORD_SEGMENT);
    segmentTypes.addItem(Tick.EXEC_RECORD_SEGMENT);
    typeColumn.setCellEditor(new DefaultCellEditor(segmentTypes));

    TableColumn templateColumn =
        view.getTableTicks().getColumnModel().getColumn(TickTableModel.Column.TEMPLATE.ordinal());
    JComboBox<String> templateTypes = new JComboBox<>();
    templateTypes.setEditable(true);
    templateTypes.addItem(Tick.NO_TEMPLATE);
    templateTypes.addItem(Tick.CAM_IMPORT_TEMPLATE);
    templateColumn.setCellEditor(new DefaultCellEditor(templateTypes));

    view.getBtnAdd().addActionListener(new VdmAddTick(Tick.RECORD_SEGMENT, Tick.NO_TEMPLATE));
    view.getBtnAddExecRecord().addActionListener(
        new VdmAddTick(Tick.EXEC_RECORD_SEGMENT, Tick.CAM_IMPORT_TEMPLATE));
    view.getBtnBrowse().addActionListener(new VdmBrowseDemo());
    view.getBtnClearTickList().addActionListener(new VdmClearTicks());
    view.getBtnCreateVdmFiles().addActionListener(new VdmCreateFile());
    view.getBtnDeleteVdmFiles().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        int answer =
            JOptionPane
                .showConfirmDialog(
                    view,
                    "Are you sure you want to clear all .vdm files in your TF2 and current demo folder?",
                    "Clear VDM Files", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
          new ClearVdmFilesTask().execute();
        }
      }
    });
    view.getBtnDeleteSelectedTick().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        int numRows = view.getTableTicks().getSelectedRowCount();
        for (int i = 0; i < numRows; i++) {
          model.removeTick(view.getTableTicks().getSelectedRow());
        }
      }
    });

    String rawSkipMode = settings.getString(Key.VdmSkipMode);
    SkipMode skipMode = SkipMode.SKIP_AHEAD;
    try {
      skipMode = SkipMode.valueOf(rawSkipMode);
    } catch (IllegalArgumentException e) {
      log.warning("Invalid value detected for skip mode: " + rawSkipMode);
    }
    view.getCmbSkipMode().setSelectedItem(skipMode);
    view.getCmbSkipMode().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        settings.setString(Key.VdmSkipMode,
            ((SkipMode) view.getCmbSkipMode().getSelectedItem()).name());
      }
    });

    return view;
  }

}
