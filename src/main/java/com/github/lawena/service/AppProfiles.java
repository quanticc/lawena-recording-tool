package com.github.lawena.service;

import com.github.lawena.domain.AppProfile;
import com.github.lawena.domain.Launcher;
import com.github.lawena.domain.Profile;
import com.github.lawena.util.LwrtUtils;
import com.github.lawena.views.GamePresenter;
import com.github.lawena.views.GameView;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Service
public class AppProfiles implements Profiles {

    private static final String DEFAULT_LAUNCHER_NAME = "Team Fortress 2";
    private static final Launcher DEFAULT_LAUNCHER = createDefaultLauncher();
    private static final Profile DEFAULT_PROFILE = new AppProfile(DEFAULT_LAUNCHER_NAME, "Default");

    private static Launcher createDefaultLauncher() {
        Launcher launcher = new Launcher(DEFAULT_LAUNCHER_NAME);
        // basePath required to load resources
        launcher.setBasePath("tf2");
        // viewName required to load UI
        launcher.setViewName("tf2");
        return launcher;
    }

    private final ObjectProperty<Profile> selected = new SimpleObjectProperty<>(this, "selected", DEFAULT_PROFILE);
    private final ListProperty<Profile> profiles = new SimpleListProperty<>(this, "profiles",
            FXCollections.checkedObservableList(FXCollections.observableArrayList(DEFAULT_PROFILE), Profile.class));
    private final ListProperty<Launcher> launchers = new SimpleListProperty<>(this, "launchers",
            FXCollections.checkedObservableList(FXCollections.observableArrayList(DEFAULT_LAUNCHER), Launcher.class));
    private final List<GameView> gameViews; // injected

    @Autowired
    public AppProfiles(List<GameView> gameViews) {
        this.gameViews = gameViews;
    }

    @PostConstruct
    private void init() {
        selected.isNull().addListener((obs, old, value) -> {
            if (value) {
                selected.set(DEFAULT_PROFILE);
                if (!profiles.get().contains(DEFAULT_PROFILE))
                    profiles.get().add(DEFAULT_PROFILE);
            }
        });
    }

    @Override
    public final ObjectProperty<Profile> selectedProperty() {
        return selected;
    }

    @Override
    public final Profile getSelected() {
        return selected.get();
    }

    @Override
    public ListProperty<Profile> profilesProperty() {
        return profiles;
    }

    @Override
    public Optional<Profile> findByName(String name) {
        Objects.requireNonNull(name);
        return profiles.get().stream().filter(p -> p.getName().equals(name)).findFirst();
    }

    @Override
    public boolean containsByName(String name) {
        Objects.requireNonNull(name);
        return findByName(name).isPresent();
    }

    private void verifyExists(Profile profile) {
        Objects.requireNonNull(profile);
        if (!profiles.get().contains(profile))
            throw new IllegalArgumentException("The profile must be already stored");
    }

    private boolean selectProfile(Profile newSelectedProfile) {
        verifyExists(newSelectedProfile);
        selected.set(newSelectedProfile);
        return true;
    }

    @Override
    public Profile create(String appId, String name) {
        Objects.requireNonNull(appId);
        Objects.requireNonNull(name);
        Profile p = createNewProfile(name);
        p.setLauncher(appId);
        profiles.get().add(p);
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
        profiles.get().add(p);
        selectProfile(p);
    }

    @Override
    public void duplicate(Profile source, String newName) {
        verifyExists(source);
        Objects.requireNonNull(newName);
        Profile p = duplicateProfile(source, newName);
        profiles.get().add(p);
        selectProfile(p);
    }

    private Profile duplicateProfile(Profile baseProfile) {
        AppProfile newProfile = new AppProfile();
        AppProfile.copy((AppProfile) baseProfile, newProfile);
        renameProfile(newProfile, LwrtUtils.findAvailableNameFrom(baseProfile.getName(), this::findByName));
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

    @Override
    public boolean remove(Profile profile) {
        verifyExists(profile);
        if (profiles.get().size() == 1) {
            Profile p = duplicateProfile(DEFAULT_PROFILE);
            profiles.get().add(p);
            selectProfile(p);
        }
        return profiles.get().remove(profile);
    }

    /////////////////////////////////////

    @Override
    public final ListProperty<Launcher> launchersProperty() {
        return launchers;
    }

    @Override
    public final ObservableList<Launcher> getLaunchers() {
        return launchers.get();
    }

    @Override
    public Optional<Launcher> getLauncher(Profile profile) {
        String name = profile.getLauncher();
        return findLauncherByName(name);
    }

    @Override
    public Optional<Launcher> findLauncherByName(String launcherName) {
        return launchers.get().stream()
                .filter(l -> launcherName.equals(l.getName()))
                .findAny();
    }

    @Override
    public GameView getView(Launcher launcher) {
        GameView view = gameViews.stream()
                .filter(v -> v.getName().equals(launcher.getViewName()))
                .findAny()
                .orElseThrow(() -> new NoSuchElementException("No view defined with name " + launcher.getViewName()));
        view.getView();
        return view;
    }

    @Override
    public GamePresenter getPresenter(Launcher launcher) {
        return getView(launcher).getPresenter();
    }

}
