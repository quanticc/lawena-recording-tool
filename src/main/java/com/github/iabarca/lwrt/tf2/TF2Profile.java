
package com.github.iabarca.lwrt.tf2;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.iabarca.lwrt.custom.CustomContent;
import com.github.iabarca.lwrt.profile.Profile;
import com.github.iabarca.lwrt.profile.Resolution;

public class TF2Profile implements Profile {

    private static final Logger log = Logger.getLogger("lawena");
    
    /* 
     * Core settings
     * - Profile name
     * - Game path
     * - Movie path
     * 
     * Launch settings
     * - Resolution
     * - DXLevel
     * 
     * Recording Settings
     * - Frame Rate
     * 
     * Game Settings
     * 
     * 
     */

    private String name = "";
    private String gamePath = "";
    private String moviePath = "";
    private Resolution resolution = new Resolution(1280, 720);
    private String dxlevel = "98";
    private int fps = 120;
    private String hud = "killnotices";
    private String skybox = "Default";
    // TODO Load default content from CustomFilesManager
    private List<CustomContent> customContent = new ArrayList<>();
    private String viewmodelSwitch = "on";
    private int viewmodelFov = 70;
    private boolean motionBlur = false;
    private boolean crosshair = false;
    private boolean crosshairSwitch = false;
    private boolean damageNumbers = false;
    private boolean hitsounds = false;
    private boolean voice = false;
    private boolean steamCloud = false;
    private boolean hudMinmode = false;
    private String extraLaunchOptions = "";
    // TODO Remove in favor of CustomContent
    private List<String> particles = Collections.emptyList();
    private String consoleLogLevel = "FINER";
    private String fileLogLevel = "FINE";
    private String uiLogLevel = "FINE";
    private int launchTimeoutSeconds = 120;
    private boolean loadKillstreaks = false;
    private boolean removeSkipToTickVdmLines = false;
    private List<String> customLaunchOptions = Collections.emptyList();

    public TF2Profile() {

    }

    public TF2Profile(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Path getGamePath() {
        return Paths.get(gamePath);
    }

    @Override
    public void setGamePath(String gamePath) {
        this.gamePath = gamePath;
    }

    @Override
    public Path getMoviePath() {
        return Paths.get(moviePath);
    }

    @Override
    public void setMoviePath(String moviePath) {
        this.moviePath = moviePath;
    }

    @Override
    public Resolution getResolution() {
        return resolution;
    }

    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    @Override
    public String getDxlevel() {
        return dxlevel;
    }

    public void setDxlevel(String dxlevel) {
        this.dxlevel = dxlevel;
    }

    public String getHud() {
        return hud;
    }

    public void setHud(String hud) {
        this.hud = hud;
    }

    public String getSkybox() {
        return skybox;
    }

    public void setSkybox(String skybox) {
        this.skybox = skybox;
    }

    @Override
    public List<CustomContent> getCustomContent() {
        return customContent;
    }

    public void setCustomContent(List<CustomContent> customFiles) {
        this.customContent = customFiles;
    }

    public String getViewmodelSwitch() {
        return viewmodelSwitch;
    }

    public void setViewmodelSwitch(String viewmodelSwitch) {
        this.viewmodelSwitch = viewmodelSwitch;
    }

    public int getViewmodelFov() {
        return viewmodelFov;
    }

    public void setViewmodelFov(int viewmodelFov) {
        this.viewmodelFov = viewmodelFov;
    }

    public boolean isMotionBlur() {
        return motionBlur;
    }

    public void setMotionBlur(boolean motionBlur) {
        this.motionBlur = motionBlur;
    }

    public boolean isCrosshair() {
        return crosshair;
    }

    public void setCrosshair(boolean crosshair) {
        this.crosshair = crosshair;
    }

    public boolean isCrosshairSwitch() {
        return crosshairSwitch;
    }

    public void setCrosshairSwitch(boolean crosshairSwitch) {
        this.crosshairSwitch = crosshairSwitch;
    }

    public boolean isDamageNumbers() {
        return damageNumbers;
    }

    public void setDamageNumbers(boolean damageNumbers) {
        this.damageNumbers = damageNumbers;
    }

    public boolean isHitsounds() {
        return hitsounds;
    }

    public void setHitsounds(boolean hitsounds) {
        this.hitsounds = hitsounds;
    }

    public boolean isVoice() {
        return voice;
    }

    public void setVoice(boolean voice) {
        this.voice = voice;
    }

    public boolean isSteamCloud() {
        return steamCloud;
    }

    public void setSteamCloud(boolean steamCloud) {
        this.steamCloud = steamCloud;
    }

    public boolean isHudMinmode() {
        return hudMinmode;
    }

    public void setHudMinmode(boolean hudMinmode) {
        this.hudMinmode = hudMinmode;
    }

    public String getExtraLaunchOptions() {
        return extraLaunchOptions;
    }

    public void setExtraLaunchOptions(String extraLaunchOptions) {
        this.extraLaunchOptions = extraLaunchOptions;
    }

    public List<String> getParticles() {
        return particles;
    }

    public void setParticles(List<String> particles) {
        this.particles = particles;
    }

    public String getConsoleLogLevel() {
        return consoleLogLevel;
    }

    public void setConsoleLogLevel(String consoleLogLevel) {
        this.consoleLogLevel = consoleLogLevel;
    }

    public String getFileLogLevel() {
        return fileLogLevel;
    }

    public void setFileLogLevel(String fileLogLevel) {
        this.fileLogLevel = fileLogLevel;
    }

    public String getUiLogLevel() {
        return uiLogLevel;
    }

    public void setUiLogLevel(String uiLogLevel) {
        this.uiLogLevel = uiLogLevel;
    }

    public int getLaunchTimeoutSeconds() {
        return launchTimeoutSeconds;
    }

    public void setLaunchTimeoutSeconds(int launchTimeoutSeconds) {
        this.launchTimeoutSeconds = launchTimeoutSeconds;
    }

    public boolean isLoadKillstreaks() {
        return loadKillstreaks;
    }

    public void setLoadKillstreaks(boolean loadKillstreaks) {
        this.loadKillstreaks = loadKillstreaks;
    }

    public boolean isRemoveSkipToTickVdmLines() {
        return removeSkipToTickVdmLines;
    }

    public void setRemoveSkipToTickVdmLines(boolean removeSkipToTickVdmLines) {
        this.removeSkipToTickVdmLines = removeSkipToTickVdmLines;
    }

    @Override
    public Collection<? extends String> getDefaultLaunchOptions() {
        return Arrays.asList("-applaunch", "440", "-dxlevel", dxlevel, "-novid", "-noborder",
                "-noforcedmparms", "-noforcemaccel", "-noforcemspd", "-console", "-high",
                "-nojoy", "-sw", "-w", resolution.getWidth() + "", "-h", resolution.getHeight()
                        + "");
    }

    @Override
    public Collection<? extends String> getCustomLaunchOptions() {
        return customLaunchOptions;
    }

    public void setCustomLaunchOptions(List<String> customLaunchOptions) {
        this.customLaunchOptions = customLaunchOptions;
    }

    private Field getField(String key) {
        try {
            Field field = this.getClass().getField(key);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException | SecurityException e) {
            log.log(Level.WARNING, "Could not retrieve " + key + " from profile", e);
        }
        return null;
    }

    @Override
    public String getString(String key) {
        Field field = getField(key);
        if (field != null) {
            try {
                Object value = field.get(this);
                if (value != null) {
                    return value.toString();
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                log.log(Level.WARNING, "Could not retrieve " + key + " from profile", e);
            }
        }
        return null;
    }

    @Override
    public boolean getBoolean(String key) {
        Field field = getField(key);
        if (field != null) {
            try {
                return field.getBoolean(this);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                log.log(Level.WARNING, "Could not retrieve " + key + " from profile", e);
            }
        }
        return false;
    }

    @Override
    public void setString(String key, String value) {
        Field field = getField(key);
        if (field != null) {
            try {
                field.set(this, value);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                log.log(Level.WARNING, "Could not retrieve " + key + " from profile", e);
            }
        }
    }

    @Override
    public void setBoolean(String key, boolean value) {
        Field field = getField(key);
        if (field != null) {
            try {
                field.setBoolean(this, value);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                log.log(Level.WARNING, "Could not retrieve " + key + " from profile", e);
            }
        }
    }

}
