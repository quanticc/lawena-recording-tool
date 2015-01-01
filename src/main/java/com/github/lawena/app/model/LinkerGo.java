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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

public class LinkerGo implements Linker {

  private static final Logger log = LoggerFactory.getLogger(LinkerGo.class);

  private MainModel model;
  private Settings settings;
  private Map<Path, Path> links = new LinkedHashMap<>();

  public void setModel(MainModel model) {
    this.model = model;
    this.settings = model.getSettings();
  }

  /**
   * Create links between original resources and launch/cfg folder.
   * 
   * @throws LawenaException
   */
  public void link() throws LawenaException {
    // csgo is very limited, we only need to link
    // 1. lwrt/csgo/config/cfg to <gamePath>/cfg
    links.clear();
    Path gamePath = toPath(Key.gamePath.getValue(settings));
    Path base = settings.getParentDataPath(); // should be lwrt/csgo

    // Abort if user folders exist, that means cleanup is not complete
    Path userConfigPath = gamePath.resolve("lawena-user-cfg");
    if (Files.exists(userConfigPath)) {
      log.warn("*** Lawena has found that your files are still present");
      log.warn("*** Your files are located in {}", userConfigPath);
      log.warn("*** Restart Lawena to attempt to restore your files to their original location");
      throw new LawenaException("Files not replaced due to backup folders still present");
    }

    // Moving user folders
    Path configPath = gamePath.resolve("cfg"); // <csgoPath>/cfg
    try {
      log.info("Moving user config folder");
      configPath.toFile().setWritable(true);
      move(configPath, userConfigPath);
    } catch (IOException e) {
      log.warn("Could not move user config folder: " + e);
      throw new LawenaException("Failed to replace config files", e);
    }
    
    // Link launch files to game paths
    Path launchConfigPath = base.resolve("config/cfg"); // lwrt/csgo/config/cfg
    createDirectoryLink(configPath, launchConfigPath);
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
    Path userConfigPath = gamePath.resolve("lawena-user-cfg");
    try {
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
    boolean doBackup = true;
    if (Files.exists(userConfigPath)) {
      doBackup = Util.notifyBigFolder(Key.bigFolderThreshold.getValue(settings), userConfigPath);
      if (!doBackup) {
        log.info("Backup creation skipped by the user");
      }
    }
    if (doBackup && (Files.exists(userConfigPath))) {
      log.info("Creating a backup of your files in: " + zip);
      try {
        ZipFile zipFile = new ZipFile(zip.toFile());
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        zipFile.addFolder(userConfigPath.toFile(), parameters);
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
      // warning: Windows-only snippet
      // TODO: delegate to OSInterface
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
