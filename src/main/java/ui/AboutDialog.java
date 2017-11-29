package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AboutDialog extends JDialog {

    private static final Logger log = Logger.getLogger("lawena");

    private static final long serialVersionUID = 1L;
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
        URL url = AboutDialog.class.getClassLoader().getResource("ui/lawena.png");
        if (url != null) {
            lblHeader.setIcon(new ImageIcon(url));
        }
        contentPanel.add(lblHeader, BorderLayout.NORTH);

        JLabel lblContent =
            new JLabel("<html><div style=\\\"text-align: center;\\\"><br>" + "Version <b>" + version + "</b><br>"
                + build);
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
            "body { font-family: " +
                UIManager.getDefaults().getFont("TextPane.font").getFamily() + "; " +
                "font-size: 10pt; text-align: center}";

        ((HTMLDocument) aboutTextPane.getDocument()).getStyleSheet().addRule(style);
        try {
            URL aboutUrl = AboutDialog.class.getClassLoader().getResource("ui/about.html");
            if (aboutUrl != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(aboutUrl.openStream()))) {
                    aboutTextPane.setText(reader.lines().collect(Collectors.joining()));
                }
            }
        } catch (IOException e) {
            log.log(Level.FINE, "Could not display text", e);
        }
        contentPanel.add(aboutTextPane, BorderLayout.SOUTH);

        JPanel buttonPane = new JPanel();
        getContentPane().add(buttonPane, BorderLayout.SOUTH);
        GridBagLayout gbl_buttonPane = new GridBagLayout();
        gbl_buttonPane.columnWidths = new int[]{0, 0, 80, 0};
        gbl_buttonPane.rowHeights = new int[]{23, 0};
        gbl_buttonPane.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
        gbl_buttonPane.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        buttonPane.setLayout(gbl_buttonPane);

        JButton btnOk = new JButton("OK");
        btnOk.setPreferredSize(new Dimension(80, 23));
        btnOk.addActionListener(new BtnOkActionListener());

        btnUpdater = new JButton("Select Updater Branch...");
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

    private static class AboutTextPaneHyperlinkListener implements HyperlinkListener {

        @Override
        public void hyperlinkUpdate(final HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                new SwingWorker<Void, Void>() {
                    @Override
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

        @Override
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
        }
    }
}
