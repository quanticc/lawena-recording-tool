package com.github.lawena.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import javax.swing.ButtonModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.github.lawena.Messages;
import com.github.lawena.app.model.LinkerGo;
import com.github.lawena.app.model.LinkerTf;
import com.github.lawena.app.model.MainModel;
import com.github.lawena.app.model.SourceGames;
import com.github.lawena.os.LinuxInterfaceGo;
import com.github.lawena.os.LinuxInterfaceTf;
import com.github.lawena.os.OSInterface;
import com.github.lawena.os.OSXInterfaceGo;
import com.github.lawena.os.OSXInterfaceTf;
import com.github.lawena.os.WindowsInterfaceGo;
import com.github.lawena.os.WindowsInterfaceTf;
import com.github.lawena.ui.GameSelectDialog;
import com.github.lawena.util.LoggingAppender;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/**
 * Entry point of the application. Invokes main presenter {@linkplain Lawena}
 * 
 * @author Ivan
 *
 */
public class Main {

  static final Logger log = LoggerFactory.getLogger(Main.class);
  private static LoggingAppender appender;
  private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public static void main(String[] args) throws Exception {
    try {
      SLF4JBridgeHandler.removeHandlersForRootLogger();
      SLF4JBridgeHandler.install();

      ch.qos.logback.classic.Logger rootLog =
          (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("root"); //$NON-NLS-1$
      appender = new LoggingAppender(rootLog.getLoggerContext());
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

      SourceGames games;
      try (Reader reader = Files.newBufferedReader(SourceGames.path, Charset.forName("UTF-8"))) { //$NON-NLS-1$
        games = gson.fromJson(reader, SourceGames.class);
      } catch (JsonSyntaxException | JsonIOException | IOException e) {
        log.debug("Could not properly load game settings file: {}", e.toString()); //$NON-NLS-1$
        games = new SourceGames(); // load defaults
      }

      // skip dialog if user wanted to remember previous choice
      if (!games.isRememberChoice() || games.getSelected() == null
          || (!games.getSelected().equals("tf") && !games.getSelected().equals("go"))) { //$NON-NLS-1$ //$NON-NLS-2$
        log.debug("Preparing to create game selection dialog"); //$NON-NLS-1$

        // create game select dialog
        final SourceGames sg = games;
        final GameSelectDialog dialog = new GameSelectDialog();
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.getOkButton().addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            ButtonModel b = dialog.getGameGroup().getSelection();
            if (b == null) {
              JOptionPane.showMessageDialog(null, Messages.getString("Main.noGameSelected"), //$NON-NLS-1$
                  Messages.getString("Main.lawenaRecordingTool"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
            } else {
              String id = b.getActionCommand();
              if ("tf".equals(id) || "go".equals(id)) { //$NON-NLS-1$ //$NON-NLS-2$
                sg.setRememberChoice(dialog.getCheckRemember().isSelected());
                sg.setSelected(id);
                setup(sg);
                dialog.setVisible(false);
              } else {
                log.debug("Invalid action command: {}", id); //$NON-NLS-1$
                JOptionPane.showMessageDialog(null, Messages.getString("Main.noGameSelected"), //$NON-NLS-1$
                    Messages.getString("Main.lawenaRecordingTool"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
              }
            }
          }
        });
        SwingUtilities.invokeAndWait(new Runnable() {

          @Override
          public void run() {
            try {
              dialog.setVisible(true);
            } catch (Exception e) {
              log.warn("Problem while launching the GUI", e); //$NON-NLS-1$
              showDialog(
                  e,
                  Messages.getString("Main.uiLaunchErrorBody"), Messages.getString("Main.uiLaunchErrorTitle")); //$NON-NLS-1$ //$NON-NLS-2$
            }
          }
        });
      } else {
        setup(games);
      }
    } catch (Exception e) {
      log.error("Exception while initializing Lawena", e); //$NON-NLS-1$
      showDialog(
          e,
          Messages.getString("Main.errorDuringStartupBody"), Messages.getString("Main.errorDuringStartupTitle")); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  static void setup(SourceGames games) {
    games.save();
    int o = "go".equals(games.getSelected()) ? 1 : 0; //$NON-NLS-1$
    try {
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
        String defp = "lwrt/tf/default.json"; //$NON-NLS-1$
        start(new LawenaTf(new MainModel(new File(defp), os, new LinkerTf(), games)));
      } else if (o == 1) {
        String defp = "lwrt/csgo/default.json"; //$NON-NLS-1$
        start(new LawenaGo(new MainModel(new File(defp), os, new LinkerGo(), games)));
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

  private static void start(Lawena lawena) throws InvocationTargetException, InterruptedException {
    lawena.setAppender(appender);
    try {
      lawena.start();
    } catch (Exception e) {
      log.warn("Problem while launching the GUI", e); //$NON-NLS-1$
      showDialog(
          e,
          Messages.getString("Main.uiLaunchErrorBody"), Messages.getString("Main.uiLaunchErrorTitle")); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  static void showDialog(Throwable t, String title, String header) {
    String msg = t.toString();
    msg = msg.substring(0, Math.min(80, msg.length()));
    JOptionPane.showMessageDialog(null, header + "\n" //$NON-NLS-1$
        + String.format(Messages.getString("Main.errorDialogDetails"), msg), //$NON-NLS-1$
        title, JOptionPane.ERROR_MESSAGE);
  }
}
