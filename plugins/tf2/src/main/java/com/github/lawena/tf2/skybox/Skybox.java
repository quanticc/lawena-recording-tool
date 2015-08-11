package com.github.lawena.tf2.skybox;

import java.nio.file.Path;

import javafx.scene.image.Image;

public class Skybox {

    public static final Skybox DEFAULT = new Skybox("Default"); //$NON-NLS-1$
    public static final int WIDTH = 32;
    public static final int HEIGHT = 32;

    private String name;
    private Image preview;

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

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public Image getPreview() {
        return preview;
    }

    public void setPreview(Image preview) {
        this.preview = preview;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Skybox other = (Skybox) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}
