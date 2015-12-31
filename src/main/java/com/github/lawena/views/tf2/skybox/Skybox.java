package com.github.lawena.views.tf2.skybox;

import javafx.scene.image.Image;

import java.nio.file.Path;
import java.util.Objects;

public class Skybox {

    public static final Skybox DEFAULT = new Skybox("Default"); //$NON-NLS-1$
    public static final int WIDTH = 32;
    public static final int HEIGHT = 32;

    private String name;
    private transient Image preview;

    public Skybox(Path upVtfPath) {
        String skybox = upVtfPath.toFile().getName();
        this.name = skybox.substring(0, skybox.indexOf("up.vtf")); //$NON-NLS-1$
    }

    private Skybox(String name) {
        this.name = name;
    }

    public Skybox(String name, Image preview) {
        this.name = name;
        this.preview = preview;
    }

    public final String getName() {
        return name;
    }

    @Override
    public final String toString() {
        return name;
    }

    public final Image getPreview() {
        return preview;
    }

    public final void setPreview(Image preview) {
        this.preview = preview;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Skybox skybox = (Skybox) o;
        return Objects.equals(name, skybox.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}
