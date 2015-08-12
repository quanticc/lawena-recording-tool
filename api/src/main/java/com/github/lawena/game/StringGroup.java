package com.github.lawena.game;

import java.util.List;

public class StringGroup extends Group {

    private String defaultValue = ""; //$NON-NLS-1$
    private List<String> validSet;

    @Override
    public final String getDefaultValue() {
        return defaultValue;
    }

    public final void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public final List<String> getValidSet() {
        return validSet;
    }

    public final void setValidSet(List<String> validSet) {
        this.validSet = validSet;
    }

}
