package com.github.lawena.app.model;

import java.io.File;
import java.io.FileNotFoundException;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.profile.Profile;
import com.github.lawena.profile.ProfileListener;
import com.github.lawena.profile.Profiles;
import com.github.lawena.profile.Provider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class Settings implements Provider {

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

  private Profiles profiles;
  private Profiles defaults;
  private File profilesFile;
  private File defaultFile;
  List<ProfileListener> listeners = new ArrayList<>();

  public Settings(File profilesFile, File defaultFile) throws IOException {
    this.profilesFile = profilesFile;
    this.defaultFile = defaultFile;
    this.profiles = loadProfiles();
    this.defaults = readJsonProfile(defaultFile);
  }

  private Profiles loadProfiles() throws IOException {
    Profiles p = null;
    try {
      p = readJsonProfile(profilesFile);
    } catch (JsonSyntaxException | JsonIOException e) {
      log.warn("Using default profile due to invalid format: ", e.toString()); //$NON-NLS-1$
    } catch (FileNotFoundException e) {
      log.info("Using default profile due to {} file missing", profilesFile); //$NON-NLS-1$
    } catch (IOException e) {
      log.warn("Using default profile: {}", e.toString()); //$NON-NLS-1$
    }
    return p == null ? readJsonProfile(defaultFile) : p;
  }

  private Profiles readJsonProfile(File file) throws IOException {
    try (Reader reader = Files.newBufferedReader(file.toPath(), Charset.forName("UTF-8"))) { //$NON-NLS-1$
      return gson.fromJson(reader, Profiles.class);
    }
  }

  @Override
  public <T> T get(TypeToken<T> type, String key) {
    Provider provider = (key.startsWith("lawena.") ? profiles : profiles.getProfile()); //$NON-NLS-1$
    return provider.get(type, key);
  }

  @Override
  public <T> T getDefault(TypeToken<T> type, String key) {
    Provider provider = (key.startsWith("lawena.") ? defaults : defaults.getProfile()); //$NON-NLS-1$
    return provider.get(type, key);
  }

  @Override
  public <T> void set(String key, T value) {
    Provider provider = (key.startsWith("lawena.") ? profiles : profiles.getProfile()); //$NON-NLS-1$
    provider.set(key, value);
  }

  public void save() {
    saveProfiles(profilesFile);
  }

  private void saveProfiles(File file) {
    try {
      Files.write(file.toPath(), Arrays.asList(gson.toJson(profiles, Profiles.class)),
          Charset.forName("UTF-8")); //$NON-NLS-1$
    } catch (IOException e) {
      log.warn("Could not save profiles: " + e); //$NON-NLS-1$
    }
  }

  public Path getParentDataPath() {
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
