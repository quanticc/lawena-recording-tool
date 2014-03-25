
package com.github.iabarca.lwrt.util;

/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;

import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

public class WatchDir {

    public abstract static class WatchAction {
        public abstract void entryDeleted(Path child);

        public abstract void entryModified(Path child);

        public abstract void entryCreated(Path child);
    }

    private static final Logger log = Logger.getLogger("lawena");

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final Map<Path, WatchAction> actions;
    private final boolean recursive;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Register the given directory with the WatchService
     */
    public void register(Path dir, WatchAction action) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
        actions.put(dir, action);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    public void registerAll(final Path start, final WatchAction action) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir, action);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    public WatchDir(boolean recursive, Path dir, WatchAction action) throws IOException {
        this(recursive);
        if (recursive) {
            registerAll(dir, action);
        } else {
            register(dir, action);
        }
    }

    /**
     * Creates a WatchService
     */
    public WatchDir(boolean recursive) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        this.actions = new HashMap<>();
        this.recursive = recursive;
    }

    /**
     * Process all events for keys queued to the watcher
     */
    public void processEvents() {
        for (;;) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }
            final Path dir = keys.get(key);
            if (dir == null) {
                continue;
            }
            for (WatchEvent<?> event : key.pollEvents()) {
                final Kind<?> kind = event.kind();
                if (kind == OVERFLOW) {
                    log.finer("[watchdir] Overflow event");
                    continue;
                }
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                final Path child = dir.resolve(name);
                log.finer("[watchdir] " + event.kind().name() + ": " + child);
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (kind == ENTRY_CREATE) {
                            actions.get(dir).entryCreated(child);
                        } else if (kind == ENTRY_MODIFY) {
                            actions.get(dir).entryModified(child);
                        } else if (kind == ENTRY_DELETE) {
                            actions.get(dir).entryDeleted(child);
                        }
                    }
                });
                if (recursive && (kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child, actions.get(dir));
                        }
                    } catch (IOException x) {
                    }
                }
            }
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

}
