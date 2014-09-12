package com.github.lawena.app.task;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.app.Lawena;
import com.github.lawena.app.Tasks;
import com.github.lawena.app.model.Linker;
import com.github.lawena.app.model.MainModel;
import com.github.lawena.app.model.Resource;
import com.github.lawena.app.model.Resources;
import com.github.lawena.model.LwrtMovies;
import com.github.lawena.model.LwrtSettings;
import com.github.lawena.model.LwrtSettings.Key;
import com.github.lawena.os.OSInterface;
import com.github.lawena.ui.LawenaView;
import com.github.lawena.util.LawenaException;
import com.github.lawena.util.StatusAppender;
import com.github.lawena.util.Util;

public class Launcher extends SwingWorker<Boolean, Void> {

  private static final Logger log = LoggerFactory.getLogger(Launcher.class);
  private static final Logger status = LoggerFactory.getLogger("status");

  private Tasks tasks;
  private MainModel model;
  private LawenaView view;
  private Lawena presenter;

  private OSInterface os;
  private LwrtSettings settings;
  private LwrtMovies movies;
  private Resources resources;
  private Linker linker;

  public Launcher(Tasks tasks) {
    this.tasks = tasks;
    this.model = tasks.getModel();
    this.view = tasks.getView();
    this.presenter = tasks.getPresenter();

    this.os = model.getOsInterface();
    this.settings = model.getSettings();
    this.movies = model.getMovies();
    this.resources = model.getResources();
    this.linker = model.getLinker();
  }

  @Override
  protected Boolean doInBackground() throws Exception {
    SwingUtilities.invokeAndWait(new Runnable() {

      @Override
      public void run() {
        view.getBtnStartTf().setEnabled(false);
      }
    });
    if (tasks.getCurrentLaunchTask() == null) {
      tasks.setCurrentLaunchTask(this);
      tasks.setCurrentWorker(this, false);
      // linker.setParentTask(this);
      setProgress(0);

      // Checking if the user selects "Custom" HUD in the dropdown,
      // he or she also selects a "hud" in the sidebar
      if (!verifyCustomHud()) {
        JOptionPane.showMessageDialog(view,
            "Please select a custom HUD in the\nCustom Resources table and retry", "Custom HUD",
            JOptionPane.INFORMATION_MESSAGE);
        log.info("Launch aborted because the custom HUD to use was not specified");
        return false;
      }

      // Check for big custom folders, mitigate OOM errors with custom folder > 2 GB
      Path tfpath = settings.getTfPath();
      Path customPath = tfpath.resolve("custom");
      Path configPath = tfpath.resolve("cfg");
      try {
        long bytes = Util.sizeOfPath(configPath) + Util.sizeOfPath(customPath);
        String size = Util.humanReadableByteCount(bytes, true);
        log.info("Config and Custom folders size: " + size);
        if (bytes / 1024 / 1024 > settings.getInt(Key.BigFolderMBThreshold)) {
          int answer =
              JOptionPane
                  .showConfirmDialog(
                      null,
                      "Your cfg and custom folders are "
                          + size
                          + " in size."
                          + "\nPlease consider moving unnecesary custom files like maps to tf/download folder."
                          + "\nDo you still want to proceed?", "Copying Folders before Launch",
                      JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
          if (answer == JOptionPane.NO_OPTION || answer == JOptionPane.CLOSED_OPTION) {
            log.info("Launch aborted by the user");
            status.info("Launch aborted by the user");
            return false;
          }
        }
      } catch (IOException e) {
        log.info("Could not determine folder size: " + e);
      }

      setProgress(5);
      closeTf2Handles();
      setProgress(15);

      // Restoring user files
      status.info("Restoring your files");
      linker.unlink();
      setProgress(40);

      // Saving ui settings to cfg files
      status.info("Saving settings and generating cfg files");
      try {
        presenter.saveSettings();
        if (model.getDemos().isAutoplay()) {
          String demoname = model.getDemos().getDemoname();
          if (demoname != null && !demoname.isEmpty()) {
            settings.setDemoname(demoname);
          }
        }
        settings.saveToCfg();
        movies.createMovienameCfgs();
      } catch (IOException e) {
        log.warn("Problem while saving settings to file", e);
        status.info(StatusAppender.ERROR, "Failed to save lawena settings to file");
        return false;
      }
      // Allow failing this without cancelling launch, notify user
      // See https://github.com/iabarca/lawena-recording-tool/issues/36
      try {
        movies.movieOffset();
      } catch (IOException e) {
        log.info("Could not detect current movie slot");
      }

      setProgress(50);

      // Backing up user files and copying lawena files
      status.info("Copying lawena files to cfg and custom...");
      try {
        linker.link();
      } catch (LawenaException e) {
        status.info(StatusAppender.ERROR, e.getMessage());
        return false;
      }
      setProgress(95);

      // Launching process
      status.info("Launching TF2 process");
      os.startTf(settings);

      SwingUtilities.invokeAndWait(new Runnable() {

        @Override
        public void run() {
          view.getBtnStartTf().setEnabled(true);
          view.getBtnStartTf().setText("Stop Team Fortress 2");
        }
      });
      setProgress(100);

      int timeout = 0;
      int cfgtimeout = settings.getLaunchTimeout();
      int millis = 5000;
      int maxtimeout = cfgtimeout / (millis / 1000);
      setProgress(0);
      status.info("Waiting for TF2 to start...");
      if (cfgtimeout > 0) {
        log.debug("TF2 launch timeout: around " + cfgtimeout + " seconds");
      } else {
        log.debug("TF2 launch timeout disabled");
      }
      while (!os.isRunningTF2() && (cfgtimeout == 0 || timeout < maxtimeout)) {
        ++timeout;
        if (cfgtimeout > 0) {
          setProgress((int) ((double) timeout / maxtimeout * 100));
        }
        Thread.sleep(millis);
      }

      if (cfgtimeout > 0 && timeout >= maxtimeout) {
        int s = timeout * (millis / 1000);
        log.info("TF2 launch timed out after " + s + " seconds");
        status.info(StatusAppender.WARN, "TF2 did not start after " + s + " seconds");
        return false;
      }

      log.info("TF2 has started running");
      status.info("Waiting for TF2 to finish running...");
      SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
          view.getProgressBar().setIndeterminate(true);
        }
      });
      while (os.isRunningTF2()) {
        Thread.sleep(millis);
      }

      Thread.sleep(5000);
      closeTf2Handles();

    } else {
      if (os.isRunningTF2()) {
        status.info("Attempting to finish TF2 process...");
        os.killTf2Process();
        Thread.sleep(5000);
      } else {
        status.info("TF2 was not running, cancelling");
      }
      if (!os.isRunningTF2()) {
        tasks.getCurrentLaunchTask().cancel(true);
      }
      closeTf2Handles();
    }

    return true;
  }

  private void closeTf2Handles() {
    status.info("Closing open handles in TF2 'cfg' folder...");
    os.closeHandles(settings.getTfPath().resolve("cfg"));
    status.info("Closing open handles in TF2 'custom' folder...");
    os.closeHandles(settings.getTfPath().resolve("custom"));
  }

  private boolean verifyCustomHud() {
    if (view.getCmbHud().getSelectedItem().equals("Custom")) {
      for (Resource resource : resources.getResourceList()) {
        if (resource.isEnabled()) {
          try {
            resources.updateTags(resource);
            if (resource.getTags().contains(Resource.HUD)) {
              log.debug("Custom HUD verified with {}", resource);
              return true;
            }
          } catch (IOException e) {
            log.warn("Could not determine resource tags", e);
          }
        }
      }
      return false;
    } else {
      return true;
    }
  }

  @Override
  protected void done() {
    if (!isCancelled()) {
      tasks.setCurrentLaunchTask(null);
      tasks.setCurrentWorker(null, false);
      // linker.setParentTask(null);
      view.getBtnStartTf().setEnabled(false);
      boolean ranTf2Correctly = false;
      try {
        ranTf2Correctly = get();
      } catch (InterruptedException | ExecutionException e) {
      }
      boolean restoredAllFiles = linker.unlink();
      if (ranTf2Correctly) {
        if (restoredAllFiles) {
          status.info(StatusAppender.OK, "TF2 has finished running. All files restored");
        } else {
          status.info(StatusAppender.WARN,
              "Your files could not be restored correctly. Check log for details");
        }
      }
      os.setSystemDxLevel(model.getOriginalDxLevel());
      view.getBtnStartTf().setText("Start Team Fortress 2");
      view.getBtnStartTf().setEnabled(true);
    }
  }

}
