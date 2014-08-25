package com.github.lawena.lwrt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CLOSX extends CommandLine {

  private static final Logger log = LoggerFactory.getLogger(CLOSX.class);

  private static Set<PosixFilePermission> perms777 = new HashSet<PosixFilePermission>();

  {
    perms777.add(PosixFilePermission.OWNER_READ);
    perms777.add(PosixFilePermission.OWNER_WRITE);
    perms777.add(PosixFilePermission.OWNER_EXECUTE);
    perms777.add(PosixFilePermission.GROUP_READ);
    perms777.add(PosixFilePermission.GROUP_WRITE);
    perms777.add(PosixFilePermission.GROUP_EXECUTE);
    perms777.add(PosixFilePermission.OTHERS_READ);
    perms777.add(PosixFilePermission.OTHERS_WRITE);
    perms777.add(PosixFilePermission.OTHERS_EXECUTE);
  }

  @Override
  public ProcessBuilder getBuilderStartTF2() {
    Path steam = getSteamPath().resolve("Steam.app");
    try {
      Files.setPosixFilePermissions(steam, perms777);
      return new ProcessBuilder(steam.toString());
    } catch (IOException e) {
      log.warn("Problem while settings permissions to steam client", e);
    }
    return null;
  }

  @Override
  public ProcessBuilder getBuilderTF2ProcessKiller() {
    return new ProcessBuilder("pkill", "-9", "hl2_osx");
  }

  @Override
  public ProcessBuilder getBuilderVTFCmd(String skyboxFilename) {
    return null;
  }

  @Override
  public void generatePreview(String skyboxFilename) {
    log.debug("[osx] Skybox preview for " + skyboxFilename + " won't be generated");
  }

  @Override
  public Path getSteamPath() {
    return Paths.get("/Applications/");
  }

  @Override
  public String getSystemDxLevel() {
    return "90";
  }

  @Override
  public boolean isRunningTF2() {
    boolean found = false;
    try {
      ProcessBuilder pb = new ProcessBuilder("pgrep", "hl2_osx");
      Process pr = pb.start();
      BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
      String line;
      line = input.readLine();
      if (line != null) {
        found = true;
      }
      input.close();
    } catch (IOException e) {
      log.warn("", e);
    }
    return found;
  }

  @Override
  public Path resolveVpkToolPath(Path tfpath) {
    Path path = tfpath.resolve("../bin/vpk_osx32");
    try {
      Files.setPosixFilePermissions(path, perms777);
    } catch (IOException e) {
      log.warn("Could not set file permissions to VPK tool", e);
    }
    return path;
  }

  @Override
  public void setSystemDxLevel(String dxlevel) {
    log.info("[osx] SystemDxLevel won't be set");
  }

  @Override
  public void setLookAndFeel() {
    // use java default: Nimbus
  }

  @Override
  public void closeHandles(Path path) {
    // no need to implement this yet
  }

  @Override
  public void delete(Path path) {
    // no need to implement this yet
  }

}
