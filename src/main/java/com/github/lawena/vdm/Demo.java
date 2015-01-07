package com.github.lawena.vdm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("nls")
public class Demo {

  private static final Logger log = LoggerFactory.getLogger(Demo.class);

  private Path path;
  private int demoProtocol = 0;
  private int networkProtocol = 0;
  private String demoStamp = "?";
  private String playerName = "?";
  private String mapName = "?";
  private String serverName = "?";
  private String gameDirectory = "?";
  private double playbackTime = 0.0;
  private int tickNumber = 0;
  private List<KillStreak> streaks;

  public Demo(Path demopath) throws FileNotFoundException {
    path = demopath;
    try (DemoFile file = new DemoFile(demopath)) {
      Map<String, Object> map = file.scanContents();
      demoStamp = (String) map.get("demoStamp");
      demoProtocol = (int) map.get("demoProtocol");
      networkProtocol = (int) map.get("networkProtocol");
      serverName = (String) map.get("serverName");
      playerName = (String) map.get("playerName");
      mapName = (String) map.get("mapName");
      gameDirectory = (String) map.get("gameDirectory");
      playbackTime = (double) map.get("playbackTime");
      tickNumber = (int) map.get("tickNumber");
    } catch (FileNotFoundException e) {
      log.warn("No .dem file found at: {}", demopath);
      throw e;
    } catch (IOException e) {
      log.warn("Could not properly read .dem file: {}", e.toString());
    }
  }

  private static String formatSeconds(double seconds) {
    long s = (long) seconds;
    return String.format("%02d:%02d:%02d", TimeUnit.SECONDS.toHours(s),
        TimeUnit.SECONDS.toMinutes(s) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(s)),
        TimeUnit.SECONDS.toSeconds(s) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(s)));
  }

  public String getDemoStamp() {
    return demoStamp;
  }

  public int getDemoProtocol() {
    return demoProtocol;
  }

  public int getNetworkProtocol() {
    return networkProtocol;
  }

  public String getGameDirectory() {
    return gameDirectory;
  }

  public String getPlaybackTime() {
    return formatSeconds(playbackTime);
  }

  public String getServerName() {
    return serverName;
  }

  public String getPlayerName() {
    return playerName;
  }

  public String getMapName() {
    return mapName;
  }

  public int getTickNumber() {
    return tickNumber;
  }

  public List<KillStreak> getStreaks() {
    if (streaks == null) {
      streaks = new ArrayList<>();
    }
    return streaks;
  }

  public Path getPath() {
    return path;
  }

  @Override
  public String toString() {
    return path.getFileName().toString();
  }

}
