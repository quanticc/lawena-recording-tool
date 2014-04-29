package ui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;

public class AboutDialog extends JDialog {

  private static final Logger log = Logger.getLogger("lawena");

  private static final long serialVersionUID = 1L;

  private class AboutTextPaneHyperlinkListener implements HyperlinkListener {
    public void hyperlinkUpdate(final HyperlinkEvent e) {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        new SwingWorker<Void, Void>() {
          protected Void doInBackground() throws Exception {
            try {
              Desktop.getDesktop().browse(e.getURL().toURI());
            } catch (IOException | URISyntaxException e1) {
              log.log(Level.FINE, "Could not open URL", e1);
            }
            return null;
          }
        }.execute();
      }
    }
  }

  private class BtnOkActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }

  private final JPanel contentPanel = new JPanel();
  private JButton btnUpdater;

  /**
   * Create the dialog.
   */
  public AboutDialog(String version, String build) {
    setResizable(false);
    setTitle("About");
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new BorderLayout(0, 0));

    JLabel lblHeader = new JLabel("");
    try {
      lblHeader.setIcon(new ImageIcon(AboutDialog.class.getClassLoader().getResource(
          "ui/lawena.png")));
    } catch (Exception e) {
    }
    contentPanel.add(lblHeader, BorderLayout.NORTH);

    JLabel lblContent =
        new JLabel("<html><div style=\\\"text-align: center;\\\"><br>" + "Version <b>" + version
            + "</b><br>" + build);
    lblContent.setHorizontalAlignment(SwingConstants.CENTER);
    lblContent.setFont(new Font("Tahoma", Font.PLAIN, 11));
    contentPanel.add(lblContent, BorderLayout.CENTER);

    JTextPane aboutTextPane = new JTextPane();
    aboutTextPane.addHyperlinkListener(new AboutTextPaneHyperlinkListener());
    aboutTextPane.setFocusable(false);
    aboutTextPane.setOpaque(false);
    aboutTextPane.setEditable(false);
    aboutTextPane.setContentType("text/html");
    String style =
        new StringBuilder().append("body { font-family: ")
            .append(UIManager.getDefaults().getFont("TextPane.font").getFamily()).append("; ")
            .append("font-size: 10pt; text-align: center}").toString();

    ((HTMLDocument) aboutTextPane.getDocument()).getStyleSheet().addRule(style);
    aboutTextPane
        .setText("Simple Team Fortress 2 (TF2) Recording Tool<br>"
            + "Copyright 2011-2014 Montz, Quantic, contributors and others<br>"
            + "<a href=\"http://code.google.com/p/lawenarecordingtool/\">http://code.google.com/p/lawenarecordingtool/</a><br><br>"
            + "Lawena is free software; you can redistribute it and/or modify it under the terms of the <a href=\"https://github.com/iabarca/lawena-recording-tool/blob/master/LICENSE.txt\">GNU GPLv3</a>.<br>"
            + "Graphical .cfg files are almost entirely based on <a href=\"http://chrisdown.name/tf2/\">Chris' maxquality config</a>, with very slight tweaks.<br>"
            + "Built-in Killnotices and Medic HUD were made by <a href=\"http://steamcommunity.com/profiles/76561198023136325\">mih</a>. Thanks! <3<br>"
            + "Most Skyboxes are from GameBanana, made by <a href=\"http://gamebanana.com/members/submissions/textures/289553\">komaokc</a>, and the rest are from the PLDX recording tool.<br>"
            + "Enhanced particles included are also from PLDX recording tool.<br>"
            + "Self-Updating mechanism is possible thanks to <a href=\"http://code.google.com/p/getdown/\">Getdown</a>, developed by Third Rings Design, Inc.<br>"
            + "Microsoft Sysinternals' <a href=\"http://technet.microsoft.com/en-us/sysinternals/bb896655.aspx\">Handle</a> utility by Mark Russinovich.<br>"
            + "TF2 icon modification made by <a href=\"http://hackcypher.deviantart.com/art/Team-Fortress-2-Icons-87662784\">hackcypher</a>.<br>"
            + "Team Fortress and the Team Fortress logo are trademarks of <a href=\"http://www.valvesoftware.com\">Valve Corporation</a>.<br>");
    contentPanel.add(aboutTextPane, BorderLayout.SOUTH);

    JPanel buttonPane = new JPanel();
    getContentPane().add(buttonPane, BorderLayout.SOUTH);
    GridBagLayout gbl_buttonPane = new GridBagLayout();
    gbl_buttonPane.columnWidths = new int[] {0, 0, 80, 0};
    gbl_buttonPane.rowHeights = new int[] {23, 0};
    gbl_buttonPane.columnWeights = new double[] {0.0, 1.0, 0.0, Double.MIN_VALUE};
    gbl_buttonPane.rowWeights = new double[] {0.0, Double.MIN_VALUE};
    buttonPane.setLayout(gbl_buttonPane);

    JButton btnOk = new JButton("OK");
    btnOk.setPreferredSize(new Dimension(80, 23));
    btnOk.addActionListener(new BtnOkActionListener());

    btnUpdater = new JButton("Updater Channel...");
    GridBagConstraints gbc_btnUpdater = new GridBagConstraints();
    gbc_btnUpdater.insets = new Insets(0, 0, 0, 5);
    gbc_btnUpdater.gridx = 0;
    gbc_btnUpdater.gridy = 0;
    buttonPane.add(btnUpdater, gbc_btnUpdater);
    GridBagConstraints gbc_btnOk = new GridBagConstraints();
    gbc_btnOk.anchor = GridBagConstraints.NORTHWEST;
    gbc_btnOk.gridx = 2;
    gbc_btnOk.gridy = 0;
    buttonPane.add(btnOk, gbc_btnOk);

    pack();
    setMinimumSize(new Dimension(520, 400));
    setMaximumSize(new Dimension(520, 400));
    setLocationByPlatform(true);
  }

  public JButton getBtnUpdater() {
    return btnUpdater;
  }
}
