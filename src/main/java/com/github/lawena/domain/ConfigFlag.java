package com.github.lawena.domain;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class ConfigFlag {

    private transient BooleanProperty enabled = new SimpleBooleanProperty(this, "enabled", false);
    private String key;
    private boolean defaultValue;
    private Object trueMappedValue;
    private Object falseMappedValue;

    public boolean getEnabled() {
        return enabled.get();
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Object getTrueMappedValue() {
        return trueMappedValue;
    }

    public void setTrueMappedValue(Object trueMappedValue) {
        this.trueMappedValue = trueMappedValue;
    }

    public Object getFalseMappedValue() {
        return falseMappedValue;
    }

    public void setFalseMappedValue(Object falseMappedValue) {
        this.falseMappedValue = falseMappedValue;
    }

    @Override
    public String toString() {
        return "ConfigFlag [" +
                "enabled=" + enabled.get() +
                ", key='" + key + '\'' +
                ", defaultValue=" + defaultValue +
                ", trueMappedValue=" + trueMappedValue +
                ", falseMappedValue=" + falseMappedValue +
                ']';
    }
}
