package com.github.lawena.vdm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.model.LwrtSettings;
import com.github.lawena.model.LwrtSettings.Key;

public class DemoTableModel extends AbstractTableModel {

  private static final Logger log = LoggerFactory.getLogger(DemoTableModel.class);
  private static final long serialVersionUID = 1L;

  public enum Columns {
    DEMONAME, MAP, PLAYER, TICKS, TIME, SERVER, STREAKS;
  }

  private LwrtSettings cfg;
  private List<Demo> list = new ArrayList<>();
  private Path demosPath;

  public DemoTableModel(LwrtSettings cfg) {
    this.cfg = cfg;
  }

  public void setDemosPath(Path path) {
    this.demosPath = path;
    clear();
    log.info("Scanning for demos in folder: {}", path);
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*.dem")) {
      int row = getRowCount();
      for (Path demopath : stream) {
        list.add(new Demo(demopath));
      }
      fireTableRowsInserted(row, getRowCount() - 1);
    } catch (IOException e) {
      log.warn("Problem while scanning .dem files: " + e);
    }
    // TODO: Make KillStreak loading optional
    if (cfg.getBoolean(Key.LoadKillstreakFile)) {
      new SwingWorker<Void, Void>() {
        protected Void doInBackground() throws Exception {
          try {
            Path streaksPath = demosPath.resolve(cfg.getString(Key.KillstreakFileName));
            log.debug("Loading Killstreak data from {}", streaksPath);
            int lineNumber = 1;
            for (String line : Files.readAllLines(streaksPath, Charset.forName("UTF-8"))) {
              if (!line.isEmpty()) {
                try {
                  KillStreak streak = new KillStreak(line);
                  for (Demo demo : list) {
                    if (demo.getPath().getFileName().toString().equals(streak.getDemoname())) {
                      demo.getStreaks().add(streak);
                      break;
                    }
                  }
                } catch (IllegalArgumentException e) {
                  log.warn("Could not parse line {}", lineNumber);
                }
              }
              lineNumber++;
            }
          } catch (IOException e) {
            log.warn("Problem while reading KillStreaks.txt", e);
          }
          return null;
        };
      }.execute();
    }
  }

  public Path getDemosPath() {
    return demosPath;
  }

  public void clear() {
    int rowsCleared = list.size();
    if (rowsCleared > 0) {
      list.clear();
      fireTableRowsDeleted(0, rowsCleared - 1);
    }
  }

  @Override
  public int getRowCount() {
    return list.size();
  }

  @Override
  public int getColumnCount() {
    return Columns.values().length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    Demo demo = list.get(rowIndex);
    Columns c = Columns.values()[columnIndex];
    switch (c) {
      case DEMONAME:
        return demo;
      case MAP:
        return demo.getMapName();
      case PLAYER:
        return demo.getPlayerName();
      case SERVER:
        return demo.getServerName();
      case TICKS:
        return demo.getTickNumber();
      case TIME:
        return demo.getPlaybackTime();
      case STREAKS:
        return demo.getStreaks().toString().replaceAll("\\[|\\]", "");
      default:
        return "";
    }
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    Columns c = Columns.values()[columnIndex];
    switch (c) {
      case DEMONAME:
        return Demo.class;
      case TICKS:
        return Integer.class;
      default:
        return String.class;
    }
  }

  @Override
  public String getColumnName(int column) {
    Columns c = Columns.values()[column];
    switch (c) {
      case DEMONAME:
        return "Filename";
      case MAP:
        return "Map";
      case PLAYER:
        return "Player";
      case SERVER:
        return "Server";
      case TICKS:
        return "Total Ticks";
      case TIME:
        return "Total Time";
      case STREAKS:
        return "Killstreaks";
    }
    return super.getColumnName(column);
  }

  public void addDemo(Path path) {
    for (Demo demo : list) {
      if (demo.getPath().equals(path)) {
        log.warn(path + " was not added because it already exists in the list");
        return;
      }
    }
    try {
      int row = getRowCount();
      list.add(new Demo(path));
      fireTableRowsInserted(row, row);
    } catch (FileNotFoundException e) {
      log.warn(path + " was not added because the file does not exist");
    }
  }

  public void removeDemo(Path path) {
    int i = 0;
    for (Demo demo : list) {
      if (demo.getPath().equals(path)) {
        list.remove(i);
        fireTableRowsDeleted(i, i);
      }
      i++;
    }
  }

  public Demo getDemo(int row) {
    return list.get(row);
  }

}
