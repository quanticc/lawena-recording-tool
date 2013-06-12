
package lwrt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.logging.Level;

public class CLLinux extends CommandLine {

    @Override
    public ProcessBuilder getBuilderStartTF2(int width, int height, String dxlevel) {
        return new ProcessBuilder(getSteamPath() + "/steam", "-applaunch", "440", "-dxlevel",
                dxlevel + "", "-novid", "-noborder", "-noforcedmparms", "-noforcemaccel",
                "-noforcemspd", "-console", "-high", "-noipx", "-nojoy", "-sw", "-w", width + "",
                "-h", height + "");
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
    public String getSteamPath() {
        return "~/.local/share/Steam";
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
    public Path resolveVpkToolPath(Path tfpath) {
        return tfpath.resolve("../bin/vpk_linux32");
    }

    @Override
    public void setSystemDxLevel(String dxlevel) {
        log.fine("[linux] SystemDxLevel won't be set");
    }

}
