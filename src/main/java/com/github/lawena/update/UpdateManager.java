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

public class UpdateManager {

  private static final Logger log = LoggerFactory.getLogger(UpdateManager.class);

  private final Map<String, Object> getdown;
  private final Gson gson = new Gson();
  private List<Channel> channels;

  public UpdateManager() throws IOException {
    this(Paths.get("getdown.txt"));
  }

  public UpdateManager(Path getdownPath) throws IOException {
    this.getdown = parseConfig(getdownPath);
  }

  private Map<String, Object> parseConfig(Path path) throws IOException {
    return ConfigUtil.parseConfig(path.toFile(), false);
  }

  public boolean isLatestVersion() {
    return isLatestVersion(getCurrentChannel(), getCurrentVersion());
  }

  boolean isLatestVersion(String channel, String version) {
    Channel current = null;
    for (Channel c : getChannels()) {
      if (c.getId().equals(channel)) {
        current = c;
      }
    }
    if (current == null || version == null)
      return true;
    long v;
    try {
      v = Long.parseLong(version);
    } catch (NumberFormatException e) {
      log.warn("Invalid version number: {}", version);
      return true;
    }
    SortedSet<VersionInfo> versions = getVersionList(current);
    if (!versions.isEmpty()) {
      return v < versions.last().getTimestamp();
    }
    log.warn("Could not retrieve latest version");
    return true;
  }

  public SortedSet<VersionInfo> getVersionList(Channel current) {
    String name = "buildlist";
    SortedSet<VersionInfo> set = new TreeSet<VersionInfo>();
    try {
      File local = new File(name).getAbsoluteFile();
      URL url = new URL(current.getUrl() + name);
      Resource res = new Resource(local.getName(), url, local, false);
      if (download(res)) {
        try {
          for (String line : Files.readAllLines(local.toPath(), Charset.defaultCharset())) {
            String[] data = line.split(";");
            set.add(new VersionInfo(data));
          }
        } catch (IOException e) {
          log.warn("Could not read lines from file: " + e);
        }
      }
    } catch (MalformedURLException e) {
      log.warn("Invalid URL: " + e);
    }
    return set;
  }

  public String getCurrentChannel() {
    String[] value = ConfigUtil.getMultiValue(getdown, "channel");
    if (value == null)
      return "none";
    return value[0];
  }

  public String getCurrentVersion() {
    String[] value = ConfigUtil.getMultiValue(getdown, "version");
    if (value == null)
      return "0";
    return value[0];
  }

  public void deleteDeprecatedResources() {
    String[] toDelete = ConfigUtil.getMultiValue(getdown, "delete");
    for (String path : toDelete) {
      try {
        if (Files.deleteIfExists(Paths.get(path))) {
          log.info("Deleted deprecated file: " + path);
        }
      } catch (IOException e) {
        log.info("Could not delete deprecated file at " + path + ": " + e);
      }
    }
  }

  public void upgradeLauncher() {
    File oldgd = new File("../lawena-old.exe");
    File curgd = new File("../lawena.exe");
    File newgd = new File("code/lawena-new.exe");
    if (!newgd.exists() || newgd.length() == curgd.length()
        || Util.compareCreationTime(newgd, curgd) == 0) {
      return;
    }
    log.info("Updating launcher with " + newgd + "...");
    if (oldgd.exists()) {
      oldgd.delete();
    }
    if (!curgd.exists() || curgd.renameTo(oldgd)) {
      if (newgd.renameTo(curgd)) {
        oldgd.delete();
        try {
          Util.copy(new FileInputStream(curgd), new FileOutputStream(newgd));
        } catch (IOException e) {
          log.warn("Problem copying updated launcher back: " + e);
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

  public void upgradeGetdown() {
    File oldgd = new File("getdown-client-old.jar");
    File curgd = new File("getdown-client.jar");
    File newgd = new File("code/getdown-client-new.exe");
    LaunchUtil.upgradeGetdown(oldgd, curgd, newgd);
  }

  public List<Channel> getChannels() {
    List<Channel> newlist = loadChannels();
    if (channels == null || !newlist.isEmpty()) {
      channels = newlist;
    }
    return channels;
  }

  private List<Channel> loadChannels() {
    String url = "https://dl.dropboxusercontent.com/u/74380/lwrt/channels.json";
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
}
