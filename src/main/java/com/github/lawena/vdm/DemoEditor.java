package com.github.lawena.vdm;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.tomahawk.ExtensionsFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.model.LwrtSettings;
import com.github.lawena.model.LwrtSettings.Key;
import com.github.lawena.os.OSInterface;
import com.github.lawena.ui.DemoEditorView;
import com.github.lawena.ui.DemoRenderer;
import com.github.lawena.util.StatusAppender;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class DemoEditor {

  private static final Logger log = LoggerFactory.getLogger(DemoEditor.class);
  private static final Logger status = LoggerFactory.getLogger("status");

  public class RegexFilterDocumentListener implements DocumentListener {

    @Override
    public void insertUpdate(DocumentEvent e) {
      updateFilter();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      updateFilter();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
      updateFilter();
    }

    private void updateFilter() {
      TableRowSorter<? extends TableModel> sorter =
          (TableRowSorter<? extends TableModel>) view.getDemosTable().getRowSorter();
      RowFilter<TableModel, Object> rf = null;
      String input = view.getDemoFilterTextField().getText();
      try {
        rf = RowFilter.regexFilter(input);
      } catch (PatternSyntaxException e) {
        return;
      }
      sorter.setRowFilter(rf);
    }

  }

  public class AddSegment implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = view.getDemosTable().getSelectedRow();
      if (row < 0) {
        JOptionPane.showMessageDialog(view, "No demo file selected", "Adding Segment",
            JOptionPane.WARNING_MESSAGE);
        return;
      }
      Demo demo = demoModel.getDemo(row);
      Path demoPath = demo.getPath();
      if (!Files.exists(demoPath)) {
        JOptionPane.showMessageDialog(view,
            "Please fill the required demo file field with a valid demo file", "Error",
            JOptionPane.ERROR_MESSAGE);
        return;
      }
      // attempt to get a demo path relative to game path
      try {
        demoPath = settings.getTfPath().relativize(demoPath);
      } catch (IllegalArgumentException ex) {
        log.warn("Demos folder is not relative to game folder. VDM files created might not work anymore");
      }
      try {
        int tick1 = Integer.parseInt(view.getStartTickTextField().getText());
        int tick2 = Integer.parseInt(view.getEndTickTextField().getText());
        Tick segment = new Tick(demoPath.toString(), tick1, tick2);
        if (demo.getTickNumber() > 0 && tick2 > demo.getTickNumber()) {
          throw new IllegalArgumentException(
              "Ending tick must not be greater than total tick count");
        }
        tickModel.addTick(segment);
        log.info("Adding segment: " + segment);
      } catch (IllegalArgumentException ex) {
        JOptionPane.showMessageDialog(view,
            "Please fill the required tick fields with valid numbers\n" + ex.getMessage(), "Error",
            JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  public class CreateVdmFiles implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      if (tickModel.getRowCount() > 0) {
        try {
          final List<Path> paths = new VDMGenerator(tickModel.getTickList(), settings).generate();
          status.info("VDM generated: " + paths.size()
              + (paths.size() == 1 ? " new file" : " new files") + " in TF2 directory");
          new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
              osInterface.openFolder(paths.get(0));
              return null;
            }
          }.execute();

        } catch (IOException e1) {
          log.warn("A problem occurred while generating the VDM: " + e1);
          status.info("Problem occurred while generating the VDM files");
        }
      }
    }
  }

  public class DeleteVdmFiles extends SwingWorker<Void, Path> {

    private int count = 0;

    @Override
    protected Void doInBackground() throws Exception {
      SwingUtilities.invokeAndWait(new Runnable() {

        @Override
        public void run() {
          view.getDeleteVdmFilesButton().setEnabled(false);
        }
      });
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(settings.getTfPath(), "*.vdm")) {

        for (Path path : stream) {
          if (isCancelled()) {
            break;
          }
          path.toFile().setWritable(true);
          Files.delete(path);
          publish(path);
        }

      } catch (IOException ex) {
        log.warn("Problem while deleting VDM files", ex);
      }

      return null;
    }

    @Override
    protected void process(List<Path> chunks) {
      count += chunks.size();
      status.info("Deleting " + count + (count == 1 ? " VDM file " : " VDM files ")
          + "from TF2 folder...");
    };

    @Override
    protected void done() {
      if (!isCancelled()) {
        if (count > 0) {
          String str =
              "VDM files cleared: " + count + (count == 1 ? " file " : " files ") + "deleted";
          log.info(str);
          status.info(str);
        } else {
          log.info("No VDM files were deleted");
          status.info("");
        }
        view.getDeleteVdmFilesButton().setEnabled(true);
      }
    };

  }

  private DemoEditorView view;
  private final TickTableModel tickModel = new TickTableModel();
  private DemoTableModel demoModel;
  private LwrtSettings settings;
  private OSInterface osInterface;
  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public DemoEditor(LwrtSettings settings, OSInterface osInterface) {
    this.settings = settings;
    this.osInterface = osInterface;
    String relative = settings.getString(Key.DemosRelativeFolder);
    Path demoPath = settings.getTfPath();
    if (!relative.equals("")) {
      demoPath = demoPath.resolve(relative);
    }
    this.demoModel = new DemoTableModel(settings);
    this.demoModel.setDemosPath(demoPath);
  }

  public Component start() {
    view = new DemoEditorView();

    view.getSegmentListTable().setModel(tickModel);
    view.getSegmentListTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    int vColIndex = 0;
    TableColumn col = view.getSegmentListTable().getColumnModel().getColumn(vColIndex);
    int columnwidth = 400;
    col.setPreferredWidth(columnwidth);
    view.getSegmentListTable().setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    view.getSegmentListTable().setFillsViewportHeight(true);

    view.getDemosTable().setModel(demoModel);
    view.getDemosTable().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    view.getDemosTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    view.getDemosTable().getColumnModel().getColumn(0).setPreferredWidth(300);
    view.getDemosTable().getColumnModel().getColumn(1).setPreferredWidth(125);
    view.getDemosTable().getColumnModel().getColumn(2).setPreferredWidth(100);
    view.getDemosTable().getColumnModel().getColumn(3).setPreferredWidth(75);
    view.getDemosTable().getColumnModel().getColumn(4).setPreferredWidth(75);
    view.getDemosTable().getColumnModel().getColumn(5).setPreferredWidth(150);
    view.getDemosTable().getColumnModel().getColumn(6).setPreferredWidth(300);
    view.getDemosTable().setFillsViewportHeight(true);
    view.getDemosTable().setDefaultRenderer(Demo.class, new DemoRenderer());
    TableRowSorter<TableModel> sorter =
        new TableRowSorter<TableModel>(view.getDemosTable().getModel());
    view.getDemosTable().setRowSorter(sorter);

    view.getAddButton().addActionListener(new AddSegment());
    view.getClearTickListButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        log.info("All VDM segments deleted");
        tickModel.clear();
      }
    });
    view.getCreateVdmFilesButton().addActionListener(new CreateVdmFiles());
    view.getDeleteVdmFilesButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        int answer =
            JOptionPane.showConfirmDialog(view,
                "Are you sure you want to clear all .vdm files in your TF2 folder?",
                "Clear VDM Files", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
          new DeleteVdmFiles().execute();
        }
      }
    });
    view.getDeleteSelectedTickButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        int numRows = view.getSegmentListTable().getSelectedRowCount();
        for (int i = 0; i < numRows; i++) {
          tickModel.removeTick(view.getSegmentListTable().getSelectedRow());
        }
      }
    });
    view.getNoSkipToTickCheckBox().setSelected(settings.getBoolean(Key.VdmSrcDemoFix));
    view.getNoSkipToTickCheckBox().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        settings.setBoolean(Key.VdmSrcDemoFix, view.getNoSkipToTickCheckBox().isSelected());
      }
    });

    final DefaultComboBoxModel<String> defaultSegmentModel =
        new DefaultComboBoxModel<>(new String[] {"Record Segment"});
    view.getSegmentTypeComboBox().setModel(defaultSegmentModel);
    view.getSegmentTypeComboBox().addItemListener(new ItemListener() {

      @Override
      public void itemStateChanged(ItemEvent e) {
        view.getStartTickTextField().setText("");
        view.getEndTickTextField().setText("");
        if (e.getStateChange() == ItemEvent.SELECTED) {
          String selected = e.getItem().toString();
          String[] tokens = selected.split(" at ");
          if (tokens.length == 2) {
            try {
              // the -500 is a margin added to avoid dropping frames due to loading a specific tick
              view.getStartTickTextField().setText(Integer.parseInt(tokens[1]) - 500 + "");
            } catch (NumberFormatException x) {
            }
          }
        }
      }
    });
    view.getDemosTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {

      @Override
      public void valueChanged(ListSelectionEvent e) {
        int index = view.getDemosTable().getSelectedRow();
        if (index >= 0) {
          if (!e.getValueIsAdjusting()) {
            Demo demo =
                (Demo) demoModel.getDemo(view.getDemosTable().convertRowIndexToModel(index));
            List<KillStreak> streaks = demo.getStreaks();
            DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();
            for (int i = 0; i < defaultSegmentModel.getSize(); i++) {
              m.addElement(defaultSegmentModel.getElementAt(i));
            }
            for (KillStreak streak : streaks) {
              m.addElement(streak.getDescription() + " at " + streak.getTick());
            }
            view.getSegmentTypeComboBox().setModel(m);
          }
        } else {
          view.getSegmentTypeComboBox().setModel(defaultSegmentModel);
        }
        view.getStartTickTextField().setText("");
        view.getEndTickTextField().setText("");
      }
    });
    view.getDemoFilterTextField().getDocument()
        .addDocumentListener(new RegexFilterDocumentListener());
    view.getSaveSegmentListButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        Frame frame = (Frame) SwingUtilities.getWindowAncestor(view);
        String file =
            osInterface.chooseSaveFile(frame, "Select the location to save the segment list",
                demoModel.getDemosPath().toString(),
                Arrays.asList(new ExtensionsFilter("JSON file", Arrays.asList("json"))));
        if (file != null) {
          if (!file.endsWith(".json")) {
            file = file + ".json";
          }
          List<Tick> list = tickModel.getTickList();
          Path dest = Paths.get(file);
          int answer = JOptionPane.YES_OPTION;
          if (Files.exists(dest)) {
            answer =
                JOptionPane.showConfirmDialog(view, "File " + dest
                    + " already exists.\nDo you want to overwrite it?", "Saving Segment List",
                    JOptionPane.YES_NO_OPTION);
          }
          if (answer == JOptionPane.YES_OPTION) {
            try {
              log.info("Saving segment list to {}", file);
              Files.write(dest,
                  Arrays.asList(gson.toJson(list, new TypeToken<List<Tick>>() {}.getType())),
                  Charset.forName("UTF-8"));
              status.info(StatusAppender.OK, "Segment list saved to {}", file);
            } catch (IOException ex) {
              log.warn("Could not save segment list: " + ex);
              status.info(StatusAppender.WARN, "Could not save segment list");
            }
          }
        }
      }
    });
    view.getLoadSegmentListButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        Frame frame = (Frame) SwingUtilities.getWindowAncestor(view);
        String file =
            osInterface.chooseSingleFile(frame, "Select a segment list file to load", demoModel
                .getDemosPath().toString(), Arrays.asList(new ExtensionsFilter("JSON file", Arrays
                .asList("json"))));
        int answer = JOptionPane.YES_OPTION;
        if (tickModel.getRowCount() > 0) {
          answer =
              JOptionPane.showConfirmDialog(view,
                  "This will clear all current segments.\nDo you want to overwrite it?",
                  "Loading Segment List", JOptionPane.YES_NO_OPTION);
        }
        if (answer == JOptionPane.YES_OPTION) {
          try {
            log.info("Loading segment list from {}", file);
            Reader reader =
                new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
            List<Tick> list = gson.fromJson(reader, new TypeToken<List<Tick>>() {}.getType());
            tickModel.clear();
            for (Tick tick : list) {
              tickModel.addTick(tick);
            }
            status.info(StatusAppender.OK, "Segment list loaded from {}", file);
          } catch (IOException ex) {
            log.warn("Could not load segment list: " + ex);
            status.info(StatusAppender.WARN, "Could not load segment list");
          }
        }
      }
    });
    view.getChangeDemoFolderButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        int answer =
            JOptionPane.showConfirmDialog(view,
                "Changing the demo folder will reload the demo list\n"
                    + "and remove all segments currently in the table.\nDo you want to continue?",
                "Switch Demo Folder", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
          Frame frame = (Frame) SwingUtilities.getWindowAncestor(view);
          String folder =
              osInterface.chooseSingleFolder(frame, "Choose a folder to load demos from", demoModel
                  .getDemosPath().toString());
          if (folder != null) {
            Path selected = Paths.get(folder);
            log.info("Reloading demos from {}", selected);
            demoModel.setDemosPath(selected);
            tickModel.clear();
            status.info(StatusAppender.OK, "Loaded demos from {}", selected);
          }
        }
      }
    });

    return view;
  }

  public String getDemoname() {
    if (!tickModel.getTickList().isEmpty()) {
      return tickModel.getTickList().get(0).getDemoname();
    }
    return "";
  }

  public boolean isAutoplay() {
    return view.getAutoplayFirstDemoCheckBox().isSelected();
  }
}
