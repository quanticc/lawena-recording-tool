package com.github.lawena.app.model;

import static com.github.lawena.util.Util.toPath;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.Messages;
import com.github.lawena.profile.Key;
import com.github.lawena.util.Consumer;
import com.github.lawena.util.LawenaException;
import com.github.lawena.util.Util;

public class LinkerGo implements Linker {

  static final Logger log = LoggerFactory.getLogger(LinkerGo.class);

  private MainModel model;
  private Settings settings;
  private Map<Path, Path> links = new LinkedHashMap<>();

  @Override
  public void setModel(MainModel model) {
    this.model = model;
    this.settings = model.getSettings();
  }

  @Override
  public void link() throws LawenaException {
    // csgo is very limited, we only need to link
    // 1. lwrt/csgo/config/cfg to <gamePath>/cfg
    links.clear();
    Path gamePath = toPath(Key.gamePath.getValue(settings));
    Path base = settings.getParentDataPath(); // should be lwrt/csgo

    // Abort if user folders exist, that means cleanup is not complete
    Path userConfigPath = gamePath.resolve("lawena-user-cfg"); //$NON-NLS-1$
    if (Files.exists(userConfigPath)) {
      log.warn("*** Lawena has found that your files are still present"); //$NON-NLS-1$
      log.warn("*** Your files are located in {}", userConfigPath); //$NON-NLS-1$
      log.warn("*** Restart Lawena to attempt to restore your files to their original location"); //$NON-NLS-1$
      throw new LawenaException(Messages.getString("LinkerGo.linkFailedBackupFoldersPresent")); //$NON-NLS-1$
    }

    // Moving user folders
    Path configPath = gamePath.resolve("cfg"); //$NON-NLS-1$
    try {
      log.info("Moving user config folder"); //$NON-NLS-1$
      configPath.toFile().setWritable(true);
      move(configPath, userConfigPath);
    } catch (IOException e) {
      log.warn("Could not move user config folder: " + e); //$NON-NLS-1$
      throw new LawenaException(Messages.getString("LinkerGo.linkFailedBackupFoldersPresentTitle"), e); //$NON-NLS-1$
    }

    // Link launch files to game paths
    Path launchConfigPath = base.resolve("config/cfg"); //$NON-NLS-1$
    createDirectoryLink(configPath, launchConfigPath);
  }

  private void delete(Path start) throws IOException {
    Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        removeLink(file);
        if (Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
          log.trace("Deleting regular file: {}", file); //$NON-NLS-1$
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
            log.trace("Deleting regular directory: {}", dir); //$NON-NLS-1$
            Files.delete(dir);
          }
        } else {
          throw exc;
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }

//  private static void createDirectories(Path dir) throws IOException {
//    log.trace("Creating directory: {}", dir); //$NON-NLS-1$
//    Files.createDirectories(dir);
//  }

  private static void move(Path src, Path dest) throws IOException {
    log.trace("Moving folder: {} -> {}", src, dest); //$NON-NLS-1$
    src.toFile().setWritable(true);
    Files.move(src, dest);
  }

//  @SuppressWarnings("nls")
//  private void createLink(Path link, Path target) {
//    log.trace("Linking: {} <-> {}", link, target);
//    Util.startProcess(Arrays.asList("cmd", "/c", "mklink", link.toAbsolutePath().toString(), target
//        .toAbsolutePath().toString()), printer);
//    links.put(link, target);
//  }

  private void createDirectoryLink(Path link, Path target) {
    log.trace("Linking directory: {} <-> {}", link, target); //$NON-NLS-1$
    Util.startProcess(Arrays.asList("cmd", "/c", "mklink", "/d", link.toAbsolutePath().toString(), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        target.toAbsolutePath().toString()), printer);
    links.put(link, target);
  }

  /**
   * Removes links created by {@link #link()} and restores folder structure to original state.
   * 
   * @return <code>true</code> if the operation was successful, <code>false</code> otherwise.
   */
  @Override
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
    Path launchPath = Paths.get("lwrt", Key.gameFolderName.getValue(settings), "launch"); //$NON-NLS-1$ //$NON-NLS-2$
    if (Files.exists(launchPath)) {
      try {
        delete(launchPath);
      } catch (IOException e) {
        log.warn("Could not remove launch folder: " + e); //$NON-NLS-1$
      }
    }

    // create a backup before moving files
    boolean restored = false;
    Path backupPath = createUserBackup();

    // move user folders back to original location
    Path gamePath = toPath(Key.gamePath.getValue(settings));
    Path configPath = gamePath.resolve("cfg"); //$NON-NLS-1$
    Path userConfigPath = gamePath.resolve("lawena-user-cfg"); //$NON-NLS-1$
    try {
      if (Files.exists(userConfigPath)) {
        removeDirectoryLink(configPath);
        log.debug("Moving folder: {} -> {}", userConfigPath, configPath); //$NON-NLS-1$
        Files.move(userConfigPath, configPath);
      }
      restored = true;
    } catch (IOException e) {
      log.warn("Could not restore original folders: " + e); //$NON-NLS-1$
    }

    if (!restored) {
      log.warn("*** Restoring Interrupted: Some launch files could not be deleted. Your files are here:"); //$NON-NLS-1$
      log.warn("{}", userConfigPath); //$NON-NLS-1$
      log.warn("*** DO NOT delete these folders. Lawena will attempt to restore them on next close or launch."); //$NON-NLS-1$
      log.warn("*** If it doesn't work, it might be caused by Windows locking font files in a way"); //$NON-NLS-1$
      log.warn("*** that can only be unlocked through a restart or using special Unlocker software"); //$NON-NLS-1$
      if (Files.exists(backupPath))
        log.info("A backup with your files was created at this location: {}", backupPath); //$NON-NLS-1$
    } else {
      links.clear();
      log.info("*** Restore and cleanup completed"); //$NON-NLS-1$
      // Created zip file is safe to delete since the process was completed
      if (Key.deleteUnneededBackups.getValue(settings)) {
        try {
          if (Files.deleteIfExists(backupPath)) {
            log.info("Deleted last backup: " + backupPath); //$NON-NLS-1$
          }
        } catch (IOException e) {
          log.warn("Could not delete backup zipfile: " + e); //$NON-NLS-1$
        }
      }
    }
    return restored;
  }

  private Path createUserBackup() {
    // always make a backup, later decide if it's useful
    Path gamePath = toPath(Key.gamePath.getValue(settings));
    Path zip = gamePath.resolve("lawena-user." + Util.now("yyMMddHHmmss") + ".bak.zip"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    Path userConfigPath = gamePath.resolve("lawena-user-cfg"); //$NON-NLS-1$
    boolean doBackup = true;
    if (Files.exists(userConfigPath)) {
      doBackup = Util.notifyBigFolder(Key.bigFolderThreshold.getValue(settings), userConfigPath);
      if (!doBackup) {
        log.info("Backup creation skipped by the user"); //$NON-NLS-1$
      }
    }
    if (doBackup && (Files.exists(userConfigPath))) {
      log.info("Creating a backup of your files in: " + zip); //$NON-NLS-1$
      try {
        ZipFile zipFile = new ZipFile(zip.toFile());
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        zipFile.addFolder(userConfigPath.toFile(), parameters);
      } catch (IllegalArgumentException | ZipException e) {
        // IllegalArgumentException can be caused by a bug in jdk versions 7u40 and older
        log.info("Emergency backup could not be created: " + e); //$NON-NLS-1$
      }
    }
    return zip;
  }

  void removeLink(Path link) {
    if (Files.exists(link, LinkOption.NOFOLLOW_LINKS)) {
      if (isSymbolicLink(link)) {
        log.trace("Unlinking file: {}", link); //$NON-NLS-1$
        Util.startProcess(Arrays.asList("cmd", "/c", "del", link.toAbsolutePath().toString())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
    }
  }

  void removeDirectoryLink(Path link) {
    if (Files.exists(link, LinkOption.NOFOLLOW_LINKS)) {
      if (isSymbolicLink(link)) {
        log.trace("Unlinking directory: {}", link); //$NON-NLS-1$
        Util.startProcess(Arrays.asList("cmd", "/c", "rd", link.toAbsolutePath().toString())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
    }
  }

  private boolean isSymbolicLink(Path path) {
    return model.getOsInterface().isSymbolicLink(path);
  }

  private static final Consumer<String> printer = new Consumer<String>() {

    @Override
    public void consume(String line) {
      log.trace("{}", line); //$NON-NLS-1$
    }
  };
}
