package com.github.lawena.ui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import com.github.lawena.Messages;
import com.github.lawena.util.Images;

public class DemoEditorView extends JPanel {

  private static final long serialVersionUID = 1L;

  private JTextFieldPlaceholder startTickTextField;
  private JTextFieldPlaceholder endTickTextField;
  private JButton addButton;
  private JPanel panelButtonsBottomLeft;
  private JButton clearTickListButton;
  private JButton createVdmFilesButton;
  private JButton deleteVdmFilesButton;
  private JScrollPane scrollPane_1;
  private JTable segmentListTable;
  private JButton deleteSelectedTickButton;
  private JCheckBox noSkipToTickCheckBox;
  private JScrollPane scrollPane_2;
  private JTable demosTable;
  private JCheckBox autoplayFirstDemoCheckBox;
  private JComboBox<String> segmentTypeComboBox;
  private JPanel panelButtonsCenterLeft;
  private JTextFieldPlaceholder demoFilterTextField;
  private JButton changeDemoFolderButton;
  private JButton saveSegmentListButton;
  private JButton loadSegmentListButton;
  private JToolBar toolbar;

  public DemoEditorView() {
    GridBagLayout gbl_panelVdm = new GridBagLayout();
    gbl_panelVdm.columnWidths = new int[] {0};
    gbl_panelVdm.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
    gbl_panelVdm.columnWeights = new double[] {1.0};
    gbl_panelVdm.rowWeights = new double[] {0.0, 1.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
    setLayout(gbl_panelVdm);

    demoFilterTextField = new JTextFieldPlaceholder();
    demoFilterTextField.setPlaceholder(Messages.getString("DemoEditorView.search")); //$NON-NLS-1$
    GridBagConstraints gbc_txtFilterDemos = new GridBagConstraints();
    gbc_txtFilterDemos.insets = new Insets(5, 5, 5, 5);
    gbc_txtFilterDemos.fill = GridBagConstraints.HORIZONTAL;
    gbc_txtFilterDemos.gridx = 0;
    gbc_txtFilterDemos.gridy = 0;
    add(demoFilterTextField, gbc_txtFilterDemos);
    demoFilterTextField.setColumns(10);

    scrollPane_2 = new JScrollPane();
    GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
    gbc_scrollPane_2.gridheight = 2;
    gbc_scrollPane_2.insets = new Insets(0, 5, 5, 5);
    gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
    gbc_scrollPane_2.gridx = 0;
    gbc_scrollPane_2.gridy = 1;
    add(scrollPane_2, gbc_scrollPane_2);

    demosTable = new JTable();
    demosTable.setShowVerticalLines(false);
    demosTable.setGridColor(new Color(0, 0, 0, 30));
    scrollPane_2.setViewportView(demosTable);

    panelButtonsCenterLeft = new JPanel();
    FlowLayout flowLayout = (FlowLayout) panelButtonsCenterLeft.getLayout();
    flowLayout.setAlignment(FlowLayout.LEADING);
    flowLayout.setVgap(0);
    GridBagConstraints gbc_panelButtonsCenterLeft = new GridBagConstraints();
    gbc_panelButtonsCenterLeft.insets = new Insets(0, 5, 5, 5);
    gbc_panelButtonsCenterLeft.fill = GridBagConstraints.BOTH;
    gbc_panelButtonsCenterLeft.gridx = 0;
    gbc_panelButtonsCenterLeft.gridy = 3;
    add(panelButtonsCenterLeft, gbc_panelButtonsCenterLeft);

    segmentTypeComboBox = new JComboBox<>();
    panelButtonsCenterLeft.add(segmentTypeComboBox);

    startTickTextField = new JTextFieldPlaceholder();
    startTickTextField.setPlaceholder(Messages.getString("DemoEditorView.startAt")); //$NON-NLS-1$
    panelButtonsCenterLeft.add(startTickTextField);
    startTickTextField.setColumns(5);

    endTickTextField = new JTextFieldPlaceholder();
    endTickTextField.setPlaceholder(Messages.getString("DemoEditorView.stopAt")); //$NON-NLS-1$
    panelButtonsCenterLeft.add(endTickTextField);
    endTickTextField.setColumns(5);

    toolbar = new JToolBar();
    toolbar.setFloatable(false);
    panelButtonsCenterLeft.add(toolbar);

    addButton = new JButton(""); //$NON-NLS-1$
    addButton.setIcon(Images.get("ui/fugue/plus-button.png")); //$NON-NLS-1$
    addButton.setToolTipText(Messages.getString("DemoEditorView.addSegmentTooltip")); //$NON-NLS-1$
    toolbar.add(addButton);

    deleteSelectedTickButton = new JButton(""); //$NON-NLS-1$
    deleteSelectedTickButton.setIcon(Images.get("ui/fugue/minus-button.png")); //$NON-NLS-1$
    deleteSelectedTickButton.setToolTipText(Messages
        .getString("DemoEditorView.removeSegmentTooltip")); //$NON-NLS-1$
    toolbar.add(deleteSelectedTickButton);

    clearTickListButton = new JButton(""); //$NON-NLS-1$
    clearTickListButton.setIcon(Images.get("ui/fugue/bin.png")); //$NON-NLS-1$
    clearTickListButton.setToolTipText(Messages.getString("DemoEditorView.clearSegmentsTooltip")); //$NON-NLS-1$
    toolbar.add(clearTickListButton);

    saveSegmentListButton = new JButton(""); //$NON-NLS-1$
    saveSegmentListButton.setIcon(Images.get("ui/fugue/drive-download.png")); //$NON-NLS-1$
    saveSegmentListButton.setToolTipText(Messages
        .getString("DemoEditorView.saveSegmentListTooltip")); //$NON-NLS-1$
    toolbar.add(saveSegmentListButton);

    loadSegmentListButton = new JButton(""); //$NON-NLS-1$
    loadSegmentListButton.setIcon(Images.get("ui/fugue/drive-upload.png")); //$NON-NLS-1$
    loadSegmentListButton.setToolTipText(Messages
        .getString("DemoEditorView.loadSegmentListTooltip")); //$NON-NLS-1$
    toolbar.add(loadSegmentListButton);

    changeDemoFolderButton = new JButton(""); //$NON-NLS-1$
    changeDemoFolderButton.setIcon(Images.get("ui/fugue/folder-bookmark.png")); //$NON-NLS-1$
    changeDemoFolderButton.setToolTipText(Messages
        .getString("DemoEditorView.changeDemosLocationTooltip")); //$NON-NLS-1$
    toolbar.add(changeDemoFolderButton);

    scrollPane_1 = new JScrollPane();
    GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
    gbc_scrollPane_1.insets = new Insets(0, 5, 5, 5);
    gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
    gbc_scrollPane_1.gridx = 0;
    gbc_scrollPane_1.gridy = 4;
    add(scrollPane_1, gbc_scrollPane_1);

    segmentListTable = new JTable();
    segmentListTable.setShowVerticalLines(false);
    segmentListTable.setGridColor(new Color(0, 0, 0, 30));
    scrollPane_1.setViewportView(segmentListTable);

    panelButtonsBottomLeft = new JPanel();
    FlowLayout fl_panelButtonsBottomLeft = (FlowLayout) panelButtonsBottomLeft.getLayout();
    fl_panelButtonsBottomLeft.setAlignment(FlowLayout.LEADING);
    fl_panelButtonsBottomLeft.setVgap(0);
    fl_panelButtonsBottomLeft.setHgap(0);
    GridBagConstraints gbc_panelButtonsBottomLeft = new GridBagConstraints();
    gbc_panelButtonsBottomLeft.fill = GridBagConstraints.HORIZONTAL;
    gbc_panelButtonsBottomLeft.insets = new Insets(0, 5, 4, 5);
    gbc_panelButtonsBottomLeft.gridx = 0;
    gbc_panelButtonsBottomLeft.gridy = 5;
    add(panelButtonsBottomLeft, gbc_panelButtonsBottomLeft);

    createVdmFilesButton = new JButton(Messages.getString("DemoEditorView.createVdmFiles")); //$NON-NLS-1$
    panelButtonsBottomLeft.add(createVdmFilesButton);

    deleteVdmFilesButton = new JButton(Messages.getString("DemoEditorView.deleteVdmFiles")); //$NON-NLS-1$
    panelButtonsBottomLeft.add(deleteVdmFilesButton);

    autoplayFirstDemoCheckBox =
        new JCheckBox(Messages.getString("DemoEditorView.autoPlayFirstDemo")); //$NON-NLS-1$
    autoplayFirstDemoCheckBox.setToolTipText(Messages
        .getString("DemoEditorView.autoPlayFirstDemoTooltip")); //$NON-NLS-1$
    panelButtonsBottomLeft.add(autoplayFirstDemoCheckBox);

    noSkipToTickCheckBox =
        new JCheckBox(Messages.getString("DemoEditorView.removeSkipToTickLines")); //$NON-NLS-1$
    noSkipToTickCheckBox.setToolTipText(Messages
        .getString("DemoEditorView.removeSkipToTickLinesTooltip")); //$NON-NLS-1$
    panelButtonsBottomLeft.add(noSkipToTickCheckBox);

  }

  public JTextFieldPlaceholder getStartTickTextField() {
    return startTickTextField;
  }

  public JTextFieldPlaceholder getEndTickTextField() {
    return endTickTextField;
  }

  public JButton getAddButton() {
    return addButton;
  }

  public JButton getClearTickListButton() {
    return clearTickListButton;
  }

  public JButton getCreateVdmFilesButton() {
    return createVdmFilesButton;
  }

  public JButton getDeleteVdmFilesButton() {
    return deleteVdmFilesButton;
  }

  public JButton getSaveSegmentListButton() {
    return saveSegmentListButton;
  }

  public JButton getLoadSegmentListButton() {
    return loadSegmentListButton;
  }

  public JButton getChangeDemoFolderButton() {
    return changeDemoFolderButton;
  }

  public JTable getSegmentListTable() {
    return segmentListTable;
  }

  public JButton getDeleteSelectedTickButton() {
    return deleteSelectedTickButton;
  }

  public JCheckBox getNoSkipToTickCheckBox() {
    return noSkipToTickCheckBox;
  }

  public JTable getDemosTable() {
    return demosTable;
  }

  public JComboBox<String> getSegmentTypeComboBox() {
    return segmentTypeComboBox;
  }

  public JTextField getDemoFilterTextField() {
    return demoFilterTextField;
  }

  public JCheckBox getAutoplayFirstDemoCheckBox() {
    return autoplayFirstDemoCheckBox;
  }

}
