package lwrt;

import lwrt.SettingsManager.Key;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

class MovieManager {

	private static final Logger log = Logger.getLogger("lawena");

	private SettingsManager cfg;

	public MovieManager(SettingsManager cfg) {
		this.cfg = cfg;
	}

	public void movieOffset() throws IOException {
		String lastmovie = "";
		String alias = "alias namescroll stmov1";
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(cfg.getMoviePath(), "*.tga")) {
			for (Path moviefile : stream) {
				String filename = moviefile.getFileName().toString();
				lastmovie = (lastmovie.compareTo(filename) > 0 ? lastmovie : filename);
			}
		}
		if (!lastmovie.equals("")) {
			int idx = "abcdefghijklmnopqrstuvwxy".indexOf(lastmovie.charAt(0));
			if (idx >= 0) {
				alias = "alias namescroll stmov" + (idx + 2);
			} else if (lastmovie.charAt(0) == 'z') {
				alias = "alias namescroll noslots";
			}
		}
		Files.write(Paths.get("cfg", "namescroll.cfg"), Collections.singletonList(alias), Charset.forName("UTF-8"));
	}

	public void createMovienameCfgs() throws IOException {
		String[] prefixes =
				{"a1", "b2", "c3", "d4", "e5", "f6", "g7", "h8", "i9", "j10", "k11", "l12", "m13", "n14",
						"o15", "p16", "q17", "r18", "s19", "t20", "u21", "v22", "w23", "x24", "y25", "z26"};
		String video = cfg.getString(Key.SourceRecorderVideoFormat);
		String audio = cfg.getString(Key.SourceRecorderAudioFormat);
		int quality = cfg.getInt(Key.SourceRecorderJpegQuality);
		Path folder = Paths.get("cfg", "mov");
		if (!Files.exists(folder)) {
			Files.createDirectories(folder);
		}
		String moviePath = "";
		if (cfg.getMoviePath().startsWith(cfg.getTfPath())) {
			try {
				Path movieRelatedToGame = cfg.getTfPath().relativize(cfg.getMoviePath());
				moviePath = movieRelatedToGame.toString();
			} catch (IllegalArgumentException e) {
				// different root
				log.info("Cannot relativize path: " + e.toString());
			}
		} else if (shareSameRoot(cfg.getMoviePath(), cfg.getTfPath())) {
			moviePath = cfg.getMoviePath().toString().replaceFirst("^[A-Z]:(.*)$", "$1");
		} else {
			moviePath = cfg.getMoviePath().toString();
		}
		String escape = needsEscape(moviePath) ? "\"" : "";
		moviePath = moviePath + (moviePath.isEmpty() ? "" : File.separator);
		log.info("Resolved movie recording path: " + moviePath);
		for (String prefix : prefixes) {
			String command = "startmovie " + escape + moviePath + prefix + "_" + escape + " " +
					video + " " + audio + (video.equals("jpg") ? " jpeg_quality " + quality : "");
			List<String> lines = Collections.singletonList(command);
			Files.write(Paths.get("cfg","mov", prefix + ".cfg"), lines, Charset.forName("UTF-8"));
		}
	}

	private boolean shareSameRoot(Path path1, Path path2) {
		String root1 = path1.toString().replaceFirst("^([A-Za-z]):.*$", "$1");
		String root2 = path2.toString().replaceFirst("^([A-Za-z]):.*$", "$1");
		return root1.equals(root2);
	}

	private boolean needsEscape(String str) {
		return str != null && str.contains(" ");
	}
}
