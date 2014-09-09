package com.github.lawena.update;

public class Build implements Comparable<Build> {

  public static final Build LATEST = new Build("", "Latest", Long.MAX_VALUE);

  private String name;
  private String describe;
  private long timestamp;

  public Build() {}

  public Build(String[] raw) {
    this(raw[0], raw[1], Long.parseLong(raw[0]));
  }

  public Build(String name, String describe) {
    this(name, describe, Long.MAX_VALUE);
  }

  public Build(String name, String describe, long timestamp) {
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
  public int compareTo(Build o) {
    return Long.compare(o.timestamp, timestamp);
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
    Build other = (Build) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

}
