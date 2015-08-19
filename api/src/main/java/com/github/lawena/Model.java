package com.github.lawena;

import com.github.lawena.files.Resources;
import com.github.lawena.game.GameDescription;
import com.github.lawena.profile.Profiles;
import com.github.lawena.util.LogAppender;

import java.nio.file.Path;
import java.util.Map;

import javafx.application.HostServices;
import ro.fortsoft.pf4j.PluginManager;

/**
 * A place for all data-related components in the application.
 *
 * @author Ivan
 */
public interface Model {

    Map<Integer, GameDescription> getGames();

    void saveSettings(Path path);

    Profiles getProfiles();

    void loadProfiles(Path path);

    void saveProfiles(Path path);

    PluginManager getPluginManager();

    HostServices getHostServices();

    void launch();

    LogAppender getLogAppender();

    Resources getResources();

    void exit();

    Map<String, String> getParameters();

}
