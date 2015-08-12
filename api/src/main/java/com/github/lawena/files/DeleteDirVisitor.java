package com.github.lawena.files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class DeleteDirVisitor extends SimpleFileVisitor<Path> {
    private static final Logger log = LoggerFactory.getLogger(DeleteDirVisitor.class);

    @Override
    public final FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        try {
            path.toFile().setWritable(true);
            log.debug("Deleting file: " + path);
            Files.delete(path);
        } catch (NoSuchFileException e) {
            log.debug("File does not exist: " + e);
        } catch (IOException e) {
            // attempt to delete custom hud font files that mess up the restore
            if (path.endsWith(".otf") || path.endsWith(".ttf")) {
                log.info("Attempting to delete font file: " + path);
                // TODO: add OS level delete
                //cl.delete(path);
            } else {
                log.warn("Could not delete file: " + e);
                throw e;
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public final FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (exc == null) {
            try {
                log.debug("Deleting folder: " + dir);
                Files.delete(dir);
            } catch (NoSuchFileException e) {
                log.debug("File does not exist: " + e);
            } catch (IOException e) {
                log.warn("Could not delete directory: " + e);
                throw e;
            }
            return FileVisitResult.CONTINUE;
        }
        throw exc;
    }
}
