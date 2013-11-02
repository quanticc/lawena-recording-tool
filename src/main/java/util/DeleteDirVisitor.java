
package util;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;

import lwrt.CommandLine;

public class DeleteDirVisitor extends SimpleFileVisitor<Path> {

    private static final Logger log = Logger.getLogger("lawena");

    private CommandLine cl;

    public DeleteDirVisitor(CommandLine cl) {
        this.cl = cl;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        try {
            path.toFile().setWritable(true);
            log.finer("Deleting file: " + path);
            Files.delete(path);
        } catch (NoSuchFileException e) {
        } catch (IOException e) {
            // attempt to delete custom hud font files that mess up the restore
            if (path.endsWith(".otf") || path.endsWith(".ttf")) {
                log.info("Attempting to delete font file: " + path);
                cl.delete(path);
            } else {
                log.log(Level.INFO, "Could not delete file", e);
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (exc == null) {
            try {
                log.finer("Deleting folder: " + dir);
                Files.delete(dir);
            } catch (NoSuchFileException e) {
            } catch (DirectoryNotEmptyException e) {
                log.info("Could not delete directory: " + dir);
            } catch (IOException e) {
                log.log(Level.INFO, "Could not delete directory", e);
            }
            return FileVisitResult.CONTINUE;
        }
        throw exc;
    }
}
