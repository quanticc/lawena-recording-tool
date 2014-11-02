package ui;

import util.StartLogger;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import lwrt.Lawena;
import lwrt.SettingsManager;

public class LwrtGUI {

  private static final Logger log = Logger.getLogger("lawena");

  public static void main(String[] args) throws Exception {
    SettingsManager cfg = new SettingsManager("settings.lwf");
    new StartLogger("lawena").toConsole(cfg.getLogConsoleLevel()).toFile(cfg.getLogFileLevel());
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      boolean dialogShown = false;

      public void uncaughtException(Thread t, final Throwable e) {
        log.log(Level.SEVERE, "Unexpected problem in " + t, e);
        if (!dialogShown) {
          JOptionPane.showMessageDialog(null, "An error occurred while running Lawena.\nCaused by "
              + e.toString() + "\nPlease see the logfile for more information.",
              "Uncaught Exception", JOptionPane.ERROR_MESSAGE);
          dialogShown = true; // to avoid dialog spam
        }
      }
    });
    try {
      final Lawena lawena = new Lawena(cfg);
      log.fine("Preparing to display main user interface");
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          try {
            lawena.start();
          } catch (Exception e) {
            log.log(Level.WARNING, "Problem while running the GUI", e);
            JOptionPane.showMessageDialog(null,
                "An error occurred while displaying the GUI.\nCaused by " + e.toString()
                    + "\nPlease see the logfile for more information.", "GUI Launch Error",
                JOptionPane.ERROR_MESSAGE);
          }
        }
      });
    } catch (Throwable t) {
      log.log(Level.SEVERE, "Problem initializing Lawena", t);
      JOptionPane.showMessageDialog(null, "An error occurred while starting Lawena.\nCaused by "
          + t.toString() + "\nPlease see the logfile for more information.", "Launch Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }

}
