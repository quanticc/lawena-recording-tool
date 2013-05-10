
package config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class SettingsManager {

    private String filename;
    private int height;
    private int width;
    private int framerate;
    private int viewmodelfov;
    private int dxlevel;
    private String hud;
    private String viewmodelswitch;
    private boolean motionblur;
    private boolean crosshairswitch;
    private boolean crosshair;
    private boolean combattext;
    private boolean announcer;
    private boolean domination;
    private boolean hitsounds;
    private boolean voice;
    private boolean steamcloud;

    public SettingsManager(String settingsFile) throws Exception {

        filename = settingsFile;

        BufferedReader settings = new BufferedReader(new FileReader(filename));
        String line;

        while ((line = settings.readLine()) != null) {

            if (line.charAt(0) == ':')
                continue;

            if (line.indexOf("height") >= 0) {
                height = Integer.parseInt(line.substring(line.indexOf('=') + 1));
            }

            if (line.indexOf("width") >= 0) {
                width = Integer.parseInt(line.substring(line.indexOf('=') + 1));
            }

            if (line.indexOf("frame rate") >= 0) {
                framerate = Integer.parseInt(line.substring(line.indexOf('=') + 1));
            }
            if (line.indexOf("hud") >= 0) {
                hud = line.substring(line.indexOf('=') + 1);
            }

            if (line.indexOf("viewmodel switching") >= 0) {
                viewmodelswitch = line.substring(line.indexOf('=') + 1);
            }

            if (line.indexOf("motion") >= 0) {
                motionblur = line.substring(line.indexOf('=') + 1).equals("on") ? true : false;
            }

            if (line.indexOf("viewmodel fov") >= 0) {
                viewmodelfov = Integer.parseInt(line.substring(line.indexOf('=') + 1));
            }

            if (line.indexOf("dxlevel") >= 0) {
                dxlevel = Integer.parseInt(line.substring(line.indexOf('=') + 1));
            }

            if (line.indexOf("crosshair switching") >= 0) {
                crosshairswitch = line.substring(line.indexOf('=') + 1).equals("on") ? true : false;
            }

            if (line.indexOf("crosshair") >= 0) {
                crosshair = line.substring(line.indexOf('=') + 1).equals("on") ? true : false;
            }

            if (line.indexOf("combat text") >= 0) {
                combattext = line.substring(line.indexOf('=') + 1).equals("on") ? true : false;
            }

            if (line.indexOf("announcer") >= 0) {
                announcer = line.substring(line.indexOf('=') + 1).equals("on") ? true : false;
            }

            if (line.indexOf("domination") >= 0) {
                domination = line.substring(line.indexOf('=') + 1).equals("on") ? true : false;
            }

            if (line.indexOf("hit sounds") >= 0) {
                hitsounds = line.substring(line.indexOf('=') + 1).equals("on") ? true : false;
            }

            if (line.indexOf("voice") >= 0) {
                voice = line.substring(line.indexOf('=') + 1).equals("on") ? true : false;
            }

            if (line.indexOf("cloud") >= 0) {
                steamcloud = line.substring(line.indexOf('=') + 1).equals("on") ? true : false;
            }
        }

        settings.close();
    }

    public void save() throws Exception {
        PrintWriter settings = new PrintWriter(new FileWriter(filename));

        settings.println(": Settings File, feel free to customize manually");
        settings.println(":");
        settings.println(": Resolution Settings");
        settings.println("width=" + width);
        settings.println("height=" + height);
        settings.println(":");
        settings.println(": Framerate Settings");
        settings.println("frame rate=" + framerate);
        settings.println(":");
        settings.println(": HUD Settings");
        settings.println(": No HUD = killnotices");
        settings.println(": Medic HUD = medic");
        settings.println(": Full HUD = full");
        settings.println(": Custom HUD = custom");
        settings.println("hud=" + hud);
        settings.println(":");
        settings.println(": Misc Settings");
        settings.println("viewmodel fov=" + viewmodelfov);
        settings.println("motion blur=" + (motionblur ? "on" : "off"));
        settings.println("viewmodel switching=" + viewmodelswitch);
        settings.println("crosshair switching=" + (crosshairswitch ? "on" : "off"));
        settings.println("crosshair=" + (crosshair ? "on" : "off"));
        settings.println("combat text=" + (combattext ? "on" : "off"));
        settings.println("announcer voice=" + (announcer ? "on" : "off"));
        settings.println("domination sounds=" + (domination ? "on" : "off"));
        settings.println("hit sounds=" + (hitsounds ? "on" : "off"));
        settings.println("voice=" + (voice ? "on" : "off"));
        settings.println("steam cloud=" + (steamcloud ? "on" : "off"));
        settings.println("dxlevel=" + dxlevel);

        settings.close();
    }

    public void saveToCfg() throws IOException {
        PrintWriter settings = new PrintWriter(new FileWriter("cfg\\settings.cfg"));

        settings.println("alias recframerate host_framerate " + framerate);

        if (framerate < 60) {
            settings.println("alias currentfpsup 60fps");
            settings.println("alias currentfpsdn 3840fps");
        }
        else if (framerate == 60) {
            settings.println("alias currentfpsup 120fps");
            settings.println("alias currentfpsdn 3840fps");
        }
        else if (framerate < 120) {
            settings.println("alias currentfpsup 120fps");
            settings.println("alias currentfpsdn 60fps");
        }
        else if (framerate == 120) {
            settings.println("alias currentfpsup 240fps");
            settings.println("alias currentfpsdn 60fps");
        }
        else if (framerate < 240) {
            settings.println("alias currentfpsup 240fps");
            settings.println("alias currentfpsdn 120fps");
        }
        else if (framerate == 240) {
            settings.println("alias currentfpsup 480fps");
            settings.println("alias currentfpsdn 120fps");
        }
        else if (framerate < 480) {
            settings.println("alias currentfpsup 480fps");
            settings.println("alias currentfpsdn 240fps");
        }
        else if (framerate == 480) {
            settings.println("alias currentfpsup 960fps");
            settings.println("alias currentfpsdn 240fps");
        }
        else if (framerate < 960) {
            settings.println("alias currentfpsup 960fps");
            settings.println("alias currentfpsdn 480fps");
        }
        else if (framerate == 960) {
            settings.println("alias currentfpsup 1920fps");
            settings.println("alias currentfpsdn 480fps");
        }
        else if (framerate < 1920) {
            settings.println("alias currentfpsup 1920fps");
            settings.println("alias currentfpsdn 960fps");
        }
        else if (framerate == 1920) {
            settings.println("alias currentfpsup 3840fps");
            settings.println("alias currentfpsdn 960fps");
        }
        else if (framerate < 3840) {
            settings.println("alias currentfpsup 3840fps");
            settings.println("alias currentfpsdn 1920fps");
        }
        else if (framerate == 3840) {
            settings.println("alias currentfpsup 60fps");
            settings.println("alias currentfpsdn 1920fps");
        }
        else {
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
        settings.println((crosshairswitch ? "//" : "") + "cl_crosshair_alpha 200");
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

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setFramerate(int framerate) {
        this.framerate = framerate;
    }

    public void setHud(String hud) {
        this.hud = hud;
    }

    public void setViewmodelFov(int viewmodelfov) {
        this.viewmodelfov = viewmodelfov;
    }

    public void setMotionBlur(boolean motionblur) {
        this.motionblur = motionblur;
    }

    public void setViewmodelSwitch(String viewmodelswitch) {
        this.viewmodelswitch = viewmodelswitch;
    }

    public void setCrosshairSwitch(boolean crosshairswitch) {
        this.crosshairswitch = crosshairswitch;
    }

    public void setCrosshair(boolean crosshair) {
        this.crosshair = crosshair;
    }

    public void setCombattext(boolean combattext) {
        this.combattext = combattext;
    }

    public void setAnnouncer(boolean announcer) {
        this.announcer = announcer;
    }

    public void setDomination(boolean domination) {
        this.domination = domination;
    }

    public void setHitsounds(boolean hitsounds) {
        this.hitsounds = hitsounds;
    }

    public void setVoice(boolean voice) {
        this.voice = voice;
    }

    public void setSteamCloud(boolean steamcloud) {
        this.steamcloud = steamcloud;
    }

    public void setDxlevel(int dxlevel) {
        this.dxlevel = dxlevel;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getFramerate() {
        return framerate;
    }

    public String getHud() {
        return hud;
    }

    public int getViewmodelFov() {
        return viewmodelfov;
    }

    public boolean getMotionBlur() {
        return motionblur;
    }

    public String getViewmodelSwitch() {
        return viewmodelswitch;
    }

    public boolean getCrosshairSwitch() {
        return crosshairswitch;
    }

    public boolean getCrosshair() {
        return crosshair;
    }

    public boolean getCombattext() {
        return combattext;
    }

    public boolean getAnnouncer() {
        return announcer;
    }

    public boolean getDomination() {
        return domination;
    }

    public boolean getHitsounds() {
        return hitsounds;
    }

    public boolean getVoice() {
        return voice;
    }

    public boolean getSteamCloud() {
        return steamcloud;
    }

    public int getDxlevel() {
        return dxlevel;
    }
}
