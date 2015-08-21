package com.github.lawena;

import com.github.lawena.game.SourceGame;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AppSettings {

    private Map<Integer, SourceGame> games = new HashMap<>();
    private Map<String, String> settings = new HashMap<>();
    private boolean prioritizedLocal = false;

    public Map<Integer, SourceGame> getGames() {
        return games;
    }

    public void setGames(Map<Integer, SourceGame> games) {
        this.games = games;
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(settings.get(key));
    }

    public void set(String key, String value) {
        settings.put(key, value);
    }

    public boolean isPrioritizedLocal() {
        return prioritizedLocal;
    }

    public void setPrioritizedLocal(boolean prioritizedLocal) {
        this.prioritizedLocal = prioritizedLocal;
    }
}
