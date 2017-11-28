package vdm.Tick;

import java.io.File;

public class TickFactory {

    public static Tick makeTick(File demoFile, String demoname, int start, int end, String segment) {
        return makeTick(demoFile, demoname, start, end, segment, null);
    }

    public static Tick makeTick(File demoFile, String demoname, int start, int end, String segment, String template) {
        Tick t;
        try {
            switch (segment) {
                case Record.Segment:
                    t = new Record(demoFile, demoname, start, end);
                    break;
                case ExecRecord.Segment:
                    t = new ExecRecord(demoFile, demoname, start, end, (template == null || template.equals(Record.Template)) ? ExecRecord.Template : template);
                    break;
                case Exec.Segment:
                    t = new Exec(demoFile, demoname, start, (template == null || template.equals(Record.Template)) ? Exec.Template : template);
                    break;
                default:
                    t = new InvalidTick(demoFile, demoname, start, end, segment, template, "Unknown Segment Type");
            }
        } catch (NumberFormatException e) {
            t = new InvalidTick(demoFile, demoname, start, end, segment, template, e.getMessage());
        }
        return t;
    }
}
