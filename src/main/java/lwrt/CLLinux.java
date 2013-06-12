
package lwrt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class CLLinux extends CommandLine {

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
    public ProcessBuilder getBuilderStartTF2(int width, int height, String dxlevel)
            throws IOException {
        Path steam = getSteamPath().resolve("steam.sh");
        Files.setPosixFilePermissions(steam, perms777);
        return new ProcessBuilder(steam.toString(), "-applaunch", "440", "-dxlevel", dxlevel + "",
                "-novid", "-noborder", "-noforcedmparms", "-noforcemaccel", "-noforcemspd",
                "-console", "-high", "-noipx", "-nojoy", "-sw", "-w", width + "", "-h", height + "");
    }

    @Override
    public ProcessBuilder getBuilderTF2ProcessKiller() {
        return new ProcessBuilder("pkill", "-9", "hl2_linux");
    }

    @Override
    public ProcessBuilder getBuilderVTFCmd(String skyboxFilename) {
        return null;
    }

    @Override
    public void generatePreview(String skyboxFilename) {
        log.fine("[linux] Skybox preview for " + skyboxFilename + " won't be generated");
    }

    @Override
    public Path getSteamPath() {
        return Paths.get(System.getProperty("user.home"), ".local/share/Steam");
    }

    @Override
    public String getSystemDxLevel() {
        return "90";
    }

    @Override
    public boolean isRunningTF2() {
        boolean found = false;
        try {
            ProcessBuilder pb = new ProcessBuilder("pgrep", "hl2_linux");
            Process pr = pb.start();
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            line = input.readLine();
            if (line != null) {
                found = true;
            }
            input.close();
        } catch (IOException e) {
            log.log(Level.INFO, "", e);
        }
        return found;
    }

    @Override
    public Path resolveVpkToolPath(Path tfpath) throws IOException {
        Path path = tfpath.resolve("../bin/vpk_linux32");
        Files.setPosixFilePermissions(path, perms777);
        return path;
    }

    @Override
    public void setSystemDxLevel(String dxlevel) {
        log.fine("[linux] SystemDxLevel won't be set");
    }

    @Override
    public void setLookAndFeel() {
    }

}
