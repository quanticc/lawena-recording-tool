package com.github.lawena.app;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.github.lawena.lwrt.Lawena;
import com.github.lawena.lwrt.SettingsManager;

public class Main {

  private static final Logger log = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws Exception {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
    SettingsManager cfg = new SettingsManager("settings.lwf");
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      public void uncaughtException(Thread t, final Throwable e) {
        log.error("Unexpected problem in " + t, e);
      }
    });
    final Lawena lawena = new Lawena(new MainModel(cfg));
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        try {
          lawena.start();
        } catch (Exception e) {
          log.warn("Problem while running the GUI", e);
        }
      }
    });
  }
}
