package com.github.lawena.exts;

import com.github.lawena.Controller;
import com.github.lawena.profile.Profile;

import org.controlsfx.validation.ValidationResult;

import ro.fortsoft.pf4j.ExtensionPoint;

/**
 * Extension to modify and enrich the standard user interface.
 *
 * @author Ivan
 */
public interface ViewProvider extends ExtensionPoint {

    /**
     * An identifier apps will use to determine what UI extensions will be displayed. The extension
     * must try to make the name unique, otherwise the behavior is undefined. Each app will define
     * the set of extensions used in the "modules" list.
     *
     * @return the identifier name for this extension
     */
    String getName();

    /**
     * Initialize required objects for this extension. It is advised to keep the controller
     * reference.
     *
     * @param controller - a reference to the controller that is using this extension
     */
    void init(Controller controller);

    /**
     * Creates the needed UI controls. Called only once when a profile related to this object is
     * selected.
     */
    void install();

    /**
     * Removes all created UI controls from the controller. Must be called as part of the cleanup
     * sequence in case the Plugin containing this extension is stopped.
     */
    void delete();

    /**
     * Binds the UI controls created by {@link #install()} to the given profile data. Can also act
     * as a load action, retrieving the given profile values and updating each control value
     * accordingly. Called every time a profile related to this object is selected.
     *
     * @param profile the profile that should be bound to the UI controls
     */
    void bind(Profile profile);

    /**
     * Unbinds the UI controls to the given profile data. Can also act as a save action, retrieving
     * all current control values and saving them directly to profile data. Called every time a
     * profile related to this object is deselected and also as a cleanup sequence in case the
     * Plugin containing this extension is stopped.
     *
     * @param profile the profile that should no longer be bound to the UI controls
     */
    void unbind(Profile profile);

    /**
     * Performs validation on the entire state of the given <code>profile</code>, to prepare the
     * application for launch. Returns a result object that collects all validation messages,
     * warnings and errors present in the current state
     *
     * @param profile - the <code>Profile</code> being validated
     * @return a <code>ValidationResult</code> object encapsulating state about the validation
     * @see ValidationResult
     */
    ValidationResult validate(Profile profile);

    /**
     * Obtains the list of <code>Path</code>s this extension depends on. This list should be built
     * using settings from the given <code>profile</code> and it is called by the application on
     * launch to make sure the provided folders are relieved of active file handles or locking
     * mechanisms that might prevent the file replacing stage of the launch process.
     *
     * @param profile - the <code>Profile</code> used in the launch process
     * @return a list, that might be empty but never <code>null</code> containing <code>Path</code>
     * objects representing directories or files
     */
//    List<Path> dependentFolders(Profile profile);

    /**
     * Return a list of <code>Path</code> representing launch related files parametrized by the given
     * <code>profile</code>. It must not perform any modification to the user's game files. All paths
     * returned will be copied or linked to an intermediate "launch" folder before the actual user
     * file replacing process begins.
     *
     * @param profile - the <code>Profile</code> used for the replacing parameters
     * @return a <code>List</code> of <code>Path</code>s representing the files required by the user
     * for the current launch instance
     * @throws LawenaException if the process could not be completed due to (but not limited to):
     *                         folders under use, not enough permissions, missing folders or missing files
     */
//    List<Path> launchPathList(Profile profile) throws LawenaException;

}
