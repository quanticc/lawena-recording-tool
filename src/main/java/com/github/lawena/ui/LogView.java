package com.github.lawena.ui;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

public class LogView extends JFrame {

  private static final long serialVersionUID = 1L;

  private JPanel contentPane;
  private JTextPane logPane;
  private JScrollPane logScroll;

  public LogView() {
    setType(Type.UTILITY);
    setTitle("Log");
    setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
    setBounds(100, 100, 450, 300);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    contentPane.setLayout(new BorderLayout(0, 0));
    setContentPane(contentPane);

    logScroll = new JScrollPane();
    contentPane.add(logScroll, BorderLayout.CENTER);

    logPane = new JTextPane();
    logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    logPane.setFont(new Font("Tahoma", Font.PLAIN, 12));
    logPane.setEditable(false);
    logPane.setText("");
    logScroll.setViewportView(logPane);
  }

  public JTextPane getLogPane() {
    return logPane;
  }

  public JScrollPane getLogScroll() {
    return logScroll;
  }
}
