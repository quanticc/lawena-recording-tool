
package ui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import java.awt.Component;

import javax.swing.Box;

public class DemoEditorView extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JTextField txtStarttick;
    private JTextField txtEndtick;
    private JButton btnAdd;
    private JPanel panelButtonsBottomLeft;
    private JButton btnClearTickList;
    private JButton btnCreateVdmFiles;
    private JButton btnDeleteVdmFiles;
    private JScrollPane scrollPane_1;
    private JTable tableTicks;
    private JButton btnDeleteSelectedTick;
    private JCheckBox chckbxSrcDemoFix;
    private JScrollPane scrollPane_2;
    private JTable tableDemos;
    private JCheckBox chckbxAutoplayFirstDemo;
    private JLabel lblStartTick;
    private JLabel lblEndTick;
    private JComboBox cmbSegmentType;
    private JPanel panelButtonsCenterLeft;
    private Component horizontalStrut;

    /**
     * Create the panel.
     */
    public DemoEditorView() {
        GridBagLayout gbl_panelVdm = new GridBagLayout();
        gbl_panelVdm.columnWidths = new int[] {
                0
        };
        gbl_panelVdm.rowHeights = new int[] {
                0, 0, 0, 0, 0, 0
        };
        gbl_panelVdm.columnWeights = new double[] {
                1.0
        };
        gbl_panelVdm.rowWeights = new double[] {
                1.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE
        };
        setLayout(gbl_panelVdm);

        scrollPane_2 = new JScrollPane();
        GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
        gbc_scrollPane_2.gridheight = 2;
        gbc_scrollPane_2.insets = new Insets(5, 5, 5, 5);
        gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
        gbc_scrollPane_2.gridx = 0;
        gbc_scrollPane_2.gridy = 0;
        add(scrollPane_2, gbc_scrollPane_2);

        tableDemos = new JTable();
        tableDemos.setShowVerticalLines(false);
        tableDemos.setGridColor(new Color(0, 0, 0, 30));
        scrollPane_2.setViewportView(tableDemos);

        panelButtonsCenterLeft = new JPanel();
        FlowLayout flowLayout = (FlowLayout) panelButtonsCenterLeft.getLayout();
        flowLayout.setHgap(0);
        flowLayout.setAlignment(FlowLayout.LEADING);
        flowLayout.setVgap(0);
        GridBagConstraints gbc_panelButtonsCenterLeft = new GridBagConstraints();
        gbc_panelButtonsCenterLeft.insets = new Insets(0, 5, 5, 5);
        gbc_panelButtonsCenterLeft.fill = GridBagConstraints.BOTH;
        gbc_panelButtonsCenterLeft.gridx = 0;
        gbc_panelButtonsCenterLeft.gridy = 2;
        add(panelButtonsCenterLeft, gbc_panelButtonsCenterLeft);

        cmbSegmentType = new JComboBox();
        panelButtonsCenterLeft.add(cmbSegmentType);

        lblStartTick = new JLabel(" Start: ");
        panelButtonsCenterLeft.add(lblStartTick);

        txtStarttick = new JTextField();
        panelButtonsCenterLeft.add(txtStarttick);
        txtStarttick.setColumns(5);

        lblEndTick = new JLabel(" End: ");
        panelButtonsCenterLeft.add(lblEndTick);

        txtEndtick = new JTextField();
        panelButtonsCenterLeft.add(txtEndtick);
        txtEndtick.setColumns(5);

        horizontalStrut = Box.createHorizontalStrut(10);
        panelButtonsCenterLeft.add(horizontalStrut);

        btnAdd = new JButton("Add Segment");
        panelButtonsCenterLeft.add(btnAdd);

        btnDeleteSelectedTick = new JButton("Delete Segment");
        panelButtonsCenterLeft.add(btnDeleteSelectedTick);

        btnClearTickList = new JButton("Clear List");
        panelButtonsCenterLeft.add(btnClearTickList);

        scrollPane_1 = new JScrollPane();
        GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
        gbc_scrollPane_1.insets = new Insets(0, 5, 5, 5);
        gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
        gbc_scrollPane_1.gridx = 0;
        gbc_scrollPane_1.gridy = 3;
        add(scrollPane_1, gbc_scrollPane_1);

        tableTicks = new JTable();
        tableTicks.setShowVerticalLines(false);
        tableTicks.setGridColor(new Color(0, 0, 0, 30));
        scrollPane_1.setViewportView(tableTicks);

        panelButtonsBottomLeft = new JPanel();
        FlowLayout fl_panelButtonsBottomLeft = (FlowLayout) panelButtonsBottomLeft.getLayout();
        fl_panelButtonsBottomLeft.setAlignment(FlowLayout.LEADING);
        fl_panelButtonsBottomLeft.setVgap(0);
        fl_panelButtonsBottomLeft.setHgap(0);
        GridBagConstraints gbc_panelButtonsBottomLeft = new GridBagConstraints();
        gbc_panelButtonsBottomLeft.fill = GridBagConstraints.HORIZONTAL;
        gbc_panelButtonsBottomLeft.insets = new Insets(0, 5, 4, 5);
        gbc_panelButtonsBottomLeft.gridx = 0;
        gbc_panelButtonsBottomLeft.gridy = 4;
        add(panelButtonsBottomLeft, gbc_panelButtonsBottomLeft);

        btnCreateVdmFiles = new JButton("Create VDM Files");
        panelButtonsBottomLeft.add(btnCreateVdmFiles);

        btnDeleteVdmFiles = new JButton("Delete VDM Files...");
        panelButtonsBottomLeft.add(btnDeleteVdmFiles);

        chckbxAutoplayFirstDemo = new JCheckBox("AutoPlay First Demo");
        panelButtonsBottomLeft.add(chckbxAutoplayFirstDemo);

        chckbxSrcDemoFix = new JCheckBox("Special SrcDemo\u00B2 Fix");
        chckbxSrcDemoFix
                .setToolTipText("<html>Don't add \"skiptotick\" lines to VDM files created, which<br>\r\nin some cases, can solve VDM playback along with SrcDemo\u00B2");
        panelButtonsBottomLeft.add(chckbxSrcDemoFix);

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

    public JButton getBtnDeleteSelectedTick() {
        return btnDeleteSelectedTick;
    }

    public JCheckBox getChckbxSrcDemoFix() {
        return chckbxSrcDemoFix;
    }

    public JTable getTableDemos() {
        return tableDemos;
    }

    public JComboBox getCmbSegmentType() {
        return cmbSegmentType;
    }
}
