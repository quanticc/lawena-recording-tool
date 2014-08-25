package com.github.lawena.lwrt;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.lwrt.SettingsManager.Key;
import com.github.lawena.util.CopyDirVisitor;
import com.github.lawena.util.DeleteDirVisitor;
import com.github.lawena.util.LawenaException;
import com.github.lawena.util.Util;
import com.github.lawena.util.Zip;

public class FileManager {

  private static final Logger log = LoggerFactory.getLogger(FileManager.class);

  private CustomPathList customPathList;
  private SettingsManager cfg;
  private CommandLine cl;

  public FileManager(SettingsManager cfg, CommandLine cl) {
    this.cfg = cfg;
    this.cl = cl;
  }

  private Path copy(Path from, Path to) throws IOException {
    return Files.walkFileTree(from, new CopyDirVisitor(from, to, false));
  }

  private Path copyReadOnly(Path from, Path to) throws IOException {
    return Files.walkFileTree(from, new CopyDirVisitor(from, to, true));
  }

  private Path copy(Path from, Path to, Filter<Path> filter) throws IOException {
    return Files.walkFileTree(from, new CopyDirVisitor(from, to, false, filter));
  }

  private Path delete(Path dir) throws IOException {
    return Files.walkFileTree(dir, new DeleteDirVisitor(cl));
  }

  private boolean isEmpty(Path dir) {
    if (Files.exists(dir)) {
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
        return !stream.iterator().hasNext();
      } catch (IOException e) {
        return false;
      }
    } else {
      return true;
    }
  }

  public CustomPathList getCustomPathList() {
    return customPathList;
  }

  public void setCustomPathList(CustomPathList customPathList) {
    this.customPathList = customPathList;
  }

  public void replaceAll() throws LawenaException {
    Path tfpath = cfg.getTfPath();
    Path customBackupPath = tfpath.resolve("lwrtcustom");
    Path customPath = tfpath.resolve("custom");
    Path configBackupPath = tfpath.resolve("lwrtcfg");
    Path configPath = tfpath.resolve("cfg");

    if (Files.exists(configBackupPath) || Files.exists(customBackupPath)) {
      log.warn("*** Backup lwrt folders are still present, not replacing files");
      log.warn("*** Your files are still in lwrtcfg and lwrtcustom folders");
      log.warn("*** Restart Lawena to attempt to restore your files again");
      throw new LawenaException("Files not replaced due to backup folders still present");
    }
    try {
      log.info("Making a backup of your config files");
      configPath.toFile().setWritable(true);
      Files.move(configPath, configBackupPath);
      Files.createDirectories(configPath);
      copyReadOnly(Paths.get("cfg"), configPath);
    } catch (IOException e) {
      log.warn("Could not replace cfg files: " + e);
      throw new LawenaException("Failed to replace cfg files", e);
    }
    try {
      log.info("Making a backup of your custom files");
      if (!Files.exists(customPath) || !Files.isDirectory(customPath)) {
        Files.createDirectory(customPath);
      }
      customPath.toFile().setWritable(true);
      Files.move(customPath, customBackupPath);
    } catch (IOException e) {
      log.warn("Could not backup custom folder: " + e);
      throw new LawenaException("Failed to replace custom files", e);
    }
    // Copy hud files
    try {
      log.info("Copying selected hud files");
      Path resourcePath = tfpath.resolve("custom/lawena/resource");
      Path scriptsPath = tfpath.resolve("custom/lawena/scripts");
      Files.createDirectories(resourcePath);
      Files.createDirectories(scriptsPath);
      String hudName = cfg.getHud();
      if (!hudName.equals("custom")) {
        copyReadOnly(Paths.get("hud", hudName, "resource"), resourcePath);
        copyReadOnly(Paths.get("hud", hudName, "scripts"), scriptsPath);
      }
    } catch (IOException e) {
      log.warn("Could not replace hud files: " + e);
      throw new LawenaException("Failed to replace hud files", e);
    }
    // Copy skybox files
    Path skyboxPath = tfpath.resolve("custom/lawena/materials/skybox");
    try {
      String sky = cfg.getSkybox();
      if (sky != null && !sky.isEmpty() && !sky.equals(Key.Skybox.defValue())) {
        log.info("Copying selected skybox files");
        Files.createDirectories(skyboxPath);
        replaceSkybox();
      }
    } catch (IOException e) {
      log.warn("Could not replace skybox files: " + e);
      try {
        delete(skyboxPath);
        log.debug("Skybox folder deleted, no skybox files were replaced");
      } catch (IOException e1) {
        log.warn("Could not delete lawena skybox folder: " + e1);
      }
      throw new LawenaException("Failed to replace skybox files", e);
    }
    // Copy selected custom files
    if (customPathList != null) {
      copyCustomFiles();
    }
  }

  private void copyCustomFiles() {
    Path tfpath = cfg.getTfPath();
    Path customBackupPath = tfpath.resolve("lwrtcustom");
    Path customPath = tfpath.resolve("custom");
    Path localCustomPath = Paths.get("custom");
    Path customParticlesPath = customPath.resolve("lawena/particles");

    log.info("Copying selected custom vpks and folders");
    for (CustomPath cp : customPathList.getList()) {
      try {
        if (cp.isSelected()) {
          Path source;
          if (cp.getPath().startsWith(customPath)) {
            source = customBackupPath.resolve(cp.getPath().getFileName());
          } else if (cp.getPath().startsWith(localCustomPath)) {
            source = localCustomPath.resolve(cp.getPath().getFileName());
          } else {
            log.warn("Skipping custom file with wrong path: " + cp.getPath());
            continue;
          }
          if (Files.exists(source)) {
            if (Files.isDirectory(source)) {
              log.debug("Copying custom folder: " + source.getFileName());
              Path dest = customPath.resolve(source.getFileName());
              copyReadOnly(source, dest);
            } else if (cp == CustomPathList.particles) {
              List<String> contents = cl.getVpkContents(tfpath, cp.getPath());
              List<String> selected = cfg.getParticles();
              if (!selected.contains("*")) {
                contents.retainAll(selected);
              }
              if (!contents.isEmpty()) {
                log.debug("Copying enhanced particles: " + contents);
                Files.createDirectories(customParticlesPath);
                // TODO: fix to avoid invoking vpk x
                cl.extractIfNeeded(tfpath, cp.getPath().toString(),
                    customParticlesPath.getParent(), contents);
              } else {
                log.debug("No enhanced particles were selected");
              }
            } else if (source.getFileName().toString().endsWith(".vpk")) {
              log.debug("Copying custom VPK: " + cp.getPath());
              Path dest = customPath.resolve(source.getFileName());
              Files.copy(source, dest);
            } else {
              log.warn("Not copying: " + source.getFileName());
            }
          } else {
            log.warn("Does not exist: " + source);
          }
        }
      } catch (IOException e) {
        log.warn("Could not copy custom file: " + e);
      }
    }
  }

  private void replaceSkybox() throws IOException {
    Path tfpath = cfg.getTfPath();
    Set<Path> vmtPaths = new LinkedHashSet<>();
    Set<Path> vtfPaths = new LinkedHashSet<>();
    Path skyboxPath = tfpath.resolve("custom/lawena/materials/skybox");
    String skyboxFilename = cfg.getSkybox();
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("skybox"))) {
      for (Path path : stream) {
        String pathStr = path.toFile().getName();
        if (pathStr.endsWith(".vmt")) {
          Files.copy(path, skyboxPath.resolve(pathStr));
          vmtPaths.add(path);
        }
        if (pathStr.endsWith(".vtf") && pathStr.startsWith(skyboxFilename)) {
          vtfPaths.add(path);
        }
      }
    }

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
          Files.copy(vtfPath, skyboxPath.resolve(vmt.substring(0, vmt.indexOf(".vmt")) + ".vtf"));
        }
      }
    }
  }

  public boolean restoreAll() {
    Path tfpath = cfg.getTfPath();
    Path customBackupPath = tfpath.resolve("lwrtcustom");
    Path customPath = tfpath.resolve("custom");
    Path configBackupPath = tfpath.resolve("lwrtcfg");
    Path configPath = tfpath.resolve("cfg");
    boolean restoreComplete = true;

    if (Files.exists(customBackupPath)) {
      log.info("Restoring all your custom files");
      try {
        delete(customPath);
      } catch (NoSuchFileException e) {
      } catch (IOException e) {
        log.warn("Could not delete lawena custom files: " + e);
      }
      try {
        if (isEmpty(customPath)) {
          copy(customBackupPath, customPath);
        } else {
          restoreComplete = false;
        }
      } catch (IOException e) {
        log.warn("Could not restore custom files: " + e);
        restoreComplete = false;
      }
    }
    if (Files.exists(configBackupPath)) {
      log.info("Restoring all your config files");
      try {
        delete(configPath);
      } catch (NoSuchFileException e) {
      } catch (IOException e) {
        log.warn("Could not delete lawena cfg folder: " + e);
      }
      try {
        if (isEmpty(configPath)) {
          copy(configBackupPath, configPath);
        } else {
          restoreComplete = false;
        }
      } catch (IOException e) {
        log.warn("Could not restore cfg files: " + e);
        restoreComplete = false;
      }
    }
    // always make a backup, later decide if it's useful
    Path zip = tfpath.resolve("lawena-user." + Util.now("yyMMddHHmmss") + ".bak.zip");
    if (Files.exists(configBackupPath) || Files.exists(customBackupPath)) {
      log.debug("Creating a backup of your files in: " + zip);
      try {
        Zip.create(zip, Arrays.asList(configBackupPath, customBackupPath));
      } catch (IllegalArgumentException | IOException e) {
        // IllegalArgumentException can be caused by a bug in jdk versions 7u40 and older
        log.warn("Emergency backup could not be created: " + e);
      }
    }
    if (restoreComplete) {
      // at this stage, restore can still fail
      try {
        if (Files.exists(configBackupPath)) {
          log.debug("Restoring: Deleting files inside lwrtcfg");
          delete(configBackupPath);
        }
        if (Files.exists(customBackupPath)) {
          log.debug("Restoring: Deleting files inside lwrtcustom");
          delete(customBackupPath);
        }
      } catch (IOException e) {
        log.warn("Could not delete one or both lwrt folders: " + e);
        restoreComplete = false;
      }
    }
    if (!restoreComplete) {
      log.warn("*** Restoring Failed: Some files could not be deleted and process was interrupted");
      log.warn("*** Your files are still inside lwrtcfg and lwrtcustom folders. DO NOT delete them");
      log.warn("*** Lawena will attempt to restore them again upon close or next launch.");
      log.warn("*** If it doesn't work, it might be caused by Windows locking font files,");
      log.warn("*** in a way that can only be unlocked through a restart or using Unlocker software :(");
      log.info("A backup with your files was created at this location: {}", zip);
    } else {
      log.info("*** Restore and cleanup completed");
      // Created zip file is safe to delete since the process was completed
      if (cfg.getBoolean(Key.DeleteBackupsWhenRestoring)) {
        try {
          if (Files.deleteIfExists(zip)) {
            log.debug("Deleted last backup: " + zip);
          }
        } catch (IOException e) {
          log.warn("Could not delete backup zipfile: " + e);
        }
      }
    }
    return restoreComplete;
  }

  public boolean copyToCustom(final Path path) {
    Path localCustomPath = Paths.get("custom");
    try {
      copy(path, localCustomPath.resolve(path.getFileName()), new Filter<Path>() {

        @Override
        public boolean accept(Path entry) throws IOException {
          if (entry.getParent().equals(path)) {
            String f = entry.getFileName().toString();
            return f.matches("^(resource|scripts|cfg|materials|addons|sound|particles)$");
          } else {
            return true;
          }
        }
      });
      return true;
    } catch (IOException e) {
      log.warn("Failed to copy folder to lawena's custom files: " + e);
    }
    return false;
  }
}
