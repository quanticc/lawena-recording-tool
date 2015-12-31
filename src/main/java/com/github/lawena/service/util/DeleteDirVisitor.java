package com.github.lawena.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static com.github.lawena.util.LwrtUtils.newProcessReader;

public class DeleteDirVisitor extends SimpleFileVisitor<Path> {

    private static final Logger log = LoggerFactory.getLogger(DeleteDirVisitor.class);

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        try {
            path.toFile().setWritable(true);
            log.debug("Deleting file: {}", path);
            Files.delete(path);
        } catch (NoSuchFileException e) {
            log.debug("File does not exist: {}", e.toString());
        } catch (IOException e) {
            // attempt to delete custom hud font files that mess up the restore
            if (path.endsWith(".otf") || path.endsWith(".ttf")) {
                log.info("Attempting to delete font file: {}", path);
                nativeDelete(path);
            } else {
                log.warn("Could not delete file: {}", e);
                throw e;
            }
        }
        return FileVisitResult.CONTINUE;
    }

    private void nativeDelete(Path path) {
        try {
            ProcessBuilder pb = new ProcessBuilder("del", "/f", "/s", "/q", "/a", path.toString());
            Process pr = pb.start();
            try (BufferedReader input = newProcessReader(pr)) {
                String line;
                while ((line = input.readLine()) != null) {
                    log.info("[delete] {}", line);
                }
            }
            pr.waitFor();
        } catch (InterruptedException | IOException e) {
            log.warn("Could not delete file: {}", e.toString());
        }
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (exc == null) {
            try {
                log.debug("Deleting folder: {}", dir);
                Files.delete(dir);
            } catch (NoSuchFileException e) {
                log.debug("File does not exist: {}", e.toString());
            } catch (IOException e) {
                log.warn("Could not delete directory: {}", e.toString());
                throw e;
            }
            return FileVisitResult.CONTINUE;
        }
        throw exc;
    }
}
