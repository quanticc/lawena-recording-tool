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

import com.github.lawena.Messages;
import com.github.lawena.app.model.Settings;
import com.github.lawena.profile.Key;

public class DemoTableModel extends AbstractTableModel {

  static final Logger log = LoggerFactory.getLogger(DemoTableModel.class);
  private static final long serialVersionUID = 1L;

  public enum Columns {
    DEMONAME, MAP, PLAYER, TICKS, TIME, SERVER, STREAKS;
  }

  private Settings settings;
  List<Demo> list = new ArrayList<>();
  private Path demosPath;

  public DemoTableModel(Settings settings) {
    this.settings = settings;
  }

  public void setDemosPath(Path path) {
    if (this.demosPath != null) {
      Key.demosPath.setValueEx(settings, path.toAbsolutePath().toString());
      settings.save();
    }
    this.demosPath = path;
    clear();
    log.info("Scanning for demos in folder: {}", path); //$NON-NLS-1$
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*.dem")) { //$NON-NLS-1$
      int row = getRowCount();
      for (Path demopath : stream) {
        list.add(new Demo(demopath));
      }
      fireTableRowsInserted(row, getRowCount() - 1);
    } catch (IOException e) {
      log.warn("Problem while scanning .dem files: {}", e.toString()); //$NON-NLS-1$
    }
    final Path streaksPath = demosPath.resolve(Key.relativeKillstreakPath.getValue(settings));
    if (Key.loadKillstreaks.getValue(settings) && Files.exists(streaksPath)) {
      new SwingWorker<Void, Void>() {
        @Override
        protected Void doInBackground() throws Exception {
          try {
            log.debug("Loading Killstreak data from {}", streaksPath); //$NON-NLS-1$
            int lineNumber = 1;
            for (String line : Files.readAllLines(streaksPath, Charset.forName("UTF-8"))) { //$NON-NLS-1$
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
                  log.warn("Could not parse line {}", lineNumber); //$NON-NLS-1$
                }
              }
              lineNumber++;
            }
          } catch (IOException e) {
            log.warn("Problem while reading KillStreaks.txt: " + e); //$NON-NLS-1$
          }
          return null;
        }
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
        return demo.getStreaks().toString().replaceAll("\\[|\\]", ""); //$NON-NLS-1$ //$NON-NLS-2$
      default:
        return ""; //$NON-NLS-1$
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
        return Messages.getString("DemoTableModel.filename"); //$NON-NLS-1$
      case MAP:
        return Messages.getString("DemoTableModel.map"); //$NON-NLS-1$
      case PLAYER:
        return Messages.getString("DemoTableModel.player"); //$NON-NLS-1$
      case SERVER:
        return Messages.getString("DemoTableModel.server"); //$NON-NLS-1$
      case TICKS:
        return Messages.getString("DemoTableModel.ticks"); //$NON-NLS-1$
      case TIME:
        return Messages.getString("DemoTableModel.time"); //$NON-NLS-1$
      case STREAKS:
        return Messages.getString("DemoTableModel.streaks"); //$NON-NLS-1$
      default:
        break;
    }
    return super.getColumnName(column);
  }

  public void addDemo(Path path) {
    for (Demo demo : list) {
      if (demo.getPath().equals(path)) {
        log.warn("No .dem file found at: {}", path); //$NON-NLS-1$
        return;
      }
    }
    try {
      int row = getRowCount();
      list.add(new Demo(path));
      fireTableRowsInserted(row, row);
    } catch (FileNotFoundException e) {
      log.warn("No .dem file found at: {}", path); //$NON-NLS-1$
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
