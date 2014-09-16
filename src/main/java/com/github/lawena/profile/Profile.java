package com.github.lawena.profile;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.github.lawena.util.Util;
import com.google.gson.reflect.TypeToken;

public class Profile implements ValueProvider {

  public static Profile newDefaultProfile() {
    Profile profile = new Profile();
    profile.loadDefaultValues();
    return profile;
  }

  public static Profile newDuplicateProfile(Profile base) {
    Profile profile = new Profile();
    profile.loadValuesFrom(base);
    return profile;
  }

  private String name;
  private Map<String, Object> settings = new LinkedHashMap<>();

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

  public void loadDefaultValues() {
    for (Entry<String, Option<?>> e : Options.getOptions().entrySet()) {
      if (!e.getKey().startsWith("lawena.")) {
        set(e.getKey(), e.getValue().getDefaultValue());
      }
    }
  }

  public void loadValuesFrom(Profile other) {
    for (Entry<String, Object> e : other.settings.entrySet()) {
      if (!e.getKey().startsWith("lawena.")) {
        set(e.getKey(), e.getValue());
      }
    }
  }

}
