
package com.github.iabarca.lwrt.lwrt;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.iabarca.lwrt.managers.Profiles;

public class MovieManager {

    private SettingsManager cfg;
    private Profiles profiles;

    public MovieManager(SettingsManager cfg) {
        this.cfg = cfg;
    }
    
    public MovieManager(Profiles profiles) {
        this.profiles = profiles;
    }

    public void movieOffset() throws IOException {
        String lastmovie = "";
        String alias = "alias namescroll stmov1";
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                cfg.getMoviePath(), "*.tga")) {
            for (Path moviefile : stream) {
                String filename = moviefile.getFileName().toString();
                lastmovie = (lastmovie.compareTo(filename) > 0 ? lastmovie : filename);
            }
        }
        if (!lastmovie.equals("")) {
            int idx = "abcdefghijklmno".indexOf(lastmovie.charAt(0));
            if (idx >= 0) {
                alias = "alias namescroll stmov" + (idx + 2);
            } else if (lastmovie.charAt(0) == 'p') {
                alias = "alias namescroll noslots";
            }
        }
        PrintWriter pw = new PrintWriter(new FileWriter("cfg/namescroll.cfg"));
        pw.println(alias);
        pw.close();
    }

    public void createMovienameCfgs() throws IOException {
        String[] prefixes = {
                "a1", "b2", "c3", "d4", "e5", "f6", "g7", "h8", "i9", "j10", "k11", "l12", "m13",
                "n14", "o15", "p16"
        };
        for (String prefix : prefixes) {
            PrintWriter pw = new PrintWriter(new FileWriter("cfg/mov/" + prefix + ".cfg"));
            pw.println("startmovie \"" + cfg.getMoviePath() + "/" + prefix + "_\"");
            pw.close();
        }
    }
}
