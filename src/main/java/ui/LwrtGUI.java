package ui;

import lwrt.Lawena;
import lwrt.SettingsManager;
import util.StartLogger;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class LwrtGUI {

	private static final Logger log = Logger.getLogger("lawena");
    private static final String n = System.getProperty("line.separator");

	public static void main(String[] args) throws Exception {
		SettingsManager cfg = new SettingsManager("settings.lwf");
		new StartLogger("lawena").toConsole(cfg.getLogConsoleLevel()).toFile(cfg.getLogFileLevel());
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			boolean dialogShown = false;

			@Override
			public void uncaughtException(Thread t, final Throwable e) {
				log.log(Level.SEVERE, "Unexpected problem in " + t, e);
				if (!dialogShown) {
					JOptionPane.showMessageDialog(null,
                        String.join(n,
                            "An error occurred while running Lawena.",
                            "Caused by " + e.toString(),
                            "Please see the logfile for more information."),
							"Uncaught Exception", JOptionPane.ERROR_MESSAGE);
					dialogShown = true; // to avoid dialog spam
				}
			}
		});
		try {
			final Lawena lawena = new Lawena(cfg);
			log.fine("Preparing to display main user interface");
			SwingUtilities.invokeAndWait(() -> {
				try {
					lawena.start();
				} catch (Exception e) {
					log.log(Level.WARNING, "Problem while running the GUI", e);
					JOptionPane.showMessageDialog(null,
							String.join(n,
                                "An error occurred while displaying the GUI.",
                                "Caused by " + e.toString(),
                                "Please see the logfile for more information."),
                        "GUI Launch Error",
							JOptionPane.ERROR_MESSAGE);
				}
			});
		} catch (Throwable t) {
			log.log(Level.SEVERE, "Problem initializing Lawena", t);
			JOptionPane.showMessageDialog(null,
					String.join(n,
                        "An error occurred while starting Lawena.",
                        "Caused by " + t.toString(),
                        "Please see the logfile for more information."),
                "Launch Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

}
