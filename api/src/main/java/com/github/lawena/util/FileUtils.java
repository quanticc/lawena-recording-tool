package com.github.lawena.util;

import com.github.lawena.files.CopyDirVisitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class FileUtils {
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    private static final FileVisitor<Path> deleteVisitor = new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            removeLink(file);
            if (Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
                file.toFile().setWritable(true);
                Files.delete(file);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc == null) {
                removeLink(dir);
                if (Files.exists(dir, LinkOption.NOFOLLOW_LINKS)) {
                    Files.delete(dir);
                }
            } else {
                throw exc;
            }
            return FileVisitResult.CONTINUE;
        }
    };

    private FileUtils() {
    }

    public static Path copy(Path from, Path to) throws IOException {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        return Files.walkFileTree(from, new CopyDirVisitor(from, to, false));
    }

    public static Path copyReadOnly(Path from, Path to) throws IOException {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        return Files.walkFileTree(from, new CopyDirVisitor(from, to, true));
    }

    public static Path copy(Path from, Path to, DirectoryStream.Filter<Path> filter) throws IOException {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        Objects.requireNonNull(filter);
        return Files.walkFileTree(from, new CopyDirVisitor(from, to, false, filter));
    }

    public static void delete(Path start) throws IOException {
        Objects.requireNonNull(start);
        Files.walkFileTree(start, deleteVisitor);
    }

    public static Path link(Path link, Path target) throws IOException {
        Objects.requireNonNull(link);
        Objects.requireNonNull(target);
        String osName = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
        if (osName.contains("windows")) { //$NON-NLS-1$
            if (!LwrtUtils.isAdmin()) {
                copy(target, link);
            } else {
                List<String> command;
                if (Files.isDirectory(target)) {
                    command = Arrays.asList("cmd", "/c", "mklink", "/d", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                            link.toAbsolutePath().toString(), target.toAbsolutePath().toString());
                } else {
                    command = Arrays.asList("cmd", "/c", "mklink", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            link.toAbsolutePath().toString(), target.toAbsolutePath().toString());
                }
                // TODO: warn if return != 0
                startProcess(command);
            }
        } else {
            Files.createSymbolicLink(link, target);
        }
        return link;
    }

    private static void removeLink(Path link) throws IOException {
        if (Files.exists(link, LinkOption.NOFOLLOW_LINKS)) {
            if (isSymbolicLink(link)) {
                String osName = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
                if (osName.contains("windows") && LwrtUtils.isAdmin()) { //$NON-NLS-1$
                    List<String> command;
                    if (Files.isDirectory(link, LinkOption.NOFOLLOW_LINKS)) {
                        command = Arrays.asList("cmd", "/c", "rd", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                link.toAbsolutePath().toString());
                    } else {
                        command = Arrays.asList("cmd", "/c", "del", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                link.toAbsolutePath().toString());
                    }
                    startProcess(command);
                } else {
                    // TODO: to be implemented
                    log.warn("Operation not implemented");
                }
            }
        }
    }

    public static boolean isSymbolicLink(Path path) throws IOException {
        if (Files.isSymbolicLink(path)) {
            return true;
        } else {
            List<String> command = Arrays.asList("cmd", "/c", "fsutil", "reparsepoint", "query", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                    path.toAbsolutePath().toString());
            int ret = startProcess(command);
            return ret == 0;
        }
    }

    private static int startProcess(List<String> command) throws IOException {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            Process p = builder.start();
            return p.waitFor();
        } catch (InterruptedException e) {
            throw new IOException("Process was interrupted", e); //$NON-NLS-1$
        }
    }

}
