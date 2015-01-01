package com.github.lawena.app;

import java.awt.Component;
import java.awt.Frame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.app.model.ConfigWriter;
import com.github.lawena.app.model.ConfigWriterGo;
import com.github.lawena.app.model.MainModel;
import com.github.lawena.profile.Key;
import com.github.lawena.ui.LawenaViewGo;

public class LawenaGo extends Lawena {

  private static final Logger log = LoggerFactory.getLogger(LawenaGo.class);

  public LawenaGo(MainModel mainModel) {
    super(mainModel);
  }

  @Override
  protected void setupView() {
    view = new LawenaViewGo();
  }

  @Override
  protected void setupIconImage() {
    // TODO Auto-generated method stub

  }

  @Override
  public Component viewAsComponent() {
    return (LawenaViewGo) view;
  }

  @Override
  protected Frame viewAsFrame() {
    return (LawenaViewGo) view;
  }

  @Override
  protected void loadDependentSettings() {
    LawenaViewGo v = (LawenaViewGo) view;
    v.getDisableCasterVoice().setSelected(Key.noCasterVoice.getValue(settings));
  }

  @Override
  protected void saveDependentSettings() {
    LawenaViewGo v = (LawenaViewGo) view;
    Key.noCasterVoice.setValueEx(settings, v.getDisableCasterVoice().isSelected());
  }

  @Override
  public ConfigWriter newConfigWriter() {
    return new ConfigWriterGo(settings);
  }
}
