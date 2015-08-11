package com.github.lawena.game;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.github.lawena.util.RuntimeTypeAdapterFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.beans.property.Property;

/**
 * A descriptor for launching a game through the application, keeping track of all constants
 * required by the application and groups of settings required by the game itself.
 *
 * @author Ivan
 */
public class GameDescription {

    private static final Logger log = LoggerFactory.getLogger(GameDescription.class);
    private static Gson gson;

    private String name;
    private Integer applaunch;
    private String shortName;
    private Map<String, String> processName;
    private String gameFolderName;
    private String localGamePath;
    // view views by name
    private List<String> views = new ArrayList<>();
    // tag views by name
    private List<String> taggers = new ArrayList<>();
    // file views by name
    private List<String> files = new ArrayList<>();
    // command groups
    private List<Group> groups = new ArrayList<>();

    public static Gson getGson() {
        if (gson == null) {
            RuntimeTypeAdapterFactory<Group> adapter =
                    RuntimeTypeAdapterFactory.of(Group.class)
                            .registerSubtype(StringGroup.class, "String") //$NON-NLS-1$
                            .registerSubtype(BooleanGroup.class, "Boolean") //$NON-NLS-1$
                            .registerSubtype(IntegerGroup.class, "Integer") //$NON-NLS-1$
                            .registerSubtype(DoubleGroup.class, "Double"); //$NON-NLS-1$
            gson = new GsonBuilder().registerTypeAdapterFactory(adapter).setPrettyPrinting().create();
        }
        return gson;
    }

    public static GameDescription load(InputStream jsonStream) {
        try (Reader reader = new InputStreamReader(jsonStream, Charset.forName("UTF-8"))) {
            return getGson().fromJson(reader, GameDescription.class);
        } catch (IOException e) {
            log.warn("Could not import game description from JSON", e);
            throw new IllegalArgumentException("Could not import game description", e);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getApplaunch() {
        return applaunch;
    }

    public void setApplaunch(Integer applaunch) {
        this.applaunch = applaunch;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Map<String, String> getProcessName() {
        return processName;
    }

    public void setProcessName(Map<String, String> processName) {
        this.processName = processName;
    }

    public String getGameFolderName() {
        return gameFolderName;
    }

    public void setGameFolderName(String gameFolderName) {
        this.gameFolderName = gameFolderName;
    }

    public String getLocalGamePath() {
        return localGamePath;
    }

    public void setLocalGamePath(String localGamePath) {
        this.localGamePath = localGamePath;
    }

    /**
     * Retrieve the list of UI extension identifiers this <code>App</code> will request to have
     * displayed whenever a profile backed by this <code>App</code> is selected.
     *
     * @return a list of <code>String</code>s identifying UI extensions
     */
    public List<String> getViews() {
        return views;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public Optional<Group> getGroup(Property<?> property) {
        return groups.stream().filter(g -> g.getName().equals(property.getName())).findFirst();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((applaunch == null) ? 0 : applaunch.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GameDescription other = (GameDescription) obj;
        if (applaunch == null) {
            if (other.applaunch != null)
                return false;
        } else if (!applaunch.equals(other.applaunch))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s (AppId: %d)", name, applaunch); //$NON-NLS-1$
    }

    public List<String> getTaggers() {
        return taggers;
    }

    public List<String> getFiles() {
        return files;
    }
}
