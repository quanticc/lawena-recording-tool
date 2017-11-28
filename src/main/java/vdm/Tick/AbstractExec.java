package vdm.Tick;

import java.io.File;

public abstract class AbstractExec extends Tick {

	public AbstractExec(File demoFile, String demoName, int start, int end, String segment, String template) {
		super(demoFile, demoName, start, end, segment, template);
	}

	public abstract String getCommand(int count);
}
