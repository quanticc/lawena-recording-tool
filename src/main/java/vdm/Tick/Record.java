package vdm.Tick;

import java.io.File;

public class Record extends Tick {
    public static final String Segment = "record";
    public static final String Template = "N/A";
    public static final String Text = "Add Record";

    public Record(File demoFile, String demoname, int start, int end) throws NumberFormatException {
        super(demoFile, demoname, start, end, Segment, Template);
        if (start >= end) {
            throw new NumberFormatException(String.format("end tick (%d) must be greater than start tick (%d)", end, start));
        }
    }
}
