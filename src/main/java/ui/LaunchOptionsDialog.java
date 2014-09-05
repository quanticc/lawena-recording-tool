package ui;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LaunchOptionsDialog extends JPanel {

  private static final long serialVersionUID = 1L;
  private static final Object[] options = {"OK", "Set defaults", "Cancel"};

  private JTextField optionsTextField;

  public LaunchOptionsDialog() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBounds(0, 0, 81, 140);
    add(new JLabel(
        "<html>Enter the list of custom launch options to use on game launch."
            + "<br>Steam AppID, Resolution and DxLevel are provided by Lawena in case they are not defined here."
            + "<br>These options are for advanced users only and can override settings from the main window."));
    add(Box.createRigidArea(new Dimension(0, 5)));
    add(new SwingLink("Launch Options Valve Developer Wiki",
        "https://developer.valvesoftware.com/wiki/Launch_options"));
    this.optionsTextField = new JTextField();
    add(Box.createRigidArea(new Dimension(0, 5)));
    add(optionsTextField);
    add(Box.createRigidArea(new Dimension(0, 5)));
    add(new JLabel(
        "Press OK to use above values or Set Defaults to use the standard launch options used by Lawena."));
  }

  public int showDialog() {
    return JOptionPane.showOptionDialog(null, this, "Custom Launch Options",
        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, null);
  }
  
  public JTextField getOptionsTextField() {
    return optionsTextField;
  }

}
