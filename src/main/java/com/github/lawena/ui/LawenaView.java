package com.github.lawena.ui;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.WindowListener;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

@SuppressWarnings("nls")
public interface LawenaView {

  public static final String renderingTutorialURL =
      "http://code.google.com/p/lawenarecordingtool/wiki/RenderingTutorial";
  public static final String releasesURL =
      "https://github.com/iabarca/lawena-recording-tool/releases";
  public static final String projectPageURL = "http://code.google.com/p/lawenarecordingtool/";
  public static final String vdmTutorialURL =
      "http://code.google.com/p/lawenarecordingtool/wiki/VDMtutorial";
  public static final String instructionsURL =
      "http://code.google.com/p/lawenarecordingtool/wiki/Instructions";

  public JLabel getLblStatus();

  public void setTitle(String title);

  public void addWindowListener(WindowListener listener);

  public AbstractButton getMntmAbout();

  public AbstractButton getMntmAddCustomSettings();

  public AbstractButton getCheckForUpdatesMenuItem();

  public AbstractButton getSwitchUpdaterBranchMenuItem();

  public AbstractButton getShowLogMenuItem();

  public int getX();

  public int getY();

  public int getWidth();

  public JTable getTableCustomContent();

  public JComboBox<String> getCmbResolution();

  public JLabel getLblResolution();

  public JComboBox<String> getCmbFramerate();

  public JLabel getLblFrameRate();

  public JComboBox<String> getCmbSkybox();

  public AbstractButton getMntmChangeGameDirectory();

  public AbstractButton getMntmChangeMovieDirectory();

  public AbstractButton getMntmRevertToDefault();

  public AbstractButton getMntmExit();

  public AbstractButton getMntmSaveSettings();

  public AbstractButton getBtnStartGame();

  public AbstractButton getBtnClearMovieFolder();

  public AbstractButton getMntmOpenMovieFolder();

  public AbstractButton getMntmOpenCustomFolder();

  public AbstractButton getChckbxmntmInsecure();

  public AbstractButton getMntmLaunchTimeout();

  public AbstractButton getCustomLaunchOptionsMenuItem();

  public JComboBox<String> getCmbViewmodel();

  public JComboBox<String> getCmbSourceVideoFormat();

  public JTabbedPane getTabbedPane();

  public void setVisible(boolean b);

  public JLabel getLblViewmodelFov();

  public JSpinner getSpinnerViewmodelFov();

  public Component getLblJpegQuality();

  public JSpinner getSpinnerJpegQuality();

  public JComboBox<String> getCmbHud();

  public void setIconImage(Image image);

  public JProgressBar getProgressBar();

  public AbstractButton getChckbxmntmBackupMode();
  
  public AbstractButton getCheckRememberGame();

}
