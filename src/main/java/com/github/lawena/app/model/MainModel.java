package com.github.lawena.app.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JFileChooser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.model.LwrtMovies;
import com.github.lawena.model.LwrtSettings;
import com.github.lawena.model.Skyboxes;
import com.github.lawena.model.LwrtSettings.Key;
import com.github.lawena.os.LinuxInterface;
import com.github.lawena.os.OSInterface;
import com.github.lawena.os.OSXInterface;
import com.github.lawena.os.WindowsInterface;
import com.github.lawena.update.Updater;
import com.github.lawena.util.Util;
import com.github.lawena.vdm.DemoEditor;

public class MainModel {

  private static final Logger log = LoggerFactory.getLogger(MainModel.class);

  private LwrtSettings settings;
  private Map<String, String> versionData;
  private OSInterface osInterface;
  private Updater updater;
  private Linker linker;

  private String originalDxLevel;
  private Path steamPath;
  private JFileChooser movieFileChooser;
  private JFileChooser gameFileChooser;

  private LwrtMovies movies;
  private Resources resources;
  private DemoEditor demos;
  private Skyboxes skyboxes;

  public MainModel(LwrtSettings settingsManager) {
    this.settings = settingsManager;
    this.versionData = loadVersionData();
    this.updater = new Updater();

    logVMInfo();
    loadOsInterface();

    originalDxLevel = osInterface.getSystemDxLevel();
    steamPath = osInterface.getSteamPath();

    Path tfpath = settings.getTfPath();
    if (tfpath == null || tfpath.toString().isEmpty()) {
      tfpath = steamPath.resolve("SteamApps/common/Team Fortress 2/tf");
    }
    if (!Files.exists(tfpath)) {
      tfpath = getChosenTfPath();
      if (tfpath == null) {
        log.info("No tf directory specified, exiting.");
        throw new IllegalArgumentException("Must set a game folder");
      }
    }
    settings.setTfPath(tfpath);

    Path moviepath = settings.getMoviePath();
    if (moviepath == null || moviepath.toString().isEmpty() || !Files.exists(moviepath)) {
      moviepath = getChosenMoviePath();
      if (moviepath == null) {
        log.info("No movie directory specified, exiting.");
        throw new IllegalArgumentException("Must set a segment folder");
      }
    }
    movies = new LwrtMovies(settings);
    settings.setMoviePath(moviepath);

    settings.save();

    linker = new Linker(this);
    linker.unlink();

    resources = new Resources();

    demos = new DemoEditor(settings, osInterface);
    skyboxes = new Skyboxes();
    loadSkyboxData();
  }

  private void loadOsInterface() {
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
    // saving essential info to log for troubleshooting
    log.debug("----------------- Lawena Recording Tool -----------------");
    log.debug("v {} {} [{}]", getFullVersion(), getBuildTime(), updater.getCurrentBranchName());
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

  public LwrtSettings getSettings() {
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

  public Path getSteamPath() {
    return steamPath;
  }

  public Path getChosenMoviePath() {
    Path selected = null;
    int ret = 0;
    while ((selected == null && ret == 0) || (selected != null && !Files.exists(selected))) {
      movieFileChooser = new JFileChooser();
      movieFileChooser.setDialogTitle("Choose a directory to store your movie files");
      movieFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      ret = movieFileChooser.showOpenDialog(null);
      if (ret == JFileChooser.APPROVE_OPTION) {
        selected = movieFileChooser.getSelectedFile().toPath();
      } else {
        selected = null;
      }
    }
    return selected;
  }

  public Path getChosenTfPath() {
    Path selected = null;
    int ret = 0;
    while ((selected == null && ret == 0)
        || (selected != null && (!Files.exists(selected) || !selected.toFile().getName().toString()
            .equals("tf")))) {
      gameFileChooser = new JFileChooser();
      gameFileChooser.setDialogTitle("Choose your \"tf\" directory");
      gameFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      gameFileChooser.setCurrentDirectory(steamPath.toFile());
      gameFileChooser.setFileHidingEnabled(false);
      ret = gameFileChooser.showOpenDialog(null);
      if (ret == JFileChooser.APPROVE_OPTION) {
        selected = gameFileChooser.getSelectedFile().toPath();
      } else {
        selected = null;
      }
      log.debug("Selected path: " + selected);
    }
    return selected;
  }

  public DemoEditor getDemos() {
    return demos;
  }

  public LwrtMovies getMovies() {
    return movies;
  }

  public Resources getResources() {
    return resources;
  }

  public Skyboxes getSkyboxes() {
    return skyboxes;
  }

  public void loadSkyboxData() {
    File skySerialFile = new File(settings.getString(Key.SkyboxDataSavePath));
    if (!Files.exists(skySerialFile.toPath()))
      return;
    try {
      skyboxes.load(skySerialFile);
      log.debug("Skybox data loaded from {}", skySerialFile);
    } catch (ClassNotFoundException | IOException e) {
      log.warn("Could not read skybox data from file: " + e);
    }
  }

  public void saveSkyboxData() {
    File skySerialFile = new File(settings.getString(Key.SkyboxDataSavePath));
    try {
      skyboxes.save(skySerialFile);
      log.debug("Skybox data saved to {}", skySerialFile);
    } catch (IOException e) {
      log.warn("Could not save skybox data to file: " + e);
    }
  }

  public Linker getLinker() {
    return linker;
  }

}
