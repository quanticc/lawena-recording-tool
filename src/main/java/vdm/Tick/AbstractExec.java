package vdm.Tick;

import java.io.File;

public abstract class AbstractExec extends Tick {

	public AbstractExec(File demoFile, String demoname, int start, int end, String segment, String template) {
		super(demoFile, demoname, start, end, segment, template);
	}

	public abstract String getCommand(int count);
}
