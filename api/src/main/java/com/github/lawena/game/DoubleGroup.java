package com.github.lawena.game;

public class DoubleGroup extends Group {

    private Double defaultValue = 0D;
    private Double min = Double.MIN_VALUE;
    private Double max = Double.MAX_VALUE;

    @Override
    public Double getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Double defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

}
