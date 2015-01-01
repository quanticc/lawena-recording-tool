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

public interface LawenaView {

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

}
