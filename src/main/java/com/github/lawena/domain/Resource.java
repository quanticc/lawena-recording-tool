package com.github.lawena.domain;

import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import java.nio.file.Path;

/**
 * Represents a resource folder (like the ones inside "custom" SteamPipe folder) or file (such as a VPK archive located
 * inside "custom" folder) used to manage user custom content to be used in-game.
 *
 * @author Ivan
 */
public interface Resource extends Comparable<Resource> {

    /**
     * Get an identifier for this resource to be displayed in user interface components.
     *
     * @return the name of this resource
     */
    String getName();

    /**
     * Get a set of String tags that this resource can relate to. Provides a way to manage special type content without
     * traversing the entire directory structure represented by this resource.
     *
     * @return a set of String tags
     */
    ObservableSet<Tag> getTags();

    /**
     * Define the set of String tags for this resource.
     *
     * @param tags the set of String tags
     */
    void setTags(ObservableSet<Tag> tags);

    /**
     * The exact Path that this resource points to.
     *
     * @return the path for this resource
     */
    Path getPath();

    boolean isEnabled();

    void setEnabled(boolean enabled);

    @Override
    default int compareTo(Resource o) {
        if (getPath() == null ^ o.getPath() == null) {
            return getPath() == null ? -1 : 1;
        }
        if (getPath() == null && o.getPath() == null) {
            return 0;
        }
        return getPath().compareTo(o.getPath());
    }

    ObservableList<String> getExcludedPaths();

    void setExcludedPaths(ObservableList<String> excludedPaths);
}
