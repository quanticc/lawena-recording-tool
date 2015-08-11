package com.github.lawena.game;

public class IntegerGroup extends Group {

    private Integer defaultValue = 0;
    private Integer min = Integer.MIN_VALUE;
    private Integer max = Integer.MAX_VALUE;

    @Override
    public Integer getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Integer defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

}
