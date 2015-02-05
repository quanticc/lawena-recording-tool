package com.github.lawena.app.model;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SourceGames {

  private static final Logger log = LoggerFactory.getLogger(SourceGames.class);
  private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
  public static final Path path = Paths.get("lwrt/games.json"); //$NON-NLS-1$

  private boolean rememberChoice = false;
  private String selected;

  public String getSelected() {
    return selected;
  }

  public void setSelected(String selected) {
    this.selected = selected;
  }

  public boolean isRememberChoice() {
    return rememberChoice;
  }

  public void setRememberChoice(boolean rememberChoice) {
    this.rememberChoice = rememberChoice;
  }

  public void save() {
    try {
      Files.write(path, Arrays.asList(gson.toJson(this)), Charset.forName("UTF-8")); //$NON-NLS-1$
    } catch (IOException ex) {
      log.warn("Could not save games data: {}", ex.toString()); //$NON-NLS-1$
    }
  }

}
