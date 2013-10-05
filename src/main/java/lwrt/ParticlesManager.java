
package lwrt;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import ui.ParticlesDialog;

public class ParticlesManager {

    private static final Logger log = Logger.getLogger("lawena");

    private ParticlesDialog view;
    private SettingsManager settings;
    private CommandLine cl;

    public ParticlesManager(SettingsManager settings, CommandLine cl) {
        this.settings = settings;
        this.cl = cl;
    }

    public void showDialog() {
        if (view == null) {
            view = new ParticlesDialog();
            view.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            view.setModalityType(ModalityType.APPLICATION_MODAL);
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
            final JTable tableParticles = view.getTableParticles();
            view.getOkButton().addActionListener(new ActionListener() {

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
                    view.setVisible(false);
                }
            });
            view.getCancelButton().addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    List<String> selected = settings.getParticles();
                    boolean selectAll = selected.contains("*");
                    for (int i = 0; i < tableParticles.getRowCount(); i++) {
                        tableParticles.setValueAt(
                                selectAll || selected.contains(tableParticles.getValueAt(i, 1)), i,
                                0);
                    }
                    log.finer("Particles: " + selected);
                    view.setVisible(false);
                }
            });
            tableParticles.setModel(dtm);
            tableParticles.getColumnModel().getColumn(0).setMaxWidth(20);
            List<String> selected = settings.getParticles();
            boolean selectAll = selected.contains("*");
            for (String particle : cl.getVpkContents(settings.getTfPath(),
                    CustomPathList.particles.getPath())) {
                dtm.addRow(new Object[] {
                        selectAll || selected.contains(particle), particle
                });
            }
        }
        view.setVisible(true);
    }

}
