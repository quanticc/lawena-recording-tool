package com.github.lawena.vdm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("nls")
public class DemoFile extends RandomAccessFile {

  private static final Logger log = LoggerFactory.getLogger(DemoFile.class);
  private static final int maxStringLength = 260;

  public DemoFile(Path demopath) throws FileNotFoundException {
    super(demopath.toString(), "r");
  }

  public Map<String, Object> scanContents() throws IOException {
    Map<String, Object> map = new HashMap<>();
    map.put("demoStamp", readString(8));
    map.put("demoProtocol", readIntBackwards());
    map.put("networkProtocol", readIntBackwards());
    map.put("serverName", readString(maxStringLength));
    map.put("playerName", readString(maxStringLength));
    map.put("mapName", readString(maxStringLength));
    map.put("gameDirectory", readString(maxStringLength));
    map.put("playbackTime", readFloatBackwards());
    map.put("tickNumber", readIntBackwards());
    return map;
  }

  private String readString(int length) throws IOException {
    byte[] aux = new byte[length];
    int read = read(aux);
    if (read < aux.length) {
      log.debug("Read {} bytes when {} were expected", read, aux.length);
    }
    String result = new String(aux, Charset.forName("UTF-8"));
    return result.substring(0, result.indexOf(0));
  }

  private float readFloatBackwards() throws IOException {
    byte[] aux = new byte[4];
    int read = read(aux);
    if (read < aux.length) {
      log.debug("Read {} bytes when {} were expected", read, aux.length);
    }
    int value = 0;
    for (int i = 0; i < aux.length; i++) {
      value += (aux[i] & 0xff) << (8 * i);
    }
    return Float.intBitsToFloat(value);
  }

  private int readIntBackwards() throws IOException {
    byte[] aux = new byte[4];
    int read = read(aux);
    if (read < aux.length) {
      log.debug("Read {} bytes when {} were expected", read, aux.length);
    }
    int value = 0;
    for (int i = 0; i < aux.length; i++) {
      value += (aux[i] & 0xff) << (8 * i);
    }
    return value;
  }

}
