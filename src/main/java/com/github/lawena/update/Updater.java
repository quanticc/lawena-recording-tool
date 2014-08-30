package com.github.lawena.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.util.Util;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.threerings.getdown.data.Resource;
import com.threerings.getdown.net.Downloader.Observer;
import com.threerings.getdown.net.HTTPDownloader;
import com.threerings.getdown.util.ConfigUtil;
import com.threerings.getdown.util.LaunchUtil;

/**
 * Supports updating and branch-switching features using Getdown as a service for deployment.
 * 
 * @author Ivan
 *
 */
public class Updater {

  private static final Logger log = LoggerFactory.getLogger(Updater.class);
  private static final String DEFAULT_BRANCHES =
      "https://dl.dropboxusercontent.com/u/74380/lwrt/channels.json";

  private boolean standalone = false;
  private final Map<String, Object> getdown;
  private final Gson gson = new Gson();
  private List<Branch> branches;
  private long lastCheck = 0L;

  public Updater() {
    this(Paths.get("getdown.txt"));
  }

  public Updater(Path getdownPath) {
    this.getdown = parseConfig(getdownPath);
  }

  private Map<String, Object> parseConfig(Path path) {
    try {
      return ConfigUtil.parseConfig(path.toFile(), false);
    } catch (IOException e) {
      log.info("No updater configuration could be loaded at {}", path);
      standalone = true;
      return new HashMap<>();
    }
  }

  public void clear() {
    branches = null;
  }

  private SortedSet<Build> getBuildList(Branch branch) {
    if (branch == null)
      throw new IllegalArgumentException("Must set a branch");
    SortedSet<Build> builds = branch.getBuilds();
    if (builds != null) {
      return builds;
    }
    builds = new TreeSet<Build>();
    if (branch.equals(Branch.STANDALONE)) {
      return builds;
    }
    if (branch.getUrl() == null || branch.getUrl().isEmpty()) {
      log.warn("Invalid url for branch {}", branch);
      return builds;
    }
    String name = "buildlist.txt";
    builds = new TreeSet<Build>();
    try {
      File local = new File(name).getAbsoluteFile();
      URL url = new URL(branch.getUrl() + name);
      Resource res = new Resource(local.getName(), url, local, false);
      if (download(res)) {
        try {
          for (String line : Files.readAllLines(local.toPath(), Charset.defaultCharset())) {
            String[] data = line.split(";");
            builds.add(new Build(data));
          }
        } catch (IOException e) {
          log.warn("Could not read lines from file: " + e);
        }
        res.erase();
        try {
          Files.deleteIfExists(local.toPath());
        } catch (IOException e) {
          log.warn("Could not delete file", e);
        }
      }
    } catch (MalformedURLException e) {
      log.warn("Invalid URL: " + e);
    }
    branch.setBuilds(builds);
    return builds;
  }

  /**
   * Retrieves the current development branch the installation is in. This method might trigger
   * {@link #getBranches()} to update available branches
   * 
   * @return the current {@link Branch}
   */
  public Branch getCurrentBranch() {
    String branchName = getCurrentBranchName();
    for (Branch branch : getBranches()) {
      if (branch.getId().equals(branchName)) {
        return branch;
      }
    }
    return Branch.STANDALONE;
  }

  public String getCurrentBranchName() {
    String[] value = getMultiValue(getdown, "channel");
    if (value.length == 0)
      return "standalone";
    return value[0];
  }

  private String getCurrentVersion() {
    String[] value = getMultiValue(getdown, "version");
    if (value.length == 0)
      return "0";
    return value[0];
  }

  private void deleteOutdatedResources() {
    String[] toDelete = getMultiValue(getdown, "delete");
    for (String path : toDelete) {
      try {
        if (Files.deleteIfExists(Paths.get(path))) {
          log.debug("Deleted outdated file: " + path);
        }
      } catch (IOException e) {
        log.warn("Could not delete outdated file", e);
      }
    }
  }

  private String[] getMultiValue(Map<String, Object> data, String name) {
    // safe way to call this and avoid NPEs
    String[] array = ConfigUtil.getMultiValue(data, name);
    if (array == null)
      return new String[0];
    return array;
  }

  private void upgrade(String desc, File oldgd, File curgd, File newgd) {
    if (!newgd.exists() || newgd.length() == curgd.length()
        || Util.compareCreationTime(newgd, curgd) == 0) {
      log.debug("Resource {} is up to date", desc);
      return;
    }
    log.info("Upgrade {} with {}...", desc, newgd);
    if (oldgd.exists()) {
      oldgd.delete();
    }
    if (!curgd.exists() || curgd.renameTo(oldgd)) {
      if (newgd.renameTo(curgd)) {
        oldgd.delete();
        try {
          Util.copy(new FileInputStream(curgd), new FileOutputStream(newgd));
        } catch (IOException e) {
          log.warn("Problem copying {} back: {}", desc, e);
        }
        return;
      }
      log.warn("Unable to rename to {}", oldgd);
      if (!oldgd.renameTo(curgd)) {
        log.warn("Could not rename {} to {}", oldgd, curgd);
      }
    }
    log.info("Attempting to upgrade by copying over " + curgd + "...");
    try {
      Util.copy(new FileInputStream(newgd), new FileOutputStream(curgd));
    } catch (IOException e) {
      log.warn("Brute force copy method also failed", e);
    }
  }

  private void upgradeLauncher() {
    File oldgd = new File("../lawena-old.exe");
    File curgd = new File("../lawena.exe");
    File newgd = new File("code/lawena-new.exe");
    upgrade("Lawena launcher", oldgd, curgd, newgd);
  }

  private void upgradeGetdown() {
    File oldgd = new File("getdown-client-old.jar");
    File curgd = new File("getdown-client.jar");
    File newgd = new File("code/getdown-client-new.exe");
    upgrade("Lawena updater", oldgd, curgd, newgd);
  }

  public List<Branch> getBranches() {
    if (branches == null || branches.isEmpty()) {
      branches = loadBranches();
    }
    return branches;
  }

  private List<Branch> loadBranches() {
    String[] value = getMultiValue(getdown, "channels");
    String url = value.length > 0 ? value[0] : DEFAULT_BRANCHES;
    File file = new File("channels.json").getAbsoluteFile();
    List<Branch> list = Collections.emptyList();
    try {
      Resource res = new Resource(file.getName(), new URL(url), file, false);
      lastCheck = System.currentTimeMillis();
      if (download(res)) {
        try {
          Reader reader = new FileReader(file);
          Type token = new TypeToken<List<Branch>>() {}.getType();
          list = gson.fromJson(reader, token);
          for (Branch branch : list) {
            if (branch.getType() == Branch.Type.SNAPSHOT) {
              getBuildList(branch);
            }
          }
          reader.close();
        } catch (JsonSyntaxException | JsonIOException e) {
          log.warn("Invalid latest version file found: " + e);
        } catch (FileNotFoundException e) {
          log.info("No latest version file found");
        } catch (IOException e) {
          log.warn("Could not close reader stream", e);
        }
        res.erase();
      }
    } catch (MalformedURLException e) {
      log.warn("Invalid URL: " + e);
    }
    try {
      Files.deleteIfExists(file.toPath());
    } catch (IOException e) {
      log.debug("Could not delete branches file: " + e);
    }
    return list;
  }

  private boolean download(Resource... res) {
    return new HTTPDownloader(Arrays.asList(res), new Observer() {

      @Override
      public void resolvingDownloads() {}

      @Override
      public boolean downloadProgress(int percent, long remaining) {
        return !Thread.currentThread().isInterrupted();
      }

      @Override
      public void downloadFailed(Resource rsrc, Exception e) {
        log.warn("Download failed: {}", e.toString());
      }
    }).download();
  }

  public void fileCleanup() {
    deleteOutdatedResources();
    upgradeLauncher();
    upgradeGetdown();
  }

  public UpdateResult checkForUpdates() {
    SortedSet<Build> buildList = getBuildList(getCurrentBranch());
    if (buildList.isEmpty()) {
      return UpdateResult.notFound("No builds were found for the current branch");
    }
    Build latest = buildList.first();
    try {
      long current = Long.parseLong(getCurrentVersion());
      if (current < latest.getTimestamp()) {
        return UpdateResult.found(latest);
      } else {
        return UpdateResult.latest("You already have the latest version");
      }
    } catch (NumberFormatException e) {
      log.warn("Bad version format: {}", getCurrentVersion());
      return UpdateResult.found(latest);
    }
  }

  public boolean upgradeApplication(Build build) {
    try {
      return LaunchUtil.updateVersionAndRelaunch(new File("").getAbsoluteFile(),
          "getdown-client.jar", build.getName());
    } catch (IOException e) {
      log.warn("Could not complete the upgrade", e);
    }
    return false;
  }

  /**
   * A standalone installation means that no deployment descriptor file was found on the application
   * folder.
   * 
   * @return <code>true</code> if this install is standalone or <code>false</code> if it is not
   */
  public boolean isStandalone() {
    return standalone;
  }

  public boolean createVersionFile(String version) {
    Path path = Paths.get("version.txt");
    try {
      path = Files.write(path, Arrays.asList(version), Charset.defaultCharset());
      log.debug("Version file created at {}", path.toAbsolutePath());
      return true;
    } catch (IOException e) {
      log.warn("Could not create version file", e);
      return false;
    }
  }

  public String getLastCheckString() {
    if (lastCheck == 0L) {
      return "Never";
    }
    return convertTime(lastCheck);
  }

  private String convertTime(long time) {
    Date date = new Date(time);
    Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return format.format(date);
  }

  public void switchBranch(Branch newBranch) throws IOException {
    String appbase = newBranch.getUrl() + "latest/";
    Path getdownPath = Paths.get("getdown.txt");
    try {
      Files.copy(getdownPath, Paths.get("getdown.bak.txt"));
    } catch (IOException e) {
      log.warn("Could not backup updater metadata file", e);
    }
    List<String> lines = new ArrayList<>();
    lines.add("appbase = " + appbase);
    try {
      for (String line : Files.readAllLines(getdownPath, Charset.defaultCharset())) {
        if (line.startsWith("ui.")) {
          lines.add(line);
        }
      }
    } catch (IOException e) {
      log.warn("Could not read current updater metadata file", e);
    }
    Files.write(getdownPath, lines, Charset.defaultCharset());
    log.info("New updater metadata file created");
  }

  public List<String> getChangeLog(Branch branch) {
    List<String> list = branch.getChangeLog();
    if (list != null) {
      return list;
    }
    list = Collections.emptyList();
    String url = branch.getUrl();
    if (url == null) {
      return list;
    }
    url = url + "changelog.txt";
    File file = new File("changelog.txt").getAbsoluteFile();
    try {
      Resource res = new Resource(file.getName(), new URL(url), file, false);
      if (download(res)) {
        try {
          list = Files.readAllLines(file.toPath(), Charset.defaultCharset());
          branch.setChangeLog(list);
        } catch (IOException e) {
          log.warn("Could not read lines from file", e);
        }
        res.erase();
      }
    } catch (MalformedURLException e) {
      log.warn("Invalid URL: " + e);
    }
    return list;
  }
}
