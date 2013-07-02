
package vdm;

import ui.DemoEditorView;
import util.DemoPreview;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumn;

import lwrt.CommandLine;
import lwrt.SettingsManager;

public class DemoEditor {

    private static final Logger log = Logger.getLogger("lawena");
    private static final Logger status = Logger.getLogger("status");

    public class VdmAddTick implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!Files.exists(settings.getTfPath().resolve(view.getTxtDemofile().getText()))) {
                JOptionPane.showMessageDialog(view,
                        "Please fill the required demo file field with a valid demo file", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                currentdemo = view.getTxtDemofile().getText();
                updateDemoDetails();
            }

            try {
                int tick1 = Integer.parseInt(view.getTxtStarttick().getText());
                int tick2 = Integer.parseInt(view.getTxtEndtick().getText());
                if (tick1 >= tick2) {
                    throw new NumberFormatException();
                }
                model.addTick(new Tick(currentdemo, tick1, tick2));
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
                currentdemo = choosedemo.getSelectedFile().getName();
                if (Files.exists(choosedemo.getSelectedFile().toPath())) {
                    view.getTxtDemofile().setText(currentdemo);
                    updateDemoDetails();
                } else {
                    JOptionPane.showMessageDialog(view, "The selected file does not exist.",
                            "Browse", JOptionPane.INFORMATION_MESSAGE);
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
                vdmgenerator = new VDMGenerator(model.getTickList(), settings.getTfPath());

                try {
                    List<Path> paths = vdmgenerator.generate();
                    status.info("VDM generated: " + paths.size()
                            + (paths.size() == 1 ? " new file" : " new files")
                            + " in TF2 directory");
                    cl.openFolder(paths.get(0));
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
    private JFileChooser choosedemo = new JFileChooser();
    private TickTableModel model;
    private SettingsManager settings;
    private CommandLine cl;
    private String currentdemo;
    private VDMGenerator vdmgenerator;

    public DemoEditor(SettingsManager settings, CommandLine cl) {
        this.settings = settings;
        this.cl = cl;
        choosedemo.setDialogTitle("Choose a demo file");
        choosedemo.setFileSelectionMode(JFileChooser.FILES_ONLY);
        choosedemo.setFileFilter(new FileNameExtensionFilter("Demo files", new String[] {
                "DEM"
        }));
        choosedemo.setCurrentDirectory(settings.getTfPath().toFile());

        model = new TickTableModel();
    }

    public void updateDemoDetails() {
        new SwingWorker<String, Void>() {

            @Override
            protected String doInBackground() throws Exception {
                try (DemoPreview dp = new DemoPreview(settings.getTfPath().resolve(currentdemo))) {
                    return dp.toString();
                }
            }

            protected void done() {
                try {
                    view.getTxtrDemodetails().setText(get());
                } catch (InterruptedException | ExecutionException e) {
                    view.getTxtrDemodetails()
                            .setText("Could not retrieve demo details");
                }
            };

        }.execute();
    }

    public Component start() {
        view = new DemoEditorView();

        view.getTableTicks().setModel(model);
        view.getTableTicks().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int vColIndex = 0;
        TableColumn col = view.getTableTicks().getColumnModel().getColumn(vColIndex);
        int columnwidth = 400;
        col.setPreferredWidth(columnwidth);
        view.getTableTicks().setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        view.getTableTicks().setFillsViewportHeight(true);

        view.getBtnAdd().addActionListener(new VdmAddTick());
        view.getBtnBrowse().addActionListener(new VdmBrowseDemo());
        view.getBtnClearTickList().addActionListener(new VdmClearTicks());
        view.getBtnCreateVdmFiles().addActionListener(new VdmCreateFile());
        view.getBtnDeleteVdmFiles().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new ClearVdmFilesTask().execute();
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

        return view;
    }

}
