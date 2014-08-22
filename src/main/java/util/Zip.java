package util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Zip {

  private static final Logger log = Logger.getLogger("lawena");

  /**
   * Creates/updates a zip file.
   * 
   * @param zipFilename the name of the zip to create
   * @param filenames list of filename to add to the zip
   * @throws IOException
   */
  public static void create(String zipFilename, String... filenames) throws IOException {
    List<Path> paths = new ArrayList<>();
    for (String f : filenames) {
      paths.add(Paths.get(f));
    }
    create(Paths.get(zipFilename), paths);
  }

  /**
   * Creates/updates a zip file.
   * 
   * @param zipFilename the name of the zip to create
   * @param filenames list of filename to add to the zip
   * @throws IOException
   */
  public static void create(Path zipFilename, Path... filenames) throws IOException {
    create(zipFilename, Arrays.asList(filenames));
  }

  /**
   * Creates/updates a zip file.
   * 
   * @param zipFilename the name of the zip to create
   * @param filenames list of filename to add to the zip
   * @throws IOException
   */
  public static void create(Path zipFilename, List<Path> filenames) throws IOException {

    try (FileSystem zipFileSystem = createZipFileSystem(zipFilename, true)) {
      final Path root = zipFileSystem.getPath("/");

      // iterate over the files we need to add
      for (final Path src : filenames) {
        if (!Files.exists(src)) {
          log.info("[zip] " + src + " was skipped because it does not exists");
          continue;
        }
        // add a file to the zip file system
        if (!Files.isDirectory(src)) {
          Path dest = zipFileSystem.getPath(root.toString(), src.getFileName().toString());
          Path parent = dest.getParent();
          if (Files.notExists(parent)) {
            log.fine("[zip] Creating directory: " + parent);
            Files.createDirectories(parent);
          }
          Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
        } else {
          // for directories, walk the file tree
          Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
              Path relativeFile = src.getFileName().resolve(src.relativize(file));
              Path dest = zipFileSystem.getPath(root.toString(), relativeFile.toString());
              Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {
              Path relativeDir = src.getFileName().resolve(src.relativize(dir));
              Path dirToCreate = zipFileSystem.getPath(root.toString(), relativeDir.toString());
              if (Files.notExists(dirToCreate)) {
                log.fine("[zip] Creating directory: " + dirToCreate);
                Files.createDirectories(dirToCreate);
              }
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
              log.info("[zip] Creation failed with: " + file + " due to " + exc.toString());
              return super.visitFileFailed(file, exc);
            }
          });
        }
      }
    }
  }

  /**
   * Unzips the specified zip file to the specified destination directory. Replaces any files in the
   * destination, if they already exist.
   * 
   * @param zipFilename the name of the zip file to extract
   * @param destDirname the directory to unzip to
   * @throws IOException
   */
  public static void unzip(String zipFilename, String destDirname) throws IOException {
    unzip(Paths.get(zipFilename), Paths.get(destDirname));
  }

  /**
   * Unzips the specified zip file to the specified destination directory. Replaces any files in the
   * destination, if they already exist.
   * 
   * @param zipFilename the name of the zip file to extract
   * @param destDir the directory to unzip to
   * @throws IOException
   */
  public static void unzip(Path zipFilename, final Path destDir) throws IOException {
    // if the destination doesn't exist, create it
    if (Files.notExists(destDir)) {
      log.fine(destDir + " does not exist. Creating...");
      Files.createDirectories(destDir);
    }

    try (FileSystem zipFileSystem = createZipFileSystem(zipFilename, false)) {
      final Path root = zipFileSystem.getPath("/");

      // walk the zip file tree and copy files to the destination
      Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          final Path destFile = Paths.get(destDir.toString(), file.toString());
          log.fine("Extracting file " + file + " to " + destFile);
          Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
            throws IOException {
          final Path dirToCreate = Paths.get(destDir.toString(), dir.toString());
          if (Files.notExists(dirToCreate)) {
            log.fine("Creating directory " + dirToCreate);
            Files.createDirectory(dirToCreate);
          }
          return FileVisitResult.CONTINUE;
        }
      });
    }
  }

  /**
   * List the contents of the specified zip file
   * 
   * @param filename
   * @throws IOException
   * @throws URISyntaxException
   */
  public static void list(String zipFilename) throws IOException {
    list(Paths.get(zipFilename));
  }

  /**
   * List the contents of the specified zip file
   * 
   * @param filename
   * @throws IOException
   * @throws URISyntaxException
   */
  public static void list(Path zipFilename) throws IOException {
    log.info("Listing archive: " + zipFilename);

    // create the file system
    try (FileSystem zipFileSystem = createZipFileSystem(zipFilename, false)) {

      final Path root = zipFileSystem.getPath("/");

      // walk the file tree and print out the directory and filenames
      Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          print(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
            throws IOException {
          print(dir);
          return FileVisitResult.CONTINUE;
        }

        /**
         * prints out details about the specified path such as size and modification time
         * 
         * @param file
         * @throws IOException
         */
        private void print(Path file) throws IOException {
          final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          final String modTime = df.format(new Date(Files.getLastModifiedTime(file).toMillis()));
          log.info(Files.size(file) + " " + modTime + " " + file);
        }
      });
    }
  }

  private static FileSystem createZipFileSystem(Path path, boolean create) throws IOException {
    // convert the filename to a URI
    final URI uri = URI.create("jar:" + path.toUri());

    final Map<String, String> env = new HashMap<>();
    if (create) {
      env.put("create", "true");
    }
    return FileSystems.newFileSystem(uri, env);
  }

}
