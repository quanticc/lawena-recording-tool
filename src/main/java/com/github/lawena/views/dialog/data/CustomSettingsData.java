package com.github.lawena.views.dialog.data;

import javafx.geometry.Dimension2D;

import java.util.Objects;

public class CustomSettingsData {

    private String content = "";
    private Dimension2D dimension = new Dimension2D(600.0, 500.0);

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        Objects.requireNonNull(content);
        this.content = content;
    }

    public Dimension2D getDimension() {
        return dimension;
    }

    public void setDimension(Dimension2D dimension) {
        Objects.requireNonNull(dimension);
        this.dimension = dimension;
    }
}
