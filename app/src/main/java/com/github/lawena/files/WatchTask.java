package com.github.lawena.files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public abstract class WatchTask extends Task<Void> {
    private static final Logger log = LoggerFactory.getLogger(WatchTask.class);
    private final Map<WatchKey, Path> keys;
    private final boolean recursive;
    private final ObservableList<Path> folders;
    private WatchService watcher;
    private ListChangeListener<Path> listener = change -> {
        while (change.next()) {
            change.getAddedSubList().forEach(this::register);
            change.getRemoved().forEach(this::unregister);
        }
    };

    /**
     * Creates a WatchService and registers the given directories
     */
    public WatchTask(ObservableList<Path> dirs, boolean recursive) {
        this.keys = new HashMap<>();
        this.recursive = recursive;
        this.folders = dirs;
        if (recursive) {
            folders.forEach(this::registerAll);
        } else {
            folders.forEach(this::register);
        }
    }

    /**
     * Creates a WatchService and registers the given directories
     */
    public WatchTask(ObservableList<Path> dirs) {
        this(dirs, false);
    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Register the given directory with the WatchService
     */
    public void register(Path dir) {
        if (!keys.containsValue(dir)) {
            try {
                log.debug("Registering {}", dir); //$NON-NLS-1$
                WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                keys.put(key, dir);
            } catch (IOException e) {
                log.warn("Could not register path: {}", dir); //$NON-NLS-1$
            }
        }
    }

    /**
     * Register the given directory, and all its sub-directories, with the WatchService.
     */
    public void registerAll(final Path start) {
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
            log.warn("Could not register path: {}", start); //$NON-NLS-1$
        }
    }

    public void unregister(Path start) {
        log.debug("Unregistering {}", start); //$NON-NLS-1$
        Map<Path, WatchKey> inverse =
                keys.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        Optional.ofNullable(inverse.get(start)).ifPresent(WatchKey::cancel);
    }

    @Override
    protected Void call() throws Exception {
        try {
            while (!isCancelled()) {
                WatchKey key;
                try {
                    key = getWatcher().take();
                } catch (InterruptedException x) {
                    log.debug("Interrupted!");
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
                    log.debug("{}: {}", event.kind().name(), child); //$NON-NLS-1$
                    Platform.runLater(() -> {
                        if (kind == ENTRY_CREATE) {
                            entryCreated(child);
                        } else if (kind == ENTRY_MODIFY) {
                            entryModified(child);
                        } else if (kind == ENTRY_DELETE) {
                            entryDeleted(child);
                        }
                    });
                    if (recursive && (kind == ENTRY_CREATE)) {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                    }
                }
                boolean valid = key.reset();
                if (!valid) {
                    keys.remove(key);
                    if (keys.isEmpty()) {
                        log.debug("Exiting");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Exception!", e);
        }
        return null;
    }

    private WatchService getWatcher() throws IOException {
        if (this.watcher == null) {
            this.watcher = FileSystems.getDefault().newWatchService();
        }
        return this.watcher;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        try {
            getWatcher().close();
        } catch (IOException e) {
            log.warn("Could not close underlying watching service: {}", e.toString()); //$NON-NLS-1$
        }
        return super.cancel(mayInterruptIfRunning);
    }

    @Override
    protected void running() {
        folders.addListener(listener);
        super.running();
    }

    @Override
    protected void done() {
        log.debug("Finished: {}", getState());
        folders.removeListener(listener);
        super.done();
    }

    public abstract void entryModified(Path child);

    public abstract void entryDeleted(Path child);

    public abstract void entryCreated(Path child);

}
