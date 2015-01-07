package com.github.lawena.app;

import static com.github.lawena.util.Util.toPath;

import java.awt.Desktop;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
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

import com.github.lawena.Messages;
import com.github.lawena.app.model.MainModel;
import com.github.lawena.app.model.Resources;
import com.github.lawena.app.model.Settings;
import com.github.lawena.app.task.Launcher;
import com.github.lawena.app.task.PreviewGenerator;
import com.github.lawena.profile.Key;
import com.github.lawena.ui.LawenaView;
import com.github.lawena.update.Build;
import com.github.lawena.update.UpdateResult;
import com.github.lawena.update.Updater;
import com.github.lawena.util.StatusAppender;
import com.github.lawena.util.Util;

/**
 * Collection of classes that support the presenter work, mostly {@link SwingWorker} inner classes.
 * 
 * @author Ivan
 *
 */
public class Tasks {

  static final Logger log = LoggerFactory.getLogger(Tasks.class);
  static final Logger status = LoggerFactory.getLogger("status"); //$NON-NLS-1$

  private class UpdaterTask extends SwingWorker<Void, Void> {

    public UpdaterTask() {}

    @Override
    protected Void doInBackground() throws Exception {
      try {
        doRun();
      } catch (Exception e) {
        log.warn("Could not check for updates", e); //$NON-NLS-1$
      }
      return null;
    }

    private void doRun() {
      Updater updater = model.getUpdater();
      if (updater.isStandalone()) {
        log.info("Application running in standalone mode"); //$NON-NLS-1$
        return;
      }
      updater.clear();
      UpdateResult result = updater.checkForUpdates();
      switch (result.getStatus()) {
        case ALREADY_LATEST_VERSION:
          log.info("Latest version already installed"); //$NON-NLS-1$
          break;
        case NO_UPDATES_FOUND:
          log.info("No updates were found: {}", result.getMessage()); //$NON-NLS-1$
          break;
        case UPDATE_AVAILABLE:
          Build details = result.getDetails();
          // TODO: add a "never ask me again" checkbox and a changelog to this dialog
          log.info("New version available: {} ({})", details.getDescribe(), details.getName()); //$NON-NLS-1$
          int answer =
              JOptionPane.showConfirmDialog(presenter.viewAsComponent(),
                  String.format(Messages.getString("Tasks.newVersionFound"), //$NON-NLS-1$
                      updater.getCurrentBranchName(), details.getDescribe(), details.getName()),
                  Messages.getString("Tasks.newVersionFoundTitle"), //$NON-NLS-1$
                  JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
          if (answer == JOptionPane.YES_OPTION) {
            presenter.upgrade(details);
          }
          break;
        default:
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
        String segmentsGlob = ""; //$NON-NLS-1$
        if (segmentsToDelete != null && !segmentsToDelete.isEmpty()) {
          segmentsGlob = segmentsToDelete.toString().replace("[", "{"). //$NON-NLS-1$ //$NON-NLS-2$
              replace("]", "}").replace(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
          log.info("Deleting segments: " + segmentsGlob); //$NON-NLS-1$
        } else {
          int answer =
              JOptionPane
                  .showConfirmDialog(
                      presenter.viewAsComponent(),
                      Messages.getString("Tasks.clearAllSegmentsConfirm"), Messages.getString("Tasks.clearAllSegmentsConfirmTitle"), //$NON-NLS-1$ //$NON-NLS-2$
                      JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
          if (answer != JOptionPane.YES_NO_OPTION) {
            return null;
          }
        }
        Path recPath = toPath(Key.recordingPath.getValue(settings));
        try (DirectoryStream<Path> stream =
            Files.newDirectoryStream(recPath, segmentsGlob + "*.{tga,wav}")) { //$NON-NLS-1$

          clearMoviesTask = this;
          setCurrentWorker(this, true);
          SwingUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
              view.getBtnClearMovieFolder().setEnabled(true);
              view.getBtnClearMovieFolder().setText(
                  Messages.getString("Tasks.buttonStopClearingSegments")); //$NON-NLS-1$
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
              log.warn("Could not delete a file", e); //$NON-NLS-1$
            }
            publish(path);
          }

        } catch (IOException ex) {
          log.warn("Problem while clearing movie folder", ex); //$NON-NLS-1$
        }
      } else {
        log.info("Cancelling movie folder clearing task"); //$NON-NLS-1$
        status.info(Messages.getString("Tasks.cancellingTask")); //$NON-NLS-1$
        clearMoviesTask.cancel(true);
      }
      return null;
    }

    @Override
    protected void process(List<Path> chunks) {
      count += chunks.size();
      status.info(Messages.getString("Tasks.deletingFilesFromSegmentFolder"), count); //$NON-NLS-1$
    }

    @Override
    protected void done() {
      if (!isCancelled()) {
        clearMoviesTask = null;
        setCurrentWorker(null, false);
        if (count > 0) {
          log.info("Movie folder cleared: {} files deleted", count); //$NON-NLS-1$
        } else {
          log.info("Movie folder already clean, no files deleted"); //$NON-NLS-1$
        }
        view.getBtnClearMovieFolder().setEnabled(true);
        view.getBtnClearMovieFolder().setText(Messages.getString("Tasks.buttonClearAllSegments")); //$NON-NLS-1$
        status.info(StatusAppender.OK, Messages.getString("Tasks.ready")); //$NON-NLS-1$
      }
    }

  }

  private class ResourceScanner extends SwingWorker<Void, Void> {

    public ResourceScanner() {}

    @Override
    protected Void doInBackground() throws Exception {
      try {
        scan();
      } catch (Exception e) {
        log.warn("Problem while scanning custom paths", e); //$NON-NLS-1$
      }
      return null;
    }

    private void scan() {
      Path base = settings.getParentDataPath();
      resources.addFolder(base.resolve("default").toFile(), true); //$NON-NLS-1$
      resources.addFolder(base.resolve("custom").toFile(), false); //$NON-NLS-1$
      for (String s : Key.extraFolders.getValue(settings)) {
        // ignore appdir
        if (s.isEmpty())
          continue;
        Path path = Util.toPath(s);
        resources.addFolder(path.toFile(), false);
      }
    }

    @Override
    protected void done() {
      resources.enableFromList(Key.resources.getValue(settings));
      presenter.loadHudComboState();
    }
  }

  private class SkyboxLoader extends SwingWorker<Void, Void> {
    public SkyboxLoader() {}

    @Override
    protected Void doInBackground() throws Exception {
      try {
        presenter.configureSkyboxes(view.getCmbSkybox());
      } catch (Exception e) {
        log.warn("Problem while configuring skyboxes", e); //$NON-NLS-1$
      }
      return null;
    }

    @Override
    protected void done() {
      presenter.selectSkyboxFromSettings();
    }
  }

  private static class DesktopOpenTask extends SwingWorker<Void, Void> {

    private File file;

    public DesktopOpenTask(File file) {
      this.file = file;
    }

    @Override
    protected Void doInBackground() throws Exception {
      try {
        Desktop.getDesktop().open(file);
      } catch (IOException ex) {
        log.warn("Could not open file", ex); //$NON-NLS-1$
      }
      return null;
    }
  }

  MainModel model;
  Lawena presenter;
  LawenaView view;

  Resources resources;
  Settings settings;

  SegmentCleaner clearMoviesTask = null;
  private Launcher currentLaunchTask = null;

  public Tasks(Lawena presenter) {
    this.presenter = presenter;
    this.model = presenter.getModel();
    this.view = presenter.getView();
    this.resources = model.getResources();
    this.settings = model.getSettings();
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

  public Launcher getCurrentLaunchTask() {
    return currentLaunchTask;
  }

  public void setCurrentLaunchTask(Launcher currentLaunchTask) {
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
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
              if ("progress".equals(evt.getPropertyName())) { //$NON-NLS-1$
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

  public static void openFile(File file) {
    new DesktopOpenTask(file).execute();
  }

  public void launch() {
    new Launcher(this).execute();
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

  public void generateSkyboxPreviews(List<String> list) {
    new PreviewGenerator(this, list).execute();
  }
}
