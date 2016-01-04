package com.github.lawena.service;

import com.github.lawena.domain.Launcher;
import com.github.lawena.domain.Profile;
import com.github.lawena.views.GamePresenter;
import com.github.lawena.views.GameView;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;

import java.util.Optional;

/**
 * A manager for a set of various {@link Profile} objects, providing compatibility with JavaFX properties. At any time
 * there is exactly one profile defined as "selected", which acts as the active point where user settings will be saved
 * to and loaded from.
 *
 * @author Ivan
 */
public interface Profiles {

    /**
     * Obtains the profile object currently selected.
     *
     * @return the selected profile
     */
    Profile getSelected();

    /**
     * Obtains the property for the selected profile name.
     *
     * @return the <code>ObjectProperty</code> for the selected com.github.lawena.profile
     */
    ObjectProperty<Profile> selectedProperty();

    /**
     * Obtains the property related to the list of profiles available.
     *
     * @return the <code>ReadOnlyListProperty</code> of a <code>Profile</code> list
     */
    ListProperty<Profile> profilesProperty();

    /**
     * Obtains the profile that has the provided name, if exists.
     *
     * @param name the name of the profile to be searched
     * @return an <code>Optional</code> for the <code>Profile</code> related to the given name
     */
    Optional<Profile> findByName(String name);

    /**
     * Returns <code>true</code> if the given profile name exists, <code>false</code> otherwise. It is equivalent to
     * returning <p>
     * <pre>
     * findByName(name).isPresent();
     * </pre>
     *
     * @param name the name of the profile to be searched
     * @return <code>true</code> if the given profile name exists, <code>false</code> otherwise
     */
    boolean containsByName(String name);

    /**
     * Creates a new profile and adds it to the list, with default values and the given name.
     *
     * @param appId the id of the application that will give meaning to the values of the profile
     * @param name  the name of the profile. Must be unique or an {@linkplain IllegalArgumentException} will be thrown
     * @return the created <code>Profile</code>
     */
    Profile create(String appId, String name);

    /**
     * Renames the given profile with a new name. New name must be unique. Listeners must be notified of the name
     * change.
     *
     * @param source  the profile to be renamed
     * @param newName the new name of the profile
     */
    void rename(Profile source, String newName);

    /**
     * Creates a new profile copying the values of the given one. The name will be based on the original profile name.
     *
     * @param source the profile to be duplicated
     */
    void duplicate(Profile source);

    /**
     * Creates a new profile copying the values of the given one and the provided name.
     *
     * @param source  the profile to be duplicated
     * @param newName the name used for the profile copy
     */
    void duplicate(Profile source, String newName);

    /**
     * Removes the given profile from the repository.
     *
     * @param profile the profile to be deleted
     * @return <code>true</code> if the operation was successful, <code>false</code> otherwise.
     */
    boolean remove(Profile profile);

    ListProperty<Launcher> launchersProperty();

    ObservableList<Launcher> getLaunchers();

    Optional<Launcher> getLauncher(Profile profile);

    Optional<Launcher> findLauncherByName(String launcherName);

    GameView getView(Launcher launcher);

    GamePresenter getPresenter(Launcher launcher);

}
