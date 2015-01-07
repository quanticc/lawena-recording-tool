package com.github.lawena.app.model;

import java.util.ArrayList;
import java.util.List;

import com.github.lawena.profile.Key;

public class ConfigWriterTf extends ConfigWriter {

  public ConfigWriterTf(Settings settings) {
    super(settings);
  }

  @SuppressWarnings("nls")
  @Override
  protected List<String> writeGameConfig() {
    List<String> lines = new ArrayList<>();
    addNotice(lines);
    addFramerateLines(lines);
    lines.add("cl_cloud_settings 0");
    lines.add("mat_motion_blur_enabled " + (Key.motionBlur.getValue(settings) ? "1" : "0"));
    lines.add("mat_motion_blur_forward_enabled " + (Key.motionBlur.getValue(settings) ? "1" : "0"));
    lines.add("mat_motion_blur_strength " + (Key.motionBlur.getValue(settings) ? "1" : "0"));
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
    lines.add("cl_hud_minmode " + (Key.hudMinmode.getValue(settings) ? "1" : "0"));
    lines.add("cl_hud_playerclass_use_playermodel "
        + (Key.hudPlayerModel.getValue(settings) ? "1" : "0"));
    lines.add((Key.viewmodelSwitch.getValue(settings).equals("off") ? "//" : "")
        + "lockviewmodelson");
    lines.add((Key.viewmodelSwitch.getValue(settings).equals("on") ? "//" : "")
        + "lockviewmodelsoff");
    lines.add((crosshairswitch ? "//" : "") + "lockcrosshair");
    lines.add((crosshairswitch ? "//" : "") + "alias toggle \"\"");
    lines.add("echo \"[Lawena] Applying user custom settings\"");
    lines.add("exec static.cfg");
    lines.add(Key.extConVars.getValue(settings));
    return lines;
  }

}
