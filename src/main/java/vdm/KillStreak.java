
package vdm;

import java.util.logging.Logger;

public class KillStreak {

    private static final Logger log = Logger.getLogger("lawena");

    private String date;
    private String description;
    private String demoname;
    private int tick;

    public KillStreak(String line) {
        String[] tokens = line.split(" \\(\"|\\)|\" at |\\] |\\[");
        if (tokens.length < 4) {
            log.finer("Some fields unparsed: " + line);
        }
        try {
            date = tokens[0];
            description = tokens[1];
            demoname = tokens[2];
            tick = Integer.parseInt(tokens[3]);
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
