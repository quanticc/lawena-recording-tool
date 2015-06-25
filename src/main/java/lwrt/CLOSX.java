package lwrt;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class CLOSX extends CommandLine {

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
  public ProcessBuilder getBuilderStartTF2(String steamPath) {
    Path steam = Paths.get(steamPath).resolve("Steam.app");
    try {
      Files.setPosixFilePermissions(steam, perms777);
      return new ProcessBuilder(steam.toString());
    } catch (IOException e) {
      log.log(Level.INFO, "Problem while settings permissions to steam client", e);
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
    log.fine("[osx] Skybox preview for " + skyboxFilename + " won't be generated");
  }

  @Override
  public Path getSteamPath() {
    return Paths.get("/Applications/");
  }

  @Override
  public boolean isValidSteamPath(Path p) {
    // value must not be empty
    // value must represent a directory named "Steam" (case insensitive)
    // the directory must have a steam.exe file inside
    String s = p.toString();
    return (!s.isEmpty() && Files.isDirectory(p)
        && p.getFileName().toString().equalsIgnoreCase("Steam") && Files.exists(p
        .resolve("Steam.app")));
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
      try (BufferedReader input = newProcessReader(pr)) {
        String line;
        line = input.readLine();
        if (line != null) {
          found = true;
        }
      }
    } catch (IOException e) {
      log.log(Level.INFO, "", e);
    }
    return found;
  }

  @Override
  public Path resolveVpkToolPath(Path tfpath) {
    Path path = tfpath.resolve("../bin/vpk_osx32");
    try {
      Files.setPosixFilePermissions(path, perms777);
    } catch (IOException e) {
      log.log(Level.FINE, "Could not set file permissions to VPK tool", e);
    }
    return path;
  }

  @Override
  public void setSystemDxLevel(String dxlevel) {
    log.fine("[osx] SystemDxLevel won't be set");
  }

  @Override
  public void setLookAndFeel() {
    // use java default: Nimbus
    log.fine("Using default Swing L&F");
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
