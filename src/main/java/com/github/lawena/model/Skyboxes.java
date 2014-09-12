package com.github.lawena.model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import com.github.lawena.util.Serials;

public class Skyboxes implements Serializable {

  private static final long serialVersionUID = 1L;

  private Map<String, ImageIcon> map = new HashMap<>();

  public Map<String, ImageIcon> getMap() {
    return map;
  }

  public void load(File src) throws ClassNotFoundException, IOException {
    Skyboxes loaded = Serials.gzRead(Skyboxes.class, src);
    map.putAll(loaded.map);
  }

  public void save(File dest) throws IOException {
    Serials.gzWrite(this, dest);
  }

}
