package lwrt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SettingsManager {

	private static final Logger log = Logger.getLogger("lawena");
	private String filename;
	private Properties properties;
	// transient property, do not save to file
	private String demoname;

	public SettingsManager(String settingsFile) {
		filename = settingsFile;
		properties = new Properties();
		for (Key key : Key.values()) {
			properties.setProperty(key.toString(), key.value + "");
		}
		try (BufferedReader r = Files.newBufferedReader(Paths.get(filename), Charset.forName("UTF-8"))) {
			properties.load(r);
		} catch (FileNotFoundException e) {
			// do nothing, will load defaults
		} catch (IOException e) {
			log.log(Level.INFO, "Problem while loading settings, reverting to defaults", e);
		}
	}

	public void loadDefaults() {
		for (Key key : Key.values()) {
			properties.setProperty(key.toString(), key.value + "");
		}
	}

	public void save() {
		try (BufferedWriter w = Files.newBufferedWriter(Paths.get(filename), Charset.forName("UTF-8"))) {
			properties.store(w, "Lawena Settings");
		} catch (IOException e) {
			log.log(Level.INFO, "Settings could not be saved", e);
		}
	}

	public void saveToCfg() throws IOException {
		List<String> lines = new ArrayList<>();
		int framerate = getFramerate();
		lines.add("alias recframerate host_framerate " + framerate);
		if (framerate < 60) {
			lines.add("alias currentfpsup 60fps");
			lines.add("alias currentfpsdn 3840fps");
		} else if (framerate == 60) {
			lines.add("alias currentfpsup 120fps");
			lines.add("alias currentfpsdn 3840fps");
		} else if (framerate < 120) {
			lines.add("alias currentfpsup 120fps");
			lines.add("alias currentfpsdn 60fps");
		} else if (framerate == 120) {
			lines.add("alias currentfpsup 240fps");
			lines.add("alias currentfpsdn 60fps");
		} else if (framerate < 240) {
			lines.add("alias currentfpsup 240fps");
			lines.add("alias currentfpsdn 120fps");
		} else if (framerate == 240) {
			lines.add("alias currentfpsup 480fps");
			lines.add("alias currentfpsdn 120fps");
		} else if (framerate < 480) {
			lines.add("alias currentfpsup 480fps");
			lines.add("alias currentfpsdn 240fps");
		} else if (framerate == 480) {
			lines.add("alias currentfpsup 960fps");
			lines.add("alias currentfpsdn 240fps");
		} else if (framerate < 960) {
			lines.add("alias currentfpsup 960fps");
			lines.add("alias currentfpsdn 480fps");
		} else if (framerate == 960) {
			lines.add("alias currentfpsup 1920fps");
			lines.add("alias currentfpsdn 480fps");
		} else if (framerate < 1920) {
			lines.add("alias currentfpsup 1920fps");
			lines.add("alias currentfpsdn 960fps");
		} else if (framerate == 1920) {
			lines.add("alias currentfpsup 3840fps");
			lines.add("alias currentfpsdn 960fps");
		} else if (framerate < 3840) {
			lines.add("alias currentfpsup 3840fps");
			lines.add("alias currentfpsdn 1920fps");
		} else if (framerate == 3840) {
			lines.add("alias currentfpsup 60fps");
			lines.add("alias currentfpsdn 1920fps");
		} else {
			lines.add("alias currentfpsup 60fps");
			lines.add("alias currentfpsdn 3840fps");
		}
		lines.add("mat_motion_blur_enabled " + (getMotionBlur() ? "1" : "0"));
		lines.add("mat_motion_blur_forward_enabled " + (getMotionBlur() ? "1" : "0"));
		lines.add("mat_motion_blur_strength " + (getMotionBlur() ? "1" : "0"));
		lines.add((getSteamCloud() ? "//" : "") + "cl_cloud_settings 0");
		lines.add((getCondebug() ? "" : "//") + "con_timestamp 1");
		lines.add("viewmodel_fov_demo " + getViewmodelFov());
		boolean crosshairswitch = getCrosshairSwitch();
		lines.add((crosshairswitch ? "//" : "") + "cl_crosshair_file \"\"");
		lines.add((crosshairswitch ? "//" : "") + "cl_crosshair_red 200");
		lines.add((crosshairswitch ? "//" : "") + "cl_crosshair_green 200");
		lines.add((crosshairswitch ? "//" : "") + "cl_crosshair_blue 200");
		lines.add((crosshairswitch ? "//" : "") + "cl_crosshair_scale 32");
		lines.add((crosshairswitch ? "//" : "") + "cl_crosshairalpha 200");
		lines.add("crosshair " + (getCrosshair() ? "1" : "0"));
		lines.add("hud_combattext " + (getCombattext() ? "1" : "0"));
		lines.add("hud_combattext_healing " + (getCombattext() ? "1" : "0"));
		lines.add("tf_dingalingaling " + (getHitsounds() ? "1" : "0"));
		lines.add("tf_dingalingaling_lasthit " + (getHitsounds() ? "1" : "0"));
		lines.add("voice_enable " + (getVoice() ? "1" : "0"));
		lines.add("cl_autorezoom 0");
		lines.add("cl_autoreload 0");
		lines.add("hud_saytext_time 0");
		lines.add("net_graph 0");
		lines.add("alias voice_menu_1 \"\"");
		lines.add("alias voice_menu_2 \"\"");
		lines.add("alias voice_menu_3 \"\"");
		lines.add("alias +taunt \"\"");
		lines.add("alias +context_action \"\"");
		lines.add("cl_showfps 0");
		lines.add("volume 0.5");
		lines.add("hud_fastswitch 1");
		lines.add("cl_hud_minmode " + (getHudMinmode() ? "1" : "0"));
		lines.add("cl_hud_playerclass_playermodel_showed_confirm_dialog 1");
		lines.add("cl_hud_playerclass_use_playermodel " + (getHudPlayerModel() ? "1" : "0"));
		lines.add("engine_no_focus_sleep 0");
		lines.add("cl_spec_carrieditems 0");
		lines.add("tf_hud_target_id_disable_floating_health 1");
		lines.add(getCustomSettings());
		lines.add((getViewmodelSwitch().equals("off") ? "//" : "") + "lockviewmodelson");
		lines.add((getViewmodelSwitch().equals("on") ? "//" : "") + "lockviewmodelsoff");
		lines.add((crosshairswitch ? "//" : "") + "lockcrosshair");
		lines.add((crosshairswitch ? "//" : "") + "alias toggle \"\"");
		lines.add("alias net_graph \"\"");
		lines.add("alias cl_showfps \"\"");
		lines.add("alias voice_enable \"\"");
		Files.write(Paths.get("cfg","settings.cfg"), lines, Charset.forName("UTF-8"));

		if (demoname != null) {
			Files.write(Paths.get("cfg","lawena.cfg"), Collections.singletonList("playdemo \"" + demoname + "\""),
					Charset.forName("UTF-8"));
		} else {
			Files.deleteIfExists(Paths.get("cfg","lawena.cfg"));
		}
	}

	public void setString(Key key, String value) {
		if (key.isValid(value)) {
			properties.setProperty(key.toString(), value);
		} else {
			throw new IllegalArgumentException(key + " does not allow value: " + value);
		}
	}

	public String getString(Key key) {
		return properties.getProperty(key.toString());
	}

	public void setInt(Key key, int value) {
		if (value >= key.min && value <= key.max) {
			properties.setProperty(key.toString(), value + "");
		} else {
			throw new IllegalArgumentException(key + " does not allow value: " + value);
		}
	}

	public int getInt(Key key) {
		String value = properties.getProperty(key.toString());
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			log.info("Invalid numeric format in " + key + ": " + value + ", loading default value");
			return Integer.parseInt(key.value.toString());
		}
	}

	public void setBoolean(Key key, boolean value) {
		properties.setProperty(key.toString(), value + "");
	}

	public boolean getBoolean(Key key) {
		return Boolean.parseBoolean(properties.getProperty(key.toString()));
	}

	public void setList(Key key, List<String> values) {
		setString(key, listToString(values, '|'));
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

	private String listToString(List<String> list, char separator) {
		Iterator<String> it = list.iterator();
		if (!it.hasNext()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (; ; ) {
			String e = it.next();
			sb.append(e);
			if (!it.hasNext()) {
				return sb.toString();
			}
			sb.append(separator);
		}
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public int getHeight() {
		return getInt(Key.Height);
	}

	public void setHeight(int value) {
		setInt(Key.Height, value);
	}

	public int getWidth() {
		return getInt(Key.Width);
	}

	public void setWidth(int value) {
		setInt(Key.Width, value);
	}

	public int getFramerate() {
		return getInt(Key.Framerate);
	}

	public void setFramerate(int value) {
		setInt(Key.Framerate, value);
	}

	public String getHud() {
		return getString(Key.Hud);
	}

	public void setHud(String value) {
		setString(Key.Hud, value);
	}

	public int getViewmodelFov() {
		return getInt(Key.ViewmodelFov);
	}

	public void setViewmodelFov(int value) {
		setInt(Key.ViewmodelFov, value);
	}

	public boolean getMotionBlur() {
		return getBoolean(Key.MotionBlur);
	}

	public void setMotionBlur(boolean value) {
		setBoolean(Key.MotionBlur, value);
	}

	public String getViewmodelSwitch() {
		return getString(Key.ViewmodelSwitch);
	}

	public void setViewmodelSwitch(String value) {
		setString(Key.ViewmodelSwitch, value);
	}

	public boolean getCrosshairSwitch() {
		return getBoolean(Key.CrosshairSwitch);
	}

	public void setCrosshairSwitch(boolean value) {
		setBoolean(Key.CrosshairSwitch, value);
	}

	public boolean getCrosshair() {
		return getBoolean(Key.Crosshair);
	}

	public void setCrosshair(boolean value) {
		setBoolean(Key.Crosshair, value);
	}

	public boolean getCombattext() {
		return getBoolean(Key.CombatText);
	}

	public void setCombattext(boolean value) {
		setBoolean(Key.CombatText, value);
	}

	public boolean getHitsounds() {
		return getBoolean(Key.Hitsounds);
	}

	public void setHitsounds(boolean value) {
		setBoolean(Key.Hitsounds, value);
	}

	public boolean getVoice() {
		return getBoolean(Key.Voice);
	}

	public void setVoice(boolean value) {
		setBoolean(Key.Voice, value);
	}

	private boolean getSteamCloud() {
		return getBoolean(Key.SteamCloud);
	}

	public void setSteamCloud(boolean value) {
		setBoolean(Key.SteamCloud, value);
	}

	public String getDxlevel() {
		return getString(Key.DxLevel);
	}

	public void setDxlevel(String value) {
		setString(Key.DxLevel, value);
	}

	public Path getTfPath() {
		String value = getString(Key.TfDir);
		return (value == null ? null : Paths.get(value));
	}

	public void setTfPath(Path value) {
		setString(Key.TfDir, value.toString());
	}

	// Getters

	public Path getMoviePath() {
		String value = getString(Key.MovieDir);
		return (value == null ? null : Paths.get(value));
	}

	public void setMoviePath(Path value) {
		setString(Key.MovieDir, value.toString());
	}

	public String getSkybox() {
		return getString(Key.Skybox);
	}

	public void setSkybox(String value) {
		setString(Key.Skybox, value);
	}

	private boolean getCondebug() {
		return getBoolean(Key.Condebug);
	}

	public void setCondebug(boolean value) {
		setBoolean(Key.Condebug, value);
	}

	public List<String> getCustomResources() {
		return getList(Key.CustomResources);
	}

	public void setCustomResources(List<String> values) {
		Key key = Key.CustomResources;
		setString(key, listToString(values, '|'));
	}

	public boolean getHudMinmode() {
		return getBoolean(Key.HudMinmode);
	}

	public void setHudMinmode(boolean value) {
		setBoolean(Key.HudMinmode, value);
	}

	public String getDemoname() {
		return demoname;
	}

	public void setDemoname(String demoname) {
		this.demoname = demoname;
	}

	public List<String> getParticles() {
		return getList(Key.Particles);
	}

	public void setParticles(List<String> values) {
		Key key = Key.Particles;
		setString(key, listToString(values, '|'));
	}

	public Level getLogConsoleLevel() {
		try {
			return Level.parse(getString(Key.LogConsoleLevel));
		} catch (IllegalArgumentException e) {
			return Level.FINER;
		}
	}

	public void setLogConsoleLevel(String value) {
		setString(Key.LogConsoleLevel, value);
	}

	public Level getLogFileLevel() {
		try {
			return Level.parse(getString(Key.LogFileLevel));
		} catch (IllegalArgumentException e) {
			return Level.FINE;
		}
	}

	public void setLogFileLevel(String value) {
		setString(Key.LogFileLevel, value);
	}

	public Level getLogUiLevel() {
		try {
			return Level.parse(getString(Key.LogUiLevel));
		} catch (IllegalArgumentException e) {
			return Level.FINE;
		}
	}

	public void setLogUiLevel(String value) {
		setString(Key.LogUiLevel, value);
	}

	public int getLaunchTimeout() {
		return getInt(Key.LaunchTimeout);
	}

	public void setLaunchTimeout(int value) {
		setInt(Key.LaunchTimeout, value);
	}

	public boolean getInsecure() {
		return getBoolean(Key.Insecure);
	}

	public void setInsecure(boolean value) {
		setBoolean(Key.Insecure, value);
	}

	public boolean getHudPlayerModel() {
		return getBoolean(Key.HudPlayerModel);
	}

	public void setHudPlayerModel(boolean value) {
		setBoolean(Key.HudPlayerModel, value);
	}

	public String getCustomSettings() {
		return getString(Key.CustomSettings);
	}

	public void setCustomSettings(String value) {
		setString(Key.CustomSettings, value);
	}

	public enum Key {
		TfDir(""),
		MovieDir(""),
		SteamDir(""),
		AltSteamDir(""),
		Width(1280, 640, Integer.MAX_VALUE),
		Height(720, 360, Integer.MAX_VALUE),
		Framerate(120, 24, Integer.MAX_VALUE),
		DxLevel("98", "80", "81", "90", "95", "98"),
		Hud("hud_medic", "hud_killnotices", "hud_medic", "hud_default", "custom"),
		Skybox("Default"),
		CustomResources("no_announcer_voices.vpk|no_applause_sounds.vpk|no_domination_sounds.vpk"),
		ViewmodelSwitch("on", "on", "off", "default"),
		ViewmodelFov(70, 1, 179),
		MotionBlur(true),
		Crosshair(false),
		CrosshairSwitch(false),
		CombatText(false),
		Hitsounds(false),
		Voice(false),
		SteamCloud(false),
		Condebug(true),
		HudMinmode(true),
		HudPlayerModel(false),
		Particles(""),
		LogConsoleLevel("FINER"),
		LogFileLevel("FINE"),
		LogUiLevel("FINE"),
		LaunchTimeout(120, 0, Integer.MAX_VALUE),
		Insecure(false),
		VdmSkipMode("SKIP_AHEAD"),
		VdmSkipStartCommand("demo_timescale 5"),
		VdmSkipStopCommand("demo_timescale 1"),
		VdmTickPadding(500, 0, Integer.MAX_VALUE),
		InstallFonts(true),
		CopyUserConfig(true),
		CustomSettings("// Custom User Settings"),
		CustomSettingsDialogWidth(300, 200, Integer.MAX_VALUE),
		CustomSettingsDialogHeight(200, 100, Integer.MAX_VALUE),
		SourceRecorderVideoFormat("tga", "tga", "jpg", "h264", "webm"),
		SourceRecorderAudioFormat("wav", "wav", ""),
		SourceRecorderJpegQuality(50, 1, 100),
		DeleteBackupsWhenRestoring(false),
		BigFolderMBThreshold(200, 0, Integer.MAX_VALUE),
		SetSystemLookAndFeel(true),
		HlaePath(""),
		LaunchMode("hl2"),
		LaunchOptions("-novid -console");

		private Object value;
		private List<String> allowedValues;
		private int min = Integer.MIN_VALUE;
		private int max = Integer.MAX_VALUE;

		Key(String value) {
			this.value = value;
		}

		Key(boolean value) {
			this.value = value;
		}

		Key(int value, int min, int max) {
			this.value = value;
			this.min = min;
			this.max = max;
		}

		Key(String value, String... validValues) {
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

}
