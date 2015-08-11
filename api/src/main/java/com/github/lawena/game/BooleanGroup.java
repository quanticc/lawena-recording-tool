package com.github.lawena.game;

public class BooleanGroup extends Group {

    private Boolean defaultValue = false;
    private boolean skipWhenFalse = false;

    @Override
    public Boolean getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isSkipWhenFalse() {
        return skipWhenFalse;
    }

    public void setSkipWhenFalse(boolean skipWhenFalse) {
        this.skipWhenFalse = skipWhenFalse;
    }

}
