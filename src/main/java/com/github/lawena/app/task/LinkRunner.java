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

import com.github.lawena.Messages;

public class LinkRunner extends SwingWorker<Void, Void> {

  private static final Logger log = LoggerFactory.getLogger(LinkRunner.class);

  private final URI uri;

  public LinkRunner(String uri) {
    if (uri == null) {
      throw new IllegalArgumentException("URI must not be null"); //$NON-NLS-1$
    }
    try {
      this.uri = new URI(uri);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Invalid URI at " + uri, e); //$NON-NLS-1$
    }
  }

  public LinkRunner(URI uri) {
    if (uri == null) {
      throw new IllegalArgumentException("URI must not be null"); //$NON-NLS-1$
    }
    this.uri = uri;
  }

  @Override
  protected Void doInBackground() throws Exception {
    if (Desktop.isDesktopSupported()) {
      Desktop desktop = Desktop.getDesktop();
      if (desktop.isSupported(Action.BROWSE)) {
        log.info("Opening {} in the browser", uri); //$NON-NLS-1$
        desktop.browse(uri);
        return null;
      } else {
        log.warn("Desktop does not support browse action"); //$NON-NLS-1$
      }
    } else {
      log.warn("Desktop is not supported"); //$NON-NLS-1$
    }
    JOptionPane.showMessageDialog(null, Messages.getString("LinkRunner.cannotLaunchBrowser"), //$NON-NLS-1$
        Messages.getString("LinkRunner.cannotLaunchBrowserTitle"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
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
    log.warn("A problem occurred while opening {}: {}", u, e); //$NON-NLS-1$
    JOptionPane.showMessageDialog(null, Messages.getString("LinkRunner.openLinkException"), //$NON-NLS-1$
        Messages.getString("LinkRunner.openLinkExceptionTitle"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
  }
}
