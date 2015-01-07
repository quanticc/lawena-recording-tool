package com.github.lawena.profile;

/**
 * Represents a class that should listen to changes to a profile or profile list change.
 * 
 * @author Ivan
 *
 */
public interface ProfileListener {

  public void onProfileSelected();

  public void onProfileListUpdated();

}
