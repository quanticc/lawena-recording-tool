package util;

import lwrt.CommandLine;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Logger;

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
            log.fine("File does not exist: " + e);
        } catch (IOException e) {
            // attempt to delete custom hud font files that mess up the restore
            if (path.endsWith(".otf") || path.endsWith(".ttf")) {
                log.info("Attempting to delete font file: " + path);
                cl.delete(path);
            } else {
                log.warning("Could not delete file: " + e);
                throw e;
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
                log.fine("File does not exist: " + e);
            } catch (IOException e) {
                log.warning("Could not delete directory: " + e);
                throw e;
            }
            return FileVisitResult.CONTINUE;
        }
        throw exc;
    }
}
