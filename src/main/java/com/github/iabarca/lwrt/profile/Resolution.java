
package com.github.iabarca.lwrt.profile;

public class Resolution {
    private int width;
    private int height;

    public Resolution(Resolution resolution) {
        this(resolution.getWidth(), resolution.getHeight());
    }

    public Resolution(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Resolution(String resolution) {
        String[] array = resolution.split("x");
        if (array.length == 2) {
            width = Integer.parseInt(array[0]);
            height = Integer.parseInt(array[1]);
        } else {
            throw new IllegalArgumentException("Bad resolution format");
        }
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }
}
