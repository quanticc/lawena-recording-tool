
package com.github.iabarca.lwrt.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

public class ParticlesView extends JPanel {

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

    private JTable tableParticles;

    /**
     * Create the dialog.
     */
    public ParticlesView() {
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[] {
                0, 0, 0, 0
        };
        gbl_contentPanel.rowHeights = new int[] {
                0, 0, 0, 0
        };
        gbl_contentPanel.columnWeights = new double[] {
                0.0, 0.0, 1.0, Double.MIN_VALUE
        };
        gbl_contentPanel.rowWeights = new double[] {
                0.0, 1.0, 0.0, Double.MIN_VALUE
        };
        setLayout(gbl_contentPanel);
        {
            JLabel lblSelectWhatEnhanced = new JLabel(
                    "<html>Select what enhanced particles are being copied to your tf/custom folder");
            GridBagConstraints gbc_lblSelectWhatEnhanced = new GridBagConstraints();
            gbc_lblSelectWhatEnhanced.fill = GridBagConstraints.HORIZONTAL;
            gbc_lblSelectWhatEnhanced.gridwidth = 3;
            gbc_lblSelectWhatEnhanced.insets = new Insets(5, 5, 5, 5);
            gbc_lblSelectWhatEnhanced.gridx = 0;
            gbc_lblSelectWhatEnhanced.gridy = 0;
            add(lblSelectWhatEnhanced, gbc_lblSelectWhatEnhanced);
        }
        {
            JScrollPane scrollPane = new JScrollPane();
            GridBagConstraints gbc_scrollPane = new GridBagConstraints();
            gbc_scrollPane.gridwidth = 3;
            gbc_scrollPane.insets = new Insets(0, 5, 5, 5);
            gbc_scrollPane.fill = GridBagConstraints.BOTH;
            gbc_scrollPane.gridx = 0;
            gbc_scrollPane.gridy = 1;
            add(scrollPane, gbc_scrollPane);
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
            JButton btnSelectAll = new JButton("Select All");
            GridBagConstraints gbc_btnSelectAll = new GridBagConstraints();
            gbc_btnSelectAll.fill = GridBagConstraints.HORIZONTAL;
            gbc_btnSelectAll.insets = new Insets(0, 5, 5, 5);
            gbc_btnSelectAll.gridx = 0;
            gbc_btnSelectAll.gridy = 2;
            add(btnSelectAll, gbc_btnSelectAll);
            btnSelectAll.addActionListener(new BtnSelectAllActionListener());
            btnSelectAll.setPreferredSize(new Dimension(65, 23));
        }
        {
            JButton btnSelectNone = new JButton("Select None");
            GridBagConstraints gbc_btnSelectNone = new GridBagConstraints();
            gbc_btnSelectNone.fill = GridBagConstraints.HORIZONTAL;
            gbc_btnSelectNone.insets = new Insets(0, 0, 5, 5);
            gbc_btnSelectNone.gridx = 1;
            gbc_btnSelectNone.gridy = 2;
            add(btnSelectNone, gbc_btnSelectNone);
            btnSelectNone.addActionListener(new BtnSelectNoneActionListener());
            btnSelectNone.setPreferredSize(new Dimension(65, 23));
        }
    }

    public JTable getTableParticles() {
        return tableParticles;
    }

}
