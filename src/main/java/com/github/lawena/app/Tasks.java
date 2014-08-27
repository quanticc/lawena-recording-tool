package com.github.lawena.app;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.lwrt.Lawena;
import com.github.lawena.ui.LawenaView;
import com.github.lawena.update.BuildInfo;
import com.github.lawena.update.UpdateManager;
import com.github.lawena.update.UpdateResult;

public class Tasks {

  private static final Logger log = LoggerFactory.getLogger(Tasks.class);
  private static final java.util.logging.Logger status = java.util.logging.Logger
      .getLogger("status");

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

  public class ClearMoviesTask extends SwingWorker<Void, Path> {

    private int count = 0;
    private List<String> segmentsToDelete;

    public ClearMoviesTask() {}

    public ClearMoviesTask(List<String> segmentsToDelete) {
      this.segmentsToDelete = segmentsToDelete;
    }

    @Override
    protected Void doInBackground() throws Exception {
      SwingUtilities.invokeAndWait(new Runnable() {

        @Override
        public void run() {
          view.getBtnClearMovieFolder().setEnabled(false);
        }
      });
      if (clearMoviesTask == null) {
        String segmentsGlob = "";
        if (segmentsToDelete != null && !segmentsToDelete.isEmpty()) {
          segmentsGlob =
              segmentsToDelete.toString().replace("[", "{").replace("]", "}").replace(" ", "");
          log.info("Deleting segments: " + segmentsGlob);
        } else {
          int answer =
              JOptionPane.showConfirmDialog(view,
                  "Are you sure you want to clear ALL movie files?", "Clearing Movie Files",
                  JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
          if (answer != JOptionPane.YES_NO_OPTION) {
            return null;
          }
        }
        try (DirectoryStream<Path> stream =
            Files.newDirectoryStream(model.getSettings().getMoviePath(), segmentsGlob
                + "*.{tga,wav}")) {

          clearMoviesTask = this;
          setCurrentWorker(this, true);
          SwingUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
              view.getBtnClearMovieFolder().setEnabled(true);
              view.getBtnClearMovieFolder().setText("Stop Clearing");
            }
          });

          for (Path path : stream) {
            if (isCancelled()) {
              break;
            }
            path.toFile().setWritable(true);
            try {
              Files.delete(path);
            } catch (IOException e) {
              log.warn("Could not delete a file", e);
            }
            publish(path);
          }

        } catch (IOException ex) {
          log.warn("Problem while clearing movie folder", ex);
        }
      } else {
        log.info("Cancelling movie folder clearing task");
        status.info("Cancelling task");
        clearMoviesTask.cancel(true);
      }

      return null;
    }

    @Override
    protected void process(List<Path> chunks) {
      count += chunks.size();
      status.info("Deleting " + count + " files from movie folder...");
    };

    @Override
    protected void done() {
      if (!isCancelled()) {
        clearMoviesTask = null;
        setCurrentWorker(null, false);
        if (count > 0) {
          log.info("Movie folder cleared: " + count + " files deleted");
        } else {
          log.info("Movie folder already clean, no files deleted");
        }
        view.getBtnClearMovieFolder().setEnabled(true);
        view.getBtnClearMovieFolder().setText("Clear Movie Files");
        status.info("Ready");
      }
    };

  }

  private MainModel model;
  private Lawena presenter;
  private LawenaView view;

  private ClearMoviesTask clearMoviesTask = null;
  private LaunchTask currentLaunchTask = null;

  public Tasks(Lawena presenter) {
    this.presenter = presenter;
    this.model = presenter.getModel();
    this.view = presenter.getView();
  }

  public MainModel getModel() {
    return model;
  }

  public Lawena getPresenter() {
    return presenter;
  }

  public LawenaView getView() {
    return view;
  }

  public LaunchTask getCurrentLaunchTask() {
    return currentLaunchTask;
  }

  public void setCurrentLaunchTask(LaunchTask currentLaunchTask) {
    this.currentLaunchTask = currentLaunchTask;
  }

  public void setCurrentWorker(final SwingWorker<?, ?> worker, final boolean indeterminate) {
    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        if (worker != null) {
          view.getProgressBar().setVisible(true);
          view.getProgressBar().setIndeterminate(indeterminate);
          view.getProgressBar().setValue(0);
          worker.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
              if ("progress".equals(evt.getPropertyName())) {
                view.getProgressBar().setValue((Integer) evt.getNewValue());
              }
            }
          });
        } else {
          view.getProgressBar().setVisible(false);
          view.getProgressBar().setIndeterminate(indeterminate);
          view.getProgressBar().setValue(0);
        }
      }
    });

  }

}
