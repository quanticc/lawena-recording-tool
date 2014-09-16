package com.github.lawena.app.model;

import static com.github.lawena.util.Util.toPath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import joptsimple.OptionSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.os.LinuxInterface;
import com.github.lawena.os.OSInterface;
import com.github.lawena.os.OSXInterface;
import com.github.lawena.os.WindowsInterface;
import com.github.lawena.profile.Key;
import com.github.lawena.update.Updater;
import com.github.lawena.util.ImageStore;
import com.github.lawena.util.Util;
import com.github.lawena.vdm.DemoEditor;

public class MainModel {

  private static final Logger log = LoggerFactory.getLogger(MainModel.class);

  private Map<String, String> versionData;
  private OSInterface osInterface;
  private Updater updater;
  private Linker linker;

  private String originalDxLevel;
  private Resources resources;
  private DemoEditor demos;
  private ImageStore skyboxPreviewStore;
  private Settings settings;

  public MainModel(OptionSet optionSet) {
    this.settings = new Settings(optionSet);
    this.versionData = loadVersionData();
    this.updater = new Updater();
    logVMInfo();
    configureOSInterface();

    originalDxLevel = osInterface.getSystemDxLevel();

    Path gamePath = toPath(Key.gamePath.getValue(settings));
    if (gamePath == null || gamePath.toString().isEmpty()) {
      gamePath = osInterface.getSteamPath().resolve(Key.relativeDefaultGamePath.getValue(settings));
    }
    Key.gamePath.setValueEx(settings, gamePath.toString());

    settings.save();

    linker = new Linker(this);
    linker.unlink();

    resources = new Resources();

    demos = new DemoEditor(settings, osInterface);
    skyboxPreviewStore = new ImageStore();
    loadSkyboxData();
  }

  private void configureOSInterface() {
    String osname = System.getProperty("os.name");
    if (osname.contains("Windows")) {
      osInterface = new WindowsInterface();
    } else if (osname.contains("Linux")) {
      osInterface = new LinuxInterface();
    } else if (osname.contains("OS X")) {
      osInterface = new OSXInterface();
    } else {
      throw new UnsupportedOperationException("OS not supported");
    }
    osInterface.setLookAndFeel();
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
