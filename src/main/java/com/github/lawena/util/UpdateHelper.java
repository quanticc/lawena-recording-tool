package com.github.lawena.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

@Deprecated
public class UpdateHelper {

  private static final Logger log = Logger.getLogger("lawena");

  private ClassLoader cl;
  private Properties channels;

  public UpdateHelper() {
    try {
      URL[] urls = new URL[] {new File("code/getdown-client.jar").toURI().toURL()};
      cl = new URLClassLoader(urls);
    } catch (MalformedURLException e) {
      log.log(Level.INFO, "URL is malformed!", e);
    }
  }

  /**
   * Workaround to delete resources not used anymore by lawena, since Getdown does not support this
   * out of the box.
   */
  public void cleanupUnusedFiles() {
    try {
      Class<?> cls = cl.loadClass("com.threerings.getdown.util.ConfigUtil");
      Method parseConfig = cls.getMethod("parseConfig", File.class, boolean.class);
      @SuppressWarnings("unchecked")
      Map<String, Object> config =
          (Map<String, Object>) parseConfig.invoke(null, new File("getdown.txt"), false);
      Method getMultiValue = cls.getMethod("getMultiValue", Map.class, String.class);
      String[] toDelete = (String[]) getMultiValue.invoke(null, config, "delete");
      for (String path : toDelete) {
        try {
          if (Files.deleteIfExists(Paths.get(path))) {
            log.info("Deleted deprecated file: " + path);
          }
        } catch (IOException e) {
          log.log(Level.INFO, "Could not delete deprecated file: " + path, e);
        }
      }
    } catch (ClassNotFoundException | NoSuchMethodException | SecurityException
        | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      log.info("Could not remove unused files: " + e);
    }
  }

  /**
   * Updates the lawena.exe file with the latest version downloaded by the updater, copying it
   * safely to be used upon next run.
   */
  public void updateLauncher() {
    try {
      Class<?> cls = cl.loadClass("com.threerings.getdown.util.LaunchUtil");
      Method upgradeLauncher = cls.getMethod("upgradeGetdown", File.class, File.class, File.class);
      Logger logger = Logger.getLogger("com.threerings.getdown");
      logger.setParent(Logger.getLogger("lawena"));
      String[] executables = {"lawena", "lawena-no-updates"};
      for (String exe : executables) {
        File oldLauncher = new File("../" + exe + "-old.exe");
        File curLauncher = new File("../" + exe + ".exe");
        File newLauncher = new File("code/" + exe + "-new.exe");
        upgradeLauncher.invoke(null, oldLauncher, curLauncher, newLauncher);
      }
    } catch (ClassNotFoundException | NoSuchMethodException | SecurityException
        | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      log.info("Could not update launcher executable: " + e);
    }
  }

  public void showSwitchUpdateChannelDialog() {
    if (channels.isEmpty()) {
      JOptionPane.showMessageDialog(null, "No development branch options found");
      return;
    }
    Object answer =
        JOptionPane.showInputDialog(null,
            "Select the channel from where updates will be downloaded.\n"
                + "This is an experimental feature to allow easy switching\n"
                + "from stable to beta/dev/experimental branches and back.\n",
            "Switch Update Channel", JOptionPane.PLAIN_MESSAGE, null, channels.keySet().toArray(),
            null);
    if (answer != null) {
      String filename = channels.getProperty((String) answer);
      if (filename != null) {
        log.info("Changing branch to: " + answer);
        JOptionPane.showMessageDialog(null, "Changes will be applied upon Lawena restart.");
        File curGetdown = new File("getdown.txt");
        File newGetdown = new File(filename);
        File oldGetdown = new File("getdown-bak.txt");
        try {
          Files.copy(curGetdown.toPath(), oldGetdown.toPath());
          Files.deleteIfExists(curGetdown.toPath());
        } catch (IOException e) {
          log.info("Problem while changing streams");
        }
        try {
          Files.copy(newGetdown.toPath(), curGetdown.toPath());
          Files.deleteIfExists(oldGetdown.toPath());
        } catch (IOException e) {
          log.info("Problem while changing streams");
        }
      } else {
        log.info("Invalid branch name: " + answer);
      }
    }
  }

  public void loadChannels() {
    channels = new Properties();
    try {
      channels.load(new FileInputStream("res/channels.txt"));
      log.finer("Loaded development channels: " + channels.toString());
    } catch (IOException e) {
      log.fine("Development channels not found, disabling feature");
    }
  }

}
