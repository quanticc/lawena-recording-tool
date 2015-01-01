package com.github.lawena.app.model;

import static com.github.lawena.util.Util.toPath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.os.OSInterface;
import com.github.lawena.profile.Key;
import com.github.lawena.update.Updater;
import com.github.lawena.util.ImageStore;
import com.github.lawena.util.Util;
import com.github.lawena.vdm.DemoEditor;

public class MainModel {

  private static final Logger log = LoggerFactory.getLogger(MainModel.class);

  private Map<String, String> versionData;
  private final OSInterface osInterface;
  private Updater updater;
  private Linker linker;

  private String originalDxLevel;
  private Resources resources;
  private DemoEditor demos;
  private ImageStore skyboxPreviewStore;
  private Settings settings;

  public MainModel(File defaultFile, OSInterface os, Linker ln) throws IOException {
    Path profilesPath = defaultFile.toPath().resolveSibling("profiles.json");
    if (!Files.exists(profilesPath)) {
      Files.copy(defaultFile.toPath(), profilesPath);
    }
    this.settings = new Settings(profilesPath.toFile(), defaultFile);
    this.versionData = loadVersionData();
    this.updater = new Updater();
    this.osInterface = os;
    logVMInfo();
    os.setLookAndFeel();

    originalDxLevel = getSystemDxLevel();

    Path steamPath = toPath(Key.steamPath.getValue(settings));
    if (steamPath == null || steamPath.toString().isEmpty()) {
      steamPath = osInterface.getSteamPath();
    }
    Key.steamPath.setValue(settings, steamPath.toString());

    Path gamePath = toPath(Key.gamePath.getValue(settings));
    if (gamePath == null || gamePath.toString().isEmpty()) {
      gamePath = steamPath.resolve(Key.relativeDefaultGamePath.getValue(settings));
    }
    Key.gamePath.setValueEx(settings, gamePath.toString());

    settings.save();

    linker = ln;
    linker.setModel(this);
    linker.unlink();

    resources = new Resources();

    demos = new DemoEditor(settings, osInterface);
    skyboxPreviewStore = new ImageStore();
    loadSkyboxData();
  }

  private String getSystemDxLevel() {
    String level = osInterface.getSystemDxLevel();
    switch (level) {
      case "62":
        log.debug("System dxlevel: 98");
        break;
      case "5f":
        log.debug("System dxlevel: 95");
        break;
      case "5a":
        log.debug("System dxlevel: 90");
        break;
      case "51":
        log.debug("System dxlevel: 81");
        break;
      case "50":
        log.debug("System dxlevel: 80");
        break;
      default:
        log.warn("Invalid system dxlevel value found: {}. Reverting to 95", level);
        osInterface.setSystemDxLevel("5f");
        return "5f";
    }
    return level;
  }

  private Map<String, String> loadVersionData() {
    Map<String, String> map = new LinkedHashMap<>();
    String impl = this.getClass().getPackage().getImplementationVersion();
    if (impl != null) {
      map.put("version", impl);
      String[] arr = impl.split("-");
      map.put("shortVersion", arr[0] + (arr.length > 1 ? "-" + arr[1] : ""));
    } else {
      map.put("version", "4.2 no-git");
      map.put("shortVersion", "4.2");
    }
    map.put("build", Util.getManifestString("Implementation-Build", Util.now("yyyyMMddHHmmss")));
    return map;
  }

  private void logVMInfo() {
    Path emptyPath = toPath("");
    Path steamPath = toPath(Key.steamPath.getValue(settings));
    Path gamePath = toPath(Key.gamePath.getValue(settings));
    Path recPath = toPath(Key.recordingPath.getValue(settings));
    // saving essential info to log for troubleshooting
    log.debug("----------------- Lawena Recording Tool -----------------");
    log.debug("v {} {} [{}]", getFullVersion(), getBuildTime(), updater.getCurrentBranchName());
    log.debug("------------------------ VM Info ------------------------");
    log.debug("OS name: {} {}", System.getProperty("os.name"), System.getProperty("os.arch"));
    log.debug("Java version: {}", System.getProperty("java.version"));
    log.debug("Java home: {}", System.getProperty("java.home"));
    log.debug("------------------------ Folders ------------------------");
    if (emptyPath.equals(steamPath)) {
      log.warn("Steam: <No folder defined>");
    } else {
      log.debug("Steam: {}", steamPath);
    }
    if (emptyPath.equals(gamePath)) {
      log.warn("Game: <No folder defined>");
    } else {
      log.debug("Game: {}", gamePath);
    }
    if (emptyPath.equals(recPath)) {
      log.warn("Segments: <No folder defined>");
    } else {
      log.debug("Segments: {}", recPath);
    }
    log.debug("Lawena: {}", Paths.get("").toAbsolutePath());
    log.debug("---------------------------------------------------------");
  }

  public String getFullVersion() {
    return versionData.get("version");
  }

  public String getShortVersion() {
    return versionData.get("shortVersion");
  }

  public String getBuildTime() {
    return versionData.get("build");
  }

  public Settings getSettings() {
    return settings;
  }

  public OSInterface getOsInterface() {
    return osInterface;
  }

  public Updater getUpdater() {
    return updater;
  }

  public String getOriginalDxLevel() {
    return originalDxLevel;
  }

  public DemoEditor getDemos() {
    return demos;
  }

  public Resources getResources() {
    return resources;
  }

  public ImageStore getSkyboxPreviewStore() {
    return skyboxPreviewStore;
  }

  public void loadSkyboxData() {
    Path base = settings.getParentDataPath();
    Path skySerialFile = base.resolve(Key.skyPreviewSavePath.getValue(settings));
    if (!Files.exists(skySerialFile))
      return;
    try {
      skyboxPreviewStore.load(skySerialFile.toFile());
      log.debug("Skybox data loaded from {}", skySerialFile);
    } catch (ClassNotFoundException | IOException e) {
      log.warn("Could not read skybox data from file: " + e);
    }
  }

  public void saveSkyboxData() {
    Path base = settings.getParentDataPath();
    Path skySerialFile = base.resolve(Key.skyPreviewSavePath.getValue(settings));
    try {
      skyboxPreviewStore.save(skySerialFile.toFile());
      log.debug("Skybox data saved to {}", skySerialFile);
    } catch (IOException e) {
      log.warn("Could not save skybox data to file: " + e);
    }
  }

  public Linker getLinker() {
    return linker;
  }

}
