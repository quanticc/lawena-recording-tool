package vdm.Tick;

import java.io.File;

public class Exec extends AbstractExec {
    public static final String Text = "Add Exec";
    public static final String Segment = "exec";
    public static final String Template = "exec spec_player";
    public static final String QuitTemplate = "exec quit";

    public Exec(File demoFile, String demoname, int start, String template) {
        super(demoFile, demoname, start, start, Segment, template);
    }

    public String getCommand(int count) {
        return getTemplate();
    }
}
