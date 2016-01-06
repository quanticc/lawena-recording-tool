package com.github.lawena.views.launchers;

import com.github.lawena.domain.ConfigFlag;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;

public class FxConfigFlag {

    public static FxConfigFlag configFlagToFxConfigFlag(ConfigFlag from) {
        return new FxConfigFlag(from).load();
    }

    public static ConfigFlag fxConfigFlagToConfigFlag(FxConfigFlag from) {
        return from.save();
    }

    private final ConfigFlag configFlag;
    private StringProperty key = new SimpleStringProperty(this, "key", "");
    private BooleanProperty defaultValue = new SimpleBooleanProperty(this, "defaultValue", false);
    private StringProperty enabledValue = new SimpleStringProperty(this, "enabledValue", "");
    private StringProperty disabledValue = new SimpleStringProperty(this, "disabledValue", "");

    public FxConfigFlag() {
        this(new ConfigFlag());
    }

    private FxConfigFlag(ConfigFlag from) {
        this.configFlag = from;
    }

    private FxConfigFlag load() {
        key.set(configFlag.getKey());
        defaultValue.set(configFlag.getDefaultValue());
        enabledValue.set(configFlag.getTrueMappedValue().toString());
        disabledValue.set(configFlag.getFalseMappedValue().toString());
        return this;
    }

    private ConfigFlag save() {
        configFlag.setKey(key.get());
        configFlag.setDefaultValue(defaultValue.get());
        configFlag.setTrueMappedValue(coerce(enabledValue.get()));
        configFlag.setFalseMappedValue(coerce(disabledValue.get()));
        return configFlag;
    }

    private Object coerce(String source) {
        // "null" -> null
        // "" | "false" -> false
        // "true" -> true
        // "[]" -> new ArrayList<Object>()
        if (source == null || source.equalsIgnoreCase("null")) {
            return null;
        } else if (source.isEmpty() || source.equalsIgnoreCase("false")) {
            return false;
        } else if (source.equalsIgnoreCase("true")) {
            return true;
        } else if (source.equals("[]")) {
            return new ArrayList<Object>();
        } else {
            return source;
        }
    }

    public StringProperty keyProperty() {
        return key;
    }

    public BooleanProperty defaultValueProperty() {
        return defaultValue;
    }

    public StringProperty enabledValueProperty() {
        return enabledValue;
    }

    public StringProperty disabledValueProperty() {
        return disabledValue;
    }
}
