package com.github.lawena.update;

public class VersionInfo implements Comparable<VersionInfo> {

  private String name;
  private String describe;
  private long timestamp;

  public VersionInfo() {}

  public VersionInfo(String[] raw) {
    name = raw[0];
    describe = raw[1];
    timestamp = Long.parseLong(raw[2]);
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
    return "VersionInfo [name=" + name + ", describe=" + describe + ", timestamp=" + timestamp
        + "]";
  }

  @Override
  public int compareTo(VersionInfo o) {
    return Long.compare(timestamp, o.timestamp);
  }

}
