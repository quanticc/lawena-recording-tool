package vdm;

public enum SkipMode {

	SKIP_AHEAD("Standard: Use SkipAhead VDM factory"), NO_SKIPS(
			"No tick skipping (older SrcDemo\u00B2 workaround)"), DEMO_TIMESCALE(
			"Run demo_timescale before each segment");

	private String description;

	SkipMode(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return description;
	}

}
