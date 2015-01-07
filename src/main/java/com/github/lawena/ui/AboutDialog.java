package com.github.lawena.ui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.Messages;

public class AboutDialog extends JDialog {

  static final Logger log = LoggerFactory.getLogger(AboutDialog.class);

  private static final long serialVersionUID = 1L;

  private static class AboutTextPaneHyperlinkListener implements HyperlinkListener {
    public AboutTextPaneHyperlinkListener() {}

    @Override
    public void hyperlinkUpdate(final HyperlinkEvent e) {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        new SwingWorker<Void, Void>() {
          @Override
          protected Void doInBackground() throws Exception {
            try {
              Desktop.getDesktop().browse(e.getURL().toURI());
            } catch (IOException | URISyntaxException e1) {
              log.warn("Could not open URL", e1); //$NON-NLS-1$
            }
            return null;
          }
        }.execute();
      }
    }
  }

  private class BtnOkActionListener implements ActionListener {
    public BtnOkActionListener() {}

    @Override
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }

  private final JPanel contentPanel = new JPanel();

  /**
   * Create the dialog.
   */
  public AboutDialog(String version, String build) {
    setResizable(false);
    setTitle(Messages.getString("AboutDialog.aboutTitle")); //$NON-NLS-1$
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new BorderLayout(0, 0));

    JLabel lblHeader = new JLabel(""); //$NON-NLS-1$
    try {
      lblHeader.setIcon(new ImageIcon(getClass().getResource("lawena.png"))); //$NON-NLS-1$
    } catch (Exception e) {
      // ignore, only effect is not having any icon
    }
    contentPanel.add(lblHeader, BorderLayout.NORTH);

    JLabel lblContent = new JLabel("<html><div style=\\\"text-align: center;\\\"><br>" + //$NON-NLS-1$
        Messages.getString("AboutDialog.version") + "<b>" + version //$NON-NLS-1$ //$NON-NLS-2$
        + "</b><br>" + build); //$NON-NLS-1$
    lblContent.setHorizontalAlignment(SwingConstants.CENTER);
    lblContent.setFont(new Font("Tahoma", Font.PLAIN, 11)); //$NON-NLS-1$
    contentPanel.add(lblContent, BorderLayout.CENTER);

    JTextPane aboutTextPane = new JTextPane();
    aboutTextPane.addHyperlinkListener(new AboutTextPaneHyperlinkListener());
    aboutTextPane.setFocusable(false);
    aboutTextPane.setOpaque(false);
    aboutTextPane.setEditable(false);
    aboutTextPane.setContentType("text/html"); //$NON-NLS-1$
    String style = new StringBuilder().append("body { font-family: ") //$NON-NLS-1$
        .append(UIManager.getDefaults().getFont("TextPane.font").getFamily()).append("; ") //$NON-NLS-1$ //$NON-NLS-2$
        .append("font-size: 10pt; text-align: center}").toString(); //$NON-NLS-1$

    ((HTMLDocument) aboutTextPane.getDocument()).getStyleSheet().addRule(style);
    aboutTextPane.setText(Messages.getString("AboutDialog.aboutTextArea")); //$NON-NLS-1$
    contentPanel.add(aboutTextPane, BorderLayout.SOUTH);

    JPanel buttonPane = new JPanel();
    getContentPane().add(buttonPane, BorderLayout.SOUTH);
    GridBagLayout gbl_buttonPane = new GridBagLayout();
    gbl_buttonPane.columnWidths = new int[] {0, 0, 80, 0};
    gbl_buttonPane.rowHeights = new int[] {23, 0};
    gbl_buttonPane.columnWeights = new double[] {0.0, 1.0, 0.0, Double.MIN_VALUE};
    gbl_buttonPane.rowWeights = new double[] {0.0, Double.MIN_VALUE};
    buttonPane.setLayout(gbl_buttonPane);

    JButton btnOk = new JButton(Messages.getString("AboutDialog.OK")); //$NON-NLS-1$
    btnOk.setPreferredSize(new Dimension(80, 23));
    btnOk.addActionListener(new BtnOkActionListener());
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

}
