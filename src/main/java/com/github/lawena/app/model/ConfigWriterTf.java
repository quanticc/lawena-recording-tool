package com.github.lawena.app.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.profile.Key;

public class ConfigWriterTf extends ConfigWriter {

  private static final Logger log = LoggerFactory.getLogger(ConfigWriterTf.class);

  public ConfigWriterTf(Settings settings) {
    super(settings);
  }

  @Override
  protected List<String> writeGameConfig() {
    List<String> lines = new ArrayList<>();
    addFramerateLines(lines);
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
    return lines;
  }

}
