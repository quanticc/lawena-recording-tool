package com.github.lawena.app;

import java.awt.Desktop;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.ui.UpdaterDialog;
import com.github.lawena.update.Build;
import com.github.lawena.update.Branch;
import com.github.lawena.update.Updater;

public class Branches {

  private static final Logger log = LoggerFactory.getLogger(Branches.class);

  private Lawena parent;
  private UpdaterDialog view;
  private Updater updater;

  private Build buildinfo;

  public Branches(Lawena parent) {
    this.parent = parent;
    this.updater = parent.getModel().getUpdater();
    String version = parent.getModel().getFullVersion();
    String buildtime = parent.getModel().getBuildTime();
    this.buildinfo = new Build(buildtime, version);
  }

  public void start() {
    if (view == null) {
      init();
    }
    refresh();
    view.setVisible(true);
  }

  private void refresh() {
    log.debug("Refreshing branches dialog data");
    JComboBox<Branch> branchCombo = view.getBranchesComboBox();
    // this operation might take a while
    Branch current = updater.getCurrentBranch();
    view.getLastCheckLabel().setText(updater.getLastCheckString());
    view.getBranchTextField().setText(current.getName());
    view.getBuildTextField().setText(buildinfo.toString());
    branchCombo.removeAllItems();
    List<Branch> branchList = updater.getBranches();
    for (Branch item : branchList) {
      branchCombo.addItem(item);
    }
    branchCombo.setSelectedItem(current);
    refreshBuilds(current);
  }

  private void refreshChangeLog(Branch ch) {
    List<String> lines = updater.getChangeLog(ch);
    JTextPane pane = view.getBranchTextPane();
    HTMLDocument doc = (HTMLDocument) pane.getDocument();
    try {
      doc.remove(0, doc.getLength());
    } catch (BadLocationException e) {
      log.warn("Could not clear text pane: " + e);
    }
    HTMLEditorKit editorKit = (HTMLEditorKit) pane.getEditorKit();
    for (String line : lines) {
      try {
        editorKit.insertHTML(doc, doc.getLength(), line, 0, 0, null);
      } catch (BadLocationException | IOException e) {
        log.warn("Could not insert text to text pane: " + e);
      }
    }
    pane.setCaretPosition(0);
  }

  private void refreshBuilds(Branch ch) {
    log.debug("Refreshing builds for branch {}", ch);
    JComboBox<Build> builds = view.getBuildsComboBox();
    builds.removeAllItems();
    if (ch.getType() == Branch.Type.SNAPSHOT) {
      for (Build item : ch.getBuilds()) {
        builds.addItem(item);
      }
      builds.setEnabled(true);
    } else {
      builds.addItem(Build.LATEST);
      builds.setEnabled(false);
    }
    if (updater.getCurrentBranch().equals(ch)) {
      builds.setSelectedItem(buildinfo);
    }
  }

  private void init() {
    log.debug("Initializing branches dialog");
    view = new UpdaterDialog();
    view.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    view.setLocationRelativeTo(parent.getView());
    view.setModalityType(ModalityType.APPLICATION_MODAL);
    view.getOkButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        Branch newBranch = (Branch) view.getBranchesComboBox().getSelectedItem();
        Build newBuild = (Build) view.getBuildsComboBox().getSelectedItem();
        Branch currentBranch = updater.getCurrentBranch();
        Build currentBuild = buildinfo;
        if (currentBranch.equals(newBranch) && currentBuild.equals(newBuild)) {
          log.debug("No switch will be done since the same branch & build as current was selected");
          return;
        }
        try {
          updater.switchBranch(newBranch);
          parent.upgrade(newBuild);
        } catch (IOException ex) {
          log.info("Could not switch update branches", ex);
        }
        view.setVisible(false);
      }
    });
    view.getCancelButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        view.setVisible(false);
      }
    });
    view.getRefreshButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        updater.clear();
        refresh();
      }
    });
    view.getBranchesComboBox().addItemListener(new ItemListener() {

      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          Branch selected = (Branch) e.getItem();
          refreshChangeLog(selected);
          refreshBuilds(selected);
          checkValidSwitchState();
        }
      }
    });
    view.getBuildsComboBox().addItemListener(new ItemListener() {

      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          checkValidSwitchState();
        }
      }
    });
    view.getBranchTextPane().addHyperlinkListener(new HyperlinkListener() {

      @Override
      public void hyperlinkUpdate(final HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
              try {
                Desktop.getDesktop().browse(e.getURL().toURI());
              } catch (IOException | URISyntaxException ex) {
                log.warn("Could not open URL", ex);
              }
              return null;
            }
          }.execute();
        }
      }
    });
  }

  private void checkValidSwitchState() {
    Branch selectedBranch = (Branch) view.getBranchesComboBox().getSelectedItem();
    Build selectedBuild = (Build) view.getBuildsComboBox().getSelectedItem();
    Branch currentBranch = updater.getCurrentBranch();
    Build currentBuild = buildinfo;
    if (updater.isStandalone()) {
      view.getOkButton().setEnabled(false);
      view.getSwitchStatusLabel().setText("Standalone builds are not allowed to switch branches");
      view.getSwitchStatusLabel().setIcon(
          new ImageIcon(Branches.class.getResource("/com/github/lawena/ui/fugue/exclamation.png")));
    } else {
      if (selectedBranch.equals(currentBranch) && selectedBuild.equals(currentBuild)) {
        view.getOkButton().setEnabled(false);
        view.getSwitchStatusLabel().setText("You are currently in this same branch and version");
        view.getSwitchStatusLabel()
            .setIcon(
                new ImageIcon(Branches.class
                    .getResource("/com/github/lawena/ui/fugue/exclamation.png")));
      } else {
        view.getOkButton().setEnabled(true);
        view.getSwitchStatusLabel().setText(" ");
        view.getSwitchStatusLabel().setIcon(null);
      }
    }

  }

}
