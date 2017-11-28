package vdm.Tick;

import java.io.File;

public class InvalidTick extends Tick {
    public InvalidTick(File demoFile, String demoname, int start, int end, String segment, String tick_template, String reason) {
        super(demoFile, demoname, start, end, segment, tick_template);
        this.valid = false;
        this.reason = reason;
    }
}
