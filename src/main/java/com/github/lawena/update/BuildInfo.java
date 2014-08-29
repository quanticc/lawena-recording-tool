package com.github.lawena.update;

public class BuildInfo implements Comparable<BuildInfo> {

  public static final BuildInfo LATEST = new BuildInfo("", "Latest", Long.MAX_VALUE);

  private String name;
  private String describe;
  private long timestamp;

  public BuildInfo() {}

  public BuildInfo(String[] raw) {
    this(raw[0], raw[1], Long.parseLong(raw[0]));
  }

  public BuildInfo(String name, String describe) {
    this(name, describe, Long.MAX_VALUE);
  }

  public BuildInfo(String name, String describe, long timestamp) {
    this.name = name;
    this.describe = describe;
    this.timestamp = timestamp;
  }

  public String getName() {
    return name;
  }

  public String getDescribe() {
    return describe;
  }

  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return describe + " " + name;
  }

  @Override
  public int compareTo(BuildInfo o) {
    return -Long.compare(timestamp, o.timestamp);
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
    BuildInfo other = (BuildInfo) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

}
