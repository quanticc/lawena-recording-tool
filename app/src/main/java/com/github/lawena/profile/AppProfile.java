package com.github.lawena.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;

/**
 * Represents the data container that will hold all user and game settings.
 *
 * @author Ivan
 */
public class AppProfile implements Profile {

    private String name = "Default"; //$NON-NLS-1$
    private Integer appId = 0;
    private Map<String, String> settings = new HashMap<>();
    private transient List<InvalidationListener> listeners = new ArrayList<>();

    public AppProfile() {
    }

    public AppProfile(Integer appId, String name) {
        this.appId = appId;
        this.name = name;
    }

    static void copy(AppProfile src, AppProfile dest) {
        dest.name = "<copy>"; //$NON-NLS-1$
        dest.appId = src.appId;
        for (Entry<String, String> e : src.settings.entrySet()) {
            dest.settings.put(e.getKey(), e.getValue());
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        // for now only publish renaming events
        Platform.runLater(() -> listeners.forEach(c -> c.invalidated(this)));
    }

    @Override
    public Integer getAppId() {
        return appId;
    }

    @Override
    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(settings.get(key));
    }

    @Override
    public void set(String key, String value) {
        settings.put(key, value);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        AppProfile other = (AppProfile) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public void addListener(InvalidationListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        listeners.remove(listener);
    }

}
