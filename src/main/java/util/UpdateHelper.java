package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import com.threerings.getdown.util.ConfigUtil;

public class UpdateHelper {

  private static final Logger log = Logger.getLogger("lawena");

  private Properties channels;

  public UpdateHelper() {}

  private void cleanupUnusedFiles() {
    try {
      Map<String, Object> config = ConfigUtil.parseConfig(new File("getdown.txt"), false);
      String[] toDelete = (String[]) ConfigUtil.getMultiValue(config, "delete");
      if (toDelete != null) {
        for (String path : toDelete) {
          if (path == null) {
            continue;
          }
          try {
            if (Files.deleteIfExists(Paths.get(path))) {
              log.info("Deleted deprecated file: " + path);
            }
          } catch (IOException e) {
            log.log(Level.INFO, "Could not delete deprecated file: " + path, e);
          }
        }
      }
    } catch (IllegalArgumentException | IOException e) {
      log.info("Could not remove unused files: " + e);
    }
  }

  private static void upgrade(String desc, File oldgd, File curgd, File newgd) {
    if (!newgd.exists() || newgd.length() == curgd.length()
        || Util.compareCreationTime(newgd, curgd) == 0) {
      log.fine("Resource " + desc + " is up to date");
      return;
    }
    log.info("Upgrade " + desc + " with " + newgd + "...");
    try {
      Files.deleteIfExists(oldgd.toPath());
    } catch (IOException e) {
      log.warning("Could not delete old path: " + e);
    }
    if (!curgd.exists() || curgd.renameTo(oldgd)) {
      if (newgd.renameTo(curgd)) {
        try {
          Files.deleteIfExists(oldgd.toPath());
        } catch (IOException e) {
          log.warning("Could not delete old path: " + e);
        }
        try (InputStream in = new FileInputStream(curgd);
            OutputStream out = new FileOutputStream(newgd)) {
          Util.copy(in, out);
        } catch (IOException e) {
          log.warning("Problem copying " + desc + " back: " + e);
        }
        return;
      }
      log.warning("Unable to rename to " + oldgd);
      if (!oldgd.renameTo(curgd)) {
        log.warning("Could not rename " + oldgd + " to " + curgd);
      }
    }
    log.info("Attempting to upgrade by copying over " + curgd + "...");
    try (InputStream in = new FileInputStream(newgd);
        OutputStream out = new FileOutputStream(curgd)) {
      Util.copy(in, out);
    } catch (IOException e) {
      log.warning("Brute force copy method also failed: " + e);
    }
  }

  private static void upgradeLauncher() {
    File oldgd = new File("../lawena-old.exe");
    File curgd = new File("../lawena.exe");
    File newgd = new File("code/lawena-new.exe");
    upgrade("Lawena launcher", oldgd, curgd, newgd);
  }

  private static void upgradeGetdown() {
    File oldgd = new File("getdown-client-old.jar");
    File curgd = new File("getdown-client.jar");
    File newgd = new File("code/getdown-client-new.jar");
    upgrade("Lawena updater", oldgd, curgd, newgd);
  }

  public void fileCleanup() {
    cleanupUnusedFiles();
    upgradeLauncher();
    upgradeGetdown();
  }

  public void showSwitchUpdateChannelDialog() {
    if (channels.isEmpty()) {
      JOptionPane.showMessageDialog(null, "No updater branch options found");
      return;
    }
    Object answer =
        JOptionPane.showInputDialog(null,
            "Select the branch from where updates will be downloaded\n", "Switch Updater Branch",
            JOptionPane.PLAIN_MESSAGE, null, channels.keySet().toArray(), null);
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
          log.info("Problem while changing branches");
        }
        try {
          Files.copy(newGetdown.toPath(), curGetdown.toPath());
          Files.deleteIfExists(oldGetdown.toPath());
        } catch (IOException e) {
          log.info("Problem while changing branches");
        }
      } else {
        log.info("Invalid branch name: " + answer);
      }
    }
  }

  public void loadChannels() {
    channels = new Properties();
    try (InputStream in = Files.newInputStream(Paths.get("res/channels.txt"))) {
      channels.load(in);
      log.finer("Loaded updater branches: " + channels.toString());
    } catch (IOException e) {
      log.fine("Updater branches file not found, disabling feature");
    }
  }

}
