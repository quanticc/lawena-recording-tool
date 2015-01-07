package com.github.lawena.app.model;

import java.util.ArrayList;
import java.util.List;

import com.github.lawena.profile.Key;

public class ConfigWriterGo extends ConfigWriter {

  public ConfigWriterGo(Settings settings) {
    super(settings);
  }

  @SuppressWarnings("nls")
  @Override
  protected List<String> writeGameConfig() {
    // in CS:GO you can't alias existing convars or concommands
    List<String> lines = new ArrayList<>();
    addFramerateLines(lines);
    // convars tied to value from config key
    lines.add("viewmodel_fov " + Key.viewmodelFov.getValue(settings));
    lines.add("crosshair " + (!Key.noCrosshair.getValue(settings) ? "1" : "0"));
    lines.add("voice_enable " + (!Key.noVoice.getValue(settings) ? "1" : "0"));
    lines.add("voice_caster_enable " + (!Key.noCasterVoice.getValue(settings) ? "1" : "0"));
    lines.add((Key.viewmodelSwitch.getValue(settings).equals("off") ? "//" : "")
        + "lockviewmodelson");
    lines.add((Key.viewmodelSwitch.getValue(settings).equals("on") ? "//" : "")
        + "lockviewmodelsoff");
    // values from cfg
    lines.add("echo \"[Lawena] Applying user custom settings\"");
    lines.add("exec static.cfg");
    lines.add(Key.extConVars.getValue(settings));
    return lines;
  }

}
