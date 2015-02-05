package com.github.lawena.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

import com.github.lawena.Messages;
import java.awt.Toolkit;

public class GameSelectDialog extends JDialog {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  private final JPanel contentPanel = new JPanel();
  private JButton okButton;
  private JRadioButton radioTf;
  private JRadioButton radioGo;
  private JCheckBox checkRemember;
  private ButtonGroup gameGroup;

  /**
   * Create the dialog.
   */
  public GameSelectDialog() {
    setIconImage(Toolkit.getDefaultToolkit().getImage(GameSelectDialog.class.getResource("/com/github/lawena/app/cap-48px.png")));
    setTitle(Messages.getString("GameSelectDialog.title")); //$NON-NLS-1$
    setBounds(100, 100, 344, 190);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new MigLayout("", "[]", "[][][][][]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    {
      JLabel labelSelectGame = new JLabel(Messages.getString("GameSelectDialog.instructions")); //$NON-NLS-1$
      contentPanel.add(labelSelectGame, "cell 0 0"); //$NON-NLS-1$
    }
    {
      radioTf = new JRadioButton("Team Fortress 2"); //$NON-NLS-1$
      contentPanel.add(radioTf, "cell 0 1"); //$NON-NLS-1$
    }
    {
      radioGo = new JRadioButton("Counter-Strike: Global Offensive"); //$NON-NLS-1$
      contentPanel.add(radioGo, "cell 0 2"); //$NON-NLS-1$
    }
    {
      checkRemember = new JCheckBox(Messages.getString("GameSelectDialog.rememberChoice")); //$NON-NLS-1$
      contentPanel.add(checkRemember, "cell 0 4"); //$NON-NLS-1$
    }
    {
      JPanel buttonPane = new JPanel();
      buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      {
        okButton = new JButton(Messages.getString("GameSelectDialog.startLawena")); //$NON-NLS-1$
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);
      }
    }

    radioTf.setActionCommand("tf"); //$NON-NLS-1$
    radioGo.setActionCommand("go"); //$NON-NLS-1$
    gameGroup = new ButtonGroup();
    gameGroup.add(radioTf);
    gameGroup.add(radioGo);
    gameGroup.clearSelection();
  }

  public JButton getOkButton() {
    return okButton;
  }

  public JCheckBox getCheckRemember() {
    return checkRemember;
  }

  public ButtonGroup getGameGroup() {
    return gameGroup;
  }
}
