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

/**
 * Contains all user modifiable values, essential to the MVP pattern. A presenter will contain an
 * instance of this class to mediate changes to and from the View.
 * 
 * @author Ivan
 *
 */
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
    Path profilesPath = defaultFile.toPath().resolveSibling("profiles.json"); //$NON-NLS-1$
    if (!Files.exists(profilesPath)) {
      Files.copy(defaultFile.toPath(), profilesPath);
    }
    this.settings = new Settings(profilesPath.toFile(), defaultFile);
    this.versionData = loadVersionData();
    this.updater = new Updater();
    this.osInterface = os;
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

    logVMInfo();

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
    try {
      // 50 51 5a 5f 62
      int dx = Integer.decode("0x" + level); //$NON-NLS-1$
      log.debug("System dxlevel: {}", dx); //$NON-NLS-1$
    } catch (NumberFormatException e) {
      log.warn("Invalid system dxlevel value found: {}. Reverting to 95", level); //$NON-NLS-1$
      osInterface.setSystemDxLevel("5f"); //$NON-NLS-1$
      return "5f"; //$NON-NLS-1$
    }
    return level;
  }

  private Map<String, String> loadVersionData() {
    Map<String, String> map = new LinkedHashMap<>();
    String impl = this.getClass().getPackage().getImplementationVersion();
    if (impl != null) {
      map.put("version", impl); //$NON-NLS-1$
      String[] arr = impl.split("-"); //$NON-NLS-1$
      map.put("shortVersion", arr[0] //$NON-NLS-1$
          + (arr.length > 1 ? "-" + (arr.length > 2 ? arr[1] + "-" + arr[2] : arr[1]) : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    } else {
      map.put("version", "4.2 no-git"); //$NON-NLS-1$ //$NON-NLS-2$
      map.put("shortVersion", "4.2"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    map.put("build", Util.getManifestString("Implementation-Build", Util.now("yyyyMMddHHmmss"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    return map;
  }

  private void logVMInfo() {
    Path emptyPath = toPath(""); //$NON-NLS-1$
    Path steamPath = toPath(Key.steamPath.getValue(settings));
    Path gamePath = toPath(Key.gamePath.getValue(settings));
    Path recPath = toPath(Key.recordingPath.getValue(settings));
    // saving essential info to log for troubleshooting
    log.debug("----------------- Lawena Recording Tool -----------------"); //$NON-NLS-1$
    log.debug("v {} {} [{}]", getFullVersion(), getBuildTime(), updater.getCurrentBranchName()); //$NON-NLS-1$
    log.debug("------------------------ VM Info ------------------------"); //$NON-NLS-1$
    log.debug("OS name: {} {}", System.getProperty("os.name"), System.getProperty("os.arch")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    log.debug("Java version: {}", System.getProperty("java.version")); //$NON-NLS-1$ //$NON-NLS-2$
    log.debug("Java home: {}", System.getProperty("java.home")); //$NON-NLS-1$ //$NON-NLS-2$
    log.debug("------------------------ Folders ------------------------"); //$NON-NLS-1$
    if (emptyPath.equals(steamPath)) {
      log.warn("Steam: <No folder defined>"); //$NON-NLS-1$
    } else {
      log.debug("Steam: {}", steamPath); //$NON-NLS-1$
    }
    if (emptyPath.equals(gamePath)) {
      log.warn("Game: <No folder defined>"); //$NON-NLS-1$
    } else {
      log.debug("Game: {}", gamePath); //$NON-NLS-1$
    }
    if (emptyPath.equals(recPath)) {
      log.warn("Segments: <No folder defined>"); //$NON-NLS-1$
    } else {
      log.debug("Segments: {}", recPath); //$NON-NLS-1$
    }
    log.debug("Lawena: {}", Paths.get("").toAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
    log.debug("---------------------------------------------------------"); //$NON-NLS-1$
  }

  public String getFullVersion() {
    return versionData.get("version"); //$NON-NLS-1$
  }

  public String getShortVersion() {
    return versionData.get("shortVersion"); //$NON-NLS-1$
  }

  public String getBuildTime() {
    return versionData.get("build"); //$NON-NLS-1$
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
      log.debug("Skybox data loaded from {}", skySerialFile); //$NON-NLS-1$
    } catch (ClassNotFoundException | IOException e) {
      log.warn("Could not read skybox data from file: {}", e.toString()); //$NON-NLS-1$
    }
  }

  public void saveSkyboxData() {
    Path base = settings.getParentDataPath();
    Path skySerialFile = base.resolve(Key.skyPreviewSavePath.getValue(settings));
    try {
      skyboxPreviewStore.save(skySerialFile.toFile());
      log.debug("Skybox data saved to {}", skySerialFile); //$NON-NLS-1$
    } catch (IOException e) {
      log.warn("Could not save skybox data to file: {}", e.toString()); //$NON-NLS-1$
    }
  }

  public Linker getLinker() {
    return linker;
  }

}
