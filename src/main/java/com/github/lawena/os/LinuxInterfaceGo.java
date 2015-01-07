package com.github.lawena.os;

import java.io.BufferedReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.util.Util;

public class LinuxInterfaceGo extends LinuxInterface {

  private static final Logger log = LoggerFactory.getLogger(LinuxInterfaceGo.class);

  @SuppressWarnings("nls")
  @Override
  public ProcessBuilder getBuilderGameProcessKiller() {
    return new ProcessBuilder("pkill", "-9", "csgo_linux");
  }

  @SuppressWarnings("nls")
  @Override
  public boolean isGameRunning() {
    boolean found = false;
    try {
      ProcessBuilder pb = new ProcessBuilder("pgrep", "csgo_linux");
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

}
