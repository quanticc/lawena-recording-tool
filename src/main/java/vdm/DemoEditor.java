package vdm;

import lwrt.CommandLine;
import lwrt.SettingsManager;
import lwrt.SettingsManager.Key;
import ui.DemoEditorView;
import util.DemoPreview;
import vdm.Tick.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumn;
import java.awt.*;
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

public class DemoEditor {

	private static final Logger log = Logger.getLogger("lawena");
	private static final Logger status = Logger.getLogger("status");
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
		choosedemo.setFileFilter(new FileNameExtensionFilter("Demo files", "DEM"));
		choosedemo.setCurrentDirectory(settings.getTfPath().toFile());
	}

	private void updateDemoDetails() {
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
        model = new TickTableModel(view);

		view.getTableTicks().setModel(model);
		view.getTableTicks().setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		view.getTableTicks().setFillsViewportHeight(true);

		TableColumn typeColumn =
				view.getTableTicks().getColumnModel().getColumn(TickTableModel.Column.TYPE.ordinal());
		JComboBox<String> segmentTypes = new JComboBox<>();
		segmentTypes.setEditable(false);
		segmentTypes.addItem(Record.Segment);
		segmentTypes.addItem(ExecRecord.Segment);
		segmentTypes.addItem(Exec.Segment);
		typeColumn.setCellEditor(new DefaultCellEditor(segmentTypes));

		TableColumn templateColumn =
				view.getTableTicks().getColumnModel().getColumn(TickTableModel.Column.TEMPLATE.ordinal());
		JComboBox<String> templateTypes = new JComboBox<>();
		templateTypes.setEditable(true);
		templateTypes.addItem(ExecRecord.Template);
		templateTypes.addItem(Exec.Template);
		templateTypes.addItem(Exec.QuitTemplate);
		templateColumn.setCellEditor(new DefaultCellEditor(templateTypes));

		view.getBtnAdd().addActionListener(new VdmAddTick(Record.Segment));
		view.getBtnAddExecRecord().addActionListener(
				new VdmAddTick(ExecRecord.Segment));
		view.getBtnAddExec().addActionListener(
		    new VdmAddTick(Exec.Segment));
		view.getBtnBrowse().addActionListener(new VdmBrowseDemo());
		view.getBtnClearTickList().addActionListener(new VdmClearTicks());
		view.getBtnCreateVdmFiles().addActionListener(new VdmCreateFile());
		view.getBtnDeleteVdmFiles().addActionListener(e -> {
			int answer =
					JOptionPane
							.showConfirmDialog(
									view,
									"Are you sure you want to clear all .vdm files in your TF2 and current demo " +
											"folder?",
									"Clear VDM Files", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (answer == JOptionPane.YES_OPTION) {
				new ClearVdmFilesTask().execute();
			}
		});
		view.getBtnDeleteSelectedTick().addActionListener(e -> {
		    if (view.getTableTicks().isEditing()) {
                view.getTableTicks().getCellEditor().stopCellEditing();
            }
			int numRows = view.getTableTicks().getSelectedRowCount();
			for (int i = 0; i < numRows; i++) {
				model.removeTick(view.getTableTicks().getSelectedRow());
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
		view.getCmbSkipMode().addActionListener(e -> settings.setString(Key.VdmSkipMode,
				((SkipMode) view.getCmbSkipMode().getSelectedItem()).name()));

		return view;
	}

	public class VdmAddTick implements ActionListener {

	    private final String segment;

		public VdmAddTick(String segment) {
		    this.segment = segment;
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
                Tick segment = TickFactory.makeTick(currentDemoFile, settings.getTfPath().relativize(currentDemoFile.toPath())
                    .toString(), tick1, tick2, this.segment);
                if(segment.isValid()) {
                    model.addTick(segment);
                    log.info("Adding segment: " + segment);
                } else {
                    JOptionPane.showMessageDialog(view,
                        segment.getReason(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
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
		    if (view.getTableTicks().isEditing()) {
                view.getTableTicks().getCellEditor().stopCellEditing();
            }
			model.clear();
		}

	}

	public class VdmCreateFile implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (model.getRowCount() > 0) {
			    List <Tick> tl = model.getTickList();
                for(int i = 0; i < tl.size(); i++) {
                    Tick t = tl.get(i);
                    if (!t.isValid()) {
                        JOptionPane.showMessageDialog(view,
                            "Please correct invalid tick in row " + (i + 1) + ". (" + t.getReason() + ")", "Error",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                vdmgenerator = new VDMGenerator(tl, settings);
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
			SwingUtilities.invokeAndWait(() -> view.getBtnDeleteVdmFiles().setEnabled(false));
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

}
