
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
        new StartLogger("lawena").toConsole(Level.ALL).toFile(Level.FINER);
        log.finer("-----------------------------------");
        log.finer("   Lawena Recording Tool Started   ");
        log.finer("-----------------------------------");
        SettingsManager cfg = new SettingsManager();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, final Throwable e) {
                log.log(Level.SEVERE, "Unexpected problem in " + t, e);
            }
        });

        log.finer("Starting Lawena instance");
        final Lawena lawena = new Lawena(cfg);

        log.finer("Starting User Interface");
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
