package com.github.lawena.update;

import java.util.List;
import java.util.SortedSet;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a set of elements to retrieve a deployment descriptor used to switch an application to
 * a specific context.
 * 
 * @author Ivan
 *
 */
public class Branch {

  public enum Type {
    /**
     * Static branches do not use the updater system. They exist as a repository for a release or a
     * version that will not be updated in the future.
     */
    @SerializedName("static")
    STATIC,
    /**
     * Versioned branches adhere strictly to the updater system and will be auto-updated as soon as
     * Getdown detects a newer version.
     */
    @SerializedName("versioned")
    VERSIONED,
    /**
     * Snapshot branches allow updating manually and also rollbacking to previous versions. Updater
     * class provides a way to retrieve the builds available for a specific snapshot branch.
     */
    @SerializedName("snapshot")
    SNAPSHOT;
  }

  /**
   * Represents a branch without a deployment descriptor
   */
  public static final Branch STANDALONE = new Branch("standalone");

  private String id;
  private String name;
  private Type type;
  private String description = "";
  private String github = "";
  private String compare = "";
  private String url = "";

  private transient SortedSet<Build> builds;
  private transient List<String> changeLog;

  private Branch(String id) {
    this.id = id;
    this.name = id;
    this.type = Type.STATIC;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Type getType() {
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

  public SortedSet<Build> getBuilds() {
    return builds;
  }

  public void setBuilds(SortedSet<Build> builds) {
    this.builds = builds;
  }

  public List<String> getChangeLog() {
    return changeLog;
  }

  public void setChangeLog(List<String> changeLog) {
    this.changeLog = changeLog;
  }

  @Override
  public String toString() {
    return name + " (" + type.toString().toLowerCase() + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
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
    Branch other = (Branch) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }


}
