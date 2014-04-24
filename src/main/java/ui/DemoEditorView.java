package ui;

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
  private JPanel panelButtonsLeft;
  private JButton btnClearTickList;
  private JButton btnCreateVdmFiles;
  private JButton btnDeleteVdmFiles;
  private JScrollPane scrollPane_1;
  private JTable tableTicks;
  private JPanel panelButtonsRight;
  private JButton btnDeleteSelectedTick;
  private JScrollPane scrollPane;
  private JTextArea txtrDemodetails;
  private JCheckBox chckbxSrcDemoFix;

  /**
   * Create the panel.
   */
  public DemoEditorView() {
    GridBagLayout gbl_panelVdm = new GridBagLayout();
    gbl_panelVdm.columnWidths = new int[] {0, 110, 0, 110, 0, 0, 0, 0};
    gbl_panelVdm.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
    gbl_panelVdm.columnWeights =
        new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 10.0, 0.0, Double.MIN_VALUE};
    gbl_panelVdm.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
    setLayout(gbl_panelVdm);

    lblSelectDemoFile = new JLabel("Demo File:");
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
    gbc_btnBrowse.fill = GridBagConstraints.HORIZONTAL;
    gbc_btnBrowse.insets = new Insets(5, 0, 5, 5);
    gbc_btnBrowse.gridx = 4;
    gbc_btnBrowse.gridy = 0;
    add(btnBrowse, gbc_btnBrowse);

    scrollPane = new JScrollPane();
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    GridBagConstraints gbc_scrollPane = new GridBagConstraints();
    gbc_scrollPane.gridheight = 4;
    gbc_scrollPane.gridwidth = 2;
    gbc_scrollPane.insets = new Insets(5, 0, 5, 5);
    gbc_scrollPane.fill = GridBagConstraints.BOTH;
    gbc_scrollPane.gridx = 5;
    gbc_scrollPane.gridy = 0;
    add(scrollPane, gbc_scrollPane);

    txtrDemodetails = new JTextArea();
    txtrDemodetails.setWrapStyleWord(true);
    txtrDemodetails.setText("Select a demo to view its details here.");
    txtrDemodetails.setOpaque(false);
    txtrDemodetails.setEditable(false);
    txtrDemodetails.setFont(new Font("Segoe UI", Font.PLAIN, 10));
    scrollPane.setViewportView(txtrDemodetails);

    lblStartTick = new JLabel("Start Tick:");
    GridBagConstraints gbc_lblStartTick = new GridBagConstraints();
    gbc_lblStartTick.anchor = GridBagConstraints.EAST;
    gbc_lblStartTick.insets = new Insets(0, 5, 5, 5);
    gbc_lblStartTick.gridx = 0;
    gbc_lblStartTick.gridy = 2;
    add(lblStartTick, gbc_lblStartTick);

    txtStarttick = new JTextField();
    GridBagConstraints gbc_txtStarttick = new GridBagConstraints();
    gbc_txtStarttick.insets = new Insets(0, 0, 5, 5);
    gbc_txtStarttick.fill = GridBagConstraints.HORIZONTAL;
    gbc_txtStarttick.gridx = 1;
    gbc_txtStarttick.gridy = 2;
    add(txtStarttick, gbc_txtStarttick);
    txtStarttick.setColumns(10);

    lblEndTick = new JLabel("End Tick:");
    GridBagConstraints gbc_lblEndTick = new GridBagConstraints();
    gbc_lblEndTick.anchor = GridBagConstraints.EAST;
    gbc_lblEndTick.insets = new Insets(0, 0, 5, 5);
    gbc_lblEndTick.gridx = 2;
    gbc_lblEndTick.gridy = 2;
    add(lblEndTick, gbc_lblEndTick);

    txtEndtick = new JTextField();
    GridBagConstraints gbc_txtEndtick = new GridBagConstraints();
    gbc_txtEndtick.insets = new Insets(0, 0, 5, 5);
    gbc_txtEndtick.fill = GridBagConstraints.HORIZONTAL;
    gbc_txtEndtick.gridx = 3;
    gbc_txtEndtick.gridy = 2;
    add(txtEndtick, gbc_txtEndtick);
    txtEndtick.setColumns(10);

    panelButtonsRight = new JPanel();
    FlowLayout flowLayout = (FlowLayout) panelButtonsRight.getLayout();
    flowLayout.setAlignment(FlowLayout.RIGHT);
    flowLayout.setVgap(0);
    flowLayout.setHgap(0);
    GridBagConstraints gbc_panelButtonsRight = new GridBagConstraints();
    gbc_panelButtonsRight.anchor = GridBagConstraints.WEST;
    gbc_panelButtonsRight.gridwidth = 4;
    gbc_panelButtonsRight.insets = new Insets(0, 5, 5, 5);
    gbc_panelButtonsRight.fill = GridBagConstraints.VERTICAL;
    gbc_panelButtonsRight.gridx = 0;
    gbc_panelButtonsRight.gridy = 3;
    add(panelButtonsRight, gbc_panelButtonsRight);

    btnAdd = new JButton("Add Segment");
    panelButtonsRight.add(btnAdd);

    btnDeleteSelectedTick = new JButton("Delete Segment");
    panelButtonsRight.add(btnDeleteSelectedTick);

    btnClearTickList = new JButton("Clear List");
    panelButtonsRight.add(btnClearTickList);

    scrollPane_1 = new JScrollPane();
    GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
    gbc_scrollPane_1.insets = new Insets(0, 5, 5, 5);
    gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
    gbc_scrollPane_1.gridwidth = 7;
    gbc_scrollPane_1.gridx = 0;
    gbc_scrollPane_1.gridy = 4;
    add(scrollPane_1, gbc_scrollPane_1);

    tableTicks = new JTable();
    scrollPane_1.setViewportView(tableTicks);

    panelButtonsLeft = new JPanel();
    FlowLayout fl_panelButtonsLeft = (FlowLayout) panelButtonsLeft.getLayout();
    fl_panelButtonsLeft.setVgap(0);
    fl_panelButtonsLeft.setHgap(0);
    GridBagConstraints gbc_panelButtonsLeft = new GridBagConstraints();
    gbc_panelButtonsLeft.gridwidth = 4;
    gbc_panelButtonsLeft.anchor = GridBagConstraints.WEST;
    gbc_panelButtonsLeft.insets = new Insets(0, 5, 5, 5);
    gbc_panelButtonsLeft.gridx = 0;
    gbc_panelButtonsLeft.gridy = 5;
    add(panelButtonsLeft, gbc_panelButtonsLeft);

    btnCreateVdmFiles = new JButton("Create VDM Files");
    panelButtonsLeft.add(btnCreateVdmFiles);

    btnDeleteVdmFiles = new JButton("Delete VDM Files...");
    panelButtonsLeft.add(btnDeleteVdmFiles);

    chckbxSrcDemoFix = new JCheckBox("SrcDemo\u00B2 Fix (hover for details)");
    chckbxSrcDemoFix
        .setToolTipText("<html>Don't add \"skiptotick\" lines to VDM files created, which<br>\r\nin some cases, can solve VDM playback along with SrcDemo\u00B2");
    panelButtonsLeft.add(chckbxSrcDemoFix);

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

  public JButton getBtnDeleteSelectedTick() {
    return btnDeleteSelectedTick;
  }

  public JTextArea getTxtrDemodetails() {
    return txtrDemodetails;
  }

  public JCheckBox getChckbxSrcDemoFix() {
    return chckbxSrcDemoFix;
  }
}
