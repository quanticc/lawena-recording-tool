package com.github.lawena.profile;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import joptsimple.OptionParser;
import joptsimple.OptionSpec;

import com.google.gson.reflect.TypeToken;

public enum Options {

  INSTANCE;

  private Map<String, Option<?>> options = new HashMap<>();
  private OptionParser parser = new OptionParser();

  private OptionSpec<File> profilesFile = parser
      .acceptsAll(Arrays.asList("p", "profiles"), "Location of profiles to load").withRequiredArg()
      .ofType(File.class).defaultsTo(new File("lwrt/tf/profiles.json"));

  public static <T> Option<T> newOption(String key, Class<T> type, T defaultValue) {
    return newOption(key, TypeToken.get(type), defaultValue);
  }

  public static <T> Option<T> newOption(String key, TypeToken<T> type, T defaultValue) {
    Option<T> option = new Option<T>(type, key, defaultValue);
    INSTANCE.options.put(key, option);
    return option;
  }

  public static Map<String, Option<?>> getOptions() {
    return INSTANCE.options;
  }

  public static OptionParser getParser() {
    return INSTANCE.parser;
  }

  public static OptionSpec<File> getProfilesFileOption() {
    return INSTANCE.profilesFile;
  }

}
