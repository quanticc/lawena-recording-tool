package com.github.lawena.os;

import static com.github.lawena.util.Util.startProcess;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.util.Consumer;

public class WindowsInterfaceTf extends WindowsInterface {

  private static final Logger _log = LoggerFactory.getLogger(WindowsInterfaceTf.class);
  private static final String DXLEVEL_REGISTRY_KEY =
      "HKEY_CURRENT_USER\\Software\\Valve\\Source\\tf\\Settings"; //$NON-NLS-1$
  private static final String DXLEVEL_REGISTRY_VALUE = "DXLevel_V1"; //$NON-NLS-1$


  @SuppressWarnings("nls")
  @Override
  public ProcessBuilder getBuilderGameProcessKiller() {
    return new ProcessBuilder("taskkill", "/F", "/IM", "hl2.exe");
  }

  @Override
  public String getSystemDxLevel() {
    return regQuery(DXLEVEL_REGISTRY_KEY, DXLEVEL_REGISTRY_VALUE);
  }

  @SuppressWarnings("nls")
  @Override
  public boolean isGameRunning() {
    final String hl2 = "hl2.exe";
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
    _log.debug("TF2 process not detected");
    return false;
  }

  @Override
  public void setSystemDxLevel(String dxlevel) {
    String tool = Paths.get("lwrt/tools/regedit/rg.bat").toAbsolutePath().toString(); //$NON-NLS-1$
    startProcess(Arrays.asList(tool, DXLEVEL_REGISTRY_KEY, DXLEVEL_REGISTRY_VALUE, dxlevel));
  }
}
