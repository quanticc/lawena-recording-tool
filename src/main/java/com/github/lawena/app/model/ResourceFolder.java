package com.github.lawena.app.model;

import java.io.File;

public class ResourceFolder {

  private boolean enabled = true;
  private File file;
  private boolean forceLoad;

  public ResourceFolder(File file, boolean forceLoad) {
    this.file = file;
    this.forceLoad = forceLoad;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public File getFile() {
    return file;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public boolean isForceLoad() {
    return forceLoad;
  }

  public void setForceLoad(boolean forceLoad) {
    this.forceLoad = forceLoad;
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
    ResourceFolder other = (ResourceFolder) obj;
    if (file == null) {
      if (other.file != null)
        return false;
    } else if (!file.equals(other.file))
      return false;
    return true;
  }

}
