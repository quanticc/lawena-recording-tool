package com.github.lawena.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;

@SuppressWarnings("nls")
public class AppProfiles implements Profiles {

    private static final Logger log = LoggerFactory.getLogger(AppProfiles.class);
    private static final Charset DEFAULT = Charset.forName("UTF-8"); //$NON-NLS-1$
    private static final OpenOption[] WRITE = {StandardOpenOption.WRITE, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING};
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Profile defProfile = new AppProfile(440, "Default");
    private transient ObjectProperty<Profile> _selected = new SimpleObjectProperty<>(this,
            "_selected", defProfile);
    private transient ListProperty<Profile> _profiles = new SimpleListProperty<>(this, "_profiles",
            FXCollections.checkedObservableList(FXCollections.observableArrayList(defProfile),
                    Profile.class));
    private transient ReadOnlyListWrapper<Profile> wrapper = new ReadOnlyListWrapper<>(
            _profiles.get());

    {
        _selected.isNull().addListener((obs, old, value) -> {
            if (value) {
                log.debug("No profiles! Selecting a default one");
                _selected.set(defProfile);
                if (!_profiles.get().contains(defProfile))
                    _profiles.get().add(defProfile);
            }
        });
    }

    @Override
    public final ObjectProperty<Profile> selectedProperty() {
        return _selected;
    }

    @Override
    public final Profile getSelected() {
        return _selected.get();
    }

    @Override
    public ReadOnlyListProperty<Profile> profilesProperty() {
        return wrapper.getReadOnlyProperty();
    }

    @Override
    public Optional<Profile> findByName(String name) {
        Objects.requireNonNull(name);
        Optional<Profile> o =
                _profiles.get().stream().filter(p -> p.getName().equals(name)).findFirst();
        // log.debug("findByName({}): {}", name, o);
        return o;
    }

    @Override
    public boolean containsByName(String name) {
        Objects.requireNonNull(name);
        return findByName(name).isPresent();
    }

    private void verifyExists(Profile profile) {
        // check if the profile is actually in the list
        Objects.requireNonNull(profile);
        if (!_profiles.get().contains(profile))
            throw new IllegalArgumentException("The profile must be already stored");
    }

    private boolean selectProfile(Profile newSelectedProfile) {
        verifyExists(newSelectedProfile);
        _selected.set(newSelectedProfile);
        return true;
    }

    @Override
    public Profile create(Integer appId, String name) {
        Objects.requireNonNull(appId);
        Objects.requireNonNull(name);
        Profile p = createNewProfile(name);
        p.setAppId(appId);
        _profiles.get().add(p);
        selectProfile(p);
        return p;
    }

    private Profile createNewProfile(String newProfileName) {
        AppProfile p = new AppProfile();
        renameProfile(p, newProfileName);
        return p;
    }

    @Override
    public void duplicate(Profile source) {
        verifyExists(source);
        Profile p = duplicateProfile(source);
        _profiles.get().add(p);
        selectProfile(p);
    }

    @Override
    public void duplicate(Profile source, String newName) {
        verifyExists(source);
        Objects.requireNonNull(newName);
        Profile p = duplicateProfile(source, newName);
        _profiles.get().add(p);
        selectProfile(p);
    }

    private Profile duplicateProfile(Profile baseProfile) {
        AppProfile newProfile = new AppProfile();
        AppProfile.copy((AppProfile) baseProfile, newProfile);
        renameProfile(newProfile, findAvailableNameFrom(baseProfile.getName()));
        return newProfile;
    }

    private Profile duplicateProfile(Profile baseProfile, String newProfileName) {
        AppProfile newProfile = new AppProfile();
        AppProfile.copy((AppProfile) baseProfile, newProfile);
        renameProfile(newProfile, newProfileName);
        return newProfile;
    }

    @Override
    public void rename(Profile source, String newName) {
        verifyExists(source);
        Objects.requireNonNull(newName);
        String name = newName.trim();
        if (name.isEmpty())
            throw new IllegalArgumentException("Name must not be empty");
        renameProfile(source, name);
    }

    private void renameProfile(Profile profile, String newName) {
        Profile find = findByName(newName).orElse(null);
        if (find != null && !find.equals(profile)) {
            throw new IllegalArgumentException("Profile name '" + newName + "' already exists");
        } else {
            profile.setName(newName);
        }
    }

    private String findAvailableNameFrom(String name) {
        String src = name.trim();
        if (!findByName(src).isPresent()) {
            return name;
        }
        String append = " - Copy";
        src = src.contains(append) ? src.substring(0, src.indexOf(append)) : src;
        String dest = src + append;
        int count = 2;
        while (findByName(dest).orElse(null) != null) {
            dest = src + " - Copy (" + count + ")";
            count++;
        }
        return dest;
    }

    @Override
    public boolean remove(Profile profile) {
        verifyExists(profile);
        if (_profiles.get().size() == 1) {
            // create a default profile then
            Profile p = duplicateProfile(defProfile);
            _profiles.get().add(p);
            selectProfile(p);
        }
        return _profiles.get().remove(profile);
    }

    @Override
    public void load(Path path) {
        Objects.requireNonNull(path);
        try (Reader reader = Files.newBufferedReader(path, DEFAULT)) {
            JsonProfiles data = gson.fromJson(reader, JsonProfiles.class);
            if (!data.profiles.isEmpty()) {
                _profiles.get().setAll(data.profiles);
                _selected.set(findByName(data.selected).orElse(null));
            }
        } catch (FileNotFoundException e) {
            log.debug("No profiles file found at {}", path); //$NON-NLS-1$
        } catch (JsonSyntaxException | JsonIOException | IOException e) {
            log.debug("Could not properly load profiles file: {}", e.toString()); //$NON-NLS-1$
        }
    }

    @Override
    public void save(Path path) {
        Objects.requireNonNull(path);
        try {
            JsonProfiles data = new JsonProfiles();
            data.selected = _selected.get().getName();
            _profiles.get().forEach(p -> data.profiles.add((AppProfile) p));
            Files.write(path, Arrays.asList(gson.toJson(data)), DEFAULT, WRITE);
        } catch (IOException e) {
            log.debug("Could not save profiles to file: {}", e.toString()); //$NON-NLS-1$
        }
    }

    static class JsonProfiles {
        String selected = "";
        List<AppProfile> profiles = new ArrayList<>();
    }

}
