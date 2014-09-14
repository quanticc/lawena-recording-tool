package com.github.lawena.app.model;

import com.github.lawena.profile.Profiles;
import com.github.lawena.profile.ValueProvider;
import com.google.gson.reflect.TypeToken;

public class Settings implements ValueProvider {

  private Profiles profiles;

  @Override
  public <T> T get(TypeToken<T> type, String key) {
    if (key.startsWith("lawena.")) {
      return profiles.get(type, key);
    } else {
      return profiles.getProfile().get(type, key);
    }
  }

  @Override
  public <T> void set(String key, T value) {
    if (key.startsWith("lawena.")) {
      profiles.set(key, value);
    } else {
      profiles.getProfile().set(key, value);
    }
  }

}
