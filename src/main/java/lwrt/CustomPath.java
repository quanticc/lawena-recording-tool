package lwrt;

import java.nio.file.Path;

public class CustomPath {
    
    private Path path;
    private boolean selected = false;
    
    public CustomPath(Path path) {
        this.path = path;
        selected = path.startsWith("custom");
    }
    
    public Path getPath() {
        return path;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    @Override
    public String toString() {
        return path.getFileName().toString();
    }

}
