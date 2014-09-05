package com.github.lawena.app.task;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileOpener extends SwingWorker<Void, Void> {

  private static final Logger log = LoggerFactory.getLogger(FileOpener.class);

  private File file;

  public FileOpener(Path path) {
    this.file = path.toFile();
  }

  @Override
  protected Void doInBackground() throws Exception {
    if (Desktop.isDesktopSupported()) {
      Desktop desktop = Desktop.getDesktop();
      if (desktop.isSupported(Action.OPEN)) {
        log.info("Opening {}", file);
        desktop.open(file);
        return null;
      } else {
        log.warn("Desktop does not support open action");
      }
    } else {
      log.warn("Desktop is not supported");
    }
    return null;
  }

  @Override
  protected void done() {
    try {
      get();
    } catch (ExecutionException e) {
      handleException(file, e);
    } catch (InterruptedException e) {
      handleException(file, e);
    }
  }

  private static void handleException(File file, Exception e) {
    log.warn("A problem occurred while opening {}: {}", file, e);
    JOptionPane.showMessageDialog(null, "Sorry, a problem occurred while trying to open this file",
        "A problem occurred", JOptionPane.ERROR_MESSAGE);
  }

}
