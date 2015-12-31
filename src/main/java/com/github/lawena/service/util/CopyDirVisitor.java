package com.github.lawena.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;

public class CopyDirVisitor extends SimpleFileVisitor<Path> {

    private static final Logger log = LoggerFactory.getLogger(CopyDirVisitor.class);

    private final Path fromPath;
    private final Path toPath;
    private final StandardCopyOption copyOption = StandardCopyOption.REPLACE_EXISTING;
    private final List<Path> excludedPathList;

    public CopyDirVisitor(Path from, Path to) {
        this(from, to, Collections.emptyList());
    }

    public CopyDirVisitor(Path from, Path to, List<Path> excludedPathList) {
        this.fromPath = from;
        this.toPath = to;
        this.excludedPathList = excludedPathList;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Path dest = toPath.resolve(fromPath.relativize(file));
        if (!excludedPathList.contains(file)) {
            mkdirs(dest.getParent());
            log.debug("Copying file: " + file + " -> " + dest);
            Files.copy(file, dest, copyOption);
        }
        return FileVisitResult.CONTINUE;
    }

    private void mkdirs(Path path) throws IOException {
        if (!Files.exists(path)) {
            log.debug("Creating directory: " + path);
            Files.createDirectories(path);
        }
    }
}
