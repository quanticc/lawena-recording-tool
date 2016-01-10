package com.github.lawena.domain;

import java.util.List;
import java.util.Objects;
import java.util.SortedSet;

public class Branch {

    public enum Type {
        /**
         * Static branches do not use the updater system. They exist as a repository for a release or a
         * version that will not be updated in the future.
         */
        STATIC,
        /**
         * Versioned branches adhere strictly to the updater system and will be auto-updated as soon as
         * Getdown detects a newer version.
         */
        VERSIONED,
        /**
         * Snapshot branches allow updating manually and also rollbacking to previous versions. Updater
         * class provides a way to retrieve the builds available for a specific snapshot branch.
         */
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

    private Branch() {

    }

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Branch branch = (Branch) o;
        return Objects.equals(id, branch.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
