package com.github.lawena.util;

/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * 
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 * 
 * - Neither the name of Oracle nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WatchDir {

  private static final Logger log = LoggerFactory.getLogger(WatchDir.class);

  private final WatchService watcher;
  private final Map<WatchKey, Path> keys;
  private final boolean recursive;

  @SuppressWarnings("unchecked")
  static <T> WatchEvent<T> cast(WatchEvent<?> event) {
    return (WatchEvent<T>) event;
  }

  /**
   * Register the given directory with the WatchService
   */
  private void register(Path dir) throws IOException {
    WatchKey key =
        dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
    keys.put(key, dir);
  }

  /**
   * Register the given directory, and all its sub-directories, with the WatchService.
   */
  private void registerAll(final Path start) throws IOException {
    Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
          throws IOException {
        register(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  /**
   * Creates a WatchService and registers the given directory
   */
  public WatchDir(Path dir, boolean recursive) throws IOException {
    this.watcher = FileSystems.getDefault().newWatchService();
    this.keys = new HashMap<>();
    this.recursive = recursive;
    if (recursive) {
      registerAll(dir);
    } else {
      register(dir);
    }
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
      Path dir = keys.get(key);
      if (dir == null) {
        continue;
      }
      for (WatchEvent<?> event : key.pollEvents()) {
        final Kind<?> kind = event.kind();
        if (kind == StandardWatchEventKinds.OVERFLOW) {
          continue;
        }
        WatchEvent<Path> ev = cast(event);
        Path name = ev.context();
        final Path child = dir.resolve(name);
        log.trace("[watchdir] " + event.kind().name() + ": " + child);
        SwingUtilities.invokeLater(new Runnable() {

          @Override
          public void run() {
            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
              entryCreated(child);
            } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
              entryModified(child);
            } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
              entryDeleted(child);
            }
          }
        });
        if (recursive && (kind == StandardWatchEventKinds.ENTRY_CREATE)) {
          try {
            if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
              registerAll(child);
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

  public abstract void entryDeleted(Path child);

  public abstract void entryModified(Path child);

  public abstract void entryCreated(Path child);

}
