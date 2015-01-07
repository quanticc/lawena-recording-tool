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
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import com.github.lawena.Constants;
import com.github.lawena.Messages;

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
        LogView.class.getResource("/com/github/lawena/ui/fugue/clock.png"))); //$NON-NLS-1$
    setType(Type.POPUP);
    setTitle(Messages.getString("LogView.title")); //$NON-NLS-1$
    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
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
    logScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    logPane.setFont(new Font("Tahoma", Font.PLAIN, 10)); //$NON-NLS-1$
    logPane.setEditable(false);
    logPane.setText(""); //$NON-NLS-1$
    logScroll.setViewportView(logPane);

    toolBar = new JToolBar();
    toolBar.setFloatable(false);
    contentPane.add(toolBar, BorderLayout.SOUTH);

    copyLogButton = new JButton(""); //$NON-NLS-1$
    copyLogButton.setIcon(new ImageIcon(LogView.class
        .getResource("/com/github/lawena/ui/fugue/clipboard-paste-document-text.png"))); //$NON-NLS-1$
    copyLogButton.setToolTipText(Messages.getString("LogView.copyToClipboardTooltip")); //$NON-NLS-1$
    toolBar.add(copyLogButton);

    openLogButton = new JButton(""); //$NON-NLS-1$
    openLogButton.setToolTipText(Messages.getString("LogView.openLogFileTooltip")); //$NON-NLS-1$
    openLogButton.setIcon(new ImageIcon(LogView.class
        .getResource("/com/github/lawena/ui/fugue/folder-open-document-text.png"))); //$NON-NLS-1$
    toolBar.add(openLogButton);

    horizontalGlue = Box.createHorizontalGlue();
    toolBar.add(horizontalGlue);

    levelComboBox = new JComboBox<>();
    levelComboBox.setMaximumSize(new Dimension(60, 20));
    levelComboBox.setPreferredSize(new Dimension(60, 20));
    levelComboBox.setFont(new Font("Tahoma", Font.PLAIN, 10)); //$NON-NLS-1$
    levelComboBox.setModel(new DefaultComboBoxModel<>(Constants.LEVELS));
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
