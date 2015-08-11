package com.github.lawena.util;

import javafx.util.StringConverter;

public class ZeroIntegerStringConverter extends StringConverter<Integer> {
    @Override
    public Integer fromString(String value) {
        // If the specified value is null or zero-length, return 0
        if (value == null) {
            return 0;
        }
        value = value.trim();
        if (value.length() < 1) {
            return 0;
        }
        return Integer.valueOf(value);
    }

    @Override
    public String toString(Integer value) {
        // If the specified value is null, return "0"
        if (value == null) {
            return "0";
        }
        return (Integer.toString(((Integer) value).intValue()));
    }
}
