package com.github.lawena.domain;

import javafx.scene.paint.Color;

import java.util.Objects;

public class Tag implements Comparable<Tag> {

    private String name;
    private String description;
    private Color cellColor;

    public Tag(String name, String description, Color cellColor) {
        this.name = name;
        this.description = description;
        this.cellColor = cellColor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Color getCellColor() {
        return cellColor;
    }

    public void setCellColor(Color cellColor) {
        this.cellColor = cellColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(name, tag.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Tag [" +
                "name='" + name + '\'' +
                //", description='" + description + '\'' +
                //", cellColor=" + cellColor +
                ']';
    }

    @Override
    public int compareTo(Tag o) {
        return name.compareTo(o.getName());
    }
}
