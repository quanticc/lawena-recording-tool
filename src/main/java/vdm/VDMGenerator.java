package vdm;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import lwrt.SettingsManager;
import lwrt.SettingsManager.Key;
import util.Util;
import vdm.Tick.AbstractExec;
import vdm.Tick.Exec;
import vdm.Tick.Record;
import vdm.Tick.Tick;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

class VDMGenerator {

    private static final Logger log = Logger.getLogger("lawena");
    private static final String n = System.getProperty("line.separator");

    private List<Tick> ticklist;
    private SettingsManager cfg;

    public VDMGenerator(List<Tick> ticklist, SettingsManager cfg) {
        this.ticklist = ticklist;
        this.cfg = cfg;
    }

    private static String segment(int count, String factory, String name, String... args) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t\"").append(count).append("\"").append(n);
        sb.append("\t{").append(n);
        sb.append("\t\tfactory \"").append(factory).append("\"").append(n);
        sb.append("\t\tname \"").append(name).append("\"").append(n);
        for (String arg : args) {
            sb.append("\t\t").append(arg).append(n);
        }
        sb.append("\t}");
        return sb.toString();
    }

    public List<Path> generate() throws IOException {
        List<Path> paths = new ArrayList<>();
        Map<String, Set<Tick>> demomap = new LinkedHashMap<>();
        Map<String, String> peeknext = new LinkedHashMap<>();
        String previous = null;
        for (Tick tick : ticklist) {
            Set<Tick> ticks = demomap.computeIfAbsent(tick.getDemoName(),
                k -> new TreeSet<>(Comparator.comparing(Tick::getDemoName)
                    .thenComparingInt(Tick::getStart)));
            ticks.add(tick);
            if (previous != null) {
                if (!peeknext.containsKey(previous) && !previous.equals(tick.getDemoName())) {
                    peeknext.put(previous, tick.getDemoName());
                }
            }
            previous = tick.getDemoName();
        }

        int cfgCount = 1;

        int padding = cfg.getInt(Key.VdmTickPadding);
        String skipStart = cfg.getString(Key.VdmSkipStartCommand);
        String skipStop = cfg.getString(Key.VdmSkipStopCommand);
        String rawSkipMode = cfg.getString(Key.VdmSkipMode);
        SkipMode skipMode = SkipMode.SKIP_AHEAD;
        try {
            skipMode = SkipMode.valueOf(rawSkipMode);
        } catch (IllegalArgumentException ex) {
            log.warning("Invalid value detected for skip mode: " + rawSkipMode);
        }

        for (Entry<String, Set<Tick>> e : demomap.entrySet()) {
            String demo = e.getKey();
            log.finer("Creating VDM file for demo: " + demo);
            List<String> lines = new ArrayList<>();
            lines.add("demoactions" + n + "{");
            int count = 1;
            int previousEndTick = 0;
            for (Tick tick : e.getValue()) {
                int safeStart = Math.max(0, tick.getStart() - padding);
                // no need to skip if the next segment is closer than the padding length
                boolean needsSkip = previousEndTick + 1 < safeStart;
                if (needsSkip) {
                    if (skipMode == SkipMode.DEMO_TIMESCALE) {
                        lines.add(segment(count++, "PlayCommands", "startskip", "starttick \""
                            + (previousEndTick + 1) + "\"", "commands \"" + skipStart + "\""));
                        lines.add(segment(count++, "PlayCommands", "stopskip", "starttick \"" + safeStart + "\"",
                            "commands \"" + skipStop + "\""));
                    } else if (skipMode == SkipMode.SKIP_AHEAD) {
                        lines.add(segment(count++, "SkipAhead", "skip", "starttick \"" + (previousEndTick + 1)
                            + "\"", "skiptotick \"" + safeStart + "\""));
                    }
                }
                String command = "startrecording";
                if (tick.getSegment().startsWith("exec")) {
                    command = ((AbstractExec) tick).getCommand(cfgCount);

					/*
                        CFG file generation from Exec and ExecRecord segments
						-----------------------------------------------------
						Lawena will create CFG files with the commands entered in the template. For CFG generation to
						work, the template must not be empty and also must not begin with "exec ", in which case it's
						assumed that you're calling an already created CFG file that's present in lawena/cfg folder,
						to be moved upon game launch.

						Template variable expansion
						---------------------------
						You can define certain variables in your exec/exec+record templates. These are:

						- {{BVH_PATH}} resolves into the full path of a BVH named the same as the demo, located in
						your TF dir.
						- {{TF_PATH}} and {{MOVIE_PATH}} for the absolute paths of TF and your movie folder,
						respectively.
						- {{DEMO_NAME}} and {{DEMO_PATH}} for the name of the demo and the full path of it. Also, an
						additional {{DEMO_PATH_NOEXT}} is given with the full path of the demo without the file
						extension. In this way, {{BVH_PATH}} is created by appending ".bvh" to {{DEMO_PATH_NOEXT}}.
					    - {{LAWENA_PATH}} resolves into the absolute location of the Lawena folder.
						- {{NEW_LINE}} resolves into a new line.
					 */
                    String demoCfgName = Util.stripFilenameExtension(tick.getDemoFile().getName());
                    if (!tick.getTemplate().equals(Record.Template)
                        && !tick.getTemplate().isEmpty()
                        && !tick.getTemplate().toLowerCase().startsWith("exec ")) {

                        log.info("Generating template #" + cfgCount + " for Tick " + tick);
                        Map<String, Object> scopes = new HashMap<>();
                        scopes.put("TF_PATH", cfg.getTfPath().toAbsolutePath());
                        scopes.put("MOVIE_PATH", cfg.getMoviePath().toAbsolutePath());
                        scopes.put("DEMO_NAME", demoCfgName);
                        scopes.put("DEMO_PATH", tick.getDemoFile().getAbsoluteFile());
                        scopes.put("DEMO_PATH_NOEXT", cfg.getTfPath().toAbsolutePath().resolve(demoCfgName));
                        scopes.put("BVH_PATH", cfg.getTfPath().toAbsolutePath().resolve(demoCfgName + ".bvh"));
                        scopes.put("LAWENA_PATH", Paths.get("").toAbsolutePath());
                        scopes.put("NEW_LINE", n);
                        Path outputPath = Paths.get("cfg", demoCfgName + "_" + cfgCount + ".cfg");
                        Files.deleteIfExists(outputPath);
                        try (Writer writer = Files.newBufferedWriter(outputPath, Charset.forName("UTF-8"))) {
                            MustacheFactory mf = new DefaultMustacheFactory();
                            Mustache mustache = mf.compile(new StringReader(tick.getTemplate()), demoCfgName);
                            mustache.execute(writer, scopes);
                            writer.flush();
                            paths.add(outputPath);
                            cfgCount++;
                        } catch (IOException ex) {
                            log.log(Level.WARNING, "Could not generate template", ex);
                        }
                    }
                }
                if (tick.getSegment().equals(Exec.Segment)) {
                    lines.add(segment(count++, "PlayCommands", tick.getSegment(), "starttick \"" + tick.getStart()
                        + "\"", "commands \"" + command + "\""));
                } else {
                    lines.add(segment(count++, "PlayCommands", "startrec", "starttick \"" + tick.getStart()
                        + "\"", "commands \"" + command + "\""));
                    lines.add(segment(count++, "PlayCommands", "stoprec",
                        "starttick \"" + tick.getEnd() + "\"", "commands \"stoprecording\""));
                }
                previousEndTick = tick.getEnd();
            }
            String nextdemo = peeknext.get(demo);
            if (nextdemo != null) {
                lines.add(segment(count, "PlayCommands", "nextdem", "starttick \""
                    + (previousEndTick + 1) + "\"", "commands \"playdemo " + nextdemo + "\""));
            } else {
                lines.add(segment(count, "PlayCommands", "stopdem", "starttick \""
                    + (previousEndTick + 1) + "\"", "commands \"stopdemo\""));
            }
            lines.add("}\n");

            // TODO: check for potential bugs for demos located in folders other than TF dir
            Path added =
                Files.write(
                    cfg.getTfPath()
                        .resolve(Util.stripFilenameExtension(demo) + ".vdm"), lines,
                    Charset.defaultCharset());
            paths.add(added);
            log.fine("VDM file written to " + added);

        }
        return paths;
    }
}
