package com.github.lawena.domain;

import java.util.Optional;

/**
 * A user profile holding configuration values and read/write capabilities. Allows entities to subscribe to certain
 * events the profile might publish.
 *
 * @author Ivan
 */
public interface Profile {

    /**
     * Get the identifying name of this profile. It must be unique within a profile container.
     *
     * @return the profile identifier
     */
    String getName();

    /**
     * Rename the profile, notifying all observers.
     *
     * @param name a new profile name
     */
    void setName(String name);

    /**
     * Get an identifier to the game description that is associated with this profile
     *
     * @return the game description identifier this profile is linked to
     */
    String getLauncher();

    /**
     * Set this profile's application identifier.
     *
     * @param launcherName a new launcher name for this profile
     */
    void setLauncher(String launcherName);

    /**
     * Get a value stored in this profile given a certain key under the "settings" group. Returns an empty {@link
     * Optional} if the key is not defined, otherwise it will contain the corresponding value.
     *
     * @param key a <code>String</code> key where the value is located
     * @return an <code>Optional</code> that may or may not contain the value expected
     */
    Optional<Object> get(String key);

    /**
     * Set a value for the given key under the "settings" group. Proper validation must be performed previously as this
     * method is only required to store the value given.
     *
     * @param key   the <code>String</code> key to save the value to
     * @param value the value to be stored
     */
    void set(String key, Object value);

    /**
     * Returns a complete String representation of this profile.
     *
     * @return a String representation of all serializable fields in this profile
     */
    String toFullString();

}
