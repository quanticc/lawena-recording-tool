package com.github.lawena.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.os.OSInterface;

public class DeleteDirVisitor extends SimpleFileVisitor<Path> {

  private static final Logger log = LoggerFactory.getLogger(DeleteDirVisitor.class);

  private OSInterface cl;

  public DeleteDirVisitor(OSInterface cl) {
    this.cl = cl;
  }

  @Override
  public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
    try {
      path.toFile().setWritable(true);
      log.trace("Deleting file: " + path);
      Files.delete(path);
    } catch (NoSuchFileException e) {
      log.info("File not found: " + e);
    } catch (IOException e) {
      // attempt to delete custom hud font files that mess up the restore
      if (path.endsWith(".otf") || path.endsWith(".ttf")) {
        log.info("Attempting to delete font file: " + path);
        cl.delete(path);
      } else {
        log.warn("Could not delete file: " + e);
        throw e;
      }
    }
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
    if (exc == null) {
      try {
        log.trace("Deleting folder: " + dir);
        Files.delete(dir);
      } catch (NoSuchFileException e) {
        log.info("File not found: " + e);
      } catch (IOException e) {
        log.warn("Could not delete directory: " + e);
        throw e;
      }
      return FileVisitResult.CONTINUE;
    }
    throw exc;
  }
}
