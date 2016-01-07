package com.github.lawena.views.launchers;

import com.github.lawena.config.Constants;
import com.github.lawena.domain.Launcher;
import com.github.lawena.util.ExternalString;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FxLauncher {

    public static FxLauncher launcherToFxLauncher(Launcher from) {
        return new FxLauncher(from).load();
    }

    public static Launcher fxLauncherToLauncher(FxLauncher from) {
        return from.save();
    }

    public static FxLauncher duplicate(FxLauncher launcher) {
        FxLauncher fxLauncher = new FxLauncher();
        fxLauncher.setName("L-" + System.currentTimeMillis());
        fxLauncher.setIcon(launcher.getIcon());
        fxLauncher.setGamePath(launcher.getGamePath());
        fxLauncher.setLaunchMode(launcher.getLaunchMode());
        fxLauncher.setModName(launcher.getModName());
        fxLauncher.setAppId(launcher.getAppId());
        fxLauncher.setSteamPath(launcher.getSteamPath());
        fxLauncher.setGameExecutable(launcher.getGameExecutable());
        fxLauncher.setGameProcess(launcher.getGameProcess());
        fxLauncher.setBasePath(launcher.getBasePath());
        fxLauncher.setViewName(launcher.getViewName());
        fxLauncher.setIncludeGamePath(launcher.getIncludeGamePath());
        launcher.getResourceFolders().stream().forEach(fxLauncher.resourceFolders::add);
        launcher.getFlags().stream().map(FxConfigFlag::duplicate).forEach(fxLauncher.flags::add);
        return fxLauncher;
    }

    private final Launcher launcher;
    private final StringProperty name = emptyStringProperty();
    private final StringProperty icon = emptyStringProperty();
    private final StringProperty gamePath = emptyStringProperty();
    private final ObjectProperty<ExternalString> launchMode = objectProperty(Constants.HL2_LAUNCH_MODE);
    private final StringProperty modName = emptyStringProperty();
    private final StringProperty appId = emptyStringProperty();
    private final StringProperty steamPath = emptyStringProperty();
    private final StringProperty gameExecutable = emptyStringProperty();
    private final StringProperty gameProcess = emptyStringProperty();
    private final StringProperty basePath = emptyStringProperty();
    private final StringProperty viewName = emptyStringProperty();
    private final BooleanProperty includeGamePath = new SimpleBooleanProperty(false);
    private final ListProperty<String> resourceFolders = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<FxConfigFlag> flags = new SimpleListProperty<>(FXCollections.observableArrayList());

    public FxLauncher() {
        this(new Launcher());
    }

    private FxLauncher(Launcher from) {
        this.launcher = from;
    }

    private FxLauncher load() {
        name.set(launcher.getName());
        icon.set(launcher.getIcon());
        gamePath.set(launcher.getGamePath());
        launchMode.set(findFromList(Constants.LAUNCH_MODES, launcher.getLaunchMode().name()));
        modName.set(launcher.getModName());
        appId.set(launcher.getAppId());
        steamPath.set(launcher.getSteamPath());
        gameExecutable.set(launcher.getGameExecutable().get());
        gameProcess.set(launcher.getGameProcess().get());
        basePath.set(launcher.getBasePath());
        viewName.set(launcher.getViewName());
        includeGamePath.set(launcher.getResourceFolders().contains(launcher.getGamePath()));
        launcher.getResourceFolders().stream().filter(s -> !s.equals(launcher.getGamePath())).forEach(resourceFolders::add);
        launcher.getFlags().stream().map(FxConfigFlag::configFlagToFxConfigFlag).forEach(flags::add);
        return this;
    }

    private Launcher save() {
        launcher.setName(name.get());
        launcher.setIcon(icon.get());
        launcher.setGamePath(gamePath.get());
        launcher.setLaunchMode(Launcher.Mode.valueOf(Launcher.Mode.class, launchMode.get().getKey()));
        launcher.setModName(modName.get());
        launcher.setAppId(appId.get());
        launcher.setSteamPath(steamPath.get());
        launcher.getGameExecutable().set(gameExecutable.get());
        launcher.getGameProcess().set(gameProcess.get());
        launcher.setBasePath(basePath.get());
        launcher.setViewName(viewName.get());
        List<String> list = new ArrayList<>();
        list.addAll(resourceFolders.get());
        if (includeGamePath.get()) {
            list.add(gamePath.get());
        }
        launcher.setResourceFolders(list);
        launcher.setFlags(flags.stream().map(FxConfigFlag::fxConfigFlagToConfigFlag).collect(Collectors.toList()));
        return launcher;
    }

    private static ExternalString findFromList(List<ExternalString> list, String name) {
        return list.stream().filter(x -> x.getKey().equals(name)).findAny().get();
    }

    private StringProperty emptyStringProperty() {
        return new SimpleStringProperty("");
    }

    private <T> ObjectProperty<T> objectProperty(T initialValue) {
        return new SimpleObjectProperty<T>(initialValue);
    }

    public Launcher getLauncher() {
        return launcher;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty iconProperty() {
        return icon;
    }

    public String getIcon() {
        return icon.get();
    }

    public void setIcon(String icon) {
        this.icon.set(icon);
    }

    public StringProperty gamePathProperty() {
        return gamePath;
    }

    public String getGamePath() {
        return gamePath.get();
    }

    public void setGamePath(String gamePath) {
        this.gamePath.set(gamePath);
    }

    public ObjectProperty<ExternalString> launchModeProperty() {
        return launchMode;
    }

    public ExternalString getLaunchMode() {
        return launchMode.get();
    }

    public void setLaunchMode(ExternalString launchMode) {
        this.launchMode.set(launchMode);
    }

    public StringProperty modNameProperty() {
        return modName;
    }

    public String getModName() {
        return modName.get();
    }

    public void setModName(String modName) {
        this.modName.set(modName);
    }

    public StringProperty appIdProperty() {
        return appId;
    }

    public String getAppId() {
        return appId.get();
    }

    public void setAppId(String appId) {
        this.appId.set(appId);
    }

    public StringProperty steamPathProperty() {
        return steamPath;
    }

    public String getSteamPath() {
        return steamPath.get();
    }

    public void setSteamPath(String steamPath) {
        this.steamPath.set(steamPath);
    }

    public StringProperty gameExecutableProperty() {
        return gameExecutable;
    }

    public String getGameExecutable() {
        return gameExecutable.get();
    }

    public void setGameExecutable(String gameExecutable) {
        this.gameExecutable.set(gameExecutable);
    }

    public StringProperty gameProcessProperty() {
        return gameProcess;
    }

    public String getGameProcess() {
        return gameProcess.get();
    }

    public void setGameProcess(String gameProcess) {
        this.gameProcess.set(gameProcess);
    }

    public StringProperty basePathProperty() {
        return basePath;
    }

    public String getBasePath() {
        return basePath.get();
    }

    public void setBasePath(String basePath) {
        this.basePath.set(basePath);
    }

    public StringProperty viewNameProperty() {
        return viewName;
    }

    public String getViewName() {
        return viewName.get();
    }

    public void setViewName(String viewName) {
        this.viewName.set(viewName);
    }

    public BooleanProperty includeGamePathProperty() {
        return includeGamePath;
    }

    public boolean getIncludeGamePath() {
        return includeGamePath.get();
    }

    public void setIncludeGamePath(boolean includeGamePath) {
        this.includeGamePath.set(includeGamePath);
    }

    public ListProperty<String> resourceFoldersProperty() {
        return resourceFolders;
    }

    public ObservableList<String> getResourceFolders() {
        return resourceFolders.get();
    }

    public void setResourceFolders(ObservableList<String> resourceFolders) {
        this.resourceFolders.set(resourceFolders);
    }

    public ListProperty<FxConfigFlag> flagsProperty() {
        return flags;
    }

    public ObservableList<FxConfigFlag> getFlags() {
        return flags.get();
    }

    public void setFlags(ObservableList<FxConfigFlag> flags) {
        this.flags.set(flags);
    }

    @Override
    public String toString() {
        return name.get();
    }
}
