package com.github.lawena.app;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.lwrt.CLLinux;
import com.github.lawena.lwrt.CLOSX;
import com.github.lawena.lwrt.CLWindows;
import com.github.lawena.lwrt.CommandLine;
import com.github.lawena.lwrt.SettingsManager;
import com.github.lawena.util.Util;

public class MainModel {

  private static final Logger log = LoggerFactory.getLogger(MainModel.class);

  private SettingsManager settings;
  private Map<String, String> ver;
  private CommandLine osInterface;

  public MainModel(SettingsManager settingsManager) {
    this.settings = settingsManager;
    this.ver = loadVersionData();
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
    log.debug("----------- Lawena Recording Tool -----------");
    log.debug("Version: {} {}", getFullVersion(), getBuildTime());
    log.debug("------------------ VM Info ------------------");
    log.debug("OS name: {}", System.getProperty("os.name"));
    log.debug("OS arch: {}", System.getProperty("os.arch"));
    log.debug("OS vers: {}", System.getProperty("os.version"));
    log.debug("Java vers: {}", System.getProperty("java.version"));
    log.debug("Java home: {}", System.getProperty("java.home"));
    log.debug("---------------------------------------------");
  }

  public String getFullVersion() {
    return ver.get("version");
  }

  public String getShortVersion() {
    return ver.get("shortVersion");
  }

  public String getBuildTime() {
    return ver.get("build");
  }

  public SettingsManager getSettings() {
    return settings;
  }
  
  public CommandLine getOsInterface() {
    return osInterface;
  }

}
