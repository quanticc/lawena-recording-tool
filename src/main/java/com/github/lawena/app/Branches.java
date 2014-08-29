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
import com.github.lawena.update.BuildInfo;
import com.github.lawena.update.Channel;
import com.github.lawena.update.Updater;

public class Branches {

  private static final Logger log = LoggerFactory.getLogger(Branches.class);

  private Lawena parent;
  private UpdaterDialog view;
  private Updater updater;

  private BuildInfo buildinfo;

  public Branches(Lawena parent) {
    this.parent = parent;
    this.updater = parent.getModel().getUpdater();
    String version = parent.getModel().getFullVersion();
    String buildtime = parent.getModel().getBuildTime();
    this.buildinfo = new BuildInfo(buildtime, version);
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
    JComboBox<Channel> branches = view.getChannelsComboBox();
    // this operation might take a while
    Channel current = updater.getCurrentChannel();
    view.getLastCheckLabel().setText(updater.getLastCheckString());
    view.getChannelTextField().setText(current.getName());
    view.getBuildTextField().setText(buildinfo.toString());
    branches.removeAllItems();
    List<Channel> channels = updater.getChannels();
    for (Channel item : channels) {
      branches.addItem(item);
    }
    branches.setSelectedItem(current);
    refreshBuilds(current);
  }

  private void refreshChangeLog(Channel ch) {
    List<String> lines = updater.getChangeLog(ch);
    JTextPane pane = view.getChannelDataPane();
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
  }

  private void refreshBuilds(Channel ch) {
    log.debug("Refreshing builds for channel {}", ch);
    JComboBox<BuildInfo> builds = view.getBuildsComboBox();
    builds.removeAllItems();
    if (ch.getType() == Channel.Type.SNAPSHOT) {
      for (BuildInfo item : ch.getBuilds()) {
        builds.addItem(item);
      }
      builds.setEnabled(true);
    } else {
      builds.addItem(BuildInfo.LATEST);
      builds.setEnabled(false);
    }
    if (updater.getCurrentChannel().equals(ch)) {
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
        Channel newChannel = (Channel) view.getChannelsComboBox().getSelectedItem();
        BuildInfo newBuild = (BuildInfo) view.getBuildsComboBox().getSelectedItem();
        Channel currentChannel = updater.getCurrentChannel();
        BuildInfo currentBuild = buildinfo;
        if (currentChannel.equals(newChannel) && currentBuild.equals(newBuild)) {
          log.debug("No switch will be done since the same branch & build as current was selected");
          return;
        }
        try {
          updater.switchChannel(newChannel);
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
    view.getChannelsComboBox().addItemListener(new ItemListener() {

      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          Channel selected = (Channel) e.getItem();
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
    view.getChannelDataPane().addHyperlinkListener(new HyperlinkListener() {

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
    Channel selectedChannel = (Channel) view.getChannelsComboBox().getSelectedItem();
    BuildInfo selectedBuild = (BuildInfo) view.getBuildsComboBox().getSelectedItem();
    Channel currentChannel = updater.getCurrentChannel();
    BuildInfo currentBuild = buildinfo;
    if (updater.isStandalone()) {
      view.getOkButton().setEnabled(false);
      view.getSwitchStatusLabel().setText("Standalone builds are not allowed to switch branches");
      view.getSwitchStatusLabel().setIcon(
          new ImageIcon(Branches.class.getResource("/com/github/lawena/ui/fugue/exclamation.png")));
    } else {
      if (selectedChannel.equals(currentChannel) && selectedBuild.equals(currentBuild)) {
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
