package com.github.lawena.update;

@SuppressWarnings("nls")
public class Build implements Comparable<Build> {

    public static final Build LATEST = new Build("", "Latest", Long.MAX_VALUE);

    private String name;
    private String describe;
    private long timestamp;

    public Build() {
    }

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

    public final String getName() {
        return name;
    }

    public final String getDescribe() {
        return describe;
    }

    public final long getTimestamp() {
        return timestamp;
    }

    @Override
    public final String toString() {
        return describe + " " + name;
    }

    @Override
    public final int compareTo(Build o) {
        return Long.compare(o.timestamp, timestamp);
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public final boolean equals(Object obj) {
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
