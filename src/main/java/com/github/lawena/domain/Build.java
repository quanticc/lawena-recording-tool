package com.github.lawena.domain;

import java.util.Objects;

public class Build implements Comparable<Build> {

    public static final Build LATEST = new Build("", "", "Latest", Long.MAX_VALUE);

    private final String name;
    private final String version;
    private final String describe;
    private final long timestamp;

    public Build(String name, String version, String describe, long timestamp) {
        this.name = name;
        this.version = version;
        this.describe = describe;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescribe() {
        return describe;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Build [" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", describe='" + describe + '\'' +
                ", timestamp=" + timestamp +
                ']';
    }

    @Override
    public int compareTo(Build o) {
        return Long.compare(o.timestamp, timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Build build = (Build) o;
        return timestamp == build.timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp);
    }
}
