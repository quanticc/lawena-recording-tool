package com.github.lawena.app.model;

import static com.github.lawena.util.Util.toPath;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.profile.Key;
import com.github.lawena.util.Consumer;
import com.github.lawena.util.LawenaException;
import com.github.lawena.util.Util;

public class LinkerTf implements Linker {

  private static final Logger log = LoggerFactory.getLogger(LinkerTf.class);

  private MainModel model;
  private Settings settings;
  private Map<Path, Path> links = new LinkedHashMap<>();

  public void setModel(MainModel model) {
    this.model = model;
    this.settings = model.getSettings();
  }

  /**
   * Create links between original resources and launch/cfg and launch/custom folders.
   * 
   * @throws LawenaException
   */
  public void link() throws LawenaException {
    links.clear();
    Path gamePath = toPath(Key.gamePath.getValue(settings));
    Path base = settings.getParentDataPath(); // should be lwrt/tf

    // Abort if user folders exist, that means cleanup is not complete
    Path userConfigPath = gamePath.resolve("lawena-user-cfg");
    Path userCustomPath = gamePath.resolve("lawena-user-custom");
    if (Files.exists(userConfigPath) || Files.exists(userCustomPath)) {
      log.warn("*** Lawena has found that your files are still present");
      log.warn("*** Your files are located in {} and {}", userConfigPath, userCustomPath);
      log.warn("*** Restart Lawena to attempt to restore your files to their original location");
      throw new LawenaException("Files not replaced due to backup folders still present");
    }

    // Moving user folders
    Path configPath = gamePath.resolve("cfg");
    Path customPath = gamePath.resolve("custom");
    try {
      log.info("Moving user config folder");
      configPath.toFile().setWritable(true);
      move(configPath, userConfigPath);
    } catch (IOException e) {
      log.warn("Could not move user config folder: " + e);
      throw new LawenaException("Failed to replace config files", e);
    }
    try {
      log.info("Moving user custom folder");
      customPath.toFile().setWritable(true);
      move(customPath, userCustomPath);
    } catch (IOException e) {
      log.warn("Could not move user custom folder: " + e);
      throw new LawenaException("Failed to replace custom files", e);
    }

    // Create config and custom folders in launch folder
    Path launchConfigPath = base.resolve("launch/cfg");
    Path launchCustomPath = base.resolve("launch/custom");
    try {
      createDirectories(launchConfigPath);
      createDirectories(launchCustomPath);
    } catch (IOException e) {
      log.warn("Could not create link folders: " + e);
      throw new LawenaException("Failed to create link folders", e);
    }

    // List of paths that will be linked from launch/custom to game folder
    List<Path> linkedPaths = new ArrayList<>();

    // Link lawena cfg files to launch folder
    Path srcConfigPath = base.resolve("config");
    linkedPaths.add(srcConfigPath);

    // Hud files
    Path srcHudPath = base.resolve("hud");
    if (Files.exists(srcHudPath)) {
      String hudFolderName = Key.hud.getValue(settings);
      if (!hudFolderName.equals("custom")) {
        linkedPaths.add(srcHudPath.resolve(hudFolderName));
      }
    }

    // Skybox files
    Path launchSkyboxPath = base.resolve("launch/skybox");
    Path contentSkyboxPath = launchSkyboxPath.resolve("materials/skybox");
    try {
      String sky = Key.skybox.getValue(settings);
      if (sky != null && !sky.isEmpty() && !sky.equals(Key.skybox.getDefaultValue())) {
        log.debug("Linking selected skybox files");
        Files.createDirectories(contentSkyboxPath);
        Set<Path> vtfPaths = new HashSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(base.resolve("skybox/vtf"))) {
          for (Path path : stream) {
            String pathStr = path.toFile().getName();
            // only save vtf files matching our selected skybox
            if (pathStr.endsWith(".vtf") && pathStr.startsWith(sky)) {
              vtfPaths.add(path);
            }
          }
        }
        Set<Path> vmtPaths = new HashSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(base.resolve("skybox/vmt"))) {
          for (Path path : stream) {
            String pathStr = path.toFile().getName();
            // collect vmt original skybox names to perform the replace later
            if (pathStr.endsWith(".vmt")) {
              vmtPaths.add(path);
              createLink(contentSkyboxPath.resolve(pathStr), path);
            }
          }
        }
        // rename selected skybox according to each original skybox name
        for (Path vtfPath : vtfPaths) {
          for (Path vmtPath : vmtPaths) {
            String vtf = vtfPath.getFileName().toString();
            String vmt = vmtPath.getFileName().toString();
            if ((vtf.endsWith("up.vtf") && vmt.endsWith("up.vmt"))
                || (vtf.endsWith("dn.vtf") && vmt.endsWith("dn.vmt"))
                || (vtf.endsWith("bk.vtf") && vmt.endsWith("bk.vmt"))
                || (vtf.endsWith("ft.vtf") && vmt.endsWith("ft.vmt"))
                || (vtf.endsWith("lf.vtf") && vmt.endsWith("lf.vmt"))
                || (vtf.endsWith("rt.vtf") && vmt.endsWith("rt.vmt"))) {
              Path link = contentSkyboxPath.resolve(vmt.substring(0, vmt.indexOf(".vmt")) + ".vtf");
              createLink(link, vtfPath);
            }
          }
        }
      }
      linkedPaths.add(launchSkyboxPath);
    } catch (IOException e) {
      log.warn("Could not link skybox files: " + e);
      throw new LawenaException("Failed to link skybox files", e);
    }

    // Other resources
    Path srcDefaultPath = base.resolve("default");
    Resources resources = model.getResources();
    log.info("Linking enabled custom vpks and folders");
    List<Path> list = new ArrayList<>();
    for (Resource r : resources.getEnabledResources()) {
      if (r.isEnabled()) {
        list.add(r.getPath());
      }
    }
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(srcDefaultPath)) {
      for (Path path : stream) {
        list.add(path);
      }
    } catch (IOException e) {
      log.warn("Could not include files from default resources folder: " + e);
    }
    for (Path src : list) {
      // relocate user custom paths since they have been moved
      if (src.startsWith(customPath)) {
        src = userCustomPath.resolve(src.getFileName());
      }
      if (Files.exists(src)) {
        if (Files.isDirectory(src) || src.getFileName().toString().endsWith(".vpk")) {
          linkedPaths.add(src);
        } else {
          log.warn("Not a valid resource type: {}", src);
        }
      } else {
        log.warn("Enabled resource does not exist: {}", src);
      }
    }

    // these paths will be linked to a custom folder
    int count = 0;
    for (Path target : linkedPaths) {
      String id = Util.leftPad(count++ + "", 3, '0');
      String key = "lawena-" + id + "-" + target.getFileName().toString();
      if (Files.isDirectory(target)) {
        createDirectoryLink(launchCustomPath.resolve(key), target);
      } else {
        createLink(launchCustomPath.resolve(key), target);
      }
    }

    // finally, link launch files to game paths
    createDirectoryLink(configPath, launchConfigPath);
    createDirectoryLink(customPath, launchCustomPath);
  }

  private void delete(Path start) throws IOException {
    Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        removeLink(file);
        if (Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
          log.trace("Deleting regular file: {}", file);
          file.toFile().setWritable(true);
          Files.delete(file);
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (exc == null) {
          removeDirectoryLink(dir);
          if (Files.exists(dir, LinkOption.NOFOLLOW_LINKS)) {
            log.trace("Deleting regular directory: {}", dir);
            Files.delete(dir);
          }
        } else {
          throw exc;
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }

  private void createDirectories(Path dir) throws IOException {
    log.trace("Creating directory: {}", dir);
    Files.createDirectories(dir);
  }

  private void move(Path src, Path dest) throws IOException {
    log.trace("Moving folder: {} -> {}", src, dest);
    src.toFile().setWritable(true);
    Files.move(src, dest);
  }

  private void createLink(Path link, Path target) {
    log.trace("Linking: {} <-> {}", link, target);
    Util.startProcess(Arrays.asList("cmd", "/c", "mklink", link.toAbsolutePath().toString(), target
        .toAbsolutePath().toString()), printer);
    links.put(link, target);
  }

  private void createDirectoryLink(Path link, Path target) {
    log.trace("Linking directory: {} <-> {}", link, target);
    Util.startProcess(Arrays.asList("cmd", "/c", "mklink", "/d", link.toAbsolutePath().toString(),
        target.toAbsolutePath().toString()), printer);
    links.put(link, target);
  }

  /**
   * Removes links created by {@link #link()} and restores folder structure to original state.
   * 
   * @return <code>true</code> if the operation was successful, <code>false</code> otherwise.
   */
  public boolean unlink() {
    // cleanup symbolic links
    for (Path link : links.keySet()) {
      if (Files.isDirectory(link)) {
        removeDirectoryLink(link);
      } else {
        removeLink(link);
      }
    }

    // cleanup other folders created
    Path launchPath = Paths.get("lwrt", Key.gameFolderName.getValue(settings), "launch");
    if (Files.exists(launchPath)) {
      try {
        delete(launchPath);
      } catch (IOException e) {
        log.warn("Could not remove launch folder: " + e);
      }
    }

    // create a backup before moving files
    boolean restored = false;
    Path backupPath = createUserBackup();

    // move user folders back to original location
    Path gamePath = toPath(Key.gamePath.getValue(settings));
    Path configPath = gamePath.resolve("cfg");
    Path customPath = gamePath.resolve("custom");
    Path userConfigPath = gamePath.resolve("lawena-user-cfg");
    Path userCustomPath = gamePath.resolve("lawena-user-custom");
    try {
      if (Files.exists(userCustomPath)) {
        removeDirectoryLink(customPath);
        log.debug("Moving folder: {} -> {}", userCustomPath, customPath);
        Files.move(userCustomPath, customPath);
      }
      if (Files.exists(userConfigPath)) {
        removeDirectoryLink(configPath);
        log.debug("Moving folder: {} -> {}", userConfigPath, configPath);
        Files.move(userConfigPath, configPath);
      }
      restored = true;
    } catch (IOException e) {
      log.warn("Could not restore original folders: " + e);
    }

    if (!restored) {
      log.warn("*** Restoring Interrupted: Some launch files could not be deleted. Your files are here:");
      log.warn("{}", userConfigPath);
      log.warn("{}", userCustomPath);
      log.warn("*** DO NOT delete these folders. Lawena will attempt to restore them on next close or launch.");
      log.warn("*** If it doesn't work, it might be caused by Windows locking font files in a way");
      log.warn("*** that can only be unlocked through a restart or using special Unlocker software");
      if (Files.exists(backupPath))
        log.info("A backup with your files was created at this location: {}", backupPath);
    } else {
      links.clear();
      log.info("*** Restore and cleanup completed");
      // Created zip file is safe to delete since the process was completed
      if (Key.deleteUnneededBackups.getValue(settings)) {
        try {
          if (Files.deleteIfExists(backupPath)) {
            log.info("Deleted last backup: " + backupPath);
          }
        } catch (IOException e) {
          log.warn("Could not delete backup zipfile: " + e);
        }
      }
    }
    return restored;
  }

  private Path createUserBackup() {
    // always make a backup, later decide if it's useful
    Path gamePath = toPath(Key.gamePath.getValue(settings));
    Path zip = gamePath.resolve("lawena-user." + Util.now("yyMMddHHmmss") + ".bak.zip");
    Path userConfigPath = gamePath.resolve("lawena-user-cfg");
    Path userCustomPath = gamePath.resolve("lawena-user-custom");
    boolean doBackup = true;
    if (Files.exists(userConfigPath) || Files.exists(userCustomPath)) {
      doBackup =
          Util.notifyBigFolder(Key.bigFolderThreshold.getValue(settings), userConfigPath,
              userCustomPath);
      if (!doBackup) {
        log.info("Backup creation skipped by the user");
      }
    }
    if (doBackup && (Files.exists(userConfigPath) || Files.exists(userCustomPath))) {
      log.info("Creating a backup of your files in: " + zip);
      try {
        ZipFile zipFile = new ZipFile(zip.toFile());
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        for (Path path : Arrays.asList(userConfigPath, userCustomPath)) {
          zipFile.addFolder(path.toFile(), parameters);
        }
      } catch (IllegalArgumentException | ZipException e) {
        // IllegalArgumentException can be caused by a bug in jdk versions 7u40 and older
        log.info("Emergency backup could not be created: " + e);
      }
    }
    return zip;
  }

  private void removeLink(Path link) {
    if (Files.exists(link, LinkOption.NOFOLLOW_LINKS)) {
      if (isSymbolicLink(link)) {
        log.trace("Unlinking file: {}", link);
        Util.startProcess(Arrays.asList("cmd", "/c", "del", link.toAbsolutePath().toString()));
      }
    }
  }

  private void removeDirectoryLink(Path link) {
    if (Files.exists(link, LinkOption.NOFOLLOW_LINKS)) {
      if (isSymbolicLink(link)) {
        log.trace("Unlinking directory: {}", link);
        Util.startProcess(Arrays.asList("cmd", "/c", "rd", link.toAbsolutePath().toString()));
      }
    }
  }

  private boolean isSymbolicLink(Path path) {
    if (Files.isSymbolicLink(path)) {
      return true;
    } else {
      // delegate to OS
      int ret =
          Util.startProcess(Arrays.asList("cmd", "/c", "fsutil", "reparsepoint", "query", path
              .toAbsolutePath().toString()));
      return ret == 0;
    }
  }

  private static final Consumer<String> printer = new Consumer<String>() {

    @Override
    public void consume(String line) {
      log.trace("{}", line);
    }
  };
}
