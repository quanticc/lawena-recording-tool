package com.github.lawena.views.launchers;

import com.github.lawena.util.LwrtUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Pair;

import java.util.Objects;

public class FxScope {

    public static FxScope create() {
        return new FxScope();
    }

    public static FxScope create(String key, Object value) {
        FxScope scope = new FxScope();
        scope.setKey(key);
        scope.setValue(LwrtUtils.tryConvertToJsonString(value));
        return scope;
    }

    public static FxScope duplicate(FxScope base) {
        FxScope fxScope = new FxScope();
        fxScope.setKey(base.getKey());
        fxScope.setValue(base.getValue());
        return fxScope;
    }

    private StringProperty key = new SimpleStringProperty(this, "key", "key.name");
    private StringProperty value = new SimpleStringProperty(this, "value", "value");

    private FxScope() {
    }

    public Pair<String, Object> toPair() {
        return new Pair<>(key.get(), LwrtUtils.coerce(value.get()));
    }

    public Object getConvertedValue() {
        return LwrtUtils.coerce(value.get());
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

    public StringProperty valueProperty() {
        return value;
    }

    public String getValue() {
        return value.get();
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FxScope that = (FxScope) o;
        return Objects.equals(key.get(), that.key.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(key.get());
    }
}
