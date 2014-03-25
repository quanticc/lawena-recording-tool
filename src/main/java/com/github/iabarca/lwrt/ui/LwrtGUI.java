
package com.github.iabarca.lwrt.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import com.github.iabarca.lwrt.lwrt.Launcher;
import com.github.iabarca.lwrt.lwrt.Lawena;
import com.github.iabarca.lwrt.lwrt.SettingsManager;
import com.github.iabarca.lwrt.util.StartLogger;

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
        
        Map<String, String> argsMap = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-g") && i + 1 < args.length) {
                if (!argsMap.containsKey("game")) {
                    argsMap.put("game", args[i + 1]);
                }
                i++;
            }
        }
        
        log.finer("Starting Lawena instance");
        //Launcher launcher = new Launcher(argsMap);
        
//        final Lawena lawena = new Lawena(cfg);
//
//        log.finer("Starting User Interface");
//        SwingUtilities.invokeAndWait(new Runnable() {
//            public void run() {
//                try {
//                    lawena.start();
//                } catch (Exception e) {
//                    log.log(Level.WARNING, "Problem while running the GUI", e);
//                }
//            }
//        });

    }

}
