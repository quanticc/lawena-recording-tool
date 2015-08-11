package com.github.lawena.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.prefs.Preferences;

import javafx.scene.image.Image;

/**
 * Utility methods for Lawena Recording Tool.
 *
 * @author Ivan
 */
public class LwrtUtils {
    public static final String IMAGES_BASE = "/com/github/lawena";
    private static final Logger log = LoggerFactory.getLogger(LwrtUtils.class);

    public static Image image(String location) {
        try (InputStream input = LwrtUtils.class.getResourceAsStream(IMAGES_BASE + location)) {
            if (input != null) {
                return new Image(input);
            }
        } catch (IOException e) {
            log.debug("Could not load image from {}: {}", location, e.toString());
        }
        return null;
    }

    /**
     * Get the current date and time using the given format.
     *
     * @param format the pattern to use, not null
     * @return a formatted date and time
     */
    public static String now(String format) {
        Objects.requireNonNull(format);
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format));
    }

    public static String getManifestString(String key, String defaultValue) {
        try (JarFile jar =
                     new JarFile(
                             new File(LwrtUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI()))) {
            String value = jar.getManifest().getMainAttributes().getValue(key);
            if (value == null) {
                log.warn("{}: {} (not built using Gradle)", key, defaultValue);
                return defaultValue;
            }
            return value;
        } catch (IOException | URISyntaxException ignored) {
        }
        log.warn("{}: {} (not packaged into a JAR file)", key, defaultValue);
        return defaultValue;
    }

    public static boolean isAdmin() {
        Preferences prefs = Preferences.systemRoot();
        try {
            prefs.put("foo", "bar"); // SecurityException on Windows
            prefs.remove("foo");
            prefs.flush(); // BackingStoreException on Linux
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
