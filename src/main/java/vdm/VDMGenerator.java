package vdm;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import util.Util;
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

    int cfgCount = 1;

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
        String command = "startrecording";
        if (tick.getType().equals(Tick.EXEC_RECORD_SEGMENT)) {
          String demoCfgName = Util.stripFilenameExtension(tick.getDemoFile().getName());
          if (tick.getTemplate().equals(Tick.NO_TEMPLATE) || tick.getTemplate().isEmpty()) {
            // Assumes a default pre-created CFG file with the name of the demo located in TF dir
            log.warning("Template for tick " + tick.toString() + " does not have a template!");
          } else {
            /**
             * Config files auto-generated will be placed in the TF dir. Templates will be resolved
             * against it.
             * 
             * The following templates are available:
             * 
             * - {{BVH_PATH}} resolves into the full path of a BVH named the same as the demo,
             * located in your TF dir.
             * 
             * - {{TF_PATH}} and {{MOVIE_PATH}} for the absolute paths of TF and your movie folder,
             * respectively.
             * 
             * - {{DEMO_NAME}} and {{DEMO_PATH}} for the name of the demo and the full path of it.
             * Also, an additional {{DEMO_PATH_NOEXT}} is given with the full path of the demo
             * without the file extension. In this way, {{BVH_PATH}} is created by appending ".bvh"
             * to {{DEMO_PATH_NOEXT}}.
             * 
             * - {{LAWENA_PATH}} resolves into the absolute location of the Lawena folder.
             * 
             * - {{NEW_LINE}} resolves into a new line.
             */
            log.info("Generating template #" + cfgCount + " for Tick " + tick);
            Map<String, Object> scopes = new HashMap<>();
            scopes.put("TF_PATH", cfg.getTfPath().toAbsolutePath());
            scopes.put("MOVIE_PATH", cfg.getMoviePath().toAbsolutePath());
            scopes.put("DEMO_NAME", demoCfgName);
            scopes.put("DEMO_PATH", tick.getDemoFile().getAbsoluteFile());
            scopes.put("DEMO_PATH_NOEXT", cfg.getTfPath().toAbsolutePath().resolve(demoCfgName));
            scopes.put("BVH_PATH", cfg.getTfPath().toAbsolutePath().resolve(demoCfgName + ".bvh"));
            scopes.put("LAWENA_PATH", Paths.get("").toAbsolutePath());
            scopes.put("NEW_LINE", "\n");
            Path outputPath = Paths.get("cfg", demoCfgName + "_" + cfgCount + ".cfg");
            Files.deleteIfExists(outputPath);
            try (Writer writer = Files.newBufferedWriter(outputPath, Charset.forName("UTF-8"))) {
              MustacheFactory mf = new DefaultMustacheFactory();
              Mustache mustache = mf.compile(new StringReader(tick.getTemplate()), demoCfgName);
              mustache.execute(writer, scopes);
              writer.flush();
              paths.add(outputPath);
            } catch (IOException ex) {
              log.log(Level.WARNING, "Could not generate template", ex);
            }
          }
          command = "exec " + demoCfgName + "_" + (cfgCount++) + "; startrecording";
        }
        lines.add(segment(count++, "PlayCommands", "startrec", "starttick \"" + tick.getStart()
            + "\"", "commands \"" + command + "\""));
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

      // TODO: check for potential bugs for demos located in folders other than TF dir
      Path added =
          Files.write(
              cfg.getTfPath()
                  .resolve(Util.stripFilenameExtension(demo.replace("\\", "/")) + ".vdm"), lines,
              Charset.defaultCharset());
      paths.add(added);
      log.fine("VDM file written to " + added);

    }
    return paths;
  }

  public static String segment(int count, String factory, String name, String... args) {
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
