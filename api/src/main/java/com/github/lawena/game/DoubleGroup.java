package com.github.lawena.game;

public class DoubleGroup extends Group {

    private Double defaultValue = 0D;
    private Double min = Double.MIN_VALUE;
    private Double max = Double.MAX_VALUE;

    @Override
    public final Double getDefaultValue() {
        return defaultValue;
    }

    public final void setDefaultValue(Double defaultValue) {
        this.defaultValue = defaultValue;
    }

    public final Double getMin() {
        return min;
    }

    public final void setMin(Double min) {
        this.min = min;
    }

    public final Double getMax() {
        return max;
    }

    public final void setMax(Double max) {
        this.max = max;
    }

}
