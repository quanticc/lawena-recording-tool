package com.github.lawena.app.model;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class Resource {

  public static final String CONFIG = "config";
  public static final String HUD = "hud";
  public static final String SKYBOX = "skybox";
  public static final String PARTICLES = "particles";

  private boolean enabled = false;
  private String name;
  private File file;
  private Set<String> tags = new HashSet<>();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public File getFile() {
    return file;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public Path getPath() {
    return file.toPath();
  }

  public void setPath(Path path) {
    this.file = path.toFile();
  }

  public Path getAbsolutePath() {
    return file.toPath().toAbsolutePath();
  }

  /**
   * Returns the absolute parent <code>Path</code> of this <code>Resource</code>.
   * 
   * @return a <code>Path</code> to the parent of this <code>Resource</code>
   */
  public Path getParentPath() {
    return file.toPath().toAbsolutePath().getParent();
  }

  public Set<String> getTags() {
    return tags;
  }

  public void setTags(Set<String> tags) {
    this.tags = tags;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((file == null) ? 0 : file.hashCode());
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
    Resource other = (Resource) obj;
    if (file == null) {
      if (other.file != null)
        return false;
    } else if (!file.equals(other.file))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return (name != null ? name : file.toString());
  }

}
