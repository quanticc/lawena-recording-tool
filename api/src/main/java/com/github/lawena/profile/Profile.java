package com.github.lawena.profile;

import java.util.Optional;

import javafx.beans.Observable;

/**
 * A user profile holding configuration values and read/write capabilities. Allows entities to
 * subscribe to certain events the profile might publish.
 *
 * @author Ivan
 */
public interface Profile extends Observable {

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
     * Get a numeric identifier to give appropriate context for this profile's settings. It is not
     * required to be unique.
     *
     * @return the appId where this profile is supposed to act on
     */
    Integer getAppId();

    /**
     * Set this profile's application identifier.
     *
     * @param appId a new appId for this profile
     */
    void setAppId(Integer appId);

    /**
     * Get a value stored in this profile given a certain key under the "settings" group. Returns an
     * empty {@link Optional} if the key is not defined, otherwise it will contain the corresponding
     * value.
     *
     * @param key a <code>String</code> key where the value is located
     * @return an <code>Optional</code> that may or may not contain the value expected
     */
    Optional<String> get(String key);

    /**
     * Set a value for the given key under the "settings" group. Proper validation must be performed
     * previously as this method is only required to store the value given.
     *
     * @param key   the <code>String</code> key to save the value to
     * @param value a <code>String</code> with value to be stored
     */
    void set(String key, String value);

}
