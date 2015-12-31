package com.github.lawena;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class Messages {

    private static final String BUNDLE_NAME = "i18n.messages";

    public static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() {
    }

    public static String getString(String key) {
        return getStringWithFallback(key, '!' + key + '!');
    }

    public static String getStringWithFallback(String key, String fallback) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return fallback;
        }
    }

    public static String getString(String key, Object... arguments) {
        return MessageFormat.format(getString(key), arguments);
    }
}
