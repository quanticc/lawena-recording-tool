
package vdm;

import ui.DemoEditorView;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

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
            }

            try {
                int tick1 = Integer.parseInt(view.getTxtStarttick().getText());
                int tick2 = Integer.parseInt(view.getTxtEndtick().getText());
                if (tick1 >= tick2) {
                    throw new NumberFormatException();
                }

                Object[] row = {
                        currentdemo, tick1, tick2
                };

                model.insertRow(view.getTableTicks().getRowCount(), row);
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
                view.getTxtDemofile().setText(currentdemo);
            }
        }

    }

    public class VdmClearTicks implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            while (model.getRowCount() > 0) {
                model.removeRow(0);
            }
        }

    }

    public class VdmCreateFile implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            ticklist = generateTickList(0);
            vdmgenerator = new VDMGenerator(ticklist, settings.getTfPath().toString());

            try {
                vdmgenerator.generate();
            } catch (IOException e1) {
                log.warning("A problem occurred while generating the VDM: " + e1);
            }
        }

        private TickList generateTickList(int i) {
            TickList current;

            current = new TickList((String) model.getValueAt(i, 0), Integer.parseInt((String) model
                    .getValueAt(i, 1)), Integer.parseInt((String) model.getValueAt(i, 2)));

            if (i + 1 == view.getTableTicks().getRowCount())
                return current;

            current.setNext(generateTickList(i + 1));

            return current;
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
            status.info("Deleting " + count + " VDM files from TF2 folder...");
        };

        @Override
        protected void done() {
            if (!isCancelled()) {
                if (count > 0) {
                    log.fine("VDM files cleared: " + count + " files deleted");
                } else {
                    log.fine("No VDM files were deleted");
                }
                view.getBtnDeleteVdmFiles().setEnabled(true);
                status.info("");
            }
        };

    }

    private DemoEditorView view;
    private JFileChooser choosedemo = new JFileChooser();
    private DefaultTableModel model;
    private SettingsManager settings;
    private String currentdemo;
    private TickList ticklist;
    private VDMGenerator vdmgenerator;

    public DemoEditor(SettingsManager settings) {
        this.settings = settings;
        choosedemo.setDialogTitle("Choose a demo file");
        choosedemo.setFileSelectionMode(JFileChooser.FILES_ONLY);
        choosedemo.setFileFilter(new FileNameExtensionFilter("Demo files", new String[] {
                "DEM"
        }));
        choosedemo.setCurrentDirectory(settings.getTfPath().toFile());
        Object[][] tickdata = {};
        String[] columnames = {
                "Demo name", "Starting Tick", "Ending Tick"
        };
        model = new DefaultTableModel(tickdata, columnames);
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

        return view;
    }

}
