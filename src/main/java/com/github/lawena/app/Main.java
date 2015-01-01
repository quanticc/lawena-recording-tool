package com.github.lawena.app;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.github.lawena.app.model.LinkerGo;
import com.github.lawena.app.model.LinkerTf;
import com.github.lawena.app.model.MainModel;
import com.github.lawena.os.LinuxInterfaceGo;
import com.github.lawena.os.LinuxInterfaceTf;
import com.github.lawena.os.OSInterface;
import com.github.lawena.os.OSXInterfaceGo;
import com.github.lawena.os.OSXInterfaceTf;
import com.github.lawena.os.WindowsInterfaceGo;
import com.github.lawena.os.WindowsInterfaceTf;
import com.github.lawena.util.LoggingAppender;

/**
 * Entry point of the application. Invokes main presenter {@linkplain Lawena}
 * 
 * @author Ivan
 *
 */
public class Main {

  private static final Logger log = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws Exception {
    try {
      SLF4JBridgeHandler.removeHandlersForRootLogger();
      SLF4JBridgeHandler.install();

      ch.qos.logback.classic.Logger rootLog =
          (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("root");
      LoggingAppender appender = new LoggingAppender(rootLog.getLoggerContext());
      rootLog.addAppender(appender);

      //final OptionSet optionSet = Options.getParser().parse(args);

      Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

        private boolean shown = false;

        @Override
        public void uncaughtException(Thread t, final Throwable e) {
          log.error("Unexpected problem in " + t, e);
          if (!shown) {
            showDialog(e, "[" + t.toString() + "] An error occurred while running Lawena.",
                "Uncaught Exception");
            shown = true;
          }
        }
      });

      String osname = System.getProperty("os.name");
      if (osname.contains("Windows")) {
        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
          log.warn("Could not set the look and feel", e);
        }
      }

      int o =
          JOptionPane.showOptionDialog(null, "Select game to run", "Lawena Recording Tool",
              JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[] {
                  "Team Fortress 2", "Counter-Strike: Global Offensive"}, "Team Fortress 2");
      if (o != -1) {
        OSInterface os = newOsInterface(o);
        if (!os.isAdmin()) {
          int opt =
              JOptionPane.showConfirmDialog(null, "This version of Lawena uses symlinks that\n"
                  + "requires Administrative permissions.\n\n" + "Do you want to continue?",
                  "No Administrative Privileges", JOptionPane.YES_NO_OPTION,
                  JOptionPane.WARNING_MESSAGE);
          if (opt != JOptionPane.YES_OPTION) {
            System.exit(0);
          }
        }
        if (o == 0) {
          start(new LawenaTf(new MainModel(new File("lwrt/tf/default.json"), os, new LinkerTf())), appender);
        } else if (o == 1) {
          start(new LawenaGo(new MainModel(new File("lwrt/csgo/default.json"), os, new LinkerGo())), appender);
        }
      }
    } catch (Exception e) {
      log.error("Exception while initializing Lawena", e);
      showDialog(e, "An error occurred while starting Lawena.", "Startup Failed");
    }
  }

  private static OSInterface newOsInterface(int o) {
    String osname = System.getProperty("os.name");
    if (osname.contains("Windows")) {
      return new OSInterface[] {new WindowsInterfaceTf(), new WindowsInterfaceGo()}[o];
    } else if (osname.contains("Linux")) {
      return new OSInterface[] {new LinuxInterfaceTf(), new LinuxInterfaceGo()}[o];
    } else if (osname.contains("OS X")) {
      return new OSInterface[] {new OSXInterfaceTf(), new OSXInterfaceGo()}[o];
    } else {
      throw new UnsupportedOperationException("OS not supported");
    }
  }

  private static void start(final Lawena lawena, LoggingAppender appender)
      throws InvocationTargetException, InterruptedException {
    lawena.setAppender(appender);
    SwingUtilities.invokeAndWait(new Runnable() {

      @Override
      public void run() {
        try {
          lawena.start();
        } catch (Exception e) {
          log.warn("Problem while launching the GUI", e);
          showDialog(e, "An error occurred while displaying the GUI.", "GUI Launch Failed");
        }
      }
    });
  }

  private static void showDialog(Throwable t, String title, String header) {
    String msg = t.toString();
    msg = msg.substring(0, Math.min(80, msg.length()));
    JOptionPane.showMessageDialog(null, header + "\nCaused by " + msg
        + "\nLogfile for details. Please report this issue.", title, JOptionPane.ERROR_MESSAGE);
  }
}
