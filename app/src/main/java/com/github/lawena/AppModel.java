package com.github.lawena;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import com.github.lawena.exts.DescriptorProvider;
import com.github.lawena.exts.TagProvider;
import com.github.lawena.files.AppResources;
import com.github.lawena.files.Resources;
import com.github.lawena.game.GameDescription;
import com.github.lawena.profile.AppProfiles;
import com.github.lawena.profile.Profiles;
import com.github.lawena.update.Updater;
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

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.PluginManager;

public class AppModel implements Model {
    private static final Logger log = LoggerFactory.getLogger(AppModel.class);
    private static final Charset DEFAULT = Charset.forName("UTF-8");
    private static final OpenOption[] WRITE = {StandardOpenOption.WRITE, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING};
    private static Version version = Version.valueOf("5.0.0");

    private final LogAppender logAppender;
    private final PluginManager pluginManager;

    private final Map<String, String> parameters = new HashMap<>();
    private final Updater updater;
    private final Resources resources;
    private final HostServices hostServices;

    private Profiles profiles = new AppProfiles();
    private AppSettings settings = new AppSettings();

    public AppModel(Application application) {
        Objects.requireNonNull(application);
        logAppender = initLog();
        hostServices = application.getHostServices();
        application.getParameters().getUnnamed().forEach(p -> parameters.put(p, null));
        parameters.putAll(application.getParameters().getNamed());
        parameters.putAll(loadVersionData());
        parameters.put("settings", "lwrt/settings.json"); //NON-NLS
        parameters.put("profiles", "lwrt/profiles.json"); //NON-NLS
        updater = new Updater();
        logVMInfo();
        pluginManager = new DefaultPluginManager();
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
        resources = new AppResources();
        resources.setProviders(pluginManager.getExtensions(TagProvider.class));
        loadSettings();
        loadProfiles();
    }

    private static List<String> readVersionData() {
        try {
            return Files.readAllLines(Paths.get("describe.tmp"), Charset.forName("UTF-8"));
        } catch (IOException e) {
            return Arrays.asList(LwrtUtils.getManifestString("git-describe", version.getNormalVersion()), //NON-NLS
                    LwrtUtils.getManifestString("buildtime", LwrtUtils.now("yyyyMMddHHmmss"))); //NON-NLS
        }
    }

    private static Map<String, String> loadVersionData() {
        Map<String, String> map = new LinkedHashMap<>();
        List<String> data = readVersionData();
        String describe = data.get(0);
        map.put("build", data.get(1)); //NON-NLS
        if (describe.startsWith("v")) { //NON-NLS
            describe = describe.substring(1);
        }
        map.put("version", describe);
        Pattern regex = Pattern.compile("^\\s?(\\d+.\\d+.\\d+(?:[A-Za-z\\d\\.\\-]+)?)-(\\d+)-(g[0-9a-f]+)\\s?$");
        Matcher m = regex.matcher(describe);
        String ver = null;
        String ahead = null;
        String hash = null;
        if (m.matches() && m.groupCount() == 3) {
            ver = m.group(1);
            ahead = m.group(2);
            hash = m.group(3);
        } else if (describe.matches("[0-9a-f]+")) {
            // fallback if we only get hash (no tag describe)
            ver = version.getNormalVersion();
            ahead = "1";
            hash = "g" + describe; //NON-NLS
        }
        if (ver == null && ahead == null && hash != null) {
            log.warn("git-describe not compatible with semver: {}", describe);
            return map;
        }
        version = new Version.Builder(ver).setBuildMetadata(ahead + "." + hash).build();
        return map;
    }

    private static LogAppender initLog() {
        ch.qos.logback.classic.Logger rootLog =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("root");
        FxLogAppender appender = new FxLogAppender(rootLog.getLoggerContext());
        rootLog.addAppender(appender);
        return appender;
    }

    private void logVMInfo() {
        log.debug("---------------- Lawena Recording Tool ----------------");
        log.debug("v{} @ {} [{}]", version, getBuildTime(), updater.getCurrentBranchName()); //NON-NLS
        log.debug("----------------------- VM Info -----------------------");
        log.debug("OS name: {} {}", System.getProperty("os.name"), System.getProperty("os.arch"));
        log.debug("Java version: {}", System.getProperty("java.version"));
        log.debug("Java home: {}", System.getProperty("java.home"));
        log.debug("----------------------- Folders -----------------------");
        log.debug("Application: {}", Paths.get("").toAbsolutePath());
        log.debug("Settings: {}", parameters.get("settings")); //NON-NLS
        log.debug("Profiles: {}", parameters.get("profiles")); //NON-NLS
        log.debug("-------------------------------------------------------");
    }

    public String getBuildTime() {
        return parameters.get("build"); //NON-NLS
    }

    @Override
    public Map<Integer, GameDescription> getGames() {
        return settings.getGames();
    }

    private void loadSettings() {
        loadSettings(Paths.get(parameters.get("settings"))); //NON-NLS
    }

    private void loadSettings(Path path) {
        try (Reader reader = Files.newBufferedReader(path, DEFAULT)) {
            settings = GameDescription.getGson().fromJson(reader, AppSettings.class);
        } catch (JsonSyntaxException | JsonIOException | IOException e) {
            log.debug("Could not properly load application settings file: {}", e.toString());
        }
        Map<Integer, GameDescription> games = settings.getGames();
        // also load from extensions - if the appid is absent
        pluginManager.getExtensions(DescriptorProvider.class).stream()
                .map(DescriptorProvider::getDescriptor).forEach(a -> {
            if (settings.isPrioritizedLocal()) {
                games.putIfAbsent(a.getApplaunch(), a);
            } else {
                games.put(a.getApplaunch(), a);
            }
        });
        // populate saved game path data if empty
        games.values().forEach(g -> {
            if ((g.getGamePath() == null || g.getGamePath().isEmpty()) && g.getApplaunch() != null) {
                settings.get("gamePath." + g.getApplaunch().toString()).ifPresent(g::setGamePath); //NON-NLS
            }
        });
    }

    private void loadProfiles() {
        loadProfiles(Paths.get(parameters.get("profiles"))); //NON-NLS
    }

    @Override
    public void saveSettings(Path path) {
        try {
            Files.write(path, Collections.singletonList(GameDescription.getGson().toJson(settings)), DEFAULT, WRITE);
        } catch (IOException e) {
            log.debug("Could not save application settings to file: {}", e.toString());
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
    public HostServices getHostServices() {
        return hostServices;
    }

    @Override
    public void launch() {
        // TODO Auto-generated method stub

    }

    @Override
    public void exit() {
        log.debug("Exiting model");
        resources.stopWatch();
        saveProfiles(Paths.get(parameters.get("profiles"))); //NON-NLS
        saveSettings(Paths.get(parameters.get("settings"))); //NON-NLS
        pluginManager.stopPlugins();
        Platform.exit();
    }
}
