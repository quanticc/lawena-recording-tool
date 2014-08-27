package com.github.lawena.app;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.lwrt.CLLinux;
import com.github.lawena.lwrt.CLOSX;
import com.github.lawena.lwrt.CLWindows;
import com.github.lawena.lwrt.CommandLine;
import com.github.lawena.lwrt.SettingsManager;
import com.github.lawena.update.UpdateManager;
import com.github.lawena.util.Util;

public class MainModel {

  private static final Logger log = LoggerFactory.getLogger(MainModel.class);

  private SettingsManager settings;
  private Map<String, String> versionData;
  private CommandLine osInterface;
  private UpdateManager updater;

  public MainModel(SettingsManager settingsManager) {
    this.settings = settingsManager;
    this.versionData = loadVersionData();
    this.updater = new UpdateManager();
    logVMInfo();
    loadOsInterface();
  }

  private void loadOsInterface() {
    String osname = System.getProperty("os.name");
    if (osname.contains("Windows")) {
      osInterface = new CLWindows();
    } else if (osname.contains("Linux")) {
      osInterface = new CLLinux();
    } else if (osname.contains("OS X")) {
      osInterface = new CLOSX();
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
      map.put("version", "v4 no-git");
      map.put("shortVersion", "v4");
    }
    map.put("build", Util.getManifestString("Implementation-Build", Util.now("yyyyMMddHHmmss")));
    return map;
  }

  private void logVMInfo() {
    // saving essential info to log for troubleshooting
    log.debug("----------------- Lawena Recording Tool -----------------");
    log.debug("v {} {} [{}]", getFullVersion(), getBuildTime(), updater.getCurrentChannel());
    log.debug("------------------------ VM Info ------------------------");
    log.debug("OS name: {} {}", System.getProperty("os.name"), System.getProperty("os.arch"));
    log.debug("Java version: {}", System.getProperty("java.version"));
    log.debug("Java home: {}", System.getProperty("java.home"));
    log.debug("------------------------ Folders ------------------------");
    log.debug("Game: {}", settings.getTfPath());
    log.debug("Segments: {}", settings.getMoviePath());
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

  public SettingsManager getSettings() {
    return settings;
  }

  public CommandLine getOsInterface() {
    return osInterface;
  }

  public UpdateManager getUpdater() {
    return updater;
  }

}
