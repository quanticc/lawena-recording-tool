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
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.Messages;
import com.github.lawena.ui.UpdaterDialog;
import com.github.lawena.update.Branch;
import com.github.lawena.update.Build;
import com.github.lawena.update.Updater;

public class Branches {

  static final Logger log = LoggerFactory.getLogger(Branches.class);

  Lawena parent;
  UpdaterDialog view;
  Updater updater;

  Build buildinfo;

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

  void refresh() {
    log.debug("Refreshing branches dialog data"); //$NON-NLS-1$
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

  void refreshChangeLog(Branch ch) {
    List<String> lines = Updater.getChangeLog(ch);
    JTextPane pane = view.getBranchTextPane();
    HTMLDocument doc = (HTMLDocument) pane.getDocument();
    try {
      doc.remove(0, doc.getLength());
    } catch (BadLocationException e) {
      log.warn("Could not clear text pane: {}", e.toString()); //$NON-NLS-1$
    }
    HTMLEditorKit editorKit = (HTMLEditorKit) pane.getEditorKit();
    for (String line : lines) {
      try {
        editorKit.insertHTML(doc, doc.getLength(), line, 0, 0, null);
      } catch (BadLocationException | IOException e) {
        log.warn("Could not insert text to text pane: {}", e.toString()); //$NON-NLS-1$
      }
    }
    pane.setCaretPosition(0);
  }

  void refreshBuilds(Branch ch) {
    log.debug("Refreshing builds for branch {}", ch); //$NON-NLS-1$
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
    log.debug("Initializing branches dialog"); //$NON-NLS-1$
    view = new UpdaterDialog();
    view.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    view.setLocationRelativeTo(parent.viewAsComponent());
    view.setModalityType(ModalityType.APPLICATION_MODAL);
    view.getOkButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        Branch newBranch = (Branch) view.getBranchesComboBox().getSelectedItem();
        Build newBuild = (Build) view.getBuildsComboBox().getSelectedItem();
        Branch currentBranch = updater.getCurrentBranch();
        Build currentBuild = buildinfo;
        if (currentBranch.equals(newBranch) && currentBuild.equals(newBuild)) {
          log.debug("No switch will be done since the same branch & build as current was selected"); //$NON-NLS-1$
          return;
        }
        try {
          Updater.switchBranch(newBranch);
          parent.upgrade(newBuild);
        } catch (IOException ex) {
          log.info("Could not switch update branches", ex); //$NON-NLS-1$
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
                log.warn("Could not open URL", ex); //$NON-NLS-1$
              }
              return null;
            }
          }.execute();
        }
      }
    });
  }

  void checkValidSwitchState() {
    Branch selectedBranch = (Branch) view.getBranchesComboBox().getSelectedItem();
    Build selectedBuild = (Build) view.getBuildsComboBox().getSelectedItem();
    Branch currentBranch = updater.getCurrentBranch();
    Build currentBuild = buildinfo;
    if (updater.isStandalone()) {
      view.getOkButton().setEnabled(false);
      view.getSwitchStatusLabel().setText(
          Messages.getString("Branches.standaloneBuildsBranchSwitchNotice")); //$NON-NLS-1$
      view.getSwitchStatusLabel().setIcon(
          new ImageIcon(Branches.class.getResource("/com/github/lawena/ui/fugue/exclamation.png"))); //$NON-NLS-1$
    } else {
      if (selectedBranch.equals(currentBranch) && selectedBuild.equals(currentBuild)) {
        view.getOkButton().setEnabled(false);
        view.getSwitchStatusLabel().setText(Messages.getString("Branches.sameBranchVersionNotice")); //$NON-NLS-1$
        view.getSwitchStatusLabel()
            .setIcon(
                new ImageIcon(Branches.class
                    .getResource("/com/github/lawena/ui/fugue/exclamation.png"))); //$NON-NLS-1$
      } else {
        view.getOkButton().setEnabled(true);
        view.getSwitchStatusLabel().setText(" "); //$NON-NLS-1$
        view.getSwitchStatusLabel().setIcon(null);
      }
    }

  }

}
