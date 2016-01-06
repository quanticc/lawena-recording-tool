package com.github.lawena.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents the data container that will hold all user and game settings.
 *
 * @author Ivan
 */
public class AppProfile implements Profile {

    private static final Logger log = LoggerFactory.getLogger(AppProfile.class);

    private String name = "Default";
    private String launcher = "?";
    private Map<String, Object> settings = new HashMap<>();

    public AppProfile() {
    }

    public AppProfile(String launcher, String name) {
        this.launcher = launcher;
        this.name = name;
    }

    public static void copy(AppProfile src, AppProfile dest) {
        dest.name = "<copy>";
        dest.launcher = src.launcher;
        for (Entry<String, Object> e : src.settings.entrySet()) {
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
    }

    public String getLauncher() {
        return launcher;
    }

    public void setLauncher(String launcherId) {
        this.launcher = launcherId;
    }

    @Override
    public Optional<Object> get(String key) {
        Optional<Object> o = Optional.ofNullable(settings.get(key));
        log.debug(">> {}: {}", key, o);
        return o;
    }

    @Override
    public void set(String key, Object value) {
        log.debug("<< {}: {}", key, value);
        settings.put(key, value);
    }

    @Override
    public String toString() {
        return "Profile [name=" + name + ", launcher=" + launcher + "]";
    }

    @Override
    public String toFullString() {
        return "Profile [name=" + name + ", launcher=" + launcher + ", settings=" + settings + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppProfile that = (AppProfile) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
