package com.github.lawena.vdm;

import static com.github.lawena.util.Util.toPath;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
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

import com.github.lawena.Messages;
import com.github.lawena.app.model.Settings;
import com.github.lawena.os.OSInterface;
import com.github.lawena.profile.Key;
import com.github.lawena.ui.DemoEditorView;
import com.github.lawena.ui.DemoRenderer;
import com.github.lawena.util.StatusAppender;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class DemoEditor {

  static final Logger log = LoggerFactory.getLogger(DemoEditor.class);
  static final Logger status = LoggerFactory.getLogger("status"); //$NON-NLS-1$

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
        JOptionPane.showMessageDialog(view, Messages.getString("DemoEditor.noDemoFileSelected"),//$NON-NLS-1$
            Messages.getString("DemoEditor.noDemoFileSelectedTitle"), //$NON-NLS-1$
            JOptionPane.WARNING_MESSAGE);
        return;
      }
      Demo demo = demoModel.getDemo(row);
      Path demoPath = demo.getPath();
      if (!Files.exists(demoPath)) {
        JOptionPane.showMessageDialog(view, Messages.getString("DemoEditor.invalidDemoFile"),//$NON-NLS-1$
            Messages.getString("DemoEditor.invalidDemoFileError"), //$NON-NLS-1$
            JOptionPane.ERROR_MESSAGE);
        return;
      }
      // attempt to get a demo path relative to game path
      try {
        demoPath = toPath(Key.gamePath.getValue(settings)).relativize(demoPath);
      } catch (IllegalArgumentException ex) {
        log.warn("Demos folder is not relative to game folder. VDM files created might not work anymore"); //$NON-NLS-1$
      }
      try {
        int tick1 = Integer.parseInt(view.getStartTickTextField().getText());
        int tick2 = Integer.parseInt(view.getEndTickTextField().getText());
        Tick segment = new Tick(demoPath.toString(), tick1, tick2);
        if (demo.getTickNumber() > 0 && tick2 > demo.getTickNumber()) {
          throw new IllegalArgumentException(Messages.getString("DemoEditor.endTickTooHighError")); //$NON-NLS-1$
        }
        tickModel.addTick(segment);
        log.info("Adding segment: {}", segment); //$NON-NLS-1$
      } catch (IllegalArgumentException ex) {
        JOptionPane.showMessageDialog(view,
            Messages.getString("DemoEditor.invalidTickFieldValues") + '\n' + ex.getMessage(),//$NON-NLS-1$
            Messages.getString("DemoEditor.invalidTickFieldValuesTitle"), //$NON-NLS-1$ 
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
          status.info(Messages.getString("DemoEditor.vdmGeneratedNotice"), paths.size(), //$NON-NLS-1$
              (paths.size() == 1 ? "" : "s")); //$NON-NLS-1$ //$NON-NLS-2$
          new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
              osInterface.openFolder(paths.get(0));
              return null;
            }
          }.execute();

        } catch (IOException e1) {
          log.warn("A problem occurred while generating the VDM: {}", e1); //$NON-NLS-1$
          status.info(Messages.getString("DemoEditor.vdmGenerationException")); //$NON-NLS-1$
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
      try (DirectoryStream<Path> stream =
          Files.newDirectoryStream(toPath(Key.gamePath.getValue(settings)), "*.vdm")) { //$NON-NLS-1$

        for (Path path : stream) {
          if (isCancelled()) {
            break;
          }
          path.toFile().setWritable(true);
          Files.delete(path);
          publish(path);
        }

      } catch (IOException ex) {
        log.warn("Problem while deleting VDM files", ex); //$NON-NLS-1$
      }

      return null;
    }

    @Override
    protected void process(List<Path> chunks) {
      count += chunks.size();
      status.info(Messages.getString("DemoEditor.deletingVdmFilesNotice"),//$NON-NLS-1$
          count, (count == 1 ? "" : "s")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected void done() {
      if (!isCancelled()) {
        if (count > 0) {
          String str = String.format(Messages.getString("DemoEditor.vdmClearResultNotice"),//$NON-NLS-1$
              count, count == 1 ? "" : "s"); //$NON-NLS-1$//$NON-NLS-2$
          log.info(str);
          status.info(str);
        } else {
          log.info("No VDM files were deleted"); //$NON-NLS-1$
          status.info(""); //$NON-NLS-1$
        }
        view.getDeleteVdmFilesButton().setEnabled(true);
      }
    }

  }

  DemoEditorView view;
  final TickTableModel tickModel = new TickTableModel();
  DemoTableModel demoModel;
  Settings settings;
  OSInterface osInterface;
  final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public DemoEditor(Settings settings, OSInterface osInterface) {
    this.settings = settings;
    this.osInterface = osInterface;
    Path demoPath = toPath(Key.demosPath.getValue(settings));
    Path gamePath = toPath(Key.gamePath.getValue(settings));
    if (demoPath.toString().equals("")) { //$NON-NLS-1$
      demoPath = gamePath;
    } else if (!Files.exists(demoPath) && Files.exists(gamePath.resolve(demoPath))) {
      demoPath = gamePath.resolve(demoPath);
    }
    demoPath = demoPath.toAbsolutePath();
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
    TableRowSorter<TableModel> sorter = new TableRowSorter<>(view.getDemosTable().getModel());
    view.getDemosTable().setRowSorter(sorter);

    view.getAddButton().addActionListener(new AddSegment());
    view.getClearTickListButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        log.info("All VDM segments deleted"); //$NON-NLS-1$
        tickModel.clear();
      }
    });
    view.getCreateVdmFilesButton().addActionListener(new CreateVdmFiles());
    view.getDeleteVdmFilesButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        int answer =
            JOptionPane.showConfirmDialog(view,
                Messages.getString("DemoEditor.clearAllVdmConfirm"), //$NON-NLS-1$
                Messages.getString("DemoEditor.clearAllVdmConfirmTitle"), //$NON-NLS-1$
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
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
    view.getNoSkipToTickCheckBox().setSelected(Key.vdmNoSkipToTick.getValue(settings));
    view.getNoSkipToTickCheckBox().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        Key.vdmNoSkipToTick.setValue(settings, view.getNoSkipToTickCheckBox().isSelected());
      }
    });

    final DefaultComboBoxModel<String> defaultSegmentModel =
        new DefaultComboBoxModel<>(new String[] {Messages.getString("DemoEditor.recordSegment")}); //$NON-NLS-1$
    view.getSegmentTypeComboBox().setModel(defaultSegmentModel);
    view.getSegmentTypeComboBox().addItemListener(new ItemListener() {

      @Override
      public void itemStateChanged(ItemEvent e) {
        view.getStartTickTextField().setText(""); //$NON-NLS-1$
        view.getEndTickTextField().setText(""); //$NON-NLS-1$
        if (e.getStateChange() == ItemEvent.SELECTED) {
          String selected = e.getItem().toString();
          String[] tokens = selected.split(" at "); //$NON-NLS-1$
          if (tokens.length == 2) {
            try {
              // the -500 is a margin added to avoid dropping frames due to loading a specific tick
              view.getStartTickTextField().setText(Integer.parseInt(tokens[1]) - 500 + ""); //$NON-NLS-1$
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
              m.addElement(streak.getDescription() + " @ " + streak.getTick()); //$NON-NLS-1$
            }
            view.getSegmentTypeComboBox().setModel(m);
          }
        } else {
          view.getSegmentTypeComboBox().setModel(defaultSegmentModel);
        }
        view.getStartTickTextField().setText(""); //$NON-NLS-1$
        view.getEndTickTextField().setText(""); //$NON-NLS-1$
      }
    });
    view.getDemoFilterTextField().getDocument()
        .addDocumentListener(new RegexFilterDocumentListener());
    view.getSaveSegmentListButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        Frame frame = (Frame) SwingUtilities.getWindowAncestor(view);
        String file =
            osInterface.chooseSaveFile(frame, Messages
                .getString("DemoEditor.selectSegmentListOutputFolder"), //$NON-NLS-1$
                demoModel.getDemosPath().toString(), Arrays.asList(new ExtensionsFilter(Messages
                    .getString("DemoEditor.jsonFile"), Arrays.asList("json")))); //$NON-NLS-1$//$NON-NLS-2$
        if (file != null) {
          if (!file.endsWith(".json")) { //$NON-NLS-1$
            file = file + ".json"; //$NON-NLS-1$
          }
          List<Tick> list = tickModel.getTickList();
          Path dest = Paths.get(file);
          int answer = JOptionPane.YES_OPTION;
          if (Files.exists(dest)) {
            answer =
                JOptionPane.showConfirmDialog(
                    view,
                    String.format(
                        Messages.getString("DemoEditor.fileAlreadyExistsOverwriteConfirm"), //$NON-NLS-1$
                        dest.toString()),
                    Messages.getString("DemoEditor.fileAlreadyExistsOverwriteConfirmTitle"), //$NON-NLS-1$
                    JOptionPane.YES_NO_OPTION);
          }
          if (answer == JOptionPane.YES_OPTION) {
            try {
              log.info("Saving segment list to {}", file); //$NON-NLS-1$
              Files.write(dest,
                  Arrays.asList(gson.toJson(list, new TypeToken<List<Tick>>() {}.getType())),
                  Charset.forName("UTF-8")); //$NON-NLS-1$
              status.info(StatusAppender.OK,
                  Messages.getString("DemoEditor.segmentListSavedNotice"), file); //$NON-NLS-1$
            } catch (IOException ex) {
              log.warn("Could not save segment list: {}", e.toString()); //$NON-NLS-1$
              status.info(StatusAppender.WARN,
                  Messages.getString("DemoEditor.couldNotSaveSegmentListNotice")); //$NON-NLS-1$
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
            osInterface.chooseSingleFile(frame, Messages
                .getString("DemoEditor.selectSegmentListFilePromptTitle"), demoModel //$NON-NLS-1$
                .getDemosPath().toString(), Arrays.asList(new ExtensionsFilter(Messages
                .getString("DemoEditor.jsonFile"), Arrays //$NON-NLS-1$
                .asList("json")))); //$NON-NLS-1$
        int answer = JOptionPane.YES_OPTION;
        if (tickModel.getRowCount() > 0) {
          answer =
              JOptionPane.showConfirmDialog(
                  view,
                  Messages.getString("DemoEditor.segmentListLoadConfirm"), //$NON-NLS-1$
                  Messages.getString("DemoEditor.segmentListLoadConfirmTitle"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
        }
        if (answer == JOptionPane.YES_OPTION) {
          log.info("Loading segment list from {}", file); //$NON-NLS-1$
          try (Reader reader = Files.newBufferedReader(Paths.get(file), Charset.forName("UTF-8"))) { //$NON-NLS-1$
            List<Tick> list = gson.fromJson(reader, new TypeToken<List<Tick>>() {}.getType());
            tickModel.clear();
            for (Tick tick : list) {
              tickModel.addTick(tick);
            }
            status.info(StatusAppender.OK,
                Messages.getString("DemoEditor.segmentListLoadedNotice"), file); //$NON-NLS-1$
          } catch (IOException e1) {
            log.warn("Could not load segment list: {}", e1.toString()); //$NON-NLS-1$
            status.info(StatusAppender.WARN,
                Messages.getString("DemoEditor.segmentListLoadFailedNotice")); //$NON-NLS-1$
          }
        }
      }
    });
    view.getChangeDemoFolderButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        int answer =
            JOptionPane.showConfirmDialog(
                view,
                Messages.getString("DemoEditor.changeDemoFolderConfirm"), //$NON-NLS-1$
                Messages.getString("DemoEditor.changeDemoFolderConfirmTitle"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
        if (answer == JOptionPane.YES_OPTION) {
          Frame frame = (Frame) SwingUtilities.getWindowAncestor(view);
          String folder =
              osInterface.chooseSingleFolder(frame,
                  Messages.getString("DemoEditor.selectDemoFolderPromptTitle"), demoModel //$NON-NLS-1$
                      .getDemosPath().toString());
          if (folder != null) {
            Path selected = Paths.get(folder);
            log.info("Reloading demos from {}", selected); //$NON-NLS-1$
            demoModel.setDemosPath(selected);
            tickModel.clear();
            status.info(StatusAppender.OK,
                Messages.getString("DemoEditor.demoFolderLoadedNotice"), selected); //$NON-NLS-1$
          }
        }
      }
    });

    return view;
  }

  private String getDemoname() {
    if (!tickModel.getTickList().isEmpty()) {
      return tickModel.getTickList().get(0).getDemoname();
    }
    return ""; //$NON-NLS-1$
  }

  private boolean isAutoplay() {
    return view.getAutoplayFirstDemoCheckBox().isSelected();
  }

  public void writeAutoplay() {
    try {
      Path path = settings.getParentDataPath().resolve("config/cfg/lawena.cfg"); //$NON-NLS-1$
      String demoname = getDemoname();
      if (isAutoplay() && !demoname.equals("")) { //$NON-NLS-1$
        Files.write(path, Arrays.asList("playdemo \"" + demoname + "\""), //$NON-NLS-1$ //$NON-NLS-2$
            Charset.forName("UTF-8")); //$NON-NLS-1$
      } else {
        Files.deleteIfExists(path);
      }
    } catch (IOException e) {
      log.warn("Could not write autoplay data to file: " + e); //$NON-NLS-1$
    }
  }
}
