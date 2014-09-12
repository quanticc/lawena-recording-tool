package com.github.lawena.os;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.util.Util;

public class OSXInterface extends OSInterface {

  private static final Logger log = LoggerFactory.getLogger(OSXInterface.class);

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
      Process p = pb.start();
      try (BufferedReader input = Util.newProcessReader(p)) {
        String line;
        line = input.readLine();
        if (line != null) {
          found = true;
        }
      }
    } catch (IOException e) {
      log.warn("", e);
    }
    return found;
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

  @Override
  public String getVTFCmdLocation() {
    throw new UnsupportedOperationException("Skybox previews are not supported on this platform");
  }

}
