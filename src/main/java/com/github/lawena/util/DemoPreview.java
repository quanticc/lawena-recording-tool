package com.github.lawena.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DemoPreview extends RandomAccessFile {

  private static final Logger log = LoggerFactory.getLogger(DemoPreview.class);

  private final int maxStringLength = 260;
  private int demoProtocol;
  private int networkProtocol;
  private String demoStamp;
  private String playerName;
  private String mapName;
  private String serverName;
  private String gameDirectory;
  private double playbackTime;
  private int tickNumber;

  public DemoPreview(String demoName) throws FileNotFoundException {
    super(demoName, "r");
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

  public DemoPreview(Path demopath) throws FileNotFoundException {
    this(demopath.toString());
  }

  private String readString(int length) {
    byte[] aux = new byte[length];
    try {
      int read = read(aux);
      if (read < aux.length) {
        log.debug("Expected {} but read {} bytes", aux.length, read);
      }
      String result = new String(aux, Charset.forName("UTF-8"));
      return result.substring(0, result.indexOf(0));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private float readFloatBackwards() {
    byte[] aux = new byte[4];
    try {
      int read = read(aux);
      if (read < aux.length) {
        log.debug("Expected {} but read {} bytes", aux.length, read);
      }
      int value = 0;
      for (int i = 0; i < aux.length; i++) {
        value += (aux[i] & 0xff) << (8 * i);
      }
      return Float.intBitsToFloat(value);

    } catch (IOException e) {
      e.printStackTrace();
    }
    return 0;
  }

  private int readIntBackwards() {
    byte[] aux = new byte[4];
    try {
      int read = read(aux);
      if (read < aux.length) {
        log.debug("Expected {} but read {} bytes", aux.length, read);
      }
      int value = 0;
      for (int i = 0; i < aux.length; i++) {
        value += (aux[i] & 0xff) << (8 * i);
      }
      return value;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return 0;
  }

  private String formatSeconds(double seconds) {
    long s = (long) seconds;
    return String.format("%02d:%02d:%02d", TimeUnit.SECONDS.toHours(s),
        TimeUnit.SECONDS.toMinutes(s) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(s)),
        TimeUnit.SECONDS.toSeconds(s) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(s)));
  }

  @Override
  public String toString() {
    String str = "";
    str += "Stamp: " + demoStamp;
    str += "\nDemoProtocol: " + demoProtocol;
    str += "\nNetworkProtocol: " + networkProtocol;
    str += "\nGameDirectory: " + gameDirectory;
    str += "\nPlaybackTime: " + formatSeconds(playbackTime);
    str += "\nServer: " + serverName;
    str += "\nPlayer: " + playerName;
    str += "\nMap: " + mapName;
    str += "\nTicks: " + tickNumber;
    return str;
  }

}
