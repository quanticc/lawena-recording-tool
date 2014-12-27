package lwrt;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import util.WinRegistry;

public class CLWindows extends CommandLine {

  private String hl2 = "hl2.exe";

  @Override
  public ProcessBuilder getBuilderStartTF2() {
    return new ProcessBuilder(getSteamPath() + "/steam.exe");
  }

  @Override
  public ProcessBuilder getBuilderTF2ProcessKiller() {
    return new ProcessBuilder("taskkill", "/F", "/IM", hl2);
  }

  @Override
  public ProcessBuilder getBuilderVTFCmd(String skyboxFilename) {
    return new ProcessBuilder("vtfcmd\\VTFCmd.exe", "-file", "skybox\\" + skyboxFilename,
        "-output", "skybox", "-exportformat", "png");
  }

  @Override
  public Path getSteamPath() {
    return Paths.get(regQueryString("Software\\Valve\\Steam", "SteamPath"));
  }

  @Override
  public String getSystemDxLevel() {
    try {
      int result =
          regQueryNumber("HKEY_CURRENT_USER\\Software\\Valve\\Source\\tf\\Settings", "DXLevel_V1");
      return result + "";
    } catch (NumberFormatException e) {
      log.warning("Could not format registry dxlevel value: " + e.toString()
          + " -- Using dxlevel 98");
      return "98";
    }
  }

  @Override
  public boolean isRunningTF2() {
    String line;
    ProcessBuilder[] builders =
        {
            new ProcessBuilder("tasklist", "/fi", "\"imagename eq " + hl2 + "\"", "/nh", "/fo",
                "csv"),
            new ProcessBuilder("cscript", "//NoLogo", new File("batch\\procchk.vbs").getPath(), hl2)};
    for (ProcessBuilder pb : builders) {
      try {
        Process p = pb.start();
        try (BufferedReader input = newProcessReader(p)) {
          while ((line = input.readLine()) != null) {
            log.finest("[" + pb.command().get(0) + "] " + line);
            if (line.contains(hl2)) {
              log.finer("TF2 process detected by " + pb.command().get(0));
              return true;
            }
          }
        }
      } catch (IOException e) {
        log.log(Level.INFO, "Problem while finding if TF2 is running", e);
      }
    }
    log.finer("TF2 process not detected");
    return false;
  }

  private void regedit(String key, String value, String content) {
    try {
      ProcessBuilder pb = new ProcessBuilder("batch\\rg.bat", key, value, content);
      Process pr = pb.start();
      pr.waitFor();
    } catch (InterruptedException | IOException e) {
      log.log(Level.INFO, "", e);
    }
  }

  private String regQueryLine(String key, String value) {
    StringBuilder result = new StringBuilder();
    try {
      ProcessBuilder pb = new ProcessBuilder("reg", "query", key, "/v", value);
      Process pr = pb.start();
      try (BufferedReader input = newProcessReader(pr)) {
        String line;
        while ((line = input.readLine()) != null) {
          result.append(line + '\n');
        }
      }
      pr.waitFor();
    } catch (InterruptedException | IOException e) {
      log.log(Level.INFO, "", e);
    }
    return result.toString();
  }

  private int regQueryNumber(String key, String value) {
    String result = regQueryLine(key, value);
    int number =
        Integer.decode(result.substring(result.lastIndexOf("0x"),
            result.indexOf("\n", result.lastIndexOf("0x"))));
    log.finer("[regQuery] Found number at key=" + key + ", value=" + value + ": " + number);
    return number;
  }

  private String regQueryString(String key, String value) {
    // first method: WinRegistry
    String hk = "HKEY_CURRENT_USER\\";
    int hkey = WinRegistry.HKEY_CURRENT_USER;
    String data = null;
    try {
      data = WinRegistry.readString(hkey, key, value);
      log.finer("[WinRegistry] key=" + hk + key + ", value=" + value + " : " + data);
    } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
      log.warning("[WinRegistry] key=" + hk + key + ", value=" + value + " Lookup Failed: "
          + e.toString());
    }
    if (data != null)
      return data;
    // old method: reg query
    String result = regQueryLine(key, value);
    try {
      data =
          result.substring(result.lastIndexOf(":") - 1,
              result.indexOf("\n", result.lastIndexOf(":")));
      log.finer("[regQuery] Found string at key=" + key + ", value=" + value + ": " + data);
      return data;
    } catch (IndexOutOfBoundsException e) {
      log.warning("[regQuery] Invalid data found at key=" + key + ", value=" + value + ": "
          + result);
      return "";
    }
  }

  @Override
  public Path resolveVpkToolPath(Path tfpath) {
    return tfpath.resolve("../bin/vpk.exe");
  }

  @Override
  public void setSystemDxLevel(String dxlevel) {
    regedit("HKEY_CURRENT_USER\\Software\\Valve\\Source\\tf\\Settings", "DXLevel_V1", dxlevel);
  }

  @Override
  public void openFolder(Path dir) {
    try {
      Process pr = Runtime.getRuntime().exec("explorer.exe /select," + dir.toString());
      pr.waitFor();
    } catch (IOException | InterruptedException e) {
      // fallback to Java desktop API
      super.openFolder(dir);
    }
  }

  private void closeHandle(String pid, String handle) {
    try {
      ProcessBuilder pb = new ProcessBuilder("batch\\handle.exe", "-c", handle, "-p", pid, "-y");
      Process pr = pb.start();
      try (BufferedReader input = newProcessReader(pr)) {
        String line;
        int count = 0;
        while ((line = input.readLine()) != null) {
          if (count > 7) {
            log.info("[handle] " + line);
          }
          count++;
        }
      }
      pr.waitFor();
    } catch (InterruptedException | IOException e) {
      log.log(Level.INFO, "", e);
    }
  }

  @Override
  public void closeHandles(Path path) {
    try {
      ProcessBuilder pb = new ProcessBuilder("batch\\handle.exe", path.toString());
      Process pr = pb.start();
      try (BufferedReader input = newProcessReader(pr)) {
        String line;
        int count = 0;
        while ((line = input.readLine()) != null) {
          if (count > 4) {
            String[] columns = line.split("[ ]+type: [A-Za-z]+[ ]+|: |[ ]+pid: ");
            if (columns.length == 4) {
              log.info("[handle] Closing handle " + columns[3] + " opened by " + columns[0]);
              closeHandle(columns[1], columns[2]);
            } else {
              log.info("[handle] " + line);
            }
          }
          count++;
        }
      }
      pr.waitFor();
    } catch (InterruptedException | IOException e) {
      log.log(Level.INFO, "", e);
    }
  }

  @Override
  public void delete(Path path) {
    try {
      ProcessBuilder pb = new ProcessBuilder("del", "/f", "/s", "/q", "/a", path.toString());
      Process pr = pb.start();
      try (BufferedReader input = newProcessReader(pr)) {
        String line;
        while ((line = input.readLine()) != null) {
          log.info("[delete] " + line);
        }
      }
      pr.waitFor();
    } catch (InterruptedException | IOException e) {
      log.log(Level.INFO, "", e);
    }
  }
}
