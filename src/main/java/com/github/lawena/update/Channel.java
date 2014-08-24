package com.github.lawena.update;

public class Channel {

  private String id;
  private String name;
  private String type;
  private String description;
  private String github;
  private String compare;
  private String url;

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public String getDescription() {
    return description;
  }

  public String getGithub() {
    return github;
  }

  public String getCompare() {
    return compare;
  }

  public String getUrl() {
    return url;
  }

  @Override
  public String toString() {
    return "Channel [id=" + id + ", name=" + name + ", type=" + type + ", description="
        + description + ", github=" + github + ", compare=" + compare + ", url=" + url + "]";
  }


}
