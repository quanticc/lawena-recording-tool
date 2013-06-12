
package lwrt;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;

public class MovieManager {

    private String moviedir;

    public MovieManager(String dir) {
        moviedir = dir;
    }

    public void movieOffset() throws IOException {
        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".tga");
            }
        };

        String[] moviefiles = new File(moviedir).list(filter);
        String lastmovie = "";
        String alias = "alias namescroll stmov1";

        for (int i = 0; i < moviefiles.length; ++i) {
            lastmovie = lastmovie.compareTo(moviefiles[i]) > 0 ? lastmovie : moviefiles[i];
        }

        if (!lastmovie.equals("")) {
            switch (lastmovie.charAt(0)) {
                case 'a':
                    alias = "alias namescroll stmov2";
                    break;
                case 'b':
                    alias = "alias namescroll stmov3";
                    break;
                case 'c':
                    alias = "alias namescroll stmov4";
                    break;
                case 'd':
                    alias = "alias namescroll stmov5";
                    break;
                case 'e':
                    alias = "alias namescroll stmov6";
                    break;
                case 'f':
                    alias = "alias namescroll stmov7";
                    break;
                case 'g':
                    alias = "alias namescroll stmov8";
                    break;
                case 'h':
                    alias = "alias namescroll stmov9";
                    break;
                case 'i':
                    alias = "alias namescroll stmov10";
                    break;
                case 'j':
                    alias = "alias namescroll stmov11";
                    break;
                case 'k':
                    alias = "alias namescroll stmov12";
                    break;
                case 'l':
                    alias = "alias namescroll stmov13";
                    break;
                case 'm':
                    alias = "alias namescroll stmov14";
                    break;
                case 'n':
                    alias = "alias namescroll stmov15";
                    break;
                case 'o':
                    alias = "alias namescroll stmov16";
                    break;
                case 'p':
                    alias = "alias namescroll noslots";
                    break;
            }
        }

        PrintWriter pw = new PrintWriter(new FileWriter("cfg/namescroll.cfg"));
        pw.println(alias);
        pw.close();
    }

    public void createMovienameCfgs() throws IOException {
        String[] letters = {
                "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
                "o", "p"
        };
        String[] numbers = {
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13",
                "14", "15", "16"
        };

        for (int i = 0; i < 16; ++i) {
            PrintWriter pw = new PrintWriter(new FileWriter("cfg/mov/" + letters[i] + numbers[i]
                    + ".cfg"));
            pw.println("startmovie \"" + moviedir + "/" + letters[i] + numbers[i] + "_\"");
            pw.close();
        }
    }
}
