package com.github.lawena.app;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.github.lawena.Messages;
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

  static final Logger log = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws Exception {
    try {
      SLF4JBridgeHandler.removeHandlersForRootLogger();
      SLF4JBridgeHandler.install();

      ch.qos.logback.classic.Logger rootLog =
          (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("root"); //$NON-NLS-1$
      LoggingAppender appender = new LoggingAppender(rootLog.getLoggerContext());
      rootLog.addAppender(appender);

      // final OptionSet optionSet = Options.getParser().parse(args);

      Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

        private boolean shown = false;

        @Override
        public void uncaughtException(Thread t, final Throwable e) {
          log.error("Unexpected problem in " + t, e); //$NON-NLS-1$
          if (!shown) {
            showDialog(e,
                String.format(Messages.getString("Main.uncaughtExceptionBody"), t.toString()), //$NON-NLS-1$
                Messages.getString("Main.uncaughtExceptionTitle")); //$NON-NLS-1$
            shown = true;
          }
        }
      });

      String osname = System.getProperty("os.name"); //$NON-NLS-1$
      if (osname.contains("Windows")) { //$NON-NLS-1$
        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
          log.warn("Could not set the look and feel", e); //$NON-NLS-1$
        }
      }

      int o =
          JOptionPane
              .showOptionDialog(
                  null,
                  Messages.getString("Main.selectGameToRun"), Messages.getString("Main.lawenaRecordingTool"), //$NON-NLS-1$ //$NON-NLS-2$
                  JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[] {
                      "Team Fortress 2", "Counter-Strike: Global Offensive"}, "Team Fortress 2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      if (o != -1) {
        OSInterface os = newOsInterface(o);
        if (!OSInterface.isAdmin()) {
          int opt =
              JOptionPane.showConfirmDialog(null, Messages.getString("Main.noAdminPrivilegesBody"), //$NON-NLS-1$
                  Messages.getString("Main.noAdminPrivilegesTitle"), JOptionPane.YES_NO_OPTION, //$NON-NLS-1$
                  JOptionPane.WARNING_MESSAGE);
          if (opt != JOptionPane.YES_OPTION) {
            System.exit(0);
          }
        }
        if (o == 0) {
          start(new LawenaTf(new MainModel(new File("lwrt/tf/default.json"), os, new LinkerTf())), //$NON-NLS-1$
              appender);
        } else if (o == 1) {
          start(
              new LawenaGo(new MainModel(new File("lwrt/csgo/default.json"), os, new LinkerGo())), //$NON-NLS-1$
              appender);
        }
      }
    } catch (Exception e) {
      log.error("Exception while initializing Lawena", e); //$NON-NLS-1$
      showDialog(
          e,
          Messages.getString("Main.errorDuringStartupBody"), Messages.getString("Main.errorDuringStartupTitle")); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  private static OSInterface newOsInterface(int o) {
    String osname = System.getProperty("os.name"); //$NON-NLS-1$
    if (osname.contains("Windows")) { //$NON-NLS-1$
      return new OSInterface[] {new WindowsInterfaceTf(), new WindowsInterfaceGo()}[o];
    } else if (osname.contains("Linux")) { //$NON-NLS-1$
      return new OSInterface[] {new LinuxInterfaceTf(), new LinuxInterfaceGo()}[o];
    } else if (osname.contains("OS X")) { //$NON-NLS-1$
      return new OSInterface[] {new OSXInterfaceTf(), new OSXInterfaceGo()}[o];
    } else {
      throw new UnsupportedOperationException("OS not supported"); //$NON-NLS-1$
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
          log.warn("Problem while launching the GUI", e); //$NON-NLS-1$
          showDialog(
              e,
              Messages.getString("Main.uiLaunchErrorBody"), Messages.getString("Main.uiLaunchErrorTitle")); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
    });
  }

  static void showDialog(Throwable t, String title, String header) {
    String msg = t.toString();
    msg = msg.substring(0, Math.min(80, msg.length()));
    JOptionPane.showMessageDialog(null, header + "\n" //$NON-NLS-1$
        + String.format(Messages.getString("Main.errorDialogDetails"), msg), //$NON-NLS-1$
        title, JOptionPane.ERROR_MESSAGE);
  }
}
