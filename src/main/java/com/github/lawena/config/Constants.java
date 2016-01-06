package com.github.lawena.config;

import com.github.lawena.Messages;
import com.github.lawena.domain.Launcher;
import com.github.lawena.domain.OperatingSystemMap;
import com.github.lawena.util.ExternalString;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Application constants.
 */
public final class Constants {

    public static final String SPRING_PROFILE_DEVELOPMENT = "dev";

    public static final String CUSTOM_FOLDER_NAME = "custom";
    public static final Path LWRT_PATH = Paths.get("lwrt");
    public static final Path VTFCMD_PATH = LWRT_PATH.resolve("tools/vtfcmd/VTFCmd.exe");
    public static final Path HANDLE_PATH = LWRT_PATH.resolve("tools/handle/handle.exe");
    public static final Path PROCESS_CHECKER_PATH = LWRT_PATH.resolve("tools/processcheck/procchk.vbs");
    public static final OperatingSystemMap STEAM_APP_NAME = OperatingSystemMap.create("steam.exe", "steam.app", "steam.sh");
    public static final List<ExternalString> CAPTURE_MODES = ExternalString.from(s -> Messages.getString("capture." + s),
            Arrays.asList("SourceRecorderTGA", "SourceRecorderJPEG", "SrcDemo2Managed", "SrcDemo2Standalone"));
    public static final List<ExternalString> DIRECTX_LEVELS = ExternalString.from(s -> Messages.getString("dxlevel." + s),
            Arrays.asList("DxLevel80", "DxLevel81", "DxLevel90", "DxLevel95", "DxLevel98"));
    public static final List<ExternalString> HUDS = ExternalString.from(s -> Messages.getString("hud." + s),
            Arrays.asList("HudMinimal", "HudBasic", "HudCustom"));
    public static final List<ExternalString> VIEWMODELS = ExternalString.from(s -> Messages.getString("viewmodel." + s),
            Arrays.asList("VmSwitchOn", "VmSwitchOff", "VmSwitchDefault"));
    public static final List<ExternalString> LAUNCH_MODES = ExternalString.from(s -> Messages.getString("launchMode." + s),
            Arrays.asList(Launcher.Mode.values()).stream().map(Enum::name).collect(Collectors.toList()));
    public static final ExternalString DEFAULT_CAPTURE_MODE = CAPTURE_MODES.get(0);
    public static final ExternalString DEFAULT_DIRECTX_LEVEL = DIRECTX_LEVELS.get(4);
    public static final ExternalString DEFAULT_HUD = HUDS.get(0);
    public static final ExternalString DEFAULT_VIEWMODEL = VIEWMODELS.get(2);
    public static final ExternalString DEFAULT_LAUNCH_MODE = LAUNCH_MODES.get(1);
    public static final String JPEG_CAPTURE_MODE_KEY = CAPTURE_MODES.get(1).getKey();
    public static final Map<String, Object> USER_FRIENDLY_KEYMAP = new HashMap<String, Object>() {
        {
            put("UPARROW", "Up arrow");
            put("DOWNARROW", "Down arrow");
            put("KP_DOWNARROW", "Numpad 2");
            put("KP_END", "Numpad 1");
            put("KP_LEFTARROW", "Numpad 4");
            put("KP_HOME", "Numpad 7");
            put("KP_UPARROW", "Numpad 8");
            put("KP_PGUP", "Numpad 9");
            put("KP_RIGHTARROW", "Numpad 6");
            put("KP_PGDN", "Numpad 3");
            put("KP_5", "Numpad 5");
            put("KP_INS", "Numpad 0");
            put("KP_MINUS", "Numpad -");
            put("KP_PLUS", "Numpad +");
        }
    };

    public static final int MINIMUM_WIDTH = 640;
    public static final int INITIAL_WIDTH = 1280;
    public static final int MINIMUM_HEIGHT = 480;
    public static final int INITIAL_HEIGHT = 720;
    public static final int MINIMUM_FPS = 24;
    public static final int INITIAL_FPS = 120;
    public static final int MINIMUM_JPEG_QUALITY = 1;
    public static final int MAXIMUM_JPEG_QUALITY = 100;
    public static final int INITIAL_JPEG_QUALITY = 90;
    public static final double MINIMUM_VM_FOV = 0.1;
    public static final double MAXIMUM_VM_FOV = 179.899994;
    public static final double INITIAL_VM_FOV = 75;
    public static final String ALLOWED_NUMERIC = "0123456789";
    public static final String ALLOWED_DECIMAL = ".0123456789";

    private Constants() {
    }
}
