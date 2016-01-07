package com.github.lawena.util;

import com.github.lawena.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.prefs.Preferences;

/**
 * Utility methods for Lawena Recording Tool.
 *
 * @author Ivan
 */
public final class LwrtUtils {
    private static final Logger log = LoggerFactory.getLogger(LwrtUtils.class);

    private static boolean windows;
    private static boolean macOS;
    private static boolean linux;

    static {
        try {
            String osname = System.getProperty("os.name");
            osname = (osname == null) ? "" : osname;
            windows = (osname.contains("Windows"));
            macOS = (osname.contains("Mac OS") ||
                    osname.contains("MacOS"));
            linux = (osname.contains("Linux"));
        } catch (Exception e) {
            log.warn("Could not get OS name property");
        }
    }

    private LwrtUtils() {
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

    public static boolean isLinux() {
        return linux;
    }

    public static boolean isMacOS() {
        return macOS;
    }

    public static boolean isWindows() {
        return windows;
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

    /**
     * Try to create a Path from a given String. It does not validate file existence.
     * If it is not possible to do so returns an empty Optional.
     *
     * @param str
     * @return
     */
    public static Optional<Path> tryGetPath(String str) {
        if (str == null) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(Paths.get(str));
            } catch (InvalidPathException e) {
                return Optional.empty();
            }
        }
    }

    public static boolean isNotEmptyPath(Path path) {
        return !Paths.get("").equals(path);
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String safeToString(Object o) {
        if (o == null) {
            return "";
        }
        return o.toString();
    }

    public static BufferedReader newProcessReader(Process p) {
        return new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.forName("UTF-8")));
    }

    public static String leftPad(String str, int outputLength, char padChar) {
        StringBuilder sb = new StringBuilder();
        for (int toPrepend = outputLength - str.length(); toPrepend > 0; toPrepend--) {
            sb.append(padChar);
        }
        sb.append(str);
        return sb.toString();
    }

    public static boolean isValidSteamPath(String steamPath) {
        Optional<Path> path = tryGetPath(steamPath);
        return path.isPresent() && isValidSteamPath(path.get());
    }

    public static boolean isValidSteamPath(Path path) {
        return path != null && Files.isDirectory(path) && Files.exists(path.resolve(Constants.STEAM_APP_NAME.get()));
    }

    public static boolean isValidGamePath(String gamePath, String executableName) {
        Optional<Path> path = tryGetPath(gamePath);
        return path.isPresent() && isValidGamePath(path.get(), executableName);
    }

    public static boolean isValidGamePath(Path gamePath, String executableName) {
        Path path = gamePath.toAbsolutePath().getParent();
        return path != null
                && Files.exists(gamePath)
                && Files.isDirectory(gamePath)
                && Files.exists(path)
                && Files.isDirectory(path)
                && !path.startsWith(Paths.get("").toAbsolutePath().getParent())
                && Files.exists(path.resolve(executableName));
    }
}
