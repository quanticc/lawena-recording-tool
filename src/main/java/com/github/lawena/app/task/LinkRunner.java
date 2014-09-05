package com.github.lawena.app.task;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkRunner extends SwingWorker<Void, Void> {

  private static final Logger log = LoggerFactory.getLogger(LinkRunner.class);

  private final URI uri;

  public LinkRunner(String uri) {
    if (uri == null) {
      throw new IllegalArgumentException("URI must not be null");
    }
    try {
      this.uri = new URI(uri);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Invalid URI at " + uri, e);
    }
  }

  public LinkRunner(URI uri) {
    if (uri == null) {
      throw new IllegalArgumentException("URI must not be null");
    }
    this.uri = uri;
  }

  @Override
  protected Void doInBackground() throws Exception {
    if (Desktop.isDesktopSupported()) {
      Desktop desktop = Desktop.getDesktop();
      if (desktop.isSupported(Action.BROWSE)) {
        log.info("Opening {} in the browser", uri);
        desktop.browse(uri);
        return null;
      } else {
        log.warn("Desktop does not support browse action");
      }
    } else {
      log.warn("Desktop is not supported");
    }
    JOptionPane.showMessageDialog(null, "Java is not able to launch links on your computer.",
        "Cannot Launch Link", JOptionPane.WARNING_MESSAGE);
    return null;
  }

  @Override
  protected void done() {
    try {
      get();
    } catch (ExecutionException e) {
      handleException(uri, e);
    } catch (InterruptedException e) {
      handleException(uri, e);
    }
  }

  private static void handleException(URI u, Exception e) {
    log.warn("A problem occurred while opening {}: {}", u, e);
    JOptionPane
        .showMessageDialog(
            null,
            "Sorry, a problem occurred while trying to open this link in your system's standard browser.",
            "A problem occured", JOptionPane.ERROR_MESSAGE);
  }
}
