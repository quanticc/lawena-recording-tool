package com.github.lawena.service;

import com.github.lawena.Messages;
import com.github.lawena.config.Constants;
import com.github.lawena.domain.*;
import com.github.lawena.service.util.CopyDirVisitor;
import com.github.lawena.service.util.DeleteDirVisitor;
import com.github.lawena.util.LaunchException;
import com.github.lawena.util.LwrtUtils;
import com.github.lawena.views.tf2.skybox.Skybox;
import com.github.lawena.vpk.*;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import javafx.collections.FXCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.lawena.event.LaunchStatusUpdateEvent.updateEvent;
import static com.github.lawena.util.LwrtUtils.newProcessReader;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final ValidationService validationService;
    private final Resources resources;
    private final GitService gitService;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public FileService(ValidationService validationService, Resources resources, GitService gitService, ApplicationEventPublisher publisher) {
        this.validationService = validationService;
        this.resources = resources;
        this.gitService = gitService;
        this.publisher = publisher;
    }

    public void replaceFiles() throws LaunchException {
        Profile profile = validationService.getSelectedProfile();
        // create configuration files
        publisher.publishEvent(updateEvent(this, Messages.getString("ui.tasks.launch.configFiles")));
        // construct scopes from launcher.settings, profiles can override if keys match
        Map<String, Object> scopes = new LinkedHashMap<>();
        Launcher launcher = validationService.getSelectedLauncher();
        scopes.putAll(launcher.getSettings());
        try {
            generateSettingsFile(scopes);
            generateBindingsFiles(scopes);
            generateMovieSegmentSlots(profile);
        } catch (IOException e) {
            throw new LaunchException("Could not generate config files", e);
        }
        log.debug("Template scopes used: {}", scopes);
        try {
            generateMovieCurrentSlot(profile);
        } catch (IOException e) {
            // don't abort launch, might be a reparse point
            log.info("Could not detect current movie slot");
        }
        // setup repository properly
        gitService.setup();
        /*
        Parameters required:
        - (ALL) basePath from launcher
        - (ALL) gamePath from launcher
        - (TF2) selected hud name from profile --> String
        - (TF2) selected skybox name from profile --> String
        - (ALL) custom resources selected + filters, from resource (or profile?)
         */
        Path basePath = validationService.getBasePath(profile);
        Path gamePath = validationService.getGamePath(profile);
        String selectedHud = validationService.getString(profile, "tf2.hud");
        String selectedSkybox = validationService.getString(profile, "tf2.skybox");

        // remove previous launch setup folder
        Path launchSetup = basePath.resolve("launch");
        if (Files.exists(launchSetup)) {
            try {
                log.debug("Removing previous launch setup folder");
                Files.walkFileTree(launchSetup, new DeleteDirVisitor());
            } catch (IOException e) {
                throw new LaunchException("Could not delete launch setup directory", e);
            }
        }

        // prepare folders to copy
        List<Resource> toCopy = new ArrayList<>();
        try {
            toCopy.add(newTempResource(basePath.resolve("config"), s -> !s.endsWith(".cfg")));
            toCopy.add(newTempResource(basePath.resolve("hud"), s -> !s.contains(selectedHud)));
            if (!selectedSkybox.equals(Skybox.DEFAULT.getName())) {
                toCopy.add(newTempResource(buildSelectedSkybox(basePath, selectedSkybox)));
            }
            toCopy.add(newTempResource(basePath.resolve("default")));
        } catch (IOException e) {
            throw new LaunchException("Could not explore resource contents", e);
        }

        // add all enabled custom resources
        toCopy.addAll(resources.getResourceList().stream().filter(Resource::isEnabled).collect(Collectors.toList()));

        /*
        Launcher fileCopy strategies: SteamPipe, Classic
        - SteamPipe strategy uses "custom" folder to copy all content collected in the "copied" list
        - Classic strategy uses the gamePath folder replacing needed folders, like cfg, materials, resources or scripts folders
         */

        // SteamPipe/Backup
        Path cfgDir = gamePath.resolve("cfg");
        Path customDir = gamePath.resolve("custom");
        Path movedCustomDir;
        // move folders away
        // TODO: don't use this everytime
        try {
            String now = "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            backup(cfgDir, now);
            movedCustomDir = backup(customDir, now);
        } catch (IOException e) {
            throw new LaunchException("Could not move original folders", e);
        }

        // SteamPipe/Replace
        int count = 1;
        for (Resource resource : toCopy) {
            publisher.publishEvent(updateEvent(this, Messages.getString("ui.tasks.launch.copyingFile", resource.getPath()), count, toCopy.size()));
            try {
                Path srcPath = resource.getPath(); // where the root of the content to copy is located
                if (srcPath.toString().toLowerCase().endsWith(".vpk")) {
                    // transform into filtered content if needed
                    if (!resource.getExcludedPaths().isEmpty()) {
                        srcPath = extractVpk(srcPath, basePath.resolve("launch"));
                    }
                } else if (!Files.isDirectory(srcPath)) {
                    log.warn("Ignoring resources at invalid directory: {}", srcPath);
                    continue;
                }
                String id = LwrtUtils.leftPad(count++ + "", 2, '0');
                if (srcPath.startsWith(customDir)) {
                    // correctly resolve resource path if it was located in gamePath/custom folder, which was moved just now
                    srcPath = movedCustomDir.resolve(srcPath.getFileName());
                }
                List<Path> excludedList = new ArrayList<>();
                resource.getExcludedPaths().stream().map(srcPath::resolve).forEach(excludedList::add);
                Path destPath = customDir.resolve("lawena-" + id + "-" + srcPath.getFileName());
                log.info("Copying resource: {} -> {}{}", srcPath, destPath, formatSize(excludedList));
                Files.walkFileTree(srcPath, EnumSet.allOf(FileVisitOption.class), Integer.MAX_VALUE,
                        new CopyDirVisitor(srcPath, destPath, excludedList));
            } catch (IOException e) {
                log.warn("Could not copy resource contents", e);
            }
        }

        // TODO: install fonts found in custom HUD

        // if this operation fails, the launch will be aborted and rolled back with "restore" method
        gitService.replace();

        // after this point, game is ready to be launched
    }

    private String formatSize(List<Path> list) {
        int size = list.size();
        return (size == 0 ? "" : " (excluding " + list.size() + " file" + (size == 1 ? ")" : "s)"));
    }

    private Path extractVpk(Path srcVpkPath, Path destExtractDir) throws IOException {
        try {
            Archive vpk = new Archive(srcVpkPath.toFile());
            vpk.load();
            Path vpkExtractPath = destExtractDir.resolve(srcVpkPath.getFileName());
            Files.createDirectories(vpkExtractPath);
            for (Directory dir : vpk.getDirectories()) {
                Path dirExtractPath = vpkExtractPath.resolve(dir.getPath());
                Files.createDirectories(dirExtractPath);
                for (Entry entry : dir.getEntries()) {
                    try {
                        entry.extract(new File(vpkExtractPath.toString(), dir.getPathFor(entry)));
                    } catch (IOException e) {
                        log.warn("Could not extract entry", e);
                    }
                }
            }
            return vpkExtractPath;
        } catch (ArchiveException | EntryException e) {
            throw new IOException("Invalid VPK archive", e);
        }
    }

    private void generateSettingsFile(Map<String, Object> scopes) throws LaunchException {
        Path cfgPath = validationService.getBasePath().resolve("config/cfg");
        Path settingsTemplatePath = cfgPath.resolve("settings.mustache");
        Path settingsCfgPath = cfgPath.resolve("settings.cfg");
        int frameRate = validationService.getInteger("recorder.fps");
        int[] steps = {60, 120, 240, 480, 960, 1920, 3840};
        int nextStep = steps[0];
        int prevStep = steps[steps.length - 1];
        for (int i = 0; i < steps.length; i++) {
            if (frameRate < steps[i]) {
                nextStep = steps[i];
                prevStep = steps[(i - 1) % steps.length];
                break;
            } else if (frameRate == steps[i]) {
                nextStep = steps[(i + 1) % steps.length];
                prevStep = steps[(i - 1) % steps.length];
                break;
            }
        }
        scopes.put("fps_prev_step", prevStep);
        scopes.put("fps_next_step", nextStep);
        scopes.put("recorder.fps", frameRate);
        scopes.put("tf2.cfg.viewmodelFov", validationService.getString("tf2.cfg.viewmodelFov"));
        scopes.put("tf2.cfg.viewmodels." + validationService.getString("tf2.cfg.viewmodels"), true);
        scopes.put("tf2.cfg.custom", validationService.getString("tf2.cfg.custom"));
        scopes.putIfAbsent("volume", "0.5");
        Launcher launcher = validationService.getSelectedLauncher();
        for (ConfigFlag flag : launcher.getFlags()) {
            String key = flag.getKey();
            boolean enabled = validationService.mapAndGet(key, o -> (Boolean) o);
            Object value = (enabled ? flag.getTrueMappedValue() : flag.getFalseMappedValue());
            if (value != null) {
                scopes.put(key, value);
            }
        }
        log.debug("Generating settings CFG files");
        compileTemplateAndExecute(settingsTemplatePath, settingsCfgPath, "settings", scopes);
    }

    private void generateBindingsFiles(Map<String, Object> scopes) throws LaunchException {
        Path cfgPath = validationService.getBasePath().resolve("config/cfg");
        Path bindingsTemplatePath = cfgPath.resolve("recbindings.mustache");
        Path bindingsCfgPath = cfgPath.resolve("recbindings.cfg");
        Path helpTemplatePath = cfgPath.resolve("help.mustache");
        Path helpCfgPath = cfgPath.resolve("help.cfg");
        // fallback in case these are not already in the scope
        scopes.putIfAbsent("host_timescale", "0.001");
        scopes.putIfAbsent("key_record", "P");
        scopes.putIfAbsent("key_toggleragdolls", "R");
        scopes.putIfAbsent("key_lockviewmodelsoff", "F1");
        scopes.putIfAbsent("key_lockviewmodelson", "F2");
        scopes.putIfAbsent("key_lockviewmodels", "N");
        scopes.putIfAbsent("key_lockcrosshair", "M");
        scopes.putIfAbsent("key_fpsup", "UPARROW");
        scopes.putIfAbsent("key_fpsdown", "DOWNARROW");
        scopes.putIfAbsent("key_showhelp", "F3");
        scopes.putIfAbsent("key_togglehud", "H");
        scopes.putIfAbsent("key_togglenotices", "K");
        scopes.putIfAbsent("key_cam_back", "KP_DOWNARROW");
        scopes.putIfAbsent("key_cam_backleft", "KP_END");
        scopes.putIfAbsent("key_cam_left", "KP_LEFTARROW");
        scopes.putIfAbsent("key_cam_frontleft", "KP_HOME");
        scopes.putIfAbsent("key_cam_front", "KP_UPARROW");
        scopes.putIfAbsent("key_cam_frontright", "KP_PGUP");
        scopes.putIfAbsent("key_cam_right", "KP_RIGHTARROW");
        scopes.putIfAbsent("key_cam_backright", "KP_PGDN");
        scopes.putIfAbsent("key_toggledist", "KP_5");
        scopes.putIfAbsent("key_togglepitch", "KP_INS");
        scopes.putIfAbsent("key_firstperson", "KP_MINUS");
        scopes.putIfAbsent("key_thirdperson", "KP_PLUS");
        addTranslatedKeys(scopes);
        log.debug("Generating key bindings CFG files");
        compileTemplateAndExecute(bindingsTemplatePath, bindingsCfgPath, "recbindings", scopes);
        compileTemplateAndExecute(helpTemplatePath, helpCfgPath, "help", scopes);
    }

    private void addTranslatedKeys(Map<String, Object> scopes) {
        Map<String, Object> translationScope = new HashMap<>();
        // only process scopes with key starting with "key."
        scopes.forEach((k, v) -> {
            if (k.startsWith("key_")) {
                translationScope.put(k + "_user", Constants.USER_FRIENDLY_KEYMAP.getOrDefault(v, v));
            }
        });
        scopes.putAll(translationScope);
    }

    private void compileTemplateAndExecute(Path inputPath, Path outputPath, String name, Map<String, Object> scopes) throws LaunchException {
        try (Reader reader = Files.newBufferedReader(inputPath, Charset.forName("UTF-8"));
             Writer writer = Files.newBufferedWriter(outputPath, Charset.forName("UTF-8"))) {
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(reader, name);
            mustache.execute(writer, scopes);
            writer.flush();
        } catch (IOException e) {
            throw new LaunchException("Could not generate file", e);
        }
    }

    private void generateMovieCurrentSlot(Profile profile) throws LaunchException, IOException {
        Path cfgPath = validationService.getBasePath(profile).resolve("config/cfg");
        Path framesPath = validationService.getPath(profile, "path.frames");
        String lastMovie = "";
        String alias = "alias namescroll stmov1";
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(framesPath, "*.tga")) {
            for (Path movieFile : stream) {
                String filename = movieFile.getFileName().toString();
                lastMovie = (lastMovie.compareTo(filename) > 0 ? lastMovie : filename);
            }
        }
        if (!lastMovie.equals("")) {
            int idx = "abcdefghijklmnopqrstuvwxy".indexOf(lastMovie.charAt(0));
            if (idx >= 0) {
                alias = "alias namescroll stmov" + (idx + 2);
            } else if (lastMovie.charAt(0) == 'z') {
                alias = "alias namescroll noslots";
            }
        }
        Files.write(cfgPath.resolve("namescroll.cfg"), Collections.singletonList(alias), Charset.forName("UTF-8"));
    }

    private void generateMovieSegmentSlots(Profile profile) throws IOException, LaunchException {
        String[] prefixes =
                {"a1", "b2", "c3", "d4", "e5", "f6", "g7", "h8", "i9", "j10", "k11", "l12", "m13", "n14",
                        "o15", "p16", "q17", "r18", "s19", "t20", "u21", "v22", "w23", "x24", "y25", "z26"};
        Path basePath = validationService.getBasePath(profile);
        String mode = validationService.getString(profile, "recorder.mode");
        String video = (Constants.JPEG_CAPTURE_MODE_KEY.equals(mode) ? "jpg" : "tga");
        String audio = "wav";
        int quality = validationService.getInteger(profile, "recorder.quality");
        String framesDir = validationService.getString(profile, "path.frames");
        Path folder = basePath.resolve("config/cfg/mov");
        Files.createDirectories(folder);
        for (String prefix : prefixes) {
            List<String> lines =
                    Collections.singletonList("startmovie \"" + framesDir + "/" + prefix + "_\" " + video + " "
                            + audio + (video.equals("jpg") ? " jpeg_quality " + quality : ""));
            Files.write(basePath.resolve("config/cfg/mov/" + prefix + ".cfg"), lines, Charset.forName("UTF-8"));
        }
    }

    public void restoreFiles() throws LaunchException {
        Path gamePath = validationService.getGamePath();
        if (gamePath != null) {
            // TODO: use folders given by the replacing-strategy
            closeHandles(gamePath.resolve("cfg"));
            closeHandles(gamePath.resolve("custom"));
            // TODO: handle case when cfg or custom were symlinks originally
        }
        gitService.restore();
    }

    private Path backup(Path path, String suffix) throws IOException {
        setWritable(path);
        return Files.move(path, path.resolveSibling(path.getFileName() + suffix));
    }

    private Path buildSelectedSkybox(Path basePath, String selectedSkybox) throws IOException {
        // Skybox files
        Path launchSkyboxPath = basePath.resolve("launch/skybox");
        Path contentSkyboxPath = launchSkyboxPath.resolve("materials/skybox");
        Files.createDirectories(contentSkyboxPath);
        Set<Path> vtfPaths = new HashSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(basePath.resolve("skybox/vtf"))) {
            for (Path path : stream) {
                String pathStr = path.toFile().getName();
                // only save vtf files matching our selected skybox
                if (pathStr.endsWith(".vtf") && pathStr.startsWith(selectedSkybox)) {
                    vtfPaths.add(path);
                }
            }
        }
        Set<Path> vmtPaths = new HashSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(basePath.resolve("skybox/vmt"))) {
            for (Path path : stream) {
                String pathStr = path.toFile().getName();
                // collect vmt original skybox names to perform the replace later
                if (pathStr.endsWith(".vmt")) {
                    vmtPaths.add(path);
                    Files.copy(path, contentSkyboxPath.resolve(pathStr));
                }
            }
        }
        // rename selected skybox according to each original skybox name
        for (Path vtfPath : vtfPaths) {
            for (Path vmtPath : vmtPaths) {
                String vtf = vtfPath.getFileName().toString();
                String vmt = vmtPath.getFileName().toString();
                if ((vtf.endsWith("up.vtf") && vmt.endsWith("up.vmt"))
                        || (vtf.endsWith("dn.vtf") && vmt.endsWith("dn.vmt"))
                        || (vtf.endsWith("bk.vtf") && vmt.endsWith("bk.vmt"))
                        || (vtf.endsWith("ft.vtf") && vmt.endsWith("ft.vmt"))
                        || (vtf.endsWith("lf.vtf") && vmt.endsWith("lf.vmt"))
                        || (vtf.endsWith("rt.vtf") && vmt.endsWith("rt.vmt"))) {
                    Path link = contentSkyboxPath.resolve(vmt.substring(0, vmt.indexOf(".vmt")) + ".vtf");
                    Files.copy(vtfPath, link);
                }
            }
        }
        return launchSkyboxPath;
    }

    private Resource newTempResource(Path path) {
        Resource resource = new AppResource(path);
        resource.setEnabled(true);
        return resource;
    }

    private Resource newTempResource(Path path, Predicate<String> excludes) throws IOException {
        Resource resource = newTempResource(path);
        resource.setExcludedPaths(
                FXCollections.observableArrayList(
                        AppResources.getContents(resource).stream()
                                .filter(excludes)
                                .collect(Collectors.toList())));
        return resource;
    }

    private static void setWritable(Path path) {
        if (!path.toFile().setWritable(true)) {
            log.warn("Could not set path as writable: {}", path);
        }
    }

    private void closeHandles(Path path) {
        if (!LwrtUtils.isWindows()) {
            return;
        }
        if (!LwrtUtils.isAdmin()) {
            log.warn("Handle closing only works when running the tool as administrator");
            return;
        }
        publisher.publishEvent(updateEvent(this, Messages.getString("ui.tasks.launch.closingHandles", path.toAbsolutePath())));
        try {
            Path handlePath = Constants.HANDLE_PATH;
            ProcessBuilder pb = new ProcessBuilder(handlePath.toString(), path.toString());
            Process pr = pb.start();
            try (BufferedReader input = newProcessReader(pr)) {
                String line;
                int count = 0;
                while ((line = input.readLine()) != null) {
                    if (count > 4) {
                        String[] columns = line.split("[ ]+type: [A-Za-z]+[ ]+|: |[ ]+pid: ");
                        if (columns.length == 4) {
                            log.info("[handle] Closing handle " + columns[3] + " opened by " + columns[0]);
                            closeHandle(columns[1], columns[2]);
                        } else {
                            log.debug("[handle] " + line);
                        }
                    }
                    count++;
                }
            }
            pr.waitFor();
        } catch (InterruptedException | IOException e) {
            log.warn("Could not complete handle closing: {}", e.toString());
        }
    }

    private void closeHandle(String pid, String handle) {
        try {
            Path handlePath = Constants.HANDLE_PATH;
            ProcessBuilder pb = new ProcessBuilder(handlePath.toString(), "-c", handle, "-p", pid, "-y");
            Process pr = pb.start();
            try (BufferedReader input = newProcessReader(pr)) {
                String line;
                int count = 0;
                while ((line = input.readLine()) != null) {
                    if (count > 7) {
                        log.info("[handle] " + line);
                    }
                    count++;
                }
            }
            pr.waitFor();
        } catch (InterruptedException | IOException e) {
            log.warn("Could not close handle: {}", e.toString());
        }
    }
}
