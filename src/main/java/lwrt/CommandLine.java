
package lwrt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;

public abstract class CommandLine {

    protected static final Logger log = Logger.getLogger("lawena");
    
    public abstract ProcessBuilder getBuilderStartTF2(int width, int height, String dxlevel);
    
    public abstract ProcessBuilder getBuilderTF2ProcessKiller();
    
    public abstract ProcessBuilder getBuilderVTFCmd(String skyboxFilename);

    public abstract String getSteamPath();

    public abstract String getSystemDxLevel();

    public abstract boolean isRunningTF2();

    public abstract Path resolveVpkToolPath(Path tfpath);

    public abstract void setSystemDxLevel(String dxlevel);

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

    public void generatePreview(String skyboxFilename) {
        try {
            ProcessBuilder pb = getBuilderVTFCmd(skyboxFilename);
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

    public void killTf2Process() {
        try {
            ProcessBuilder pb = getBuilderTF2ProcessKiller();
            Process pr = pb.start();
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                log.fine("[taskkill] " + line);
            }
            pr.waitFor();
        } catch (InterruptedException | IOException e) {
            log.info("Problem stopping TF2 process");
        }
    }

    public void startTf(int width, int height, String dxlevel) {
        try {
            ProcessBuilder pb = getBuilderStartTF2(width, height, dxlevel);
            log.fine("Starting TF2 in " + width + "x" + height + " with dxlevel " + dxlevel);
            Process pr = pb.start();
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                log.fine("[steam] " + line);
            }
            pr.waitFor();
        } catch (InterruptedException | IOException e) {
            log.log(Level.INFO, "Process was interrupted", e);
        }
    }

    public void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            log.log(Level.WARNING, "Could not set the look and feel", e);
        }
    }

}
