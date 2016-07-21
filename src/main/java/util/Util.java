package util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class Util {

  private static final Logger log = Logger.getLogger("lawena");

  public static int compareCreationTime(File newgd, File curgd) {
    try {
      Path newp = newgd.toPath();
      BasicFileAttributes newAttributes = Files.readAttributes(newp, BasicFileAttributes.class);
      FileTime newCreationTime = newAttributes.creationTime();
      Path curp = curgd.toPath();
      BasicFileAttributes curAttributes = Files.readAttributes(curp, BasicFileAttributes.class);
      FileTime curCreationTime = curAttributes.creationTime();
      log.fine("Comparing creationTime: new=" + newCreationTime + " cur=" + curCreationTime);
      return newCreationTime.compareTo(curCreationTime);
    } catch (IOException e) {
      log.warning("Could not retrieve file creation time: " + e.toString());
    }
    return 1;
  }

  public static void copy(InputStream in, OutputStream out) throws IOException {
    try {
      byte[] buffer = new byte[4096];
      for (int read = 0; (read = in.read(buffer)) > 0;) {
        out.write(buffer, 0, read);
      }
    } catch (IOException e) {
      throw e;
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
        }
      }
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
        }
      }
    }
  }

  public static String now(String format) {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    return sdf.format(Calendar.getInstance().getTime());
  }

  public static long sizeOfPath(Path startPath) throws IOException {
    final AtomicLong size = new AtomicLong(0);
    Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        size.addAndGet(attrs.size());
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
      }
    });
    return size.get();
  }

  public static String humanReadableByteCount(long bytes, boolean si) {
    int unit = si ? 1000 : 1024;
    if (bytes < unit)
      return bytes + " B";
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }

  public static int startProcess(List<String> command) {
    try {
      ProcessBuilder builder = new ProcessBuilder(command);
      Process p = builder.start();
      return p.waitFor();
    } catch (InterruptedException | IOException e) {
      log.warning("Process could not be completed: " + e.toString()); //$NON-NLS-1$
    }
    return 1;
  }

  /**
   * Strip the filename extension from the given Java resource path, e.g. "mypath/myfile.txt" ->
   * "mypath/myfile".
   * 
   * @param path the file path (may be {@code null})
   * @return the path with stripped filename extension, or {@code null} if none
   * @author Spring Framework StringUtils
   */
  public static String stripFilenameExtension(String path) {
    if (path == null) {
      return null;
    }
    int extIndex = path.lastIndexOf('.');
    if (extIndex == -1) {
      return path;
    }
    int folderIndex = path.lastIndexOf('/');
    if (folderIndex > extIndex) {
      return path;
    }
    return path.substring(0, extIndex);
  }

  private Util() {}

}
