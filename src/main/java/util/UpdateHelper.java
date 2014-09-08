package util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import com.threerings.getdown.util.ConfigUtil;
import com.threerings.getdown.util.LaunchUtil;

public class UpdateHelper {

  private static final Logger log = Logger.getLogger("lawena");

  private Properties channels;

  public UpdateHelper() {}

  /**
   * Workaround to delete resources not used anymore by lawena, since Getdown does not support this
   * out of the box.
   */
  public void cleanupUnusedFiles() {
    try {
      Map<String, Object> config = ConfigUtil.parseConfig(new File("getdown.txt"), false);
      String[] toDelete = (String[]) ConfigUtil.getMultiValue(config, "delete");
      for (String path : toDelete) {
        try {
          if (Files.deleteIfExists(Paths.get(path))) {
            log.info("Deleted deprecated file: " + path);
          }
        } catch (IOException e) {
          log.log(Level.INFO, "Could not delete deprecated file: " + path, e);
        }
      }
    } catch (IllegalArgumentException | IOException e) {
      log.info("Could not remove unused files: " + e);
    }
  }

  /**
   * Updates the lawena.exe file with the latest version downloaded by the updater, copying it
   * safely to be used upon next run.
   */
  public void updateLauncher() {
    try {
      Logger logger = Logger.getLogger("com.threerings.getdown");
      logger.setParent(Logger.getLogger("lawena"));
      String[] executables = {"lawena", "lawena-no-updates"};
      for (String exe : executables) {
        File oldgd = new File("../" + exe + "-old.exe");
        File curgd = new File("../" + exe + ".exe");
        File newgd = new File("code/" + exe + "-new.exe");
        LaunchUtil.upgradeGetdown(oldgd, curgd, newgd);
      }
    } catch (IllegalArgumentException e) {
      log.info("Could not update launcher executable: " + e);
    }
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
