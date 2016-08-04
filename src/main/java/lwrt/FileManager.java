package lwrt;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import lwrt.SettingsManager.Key;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import util.CopyDirVisitor;
import util.DeleteDirVisitor;
import util.LawenaException;
import util.Util;

public class FileManager {

  private static final Logger log = Logger.getLogger("lawena");

  private CustomPathList customPathList;
  private SettingsManager cfg;
  private CommandLine cl;

  public FileManager(SettingsManager cfg, CommandLine cl) {
    this.cfg = cfg;
    this.cl = cl;
  }

  private static Path copy(Path from, Path to) throws IOException {
    return Files.walkFileTree(from, new CopyDirVisitor(from, to, false));
  }

  private static Path copyReadOnly(Path from, Path to) throws IOException {
    return Files.walkFileTree(from, new CopyDirVisitor(from, to, true));
  }

  private static Path copy(Path from, Path to, Filter<Path> filter) throws IOException {
    return Files.walkFileTree(from, new CopyDirVisitor(from, to, false, filter));
  }

  private Path delete(Path dir) throws IOException {
    return Files.walkFileTree(dir, new DeleteDirVisitor(cl));
  }

  private static void mkdirs(Path dir) throws IOException {
    try {
      Files.createDirectories(dir);
    } catch (FileAlreadyExistsException e) {
      log.fine("Folder already exists: " + e);
    }
  }

  private static void mkdir(Path dir) throws IOException {
    try {
      Files.createDirectory(dir);
    } catch (FileAlreadyExistsException e) {
      log.fine("Folder already exists: " + e);
    }
  }

  private static boolean isEmpty(Path dir) {
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
      log.info("*** Backup lwrt folders are still present, not replacing files");
      log.info("*** Your files are still in lwrtcfg and lwrtcustom folders");
      log.info("*** Restart Lawena to attempt to restore your files again");
      throw new LawenaException("Files not replaced due to backup folders still present");
    }
    try {
      log.fine("Making a backup of your config files");
      configPath.toFile().setWritable(true);
      Files.move(configPath, configBackupPath);
      mkdirs(configPath);
      copy(Paths.get("cfg"), configPath);
      if (cfg.getBoolean(Key.CopyUserConfig)) {
        Path configCfg = configBackupPath.resolve("config.cfg");
        if (Files.exists(configCfg)) {
          log.fine("Copying user config.cfg to be used in Lawena");
          Files.copy(configCfg, configPath.resolve("config.cfg"),
              StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    } catch (IOException e) {
      log.log(Level.INFO, "Could not replace cfg files", e);
      throw new LawenaException("Failed to replace cfg files", e);
    }
    try {
      log.fine("Making a backup of your custom files");
      if (!Files.exists(customPath) || !Files.isDirectory(customPath)) {
        mkdir(customPath);
      }
      customPath.toFile().setWritable(true);
      Files.move(customPath, customBackupPath);
    } catch (IOException e) {
      log.log(Level.INFO, "Could not backup custom folder", e);
      throw new LawenaException("Failed to replace custom files", e);
    }
    // Copy hud files
    try {
      log.fine("Copying selected hud files");
      Path resourcePath = tfpath.resolve("custom/lawena/resource");
      Path scriptsPath = tfpath.resolve("custom/lawena/scripts");
      mkdirs(resourcePath);
      mkdirs(scriptsPath);
      String hudName = cfg.getHud();
      if (!hudName.equals("custom")) {
        copyReadOnly(Paths.get("hud", hudName, "resource"), resourcePath);
        copyReadOnly(Paths.get("hud", hudName, "scripts"), scriptsPath);
        if (!hudName.equals("hud_default")) {
          Files.copy(Paths.get("hud", hudName, "info.vdf"),
              tfpath.resolve("custom/lawena/info.vdf"));
        }
      }
    } catch (IOException e) {
      log.log(Level.INFO, "Could not replace hud files", e);
      throw new LawenaException("Failed to replace hud files", e);
    }
    // Copy skybox files
    Path skyboxPath = tfpath.resolve("custom/lawena/materials/skybox");
    try {
      String sky = cfg.getSkybox();
      if (sky != null && !sky.isEmpty() && !sky.equals(Key.Skybox.defValue())) {
        log.fine("Copying selected skybox files");
        mkdirs(skyboxPath);
        replaceSkybox();
      }
    } catch (IOException e) {
      log.log(Level.INFO, "Could not replace skybox files", e);
      try {
        delete(skyboxPath);
        log.fine("Skybox folder deleted, no skybox files were replaced");
      } catch (IOException e1) {
        log.info("Could not delete lawena skybox folder: " + e1);
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

    log.fine("Copying selected custom vpks and folders");
    for (CustomPath cp : customPathList.getList()) {
      try {
        if (cp.isSelected()) {
          Path source;
          if (cp.getPath().startsWith(customPath)) {
            source = customBackupPath.resolve(cp.getPath().getFileName());
          } else if (cp.getPath().startsWith(localCustomPath)) {
            source = localCustomPath.resolve(cp.getPath().getFileName());
          } else {
            log.info("Skipping custom file with wrong path: " + cp.getPath());
            continue;
          }
          if (Files.exists(source)) {
            if (Files.isDirectory(source)) {
              log.fine("Copying custom folder: " + source.getFileName());
              Path dest = customPath.resolve(source.getFileName());
              copyReadOnly(source, dest);
            } else if (cp == CustomPathList.particles) {
              List<String> contents = cl.getVpkContents(tfpath, cp.getPath());
              List<String> selected = cfg.getParticles();
              if (!selected.contains("*")) {
                contents.retainAll(selected);
              }
              if (!contents.isEmpty()) {
                log.fine("Copying enhanced particles: " + contents);
                mkdirs(customParticlesPath);
                // TODO: fix to avoid invoking vpk x
                cl.extractIfNeeded(tfpath, cp.getPath().toString(),
                    customParticlesPath.getParent(), contents);
              } else {
                log.fine("No enhanced particles were selected");
              }
            } else if (source.getFileName().toString().endsWith(".vpk")) {
              log.fine("Copying custom VPK: " + cp.getPath());
              Path dest = customPath.resolve(source.getFileName());
              Files.copy(source, dest);
            } else {
              log.info("Not copying: " + source.getFileName());
            }
          } else {
            log.info("Does not exist: " + source);
          }
        }
      } catch (IOException e) {
        log.info("Could not copy custom file: " + e);
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
      log.fine("Restoring all your custom files");
      try {
        delete(customPath);
      } catch (NoSuchFileException e) {
        log.fine("File does not exist: " + e);
      } catch (IOException e) {
        log.info("Could not delete lawena custom files: " + e);
      }
      try {
        if (isEmpty(customPath)) {
          if (isSymbolicLink(customBackupPath)) {
            Files.deleteIfExists(customPath);
            Files.move(customBackupPath, customPath);
          } else {
            copy(customBackupPath, customPath);
          }
        } else {
          restoreComplete = false;
        }
      } catch (IOException e) {
        log.info("Could not restore custom files: " + e);
        restoreComplete = false;
      }
    } else {
      log.fine("No custom backup folder present");
    }
    if (Files.exists(configBackupPath)) {
      log.fine("Restoring all your config files");
      try {
        delete(configPath);
      } catch (NoSuchFileException e) {
        log.fine("File does not exist: " + e);
      } catch (IOException e) {
        log.info("Could not delete lawena cfg folder: " + e);
      }
      try {
        if (isEmpty(configPath)) {
          if (isSymbolicLink(configBackupPath)) {
            Files.deleteIfExists(configPath);
            Files.move(configBackupPath, configPath);
          } else {
            copy(configBackupPath, configPath);
          }
        } else {
          restoreComplete = false;
        }
      } catch (IOException e) {
        log.info("Could not restore cfg files: " + e);
        restoreComplete = false;
      }
    } else {
      log.fine("No config backup folder present");
    }
    // always make a backup, later decide if it's useful
    Path zip = tfpath.resolve("lawena-user." + Util.now("yyMMddHHmmss") + ".bak.zip");
    boolean doBackup = true;
    if (Files.exists(configBackupPath) || Files.exists(customBackupPath)) {
      try {
        long bytes = Util.sizeOfPath(configBackupPath) + Util.sizeOfPath(customBackupPath);
        String size = Util.humanReadableByteCount(bytes, true);
        log.info("Backup folders size: " + size);
        if (bytes / 1024 / 1024 > cfg.getInt(Key.BigFolderMBThreshold)) {
          int answer =
              JOptionPane
                  .showConfirmDialog(
                      null,
                      "Your cfg and custom folders are "
                          + size
                          + " in size.\nThis might cause Lawena to hang or crash while it creates a backup."
                          + "\nPlease consider moving unnecesary custom files like maps to tf/download folder."
                          + "\nDo you still want to create a backup?", "Backup Folder",
                      JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
          if (answer == JOptionPane.NO_OPTION || answer == JOptionPane.CLOSED_OPTION) {
            log.info("Backup creation skipped by the user");
            doBackup = false;
          }
        }
      } catch (IOException e) {
        log.info("Could not determine folder size: " + e);
      }
    }
    if (doBackup && (Files.exists(configBackupPath) || Files.exists(customBackupPath))) {
      log.info("Creating a backup of your files in: " + zip);
      try {
        ZipFile zipFile = new ZipFile(zip.toFile());
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        for (Path path : Arrays.asList(configBackupPath, customBackupPath)) {
          if (Files.exists(path)) {
            zipFile.addFolder(path.toFile(), parameters);
          }
        }
      } catch (IllegalArgumentException | ZipException e) {
        // IllegalArgumentException can be caused by a bug in jdk versions 7u40 and older
        log.info("Emergency backup could not be created: " + e);
      }
    }
    if (restoreComplete) {
      // at this stage, restore can still fail
      try {
        if (Files.exists(customBackupPath)) {
          log.info("Restoring: Deleting files inside lwrtcustom");
          delete(customBackupPath);
        }
        if (Files.exists(configBackupPath)) {
          log.info("Restoring: Deleting files inside lwrtcfg");
          delete(configBackupPath);
        }
      } catch (IOException e) {
        log.info("Could not delete one or both lwrt folders: " + e);
        restoreComplete = false;
      }
    }
    if (!restoreComplete) {
      log.info("*** Restoring Failed: Some files could not be deleted and process was interrupted");
      log.info("*** Your files are still inside lwrtcfg and lwrtcustom folders. DO NOT delete them");
      log.info("*** Lawena will attempt to restore them again when closing or at next launch.");
      log.info("*** If this doesn't work, it might be caused by Windows locking font files,");
      log.info("*** in a way that can only be unlocked through a restart or using Unlocker software :(");
    } else {
      log.info("*** Restore and cleanup completed");
      // Created zip file is safe to delete since the process was completed
      if (cfg.getBoolean(Key.DeleteBackupsWhenRestoring)) {
        try {
          if (Files.deleteIfExists(zip)) {
            log.info("Deleted last backup: " + zip);
          }
        } catch (IOException e) {
          log.info("Could not delete backup zipfile: " + e);
        }
      }
    }
    return restoreComplete;
  }

  private boolean isSymbolicLink(Path path) {
    String osname = System.getProperty("os.name");
    if (osname.contains("Windows")) {
      if (Files.isSymbolicLink(path)) {
        return true;
      } else {
        int ret =
            Util.startProcess(Arrays.asList("cmd", "/c", "fsutil", "reparsepoint", "query", path
                .toAbsolutePath().toString()));
        return ret == 0;
      }
    } else {
      return Files.isSymbolicLink(path);
    }
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
      log.fine("Failed to copy folder to lawena's custom files: " + e);
    }
    return false;
  }

  public static Set<Path> scanFonts(Path path) throws IOException {
    final Set<Path> parents = new HashSet<>();
    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (file.getFileName().toString().matches("^.*\\.(fon|ttf|ttc|otf)$")) {
          parents.add(file.toAbsolutePath().getParent());
        }
        return FileVisitResult.CONTINUE;
      }
    });
    return parents;
  }
}
