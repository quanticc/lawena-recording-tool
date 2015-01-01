package com.github.lawena.app;

import java.awt.Component;
import java.awt.Frame;

import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.app.model.ConfigWriter;
import com.github.lawena.app.model.ConfigWriterTf;
import com.github.lawena.app.model.MainModel;
import com.github.lawena.profile.Key;
import com.github.lawena.profile.ValuesValidator;
import com.github.lawena.ui.LawenaViewTf;
import com.github.lawena.util.Util;

public class LawenaTf extends Lawena {

  private static final Logger log = LoggerFactory.getLogger(LawenaTf.class);

  public LawenaTf(MainModel mainModel) {
    super(mainModel);
  }

  @Override
  protected void setupView() {
    view = new LawenaViewTf();
  }

  @Override
  protected void setupIconImage() {
    try {
      view.setIconImage(new ImageIcon(getClass().getResource("tf2.png")).getImage());
    } catch (Exception e) {
      log.warn("Window icon missing / could not be set");
    }
  }

  @Override
  public Component viewAsComponent() {
    return (LawenaViewTf) view;
  }

  @Override
  protected void loadDependentSettings() {
    LawenaViewTf v = (LawenaViewTf) view;
    Util.selectComboItem(v.getCmbQuality(), Key.dxlevel.getValue(settings),
        ((ValuesValidator) Key.dxlevel.getValidator()).getAllowedValues());
    v.getEnableMotionBlur().setSelected(Key.motionBlur.getValue(settings));
    v.getDisableCombatText().setSelected(Key.noDamageNumbers.getValue(settings));
    v.getDisableCrosshair().setSelected(Key.noCrosshair.getValue(settings));
    v.getDisableCrosshairSwitch().setSelected(Key.noCrosshairSwitch.getValue(settings));
    v.getDisableHitSounds().setSelected(Key.noHitsounds.getValue(settings));
    v.getDisableVoiceChat().setSelected(Key.noVoice.getValue(settings));
    v.getUseHudMinmode().setSelected(Key.hudMinmode.getValue(settings));
    v.getUsePlayerModel().setSelected(Key.hudPlayerModel.getValue(settings));
  }

  @Override
  protected void saveDependentSettings() {
    LawenaViewTf v = (LawenaViewTf) view;
    Key.motionBlur.setValueEx(settings, v.getEnableMotionBlur().isSelected());
    Key.noDamageNumbers.setValueEx(settings, v.getDisableCombatText().isSelected());
    Key.noCrosshair.setValueEx(settings, v.getDisableCrosshair().isSelected());
    Key.noCrosshairSwitch.setValueEx(settings, v.getDisableCrosshairSwitch().isSelected());
    Key.noHitsounds.setValueEx(settings, v.getDisableHitSounds().isSelected());
    Key.noVoice.setValueEx(settings, v.getDisableVoiceChat().isSelected());
    Key.hudMinmode.setValueEx(settings, v.getUseHudMinmode().isSelected());
    Key.hudPlayerModel.setValueEx(settings, v.getUsePlayerModel().isSelected());
  }

  @Override
  protected Frame viewAsFrame() {
    return (LawenaViewTf) view;
  }

  @Override
  public ConfigWriter newConfigWriter() {
    return new ConfigWriterTf(settings);
  }
}
