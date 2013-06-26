
package vdm;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

public class VDMGenerator {

    private static final Logger log = Logger.getLogger("lawena");

    private TickList ticklist;
    private String tfdir;

    public VDMGenerator(TickList ticklist, String tfdir) {
        this.ticklist = ticklist;
        this.tfdir = tfdir;
    }

    public void generate() throws IOException {
        PrintWriter vdm;
        TickList current = ticklist;
        String vdmcontent;
        String[] demolist = new String[1000];
        int i = 0;

        while (current != null) {
            boolean seen = false;
            for (int j = 0; j < i; ++j) {
                if (current.getDemoName().equals(demolist[j])) {
                    seen = true;
                    break;
                }
            }

            if (!seen) {
                demolist[i++] = current.getDemoName();
            }

            current = current.getNext();
        }

        for (int j = 0; j < i; ++j) {
            current = ticklist;
            vdmcontent = "demoactions\n{\n";
            int count = 1;
            int previousSecondTick = 0;

            while (current != null) {
                if (current.getDemoName().equals(demolist[j])) {

                    vdmcontent += "\t\"" + count++ + "\"\n" +
                            "\t{\n" +
                            "\t\tfactory \"SkipAhead\"\n" +
                            "\t\tname \"skip\"\n" +
                            "\t\tstarttick \"" + (previousSecondTick + 1) + "\"\n" +
                            "\t\tskiptotick \"" + (current.getFirstTick() - 1) + "\"\n" +
                            "\t}\n";

                    vdmcontent += "\t\"" + count++ + "\"\n" +
                            "\t{\n" +
                            "\t\tfactory \"PlayCommands\"\n" +
                            "\t\tname \"startRecording\"\n" +
                            "\t\tstarttick \"" + current.getFirstTick() + "\"\n" +
                            "\t\tcommands \"startrecording\"\n" +
                            "\t}\n" +
                            "\t\"" + count++ + "\"\n" +
                            "\t{\n" +
                            "\t\tfactory \"PlayCommands\"\n" +
                            "\t\tname \"stopRecording\"\n" +
                            "\t\tstarttick \"" + current.getSecondTick() + "\"\n" +
                            "\t\tcommands \"stoprecording\"\n" +
                            "\t}\n";

                    previousSecondTick = current.getSecondTick();
                }

                current = current.getNext();
            }

            if (j + 1 < i) {
                vdmcontent += "\t\"" + count++ + "\"\n" +
                        "\t{\n" +
                        "\t\tfactory \"PlayCommands\"\n" +
                        "\t\tname \"nextDemo\"\n" +
                        "\t\tstarttick \"" + (previousSecondTick + 1) + "\"\n" +
                        "\t\tcommands \"playdemo " + demolist[j + 1] + "\"\n" +
                        "\t}\n";
            }
            else if (j + 1 == i) {
                vdmcontent += "\t\"" + count++ + "\"\n" +
                        "\t{\n" +
                        "\t\tfactory \"PlayCommands\"\n" +
                        "\t\tname \"stopDemo\"\n" +
                        "\t\tstarttick \"" + (previousSecondTick + 1) + "\"\n" +
                        "\t\tcommands \"stopdemo\"\n" +
                        "\t}\n";
            }

            vdmcontent += "}\n";

            String filename = tfdir + "/" + demolist[j].substring(0, demolist[j].indexOf(".dem"))
                    + ".vdm";
            vdm = new PrintWriter(new FileWriter(filename));
            vdm.print(vdmcontent);
            log.fine("VDM file written to " + filename);
            vdm.close();
        }
    }
}
