
package com.github.iabarca.lwrt.tree;

import java.nio.file.Path;

public class VpkPath {

    private Path root;
    private String path;
    private String file;

    public VpkPath(Path root) {
        this(root, "", "");
    }

    public VpkPath(Path root, String path, String file) {
        this.root = root;
        this.path = path;
        this.file = file;
    }

    public Path getRoot() {
        return root;
    }

    public String getPath() {
        return path;
    }

    public String getFile() {
        return file;
    }

    @Override
    public String toString() {
        return root.toString() + ":" + path.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((file == null) ? 0 : file.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((root == null) ? 0 : root.hashCode());
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
        VpkPath other = (VpkPath) obj;
        if (file == null) {
            if (other.file != null)
                return false;
        } else if (!file.equals(other.file))
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (root == null) {
            if (other.root != null)
                return false;
        } else if (!root.equals(other.root))
            return false;
        return true;
    }

}
