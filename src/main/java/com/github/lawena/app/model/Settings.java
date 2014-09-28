package com.github.lawena.app.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

import joptsimple.OptionSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.profile.Options;
import com.github.lawena.profile.Profile;
import com.github.lawena.profile.ProfileListener;
import com.github.lawena.profile.Profiles;
import com.github.lawena.profile.ValueProvider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class Settings implements ValueProvider {

  private static final Logger log = LoggerFactory.getLogger(Settings.class);

  private final Gson gson = new GsonBuilder().setPrettyPrinting()
      .registerTypeAdapter(Double.class, new JsonSerializer<Double>() {

        @Override
        public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
          if (src == src.intValue())
            return new JsonPrimitive(src.intValue());
          return new JsonPrimitive(src);
        }
      }).create();
  private final OptionSet optionSet;
  private Profiles profiles;
  private List<ProfileListener> listeners = new ArrayList<>();

  public Settings(OptionSet optionSet) {
    this.optionSet = optionSet;
    this.profiles = loadProfiles(Options.getProfilesFileOption().value(optionSet));
  }

  private Profiles loadProfiles(File file) {
    try (Reader reader = new FileReader(file)) {
      return gson.fromJson(reader, Profiles.class);
    } catch (JsonSyntaxException | JsonIOException e) {
      log.warn("Invalid profiles file found, reverting to defaults: " + e);
    } catch (FileNotFoundException e) {
      log.info("No profiles file found, loading defaults");
    } catch (IOException e) {
      log.warn("Problem while reading file, reverting to defaults: " + e);
    }
    return defaultProfiles();
  }

  private Profiles defaultProfiles() {
    return new Profiles();
  }

  @Override
  public <T> T get(TypeToken<T> type, String key) {
    ValueProvider provider = (key.startsWith("lawena.") ? profiles : profiles.getProfile());
    return provider.get(type, key);
  }

  @Override
  public <T> void set(String key, T value) {
    ValueProvider provider = (key.startsWith("lawena.") ? profiles : profiles.getProfile());
    provider.set(key, value);
  }

  public void save() {
    saveProfiles(Options.getProfilesFileOption().value(optionSet));
  }

  private void saveProfiles(File file) {
    try {
      Files.write(file.toPath(), Arrays.asList(gson.toJson(profiles, Profiles.class)),
          Charset.forName("UTF-8"));
    } catch (IOException e) {
      log.warn("Could not save profiles: " + e);
    }
  }

  public Path getParentDataPath() {
    File profilesFile = Options.getProfilesFileOption().value(optionSet);
    return profilesFile.toPath().toAbsolutePath().getParent();
  }

  public void loadDefaultValues() {
    Profile current = profiles.getProfile();
    current.loadDefaultValues();
  }

  public void addProfileListener(ProfileListener listener) {
    listeners.add(listener);
  }

  public boolean removeProfileListener(ProfileListener listener) {
    return listeners.remove(listener);
  }

  public String getSelectedName() {
    return profiles.getSelected();
  }

  public void selectProfile(String selected) {
    if (profiles.select(selected)) {
      fireSelectedProfile();
    }
  }

  private void fireSelectedProfile() {
    if (!listeners.isEmpty()) {
      SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
          for (ProfileListener l : listeners) {
            l.onProfileSelected();
          }
        }
      });
    }
  }
}
