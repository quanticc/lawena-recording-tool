package ui;

import util.StartLogger;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import lwrt.Lawena;
import lwrt.SettingsManager;

public class LwrtGUI {

  private static final Logger log = Logger.getLogger("lawena");

  public static void main(String[] args) throws Exception {
    SettingsManager cfg = new SettingsManager("settings.lwf");
    new StartLogger("lawena").toConsole(cfg.getLogConsoleLevel()).toFile(cfg.getLogFileLevel());
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      public void uncaughtException(Thread t, final Throwable e) {
        log.log(Level.SEVERE, "Unexpected problem in " + t, e);
      }
    });
    final Lawena lawena = new Lawena(cfg);
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        try {
          lawena.start();
        } catch (Exception e) {
          log.log(Level.WARNING, "Problem while running the GUI", e);
        }
      }
    });

  }

}
