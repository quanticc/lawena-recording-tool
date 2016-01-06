package com.github.lawena.views.tf2;

import com.github.lawena.config.Constants;
import com.github.lawena.domain.ConfigFlag;
import com.github.lawena.util.ExternalString;
import com.github.lawena.views.tf2.skybox.Skybox;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TF2Properties {

    public static final String RESOURCES_KEY = "tf2.resources";
    public static final String EXCLUDED_KEY = "tf2.excluded";
    public static final List<String> DEFAULT_RESOURCES = Arrays.asList("no_announcer_voices.vpk",
            "no_applause_sounds.vpk", "no_domination_sounds.vpk");

    // @formatter:off
    private ObjectProperty<Integer>        width                 = objectProperty("launch.width", 1280);
    private ObjectProperty<Integer>        height                = objectProperty("launch.height", 720);
    private ObjectProperty<ExternalString> dxlevel               = objectProperty("launch.dxlevel", Constants.DEFAULT_DIRECTX_LEVEL);
    private BooleanProperty                insecure              = booleanProperty("launch.insecure", false);
    private BooleanProperty                defaultLaunch         = booleanProperty("launch.default", false);
    private IntegerProperty                launchTimeout         = integerProperty("launch.timeout", 120);
    private StringProperty                 launchOptions         = stringProperty("launch.custom", "-console -novid");
    private BooleanProperty                noSkipToTickLines     = booleanProperty("vdm.noSkipToTickLines", false);
    private BooleanProperty                deleteUnneededBackups = booleanProperty("backups.deleteUnneeded", true);
    private IntegerProperty                folderSizeLimit       = integerProperty("backups.folderSizeLimit", 120);
    private ObjectProperty<ExternalString> captureMode           = objectProperty("recorder.mode", Constants.DEFAULT_CAPTURE_MODE);
    private ObjectProperty<Integer>        quality               = objectProperty("recorder.quality", 90);
    private ObjectProperty<Integer>        fps                   = objectProperty("recorder.fps", 120);
    private StringProperty                 gamePath              = stringProperty("path.game", "");
    private StringProperty                 framesPath            = stringProperty("path.frames", "");
    private StringProperty                 steamPath             = stringProperty("path.steam", "");
    private ListProperty<ConfigFlag>       flagItems             = listProperty("flag.items");
    private ObjectProperty<ExternalString> hud                   = objectProperty("tf2.hud", Constants.DEFAULT_HUD);
    private ObjectProperty<Skybox>         skybox                = objectProperty("tf2.skybox", Skybox.DEFAULT);
    private ObjectProperty<ExternalString> viewmodelSwitch       = objectProperty("tf2.cfg.viewmodels", Constants.DEFAULT_VIEWMODEL);
    private ObjectProperty<Double>         viewmodelFov          = objectProperty("tf2.cfg.viewmodelFov", 75D);
    private StringProperty                 customSettings        = stringProperty("tf2.cfg.custom", "");
    private ObjectProperty<Dimension2D>    customSettingsDialog  = objectProperty("tf2.customSettingsDialog", new Dimension2D(500.0, 400.0));
    // @formatter:on

    private Map<String, BooleanProperty> propertyMap = new HashMap<>();

    private <T> ObjectProperty<T> objectProperty(String key, T initialValue) {
        return new SimpleObjectProperty<T>(this, key, initialValue);
    }

    private BooleanProperty booleanProperty(String key, boolean initialValue) {
        return new SimpleBooleanProperty(this, key, initialValue);
    }

    private IntegerProperty integerProperty(String key, int initialValue) {
        return new SimpleIntegerProperty(this, key, initialValue);
    }

    private StringProperty stringProperty(String key, String initialValue) {
        return new SimpleStringProperty(this, key, initialValue);
    }

    private <T> ListProperty<T> listProperty(String key) {
        return new SimpleListProperty<>(this, key, FXCollections.observableArrayList());
    }

    public final ObjectProperty<Integer> widthProperty() {
        return this.width;
    }

    public final ObjectProperty<Integer> heightProperty() {
        return this.height;
    }

    public final ObjectProperty<Integer> fpsProperty() {
        return this.fps;
    }

    public final ObjectProperty<ExternalString> dxlevelProperty() {
        return this.dxlevel;
    }

    public final BooleanProperty deleteUnneededBackupsProperty() {
        return this.deleteUnneededBackups;
    }

    public final BooleanProperty noSkipToTickLinesProperty() {
        return this.noSkipToTickLines;
    }

    public final IntegerProperty folderSizeLimitProperty() {
        return this.folderSizeLimit;
    }

    public final ObjectProperty<Integer> qualityProperty() {
        return this.quality;
    }

    public final IntegerProperty launchTimeoutProperty() {
        return this.launchTimeout;
    }

    public final StringProperty launchOptionsProperty() {
        return this.launchOptions;
    }

    public final StringProperty gamePathProperty() {
        return this.gamePath;
    }

    public final StringProperty framesPathProperty() {
        return this.framesPath;
    }

    public final StringProperty steamPathProperty() {
        return this.steamPath;
    }

    public final ObjectProperty<ExternalString> captureModeProperty() {
        return this.captureMode;
    }

    public final ListProperty<ConfigFlag> flagItemsProperty() {
        return flagItems;
    }

    public ObservableList<ConfigFlag> getFlagItems() {
        return flagItems.get();
    }

    public final BooleanProperty insecureProperty() {
        return this.insecure;
    }

    public final BooleanProperty defaultLaunchProperty() {
        return defaultLaunch;
    }

    public BooleanProperty getProperty(String key, boolean defaultValue) {
        return propertyMap.computeIfAbsent(key, k -> new SimpleBooleanProperty(this, k, defaultValue));
    }

    public Map<String, BooleanProperty> getPropertyMap() {
        return propertyMap;
    }

    public final ObjectProperty<ExternalString> hudProperty() {
        return this.hud;
    }

    public final ExternalString getHud() {
        return this.hudProperty().get();
    }

    public final ObjectProperty<Skybox> skyboxProperty() {
        return this.skybox;
    }

    public final Skybox getSkybox() {
        return this.skyboxProperty().get();
    }

    public final void setSkybox(final Skybox skybox) {
        this.skyboxProperty().set(skybox);
    }

    public final ObjectProperty<ExternalString> viewmodelSwitchProperty() {
        return this.viewmodelSwitch;
    }

    public final ObjectProperty<Double> viewmodelFovProperty() {
        return this.viewmodelFov;
    }

    public final StringProperty customSettingsProperty() {
        return this.customSettings;
    }

    public final ObjectProperty<Dimension2D> customSettingsDialogProperty() {
        return customSettingsDialog;
    }
}
