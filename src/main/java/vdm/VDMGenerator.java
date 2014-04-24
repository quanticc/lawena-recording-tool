package vdm;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import lwrt.SettingsManager;

public class VDMGenerator {

  private static final Logger log = Logger.getLogger("lawena");

  private List<Tick> ticklist;
  private SettingsManager cfg;

  public VDMGenerator(List<Tick> ticklist, SettingsManager cfg) {
    this.ticklist = ticklist;
    this.cfg = cfg;
  }

  public List<Path> generate() throws IOException {
    List<Path> paths = new ArrayList<>();
    Map<String, List<Tick>> demomap = new LinkedHashMap<>();
    Map<String, String> peeknext = new LinkedHashMap<>();
    String previous = null;
    for (Tick tick : ticklist) {
      List<Tick> ticks;
      if (!demomap.containsKey(tick.getDemoname())) {
        ticks = new ArrayList<>();
        demomap.put(tick.getDemoname(), ticks);
      } else {
        ticks = demomap.get(tick.getDemoname());
      }
      ticks.add(tick);
      if (previous != null) {
        if (!peeknext.containsKey(previous) && !previous.equals(tick.getDemoname())) {
          peeknext.put(previous, tick.getDemoname());
        }
      }
      previous = tick.getDemoname();
    }

    for (Entry<String, List<Tick>> e : demomap.entrySet()) {
      String demo = e.getKey();
      log.finer("Creating VDM file for demo: " + demo);
      List<String> lines = new ArrayList<>();
      lines.add("demoactions\n{");
      int count = 1;
      int previousEndTick = 0;
      for (Tick tick : e.getValue()) {
        if (cfg.getVdmSrcDemoFix()) {
          lines.add(segment(count++, "SkipAhead", "skip", "starttick \"" + (previousEndTick + 1)
              + "\""));
        } else {
          lines.add(segment(count++, "SkipAhead", "skip", "starttick \"" + (previousEndTick + 1)
              + "\"", "skiptotick \"" + (tick.getStart() - 500) + "\""));
        }
        lines.add(segment(count++, "PlayCommands", "startrec", "starttick \"" + tick.getStart()
            + "\"", "commands \"startrecording\""));
        lines.add(segment(count++, "PlayCommands", "stoprec",
            "starttick \"" + tick.getEnd() + "\"", "commands \"stoprecording\""));
        previousEndTick = tick.getEnd();
      }
      String nextdemo = peeknext.get(demo);
      if (nextdemo != null) {
        lines.add(segment(count++, "PlayCommands", "nextdem", "starttick \""
            + (previousEndTick + 1) + "\"", "commands \"playdemo " + nextdemo + "\""));
      } else {
        lines.add(segment(count++, "PlayCommands", "stopdem", "starttick \""
            + (previousEndTick + 1) + "\"", "commands \"stopdemo\""));
      }
      lines.add("}\n");

      Path added =
          Files.write(cfg.getTfPath().resolve(demo.substring(0, demo.indexOf(".dem")) + ".vdm"),
              lines, Charset.defaultCharset());
      paths.add(added);
      log.fine("VDM file written to " + added);

    }
    return paths;
  }

  public String segment(int count, String factory, String name, String... args) {
    StringBuilder sb = new StringBuilder();
    sb.append("\t\"" + count + "\"\n");
    sb.append("\t{\n");
    sb.append("\t\tfactory \"" + factory + "\"\n");
    sb.append("\t\tname \"" + name + "\"\n");
    for (String arg : args) {
      sb.append("\t\t" + arg + "\n");
    }
    sb.append("\t}");
    return sb.toString();
  }
}
