
package ui;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class DemoEditorView extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JLabel lblSelectDemoFile;
    private JTextField txtDemofile;
    private JButton btnBrowse;
    private JLabel lblStartTick;
    private JTextField txtStarttick;
    private JLabel lblEndTick;
    private JTextField txtEndtick;
    private JButton btnAdd;
    private JPanel panel_1;
    private JButton btnClearTickList;
    private JButton btnCreateVdmFiles;
    private JButton btnDeleteVdmFiles;
    private JScrollPane scrollPane_1;
    private JTable tableTicks;

    /**
     * Create the panel.
     */
    public DemoEditorView() {
        GridBagLayout gbl_panelVdm = new GridBagLayout();
        gbl_panelVdm.columnWidths = new int[] {
                0, 0, 0, 0, 0, 0, 0, 0
        };
        gbl_panelVdm.rowHeights = new int[] {
                0, 0, 0, 0, 0
        };
        gbl_panelVdm.columnWeights = new double[] {
                0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE
        };
        gbl_panelVdm.rowWeights = new double[] {
                0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE
        };
        setLayout(gbl_panelVdm);

        lblSelectDemoFile = new JLabel("Select Demo File:");
        GridBagConstraints gbc_lblSelectDemoFile = new GridBagConstraints();
        gbc_lblSelectDemoFile.anchor = GridBagConstraints.EAST;
        gbc_lblSelectDemoFile.insets = new Insets(5, 5, 5, 5);
        gbc_lblSelectDemoFile.gridx = 0;
        gbc_lblSelectDemoFile.gridy = 0;
        add(lblSelectDemoFile, gbc_lblSelectDemoFile);

        txtDemofile = new JTextField();
        GridBagConstraints gbc_txtDemofile = new GridBagConstraints();
        gbc_txtDemofile.gridwidth = 3;
        gbc_txtDemofile.insets = new Insets(5, 0, 5, 5);
        gbc_txtDemofile.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtDemofile.gridx = 1;
        gbc_txtDemofile.gridy = 0;
        add(txtDemofile, gbc_txtDemofile);
        txtDemofile.setColumns(10);

        btnBrowse = new JButton("Browse...");
        GridBagConstraints gbc_btnBrowse = new GridBagConstraints();
        gbc_btnBrowse.anchor = GridBagConstraints.WEST;
        gbc_btnBrowse.insets = new Insets(5, 0, 5, 5);
        gbc_btnBrowse.gridx = 4;
        gbc_btnBrowse.gridy = 0;
        add(btnBrowse, gbc_btnBrowse);

        lblStartTick = new JLabel("Start Tick:");
        GridBagConstraints gbc_lblStartTick = new GridBagConstraints();
        gbc_lblStartTick.anchor = GridBagConstraints.EAST;
        gbc_lblStartTick.insets = new Insets(0, 5, 5, 5);
        gbc_lblStartTick.gridx = 0;
        gbc_lblStartTick.gridy = 1;
        add(lblStartTick, gbc_lblStartTick);

        txtStarttick = new JTextField();
        GridBagConstraints gbc_txtStarttick = new GridBagConstraints();
        gbc_txtStarttick.insets = new Insets(0, 0, 5, 5);
        gbc_txtStarttick.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtStarttick.gridx = 1;
        gbc_txtStarttick.gridy = 1;
        add(txtStarttick, gbc_txtStarttick);
        txtStarttick.setColumns(10);

        lblEndTick = new JLabel("End Tick:");
        GridBagConstraints gbc_lblEndTick = new GridBagConstraints();
        gbc_lblEndTick.anchor = GridBagConstraints.EAST;
        gbc_lblEndTick.insets = new Insets(0, 0, 5, 5);
        gbc_lblEndTick.gridx = 2;
        gbc_lblEndTick.gridy = 1;
        add(lblEndTick, gbc_lblEndTick);

        txtEndtick = new JTextField();
        GridBagConstraints gbc_txtEndtick = new GridBagConstraints();
        gbc_txtEndtick.insets = new Insets(0, 0, 5, 5);
        gbc_txtEndtick.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtEndtick.gridx = 3;
        gbc_txtEndtick.gridy = 1;
        add(txtEndtick, gbc_txtEndtick);
        txtEndtick.setColumns(10);

        btnAdd = new JButton("Add");
        GridBagConstraints gbc_btnAdd = new GridBagConstraints();
        gbc_btnAdd.anchor = GridBagConstraints.WEST;
        gbc_btnAdd.insets = new Insets(0, 0, 5, 5);
        gbc_btnAdd.gridx = 4;
        gbc_btnAdd.gridy = 1;
        add(btnAdd, gbc_btnAdd);

        panel_1 = new JPanel();
        FlowLayout flowLayout_3 = (FlowLayout) panel_1.getLayout();
        flowLayout_3.setVgap(0);
        flowLayout_3.setHgap(0);
        GridBagConstraints gbc_panel_1 = new GridBagConstraints();
        gbc_panel_1.gridwidth = 7;
        gbc_panel_1.anchor = GridBagConstraints.WEST;
        gbc_panel_1.insets = new Insets(0, 5, 5, 0);
        gbc_panel_1.gridx = 0;
        gbc_panel_1.gridy = 2;
        add(panel_1, gbc_panel_1);

        btnClearTickList = new JButton("Clear Tick List");
        panel_1.add(btnClearTickList);

        btnCreateVdmFiles = new JButton("Create VDM Files");
        panel_1.add(btnCreateVdmFiles);

        btnDeleteVdmFiles = new JButton("Delete VDM Files");
        panel_1.add(btnDeleteVdmFiles);

        scrollPane_1 = new JScrollPane();
        GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
        gbc_scrollPane_1.insets = new Insets(0, 5, 5, 5);
        gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
        gbc_scrollPane_1.gridwidth = 7;
        gbc_scrollPane_1.gridx = 0;
        gbc_scrollPane_1.gridy = 3;
        add(scrollPane_1, gbc_scrollPane_1);

        tableTicks = new JTable();
        scrollPane_1.setViewportView(tableTicks);

    }

    public JTextField getTxtDemofile() {
        return txtDemofile;
    }

    public JButton getBtnBrowse() {
        return btnBrowse;
    }

    public JTextField getTxtStarttick() {
        return txtStarttick;
    }

    public JTextField getTxtEndtick() {
        return txtEndtick;
    }

    public JButton getBtnAdd() {
        return btnAdd;
    }

    public JButton getBtnClearTickList() {
        return btnClearTickList;
    }

    public JButton getBtnCreateVdmFiles() {
        return btnCreateVdmFiles;
    }

    public JButton getBtnDeleteVdmFiles() {
        return btnDeleteVdmFiles;
    }

    public JTable getTableTicks() {
        return tableTicks;
    }
}
