package com.github.lawena.tf2;

import com.github.lawena.Controller;
import com.github.lawena.exts.FileProvider;
import com.github.lawena.files.Resource;
import com.github.lawena.game.GameDescription;
import com.github.lawena.profile.Profile;
import com.github.lawena.tf2.skybox.Skybox;
import com.github.lawena.util.FileUtils;
import com.github.lawena.util.LawenaException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ro.fortsoft.pf4j.Extension;

@Extension
public class Linker implements FileProvider {
    private static final Logger log = LoggerFactory.getLogger(Linker.class);

    private Controller controller;
    private GameDescription game;
    private TF2Plugin plugin;

    @Override
    public void init(Controller parent) {
        this.controller = parent;
        this.game = controller.getModel().getGames().get(440);
        this.plugin = (TF2Plugin) controller.getModel().getPluginManager().getPlugin("TF2Plugin").getPlugin();
    }

    @Override
    public String getName() {
        return "tf2-linker";
    }

    @Override
    public void copyLaunchFiles(Profile profile) throws LawenaException {
        log.info("{}", makeLaunchPathList());
    }

    public List<Path> makeLaunchPathList() throws LawenaException {
        Properties cfg = plugin.getConfig();
        Path base = Paths.get(game.getLocalGamePath());
        // List of paths that will be linked from launch/custom to game folder
        List<Path> linkedPaths = new ArrayList<>();
        // Link lawena cfg files to launch folder
        Path srcConfigPath = base.resolve("config"); //$NON-NLS-1$
        linkedPaths.add(srcConfigPath);
        // Hud files
        Path srcHudPath = base.resolve("hud"); //$NON-NLS-1$
        if (Files.exists(srcHudPath)) {
            String hudFolderName = cfg.getHud().getKey();
            // custom hud will be handled by enabled resources
            if (!hudFolderName.equals("custom")) { //$NON-NLS-1$
                linkedPaths.add(srcHudPath.resolve(hudFolderName));
            }
        }
        // Skybox files
        Path launchSkyboxPath = base.resolve("skybox-launch"); //$NON-NLS-1$
        Path contentSkyboxPath = launchSkyboxPath.resolve("materials/skybox"); //$NON-NLS-1$
        try {
            String sky = cfg.getSkybox().getName();
            if (sky != null && !sky.isEmpty() && !sky.equals(Skybox.DEFAULT.getName())) {
                log.debug("Linking selected skybox files"); //$NON-NLS-1$
                Files.createDirectories(contentSkyboxPath);
                Set<Path> vtfPaths = new HashSet<>();
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(base.resolve("skybox/vtf"))) { //$NON-NLS-1$
                    for (Path path : stream) {
                        String pathStr = path.toFile().getName();
                        // only save vtf files matching our selected skybox
                        if (pathStr.endsWith(".vtf") && pathStr.startsWith(sky)) { //$NON-NLS-1$
                            vtfPaths.add(path);
                        }
                    }
                }
                Set<Path> vmtPaths = new HashSet<>();
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(base.resolve("skybox/vmt"))) { //$NON-NLS-1$
                    for (Path path : stream) {
                        String pathStr = path.toFile().getName();
                        // collect vmt original skybox names to perform the replace later
                        if (pathStr.endsWith(".vmt")) { //$NON-NLS-1$
                            vmtPaths.add(path);
                            FileUtils.link(contentSkyboxPath.resolve(pathStr), path);
                        }
                    }
                }
                // rename selected skybox according to each original skybox name
                for (Path vtfPath : vtfPaths) {
                    for (Path vmtPath : vmtPaths) {
                        if (vtfPath.getFileName() == null || vmtPath.getFileName() == null) {
                            continue;
                        }
                        String vtf = vtfPath.getFileName().toString();
                        String vmt = vmtPath.getFileName().toString();
                        if ((vtf.endsWith("up.vtf") && vmt.endsWith("up.vmt")) //$NON-NLS-1$ //$NON-NLS-2$
                                || (vtf.endsWith("dn.vtf") && vmt.endsWith("dn.vmt")) //$NON-NLS-1$ //$NON-NLS-2$
                                || (vtf.endsWith("bk.vtf") && vmt.endsWith("bk.vmt")) //$NON-NLS-1$ //$NON-NLS-2$
                                || (vtf.endsWith("ft.vtf") && vmt.endsWith("ft.vmt")) //$NON-NLS-1$ //$NON-NLS-2$
                                || (vtf.endsWith("lf.vtf") && vmt.endsWith("lf.vmt")) //$NON-NLS-1$ //$NON-NLS-2$
                                || (vtf.endsWith("rt.vtf") && vmt.endsWith("rt.vmt"))) { //$NON-NLS-1$ //$NON-NLS-2$
                            Path link = contentSkyboxPath.resolve(vmt.substring(0, vmt.indexOf(".vmt")) + ".vtf"); //$NON-NLS-1$ //$NON-NLS-2$
                            FileUtils.link(link, vtfPath);
                        }
                    }
                }
            }
            linkedPaths.add(launchSkyboxPath);
        } catch (IOException e) {
            log.warn("Could not link skybox files: {}", e.toString()); //$NON-NLS-1$
            throw new LawenaException(Messages.getString("LinkerTf.linkFailedToCreateSkyboxLinks"), e); //$NON-NLS-1$
        }
        // Other resources
        Path srcDefaultPath = base.resolve("default"); //$NON-NLS-1$
        log.info("Linking enabled custom vpks and folders"); //$NON-NLS-1$
        List<Path> list = new ArrayList<>();
        controller.getModel().getResources().getResourceList().stream().filter(Resource::isEnabled)
                .forEach(r -> list.add(r.getPath()));
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(srcDefaultPath)) {
            stream.forEach(list::add);
        } catch (IOException e) {
            log.warn("Could not include files from default resources folder: {}", e.toString()); //$NON-NLS-1$
        }
        for (Path src : list) {
            if (Files.exists(src)) {
                if (Files.isDirectory(src) || (src.getFileName() != null && src.getFileName().toString().endsWith(".vpk"))) { //$NON-NLS-1$
                    linkedPaths.add(src);
                } else {
                    log.warn("Not a valid resource type: {}", src); //$NON-NLS-1$
                }
            } else {
                log.warn("Enabled resource does not exist: {}", src); //$NON-NLS-1$
            }
        }
        return linkedPaths;
    }
}
