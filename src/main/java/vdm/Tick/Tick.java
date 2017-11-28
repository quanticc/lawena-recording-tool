package vdm.Tick;

import java.io.File;

abstract public class Tick {

    public String getSegment() {
        return segment;
    }

    private final String segment;

    private final String tickTemplate;
	private final String demoname;
	private final File demoFile;
	private int start;
	private int end;
    boolean valid;
    String reason;

    public boolean isValid() {
        return valid;
    }

    public String getReason() {
        return reason;
    }

	public Tick(File demoFile, String demoname, int start, int end, String segment, String tickTemplate) {
		this.demoFile = demoFile;
		this.demoname = demoname;
		this.start = start;
		this.end = end;
		this.segment = segment;
		this.tickTemplate = tickTemplate;
		this.valid = true;
	}

	public File getDemoFile() {
		return demoFile;
	}

	public String getDemoname() {
		return demoname;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public String getTemplate() {
		return tickTemplate;
	}

	@Override
	public String toString() {
		return demoname + ": " + start + "-" + end;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((demoname == null) ? 0 : demoname.hashCode());
		result = prime * result + end;
		result = prime * result + start;
		result = prime * result + ((segment == null) ? 0 : segment.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Tick other = (Tick) obj;
		if (demoname == null) {
			if (other.demoname != null) {
				return false;
			}
		} else if (!demoname.equals(other.demoname)) {
			return false;
		}
		if (end != other.end) {
			return false;
		}
		if (start != other.start) {
			return false;
		}
		if (segment == null) {
			if (other.segment != null) {
				return false;
			}
		} else if (!segment.equals(other.segment)) {
			return false;
		}
		return true;
	}
}
