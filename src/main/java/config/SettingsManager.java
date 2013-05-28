
package config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SettingsManager {

    private static final Logger log = Logger.getLogger("lwrt");

    private enum Key {
        Height(720, 360, Integer.MAX_VALUE),
        Width(1280, 640, Integer.MAX_VALUE),
        Framerate(120, 30, Integer.MAX_VALUE),
        ViewmodelSwitch("on", "on", "off", "default"),
        ViewmodelFov(70, 55, 90),
        DxLevel("98", "80", "81", "90", "95", "98"),
        Hud("medic", "killnotices", "medic", "full", "custom"),
        TfDir(""),
        MovieDir(""),
        EnableMotionBlur(true),
        EnableCustomParticles(false),
        DisableCrosshairSwitch(false),
        DisableCrosshair(false),
        DisableCombatText(false),
        DisableAnnouncer(true),
        DisableDomination(true),
        DisableHitsounds(false),
        DisableVoice(false),
        DisableSteamCloud(false);

        private Object value;
        private List<String> valid;
        private int min;
        private int max;
        
        private Key(String value) {
            this.value = value;
        }

        private Key(boolean value) {
            this.value = value;
        }

        private Key(int value, int min, int max) {
            this.value = value;
        }

        private Key(String value, String... validValues) {
            this(value);
            if (validValues != null) {
                this.valid = Arrays.asList(validValues);
            }
        }

        @Override
        public String toString() {
            return name();
        }

        public boolean isValid(String str) {
            return valid == null || valid.contains(str);
        }
    }

    private String filename;
    private Properties properties;

    public SettingsManager(String settingsFile) {
        filename = settingsFile;
        Properties defaults = new Properties();
        for (Key key : Key.values()) {
            defaults.setProperty(key.toString(), key.value + "");
        }
        properties = new Properties(defaults);
        try {
            properties.load(new FileReader(filename));
        } catch (FileNotFoundException e) {
            // do nothing, will load defaults
        } catch (IOException e) {
            log.log(Level.INFO, "Problem while loading settings, reverting to defaults", e);
        }
    }

    public void save() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filename));
            properties.store(pw, "lawena settings");
        } catch (IOException e) {
            log.log(Level.INFO, "Settings could not be saved", e);
        }
    }

    public void saveToCfg() throws IOException {
        PrintWriter settings = new PrintWriter(new FileWriter("cfg\\settings.cfg"));
        int framerate = getFramerate();
        boolean motionblur = getEnableMotionBlur();
        boolean steamcloud = getDisableSteamCloud();
        int viewmodelfov = getViewmodelFov();
        String viewmodelswitch = getViewmodelSwitch();
        boolean crosshairswitch = getDisableCrosshairSwitch();
        boolean crosshair = getDisableCrosshair();
        boolean combattext = getDisableCombattext();
        boolean hitsounds = getDisableHitsounds();
        boolean voice = getDisableVoice();
        settings.println("alias recframerate host_framerate " + framerate);
        if (framerate < 60) {
            settings.println("alias currentfpsup 60fps");
            settings.println("alias currentfpsdn 3840fps");
        } else if (framerate == 60) {
            settings.println("alias currentfpsup 120fps");
            settings.println("alias currentfpsdn 3840fps");
        } else if (framerate < 120) {
            settings.println("alias currentfpsup 120fps");
            settings.println("alias currentfpsdn 60fps");
        } else if (framerate == 120) {
            settings.println("alias currentfpsup 240fps");
            settings.println("alias currentfpsdn 60fps");
        } else if (framerate < 240) {
            settings.println("alias currentfpsup 240fps");
            settings.println("alias currentfpsdn 120fps");
        } else if (framerate == 240) {
            settings.println("alias currentfpsup 480fps");
            settings.println("alias currentfpsdn 120fps");
        } else if (framerate < 480) {
            settings.println("alias currentfpsup 480fps");
            settings.println("alias currentfpsdn 240fps");
        } else if (framerate == 480) {
            settings.println("alias currentfpsup 960fps");
            settings.println("alias currentfpsdn 240fps");
        } else if (framerate < 960) {
            settings.println("alias currentfpsup 960fps");
            settings.println("alias currentfpsdn 480fps");
        } else if (framerate == 960) {
            settings.println("alias currentfpsup 1920fps");
            settings.println("alias currentfpsdn 480fps");
        } else if (framerate < 1920) {
            settings.println("alias currentfpsup 1920fps");
            settings.println("alias currentfpsdn 960fps");
        } else if (framerate == 1920) {
            settings.println("alias currentfpsup 3840fps");
            settings.println("alias currentfpsdn 960fps");
        } else if (framerate < 3840) {
            settings.println("alias currentfpsup 3840fps");
            settings.println("alias currentfpsdn 1920fps");
        } else if (framerate == 3840) {
            settings.println("alias currentfpsup 60fps");
            settings.println("alias currentfpsdn 1920fps");
        } else {
            settings.println("alias currentfpsup 60fps");
            settings.println("alias currentfpsdn 3840fps");
        }

        settings.println("mat_motion_blur_enabled " + (motionblur ? "1" : "0"));
        settings.println("mat_motion_blur_forward_enabled " + (motionblur ? "1" : "0"));
        settings.println("mat_motion_blur_strength " + (motionblur ? "1" : "0"));
        settings.println((steamcloud ? "//" : "") + "cl_cloud_settings 0");
        settings.println("viewmodel_fov_demo " + viewmodelfov);
        settings.println((viewmodelswitch.equals("off") ? "//" : "") + "lockviewmodelson");
        settings.println((viewmodelswitch.equals("on") ? "//" : "") + "lockviewmodelsoff");
        settings.println((crosshairswitch ? "//" : "") + "cl_crosshair_file \"\"");
        settings.println((crosshairswitch ? "//" : "") + "cl_crosshair_red 200");
        settings.println((crosshairswitch ? "//" : "") + "cl_crosshair_green 200");
        settings.println((crosshairswitch ? "//" : "") + "cl_crosshair_blue 200");
        settings.println((crosshairswitch ? "//" : "") + "cl_crosshair_scale 32");
        settings.println((crosshairswitch ? "//" : "") + "cl_crosshairalpha 200");
        settings.println((crosshairswitch ? "//" : "") + "lockcrosshair");
        settings.println("crosshair " + (crosshair ? "1" : "0"));
        settings.println("hud_combattext " + (combattext ? "1" : "0"));
        settings.println("hud_combattext_healing " + (combattext ? "1" : "0"));
        settings.println("tf_dingalingaling " + (hitsounds ? "1" : "0"));
        settings.println("voice_enable " + (voice ? "1" : "0"));
        settings.println("alias voice_enable \"\"");
        settings.println("cl_autorezoom 0");
        settings.println("hud_saytext_time 0");
        settings.println("net_graph 0");
        settings.println("alias net_graph \"\"");
        settings.println("alias voice_menu_1 \"\"");
        settings.println("alias voice_menu_2 \"\"");
        settings.println("alias voice_menu_3 \"\"");
        settings.println("cl_showfps 0");
        settings.println("alias cl_showfps \"\"");
        settings.println("volume 0.5");
        settings.println("hud_fastswitch 1");
        settings.println("cl_hud_minmode 1");
        settings.close();
    }

    private void setString(Key key, String value) {
        if (key.isValid(value)) {
            properties.setProperty(key.toString(), value);
        } else {
            throw new IllegalArgumentException(key + " does not allow value: " + value);
        }
    }

    private String getString(Key key) {
        return properties.getProperty(key.toString());
    }

    private void setInt(Key key, int value) {
        if (value >= key.min && value <= key.max) {
            properties.setProperty(key.toString(), value + "");
        } else {
            throw new IllegalArgumentException(key + " does not allow value: " + value);
        }
    }

    private int getInt(Key key) {
        String value = properties.getProperty(key.toString());
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.info("Invalid numeric format in " + key + ": " + value + ", loading default value");
            return Integer.parseInt(key.value.toString());
        }
    }

    private void setBoolean(Key key, boolean value) {
        properties.setProperty(key.toString(), value + "");
    }

    private boolean getBoolean(Key key) {
        return Boolean.parseBoolean(properties.getProperty(key.toString()));
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setHeight(int value) {
        setInt(Key.Height, value);
    }

    public void setWidth(int value) {
        setInt(Key.Width, value);
    }

    public void setFramerate(int value) {
        setInt(Key.Framerate, value);
    }

    public void setHud(String value) {
        setString(Key.Hud, value);
    }

    public void setViewmodelFov(int value) {
        setInt(Key.ViewmodelFov, value);
    }

    public void setEnableMotionBlur(boolean value) {
        setBoolean(Key.EnableMotionBlur, value);
    }

    public void setViewmodelSwitch(String value) {
        setString(Key.ViewmodelSwitch, value);
    }

    public void setDisableCrosshairSwitch(boolean value) {
        setBoolean(Key.DisableCrosshairSwitch, value);
    }

    public void setDisableCrosshair(boolean value) {
        setBoolean(Key.DisableCrosshair, value);
    }

    public void setDisableCombattext(boolean value) {
        setBoolean(Key.DisableCombatText, value);
    }

    public void setDisableAnnouncer(boolean value) {
        setBoolean(Key.DisableAnnouncer, value);
    }

    public void setDisableDomination(boolean value) {
        setBoolean(Key.DisableDomination, value);
    }

    public void setDisableHitsounds(boolean value) {
        setBoolean(Key.DisableHitsounds, value);
    }

    public void setDisableVoice(boolean value) {
        setBoolean(Key.DisableVoice, value);
    }

    public void setDisableSteamCloud(boolean value) {
        setBoolean(Key.DisableSteamCloud, value);
    }

    public void setDxlevel(String value) {
        setString(Key.DxLevel, value);
    }

    public void setTfDir(String value) {
        setString(Key.TfDir, value);
    }

    public void setMovieDir(String value) {
        setString(Key.MovieDir, value);
    }

    public int getHeight() {
        return getInt(Key.Height);
    }

    public int getWidth() {
        return getInt(Key.Width);
    }

    public int getFramerate() {
        return getInt(Key.Framerate);
    }

    public String getHud() {
        return getString(Key.Hud);
    }

    public int getViewmodelFov() {
        return getInt(Key.ViewmodelFov);
    }

    public boolean getEnableMotionBlur() {
        return getBoolean(Key.EnableMotionBlur);
    }

    public String getViewmodelSwitch() {
        return getString(Key.ViewmodelSwitch);
    }

    public boolean getDisableCrosshairSwitch() {
        return getBoolean(Key.DisableCrosshairSwitch);
    }

    public boolean getDisableCrosshair() {
        return getBoolean(Key.DisableCrosshair);
    }

    public boolean getDisableCombattext() {
        return getBoolean(Key.DisableCombatText);
    }

    public boolean getDisableAnnouncer() {
        return getBoolean(Key.DisableAnnouncer);
    }

    public boolean getDisableDomination() {
        return getBoolean(Key.DisableDomination);
    }

    public boolean getDisableHitsounds() {
        return getBoolean(Key.DisableHitsounds);
    }

    public boolean getDisableVoice() {
        return getBoolean(Key.DisableVoice);
    }

    public boolean getDisableSteamCloud() {
        return getBoolean(Key.DisableSteamCloud);
    }

    public String getDxlevel() {
        return getString(Key.DxLevel);
    }

    public String getTfDir() {
        String value = getString(Key.TfDir);
        return (value == null ? "" : value);
    }

    public String getMovieDir() {
        String value = getString(Key.MovieDir);
        return (value == null ? "" : value);
    }
}
