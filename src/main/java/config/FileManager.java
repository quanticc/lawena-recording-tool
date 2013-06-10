
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

    private static final Logger log = Logger.getLogger("lawena");

    private String tfdir;

    private String hudName;
    private String skyboxFilename;

    private Path configBackupPath;
    private Path configPath;
    private Path customBackupPath;
    private Path customPath;
    private Path localCustomPath = Paths.get("custom");

    private CustomPathList customPathList;
    private CommandLine cl;

    public FileManager(String dir, CommandLine cmdLine) {
        tfdir = dir;
        customBackupPath = Paths.get(tfdir, "lwrtcustom");
        customPath = Paths.get(tfdir, "custom");
        configBackupPath = Paths.get(tfdir, "lwrtcfg");
        configPath = Paths.get(tfdir, "cfg");
        cl = cmdLine;
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

    public CustomPathList getCustomPathList() {
        return customPathList;
    }

    public void setCustomPathList(CustomPathList customPathList) {
        this.customPathList = customPathList;
    }

    public void replaceAll() {
        if (!Files.exists(configBackupPath)) {
            try {
                log.fine("Making a backup of your config files");
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
                log.fine("Making a backup of your custom files");
                customPath.toFile().setWritable(true);
                Files.move(customPath, customBackupPath);
            } catch (IOException e) {
                log.log(Level.INFO, "Could not backup custom folder", e);
                return;
            }
            try {
                log.fine("Copying selected hud files");
                Path resourcePath = Paths.get(tfdir, "custom/lawena/resource");
                Path scriptsPath = Paths.get(tfdir, "custom/lawena/scripts");
                Files.createDirectories(resourcePath);
                Files.createDirectories(scriptsPath);
                copy(Paths.get("hud", hudName, "resource"), resourcePath);
                copy(Paths.get("hud", hudName, "scripts"), scriptsPath);
            } catch (IOException e) {
                log.log(Level.INFO, "Could not replace hud files", e);
            }
            Path materialsPath = Paths.get(tfdir, "custom/lawena/materials/skybox");
            try {
                if (skyboxFilename != null && !skyboxFilename.isEmpty()) {
                    log.fine("Copying selected skybox files");
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
            // Copy selected custom files
            if (customPathList != null) {
                log.fine("Copying selected custom vpks and folders");
                for (CustomPath cp : customPathList.getList()) {
                    if (cp.isSelected()) {
                        Path source;
                        if (cp.getPath().startsWith(customPath)) {
                            source = customBackupPath.resolve(cp.getPath().getFileName());
                        } else if (cp.getPath().startsWith(localCustomPath)) {
                            source = localCustomPath.resolve(cp.getPath().getFileName());
                        } else {
                            log.info("Not loading custom file with wrong path: "
                                    + cp.getPath());
                            continue;
                        }
                        if (Files.exists(source)) {
                            if (Files.isDirectory(source)) {
                                try {
                                    Path dest = customPath.resolve(source.getFileName());
                                    copy(source, dest);
                                } catch (IOException e) {
                                    log.log(Level.INFO,
                                            "Could not copy custom folder: " + source.getFileName(),
                                            e);
                                }
                            } else if (source.getFileName().toString().endsWith(".vpk")) {
                                try {
                                    Path dest = customPath.resolve(source.getFileName());
                                    Files.copy(source, dest);
                                } catch (IOException e) {
                                    log.log(Level.INFO,
                                            "Could not copy custom vpk: " + source.getFileName(), e);
                                }
                            } else {
                                log.info("Not copying this custom file: " + source.getFileName());
                            }
                        } else {
                            log.info("Custom file does not exist: " + source);
                        }
                    }
                }
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
                    cl.extractIfNeeded(Paths.get(tfdir), "custom/skybox.vpk",
                            Paths.get("skybox"), path.getFileName().toString());
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
                    Path src = vtfPaths.get(i);
                    cl.extractIfNeeded(Paths.get(tfdir), "skybox.vpk",
                            Paths.get("skybox"), src.getFileName().toString());
                    Files.copy(vtfPaths.get(i),
                            skyboxPath.resolve(vmt.substring(0, vmt.indexOf(".vmt")) + ".vtf"));
                }
            }
        }
    }

    public void restoreAll() {
        if (Files.exists(configBackupPath)) {
            log.fine("Restoring all user config files");
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
            log.fine("Restoring all user custom files");
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
