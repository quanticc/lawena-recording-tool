package com.github.lawena.tf2;

import com.github.lawena.game.Group;
import com.github.lawena.tf2.skybox.Skybox;
import com.github.lawena.util.ExternalString;

import java.util.Arrays;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

@SuppressWarnings("nls")
public class Properties {

    public static final String RESOURCES_KEY = "tf2.resources";
    public static final List<String> DEFAULT_RESOURCES = Arrays.asList("no_announcer_voices.vpk",
            "no_applause_sounds.vpk", "no_domination_sounds.vpk");
    private ObjectProperty<Integer> width = new SimpleObjectProperty<>(this, "launch.width", 1280);
    private ObjectProperty<Integer> height = new SimpleObjectProperty<>(this, "launch.height", 720);
    private ObjectProperty<Integer> fps = new SimpleObjectProperty<>(this, "recorder.fps", 120);
    private ObjectProperty<ExternalString> dxlevel = new SimpleObjectProperty<>(this,
            "launch.dxlevel", ext("DxLevel80"));
    private ObjectProperty<ExternalString> hud = new SimpleObjectProperty<>(this, "tf2.hud",
            ext("HudMinimal"));
    private ObjectProperty<Skybox> skybox = new SimpleObjectProperty<>(this, "tf2.skybox",
            Skybox.DEFAULT);
    private ObjectProperty<ExternalString> viewmodelSwitch = new SimpleObjectProperty<>(this,
            "tf2.cfg.viewmodels", ext("VmSwitchOn"));
    private ObjectProperty<Double> viewmodelFov = new SimpleObjectProperty<>(this,
            "tf2.cfg.viewmodelFov", 75D);
    private BooleanProperty motionBlur = new SimpleBooleanProperty(this, "tf2.cfg.motionBlur", false);
    private BooleanProperty pixelfog = new SimpleBooleanProperty(this, "tf2.cfg.pixelfog", false);
    private BooleanProperty noCrosshair = new SimpleBooleanProperty(this, "tf2.cfg.noCrosshair",
            false);
    private BooleanProperty noCrosshairSwitch = new SimpleBooleanProperty(this,
            "tf2.cfg.noCrosshairSwitch", false);
    private BooleanProperty noHitsounds = new SimpleBooleanProperty(this, "tf2.cfg.noHitsounds",
            false);
    private BooleanProperty noVoice = new SimpleBooleanProperty(this, "tf2.cfg.noVoice", false);
    private BooleanProperty noDamageNumbers = new SimpleBooleanProperty(this,
            "tf2.cfg.noDamageNumbers", false);
    private BooleanProperty hudMinmode = new SimpleBooleanProperty(this, "tf2.cfg.hudMinmode", false);
    private BooleanProperty hudPlayerModel = new SimpleBooleanProperty(this,
            "tf2.cfg.hudPlayerModel", false);
    private BooleanProperty deleteUnneededBackups = new SimpleBooleanProperty(this,
            "backups.deleteUnneeded", true);
    private BooleanProperty noSkipToTickLines = new SimpleBooleanProperty(this,
            "vdm.noSkipToTickLines", false);
    private IntegerProperty folderSizeLimit = new SimpleIntegerProperty(this,
            "backups.folderSizeLimit", 120);
    private ObjectProperty<Integer> quality =
            new SimpleObjectProperty<>(this, "recorder.quality", 90);
    private IntegerProperty launchTimeout = new SimpleIntegerProperty(this, "launch.timeout", 120);
    private StringProperty launchOptions = new SimpleStringProperty(this, "launch.custom",
            "-console -novid");
    private ObjectProperty<ExternalString> captureMode = new SimpleObjectProperty<>(this,
            "recorder.mode", ext("SourceRecorderTGA"));
    private StringProperty videoFormat = new SimpleStringProperty(this, "recorder.video", "tga");
    private StringProperty audioFormat = new SimpleStringProperty(this, "recorder.audio", "wav");
    private StringProperty customSettings = new SimpleStringProperty(this, "tf2.cfg.custom", "");
    private StringProperty gamePath = new SimpleStringProperty(this, "path.game", "");
    private StringProperty framesPath = new SimpleStringProperty(this, "path.frames", "");
    private StringProperty steamPath = new SimpleStringProperty(this, "path.steam", "");
    private ListProperty<Group> groupItems = new SimpleListProperty<>(this, "tf2.group.items",
            FXCollections.observableArrayList());

    private static ExternalString ext(String key) {
        return new ExternalString(key, Messages::getString);
    }

    public final ObjectProperty<Integer> widthProperty() {
        return this.width;
    }

    public final int getWidth() {
        return this.widthProperty().get();
    }

    public final void setWidth(final int width) {
        this.widthProperty().set(width);
    }

    public final ObjectProperty<Integer> heightProperty() {
        return this.height;
    }

    public final int getHeight() {
        return this.heightProperty().get();
    }

    public final void setHeight(final int height) {
        this.heightProperty().set(height);
    }

    public final ObjectProperty<Integer> fpsProperty() {
        return this.fps;
    }

    public final int getFps() {
        return this.fpsProperty().get();
    }

    public final void setFps(final int fps) {
        this.fpsProperty().set(fps);
    }

    public final ObjectProperty<ExternalString> dxlevelProperty() {
        return this.dxlevel;
    }

    public final ExternalString getDxlevel() {
        return this.dxlevelProperty().get();
    }

    public final void setDxlevel(final ExternalString dxlevel) {
        this.dxlevelProperty().set(dxlevel);
    }

    public final ObjectProperty<ExternalString> hudProperty() {
        return this.hud;
    }

    public final ExternalString getHud() {
        return this.hudProperty().get();
    }

    public final void setHud(final ExternalString hud) {
        this.hudProperty().set(hud);
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

    public final ExternalString getViewmodelSwitch() {
        return this.viewmodelSwitchProperty().get();
    }

    public final void setViewmodelSwitch(final ExternalString viewmodelSwitch) {
        this.viewmodelSwitchProperty().set(viewmodelSwitch);
    }

    public final ObjectProperty<Double> viewmodelFovProperty() {
        return this.viewmodelFov;
    }

    public final Double getViewmodelFov() {
        return this.viewmodelFovProperty().get();
    }

    public final void setViewmodelFov(final Double viewmodelFov) {
        this.viewmodelFovProperty().set(viewmodelFov);
    }

    public final BooleanProperty motionBlurProperty() {
        return this.motionBlur;
    }

    public final boolean isMotionBlur() {
        return this.motionBlurProperty().get();
    }

    public final void setMotionBlur(final boolean motionBlur) {
        this.motionBlurProperty().set(motionBlur);
    }

    public final BooleanProperty pixelfogProperty() {
        return this.pixelfog;
    }

    public final boolean isPixelfog() {
        return this.pixelfogProperty().get();
    }

    public final void setPixelfog(final boolean pixelfog) {
        this.pixelfogProperty().set(pixelfog);
    }

    public final BooleanProperty noCrosshairProperty() {
        return this.noCrosshair;
    }

    public final boolean isNoCrosshair() {
        return this.noCrosshairProperty().get();
    }

    public final void setNoCrosshair(final boolean noCrosshair) {
        this.noCrosshairProperty().set(noCrosshair);
    }

    public final BooleanProperty noCrosshairSwitchProperty() {
        return this.noCrosshairSwitch;
    }

    public final boolean isNoCrosshairSwitch() {
        return this.noCrosshairSwitchProperty().get();
    }

    public final void setNoCrosshairSwitch(final boolean noCrosshairSwitch) {
        this.noCrosshairSwitchProperty().set(noCrosshairSwitch);
    }

    public final BooleanProperty noHitsoundsProperty() {
        return this.noHitsounds;
    }

    public final boolean isNoHitsounds() {
        return this.noHitsoundsProperty().get();
    }

    public final void setNoHitsounds(final boolean noHitsounds) {
        this.noHitsoundsProperty().set(noHitsounds);
    }

    public final BooleanProperty noVoiceProperty() {
        return this.noVoice;
    }

    public final boolean isNoVoice() {
        return this.noVoiceProperty().get();
    }

    public final void setNoVoice(final boolean noVoice) {
        this.noVoiceProperty().set(noVoice);
    }

    public final BooleanProperty hudMinmodeProperty() {
        return this.hudMinmode;
    }

    public final boolean isHudMinmode() {
        return this.hudMinmodeProperty().get();
    }

    public final void setHudMinmode(final boolean hudMinmode) {
        this.hudMinmodeProperty().set(hudMinmode);
    }

    public final BooleanProperty hudPlayerModelProperty() {
        return this.hudPlayerModel;
    }

    public final boolean isHudPlayerModel() {
        return this.hudPlayerModelProperty().get();
    }

    public final void setHudPlayerModel(final boolean hudPlayerModel) {
        this.hudPlayerModelProperty().set(hudPlayerModel);
    }

    public final BooleanProperty deleteUnneededBackupsProperty() {
        return this.deleteUnneededBackups;
    }

    public final boolean isDeleteUnneededBackups() {
        return this.deleteUnneededBackupsProperty().get();
    }

    public final void setDeleteUnneededBackups(final boolean deleteUnneededBackups) {
        this.deleteUnneededBackupsProperty().set(deleteUnneededBackups);
    }

    public final BooleanProperty noSkipToTickLinesProperty() {
        return this.noSkipToTickLines;
    }

    public final boolean isNoSkipToTickLines() {
        return this.noSkipToTickLinesProperty().get();
    }

    public final void setNoSkipToTickLines(final boolean noSkipToTickLines) {
        this.noSkipToTickLinesProperty().set(noSkipToTickLines);
    }

    public final IntegerProperty folderSizeLimitProperty() {
        return this.folderSizeLimit;
    }

    public final int getFolderSizeLimit() {
        return this.folderSizeLimitProperty().get();
    }

    public final void setFolderSizeLimit(final int folderSizeLimit) {
        this.folderSizeLimitProperty().set(folderSizeLimit);
    }

    public final ObjectProperty<Integer> qualityProperty() {
        return this.quality;
    }

    public final int getQuality() {
        return this.qualityProperty().get();
    }

    public final void setQuality(final int quality) {
        this.qualityProperty().set(quality);
    }

    public final IntegerProperty launchTimeoutProperty() {
        return this.launchTimeout;
    }

    public final int getLaunchTimeout() {
        return this.launchTimeoutProperty().get();
    }

    public final void setLaunchTimeout(final int launchTimeout) {
        this.launchTimeoutProperty().set(launchTimeout);
    }

    public final StringProperty launchOptionsProperty() {
        return this.launchOptions;
    }

    public final java.lang.String getLaunchOptions() {
        return this.launchOptionsProperty().get();
    }

    public final void setLaunchOptions(final java.lang.String launchOptions) {
        this.launchOptionsProperty().set(launchOptions);
    }

    public final StringProperty videoFormatProperty() {
        return this.videoFormat;
    }

    public final java.lang.String getVideoFormat() {
        return this.videoFormatProperty().get();
    }

    public final void setVideoFormat(final java.lang.String videoFormat) {
        this.videoFormatProperty().set(videoFormat);
    }

    public final StringProperty audioFormatProperty() {
        return this.audioFormat;
    }

    public final java.lang.String getAudioFormat() {
        return this.audioFormatProperty().get();
    }

    public final void setAudioFormat(final java.lang.String audioFormat) {
        this.audioFormatProperty().set(audioFormat);
    }

    public final StringProperty customSettingsProperty() {
        return this.customSettings;
    }

    public final java.lang.String getCustomSettings() {
        return this.customSettingsProperty().get();
    }

    public final void setCustomSettings(final java.lang.String customSettings) {
        this.customSettingsProperty().set(customSettings);
    }

    public final StringProperty gamePathProperty() {
        return this.gamePath;
    }

    public final java.lang.String getGamePath() {
        return this.gamePathProperty().get();
    }

    public final void setGamePath(final java.lang.String gamePath) {
        this.gamePathProperty().set(gamePath);
    }

    public final StringProperty framesPathProperty() {
        return this.framesPath;
    }

    public final java.lang.String getFramesPath() {
        return this.framesPathProperty().get();
    }

    public final void setFramesPath(final java.lang.String framesPath) {
        this.framesPathProperty().set(framesPath);
    }

    public final StringProperty steamPathProperty() {
        return this.steamPath;
    }

    public final java.lang.String getSteamPath() {
        return this.steamPathProperty().get();
    }

    public final void setSteamPath(final java.lang.String steamPath) {
        this.steamPathProperty().set(steamPath);
    }

    public final ObjectProperty<ExternalString> captureModeProperty() {
        return this.captureMode;
    }

    public final ExternalString getCaptureMode() {
        return this.captureModeProperty().get();
    }

    public final void setCaptureMode(final ExternalString captureMode) {
        this.captureModeProperty().set(captureMode);
    }

    public final ListProperty<Group> groupItemsProperty() {
        return this.groupItems;
    }

    public final javafx.collections.ObservableList<com.github.lawena.game.Group> getGroupItems() {
        return this.groupItemsProperty().get();
    }

    public final void setGroupItems(
            final javafx.collections.ObservableList<com.github.lawena.game.Group> groupItems) {
        this.groupItemsProperty().set(groupItems);
    }

    public final BooleanProperty noDamageNumbersProperty() {
        return this.noDamageNumbers;
    }

    public final boolean isNoDamageNumbers() {
        return this.noDamageNumbersProperty().get();
    }

    public final void setNoDamageNumbers(final boolean noDamageNumbers) {
        this.noDamageNumbersProperty().set(noDamageNumbers);
    }

}
