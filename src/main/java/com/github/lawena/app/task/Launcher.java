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

import com.github.lawena.Messages;
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
  private static final Logger status = LoggerFactory.getLogger("status"); //$NON-NLS-1$

  private Tasks tasks;
  private MainModel model;
  LawenaView view;
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
        JOptionPane
            .showMessageDialog(
                presenter.viewAsComponent(),
                Messages.getString("Launcher.noCustomHudSelectedOnLaunch"), Messages.getString("Launcher.noCustomHudSelectedOnLaunchTitle"), //$NON-NLS-1$ //$NON-NLS-2$
                JOptionPane.INFORMATION_MESSAGE);
        log.info("Launch aborted because the custom HUD to use was not specified"); //$NON-NLS-1$
        return false;
      }

      // Check for big custom folders, mitigate OOM errors with custom folder > 2 GB
      Path tfpath = toPath(Key.gamePath.getValue(settings));
      Path customPath = tfpath.resolve("custom"); //$NON-NLS-1$
      Path configPath = tfpath.resolve("cfg"); //$NON-NLS-1$
      boolean ok =
          Util.notifyBigFolder(Key.bigFolderThreshold.getValue(settings), configPath, customPath);
      if (!ok) {
        log.info("Launch aborted by the user"); //$NON-NLS-1$
        status.info(Messages.getString("Launcher.launchAbortedByUser")); //$NON-NLS-1$
        return false;
      }

      setProgress(5);
      closeGameHandles();
      setProgress(15);

      // Restoring user files
      status.info(Messages.getString("Launcher.restoringYourFiles")); //$NON-NLS-1$
      linker.unlink();
      setProgress(40);

      // Saving ui settings to cfg files
      status.info(Messages.getString("Launcher.generatingGameConfigFiles")); //$NON-NLS-1$
      try {
        presenter.saveSettings();
        writer.writeAll();
        model.getDemos().writeAutoplay();
      } catch (IOException e) {
        log.warn("Problem while saving settings to file", e); //$NON-NLS-1$
        status.info(StatusAppender.ERROR,
            Messages.getString("Launcher.failedToGenerateConfigFiles")); //$NON-NLS-1$
        return false;
      }
      setProgress(50);

      // Backing up user files and copying lawena files
      status.info(Messages.getString("Launcher.copyingLawenaFilesToGameFolders")); //$NON-NLS-1$
      try {
        linker.link();
      } catch (LawenaException e) {
        status.info(StatusAppender.ERROR, e.getMessage());
        return false;
      }
      setProgress(95);

      // Launching process
      status.info(Messages.getString("Launcher.launchingGameProcess")); //$NON-NLS-1$
      os.launchSteam(settings);

      SwingUtilities.invokeAndWait(new Runnable() {

        @Override
        public void run() {
          view.getBtnStartGame().setEnabled(true);
          view.getBtnStartGame().setText(Messages.getString("Launcher.stopGame")); //$NON-NLS-1$
        }
      });
      setProgress(100);

      int timeout = 0;
      int cfgtimeout = Key.launchTimeout.getValue(settings);
      int millis = 5000;
      int maxtimeout = cfgtimeout / (millis / 1000);
      setProgress(0);
      status.info(Messages.getString("Launcher.waitingForGameProcess")); //$NON-NLS-1$
      if (cfgtimeout > 0) {
        log.debug("Game launch timeout: around {} seconds", cfgtimeout); //$NON-NLS-1$
      } else {
        log.debug("Game launch timeout disabled"); //$NON-NLS-1$
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
        log.info("Game launch timed out after {} seconds", s); //$NON-NLS-1$
        status.info(StatusAppender.WARN,
            Messages.getString("Launcher.gameProcessNotFoundTimeout"), s); //$NON-NLS-1$
        return false;
      }

      log.info("Game has started running"); //$NON-NLS-1$
      status.info(Messages.getString("Launcher.waitingForProcessEnd")); //$NON-NLS-1$
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
        status.info(Messages.getString("Launcher.attemptingToKillProcess")); //$NON-NLS-1$
        os.killTf2Process();
        Thread.sleep(5000);
      } else {
        status.info(Messages.getString("Launcher.noGameProcessFoundToStop")); //$NON-NLS-1$
      }
      if (!os.isGameRunning()) {
        tasks.getCurrentLaunchTask().cancel(true);
      }
      closeGameHandles();
    }

    return true;
  }

  private void closeGameHandles() {
    status.info(Messages.getString("Launcher.closingOpenHandles1")); //$NON-NLS-1$
    os.closeHandles(toPath(Key.gamePath.getValue(settings)).resolve("cfg")); //$NON-NLS-1$
    status.info(Messages.getString("Launcher.closingOpenHandles2")); //$NON-NLS-1$
    os.closeHandles(toPath(Key.gamePath.getValue(settings)).resolve("custom")); //$NON-NLS-1$
  }

  private boolean verifyCustomHud() {
    if (view.getCmbHud().getSelectedItem().equals("Custom")) { //$NON-NLS-1$
      for (Resource resource : resources.getResourceList()) {
        if (resource.isEnabled()) {
          try {
            Resources.updateTags(resource);
            if (resource.getTags().contains(Resource.HUD)) {
              log.debug("Custom HUD verified with {}", resource); //$NON-NLS-1$
              return true;
            }
          } catch (IOException e) {
            log.warn("Could not determine resource tags", e); //$NON-NLS-1$
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
        // ignore
      }
      boolean restoredAllFiles = linker.unlink();
      if (ranGameCorrectly) {
        if (restoredAllFiles) {
          status.info(StatusAppender.OK, Messages.getString("Launcher.fileRestoreSuccessful")); //$NON-NLS-1$
        } else {
          status.info(StatusAppender.WARN, Messages.getString("Launcher.fileRestoreFailed")); //$NON-NLS-1$
        }
      }
      os.setSystemDxLevel(model.getOriginalDxLevel());
      view.getBtnStartGame().setText(Messages.getString("Launcher.launchGame")); //$NON-NLS-1$
      view.getBtnStartGame().setEnabled(true);
    }
  }

}
