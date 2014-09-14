package com.github.lawena.profile;

import java.util.Map;
import java.util.Map.Entry;

import com.github.lawena.util.Util;
import com.google.gson.reflect.TypeToken;

public class Profile implements ValueProvider {

  public static Profile newDefaultProfile() {
    Profile profile = new Profile();
    for (Entry<String, Option<?>> e : Options.getOptions().entrySet()) {
      profile.settings.put(e.getKey(), e.getValue().getDefaultValue());
    }
    return profile;
  }

  public static Profile newDuplicateProfile(Profile base) {
    Profile profile = new Profile();
    for (Entry<String, Object> e : base.settings.entrySet()) {
      profile.settings.put(e.getKey(), e.getValue());
    }
    return profile;
  }

  private String name;
  private Map<String, Object> settings;

  private Profile() {}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public <T> T get(TypeToken<T> type, String key) {
    return Util.getFromTree(settings, type, key);
  }

  @Override
  public <T> void set(String key, T value) {
    Util.setToTree(settings, key, value);
  }

}
