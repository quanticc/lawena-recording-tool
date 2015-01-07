package com.github.lawena.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLDocument;

import com.github.lawena.Messages;
import com.github.lawena.update.Build;
import com.github.lawena.update.Branch;

public class UpdaterDialog extends JDialog {

  private static final long serialVersionUID = 1L;

  private final JPanel contentPanel = new JPanel();
  private JComboBox<Branch> branchesComboBox;
  private JComboBox<Build> buildsComboBox;
  private JTextPane branchTextPane;
  private JButton okButton;
  private JButton cancelButton;
  private JTextField branchTextField;
  private JTextField buildTextField;
  private JButton refreshButton;
  private JLabel lblLastCheck;
  private JLabel lastCheckLabel;
  private JLabel switchStatusLabel;

  public UpdaterDialog() {
    setIconImage(Toolkit.getDefaultToolkit().getImage(
        UpdaterDialog.class.getResource("/com/github/lawena/ui/fugue/globe-green.png"))); //$NON-NLS-1$
    setTitle(Messages.getString("UpdaterDialog.title")); //$NON-NLS-1$
    setBounds(100, 100, 600, 400);
    BorderLayout borderLayout = new BorderLayout();
    getContentPane().setLayout(borderLayout);
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.NORTH);
    GridBagLayout gbl_contentPanel = new GridBagLayout();
    gbl_contentPanel.columnWidths = new int[] {0, 0, 0, 0, 0, 0};
    gbl_contentPanel.rowHeights = new int[] {0, 0, 0, 0, 0};
    gbl_contentPanel.columnWeights = new double[] {0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
    gbl_contentPanel.rowWeights = new double[] {0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
    contentPanel.setLayout(gbl_contentPanel);
    {
      JLabel lblNewLabel = new JLabel(Messages.getString("UpdaterDialog.description")); //$NON-NLS-1$
      GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
      gbc_lblNewLabel.fill = GridBagConstraints.HORIZONTAL;
      gbc_lblNewLabel.gridwidth = 5;
      gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
      gbc_lblNewLabel.gridx = 0;
      gbc_lblNewLabel.gridy = 0;
      contentPanel.add(lblNewLabel, gbc_lblNewLabel);
    }
    {
      JLabel lblCurrentBranch = new JLabel(Messages.getString("UpdaterDialog.currentBranch")); //$NON-NLS-1$
      GridBagConstraints gbc_lblCurrentBranch = new GridBagConstraints();
      gbc_lblCurrentBranch.anchor = GridBagConstraints.EAST;
      gbc_lblCurrentBranch.insets = new Insets(0, 0, 5, 5);
      gbc_lblCurrentBranch.gridx = 0;
      gbc_lblCurrentBranch.gridy = 1;
      contentPanel.add(lblCurrentBranch, gbc_lblCurrentBranch);
    }
    {
      branchTextField = new JTextField();
      branchTextField.setEditable(false);
      GridBagConstraints gbc_branchTextField = new GridBagConstraints();
      gbc_branchTextField.insets = new Insets(0, 0, 5, 5);
      gbc_branchTextField.fill = GridBagConstraints.HORIZONTAL;
      gbc_branchTextField.gridx = 1;
      gbc_branchTextField.gridy = 1;
      contentPanel.add(branchTextField, gbc_branchTextField);
      branchTextField.setColumns(10);
    }
    {
      JLabel lblVersion_1 = new JLabel(Messages.getString("UpdaterDialog.version")); //$NON-NLS-1$
      GridBagConstraints gbc_lblVersion_1 = new GridBagConstraints();
      gbc_lblVersion_1.anchor = GridBagConstraints.EAST;
      gbc_lblVersion_1.insets = new Insets(0, 0, 5, 5);
      gbc_lblVersion_1.gridx = 2;
      gbc_lblVersion_1.gridy = 1;
      contentPanel.add(lblVersion_1, gbc_lblVersion_1);
    }
    {
      buildTextField = new JTextField();
      buildTextField.setEditable(false);
      GridBagConstraints gbc_buildTextField = new GridBagConstraints();
      gbc_buildTextField.gridwidth = 2;
      gbc_buildTextField.insets = new Insets(0, 0, 5, 0);
      gbc_buildTextField.fill = GridBagConstraints.HORIZONTAL;
      gbc_buildTextField.gridx = 3;
      gbc_buildTextField.gridy = 1;
      contentPanel.add(buildTextField, gbc_buildTextField);
      buildTextField.setColumns(10);
    }
    {
      JLabel lblUpdateBranch = new JLabel(Messages.getString("UpdaterDialog.newBranch")); //$NON-NLS-1$
      GridBagConstraints gbc_lblUpdateBranch = new GridBagConstraints();
      gbc_lblUpdateBranch.insets = new Insets(0, 0, 5, 5);
      gbc_lblUpdateBranch.anchor = GridBagConstraints.EAST;
      gbc_lblUpdateBranch.gridx = 0;
      gbc_lblUpdateBranch.gridy = 2;
      contentPanel.add(lblUpdateBranch, gbc_lblUpdateBranch);
    }
    {
      branchesComboBox = new JComboBox<>();
      GridBagConstraints gbc_branchesComboBox = new GridBagConstraints();
      gbc_branchesComboBox.insets = new Insets(0, 0, 5, 5);
      gbc_branchesComboBox.fill = GridBagConstraints.HORIZONTAL;
      gbc_branchesComboBox.gridx = 1;
      gbc_branchesComboBox.gridy = 2;
      contentPanel.add(branchesComboBox, gbc_branchesComboBox);
    }
    {
      JLabel lblVersion = new JLabel(Messages.getString("UpdaterDialog.version")); //$NON-NLS-1$
      GridBagConstraints gbc_lblVersion = new GridBagConstraints();
      gbc_lblVersion.anchor = GridBagConstraints.EAST;
      gbc_lblVersion.insets = new Insets(0, 0, 5, 5);
      gbc_lblVersion.gridx = 2;
      gbc_lblVersion.gridy = 2;
      contentPanel.add(lblVersion, gbc_lblVersion);
    }
    {
      buildsComboBox = new JComboBox<>();
      GridBagConstraints gbc_buildsComboBox = new GridBagConstraints();
      gbc_buildsComboBox.gridwidth = 2;
      gbc_buildsComboBox.insets = new Insets(0, 0, 5, 0);
      gbc_buildsComboBox.fill = GridBagConstraints.HORIZONTAL;
      gbc_buildsComboBox.gridx = 3;
      gbc_buildsComboBox.gridy = 2;
      contentPanel.add(buildsComboBox, gbc_buildsComboBox);
    }
    {
      lblLastCheck = new JLabel(Messages.getString("UpdaterDialog.lastCheck")); //$NON-NLS-1$
      GridBagConstraints gbc_lblLastCheck = new GridBagConstraints();
      gbc_lblLastCheck.anchor = GridBagConstraints.EAST;
      gbc_lblLastCheck.insets = new Insets(0, 0, 0, 5);
      gbc_lblLastCheck.gridx = 0;
      gbc_lblLastCheck.gridy = 3;
      contentPanel.add(lblLastCheck, gbc_lblLastCheck);
    }
    {
      lastCheckLabel = new JLabel(Messages.getString("UpdaterDialog.never")); //$NON-NLS-1$
      GridBagConstraints gbc_lastCheckLabel = new GridBagConstraints();
      gbc_lastCheckLabel.anchor = GridBagConstraints.WEST;
      gbc_lastCheckLabel.insets = new Insets(0, 0, 0, 5);
      gbc_lastCheckLabel.gridx = 1;
      gbc_lastCheckLabel.gridy = 3;
      contentPanel.add(lastCheckLabel, gbc_lastCheckLabel);
    }
    {
      JPanel panel = new JPanel();
      FlowLayout flowLayout = (FlowLayout) panel.getLayout();
      flowLayout.setAlignment(FlowLayout.RIGHT);
      flowLayout.setVgap(0);
      flowLayout.setHgap(0);
      GridBagConstraints gbc_panel = new GridBagConstraints();
      gbc_panel.gridwidth = 3;
      gbc_panel.fill = GridBagConstraints.BOTH;
      gbc_panel.gridx = 2;
      gbc_panel.gridy = 3;
      contentPanel.add(panel, gbc_panel);
      {
        refreshButton = new JButton(Messages.getString("UpdaterDialog.checkNow")); //$NON-NLS-1$
        panel.add(refreshButton);
      }
    }
    {
      JPanel buttonPane = new JPanel();
      FlowLayout fl_buttonPane = new FlowLayout(FlowLayout.RIGHT);
      buttonPane.setLayout(fl_buttonPane);
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      {
        switchStatusLabel = new JLabel(" "); //$NON-NLS-1$
        switchStatusLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        buttonPane.add(switchStatusLabel);
      }
      {
        okButton = new JButton(Messages.getString("UpdaterDialog.switchUpdateBranch")); //$NON-NLS-1$
        okButton.setActionCommand("OK"); //$NON-NLS-1$
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);
      }
      {
        cancelButton = new JButton(Messages.getString("UpdaterDialog.close")); //$NON-NLS-1$
        cancelButton.setActionCommand("Cancel"); //$NON-NLS-1$
        buttonPane.add(cancelButton);
      }
    }
    {
      JPanel centerPanel = new JPanel();
      getContentPane().add(centerPanel, BorderLayout.CENTER);
      centerPanel.setLayout(new BorderLayout(0, 0));
      {
        JScrollPane scrollPane = new JScrollPane();
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        {
          branchTextPane = new JTextPane();
          branchTextPane.setContentType("text/html"); //$NON-NLS-1$
          branchTextPane.setEditable(false);
          String style = new StringBuilder().append("body { font-family: ") //$NON-NLS-1$
              .append(UIManager.getDefaults().getFont("TextPane.font").getFamily()) //$NON-NLS-1$
              .append("; ").append("font-size: 10pt; text-align: left}").toString(); //$NON-NLS-1$ //$NON-NLS-2$

          ((HTMLDocument) branchTextPane.getDocument()).getStyleSheet().addRule(style);
          scrollPane.setViewportView(branchTextPane);
        }
      }
    }
  }

  public JComboBox<Branch> getBranchesComboBox() {
    return branchesComboBox;
  }

  public JComboBox<Build> getBuildsComboBox() {
    return buildsComboBox;
  }

  public JTextPane getBranchTextPane() {
    return branchTextPane;
  }

  public JButton getOkButton() {
    return okButton;
  }

  public JButton getCancelButton() {
    return cancelButton;
  }

  public JButton getRefreshButton() {
    return refreshButton;
  }

  public JTextField getBranchTextField() {
    return branchTextField;
  }

  public JTextField getBuildTextField() {
    return buildTextField;
  }

  public JLabel getLastCheckLabel() {
    return lastCheckLabel;
  }

  public JLabel getSwitchStatusLabel() {
    return switchStatusLabel;
  }
}
