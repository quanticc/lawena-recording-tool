
package lwrt;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.logging.Level;

public class CLWindows extends CommandLine {

    @Override
    public ProcessBuilder getBuilderStartTF2(int width, int height, String dxlevel) {
        return new ProcessBuilder(getSteamPath() + "/steam.exe", "-applaunch", "440", "-dxlevel",
                dxlevel + "", "-novid", "-noborder", "-noforcedmparms", "-noforcemaccel",
                "-noforcemspd", "-console", "-high", "-noipx", "-nojoy", "-sw", "-w", width + "",
                "-h", height + "");
    }

    @Override
    public ProcessBuilder getBuilderTF2ProcessKiller() {
        return new ProcessBuilder("taskkill", "/F", "/IM", "hl2.exe");
    }

    @Override
    public ProcessBuilder getBuilderVTFCmd(String skyboxFilename) {
        return new ProcessBuilder("vtfcmd\\VTFCmd.exe", "-file", "skybox\\" +
                skyboxFilename, "-output", "skybox", "-exportformat", "png");
    }

    @Override
    public String getSteamPath() {
        return regQuery("HKEY_CURRENT_USER\\Software\\Valve\\Steam", "SteamPath", 1);
    }

    @Override
    public String getSystemDxLevel() {
        return regQuery("HKEY_CURRENT_USER\\Software\\Valve\\Source\\tf\\Settings", "DXLevel_V1", 0);
    }

    @Override
    public boolean isRunningTF2() {
        String processName = "hl2.exe";
        boolean found = false;
        try {
            ProcessBuilder pb = new ProcessBuilder("cscript", "//NoLogo", new File(
                    "batch\\procchk.vbs").getPath(), "hl2.exe");
            Process pr = pb.start();
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            line = input.readLine();
            if (line != null) {
                if (line.equals(processName)) {
                    found = true;
                }
            }
            input.close();
        } catch (IOException e) {
            log.log(Level.INFO, "", e);
        }
        return found;
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

    private String regQuery(String key, String value, int mode) {
        String result = "";
        try {
            ProcessBuilder pb = new ProcessBuilder("reg", "query", key, "/v", value);
            Process pr = pb.start();
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result = result + line + '\n';
            }
            pr.waitFor();
        } catch (InterruptedException | IOException e) {
            log.log(Level.INFO, "", e);
        }

        if (mode == 0)
            return result.substring(result.lastIndexOf("0x") + 2,
                    result.indexOf('\n', result.lastIndexOf("0x")));
        return result.substring(result.lastIndexOf(":") - 1,
                result.indexOf('\n', result.lastIndexOf(":")));
    }

    @Override
    public Path resolveVpkToolPath(Path tfpath) {
        return tfpath.resolve("../bin/vpk.exe");
    }

    @Override
    public void setSystemDxLevel(String dxlevel) {
        regedit("HKEY_CURRENT_USER\\Software\\Valve\\Source\\tf\\Settings", "DXLevel_V1", dxlevel);
    }

}
