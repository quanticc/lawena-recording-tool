package com.github.lawena.os;

import static com.github.lawena.util.Util.startProcess;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.util.Consumer;

public class WindowsInterfaceGo extends WindowsInterface {

  private static final Logger log = LoggerFactory.getLogger(WindowsInterfaceGo.class);

  @Override
  public ProcessBuilder getBuilderGameProcessKiller() {
    return new ProcessBuilder("taskkill", "/F", "/IM", "csgo.exe");
  }

  @Override
  public boolean isGameRunning() {
    final String hl2 = "csgo.exe";
    String tool = Paths.get("lwrt/tools/processcheck/procchk.vbs").toAbsolutePath().toString();
    List<List<String>> commandLists =
        Arrays.asList(
            Arrays.asList("tasklist", "/fi", "\"imagename eq " + hl2 + "\"", "/nh", "/fo", "csv"),
            Arrays.asList("cscript", "//NoLogo", tool, hl2));
    final AtomicBoolean running = new AtomicBoolean(false);
    for (List<String> commands : commandLists) {
      startProcess(commands, new Consumer<String>() {

        @Override
        public void consume(String line) {
          if (line.contains(hl2)) {
            running.set(true);
          }
        }
      });
      if (running.get()) {
        return true;
      }
    }
    log.debug("CSGO process not detected");
    return false;
  }

  @Override
  public String getSystemDxLevel() {
    return "5f";
  }

  @Override
  public void setSystemDxLevel(String dxlevel) {
    // no need since cs:go doesn't use dxlevel
  }
}
