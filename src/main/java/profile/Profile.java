
package profile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Profile {    

    private static final Logger log = Logger.getLogger("lawena");

    public enum Key {
        TfDir(""),
        MovieDir(""),
        Width(1280, 640, Integer.MAX_VALUE),
        Height(720, 360, Integer.MAX_VALUE),
        Framerate(120, 24, Integer.MAX_VALUE),
        DxLevel("98", "80", "81", "90", "95", "98"),
        Hud("medic", "killnotices", "medic", "full", "default", "custom"),
        Skybox("Default"),
        CustomResources("no_announcer_voices.vpk|no_applause_sounds.vpk|no_domination_sounds.vpk"),
        ViewmodelSwitch("on", "on", "off", "default"),
        ViewmodelFov(70, Integer.MIN_VALUE, Integer.MAX_VALUE),
        MotionBlur(true),
        Crosshair(false),
        CrosshairSwitch(false),
        CombatText(false),
        Hitsounds(false),
        Voice(false),
        SteamCloud(false),
        Condebug(true),
        HudMinmode(true),
        Particles(""),
        LogConsoleLevel("ALL"),
        LogFileLevel("FINE"),
        LogUiLevel("FINE"),
        LaunchTimeout(120, 0, Integer.MAX_VALUE),
        Insecure(false),
        VdmSrcDemoFix(false),
        KillStreaks(true);

        private Object value;
        private List<String> allowedValues;
        private int min = Integer.MIN_VALUE;
        private int max = Integer.MAX_VALUE;

        private Key(String value) {
            this.value = value;
        }

        private Key(boolean value) {
            this.value = value;
        }

        private Key(int value, int min, int max) {
            this.value = value;
            this.min = min;
            this.max = max;
        }

        private Key(String value, String... validValues) {
            this(value);
            if (validValues != null) {
                this.allowedValues = Arrays.asList(validValues);
            }
        }

        @Override
        public String toString() {
            return name();
        }

        public boolean isValid(String str) {
            return allowedValues == null || allowedValues.contains(str);
        }

        public List<String> getAllowedValues() {
            return allowedValues;
        }

        public Object defValue() {
            return value;
        }

    }

    private Properties properties;
    private String demoname;
    
    public Profile() {
        
    }
    
    public Profile(Profile other) {
        this.properties = other.properties; 
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

    public void setHeight(int value) {
        setInt(Key.Height, value);
    }
    
    public int getHeight() {
        return getInt(Key.Height);
    }

    public void setWidth(int value) {
        setInt(Key.Width, value);
    }
    
    public int getWidth() {
        return getInt(Key.Width);
    }

    public void setFramerate(int value) {
        setInt(Key.Framerate, value);
    }

    public int getFramerate() {
        return getInt(Key.Framerate);
    }

    public void setHud(String value) {
        setString(Key.Hud, value);
    }

    public String getHud() {
        return getString(Key.Hud);
    }

    public void setViewmodelFov(int value) {
        setInt(Key.ViewmodelFov, value);
    }

    public int getViewmodelFov() {
        return getInt(Key.ViewmodelFov);
    }

    public void setMotionBlur(boolean value) {
        setBoolean(Key.MotionBlur, value);
    }

    public boolean getMotionBlur() {
        return getBoolean(Key.MotionBlur);
    }

    public void setViewmodelSwitch(String value) {
        setString(Key.ViewmodelSwitch, value);
    }

    public String getViewmodelSwitch() {
        return getString(Key.ViewmodelSwitch);
    }

    public void setCrosshairSwitch(boolean value) {
        setBoolean(Key.CrosshairSwitch, value);
    }

    public boolean getCrosshairSwitch() {
        return getBoolean(Key.CrosshairSwitch);
    }

    public void setCrosshair(boolean value) {
        setBoolean(Key.Crosshair, value);
    }

    public boolean getCrosshair() {
        return getBoolean(Key.Crosshair);
    }

    public void setCombattext(boolean value) {
        setBoolean(Key.CombatText, value);
    }

    public boolean getCombattext() {
        return getBoolean(Key.CombatText);
    }

    public void setHitsounds(boolean value) {
        setBoolean(Key.Hitsounds, value);
    }

    public boolean getHitsounds() {
        return getBoolean(Key.Hitsounds);
    }

    public void setVoice(boolean value) {
        setBoolean(Key.Voice, value);
    }

    public boolean getVoice() {
        return getBoolean(Key.Voice);
    }

    public void setSteamCloud(boolean value) {
        setBoolean(Key.SteamCloud, value);
    }

    public boolean getSteamCloud() {
        return getBoolean(Key.SteamCloud);
    }

    public void setDxlevel(String value) {
        setString(Key.DxLevel, value);
    }

    public String getDxlevel() {
        return getString(Key.DxLevel);
    }

    public void setTfPath(Path value) {
        setString(Key.TfDir, value.toString());
    }

    public Path getTfPath() {
        String value = getString(Key.TfDir);
        return (value == null ? null : Paths.get(value));
    }

    public void setMoviePath(Path value) {
        setString(Key.MovieDir, value.toString());
    }

    public Path getMoviePath() {
        String value = getString(Key.MovieDir);
        return (value == null ? null : Paths.get(value));
    }

    public void setSkybox(String value) {
        setString(Key.Skybox, value);
    }

    public String getSkybox() {
        return getString(Key.Skybox);
    }

    public void setCondebug(boolean value) {
        setBoolean(Key.Condebug, value);
    }

    public boolean getCondebug() {
        return getBoolean(Key.Condebug);
    }

    public void setCustomResources(List<String> values) {
        Key key = Key.CustomResources;
        setString(key, listToString(values, '|'));
    }

    public List<String> getCustomResources() {
        return getList(Key.CustomResources);
    }

    private String listToString(List<String> list, char separator) {
        Iterator<String> it = list.iterator();
        if (!it.hasNext())
            return "";

        StringBuilder sb = new StringBuilder();
        for (;;) {
            String e = it.next();
            sb.append(e);
            if (!it.hasNext())
                return sb.toString();
            sb.append(separator);
        }
    }

    private List<String> getList(Key key) {
        String value = getString(key);
        List<String> list = new ArrayList<>();
        if (!value.isEmpty()) {
            String[] resources = getString(key).split("\\|");
            Collections.addAll(list, resources);
        }
        return list;
    }

    public void setHudMinmode(boolean value) {
        setBoolean(Key.HudMinmode, value);
    }

    public boolean getHudMinmode() {
        return getBoolean(Key.HudMinmode);
    }

    public void setDemoname(String demoname) {
        this.demoname = demoname;
    }

    public String getDemoname() {
        return demoname;
    }

    public void setParticles(List<String> values) {
        Key key = Key.Particles;
        setString(key, listToString(values, '|'));
    }

    public List<String> getParticles() {
        return getList(Key.Particles);
    }

    public void setLogConsoleLevel(String value) {
        setString(Key.LogConsoleLevel, value);
    }

    public Level getLogConsoleLevel() {
        try {
            return Level.parse(getString(Key.LogConsoleLevel));
        } catch (IllegalArgumentException e) {
            return Level.ALL;
        }
    }

    public void setLogFileLevel(String value) {
        setString(Key.LogFileLevel, value);
    }

    public Level getLogFileLevel() {
        try {
            return Level.parse(getString(Key.LogFileLevel));
        } catch (IllegalArgumentException e) {
            return Level.FINE;
        }
    }

    public void setLogUiLevel(String value) {
        setString(Key.LogUiLevel, value);
    }

    public Level getLogUiLevel() {
        try {
            return Level.parse(getString(Key.LogUiLevel));
        } catch (IllegalArgumentException e) {
            return Level.FINE;
        }
    }

    public void setLaunchTimeout(int value) {
        setInt(Key.LaunchTimeout, value);
    }

    public int getLaunchTimeout() {
        return getInt(Key.LaunchTimeout);
    }

    public void setInsecure(boolean value) {
        setBoolean(Key.Insecure, value);
    }

    public boolean getInsecure() {
        return getBoolean(Key.Insecure);
    }

    public void setVdmSrcDemoFix(boolean value) {
        setBoolean(Key.VdmSrcDemoFix, value);
    }

    public boolean getVdmSrcDemoFix() {
        return getBoolean(Key.VdmSrcDemoFix);
    }

    public void setKillStreaks(boolean value) {
        setBoolean(Key.KillStreaks, value);
    }

    public boolean getKillStreaks() {
        return getBoolean(Key.KillStreaks);
    }

}
