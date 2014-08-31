package com.github.lawena.app;

import java.awt.Desktop;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import com.github.lawena.app.task.Launch;
import com.github.lawena.app.task.PreviewGenerator;
import com.github.lawena.model.LwrtFiles;
import com.github.lawena.model.LwrtResources;
import com.github.lawena.model.LwrtSettings;
import com.github.lawena.model.MainModel;
import com.github.lawena.ui.LawenaView;
import com.github.lawena.update.Build;
import com.github.lawena.update.UpdateResult;
import com.github.lawena.update.Updater;

public class Tasks {

  private static final Logger log = LoggerFactory.getLogger(Tasks.class);
  private static final Logger status = LoggerFactory.getLogger("status");

  private class UpdaterTask extends SwingWorker<Void, Void> {

    protected Void doInBackground() throws Exception {
      try {
        doRun();
      } catch (Exception e) {
        log.warn("Could not check for updates", e);
      }
      return null;
    }

    private void doRun() {
      Updater updater = model.getUpdater();
      if (updater.isStandalone()) {
        log.info("Application running in standalone mode");
        return;
      }
      updater.clear();
      UpdateResult result = updater.checkForUpdates();
      switch (result.getStatus()) {
        case ALREADY_LATEST_VERSION:
          log.info("Latest version already installed");
          break;
        case NO_UPDATES_FOUND:
          log.info("No updates were found: {}", result.getMessage());
          break;
        case UPDATE_AVAILABLE:
          Build details = result.getDetails();
          // TODO: add a "never ask me again" checkbox and a changelog to this dialog
          log.info("New version available: {} ({})", details.getDescribe(), details.getName());
          int answer =
              JOptionPane.showConfirmDialog(view,
                  "New version found!\nBranch: " + updater.getCurrentBranchName() + "\nVersion: "
                      + details.getDescribe() + "\nBuild: " + details.getName()
                      + "\n\nDo you want to update to the latest version?"
                      + "\nApplication will be restarted", "New version available",
                  JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
          if (answer == JOptionPane.YES_OPTION) {
            presenter.upgrade(details);
          }
          break;
      }
    }
  }

  private class SegmentCleaner extends SwingWorker<Void, Path> {

    private int count = 0;
    private List<String> segmentsToDelete;

    public SegmentCleaner(List<String> segmentsToDelete) {
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
        status.info(MarkerFactory.getMarker("OK"), "Ready");
      }
    };

  }

  private class ResourceScanner extends SwingWorker<Void, Void> {
    @Override
    protected Void doInBackground() throws Exception {
      try {
        scan();
        watcher.start();
      } catch (Exception e) {
        log.warn("Problem while scanning custom paths", e);
      }
      return null;
    }

    private void scan() {
      resources.clear();
      resources.addPaths(Paths.get("custom"));
      resources.addPaths(settings.getTfPath().resolve("custom"));
      resources.validateRequired();
    }

    @Override
    protected void done() {
      resources.loadResourceSettings();
      presenter.loadHudComboState();
    }
  }

  private class ResourceCopyTask extends SwingWorker<Boolean, Void> {

    private Path from;

    public ResourceCopyTask(Path from) {
      this.from = from;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
      status.info("Copying " + from + " into lawena custom folder...");
      return files.copyToCustom(from);
    }

    @Override
    protected void done() {
      boolean result = false;
      try {
        result = get();
      } catch (CancellationException | InterruptedException | ExecutionException e) {
        log.warn("Custom path copy task was cancelled", e);
      }
      if (!result) {
        try {
          resources.addPath(from);
          log.info(from + " added to custom resource list");
        } catch (IOException e) {
          log.warn("Problem while loading a custom path", e);
        }
      } else {
        log.info(from + " copied to custom resource folder");
      }
      status.info(from.getFileName() + " was added"
          + (result ? " to lawena custom folder" : " to custom resource list"));
    }

  }

  private class SkyboxLoader extends SwingWorker<Void, Void> {
    @Override
    protected Void doInBackground() throws Exception {
      try {
        
        presenter.configureSkyboxes(view.getCmbSkybox());
      } catch (Exception e) {
        log.warn("Problem while configuring skyboxes", e);
      }
      return null;
    }

    @Override
    protected void done() {
      presenter.selectSkyboxFromSettings();
    };
  }

  private class DesktopOpenTask extends SwingWorker<Void, Void> {

    private File file;

    public DesktopOpenTask(File file) {
      this.file = file;
    }

    @Override
    protected Void doInBackground() throws Exception {
      try {
        Desktop.getDesktop().open(file);
      } catch (IOException ex) {
        log.warn("Could not open file", ex);
      }
      return null;
    }
  }

  private MainModel model;
  private Lawena presenter;
  private LawenaView view;

  private Thread watcher;
  private LwrtResources resources;
  private LwrtSettings settings;
  private LwrtFiles files;

  private SegmentCleaner clearMoviesTask = null;
  private Launch currentLaunchTask = null;

  public Tasks(Lawena presenter) {
    this.presenter = presenter;
    this.model = presenter.getModel();
    this.view = presenter.getView();
    this.watcher = model.getWatcher();
    this.resources = model.getResources();
    this.settings = model.getSettings();
    this.files = model.getFiles();
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

  public Launch getCurrentLaunchTask() {
    return currentLaunchTask;
  }

  public void setCurrentLaunchTask(Launch currentLaunchTask) {
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

  public void openFile(File file) {
    new DesktopOpenTask(file).execute();
  }

  public void launch() {
    new Launch(this).execute();
  }

  public void checkForUpdates() {
    new UpdaterTask().execute();
  }

  public void cleanSegments(List<String> selected) {
    new SegmentCleaner(selected).execute();
  }

  public void scanResources() {
    new ResourceScanner().execute();
  }

  public void loadSkyboxes() {
    new SkyboxLoader().execute();
  }

  public void copyResourcesFrom(Path path) {
    new ResourceCopyTask(path).execute();
  }

  public void generateSkyboxPreviews(List<String> list) {
    new PreviewGenerator(this, list).execute();
  }
}
