
package config;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileManager {

    private static final Logger log = Logger.getLogger("lwrt");

    private String tfdir;

    private String hudName;
    private String skyboxFilename;
    private boolean replaceVo;
    private boolean replaceDomination;
    private boolean replaceAnnouncer;

    private Path configBackupPath;
    private Path configPath;
    private Path customBackupPath;
    private Path customPath;

    public FileManager(String dir) {
        tfdir = dir;
        customBackupPath = Paths.get(tfdir, "lwrtcustom");
        customPath = Paths.get(tfdir, "custom");
        configBackupPath = Paths.get(tfdir, "lwrtcfg");
        configPath = Paths.get(tfdir, "cfg");
    }

    private Path copy(Path from, Path to) throws IOException {
        return Files.walkFileTree(from, new CopyDirVisitor(from, to));
    }

    private Path delete(Path dir) throws IOException {
        return Files.walkFileTree(dir, new DeleteDirVisitor());
    }

    public String getHudName() {
        return hudName;
    }

    public String getSkyboxFilename() {
        return skyboxFilename;
    }

    public boolean isReplaceAnnouncer() {
        return replaceAnnouncer;
    }

    public boolean isReplaceDomination() {
        return replaceDomination;
    }

    public boolean isReplaceVo() {
        return replaceVo;
    }

    public void replaceAll() {
        if (!Files.exists(configBackupPath)) {
            log.info("Backing up cfg files");
            try {
                // backup tf/cfg
                Files.createDirectories(configBackupPath);
                copy(configPath, configBackupPath);
            } catch (IOException e) {
                log.log(Level.INFO, "Could not backup cfg folder", e);
                return;
            }
        }
        if (!Files.exists(customBackupPath)) {
            log.info("Replacing custom files");
            try {
                // backup all custom
                Files.move(customPath, customBackupPath);
            } catch (IOException e) {
                log.log(Level.INFO, "Could not backup custom folder", e);
                return;
            }
            try {
                // copy lawena's cfg
                Path cfgPath = Paths.get(tfdir, "custom/lawena/cfg");
                Files.createDirectories(cfgPath);
                copy(Paths.get("cfg"), cfgPath);
            } catch (IOException e) {
                log.log(Level.INFO, "Could not replace cfg files", e);
            }
            // copy lawena's hud (resource, scripts)
            try {
                Path resourcePath = Paths.get(tfdir, "custom/lawena/resource");
                Path scriptsPath = Paths.get(tfdir, "custom/lawena/scripts");
                Files.createDirectories(resourcePath);
                Files.createDirectories(scriptsPath);
                copy(Paths.get("hud", hudName, "resource"), resourcePath);
                copy(Paths.get("hud", hudName, "scripts"), scriptsPath);
            } catch (IOException e) {
                log.log(Level.INFO, "Could not replace hud files", e);
            }
            // copy lawena's materials/skybox
            Path materialsPath = Paths.get(tfdir, "custom/lawena/materials/skybox");
            try {
                if (skyboxFilename != null && !skyboxFilename.isEmpty()) {
                    Files.createDirectories(materialsPath);
                    replaceSkybox();
                }
            } catch (IOException e) {
                log.log(Level.INFO, "Could not replace skybox files", e);
                try {
                    delete(materialsPath);
                } catch (IOException e1) {
                    log.log(Level.INFO, "Could not delete lawena skybox folder", e1);
                }
            }
            // copy lawena's sound/vo
            try {
                if (replaceVo) {
                    Path voPath = Paths.get(tfdir, "custom/lawena/sound/vo");
                    Files.createDirectories(voPath);
                    copy(Paths.get("sound/vo"), voPath);
                }
            } catch (IOException e) {
                log.log(Level.INFO, "Could not replace vo sound files", e);
            }
            // copy lawena's sound/misc
            try {
                Path miscPath = Paths.get(tfdir, "custom/lawena/sound/misc");
                Files.createDirectories(miscPath);
                copy(Paths.get("sound/misc"), miscPath);
                if (replaceDomination) {
                    copy(Paths.get("sound/miscdom"), miscPath);
                }
                if (replaceAnnouncer) {
                    copy(Paths.get("sound/miscann"), miscPath);
                }
            } catch (IOException e) {
                log.log(Level.INFO, "Could not replace misc sound files", e);
            }
        } else {
            log.info("Could not replace custom files because the backup folder 'lwrtcustom' still exists");
        }
    }

    private void replaceSkybox() throws IOException {
        List<Path> vmtPaths = new ArrayList<>();
        List<Path> vtfPaths = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("skybox"))) {
            for (Path path : stream) {
                if (path.endsWith(".vmt")) {
                    Files.copy(
                            path,
                            Paths.get(tfdir, "custom/lawena/materials/skybox").resolve(
                                    path.getFileName()));
                    vmtPaths.add(path);
                }
                if (path.endsWith(".vtf") && path.startsWith(skyboxFilename)) {
                    vtfPaths.add(path);
                }
            }
        }

        for (int i = 0; i < vtfPaths.size(); ++i) {
            for (int j = 0; j < vmtPaths.size(); ++j) {
                Path vtf = vtfPaths.get(i);
                Path vmt = vmtPaths.get(j);
                if ((vtf.endsWith("up.vtf") && vmt.endsWith("up.vmt"))
                        || (vtf.endsWith("dn.vtf") && vmt.endsWith("dn.vmt"))
                        || (vtf.endsWith("bk.vtf") && vmt.endsWith("bk.vmt"))
                        || (vtf.endsWith("ft.vtf") && vmt.endsWith("ft.vmt"))
                        || (vtf.endsWith("lf.vtf") && vmt.endsWith("lf.vmt"))
                        || (vtf.endsWith("rt.vtf") && vmt.endsWith("rt.vmt"))) {
                    String vmtFilename = vmt.getFileName().toString();
                    Files.copy(
                            vtfPaths.get(i),
                            Paths.get(tfdir, "custom/lawena/materials/skybox").resolve(
                                    vmtFilename.substring(0, vmtFilename.indexOf(".vmt")) + ".vtf"));
                }
            }
        }
    }

    public void restoreAll() {
        if (Files.exists(configBackupPath)) {
            log.info("Restoring cfg files");
            try {
                delete(configPath);
                Files.move(configBackupPath, configPath);
            } catch (IOException e) {
                log.log(Level.INFO, "Could not restore cfg files", e);
            }
        }
        if (Files.exists(customBackupPath)) {
            log.info("Restoring custom files");
            try {
                delete(customPath);
                Files.move(customBackupPath, customPath);
            } catch (IOException e) {
                log.log(Level.INFO, "Could not restore custom files", e);
            }
        }
    }

    public void setHudName(String hudName) {
        this.hudName = hudName;
    }

    public void setReplaceAnnouncer(boolean replaceAnnouncer) {
        this.replaceAnnouncer = replaceAnnouncer;
    }

    public void setReplaceDomination(boolean replaceDomination) {
        this.replaceDomination = replaceDomination;
    }

    public void setReplaceVo(boolean replaceVo) {
        this.replaceVo = replaceVo;
    }

    public void setSkyboxFilename(String skyboxFilename) {
        this.skyboxFilename = skyboxFilename;
    }
}
