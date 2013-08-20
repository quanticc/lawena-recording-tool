
package vdm;

import ui.DemoEditorView;
import ui.DemoRenderer;
import util.WatchDir;
import util.WatchDir.WatchAction;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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

import lwrt.CommandLine;
import lwrt.SettingsManager;

public class DemoEditor {

    private static final Logger log = Logger.getLogger("lawena");
    private static final Logger status = Logger.getLogger("status");

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
            TableRowSorter<? extends TableModel> sorter = (TableRowSorter<? extends TableModel>) view
                    .getTableDemos().getRowSorter();
            RowFilter<TableModel, Object> rf = null;
            String input = view.getTxtFilterDemos().getText();
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
            String currentdemo;
            if (!Files.exists(settings.getTfPath().resolve(
                    demoModel.getDemo(view.getTableDemos().getSelectedRow()).getPath()
                            .getFileName().toString()))) {
                JOptionPane.showMessageDialog(view,
                        "Please fill the required demo file field with a valid demo file", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                currentdemo = demoModel.getDemo(view.getTableDemos().getSelectedRow()).getPath()
                        .getFileName().toString();
            }

            try {
                int tick1 = Integer.parseInt(view.getTxtStarttick().getText());
                int tick2 = Integer.parseInt(view.getTxtEndtick().getText());
                if (tick1 >= tick2) {
                    throw new NumberFormatException();
                }
                tickModel.addTick(new Tick(currentdemo, tick1, tick2));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(view,
                        "Please fill the required tick fields with valid numbers", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    public class ClearSegments implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            tickModel.clear();
        }

    }

    public class CreateVdmFiles implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (tickModel.getRowCount() > 0) {
                try {
                    final List<Path> paths = new VDMGenerator(tickModel.getTickList(), settings)
                            .generate();
                    status.info("VDM generated: " + paths.size()
                            + (paths.size() == 1 ? " new file" : " new files")
                            + " in TF2 directory");
                    new SwingWorker<Void, Void>() {
                        protected Void doInBackground() throws Exception {
                            cl.openFolder(paths.get(0));
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

    public class DeleteVdmFiles extends SwingWorker<Void, Path> {

        private int count = 0;

        @Override
        protected Void doInBackground() throws Exception {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    view.getBtnDeleteVdmFiles().setEnabled(false);
                }
            });
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(settings.getTfPath(),
                    "*.vdm")) {

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
                    String str = "VDM files cleared: " + count
                            + (count == 1 ? " file " : " files ")
                            + "deleted";
                    log.fine(str);
                    status.info(str);
                } else {
                    log.fine("No VDM files were deleted");
                    status.info("");
                }
                view.getBtnDeleteVdmFiles().setEnabled(true);
            }
        };

    }

    private DemoEditorView view;
    private TickTableModel tickModel;
    private SettingsManager settings;
    private CommandLine cl;
    private DemoTableModel demoModel;

    public DemoEditor(SettingsManager settings, CommandLine cl, WatchDir watcher) {
        this.settings = settings;
        this.cl = cl;
        demoModel = new DemoTableModel(settings.getTfPath());
        try {
            watcher.register(settings.getTfPath(), new WatchAction() {

                @Override
                public void entryModified(Path child) {
                    // TODO: handle case when user renames .dem file
                }

                @Override
                public void entryDeleted(Path child) {
                    if (child.endsWith(".dem")) {
                        demoModel.removeDemo(child);
                    }
                }

                @Override
                public void entryCreated(Path child) {
                    if (child.endsWith(".dem")) {
                        demoModel.addDemo(child);
                    }
                }
            });
        } catch (IOException e) {
            log.log(Level.INFO, "", e);
        }

        tickModel = new TickTableModel();
    }

    public Component start() {
        view = new DemoEditorView();

        view.getTableTicks().setModel(tickModel);
        view.getTableTicks().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int vColIndex = 0;
        TableColumn col = view.getTableTicks().getColumnModel().getColumn(vColIndex);
        int columnwidth = 400;
        col.setPreferredWidth(columnwidth);
        view.getTableTicks().setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        view.getTableTicks().setFillsViewportHeight(true);

        view.getTableDemos().setModel(demoModel);
        view.getTableDemos().getSelectionModel()
                .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        view.getTableDemos().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        view.getTableDemos().getColumnModel().getColumn(0).setPreferredWidth(300);
        view.getTableDemos().getColumnModel().getColumn(1).setPreferredWidth(125);
        view.getTableDemos().getColumnModel().getColumn(2).setPreferredWidth(100);
        view.getTableDemos().getColumnModel().getColumn(3).setPreferredWidth(75);
        view.getTableDemos().getColumnModel().getColumn(4).setPreferredWidth(75);
        view.getTableDemos().getColumnModel().getColumn(5).setPreferredWidth(150);
        view.getTableDemos().getColumnModel().getColumn(6).setPreferredWidth(300);
        view.getTableDemos().setFillsViewportHeight(true);
        view.getTableDemos().setDefaultRenderer(Demo.class, new DemoRenderer());
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(view.getTableDemos()
                .getModel());
        view.getTableDemos().setRowSorter(sorter);

        view.getBtnAdd().addActionListener(new AddSegment());
        view.getBtnClearTickList().addActionListener(new ClearSegments());
        view.getBtnCreateVdmFiles().addActionListener(new CreateVdmFiles());
        view.getBtnDeleteVdmFiles().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int answer = JOptionPane.showConfirmDialog(view,
                        "Are you sure you want to clear all .vdm files in your TF2 folder?",
                        "Clear VDM Files", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (answer == JOptionPane.YES_OPTION) {
                    new DeleteVdmFiles().execute();
                }
            }
        });
        view.getBtnDeleteSelectedTick().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int numRows = view.getTableTicks().getSelectedRowCount();
                for (int i = 0; i < numRows; i++) {
                    tickModel.removeTick(view.getTableTicks().getSelectedRow());
                }
            }
        });
        view.getChckbxSrcDemoFix().setSelected(settings.getVdmSrcDemoFix());
        view.getChckbxSrcDemoFix().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                settings.setVdmSrcDemoFix(view.getChckbxSrcDemoFix().isSelected());
            }
        });
        final DefaultComboBoxModel<String> defaultSegmentModel = new DefaultComboBoxModel<>(
                new String[] {
                        "Record Segment", "Go to Segment"
                });
        view.getCmbSegmentType().setModel(defaultSegmentModel);
        view.getCmbSegmentType().addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                view.getTxtStarttick().setText("");
                view.getTxtEndtick().setText("");
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String[] tokens = e.getItem().toString().split(" at ");
                    if (tokens.length == 2) {
                        try {
                            view.getTxtStarttick().setText(Integer.parseInt(tokens[1]) - 500 + "");
                        } catch (NumberFormatException x) {
                        }
                    }
                }
            }
        });
        view.getTableDemos().getSelectionModel()
                .addListSelectionListener(new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        int index = view.getTableDemos().getSelectedRow();
                        if (index >= 0) {
                            if (!e.getValueIsAdjusting()) {
                                Demo demo = (Demo) demoModel.getDemo(view.getTableDemos()
                                        .convertRowIndexToModel(index));
                                List<KillStreak> streaks = demo.getStreaks();
                                DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();
                                m.addElement("Record Segment");
                                m.addElement("Go to Segment");
                                for (KillStreak streak : streaks) {
                                    m.addElement(streak.getDescription() + " at "
                                            + streak.getTick());
                                }
                                view.getCmbSegmentType().setModel(m);
                            }
                        } else {
                            view.getCmbSegmentType().setModel(defaultSegmentModel);
                        }
                        view.getTxtStarttick().setText("");
                        view.getTxtEndtick().setText("");
                    }
                });
        view.getTxtFilterDemos().getDocument()
                .addDocumentListener(new RegexFilterDocumentListener());

        return view;
    }

    public String getDemoname() {
        if (!tickModel.getTickList().isEmpty()) {
            return tickModel.getTickList().get(0).getDemoname();
        }
        return "";
    }

    public boolean isAutoplay() {
        return view.getChckbxAutoplayFirstDemo().isSelected();
    }
}
