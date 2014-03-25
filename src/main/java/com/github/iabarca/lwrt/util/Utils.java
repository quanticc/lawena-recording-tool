
package com.github.iabarca.lwrt.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.jar.JarFile;

public class Utils {

    private Utils() {

    }

    public static String getManifestString(String key, String defaultValue) {
        try (JarFile jar = new JarFile(new File(Utils.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()))) {
            String value = jar.getManifest().getMainAttributes()
                    .getValue(key);
            return (value == null ? "bat." + defaultValue : value);
        } catch (IOException | URISyntaxException e) {
        }
        return "custom." + defaultValue;
    }

    public static String now(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(Calendar.getInstance().getTime());
    }

    public static int countOccurrences(String str, char sub) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == sub) {
                count++;
            }
        }
        return count;
    }

}
