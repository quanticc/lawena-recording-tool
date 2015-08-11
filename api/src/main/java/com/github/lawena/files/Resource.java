package com.github.lawena.files;

import java.nio.file.Path;
import java.util.Set;

/**
 * Represents a resource folder (like the ones inside "custom" SteamPipe folder) or file (such as a
 * VPK archive located inside "custom" folder) used to manage user custom content to be used
 * in-game.
 *
 * @author Ivan
 */
public interface Resource {

    /**
     * Get an identifier for this resource to be displayed in user interface components.
     *
     * @return the name of this resource
     */
    String getName();

    /**
     * Get a set of String tags that this resource can relate to. Provides a way to manage special
     * type content without traversing the entire directory structure represented by this resource.
     *
     * @return a set of String tags
     */
    Set<String> getTags();

    /**
     * Define the set of String tags for this resource.
     *
     * @param tags the set of String tags
     */
    void setTags(Set<String> tags);

    /**
     * The exact Path that this resource points to.
     *
     * @return the path for this resource
     */
    Path getPath();

    boolean isEnabled();

    void setEnabled(boolean enabled);

}
