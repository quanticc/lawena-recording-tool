package com.github.lawena.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import java.awt.Toolkit;

public class LogView extends JFrame {

  private static final long serialVersionUID = 1L;

  private JPanel contentPane;
  private JTextPane logPane;
  private JScrollPane logScroll;
  private JToolBar toolBar;
  private JButton copyLogButton;
  private JButton openLogButton;
  private JComboBox<String> levelComboBox;
  private Component horizontalGlue;

  public LogView() {
    setIconImage(Toolkit.getDefaultToolkit().getImage(
        LogView.class.getResource("/com/github/lawena/ui/fugue/clock.png")));
    setType(Type.POPUP);
    setTitle("Log");
    setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
    setBounds(100, 100, 450, 300);
    setMinimumSize(new Dimension(300, 100));
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 0, 5));
    contentPane.setLayout(new BorderLayout(0, 0));
    setContentPane(contentPane);

    logScroll = new JScrollPane();
    contentPane.add(logScroll, BorderLayout.CENTER);

    logPane = new JTextPane();
    logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    logPane.setFont(new Font("Tahoma", Font.PLAIN, 10));
    logPane.setEditable(false);
    logPane.setText("");
    logScroll.setViewportView(logPane);

    toolBar = new JToolBar();
    toolBar.setFloatable(false);
    contentPane.add(toolBar, BorderLayout.SOUTH);

    copyLogButton = new JButton("");
    copyLogButton.setIcon(new ImageIcon(LogView.class
        .getResource("/com/github/lawena/ui/fugue/clipboard-paste-document-text.png")));
    copyLogButton.setToolTipText("Copy to Clipboard");
    toolBar.add(copyLogButton);

    openLogButton = new JButton("");
    openLogButton.setToolTipText("Open Full Log");
    openLogButton.setIcon(new ImageIcon(LogView.class
        .getResource("/com/github/lawena/ui/fugue/folder-open-document-text.png")));
    toolBar.add(openLogButton);

    horizontalGlue = Box.createHorizontalGlue();
    toolBar.add(horizontalGlue);

    levelComboBox = new JComboBox<String>();
    levelComboBox.setMaximumSize(new Dimension(60, 20));
    levelComboBox.setPreferredSize(new Dimension(60, 20));
    levelComboBox.setFont(new Font("Tahoma", Font.PLAIN, 10));
    levelComboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"OFF", "ERROR", "WARN",
        "INFO", "DEBUG", "TRACE", "ALL"}));
    levelComboBox.setSelectedIndex(4);
    toolBar.add(levelComboBox);
  }

  public JTextPane getLogPane() {
    return logPane;
  }

  public JScrollPane getLogScroll() {
    return logScroll;
  }

  public JButton getOpenLogButton() {
    return openLogButton;
  }

  public JButton getCopyLogButton() {
    return copyLogButton;
  }

  public JComboBox<String> getLevelComboBox() {
    return levelComboBox;
  }

}
