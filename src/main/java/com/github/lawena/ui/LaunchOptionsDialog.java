package com.github.lawena.ui;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.github.lawena.Messages;

public class LaunchOptionsDialog extends JPanel {

  private static final long serialVersionUID = 1L;
  private static final Object[] options = {Messages.getString("LaunchOptionsDialog.OK"), //$NON-NLS-1$
      Messages.getString("LaunchOptionsDialog.setDefaults"), //$NON-NLS-1$
      Messages.getString("LaunchOptionsDialog.cancel")}; //$NON-NLS-1$ 

  private JTextField optionsTextField;

  public LaunchOptionsDialog() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBounds(0, 0, 81, 140);
    add(new JLabel(Messages.getString("LaunchOptionsDialog.text1"))); //$NON-NLS-1$
    add(Box.createRigidArea(new Dimension(0, 5)));
    add(new SwingLink(Messages.getString("LaunchOptionsDialog.developerWikiName"), //$NON-NLS-1$
        "https://developer.valvesoftware.com/wiki/Launch_options")); //$NON-NLS-1$
    this.optionsTextField = new JTextField();
    add(Box.createRigidArea(new Dimension(0, 5)));
    add(optionsTextField);
    add(Box.createRigidArea(new Dimension(0, 5)));
    add(new JLabel(Messages.getString("LaunchOptionsDialog.text2"))); //$NON-NLS-1$
  }

  public int showDialog() {
    return JOptionPane.showOptionDialog(null, this,
        Messages.getString("LaunchOptionsDialog.title"), //$NON-NLS-1$
        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, null);
  }

  public JTextField getOptionsTextField() {
    return optionsTextField;
  }

}
