package com.github.lawena.files;

import com.github.lawena.exts.TagProvider;
import com.github.lawena.i18n.Messages;
import com.github.lawena.vpk.Archive;
import com.github.lawena.vpk.ArchiveException;
import com.github.lawena.vpk.Directory;
import com.github.lawena.vpk.EntryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class AppResources implements Resources {
    private static final Logger log = LoggerFactory.getLogger(AppResources.class);

    private ListProperty<Path> folders = new SimpleListProperty<>(this, "folders", //$NON-NLS-1$
            FXCollections.observableArrayList());
    private ListProperty<Resource> resourceList = new SimpleListProperty<>(this,
            "resources", FXCollections.observableArrayList()); //$NON-NLS-1$
    private ObservableList<Task<?>> tasks = FXCollections.observableArrayList();
    // use an empty list as a fallback in case the controller forgot to set one
    private List<TagProvider> providers = Collections.emptyList();

    private WatchService watcher = new WatchService(this);
    private ListChangeListener<Path> listener = change -> {
        while (change.next()) {
            if (change.wasAdded()) {
                scheduleScan(change.getAddedSubList());
            }
            if (change.wasRemoved()) {
                List<? extends Path> removed = change.getRemoved();
                Platform.runLater(() -> resourceList.get().removeIf(r -> removed.contains(r.getPath())));
            }
        }
    };

    public AppResources() {
        folders.get().addListener(listener);
        watcher.restart();
    }

    private static List<String> getContents(Resource resource) throws IOException {
        final Path start = resource.getPath().toAbsolutePath();
        final List<String> contents = new ArrayList<>();
        if (Files.isDirectory(start)) {
            Files.walkFileTree(start, EnumSet.noneOf(FileVisitOption.class), 3,
                    new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                throws IOException {
                            contents.add(start.toAbsolutePath().relativize(file.toAbsolutePath()).toString()
                                    .replace('\\', '/'));
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } else if (start.toString().toLowerCase().endsWith(".vpk")) { //$NON-NLS-1$
            try {
                Archive vpk = new Archive(start.toFile());
                vpk.load();
                for (Directory dir : vpk.getDirectories()) {
                    contents.addAll(dir.getEntries().stream().map(entry -> dir.getPath() + "/" + entry.getFullName()).collect(Collectors.toList()));
                }
            } catch (ArchiveException | EntryException e) {
                throw new IOException("Invalid VPK archive", e); //$NON-NLS-1$
            }
        }
        log.trace("Contents found for {}: {}", start, contents); //$NON-NLS-1$
        return contents;
    }

    @Override
    public Set<Resource> scanForResources(Path dir) {
        Set<Resource> newResourceList = new HashSet<>();
        // path must be a folder -- no vpk files at this stage!
        if (!Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS)) {
            log.warn("Skipping invalid or non existing path: {}", dir); //$NON-NLS-1$
        } else {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path path : stream) {
                    // each resource must be a folder or a vpk file
                    newResource(path).ifPresent(newResourceList::add);
                }
            } catch (IOException e) {
                log.warn("Problem while loading custom paths: {}", e.toString()); //$NON-NLS-1$
            }
        }
        return newResourceList;
    }

    private void scheduleScan(List<? extends Path> list) {
        Task<Set<Resource>> task = new ScanTask(list) {

            @Override
            public Set<Resource> scan(Path dir) {
                updateMessage(String.format(Messages.getString("AppResources.ScanningResources"), dir)); //$NON-NLS-1$
                return scanForResources(dir);
            }

        };
        tasks.add(task);
    }

    @Override
    public Optional<Resource> newResource(Path start) {
        // get the resource for this path or create a new one if it doesn't exist
        Optional<Resource> optional =
                resourceList.get().stream().filter(r -> r.getPath().equals(start)).findFirst();
        Resource resource;
        if (!optional.isPresent()) {
            resource = new AppResource(start);
            Platform.runLater(() -> resourceList.get().add(resource));
        } else {
            resource = optional.get();
        }
        try {
            List<String> contents = getContents(resource);
            Set<String> tags =
                    providers.stream().flatMap(p -> p.tag(contents).stream()).distinct()
                            .collect(Collectors.toSet());
            Platform.runLater(() -> resource.setTags(tags));
            return Optional.of(resource);
        } catch (IOException e) {
            log.warn("Could not create resource at {}: {}", start, e.toString()); //$NON-NLS-1$
        }
        return Optional.empty();
    }

    @Override
    public void deleteResource(Path path) {
        Platform.runLater(() -> resourceList.get().removeIf(r -> r.getPath().equals(path)));
    }

    @Override
    public final ListProperty<Path> foldersProperty() {
        return this.folders;
    }

    @Override
    public final ObservableList<Path> getFolders() {
        return this.foldersProperty().get();
    }

    public final void setFolders(final ObservableList<Path> folders) {
        this.foldersProperty().set(folders);
    }

    @Override
    public final ListProperty<Resource> resourceListProperty() {
        return this.resourceList;
    }

    @Override
    public final ObservableList<Resource> getResourceList() {
        return this.resourceListProperty().get();
    }

    public final void setResourceList(final ObservableList<Resource> resources) {
        this.resourceListProperty().set(resources);
    }

    @Override
    public List<TagProvider> getProviders() {
        return providers;
    }

    @Override
    public void setProviders(List<TagProvider> providers) {
        this.providers = providers;
    }

    @Override
    public ObservableList<Task<?>> getTasks() {
        return tasks;
    }

    @Override
    public void shutdown() {
        folders.get().removeListener(listener);
        Platform.runLater(watcher::cancel);
    }

}
