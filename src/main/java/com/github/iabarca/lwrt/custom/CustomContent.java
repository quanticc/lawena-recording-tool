
package com.github.iabarca.lwrt.custom;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

public class CustomContent {

    private String path;
    private boolean selected;
    private String tag;
    
    private transient String name;
    private transient Set<String> files = new LinkedHashSet<>();

    public CustomContent() {

    }

    public CustomContent(Path root) {
        this(root, null, false);
    }

    public CustomContent(Path root, String tag) {
        this(root, tag, false);
    }

    public CustomContent(Path root, boolean selected) {
        this(root, null, selected);
    }

    public CustomContent(Path root, String tag, boolean selected) {
        name = root.getFileName().toString();
        path = root.toString();
        this.tag = tag;
        this.selected = selected;
    }

    public Set<String> getFiles() {
        return files;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String rootPath) {
        this.name = Paths.get(rootPath).getFileName().toString();
        this.path = rootPath;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        if (name == null) {
            name = Paths.get(path).getFileName().toString();
        }
        return name;
    }

    @Override
    public String toString() {
        return (tag != null ? "[" + tag + "] " : "") + getName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((tag == null) ? 0 : tag.hashCode());
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
        CustomContent other = (CustomContent) obj;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (tag == null) {
            if (other.tag != null)
                return false;
        } else if (!tag.equals(other.tag))
            return false;
        return true;
    }
}
