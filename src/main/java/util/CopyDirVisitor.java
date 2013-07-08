
package util;

import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Logger;

public class CopyDirVisitor extends SimpleFileVisitor<Path> {

    private static final Logger log = Logger.getLogger("lawena");

    private Path fromPath;
    private Path toPath;
    private StandardCopyOption copyOption = StandardCopyOption.REPLACE_EXISTING;
    private Filter<Path> filter;

    public CopyDirVisitor(Path from, Path to) {
        fromPath = from;
        toPath = to;
    }

    public CopyDirVisitor(Path from, Path to, Filter<Path> filter) {
        this(from, to);
        this.filter = filter;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
            throws IOException {
        if (filter != null && !filter.accept(dir)) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        Path targetPath = toPath.resolve(fromPath.relativize(dir));
        if (!Files.exists(targetPath)) {
            log.finer("Creating directory: " + targetPath);
            Files.createDirectory(targetPath);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Path dest = toPath.resolve(fromPath.relativize(file));
        log.finer("Copying file: " + file + " -> " + dest);
        Path target = Files.copy(file, dest, copyOption);
        target.toFile().setWritable(false);
        return FileVisitResult.CONTINUE;
    }
}
