package vdm.Tick;

import util.Util;

import java.io.File;

public class ExecRecord extends AbstractExec {
    public static final String Segment = "exec_record";
    public static final String Template = "mirv_camimport start \"{{BVH_PATH}}\"";
    public static final String Text = "Add Exec + Record";

    public ExecRecord(File demoFile, String demoname, int start, int end, String template) throws NumberFormatException {
        super(demoFile, demoname, start, end, Segment, template);
        if (start >= end) {
            throw new NumberFormatException(String.format("end tick (%d) must be greater than start tick (%d)", end, start));
        }
    }

    public String getCommand(int count) {
        return "exec " + Util.stripFilenameExtension(getDemoFile().getName()) + "_" + count + "; startrecording";
    }
}
