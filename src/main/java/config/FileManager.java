
package config;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

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

    private boolean isEmpty(Path dir) {
        if (Files.exists(dir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                return !stream.iterator().hasNext();
            } catch (IOException e) {
                return false;
            }
        } else {
            return true;
        }
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
            try {
                // backup tf/cfg and copy lawena's cfg
                configPath.toFile().setWritable(true);
                Files.move(configPath, configBackupPath);
                Files.createDirectories(configPath);
                copy(Paths.get("cfg"), configPath);
            } catch (IOException e) {
                log.log(Level.INFO, "Could not replace cfg files", e);
                return;
            }
        }
        if (!Files.exists(customBackupPath)) {
            try {
                // backup all custom
                customPath.toFile().setWritable(true);
                Files.move(customPath, customBackupPath);
            } catch (IOException e) {
                log.log(Level.INFO, "Could not backup custom folder", e);
                return;
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
                    log.log(Level.INFO, "Could not delete lawena skybox folder", e);
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
        }
    }

    private void replaceSkybox() throws IOException {
        List<Path> vmtPaths = new ArrayList<>();
        List<Path> vtfPaths = new ArrayList<>();
        Path skyboxPath = Paths.get(tfdir, "custom/lawena/materials/skybox");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("skybox"))) {
            for (Path path : stream) {
                String pathStr = path.toFile().getName();
                if (pathStr.endsWith(".vmt")) {
                    Files.copy(path, skyboxPath.resolve(pathStr));
                    vmtPaths.add(path);
                }
                if (pathStr.endsWith(".vtf") && pathStr.startsWith(skyboxFilename)) {
                    vtfPaths.add(path);
                }
            }
        }

        for (int i = 0; i < vtfPaths.size(); ++i) {
            for (int j = 0; j < vmtPaths.size(); ++j) {
                String vtf = vtfPaths.get(i).getFileName().toString();
                String vmt = vmtPaths.get(j).getFileName().toString();
                if ((vtf.endsWith("up.vtf") && vmt.endsWith("up.vmt"))
                        || (vtf.endsWith("dn.vtf") && vmt.endsWith("dn.vmt"))
                        || (vtf.endsWith("bk.vtf") && vmt.endsWith("bk.vmt"))
                        || (vtf.endsWith("ft.vtf") && vmt.endsWith("ft.vmt"))
                        || (vtf.endsWith("lf.vtf") && vmt.endsWith("lf.vmt"))
                        || (vtf.endsWith("rt.vtf") && vmt.endsWith("rt.vmt"))) {
                    Files.copy(vtfPaths.get(i),
                            skyboxPath.resolve(vmt.substring(0, vmt.indexOf(".vmt")) + ".vtf"));
                }
            }
        }
    }

    public void restoreAll() {
        if (Files.exists(configBackupPath)) {
            try {
                delete(configPath);
            } catch (NoSuchFileException e) {
            } catch (IOException e) {
                log.log(Level.INFO, "Could not delete lawena cfg folder", e);
            }
            try {
                if (isEmpty(configPath)) {
                    Files.move(configBackupPath, configPath, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    showRestoreMessage();
                }
            } catch (IOException e) {
                log.log(Level.INFO, "Could not restore cfg files", e);
                showRestoreMessage();
            }
        }
        if (Files.exists(customBackupPath)) {
            try {
                delete(customPath);
            } catch (NoSuchFileException e) {
            } catch (IOException e) {
                log.log(Level.INFO, "Could not delete lawena custom files", e);
            }
            try {
                if (isEmpty(customPath)) {
                    Files.move(customBackupPath, customPath, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    showRestoreMessage();
                }
            } catch (IOException e) {
                log.log(Level.INFO, "Could not restore custom files", e);
                showRestoreMessage();
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

    private void showRestoreMessage() {
        JOptionPane
                .showMessageDialog(
                        null,
                        "Some lawena files might still exist inside 'cfg' or 'custom'.\n" +
                                "Your files will be restored once you close lawena.",
                        "Restoring user files",
                        JOptionPane.INFORMATION_MESSAGE);
    }
}
