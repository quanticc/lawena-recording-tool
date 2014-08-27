package com.github.lawena.app;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.lwrt.Lawena;
import com.github.lawena.ui.LawenaView;
import com.github.lawena.update.UpdateManager;
import com.github.lawena.update.UpdateResult;
import com.github.lawena.update.BuildInfo;

public class Tasks {

  private static final Logger log = LoggerFactory.getLogger(Tasks.class);

  public class UpdaterTask extends SwingWorker<Void, Void> {

    protected Void doInBackground() throws Exception {
      try {
        doRun();
      } catch (Exception e) {
        log.warn("Task could not be completed", e);
      }
      return null;
    }

    private void doRun() {
      UpdateManager updater = model.getUpdater();
      updater.cleanup();
      UpdateResult result = updater.checkForUpdates(false);
      switch (result.getStatus()) {
        case ALREADY_LATEST_VERSION:
          log.info("Latest version already installed");
          break;
        case NO_UPDATES_FOUND:
          log.info("No updates were found: {}", result.getMessage());
          break;
        case UPDATE_AVAILABLE:
          BuildInfo details = result.getDetails();
          log.info("New version available: {} ({})", details.getDescribe(), details.getName());
          int answer =
              JOptionPane.showConfirmDialog(view, "Do you want to update to the latest version?",
                  "New version " + details.getDescribe() + " available", JOptionPane.YES_NO_OPTION,
                  JOptionPane.QUESTION_MESSAGE);
          if (answer == JOptionPane.YES_OPTION) {
            if (updater.upgradeApplication(details)) {
              log.info("Upgrade in progress..");
            } else {
              log.info("Upgrade could not be completed, please retry or restart the application");
            }
          }
          break;
      }
    }
  }

  private MainModel model;
  private Lawena presenter;
  private LawenaView view;

  public Tasks(Lawena presenter) {
    this.presenter = presenter;
    this.model = presenter.getModel();
    this.view = presenter.getView();
  }

}
