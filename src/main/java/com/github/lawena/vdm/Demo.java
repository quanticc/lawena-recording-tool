package com.github.lawena.vdm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demo extends RandomAccessFile {

  private static final Logger log = LoggerFactory.getLogger(Demo.class);
  private static final int maxStringLength = 260;

  private Path path;
  private int demoProtocol = 0;
  private int networkProtocol = 0;
  private String demoStamp = "";
  private String playerName = "";
  private String mapName = "";
  private String serverName = "";
  private String gameDirectory = "";
  private double playbackTime = 0.0;
  private int tickNumber = 0;
  private List<KillStreak> streaks;

  public Demo(Path demopath) throws FileNotFoundException {
    super(demopath.toString(), "r");
    path = demopath;
    try {
      demoStamp = readString(8);
      demoProtocol = readIntBackwards();
      networkProtocol = readIntBackwards();
      serverName = readString(maxStringLength);
      playerName = readString(maxStringLength);
      mapName = readString(maxStringLength);
      gameDirectory = readString(maxStringLength);
      playbackTime = readFloatBackwards();
      tickNumber = readIntBackwards();
    } catch (Exception e) {
      log.warn("Could not retrieve demo details", e);
    }
  }

  private String readString(int length) {
    byte[] aux = new byte[length];
    try {
      int read = read(aux);
      if (read < aux.length) {
        log.debug("Read {} bytes when {} were expected", read, aux.length);
      }
      String result = new String(aux, Charset.forName("UTF-8"));
      return result.substring(0, result.indexOf(0));
    } catch (IOException e) {
      log.warn("Could not read value from demo data: " + e);
    }
    return null;
  }

  private float readFloatBackwards() {
    byte[] aux = new byte[4];
    try {
      int read = read(aux);
      if (read < aux.length) {
        log.debug("Read {} bytes when {} were expected", read, aux.length);
      }
      int value = 0;
      for (int i = 0; i < aux.length; i++) {
        value += (aux[i] & 0xff) << (8 * i);
      }
      return Float.intBitsToFloat(value);

    } catch (IOException e) {
      log.warn("Could not read value from demo data: " + e);
    }
    return 0;
  }

  private int readIntBackwards() {
    byte[] aux = new byte[4];
    try {
      int read = read(aux);
      if (read < aux.length) {
        log.debug("Read {} bytes when {} were expected", read, aux.length);
      }
      int value = 0;
      for (int i = 0; i < aux.length; i++) {
        value += (aux[i] & 0xff) << (8 * i);
      }
      return value;
    } catch (IOException e) {
      log.warn("Could not read value from demo data: " + e);
    }
    return 0;
  }

  private String formatSeconds(double seconds) {
    long s = (long) seconds;
    return String.format("%02d:%02d:%02d", TimeUnit.SECONDS.toHours(s),
        TimeUnit.SECONDS.toMinutes(s) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(s)),
        TimeUnit.SECONDS.toSeconds(s) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(s)));
  }

  public Path getPath() {
    return path;
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

  @Override
  public String toString() {
    return getPath().getFileName().toString();
  }

}
