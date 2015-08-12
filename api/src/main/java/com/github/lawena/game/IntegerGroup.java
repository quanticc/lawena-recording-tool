package com.github.lawena.game;

public class IntegerGroup extends Group {

    private Integer defaultValue = 0;
    private Integer min = Integer.MIN_VALUE;
    private Integer max = Integer.MAX_VALUE;

    @Override
    public final Integer getDefaultValue() {
        return defaultValue;
    }

    public final void setDefaultValue(Integer defaultValue) {
        this.defaultValue = defaultValue;
    }

    public final Integer getMin() {
        return min;
    }

    public final void setMin(Integer min) {
        this.min = min;
    }

    public final Integer getMax() {
        return max;
    }

    public final void setMax(Integer max) {
        this.max = max;
    }

}
