package com.github.lawena.game;

public class BooleanGroup extends Group {

    private Boolean defaultValue = false;
    private boolean skipWhenFalse = false;

    @Override
    public final Boolean getDefaultValue() {
        return defaultValue;
    }

    public final void setDefaultValue(Boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    public final boolean isSkipWhenFalse() {
        return skipWhenFalse;
    }

    public final void setSkipWhenFalse(boolean skipWhenFalse) {
        this.skipWhenFalse = skipWhenFalse;
    }

}
