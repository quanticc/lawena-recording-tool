package com.github.lawena.os;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OSXInterface extends UnixInterface {

  private static final Logger log = LoggerFactory.getLogger(OSXInterface.class);

  @Override
  public ProcessBuilder getBuilderSteamLaunch(String steamPath) {
    Path steam = Paths.get(steamPath).resolve("Steam.app");
    try {
      Files.setPosixFilePermissions(steam, perms777);
      return new ProcessBuilder(steam.toString());
    } catch (IOException e) {
      log.warn("Problem while settings permissions to steam client", e);
    }
    return null;
  }

  @Override
  public Path getSteamPath() {
    return Paths.get("/Applications/");
  }

}
