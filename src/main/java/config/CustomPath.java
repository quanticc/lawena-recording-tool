package config;

import java.nio.file.Path;
import java.util.List;

public class CustomPath {
    
    private Path path;
    private boolean selected = false;
    private transient List<String> contents;
    
    public CustomPath(Path path) {
        this.path = path;
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
    
    public List<String> getContents() {
        return contents;
    }
    
    public void setContents(List<String> contents) {
        this.contents = contents;
    }
    
    @Override
    public String toString() {
        return path.getFileName().toString();
    }

}
