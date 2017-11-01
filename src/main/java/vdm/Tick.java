package vdm;

import java.io.File;

class Tick {

	public static final String RECORD_SEGMENT = "record";
	public static final String EXEC_RECORD_SEGMENT = "exec_record";
	public static final String NO_TEMPLATE = "N/A";
	public static final String CAM_IMPORT_TEMPLATE = "mirv_camimport start \"{{BVH_PATH}}\"";

	private final String demoname;
	private final File demoFile;
	private int start;
	private int end;
	private String type;
	private String template;

	public Tick(File demoFile, String demoname, int start, int end) {
		this.demoFile = demoFile;
		this.demoname = demoname;
		this.start = start;
		this.end = end;
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

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
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
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}

}
