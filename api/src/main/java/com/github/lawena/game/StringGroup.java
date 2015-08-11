package com.github.lawena.game;

import java.util.List;

public class StringGroup extends Group {

    private String defaultValue = ""; //$NON-NLS-1$
    private List<String> validSet;

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<String> getValidSet() {
        return validSet;
    }

    public void setValidSet(List<String> validSet) {
        this.validSet = validSet;
    }

}
