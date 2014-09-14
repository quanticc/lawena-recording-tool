package com.github.lawena.profile;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

public enum Options {

  INSTANCE;

  private Map<String, Option<?>> options = new HashMap<>();

  public static <T> Option<T> newOption(String key, Class<T> type, T defaultValue) {
    return newOption(key, TypeToken.get(type), defaultValue);
  }

  public static <T> Option<T> newOption(String key, TypeToken<T> type, T defaultValue) {
    Option<T> option = new Option<T>(type);
    option.setKey(key);
    option.setDefaultValue(defaultValue);
    INSTANCE.options.put(key, option);
    return option;
  }

  public static Map<String, Option<?>> getOptions() {
    return INSTANCE.options;
  }

}
