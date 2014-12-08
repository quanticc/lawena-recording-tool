package com.github.lawena.app.model;

import static com.github.lawena.util.Util.toPath;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.profile.Key;

public class ConfigWriter {

  private static final Logger log = LoggerFactory.getLogger(ConfigWriter.class);

  private Settings settings;

  public ConfigWriter(Settings settings) {
    this.settings = settings;
  }

  public void writeAll() throws IOException {
    writeGameConfig();
    writeSegmentSlotConfigs();
    try {
      writeNamescrollConfig();
    } catch (IOException e) {
      log.warn("Could not detect current movie slot");
    }
  }

  private void writeGameConfig() throws IOException {
    List<String> lines = new ArrayList<>();
    int framerate = Key.framerate.getValue(settings);
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
    lines.add("mat_motion_blur_enabled " + (Key.motionBlur.getValue(settings) ? "1" : "0"));
    lines.add("mat_motion_blur_forward_enabled " + (Key.motionBlur.getValue(settings) ? "1" : "0"));
    lines.add("mat_motion_blur_strength " + (Key.motionBlur.getValue(settings) ? "1" : "0"));
    lines.add("cl_cloud_settings 0");
    lines.add("viewmodel_fov_demo " + Key.viewmodelFov.getValue(settings));
    boolean crosshairswitch = !Key.noCrosshairSwitch.getValue(settings);
    lines.add((crosshairswitch ? "//" : "") + "cl_crosshair_file \"\"");
    lines.add((crosshairswitch ? "//" : "") + "cl_crosshair_red 200");
    lines.add((crosshairswitch ? "//" : "") + "cl_crosshair_green 200");
    lines.add((crosshairswitch ? "//" : "") + "cl_crosshair_blue 200");
    lines.add((crosshairswitch ? "//" : "") + "cl_crosshair_scale 32");
    lines.add((crosshairswitch ? "//" : "") + "cl_crosshairalpha 200");
    lines.add("crosshair " + (!Key.noCrosshair.getValue(settings) ? "1" : "0"));
    lines.add("hud_combattext " + (!Key.noDamageNumbers.getValue(settings) ? "1" : "0"));
    lines.add("hud_combattext_healing " + (!Key.noDamageNumbers.getValue(settings) ? "1" : "0"));
    lines.add("tf_dingalingaling " + (!Key.noHitsounds.getValue(settings) ? "1" : "0"));
    lines.add("voice_enable " + (!Key.noVoice.getValue(settings) ? "1" : "0"));
    lines.add("alias voice_enable \"\"");
    lines.add("cl_autorezoom 0");
    lines.add("hud_saytext_time 0");
    lines.add("net_graph 0");
    lines.add("alias net_graph \"\"");
    lines.add("alias voice_menu_1 \"\"");
    lines.add("alias voice_menu_2 \"\"");
    lines.add("alias voice_menu_3 \"\"");
    lines.add("cl_showfps 0");
    lines.add("alias cl_showfps \"\"");
    lines.add("volume 0.2");
    lines.add("hud_fastswitch 1");
    lines.add("cl_hud_minmode " + (Key.hudMinmode.getValue(settings) ? "1" : "0"));
    lines.add("cl_hud_playerclass_playermodel_showed_confirm_dialog 1");
    lines.add("cl_hud_playerclass_use_playermodel "
        + (Key.hudPlayerModel.getValue(settings) ? "1" : "0"));
    lines.add("tf_training_has_prompted_for_loadout 1");
    lines.add("tf_training_has_prompted_for_training 1");
    lines.add("tf_training_has_prompted_for_offline_practice 1");
    lines.add("tf_training_has_prompted_for_forums 1");
    lines.add("tf_training_has_prompted_for_options 1");
    lines.add("tf_training_has_prompted_for_loadout 1");
    lines.add("tf_explanations_charinfo_armory_panel 1");
    lines.add("tf_explanations_charinfopanel 1");
    lines.add("tf_show_preset_explanation_in_class_loadout 0");
    lines.add("tf_show_taunt_explanation_in_class_loadout 0");
    lines.add("tf_explanations_craftingpanel 1");
    lines.add("tf_explanations_discardpanel 1");
    lines.add("tf_explanations_store 1");
    lines.add("tf_explanations_backpackpanel 1");
    lines.add("engine_no_focus_sleep 0");
    lines.add("cl_spec_carrieditems 0");
    lines.add("tf_hud_target_id_disable_floating_health 1");
    lines.add(Key.extConVars.getValue(settings));
    lines.add((Key.viewmodelSwitch.getValue(settings).equals("off") ? "//" : "")
        + "lockviewmodelson");
    lines.add((Key.viewmodelSwitch.getValue(settings).equals("on") ? "//" : "")
        + "lockviewmodelsoff");
    lines.add((crosshairswitch ? "//" : "") + "lockcrosshair");
    lines.add((crosshairswitch ? "//" : "") + "alias toggle \"\"");
    Files.write(
        Paths.get("lwrt", Key.gameFolderName.getValue(settings), "config/cfg/settings.cfg"), lines,
        Charset.forName("UTF-8"));
  }

  private void writeNamescrollConfig() throws IOException {
    String lastmovie = "";
    String alias = "alias namescroll stmov1";
    Path recPath = toPath(Key.recordingPath.getValue(settings));
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(recPath, "*.tga")) {
      for (Path moviefile : stream) {
        String filename = moviefile.getFileName().toString();
        lastmovie = (lastmovie.compareTo(filename) > 0 ? lastmovie : filename);
      }
    }
    if (!lastmovie.equals("")) {
      int idx = "abcdefghijklmno".indexOf(lastmovie.charAt(0));
      if (idx >= 0) {
        alias = "alias namescroll stmov" + (idx + 2);
      } else if (lastmovie.charAt(0) == 'p') {
        alias = "alias namescroll noslots";
      }
    }
    Path data = settings.getParentDataPath();
    Files.write(data.resolve("config/cfg/namescroll.cfg"), Arrays.asList(alias),
        Charset.forName("UTF-8"));
  }

  private void writeSegmentSlotConfigs() throws IOException {
    String[] prefixes =
        {"a1", "b2", "c3", "d4", "e5", "f6", "g7", "h8", "i9", "j10", "k11", "l12", "m13", "n14",
            "o15", "p16"};
    String video = Key.recorderVideoFormat.getValue(settings);
    int quality = Key.recorderJpegQuality.getValue(settings);
    String audio = "wav";
    Path recPath = toPath(Key.recordingPath.getValue(settings));
    Path data = settings.getParentDataPath();
    for (String prefix : prefixes) {
      String line =
          "startmovie \"" + recPath + "/" + prefix + "_\" " + video + " " + audio
              + (video.equals("jpg") ? " jpeg_quality " + quality : "");
      Files.write(data.resolve("config/cfg/mov/" + prefix + ".cfg"), Arrays.asList(line),
          Charset.forName("UTF-8"));
    }
  }

}
