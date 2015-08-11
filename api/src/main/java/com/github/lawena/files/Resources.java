package com.github.lawena.files;

import com.github.lawena.exts.TagProvider;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javafx.beans.property.ListProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

/**
 * A repository for {@link Resource} objects that provides actions over their contents, like
 * creating, updating and removing individual <code>Resource</code>s and scanning directories for
 * potential elements.
 *
 * @author Ivan
 */
public interface Resources {

    /**
     * Scan a given directory for {@link Resource} elements. Only the direct children of
     * <code>dir</code> will be scanned as potential <code>Resource</code>s. Each element found will
     * be processed using {@link #newResource(Path)}.
     *
     * @param dir the directory to perform the scan
     * @return a <code>Set</code> of all <code>Resource</code> elements found in <code>dir</code>
     */
    Set<Resource> scanForResources(Path dir);

    /**
     * Attempt to create a new {@link Resource} for the given <code>path</code> or update it if
     * already exists. It may or may not return a result, indicating if the path pointed to an
     * actual <code>Resource</code> or not.
     *
     * @param path the <code>Path</code> that contains the <code>Resource</code>
     * @return if it could be created or updated, a <code>Resource</code>
     */
    Optional<Resource> newResource(Path path);

    /**
     * Attempt to remove a potential {@link Resource} existing under the given <code>path</code>.
     * This method does not remove any file in the filesystem.
     *
     * @param path the <code>Path</code> that contains the <code>Resource</code>
     * @return <code>true</code> if the <code>Resource</code> existed and could be deleted,
     * <code>false</code> otherwise
     */
    void deleteResource(Path path);

    /**
     * Retrieves the collection of folders under Resource watch.
     *
     * @return a <code>ListProperty</code> of folders currently being watched for changes.
     */
    ListProperty<Path> foldersProperty();

    /**
     * @return
     */
    ObservableList<Path> getFolders();

    /**
     * Retrieves the collection of {@link Resource} objects in this repository.
     *
     * @return a <code>ListProperty</code> of <code>Resource</code> objects currently stored in this
     * repository.
     */
    ListProperty<Resource> resourceListProperty();

    /**
     * @return
     */
    ObservableList<Resource> getResourceList();

    /**
     * @return
     */
    ObservableList<Task<?>> getTasks();

    /**
     * @return
     */
    List<TagProvider> getProviders();

    /**
     * @param providers
     */
    void setProviders(List<TagProvider> providers);

    /**
     *
     */
    void shutdown();

}
