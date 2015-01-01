package com.github.lawena.app.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.profile.Key;

public class ConfigWriterGo extends ConfigWriter {

  private static final Logger log = LoggerFactory.getLogger(ConfigWriterGo.class);

  public ConfigWriterGo(Settings settings) {
    super(settings);
  }

  @Override
  protected List<String> writeGameConfig() {
    // in CS:GO you can't alias existing convars or concommands
    List<String> lines = new ArrayList<>();
    addFramerateLines(lines);
    lines.add("viewmodel_fov " + Key.viewmodelFov.getValue(settings));
    lines.add("crosshair " + (!Key.noCrosshair.getValue(settings) ? "1" : "0"));
    lines.add("voice_enable " + (!Key.noVoice.getValue(settings) ? "1" : "0"));
    lines.add("voice_caster_enable " + (!Key.noCasterVoice.getValue(settings) ? "1" : "0"));
    lines.add("net_graph 0");
    lines.add("cl_showfps 0");
    lines.add("cl_draw_only_deathnotices 1");
    lines.add("volume 0.2");
    lines.add("hidehud 8329");
    lines.add("hidehud 8320");
    lines.add("hud_takesshots 0");
    lines.add("");
    lines.add("engine_no_focus_sleep 0");
    lines.add(Key.extConVars.getValue(settings));
    lines.add((Key.viewmodelSwitch.getValue(settings).equals("off") ? "//" : "")
        + "lockviewmodelson");
    lines.add((Key.viewmodelSwitch.getValue(settings).equals("on") ? "//" : "")
        + "lockviewmodelsoff");
    return lines;
  }

}
