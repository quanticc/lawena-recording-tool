package vdm.Tick;

import java.io.File;

abstract public class Tick {

    public String getSegment() {
        return segment;
    }

    private final String segment;

    private final String tickTemplate;
	private final String demoName;
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

	public Tick(File demoFile, String demoName, int start, int end, String segment, String tickTemplate) {
		this.demoFile = demoFile;
		this.demoName = demoName;
		this.start = start;
		this.end = end;
		this.segment = segment;
		this.tickTemplate = tickTemplate;
		this.valid = true;
	}

	public File getDemoFile() {
		return demoFile;
	}

	public String getDemoName() {
		return demoName;
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
		return demoName + ": " + start + "-" + end;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((demoName == null) ? 0 : demoName.hashCode());
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
		if (demoName == null) {
			if (other.demoName != null) {
				return false;
			}
		} else if (!demoName.equals(other.demoName)) {
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
