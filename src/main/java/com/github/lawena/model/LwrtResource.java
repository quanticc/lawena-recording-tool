package com.github.lawena.model;

import java.nio.file.Path;
import java.util.EnumSet;

public class LwrtResource {

  public enum PathContents {
    DEFAULT, READONLY, HUD, CONFIG, SKYBOX;

    public String toString() {
      return name().toLowerCase();
    };
  }

  private Path path;
  private String name;
  private boolean selected = false;
  private EnumSet<PathContents> contents;

  public LwrtResource(Path path) {
    this(path, path.getFileName().toString());
  }

  public LwrtResource(Path path, String name) {
    this(path, name, EnumSet.noneOf(PathContents.class));
  }

  public LwrtResource(Path path, String name, EnumSet<PathContents> contents) {
    this.path = path;
    this.name = name;
    this.contents = contents;
    if (contents.contains(PathContents.READONLY)) {
      selected = true;
    }
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

  @Override
  public String toString() {
    return name;
  }

}
