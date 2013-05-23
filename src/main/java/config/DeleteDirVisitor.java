
package config;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Logger;

public class DeleteDirVisitor extends SimpleFileVisitor<Path> {

    private static final Logger log = Logger.getLogger("lwrt");

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        try {
            path.toFile().setWritable(true);
            Files.delete(path);
        } catch (NoSuchFileException e) {
        } catch (IOException e) {
            log.info("Could not delete file: " + e);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (exc == null) {
            try {
                Files.delete(dir);
            } catch (NoSuchFileException e) {
            } catch (IOException e) {
                log.info("Could not delete directory: " + e);
            }
            return FileVisitResult.CONTINUE;
        }
        throw exc;
    }
}
