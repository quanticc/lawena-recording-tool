package com.github.lawena.views.launchers;

import com.github.lawena.domain.ConfigFlag;
import com.github.lawena.util.LwrtUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

public class FxConfigFlag {

    public static FxConfigFlag configFlagToFxConfigFlag(ConfigFlag from) {
        return new FxConfigFlag(from).load();
    }

    public static ConfigFlag fxConfigFlagToConfigFlag(FxConfigFlag from) {
        return from.save();
    }

    public static FxConfigFlag duplicate(FxConfigFlag base) {
        FxConfigFlag fxConfigFlag = new FxConfigFlag();
        fxConfigFlag.setKey(base.getKey());
        fxConfigFlag.setDefaultValue(base.getDefaultValue());
        fxConfigFlag.setEnabledValue(base.getEnabledValue());
        fxConfigFlag.setDisabledValue(base.getDisabledValue());
        return fxConfigFlag;
    }

    private final ConfigFlag configFlag;
    private StringProperty key = new SimpleStringProperty(this, "key", "profile.key.name");
    private BooleanProperty defaultValue = new SimpleBooleanProperty(this, "defaultValue", false);
    private StringProperty enabledValue = new SimpleStringProperty(this, "enabledValue", "1");
    private StringProperty disabledValue = new SimpleStringProperty(this, "disabledValue", "0");

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
        configFlag.setTrueMappedValue(LwrtUtils.coerce(enabledValue.get()));
        configFlag.setFalseMappedValue(LwrtUtils.coerce(disabledValue.get()));
        return configFlag;
    }

    public StringProperty keyProperty() {
        return key;
    }

    public String getKey() {
        return key.get();
    }

    public void setKey(String key) {
        this.key.set(key);
    }

    public BooleanProperty defaultValueProperty() {
        return defaultValue;
    }

    public boolean getDefaultValue() {
        return defaultValue.get();
    }

    public void setDefaultValue(boolean defaultValue) {
        this.defaultValue.set(defaultValue);
    }

    public StringProperty enabledValueProperty() {
        return enabledValue;
    }

    public String getEnabledValue() {
        return enabledValue.get();
    }

    public void setEnabledValue(String enabledValue) {
        this.enabledValue.set(enabledValue);
    }

    public StringProperty disabledValueProperty() {
        return disabledValue;
    }

    public String getDisabledValue() {
        return disabledValue.get();
    }

    public void setDisabledValue(String disabledValue) {
        this.disabledValue.set(disabledValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FxConfigFlag that = (FxConfigFlag) o;
        return Objects.equals(key.get(), that.key.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(key.get());
    }
}
