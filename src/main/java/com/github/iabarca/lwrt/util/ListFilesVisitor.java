
package com.github.iabarca.lwrt.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class ListFilesVisitor extends SimpleFileVisitor<Path> {

    private List<String> files = new ArrayList<>();
    private Path start;

    public ListFilesVisitor(Path start) {
        this.start = start;
    }

    public List<String> getFiles() {
        return files;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String str = start.toAbsolutePath().relativize(file.toAbsolutePath()).toString()
                .replace('\\', '/');
        files.add(str);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.TERMINATE;
    }

}
