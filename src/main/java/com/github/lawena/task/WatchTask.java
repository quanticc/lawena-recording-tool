package com.github.lawena.task;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.*;

public abstract class WatchTask extends Task<Void> {
    private static final Logger log = LoggerFactory.getLogger(WatchTask.class);

    private final Map<WatchKey, Path> keys = new HashMap<>();
    private final ListProperty<Path> foldersProperty;
    private final ListChangeListener<Path> listener = change -> {
        while (change.next()) {
            if (change.wasAdded()) {
                CompletableFuture.supplyAsync(change::getAddedSubList, Platform::runLater).thenAccept(this::registerAll);
            }
            if (change.wasRemoved()) {
                CompletableFuture.supplyAsync(change::getRemoved, Platform::runLater).thenAccept(this::unregisterAll);
            }
        }
    };
    private WatchService watchService;

    public WatchTask(ListProperty<Path> foldersProperty) {
        this.foldersProperty = foldersProperty;
    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    public void registerAll(List<? extends Path> pathList) {
        pathList.forEach(this::register);
    }

    /**
     * Register the given directory with the WatchService
     */
    public void register(Path dir) {
        if (!keys.containsValue(dir)) {
            try {
                log.debug("Watching for changes: {}", dir);
                WatchKey key = dir.register(getWatchService(), ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                keys.put(key, dir);
            } catch (IOException e) {
                log.warn("Could not register path: {}", dir);
            }
        }
    }

    /**
     * Register the given directory, and all its sub-directories, with the WatchService.
     */
    public void registerWithSubdirectories(final Path start) {
        try {
            Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException {
                    register(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.warn("Could not register path: {}", start);
        }
    }

    public void unregisterAll(List<? extends Path> pathList) {
        pathList.forEach(this::unregister);
    }

    public void unregister(Path start) {
        log.debug("No longer watching: {}", start);
        Map<Path, WatchKey> inverse =
                keys.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        Optional.ofNullable(inverse.get(start)).ifPresent(WatchKey::cancel);
    }

    @Override
    protected Void call() throws Exception {
        try {
            CompletableFuture.supplyAsync(foldersProperty::get, Platform::runLater).thenAccept(this::registerAll);
            while (!isCancelled()) {
                WatchKey key;
                try {
                    key = getWatchService().take();
                } catch (ClosedWatchServiceException | InterruptedException x) {
                    log.debug("Terminating directory watch task");
                    return null;
                }
                Path dir = keys.get(key);
                if (dir == null) {
                    continue;
                }
                for (WatchEvent<?> event : key.pollEvents()) {
                    final Kind<?> kind = event.kind();
                    if (kind == OVERFLOW) {
                        continue;
                    }
                    WatchEvent<Path> ev = cast(event);
                    Path name = ev.context();
                    final Path child = dir.resolve(name);
                    log.debug("{}: {}", event.kind().name(), child);
                    if (kind == ENTRY_CREATE) {
                        entryCreated(child);
                    } else if (kind == ENTRY_MODIFY) {
                        entryModified(child);
                    } else if (kind == ENTRY_DELETE) {
                        entryDeleted(child);
                    }
//                    if (recursive && (kind == ENTRY_CREATE)) {
//                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
//                            registerAll(child);
//                        }
//                    }
                }
                boolean valid = key.reset();
                if (!valid) {
                    keys.remove(key);
                }
            }
        } catch (Exception e) {
            log.warn("Exception while watching for changes", e);
        }
        return null;
    }

    public WatchService getWatchService() {
        if (watchService == null) {
            try {
                watchService = FileSystems.getDefault().newWatchService();
            } catch (IOException e) {
                log.warn("Could not create a watch service: ", e.toString());
            }
        }
        return watchService;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        try {
            getWatchService().close();
        } catch (IOException e) {
            log.warn("Could not close underlying watching service: {}", e.toString());
        }
        return super.cancel(mayInterruptIfRunning);
    }

    @Override
    protected void running() {
        foldersProperty.addListener(listener);
        super.running();
    }

    @Override
    protected void done() {
        foldersProperty.removeListener(listener);
        super.done();
    }

    public abstract void entryModified(Path child);

    public abstract void entryDeleted(Path child);

    public abstract void entryCreated(Path child);

}
