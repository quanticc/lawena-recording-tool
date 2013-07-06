
package ui;

import util.StartLogger;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import lwrt.Lawena;

public class LwrtGUI {

    private static final Logger log = Logger.getLogger("lawena");

    public static void main(String[] args) throws Exception {
        new StartLogger("lawena").toConsole(Level.FINER).toFile(Level.FINE);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, final Throwable e) {
                log.log(Level.SEVERE, "Unexpected problem in " + t, e);
            }
        });

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Lawena lawena = new Lawena();
                try {
                    lawena.start();
                } catch (Exception e) {
                    log.log(Level.WARNING, "Problem while running the GUI", e);
                }
            }
        });
    }
}
