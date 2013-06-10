
package config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CLWindows implements CommandLine {

    private static final Logger log = Logger.getLogger("lawena");

    @Override
    public void generatePreview(String skyboxFilename) {
        try {
            ProcessBuilder pb = new ProcessBuilder("vtfcmd\\VTFCmd.exe", "-file", "skybox\\" +
                    skyboxFilename, "-output", "skybox", "-exportformat", "png");
            log.finer("generatePreview: " + pb.command());
            Process pr = pb.start();
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                log.finer("[vtfcmd] " + line);
            }
            pr.waitFor();
        } catch (InterruptedException | IOException e) {
            log.log(Level.INFO, "Problem while generating png from vtf file", e);
        }
    }

    @Override
    public boolean isTf2Running() {
        String processName = "hl2.exe";
        boolean found = false;
        File file = new File("batch\\procchk.vbs");
        try {
            ProcessBuilder pb = new ProcessBuilder("cscript", "//NoLogo", file.getPath(),
                    processName);
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

    @Override
    public String getSystemDxLevel() {
        return regQuery("HKEY_CURRENT_USER\\Software\\Valve\\Source\\tf\\Settings", "DXLevel_V1", 0);
    }

    @Override
    public void setSystemDxLevel(String dxlevel) {
        regedit("HKEY_CURRENT_USER\\Software\\Valve\\Source\\tf\\Settings", "DXLevel_V1", dxlevel);
    }

    @Override
    public String getSteamPath() {
        return regQuery("HKEY_CURRENT_USER\\Software\\Valve\\Steam", "SteamPath", 1);
    }

    @Override
    public Process startTf(int width, int height, String dxlevel) {
        try {
            ProcessBuilder pb = new ProcessBuilder(getSteamPath() + "/steam.exe", "-applaunch",
                    "440", "-dxlevel", dxlevel + "", "-novid", "-noborder", "-noforcedmparms",
                    "-noforcemaccel", "-noforcemspd", "-console", "-high", "-noipx", "-nojoy",
                    "-sw", "-w", width + "", "-h", height + "");
            log.fine("Starting TF2 in " + width + "x" + height + " with dxlevel " + dxlevel);
            Process pr = pb.start();
            pr.waitFor();
            return pr;
        } catch (InterruptedException | IOException e) {
            log.log(Level.INFO, "Process was interrupted", e);
        }
        return null;
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
    public List<String> getVpkContents(Path tfpath, Path vpkpath) {
        Path vpktool = resolveVpkToolPath(tfpath);
        List<String> files = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder(vpktool.toString(), "l", vpkpath.toString());
            Process pr = pb.start();
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                files.add(line);
            }
            pr.waitFor();
        } catch (InterruptedException | IOException e) {
            log.info("Problem retrieving contents of VPK file: " + vpkpath);
        }
        return files;
    }

    private Path resolveVpkToolPath(Path tfpath) {
        return tfpath.resolve("../bin/vpk.exe");
    }

    private void extractVpkFile(Path tfpath, String vpkname, Path dest, List<String> files) {
        Path vpktool = resolveVpkToolPath(tfpath);
        List<String> cmds = new ArrayList<>();
        try {
            cmds.add(vpktool.toString());
            cmds.add("x");
            cmds.add(Paths.get(vpkname).toAbsolutePath().toString());
            cmds.addAll(files);
            ProcessBuilder pb = new ProcessBuilder(cmds);
            pb.directory(dest.toFile());
            Process pr = pb.start();
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                log.fine("[vpk] " + line);
            }
            pr.waitFor();
        } catch (InterruptedException | IOException e) {
            log.info("Problem extracting contents from VPK file: " + vpkname);
        }
    }

    @Override
    public void extractIfNeeded(Path tfpath, String vpkname, Path dest, String... files)
            throws IOException {
        List<String> fileList = new ArrayList<>();
        for (String file : files) {
            if (!Files.exists(dest.resolve(file))) {
                if (Files.exists(Paths.get(vpkname))) {
                    if (!Files.exists(dest) || !Files.isDirectory(dest)) {
                        Files.createDirectory(dest);
                    }
                    fileList.add(file);
                } else {
                    log.info("Required file was not found: " + file);
                }
            }
        }
        if (!fileList.isEmpty()) {
            extractVpkFile(tfpath, vpkname, dest, fileList);
        }
    }

}
