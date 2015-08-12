package com.github.lawena;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import com.github.lawena.exts.DescriptorProvider;
import com.github.lawena.exts.TagProvider;
import com.github.lawena.files.AppResources;
import com.github.lawena.files.Resources;
import com.github.lawena.game.GameDescription;
import com.github.lawena.update.Updater;
import com.github.lawena.profile.AppProfiles;
import com.github.lawena.profile.Profiles;
import com.github.lawena.util.FxLogAppender;
import com.github.lawena.util.LogAppender;
import com.github.lawena.util.LwrtUtils;
import com.github.zafarkhaja.semver.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Application.Parameters;
import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.PluginManager;

public class AppModel implements Model {
    private static final Logger log = LoggerFactory.getLogger(AppModel.class);
    private static final Charset DEFAULT = Charset.forName("UTF-8"); //$NON-NLS-1$
    private static final OpenOption[] WRITE = {StandardOpenOption.WRITE, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING};
    private static Version semVer = Version.valueOf("5.0.0"); // hardcode in case of fallback

    private final LogAppender logAppender;
    private final PluginManager pluginManager;

    private final Map<String, String> parameters = new HashMap<>();
    private final Updater updater;
    private final Resources resources;

    private Map<Integer, GameDescription> games = new HashMap<>();
    private Profiles profiles = new AppProfiles();

    public AppModel(Parameters params) {
        Objects.requireNonNull(params);
        logAppender = initLog();
        params.getUnnamed().forEach(p -> parameters.put(p, null));
        parameters.putAll(params.getNamed());
        parameters.putAll(loadVersionData());
        parameters.put("games", "lwrt/games.json"); //$NON-NLS-1$ //$NON-NLS-2$
        parameters.put("profiles", "lwrt/profiles.json"); //$NON-NLS-1$ //$NON-NLS-2$
        updater = new Updater();
        logVMInfo();
        pluginManager = new DefaultPluginManager();
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
        resources = new AppResources();
        resources.setProviders(pluginManager.getExtensions(TagProvider.class));
        loadGameDescriptions();
        loadProfiles();
    }

    private static List<String> readVersionData() {
        try {
            return Files.readAllLines(Paths.get("describe.tmp"), Charset.forName("UTF-8"));
        } catch (IOException e) {
            log.debug("Could not read describe version data: {}", e.toString());
            // read manifest only as a fallback
            return Arrays.asList(LwrtUtils.getManifestString("git-describe", semVer.getNormalVersion()),
                    LwrtUtils.getManifestString("buildtime", LwrtUtils.now("yyyyMMddHHmmss")));
        }
    }

    private static Map<String, String> loadVersionData() {
        Map<String, String> map = new LinkedHashMap<>();
        List<String> data = readVersionData();
        String describe = data.get(0);
        map.put("build", data.get(1));
        if (describe.startsWith("v")) { // remove prefix
            describe = describe.substring(1);
        }
        map.put("version", describe);
        Pattern regex = Pattern.compile("^\\s?(\\d+.\\d+.\\d+(?:[A-Za-z\\d\\.\\-]+)?)-(\\d+)-(g[0-9a-f]+)\\s?$");
        Matcher m = regex.matcher(describe);
        String version = null;
        String ahead = null;
        String hash = null;
        if (m.matches() && m.groupCount() == 3) {
            version = m.group(1);
            ahead = m.group(2);
            hash = m.group(3);
        } else if (describe.matches("[0-9a-f]+")) {
            // fallback if we only get hash (no tag describe)
            version = semVer.getNormalVersion();
            ahead = "1";
            hash = "g" + describe;
        }
        if (version == null && ahead == null && hash != null) {
            log.warn("git-describe not compatible with semver: {}", describe);
            return map;
        }
        semVer = new Version.Builder(version).setBuildMetadata(ahead + "." + hash).build();
        return map;
    }

    private static LogAppender initLog() {
        ch.qos.logback.classic.Logger rootLog =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("root"); //$NON-NLS-1$
        FxLogAppender appender = new FxLogAppender(rootLog.getLoggerContext());
        rootLog.addAppender(appender);
        return appender;
    }

    private void logVMInfo() {
        log.debug("---------------- Lawena Recording Tool ----------------");
        log.debug("v{} @ {} [{}]", semVer, getBuildTime(), updater.getCurrentBranchName());
        log.debug("----------------------- VM Info -----------------------");
        log.debug("OS name: {} {}", System.getProperty("os.name"), System.getProperty("os.arch"));
        log.debug("Java version: {}", System.getProperty("java.version"));
        log.debug("Java home: {}", System.getProperty("java.home"));
        log.debug("----------------------- Folders -----------------------");
        log.debug("Application: {}", Paths.get("").toAbsolutePath());
        log.debug("Game definitions: {}", parameters.get("games"));
        log.debug("Profiles: {}", parameters.get("profiles"));
        log.debug("-------------------------------------------------------");
    }

    public String getBuildTime() {
        return parameters.get("build"); //$NON-NLS-1$
    }

    @Override
    public Map<Integer, GameDescription> getGames() {
        return games;
    }

    private void loadGameDescriptions() {
        loadGameDescriptions(Paths.get(parameters.get("games"))); //$NON-NLS-1$
    }

    private void loadProfiles() {
        loadProfiles(Paths.get(parameters.get("profiles"))); //$NON-NLS-1$
    }

    @Override
    public void loadGameDescriptions(Path path) {
        try (Reader reader = Files.newBufferedReader(path, DEFAULT)) {
            games = GameDescription.getGson().fromJson(reader, new TypeToken<Map<Integer, GameDescription>>() {
            }.getType());
        } catch (JsonSyntaxException | JsonIOException | IOException e) {
            log.debug("Could not properly load game settings file: {}", e.toString()); //$NON-NLS-1$
        }
        // also load from extensions
        pluginManager.getExtensions(DescriptorProvider.class).stream()
                .map(DescriptorProvider::getDescriptor).forEach(a -> games.put(a.getApplaunch(), a));
    }

    @Override
    public void saveGameDescriptions(Path path) {
        try {
            Files.write(path, Collections.singletonList(GameDescription.getGson().toJson(games)), DEFAULT, WRITE);
        } catch (IOException e) {
            log.debug("Could not save game settings to file: {}", e.toString()); //$NON-NLS-1$
        }
    }

    @Override
    public Profiles getProfiles() {
        return profiles;
    }

    @Override
    public void loadProfiles(Path path) {
        profiles.load(path);
    }

    @Override
    public void saveProfiles(Path path) {
        profiles.save(path);
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public LogAppender getLogAppender() {
        return logAppender;
    }

    @Override
    public Resources getResources() {
        return resources;
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public void launch() {
        // TODO Auto-generated method stub

    }

    @Override
    public void exit() {
        resources.shutdown();
        saveProfiles(Paths.get(parameters.get("profiles"))); //$NON-NLS-1$
        pluginManager.stopPlugins();
    }
}
