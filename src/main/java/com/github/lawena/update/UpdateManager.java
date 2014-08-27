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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

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

public class UpdateManager {

  private static final Logger log = LoggerFactory.getLogger(UpdateManager.class);
  private static final String DEFAULT_CHANNELS =
      "https://dl.dropboxusercontent.com/u/74380/lwrt/channels.json";

  private boolean standalone = false;
  private final Map<String, Object> getdown;
  private final Gson gson = new Gson();
  private List<Channel> channels;

  private long lastCheck = 0L;
  private SortedSet<BuildInfo> buildList;

  public UpdateManager() {
    this(Paths.get("getdown.txt"));
  }

  public UpdateManager(Path getdownPath) {
    this.getdown = parseConfig(getdownPath);
    this.channels = loadChannels();
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

  private SortedSet<BuildInfo> getVersionList(String channelName) {
    for (Channel channel : getChannels()) {
      if (channel.getId().equals(channelName)) {
        return getVersionList(channel);
      }
    }
    throw new IllegalArgumentException("Channel name not found");
  }

  private SortedSet<BuildInfo> getVersionList(Channel channel) {
    String name = "buildlist.txt";
    SortedSet<BuildInfo> set = new TreeSet<BuildInfo>();
    try {
      File local = new File(name).getAbsoluteFile();
      URL url = new URL(channel.getUrl() + name);
      Resource res = new Resource(local.getName(), url, local, false);
      if (download(res)) {
        try {
          for (String line : Files.readAllLines(local.toPath(), Charset.defaultCharset())) {
            String[] data = line.split(";");
            set.add(new BuildInfo(data));
          }
        } catch (IOException e) {
          log.warn("Could not read lines from file: " + e);
        }
        res.erase();
      }
    } catch (MalformedURLException e) {
      log.warn("Invalid URL: " + e);
    }
    return set;
  }

  public String getCurrentChannel() {
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

  private List<Channel> getChannels() {
    List<Channel> newlist = loadChannels();
    if (channels == null || !newlist.isEmpty()) {
      channels = newlist;
    }
    return channels;
  }

  private List<Channel> loadChannels() {
    // don't load channels in standalone mode
    if (standalone)
      return Collections.emptyList();
    String[] value = getMultiValue(getdown, "channels");
    String url = value.length > 0 ? value[0] : DEFAULT_CHANNELS;
    File file = new File("channels.json").getAbsoluteFile();
    try {
      Resource res = new Resource(file.getName(), new URL(url), file, false);
      if (download(res)) {
        try {
          Reader reader = new FileReader(file);
          Type token = new TypeToken<List<Channel>>() {}.getType();
          return gson.fromJson(reader, token);
        } catch (JsonSyntaxException | JsonIOException e) {
          log.warn("Invalid latest version file found: " + e);
        } catch (FileNotFoundException e) {
          log.info("No latest version file found");
        }
        res.erase();
      }
    } catch (MalformedURLException e) {
      log.warn("Invalid URL: " + e);
    }
    try {
      Files.deleteIfExists(file.toPath());
    } catch (IOException e) {
      log.debug("Could not delete channels file: " + e);
    }
    return Collections.emptyList();
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

  public void cleanup() {
    deleteOutdatedResources();
    upgradeLauncher();
    upgradeGetdown();
  }

  public UpdateResult checkForUpdates(boolean force) {
    long now = System.currentTimeMillis();
    long delta = TimeUnit.MILLISECONDS.toHours(now - lastCheck);
    if (force || delta >= 1 || buildList == null || buildList.isEmpty()) {
      lastCheck = System.currentTimeMillis();
      buildList = getVersionList(getCurrentChannel());
    }
    if (buildList.isEmpty()) {
      return UpdateResult.notFound("No builds were found for this channel");
    }
    BuildInfo latest = buildList.last();
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

  public boolean upgradeApplication(BuildInfo build) {
    try {
      return LaunchUtil.updateVersionAndRelaunch(new File(""), "getdown-client.jar",
          build.getName());
    } catch (IOException e) {
      log.warn("Could not complete the upgrade", e);
    }
    return false;
  }

  public boolean isStandalone() {
    return standalone;
  }
}
