package com.github.lawena.app.task;

import static com.github.lawena.util.Util.toPath;

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
import com.github.lawena.app.model.ConfigWriter;
import com.github.lawena.app.model.Linker;
import com.github.lawena.app.model.MainModel;
import com.github.lawena.app.model.Resource;
import com.github.lawena.app.model.Resources;
import com.github.lawena.app.model.Settings;
import com.github.lawena.os.OSInterface;
import com.github.lawena.profile.Key;
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
  private Settings settings;
  private Resources resources;
  private Linker linker;
  private ConfigWriter writer;

  public Launcher(Tasks tasks) {
    this.tasks = tasks;
    this.model = tasks.getModel();
    this.view = tasks.getView();
    this.presenter = tasks.getPresenter();

    this.os = model.getOsInterface();
    this.settings = model.getSettings();
    this.resources = model.getResources();
    this.linker = model.getLinker();
    this.writer = presenter.newConfigWriter();
  }

  @Override
  protected Boolean doInBackground() throws Exception {
    SwingUtilities.invokeAndWait(new Runnable() {

      @Override
      public void run() {
        view.getBtnStartGame().setEnabled(false);
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
        JOptionPane.showMessageDialog(presenter.viewAsComponent(),
            "Please select a custom HUD in the\nCustom Resources table and retry", "Custom HUD",
            JOptionPane.INFORMATION_MESSAGE);
        log.info("Launch aborted because the custom HUD to use was not specified");
        return false;
      }

      // Check for big custom folders, mitigate OOM errors with custom folder > 2 GB
      Path tfpath = toPath(Key.gamePath.getValue(settings));
      Path customPath = tfpath.resolve("custom");
      Path configPath = tfpath.resolve("cfg");
      boolean ok =
          Util.notifyBigFolder(Key.bigFolderThreshold.getValue(settings), configPath, customPath);
      if (!ok) {
        log.info("Launch aborted by the user");
        status.info("Launch aborted by the user");
        return false;
      }

      setProgress(5);
      closeGameHandles();
      setProgress(15);

      // Restoring user files
      status.info("Restoring your files");
      linker.unlink();
      setProgress(40);

      // Saving ui settings to cfg files
      status.info("Saving settings and generating cfg files");
      try {
        presenter.saveSettings();
        writer.writeAll();
        model.getDemos().writeAutoplay();
      } catch (IOException e) {
        log.warn("Problem while saving settings to file", e);
        status.info(StatusAppender.ERROR, "Failed to save lawena settings to file");
        return false;
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
      status.info("Launching game process");
      os.launchSteam(settings);

      SwingUtilities.invokeAndWait(new Runnable() {

        @Override
        public void run() {
          view.getBtnStartGame().setEnabled(true);
          view.getBtnStartGame().setText("Stop Game");
        }
      });
      setProgress(100);

      int timeout = 0;
      int cfgtimeout = Key.launchTimeout.getValue(settings);
      int millis = 5000;
      int maxtimeout = cfgtimeout / (millis / 1000);
      setProgress(0);
      status.info("Waiting for the game to start...");
      if (cfgtimeout > 0) {
        log.debug("Game launch timeout: around " + cfgtimeout + " seconds");
      } else {
        log.debug("Game launch timeout disabled");
      }
      while (!os.isGameRunning() && (cfgtimeout == 0 || timeout < maxtimeout)) {
        ++timeout;
        if (cfgtimeout > 0) {
          setProgress((int) ((double) timeout / maxtimeout * 100));
        }
        Thread.sleep(millis);
      }

      if (cfgtimeout > 0 && timeout >= maxtimeout) {
        int s = timeout * (millis / 1000);
        log.info("Game launch timed out after " + s + " seconds");
        status.info(StatusAppender.WARN, "Game did not start after " + s + " seconds");
        return false;
      }

      log.info("Game has started running");
      status.info("Waiting for the game to finish running...");
      SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
          view.getProgressBar().setIndeterminate(true);
        }
      });
      while (os.isGameRunning()) {
        Thread.sleep(millis);
      }

      Thread.sleep(5000);
      closeGameHandles();

    } else {
      if (os.isGameRunning()) {
        status.info("Attempting to finish game process...");
        os.killTf2Process();
        Thread.sleep(5000);
      } else {
        status.info("Game was not running, cancelling");
      }
      if (!os.isGameRunning()) {
        tasks.getCurrentLaunchTask().cancel(true);
      }
      closeGameHandles();
    }

    return true;
  }

  private void closeGameHandles() {
    status.info("Closing open handles in game 'cfg' folder...");
    os.closeHandles(toPath(Key.gamePath.getValue(settings)).resolve("cfg"));
    status.info("Closing open handles in game 'custom' folder...");
    os.closeHandles(toPath(Key.gamePath.getValue(settings)).resolve("custom"));
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
      view.getBtnStartGame().setEnabled(false);
      boolean ranGameCorrectly = false;
      try {
        ranGameCorrectly = get();
      } catch (InterruptedException | ExecutionException e) {
      }
      boolean restoredAllFiles = linker.unlink();
      if (ranGameCorrectly) {
        if (restoredAllFiles) {
          status.info(StatusAppender.OK, "Game has finished running. All files restored");
        } else {
          status.info(StatusAppender.WARN,
              "Your files could not be restored correctly. Check log for details");
        }
      }
      os.setSystemDxLevel(model.getOriginalDxLevel());
      view.getBtnStartGame().setText("Launch Game");
      view.getBtnStartGame().setEnabled(true);
    }
  }

}
