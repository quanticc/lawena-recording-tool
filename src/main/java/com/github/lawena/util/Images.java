package com.github.lawena.util;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Images {

  INSTANCE;

  private static final Logger log = LoggerFactory.getLogger(Images.class);
  public static final String DEFAULT_LOCATION = "/com/github/lawena/";

  private Map<String, ImageIcon> map = new HashMap<>();

  public static Images getInstance() {
    return INSTANCE;
  }

  public static ImageIcon get(String resourceName) {
    return INSTANCE.getImage(DEFAULT_LOCATION, resourceName);
  }

  public static ImageIcon get(String prefix, String resourceName) {
    return INSTANCE.getImage(prefix, resourceName);
  }

  public ImageIcon getImage(String prefix, String resourceName) {
    String key = prefix + resourceName;
    ImageIcon icon = map.get(key);
    if (icon == null) {
      try {
        icon = new ImageIcon(Images.class.getResource(key));
        map.put(key, icon);
      } catch (Exception e) {
        log.warn("Could not load image at {}: {}", key, e);
        new ImageIcon();
      }
    }
    return icon;
  }

}
