
package com.github.iabarca.lwrt.managers;

import com.github.iabarca.lwrt.games.SteamGame;
import com.github.iabarca.lwrt.lwrt.OSInterface;
import com.github.iabarca.lwrt.profile.Profile;
import com.github.iabarca.lwrt.profile.ProfilesListener;
import com.github.iabarca.lwrt.util.PathTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

public class Profiles {

    private static final Logger log = Logger.getLogger("lawena");

    private Map<String, Profile> profiles;
    private String selectedProfile;

    private transient Map<String, String> arguments;
    private transient Path profilesPath;
    private transient OSInterface os;
    private transient Gson gson;
    private transient SteamGame game;
    private transient List<ProfilesListener> listeners = Collections
            .synchronizedList(new ArrayList<ProfilesListener>());

    public Profiles(SteamGame game) {
        this.game = game;
        profilesPath = Paths.get(game.getShortName().toLowerCase(), "profiles.json");
        gson = new GsonBuilder()
                .registerTypeAdapter(Path.class, new PathTypeAdapter())
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();
        profiles = new LinkedHashMap<>();
        arguments = new HashMap<>();
        os = game.getOSInterface(System.getProperty("os.name"));
        os.setLookAndFeel();
        loadProfiles();
    }

    public Profile getProfile() {
        String defProfileName = "Default";
        if (selectedProfile == null || !profiles.containsKey(selectedProfile)) {
            if (profiles.get(defProfileName) != null) {
                selectedProfile = defProfileName;
            } else if (profiles.size() > 0) {
                selectedProfile = profiles.values().iterator().next().getName();
            } else {
                selectedProfile = defProfileName;
                profiles.put(selectedProfile, game.createProfile(selectedProfile));
            }
        }
        return profiles.get(this.selectedProfile);
    }

    public void setSelectedProfile(String selected) {
        boolean update = !selectedProfile.equals(selected);
        selectedProfile = selected;

        if (update) {
            log.fine("--- Fire profile refresh event ---");
            fireProfileRefreshed();
        }
    }

    public void addProfileListener(ProfilesListener listener) {
        listeners.add(listener);
    }

    private void fireProfileRefreshed() {
        if (!listeners.isEmpty()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    for (ProfilesListener listener : listeners) {
                        listener.onRefresh(Profiles.this);
                    }
                }
            });
        }
    }

    public void saveProfiles() throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(profilesPath.toFile()));
        writer.println(gson.toJson(this));
        writer.close();
    }

    public void loadProfiles() {
        try {
            Profiles loaded = gson.fromJson(new FileReader(profilesPath.toFile()),
                    Profiles.class);
            profiles.clear();
            profiles.putAll(loaded.profiles);
            selectedProfile = loaded.selectedProfile;
        } catch (FileNotFoundException e) {
            log.info("Profiles file not found, loading defaults");
        } catch (JsonSyntaxException | JsonIOException e) {
            log.log(Level.WARNING, "Could not load profiles", e);
        }
    }

    public Map<String, Profile> getProfiles() {
        return profiles;
    }

    public SteamGame getGame() {
        return game;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    public OSInterface getInterface() {
        return os;
    }

    // /////////////////////////////////////////

    public void validateGamePath() {
        Path gamePath = getProfile().getGamePath();
        if (gamePath == null || gamePath.toString().isEmpty()) {
            gamePath = os.getSteamPath().resolve(game.getRelativeSteamGamePath());
        }
        if (!Files.exists(gamePath)) {
            gamePath = getSelectedGamePath();
            if (gamePath == null) {
                log.info("No game directory specified, exiting.");
                System.exit(1);
            }
        }
        getProfile().setGamePath(gamePath.toString());
    }

    private Path getSelectedGamePath() {
        String gameFolderName = game.getRequiredGameFolderName();
        Path selected = null;
        int ret = 0;
        JFileChooser chooser;
        while ((selected == null && ret == 0)
                || (selected != null && (!Files.exists(selected) || !selected.toFile().getName()
                        .toString().equals(gameFolderName)))) {
            chooser = new JFileChooser();
            chooser.setDialogTitle("Choose your \"" + gameFolderName + "\" directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setCurrentDirectory(os.getSteamPath().toFile());
            chooser.setFileHidingEnabled(false);
            ret = chooser.showOpenDialog(null);
            if (ret == JFileChooser.APPROVE_OPTION) {
                selected = chooser.getSelectedFile().toPath();
            } else {
                selected = null;
            }
            log.finer("Selected path: " + selected);
        }
        return selected;
    }

    public void validateMoviePath() {
        Path moviePath = getProfile().getMoviePath();
        if (moviePath == null || moviePath.toString().isEmpty() || !Files.exists(moviePath)) {
            moviePath = getSelectedMoviePath();
            if (moviePath == null) {
                log.info("No movie directory specified, exiting.");
                System.exit(1);
            }
        }
        getProfile().setMoviePath(moviePath.toString());
    }

    private Path getSelectedMoviePath() {
        Path selected = null;
        int ret = 0;
        JFileChooser chooser;
        while ((selected == null && ret == 0) || (selected != null && !Files.exists(selected))) {
            chooser = new JFileChooser();
            chooser.setDialogTitle("Choose a directory to store your movie files");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            ret = chooser.showOpenDialog(null);
            if (ret == JFileChooser.APPROVE_OPTION) {
                selected = chooser.getSelectedFile().toPath();
            } else {
                selected = null;
            }
        }
        return selected;
    }

}
