
package com.github.iabarca.lwrt.vdm;

public class KillStreak {

    private String date;
    private String description;
    private String demoname;
    private int tick;

    public KillStreak(String line) {
        String[] tokens = line.split(" \\(\"|\\)|\" at |\\] |\\[");
        if (tokens.length < 5) {
            throw new IllegalArgumentException("Could not parse all fields from: " + line);
        }
        try {
            date = tokens[1];
            description = tokens[2];
            demoname = tokens[3].toLowerCase() + ".dem";
            tick = Integer.parseInt(tokens[4]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException x) {
        }
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getDemoname() {
        return demoname;
    }

    public int getTick() {
        return tick;
    }

    @Override
    public String toString() {
        return description + " at " + tick;
    }

}
