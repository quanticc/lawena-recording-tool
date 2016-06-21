package com.github.lawena.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;

import static com.github.lawena.util.LwrtUtils.newProcessReader;

public class RegistryUtils {

    private static final Logger log = LoggerFactory.getLogger(RegistryUtils.class);

    private void set(String key, String value, String content) {
        try {
            ProcessBuilder pb = new ProcessBuilder("lwrt\\tools\\regedit\\rg.bat", key, value, content);
            Process pr = pb.start();
            pr.waitFor();
        } catch (InterruptedException | IOException e) {
            log.warn("Could not edit registry: {}", e.toString());
        }
    }

    private String get(String key, String value) {
        StringBuilder result = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder("reg", "query", key, "/v", value);
            Process pr = pb.start();
            try (BufferedReader input = newProcessReader(pr)) {
                String line;
                while ((line = input.readLine()) != null) {
                    result.append(line).append('\n');
                }
            }
            pr.waitFor();
        } catch (InterruptedException | IOException e) {
            log.warn("Could not query registry: {}", e.toString());
        }
        try {
            if (result.lastIndexOf("0x") > 0) {
                return result.substring(result.lastIndexOf("0x") + 2,
                        result.indexOf("\n", result.lastIndexOf("0x")));
            } else {
                String str = "REG_SZ    ";
                return result.substring(result.lastIndexOf(str) + str.length(),
                        result.indexOf("\n", result.lastIndexOf(str)));
            }
        } catch (IndexOutOfBoundsException e) {
            log.debug("Data not found at value {} for key {}", value, key);
            return "";
        }
    }

    private RegistryUtils() {
    }
}
