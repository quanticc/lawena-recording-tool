
package config;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileManager {

    private static final Logger log = Logger.getLogger("lwrt");

    private String tfdir;

    private Path hudPath = null;
    private List<Path> voPaths = new ArrayList<>();
    private List<Path> miscPaths = new ArrayList<>();
    private List<Path> skyboxPaths = new ArrayList<>();

    public FileManager(String dir) {
        tfdir = dir;
    }

    public void scanBackupFolder() {
        log.info("Scanning lawena backup folders ...");
        hudPath = null;
        voPaths.clear();
        miscPaths.clear();
        skyboxPaths.clear();
        scanFolder(Paths.get(tfdir), "lwrt*");
    }

    public void scanCustomFolder() {
        log.info("Scanning tf custom folder ...");
        hudPath = null;
        voPaths.clear();
        miscPaths.clear();
        skyboxPaths.clear();
        scanFolder(Paths.get(tfdir, "custom"), "*");
    }

    private void scanFolder(Path start, String glob) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(start, glob)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    if (Files.isDirectory(path.resolve("resource"))
                            && Files.isDirectory(path.resolve("scripts"))) {
                        log.info("Hud folder found: " + path);
                        hudPath = path;
                    } else if (Files.isDirectory(path.resolve("materials/skybox"))) {
                        log.info("Skybox folder found: " + path);
                        skyboxPaths.add(path);
                    } else if (Files.isDirectory(path.resolve("sound/misc"))) {
                        log.info("Misc folder found: " + path);
                        miscPaths.add(path);
                    } else if (Files.isDirectory(path.resolve("sound/vo"))) {
                        log.info("Vo folder found: " + path);
                        voPaths.add(path);
                    }
                }
            }
        } catch (IOException e) {
            log.log(Level.INFO, "Problem occurred while scanning custom folder", e);
        }
    }

    private Path copy(Path from, Path to) throws IOException {
        return Files.walkFileTree(from, new CopyDirVisitor(from, to));
    }

    private Path delete(Path dir) throws IOException {
        return Files.walkFileTree(dir, new DeleteDirVisitor());
    }

    public void replaceCfg() {
        while (!Files.exists(Paths.get(tfdir, "lwrtcfg"))) {
            try {
                Files.move(Paths.get(tfdir, "cfg"), Paths.get(tfdir, "lwrtcfg"));
                copy(Paths.get("cfg"), Paths.get(tfdir, "cfg"));
            } catch (IOException e) {
                log.log(Level.INFO, "Problem occurred while replacing cfg", e);
            }
        }
    }

    public void restoreCfg() {
        while (Files.exists(Paths.get(tfdir, "lwrtcfg"))) {
            try {
                delete(Paths.get(tfdir, "cfg"));
                Files.move(Paths.get(tfdir, "lwrtcfg"), Paths.get(tfdir, "cfg"));
            } catch (IOException e) {
                log.log(Level.INFO, "Problem occurred while restoring cfg", e);
            }
        }
    }

    public void replaceHud(String hud) {
        while (!Files.exists(Paths.get(tfdir, "lwrthud"))) {
            try {
                Files.createDirectory(Paths.get(tfdir, "lwrthud"));
            } catch (IOException e) {
                log.log(Level.INFO, "Problem occurred while creating directory", e);
            }
            if (hudPath != null) {
                try {
                    Files.move(hudPath, Paths.get(tfdir, "lwrthud").resolve(hudPath.getFileName()));
                    copy(Paths.get("hud", hud), Paths.get(tfdir, "custom", "lwrthud"));
                } catch (IOException e) {
                    log.log(Level.INFO, "Problem occurred while replacing hud", e);
                }
            }
        }
    }

    public void restoreHud() {
        while (Files.exists(Paths.get(tfdir, "lwrthud"))) {
            try {
                delete(Paths.get(tfdir, "custom", "lwrthud"));
            } catch (NoSuchFileException e) {
                log.info("Custom hud folder not found");
            } catch (IOException e) {
                log.log(Level.INFO, "Problem occurred while deleting lawena hud", e);
            }
            try {
                if (hudPath != null) {
                    Files.move(Paths.get(tfdir, "lwrthud").resolve(hudPath.getFileName()), Paths
                            .get(tfdir, "custom").resolve(hudPath.getFileName()));
                }
                delete(Paths.get(tfdir, "lwrthud"));
            } catch (IOException e) {
                log.log(Level.INFO, "Problem occurred while restoring hud", e);
            }
        }
    }

    public void replaceVo() {
        while (!Files.exists(Paths.get(tfdir, "lwrtvo"))) {
            try {
                Files.createDirectory(Paths.get(tfdir, "lwrtvo"));
            } catch (IOException e) {
                log.log(Level.INFO, "Problem occurred while creating directory", e);
            }
            for (Path path : voPaths) {
                try {
                    Files.move(path, Paths.get(tfdir, "lwrtvo").resolve(path.getFileName()));
                } catch (IOException e) {
                    log.log(Level.INFO, "Problem occurred while moving existing vo files", e);
                }
            }
            try {
                Files.createDirectories(Paths.get(tfdir, "custom", "lwrtvo", "sound", "vo"));
                copy(Paths.get("sound", "vo"),
                        Paths.get(tfdir, "custom", "lwrtvo", "sound", "vo"));
            } catch (IOException e) {
                log.log(Level.INFO, "Problem occurred while copying lawena vo files", e);
            }
        }
    }

    public void restoreVo() {
        while (Files.exists(Paths.get(tfdir, "lwrtvo"))) {
            try {
                delete(Paths.get(tfdir, "custom", "lwrtvo"));
            } catch (IOException e) {
                log.log(Level.INFO, "Problem occurred while deleting lawena vo files", e);
            }
            for (Path path : voPaths) {
                try {
                    Files.move(Paths.get(tfdir, "lwrtvo").resolve(path.getFileName()),
                            Paths.get(tfdir, "custom").resolve(path.getFileName()));
                } catch (IOException e) {
                    log.log(Level.INFO, "Problem occurred while restoring vo files", e);
                }
            }
            try {
                delete(Paths.get(tfdir, "lwrtvo"));
            } catch (IOException e) {
                log.log(Level.INFO, "Problem occurred while deleting lawena vo folder", e);
            }
        }
    }

    public void replaceMisc(boolean dom, boolean ann) {
        while (!Files.exists(Paths.get(tfdir, "lwrtmisc"))) {
            try {
                Files.createDirectory(Paths.get(tfdir, "lwrtmisc"));
            } catch (IOException e) {
                log.log(Level.INFO, "Problem occurred while creating directory", e);
            }
            for (Path path : miscPaths) {
                try {
                    Files.move(path, Paths.get(tfdir, "lwrtmisc").resolve(path.getFileName()));
                } catch (IOException e) {
                    log.log(Level.INFO, "Problem occurred while moving existing misc files", e);
                }
            }
            try {
                Path soundMiscPath = Paths.get(tfdir, "custom", "lwrtmisc", "sound", "misc");
                Files.createDirectories(soundMiscPath);
                copy(Paths.get("sound", "misc"), soundMiscPath);
                if (dom)
                    copy(Paths.get("sound", "miscdom"), soundMiscPath);
                if (ann)
                    copy(Paths.get("sound", "miscann"), soundMiscPath);
            } catch (IOException e) {
                log.log(Level.INFO, "Problem occurred while copying lawena misc files", e);
            }
        }
    }

    public void restoreMisc() {
        while (Files.exists(Paths.get(tfdir, "lwrtmisc"))) {
            try {
                delete(Paths.get(tfdir, "custom", "lwrtmisc"));
            } catch (NoSuchFileException e) {
                log.info("Custom misc folder not found");
            } catch (IOException e) {
                log.log(Level.INFO, "Problem occurred while deleting lawena misc files", e);
            }
            for (Path path : miscPaths) {
                try {
                    Files.move(Paths.get(tfdir, "lwrtmisc").resolve(path.getFileName()),
                            Paths.get(tfdir, "custom").resolve(path.getFileName()));
                } catch (IOException e) {
                    log.log(Level.INFO, "Problem occurred while restoring misc files", e);
                }
            }
            try {
                delete(Paths.get(tfdir, "lwrtmisc"));
            } catch (IOException e) {
                log.log(Level.INFO, "Problem occurred while deleting lawena misc folder", e);
            }
        }

    }

    public void replaceSkybox(final String filename) {
        while (!Files.exists(Paths.get(tfdir, "lwrtSkybox"))) {
            try {
                Files.createDirectory(Paths.get(tfdir, "lwrtSkybox"));
            } catch (IOException e) {
                log.log(Level.INFO, "Problem occurred while creating directory", e);
            }
            for (Path path : skyboxPaths) {
                try {
                    Files.move(path, Paths.get(tfdir, "lwrtSkybox").resolve(path.getFileName()));
                } catch (IOException e) {
                    log.log(Level.INFO, "Couldn't backup Skybox files", e);
                }
            }

            List<Path> vmtPaths = new ArrayList<>();
            List<Path> vtfPaths = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("Skybox"))) {
                for (Path path : stream) {
                    if (path.endsWith(".vmt")) {
                        Files.copy(path,
                                Paths.get(tfdir, "custom", "lwrtSkybox", "materials", "Skybox")
                                        .resolve(path.getFileName()));
                        vmtPaths.add(path);
                    }
                    if (path.endsWith(".vtf")) {
                        vtfPaths.add(path);
                    }
                }
            } catch (IOException e) {
                log.log(Level.INFO, "Problem occurred while replacing Skybox files", e);
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
                        try {
                            Files.copy(
                                    vtfPaths.get(i),
                                    Paths.get(tfdir, "custom", "lwrtSkybox", "materials",
                                            "Skybox")
                                            .resolve(
                                                    vmtFilename.substring(0,
                                                            vmtFilename.indexOf(".vmt"))
                                                            + ".vtf"));
                        } catch (IOException e) {
                            log.log(Level.INFO, "Problem occurred while copying Skybox files",
                                    e);
                        }
                    }
                }
            }
        }
    }

    public void restoreSkybox() {
        while (Files.exists(Paths.get(tfdir, "lwrtSkybox"))) {
            try {
                delete(Paths.get(tfdir, "custom", "lwrtSkybox"));
            } catch (NoSuchFileException e) {
                log.info("Custom Skybox folder not found");
            } catch (IOException e) {
                log.log(Level.INFO, "Problem occurred while deleting lawena Skybox files", e);
            }
            for (Path path : skyboxPaths) {
                try {
                    Files.move(Paths.get(tfdir, "lwrtSkybox").resolve(path.getFileName()), Paths
                            .get(tfdir, "custom").resolve(path.getFileName()));
                } catch (IOException e) {
                    log.log(Level.INFO, "Problem occured while restoring Skybox files", e);
                }
            }
            try {
                delete(Paths.get(tfdir, "lwrtSkybox"));
            } catch (IOException e) {
                log.log(Level.INFO, "Problem occurred while deleting lawena Skybox folder", e);
            }
        }
    }

}
