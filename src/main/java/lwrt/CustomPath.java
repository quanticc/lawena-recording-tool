
package lwrt;

import java.nio.file.Path;
import java.util.EnumSet;

public class CustomPath {

    public enum PathContents {
        REQUIRED, READONLY, HUD, CONFIG, SKYBOX;

        public String toString() {
            return name().toLowerCase();
        };
    }

    private Path path;
    private String name;
    private boolean selected;
    private EnumSet<PathContents> contents;

    public CustomPath(Path path) {
        this(path, path.getFileName().toString());
    }

    public CustomPath(Path path, String name) {
        this(path, name, EnumSet.of(PathContents.REQUIRED));
    }

    public CustomPath(Path path, String name, EnumSet<PathContents> contents) {
        this.path = path;
        this.name = name;
        this.selected = path.startsWith("custom");
        this.contents = contents;
    }

    public Path getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public EnumSet<PathContents> getContents() {
        return contents;
    }

    public void setContents(EnumSet<PathContents> contents) {
        this.contents = contents;
    }

    @Override
    public String toString() {
        EnumSet<PathContents> c = EnumSet.copyOf(contents);
        c.remove(PathContents.REQUIRED);
        c.remove(PathContents.READONLY);
        return name + (!c.isEmpty() ? " " + c : "");
    }

}
