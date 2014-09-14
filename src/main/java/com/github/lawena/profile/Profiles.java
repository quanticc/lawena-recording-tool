package com.github.lawena.profile;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.util.Util;
import com.google.gson.reflect.TypeToken;

public class Profiles implements ValueProvider {

  private static final Logger log = LoggerFactory.getLogger(Profiles.class);

  private String selected;
  private List<Profile> profiles;
  private Map<String, Object> global;

  public String getSelected() {
    return selected;
  }

  public List<Profile> getProfiles() {
    return profiles;
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
    select(p);
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

  public void select(Profile newSelectedProfile) {
    if (newSelectedProfile == null)
      throw new IllegalArgumentException("Must set the profile");
    String selected = getSelected();
    boolean update = (selected == null ? true : !selected.equals(newSelectedProfile.getName()));
    if (update) {
      selected = newSelectedProfile.getName();
    }
  }

  public void create(String name) {
    Profile p = createNewProfile(name);
    profiles.add(p);
    select(p);
  }

  public void rename(String name, String newName) {
    Profile p;
    if ((p = findByName(name)) == null)
      throw new IllegalArgumentException("Profile '" + name + "' does not exist");
    renameProfile(p, newName);
  }

  public void duplicate(String base) {
    Profile p;
    if ((p = findByName(base)) == null)
      throw new IllegalArgumentException("Profile '" + base + "' does not exist");
    p = duplicateProfile(p);
    profiles.add(p);
    select(p);
  }

  public void duplicate(String base, String newName) {
    Profile p;
    if ((p = findByName(base)) == null)
      throw new IllegalArgumentException("Profile '" + base + "' does not exist");
    p = duplicateProfile(p, newName);
    profiles.add(p);
    select(p);
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
    // no validation checks will be performed at this level
    Util.setToTree(global, key, value);
  }

}
