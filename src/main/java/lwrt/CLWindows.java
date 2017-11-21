package lwrt;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

public class CLWindows extends CommandLine {

	private String hl2 = "hl2.exe";

	@Override
	public ProcessBuilder getBuilderStartTF2(String gamePath) {
		Path path = Paths.get(gamePath, "..", "hl2.exe");
		try {
			path = path.toRealPath();
		} catch (IOException e) {
			log.warning("Could not obtain real path of game executable: " + e.toString());
		}
		return new ProcessBuilder(path.toString());

	}

	@Override
	public ProcessBuilder getBuilderStartSteam(String steamPath) {
		return new ProcessBuilder(steamPath + File.separator + "steam.exe");
	}

	@Override
	public ProcessBuilder getBuilderStartHLAE(String hlaePath, String gamePath) {
		Path hl2Path = Paths.get(gamePath).resolve(".." + File.separator + "hl2.exe");
		try {
			hl2Path = hl2Path.toRealPath();
		} catch (IOException e) {
			log.warning("Could not obtain real path of game executable: " + e.toString());
		}
		Path hookPath = Paths.get(hlaePath).resolveSibling("AfxHookSource.dll");
		try {
			hookPath = hookPath.toRealPath();
		} catch (IOException e) {
			log.warning("Could not obtain real path of HLAE Source hook DLL: " + e.toString());
		}
		return new ProcessBuilder(hlaePath, "-customLoader", "-autoStart",
				"-hookDllPath", hookPath.toString(), "-programPath", hl2Path.toString(), "-cmdLine");
	}

	@Override
	public ProcessBuilder getBuilderTF2ProcessKiller() {
		return new ProcessBuilder("taskkill", "/F", "/IM", hl2);
	}

	@Override
	public ProcessBuilder getBuilderHLAEProcessKiller() {
		return new ProcessBuilder("taskkill", "/F", "/IM", "HLAE.exe");
	}

	@Override
	public ProcessBuilder getBuilderVTFCmd(String skyboxFilename) {
		return new ProcessBuilder("vtfcmd\\VTFCmd.exe", "-file", "skybox\\" + skyboxFilename,
				"-output", "skybox", "-exportformat", "png");
	}

	@Override
	public Path getSteamPath() {
		return Paths.get(regQuery("HKEY_CURRENT_USER\\Software\\Valve\\Steam", "SteamPath"));
	}

	@Override
	public boolean isValidSteamPath(Path p) {
		// value must not be empty
		// value must represent a directory named "Steam" (case insensitive)
		// the directory must have a steam.exe file inside
		String s = p.toString();
		return (!s.isEmpty() && Files.isDirectory(p)
				&& p.getFileName().toString().equalsIgnoreCase("Steam") && Files.exists(p
				.resolve("steam.exe")));
	}

	@Override
	public String getSystemDxLevel() {
		try {
			return regQuery("HKEY_CURRENT_USER\\Software\\Valve\\Source\\tf\\Settings", "DXLevel_V1");
		} catch (IndexOutOfBoundsException e) {
			log.warning("Could not read registry dxlevel value: " + e.toString() + " -- Using dxlevel 95");
			return "5f";
		}
	}

	@Override
	public void setSystemDxLevel(String dxlevel) {
		regedit("HKEY_CURRENT_USER\\Software\\Valve\\Source\\tf\\Settings", "DXLevel_V1", dxlevel);
	}

	@Override
	public boolean isRunningTF2() {
		String line;
		ProcessBuilder[] builders =
				{
						new ProcessBuilder("tasklist", "/fi", "\"imagename eq " + hl2 + "\"", "/nh", "/fo",
								"csv"),
						new ProcessBuilder("cscript", "//NoLogo", new File("batch\\procchk.vbs").getPath(), hl2)};
		for (ProcessBuilder pb : builders) {
			try {
				Process p = pb.start();
				try (BufferedReader input = newProcessReader(p)) {
					while ((line = input.readLine()) != null) {
						log.finest("[" + pb.command().get(0) + "] " + line);
						if (line.contains(hl2)) {
							log.finer("TF2 process detected by " + pb.command().get(0));
							return true;
						}
					}
				}
			} catch (IOException e) {
				log.log(Level.INFO, "Problem while finding if TF2 is running", e);
			}
		}
		log.finer("TF2 process not detected");
		return false;
	}

	private void regedit(String key, String value, String content) {
		try {
			ProcessBuilder pb = new ProcessBuilder("batch\\rg.bat", key, value, content);
			Process pr = pb.start();
			pr.waitFor();
		} catch (InterruptedException | IOException e) {
			log.log(Level.INFO, "", e);
		}
	}

	private String regQuery(String key, String value) {
		StringBuilder result = new StringBuilder();
		try {
			ProcessBuilder pb = new ProcessBuilder("reg", "query", key, "/v", value);
			Process pr = pb.start();
			try (BufferedReader input = newProcessReader(pr)) {
				String line;
				while ((line = input.readLine()) != null) {
					result.append(line).append('\n');
				}
			}
			pr.waitFor();
		} catch (InterruptedException | IOException e) {
			log.log(Level.INFO, "", e);
		}
		try {
			if (result.lastIndexOf("0x") > 0) {
				return result.substring(result.lastIndexOf("0x") + 2,
						result.indexOf("\n", result.lastIndexOf("0x")));
			} else {
				String str = "REG_SZ    ";
				return result.substring(result.lastIndexOf(str) + str.length(),
						result.indexOf("\n", result.lastIndexOf(str)));
			}
		} catch (IndexOutOfBoundsException e) {
			log.fine("Data not found at value " + value + " for key " + key);
			return "";
		}
	}

	@Override
	public Path resolveVpkToolPath(Path tfpath) {
		return tfpath.resolve(
		    String.join(File.separator, "..", "bin", "vpk.exe")
        );
	}

	@Override
	public void openFolder(Path dir) {
		try {
			Process pr = Runtime.getRuntime().exec("explorer.exe /select," + dir.toString());
			pr.waitFor();
		} catch (IOException | InterruptedException e) {
			// fallback to Java desktop API
			super.openFolder(dir);
		}
	}

	private void closeHandle(String pid, String handle) {
		try {
			ProcessBuilder pb = new ProcessBuilder("batch\\handle.exe", "-c", handle, "-p", pid, "-y");
			Process pr = pb.start();
			try (BufferedReader input = newProcessReader(pr)) {
				String line;
				int count = 0;
				while ((line = input.readLine()) != null) {
					if (count > 7) {
						log.info("[handle] " + line);
					}
					count++;
				}
			}
			pr.waitFor();
		} catch (InterruptedException | IOException e) {
			log.log(Level.INFO, "", e);
		}
	}

	@Override
	public void closeHandles(Path path) {
		try {
			ProcessBuilder pb = new ProcessBuilder("batch\\handle.exe", path.toString());
			Process pr = pb.start();
			try (BufferedReader input = newProcessReader(pr)) {
				String line;
				int count = 0;
				while ((line = input.readLine()) != null) {
					if (count > 4) {
						String[] columns = line.split("[ ]+type: [A-Za-z]+[ ]+|: |[ ]+pid: ");
						if (columns.length == 4) {
							log.info("[handle] Closing handle " + columns[3] + " opened by " + columns[0]);
							closeHandle(columns[1], columns[2]);
						} else {
							log.info("[handle] " + line);
						}
					}
					count++;
				}
			}
			pr.waitFor();
		} catch (InterruptedException | IOException e) {
			log.log(Level.INFO, "", e);
		}
	}

	@Override
	public void delete(Path path) {
		try {
			ProcessBuilder pb = new ProcessBuilder("del", "/f", "/s", "/q", "/a", path.toString());
			Process pr = pb.start();
			try (BufferedReader input = newProcessReader(pr)) {
				String line;
				while ((line = input.readLine()) != null) {
					log.info("[delete] " + line);
				}
			}
			pr.waitFor();
		} catch (InterruptedException | IOException e) {
			log.log(Level.INFO, "", e);
		}
	}

	@Override
	public void registerFonts(Path path) {
		try {
			ProcessBuilder pb = new ProcessBuilder("batch\\FontReg.exe", "/copy");
			pb.directory(path.toAbsolutePath().toFile());
			Process pr = pb.start();
			int code = pr.waitFor();
			if (code != 0) {
				log.warning("[FontReg] Process at " + pb.directory() + " returned with exit code: " + code);
			} else {
				log.info("[FontReg] Registered all fonts found in " + pb.directory());
			}
		} catch (InterruptedException | IOException e) {
			log.log(Level.INFO, "Could not launch FontReg process", e);
		}
	}
}
