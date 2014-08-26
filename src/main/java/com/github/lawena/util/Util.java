package com.github.lawena.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {

  private static final Logger log = LoggerFactory.getLogger(Util.class);

  private Util() {}

  public static int compareCreationTime(File newgd, File curgd) {
    try {
      Path newp = newgd.toPath();
      BasicFileAttributes newAttributes = Files.readAttributes(newp, BasicFileAttributes.class);
      FileTime newCreationTime = newAttributes.creationTime();
      Path curp = curgd.toPath();
      BasicFileAttributes curAttributes = Files.readAttributes(curp, BasicFileAttributes.class);
      FileTime curCreationTime = curAttributes.creationTime();
      log.debug("Comparing creationTime: new={} cur={}", newCreationTime, curCreationTime);
      return newCreationTime.compareTo(curCreationTime);
    } catch (IOException e) {
      log.warn("Could not retrieve file creation time: " + e);
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

  public static String getManifestString(String key, String defaultValue) {
    try (JarFile jar =
        new JarFile(
            new File(Util.class.getProtectionDomain().getCodeSource().getLocation().toURI()))) {
      String value = jar.getManifest().getMainAttributes().getValue(key);
      return (value == null ? "no-gradle " + defaultValue : value);
    } catch (IOException | URISyntaxException e) {
    }
    return "no-jar " + defaultValue;
  }

}
