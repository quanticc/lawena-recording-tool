package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class CustomSettingsDialog extends JDialog {

  private static final long serialVersionUID = 1L;

  private final JPanel contentPanel = new JPanel();
  private JTextArea textArea;
  private JButton okButton;
  private JButton cancelButton;

  /**
   * Create the dialog.
   */
  public CustomSettingsDialog() {
    setTitle("Custom Settings");
    setModalityType(ModalityType.APPLICATION_MODAL);
    setBounds(100, 100, 450, 300);
    setMinimumSize(new Dimension(450, 300));
    try {
      setIconImage(new ImageIcon(getClass().getClassLoader().getResource("ui/tf2.png")).getImage());
    } catch (Exception e) {
    }
    BorderLayout borderLayout = new BorderLayout();
    getContentPane().setLayout(borderLayout);
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    GridBagLayout gbl_contentPanel = new GridBagLayout();
    gbl_contentPanel.columnWidths = new int[] {0, 0};
    gbl_contentPanel.rowHeights = new int[] {0, 0, 0};
    gbl_contentPanel.columnWeights = new double[] {1.0, Double.MIN_VALUE};
    gbl_contentPanel.rowWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};
    contentPanel.setLayout(gbl_contentPanel);
    {
      JLabel lblHeader =
          new JLabel(
              "Override or enhance the standard configuration. These commands will execute while your demo is playing.");
      GridBagConstraints gbc_lblHeader = new GridBagConstraints();
      gbc_lblHeader.anchor = GridBagConstraints.WEST;
      gbc_lblHeader.insets = new Insets(0, 0, 5, 0);
      gbc_lblHeader.gridx = 0;
      gbc_lblHeader.gridy = 0;
      contentPanel.add(lblHeader, gbc_lblHeader);
    }
    {
      JScrollPane scrollPane = new JScrollPane();
      GridBagConstraints gbc_scrollPane = new GridBagConstraints();
      gbc_scrollPane.fill = GridBagConstraints.BOTH;
      gbc_scrollPane.gridx = 0;
      gbc_scrollPane.gridy = 1;
      contentPanel.add(scrollPane, gbc_scrollPane);
      {
        textArea = new JTextArea(20, 50);
        textArea.setEditable(true);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        scrollPane.setViewportView(textArea);
      }
    }
    {
      JPanel buttonPane = new JPanel();
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      GridBagLayout gbl_buttonPane = new GridBagLayout();
      gbl_buttonPane.columnWidths = new int[] {0, 0, 0, 47, 65, 0};
      gbl_buttonPane.rowHeights = new int[] {23, 0};
      gbl_buttonPane.columnWeights = new double[] {0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
      gbl_buttonPane.rowWeights = new double[] {0.0, Double.MIN_VALUE};
      buttonPane.setLayout(gbl_buttonPane);
      {
        okButton = new JButton("OK");
        okButton.setPreferredSize(new Dimension(65, 23));
        GridBagConstraints gbc_okButton = new GridBagConstraints();
        gbc_okButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_okButton.insets = new Insets(0, 0, 5, 5);
        gbc_okButton.gridx = 3;
        gbc_okButton.gridy = 0;
        buttonPane.add(okButton, gbc_okButton);
      }
      {
        cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("Cancel");
        GridBagConstraints gbc_cancelButton = new GridBagConstraints();
        gbc_cancelButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_cancelButton.insets = new Insets(0, 0, 5, 5);
        gbc_cancelButton.gridx = 4;
        gbc_cancelButton.gridy = 0;
        buttonPane.add(cancelButton, gbc_cancelButton);
      }
    }
  }

  public JTextArea getTextArea() {
    return textArea;
  }

  public JButton getOkButton() {
    return okButton;
  }

  public JButton getCancelButton() {
    return cancelButton;
  }
}
