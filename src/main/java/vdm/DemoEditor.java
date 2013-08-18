
package vdm;

import ui.DemoEditorView;
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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import lwrt.CommandLine;
import lwrt.SettingsManager;

public class DemoEditor {

    private static final Logger log = Logger.getLogger("lawena");
    private static final Logger status = Logger.getLogger("status");

    public class VdmAddTick implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
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
                updateDemoDetails();
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

    public class VdmClearTicks implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            tickModel.clear();
        }

    }

    public class VdmCreateFile implements ActionListener {

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
    private String currentdemo;
    private DemoTableModel demoModel;

    public DemoEditor(SettingsManager settings, CommandLine cl, WatchDir watcher) {
        this.settings = settings;
        this.cl = cl;
        demoModel = new DemoTableModel(settings.getTfPath());
        try {
            watcher.register(settings.getTfPath(), new WatchAction() {

                @Override
                public void entryModified(Path child) {
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

        // choosedemo.setDialogTitle("Choose a demo file");
        // choosedemo.setFileSelectionMode(JFileChooser.FILES_ONLY);
        // choosedemo.setFileFilter(new FileNameExtensionFilter("Demo files",
        // new String[] {
        // "DEM"
        // }));
        // choosedemo.setCurrentDirectory(settings.getTfPath().toFile());

        tickModel = new TickTableModel();
    }

    public void updateDemoDetails() {
        new SwingWorker<String, Void>() {

            @Override
            protected String doInBackground() throws Exception {
                try (Demo dp = new Demo(settings.getTfPath().resolve(currentdemo))) {
                    return dp.toString();
                }
            }

            protected void done() {
                // try {
                // view.getTxtrDemodetails().setText(get());
                // } catch (InterruptedException | ExecutionException e) {
                // view.getTxtrDemodetails()
                // .setText("Could not retrieve demo details");
                // }
            };

        }.execute();
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
        view.getTableDemos().getColumnModel().getColumn(1).setPreferredWidth(100);
        view.getTableDemos().getColumnModel().getColumn(2).setPreferredWidth(100);
        view.getTableDemos().getColumnModel().getColumn(3).setPreferredWidth(75);
        view.getTableDemos().getColumnModel().getColumn(4).setPreferredWidth(75);
        view.getTableDemos().getColumnModel().getColumn(5).setPreferredWidth(150);
        view.getTableDemos().setFillsViewportHeight(true);

        view.getBtnAdd().addActionListener(new VdmAddTick());
        // view.getBtnBrowse().addActionListener(new VdmBrowseDemo());
        view.getBtnClearTickList().addActionListener(new VdmClearTicks());
        view.getBtnCreateVdmFiles().addActionListener(new VdmCreateFile());
        view.getBtnDeleteVdmFiles().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int answer = JOptionPane.showConfirmDialog(view,
                        "Are you sure you want to clear all .vdm files in your TF2 folder?",
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
        view.getCmbSegmentType().setModel(new DefaultComboBoxModel<>(new String[] {
                "Custom Segment"
        }));
        view.getCmbSegmentType().addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(ItemEvent e) {
                // TODO Auto-generated method stub
                
            }
        });
        view.getTableDemos().getSelectionModel()
                .addListSelectionListener(new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        log.finer(e.toString());
                        if (e.getFirstIndex() >= 0 && !e.getValueIsAdjusting()) {
                            Demo demo = (Demo) demoModel.getDemo(e.getFirstIndex());
                            List<KillStreak> streaks = demo.getStreaks();
                            DefaultComboBoxModel m = new DefaultComboBoxModel<>();
                            m.addElement("Custom Segment");
                            for (KillStreak streak : streaks) {
                                log.finer("adding element: " + streak.getDescription() + " at " + streak.getTick());
                                m.addElement(streak.getDescription() + " at " + streak.getTick());
                            }
                            view.getCmbSegmentType().setModel(m);
                        }
                    }
                });

        return view;
    }
}
