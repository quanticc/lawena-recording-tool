package com.github.lawena.util;

import com.github.lawena.Messages;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Encapsulates a key-value pair of Strings allowing localization support. The key portion will be an internal value for
 * the application to use, while the value portion will represent the String the user sees and can be localized.
 *
 * @author Ivan
 */
public class ExternalString {

    private String key;
    private String value;

    public ExternalString(String key) {
        this(key, s -> Messages.getString("ExternalString." + s)); //$NON-NLS-1$
    }

    public ExternalString(String key, Function<String, String> provider) {
        this.key = key;
        this.value = provider.apply(key);
    }

    /**
     * Wrap a list of strings into <code>ExternalString</code>s mapped to the API localized messages.
     */
    public static List<ExternalString> from(String... values) {
        return Arrays.asList(values).stream().map(ExternalString::new).collect(Collectors.toList());
    }

    /**
     * Wrap a list of strings into <code>ExternalString</code>s using a custom provider of localized messages.
     */
    public static List<ExternalString> from(Function<String, String> provider, String... values) {
        return from(provider, Arrays.asList(values));
    }

    public static List<ExternalString> from(Function<String, String> provider, List<String> values) {
        return values.stream().map(s -> new ExternalString(s, provider)).collect(Collectors.toList());
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }

}
