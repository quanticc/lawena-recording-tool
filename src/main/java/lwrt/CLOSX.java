package lwrt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

public class CLOSX extends CommandLine {

    @Override
    public ProcessBuilder getBuilderStartTF2(String gamePath) {
        Path path = Paths.get(gamePath, "..", "hl2_osx");
        Path link = Paths.get(gamePath, "..", "bin");
        Path start = Paths.get(gamePath, "..");
        try {
            path = path.toRealPath();
            link = link.toRealPath();
            start = start.toRealPath();
        } catch (IOException e) {
            log.warning("Could not obtain real path of game executable: " + e.toString());
        }
        ProcessBuilder pb = new ProcessBuilder(path.toString());
        pb.environment().put("DYLD_LIBRARY_PATH", link.toString());
        pb.directory(start.toFile());
        return pb;
    }

    @Override
    public ProcessBuilder getBuilderStartHLAE(String hlaePath, String gamePath) {
        throw new UnsupportedOperationException("HLAE is not supported here");
    }

    @Override
    public ProcessBuilder getBuilderStartSteam(String steamPath) {
        Path steam = Paths.get(steamPath).resolve("Steam.app");
        try {
            Files.setPosixFilePermissions(steam, ALL_PERMISSIONS);
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
    public ProcessBuilder getBuilderHLAEProcessKiller() {
        throw new UnsupportedOperationException("HLAE not supported here");
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
        return Paths.get(System.getProperty("user.home"), "Library", "Application Support", "Steam");
    }

    @Override
    public boolean isValidSteamPath(Path p) {
        // value must not be empty
        // value must represent a directory named "Steam" (case insensitive)
        // the directory must have a steam.exe file inside
        String s = p.toString();
        return (!s.isEmpty() && Files.isDirectory(p)
            && p.getFileName().toString().equalsIgnoreCase("Steam") && Files.exists(p
            .resolve("Steam.AppBundle")));
    }

    @Override
    public String getSystemDxLevel() {
        return "90";
    }

    @Override
    public void setSystemDxLevel(String dxlevel) {
        log.fine("[osx] SystemDxLevel won't be set");
    }

    @Override
    public boolean isRunningTF2() {
        boolean found = false;
        try {
            ProcessBuilder pb = new ProcessBuilder("pgrep", "hl2_osx");
            Process pr = pb.start();
            found = processOutputHasLine(pr);
        } catch (IOException e) {
            log.log(Level.INFO, "", e);
        }
        return found;
    }

    @Override
    public Path resolveVpkToolPath(Path tfpath) {
        Path path = tfpath.resolve(
            String.join(File.separator, "..", "bin", "vpk_osx32")
        );
        try {
            Files.setPosixFilePermissions(path, ALL_PERMISSIONS);
        } catch (IOException e) {
            log.log(Level.FINE, "Could not set file permissions to VPK tool", e);
        }
        return path;
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
