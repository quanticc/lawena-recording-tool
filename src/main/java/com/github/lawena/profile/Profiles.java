package com.github.lawena.profile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.util.Util;
import com.google.gson.reflect.TypeToken;

@SuppressWarnings("nls")
public class Profiles implements Provider {

  private static final Logger log = LoggerFactory.getLogger(Profiles.class);

  private String _selected = "Default";
  private List<Profile> profiles = new ArrayList<>();
  private Map<String, Object> global = new LinkedHashMap<>();

  public Profiles() {
    for (Entry<String, Option<?>> e : Options.getOptions().entrySet()) {
      if (e.getKey().startsWith("lawena.")) {
        set(e.getKey(), e.getValue().getDefaultValue());
      }
    }
  }

  public String getSelected() {
    return _selected;
  }

  private List<Profile> getProfiles() {
    return profiles;
  }

  public List<String> getProfileNames() {
    List<String> list = new ArrayList<>();
    for (Profile p : profiles) {
      list.add(p.getName());
    }
    return list;
  }

  public Profile getProfile() {
    String defProfileName = "Default";
    Profile p = findByName(getSelected());
    if (p == null) {
      if ((p = findByName(defProfileName)) == null) {
        if (!getProfiles().isEmpty()) {
          p = getProfiles().get(0);
        } else {
          log.debug("Creating default profile");
          p = createNewProfile(defProfileName);
          profiles.add(p);
        }
      }
    }
    selectProfile(p);
    return p;
  }

  public Profile findByName(String name) {
    for (Profile profile : getProfiles()) {
      if (profile.getName().equals(name)) {
        return profile;
      }
    }
    return null;
  }

  public boolean containsByName(String name) {
    return findByName(name) != null;
  }

  public boolean select(String name) {
    Profile p;
    if ((p = findByName(name)) == null)
      throw new IllegalArgumentException("Profile '" + name + "' does not exist");
    return selectProfile(p);
  }

  private boolean selectProfile(Profile newSelectedProfile) {
    if (newSelectedProfile == null)
      throw new IllegalArgumentException("Profile must not be null");
    if (!profiles.contains(newSelectedProfile))
      throw new IllegalArgumentException("Profile must exist");
    String selected = getSelected();
    boolean update = (selected == null ? true : !selected.equals(newSelectedProfile.getName()));
    if (update) {
      _selected = newSelectedProfile.getName();
    }
    return update;
  }

  /**
   * Creates a new profile and adds it to the list, with default values and the given name.
   * 
   * @param name - the name of the profile. Must be unique or an
   *        {@linkplain IllegalArgumentException} will be thrown
   */
  public void create(String name) {
    Profile p = createNewProfile(name);
    profiles.add(p);
    selectProfile(p);
  }

  /**
   * Renames the given profile with a new name. New name must be unique.
   * 
   * @param name - the name of the profile to be renamed
   * @param newName - the new name of the profile
   */
  public void rename(String name, String newName) {
    Profile p;
    if ((p = findByName(name)) == null)
      throw new IllegalArgumentException("Profile '" + name + "' does not exist");
    renameProfile(p, newName);
  }

  /**
   * Creates a new profile copying the values of the given one.
   * 
   * @param base - the name of the original profile
   */
  public void duplicate(String base) {
    Profile p;
    if ((p = findByName(base)) == null)
      throw new IllegalArgumentException("Profile '" + base + "' does not exist");
    p = duplicateProfile(p);
    profiles.add(p);
    selectProfile(p);
  }

  public void duplicate(String base, String newName) {
    Profile p;
    if ((p = findByName(base)) == null)
      throw new IllegalArgumentException("Profile '" + base + "' does not exist");
    p = duplicateProfile(p, newName);
    profiles.add(p);
    selectProfile(p);
  }

  private Profile createNewProfile(String newProfileName) {
    if (newProfileName == null)
      throw new IllegalArgumentException("Must set a name");
    Profile p = Profile.newDefaultProfile();
    renameProfile(p, newProfileName);
    return p;
  }

  private void renameProfile(Profile profile, String newProfileName) {
    if (profile == null)
      throw new IllegalArgumentException("Must set a profile");
    if (newProfileName == null)
      throw new IllegalArgumentException("Must set a name");
    Profile find = findByName(newProfileName);
    if (find != null && !find.equals(profile)) {
      throw new IllegalArgumentException("Profile name '" + newProfileName + "' already exists");
    } else {
      profile.setName(newProfileName);
    }
  }

  private Profile duplicateProfile(Profile baseProfile) {
    if (baseProfile == null)
      throw new IllegalArgumentException("Must set a profile");
    Profile newProfile = Profile.newDuplicateProfile(baseProfile);
    renameProfile(newProfile, findAvailableNameFrom(baseProfile.getName()));
    return newProfile;
  }

  private String findAvailableNameFrom(String src) {
    String dest = src + " - Copy";
    int count = 2;
    while (findByName(dest) != null) {
      dest = src + " - Copy (" + count + ")";
      count++;
    }
    return dest;
  }

  private Profile duplicateProfile(Profile baseProfile, String newProfileName) {
    if (baseProfile == null)
      throw new IllegalArgumentException("Must set a profile");
    if (newProfileName == null)
      throw new IllegalArgumentException("Must set a name");
    Profile newProfile = Profile.newDuplicateProfile(baseProfile);
    renameProfile(newProfile, newProfileName);
    return newProfile;
  }

  @Override
  public <T> T get(TypeToken<T> type, String key) {
    return Util.getFromTree(global, type, key);
  }

  @Override
  public <T> void set(String key, T value) {
    Util.setToTree(global, key, value);
  }

}
