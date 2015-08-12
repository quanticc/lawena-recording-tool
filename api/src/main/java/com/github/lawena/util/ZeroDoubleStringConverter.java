package com.github.lawena.util;

import javafx.util.StringConverter;

public class ZeroDoubleStringConverter extends StringConverter<Double> {

    @Override
    public final Double fromString(String value) {
        // If the specified value is null or zero-length, return 0
        if (value == null) {
            return 0.0;
        }
        value = value.trim();
        if (value.length() < 1) {
            return 0.0;
        }
        return Double.valueOf(value);
    }

    @Override
    public final String toString(Double value) {
        // If the specified value is null, return "0"
        if (value == null) {
            return "0";
        }
        return Double.toString(value.doubleValue());
    }
}
