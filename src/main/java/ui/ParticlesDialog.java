
package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

public class ParticlesDialog extends JDialog {

    private class BtnSelectNoneActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < tableParticles.getRowCount(); i++) {
                tableParticles.setValueAt(false, i, 0);
            }
        }
    }

    private class BtnSelectAllActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < tableParticles.getRowCount(); i++) {
                tableParticles.setValueAt(true, i, 0);
            }
        }
    }

    private static final long serialVersionUID = 1L;

    private final JPanel contentPanel = new JPanel();
    private JTable tableParticles;
    private JButton okButton;
    private JButton cancelButton;

    /**
     * Create the dialog.
     */
    public ParticlesDialog() {
        setTitle("Select Enhanced Particles");
        setModalityType(ModalityType.APPLICATION_MODAL);
        setBounds(100, 100, 450, 300);
        BorderLayout borderLayout = new BorderLayout();
        getContentPane().setLayout(borderLayout);
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[] {
                0, 0
        };
        gbl_contentPanel.rowHeights = new int[] {
                0, 0, 0
        };
        gbl_contentPanel.columnWeights = new double[] {
                1.0, Double.MIN_VALUE
        };
        gbl_contentPanel.rowWeights = new double[] {
                0.0, 1.0, Double.MIN_VALUE
        };
        contentPanel.setLayout(gbl_contentPanel);
        {
            JLabel lblSelectWhatEnhanced = new JLabel(
                    "Select what enhanced particles are being copied to your tf/custom folder.");
            GridBagConstraints gbc_lblSelectWhatEnhanced = new GridBagConstraints();
            gbc_lblSelectWhatEnhanced.anchor = GridBagConstraints.WEST;
            gbc_lblSelectWhatEnhanced.insets = new Insets(0, 0, 5, 0);
            gbc_lblSelectWhatEnhanced.gridx = 0;
            gbc_lblSelectWhatEnhanced.gridy = 0;
            contentPanel.add(lblSelectWhatEnhanced, gbc_lblSelectWhatEnhanced);
        }
        {
            JScrollPane scrollPane = new JScrollPane();
            GridBagConstraints gbc_scrollPane = new GridBagConstraints();
            gbc_scrollPane.fill = GridBagConstraints.BOTH;
            gbc_scrollPane.gridx = 0;
            gbc_scrollPane.gridy = 1;
            contentPanel.add(scrollPane, gbc_scrollPane);
            {
                tableParticles = new JTable();
                tableParticles.setShowVerticalLines(false);
                tableParticles.setGridColor(new Color(0, 0, 0, 30));
                tableParticles.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
                tableParticles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                tableParticles.getTableHeader().setReorderingAllowed(false);
                scrollPane.setViewportView(tableParticles);
            }
        }
        {
            JPanel buttonPane = new JPanel();
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            GridBagLayout gbl_buttonPane = new GridBagLayout();
            gbl_buttonPane.columnWidths = new int[] {
                    0, 0, 0, 47, 65, 0
            };
            gbl_buttonPane.rowHeights = new int[] {
                    23, 0
            };
            gbl_buttonPane.columnWeights = new double[] {
                    0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE
            };
            gbl_buttonPane.rowWeights = new double[] {
                    0.0, Double.MIN_VALUE
            };
            buttonPane.setLayout(gbl_buttonPane);
            {
                JButton btnSelectAll = new JButton("All");
                btnSelectAll.addActionListener(new BtnSelectAllActionListener());
                btnSelectAll.setPreferredSize(new Dimension(65, 23));
                GridBagConstraints gbc_btnSelectAll = new GridBagConstraints();
                gbc_btnSelectAll.anchor = GridBagConstraints.WEST;
                gbc_btnSelectAll.insets = new Insets(0, 5, 5, 5);
                gbc_btnSelectAll.gridx = 0;
                gbc_btnSelectAll.gridy = 0;
                buttonPane.add(btnSelectAll, gbc_btnSelectAll);
            }
            {
                JButton btnSelectNone = new JButton("None");
                btnSelectNone.addActionListener(new BtnSelectNoneActionListener());
                btnSelectNone.setPreferredSize(new Dimension(65, 23));
                GridBagConstraints gbc_btnSelectNone = new GridBagConstraints();
                gbc_btnSelectNone.anchor = GridBagConstraints.WEST;
                gbc_btnSelectNone.insets = new Insets(0, 0, 5, 5);
                gbc_btnSelectNone.gridx = 1;
                gbc_btnSelectNone.gridy = 0;
                buttonPane.add(btnSelectNone, gbc_btnSelectNone);
            }
            {
                okButton = new JButton("OK");
                okButton.setPreferredSize(new Dimension(65, 23));
                GridBagConstraints gbc_okButton = new GridBagConstraints();
                gbc_okButton.anchor = GridBagConstraints.NORTHWEST;
                gbc_okButton.insets = new Insets(0, 0, 5, 5);
                gbc_okButton.gridx = 3;
                gbc_okButton.gridy = 0;
                buttonPane.add(okButton, gbc_okButton);
            }
            {
                cancelButton = new JButton("Cancel");
                cancelButton.setActionCommand("Cancel");
                GridBagConstraints gbc_cancelButton = new GridBagConstraints();
                gbc_cancelButton.anchor = GridBagConstraints.NORTHWEST;
                gbc_cancelButton.insets = new Insets(0, 0, 5, 5);
                gbc_cancelButton.gridx = 4;
                gbc_cancelButton.gridy = 0;
                buttonPane.add(cancelButton, gbc_cancelButton);
            }
        }
    }

    public JTable getTableParticles() {
        return tableParticles;
    }

    public JButton getOkButton() {
        return okButton;
    }
    public JButton getCancelButton() {
        return cancelButton;
    }
}
