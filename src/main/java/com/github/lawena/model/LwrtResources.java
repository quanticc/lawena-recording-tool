package com.github.lawena.model;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.model.LwrtResource.PathContents;
import com.github.lawena.os.OSInterface;
import com.github.lawena.util.ListFilesVisitor;

public class LwrtResources extends AbstractTableModel {

  private static final Logger log = LoggerFactory.getLogger(LwrtResources.class);

  public enum Column {
    SELECTED, PATH, CONTENTS;
  }

  private static final long serialVersionUID = 1L;

  private static final Map<Path, LwrtResource> defaultPaths = new LinkedHashMap<>();
  private static final List<Path> ignoredPaths = new ArrayList<>();

  public static final LwrtResource particles = new LwrtResource(
      Paths.get("custom/pldx_particles.vpk"), "Enable enhanced particles",
      EnumSet.of(PathContents.READONLY));

  {
    List<LwrtResource> list = new ArrayList<>();
    list.add(new LwrtResource(Paths.get("custom/default_cfgs.vpk"), "default_cfgs.vpk", EnumSet
        .of(PathContents.READONLY)));
    list.add(new LwrtResource(Paths.get("custom/no_announcer_voices.vpk"),
        "Disable announcer voices"));
    list.add(new LwrtResource(Paths.get("custom/no_applause_sounds.vpk"), "Disable applause sounds"));
    list.add(new LwrtResource(Paths.get("custom/no_domination_sounds.vpk"),
        "Disable domination/revenge sounds"));
    list.add(particles);
    for (LwrtResource path : list) {
      path.getContents().add(PathContents.DEFAULT);
      defaultPaths.put(path.getPath(), path);
    }
    ignoredPaths.add(Paths.get("custom/skybox.vpk"));
  }

  public static boolean accept(Path entry) throws IOException {
    return (Files.isDirectory(entry) || entry.toString().endsWith(".vpk"))
        && !ignoredPaths.contains(entry);
  }

  private List<LwrtResource> list = new ArrayList<>();
  private LwrtSettings cfg;
  private OSInterface cl;

  public LwrtResources(LwrtSettings cfg, OSInterface cl) {
    this.cfg = cfg;
    this.cl = cl;
  }

  public List<LwrtResource> getList() {
    return Collections.unmodifiableList(list);
  }

  private boolean isCustomHudSelected() {
    for (LwrtResource cp : list) {
      if (cp.getContents().contains(PathContents.HUD) && cp.isSelected()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isCellEditable(int row, int column) {
    LwrtResource cp = list.get(row);
    EnumSet<PathContents> conts = cp.getContents();
    boolean readonly = conts.contains(PathContents.READONLY);
    boolean hud = conts.contains(PathContents.HUD);
    return !readonly && column == Column.SELECTED.ordinal()
        && !(isCustomHudSelected() && hud && !cp.isSelected());
  }

  @Override
  public Class<?> getColumnClass(int column) {
    switch (Column.values()[column]) {
      case SELECTED:
        return Boolean.class;
      case PATH:
        return LwrtResource.class;
      default:
        return String.class;
    }
  }

  @Override
  public int getRowCount() {
    return list.size();
  }

  @Override
  public int getColumnCount() {
    return Column.values().length;
  }

  @Override
  public String getColumnName(int column) {
    switch (Column.values()[column]) {
      case CONTENTS:
        return "Type";
      case SELECTED:
        return "";
      case PATH:
        return "Custom Resource";
      default:
        return "";
    }
  }

  private String simplify(EnumSet<PathContents> c) {
    EnumSet<PathContents> set = EnumSet.copyOf(c);
    set.removeAll(EnumSet.of(PathContents.DEFAULT));
    return set.toString().replaceAll("\\[|\\]", "").replaceAll(", ", "\\+");
  }

  @Override
  public Object getValueAt(int row, int column) {
    LwrtResource cp = list.get(row);
    switch (Column.values()[column]) {
      case CONTENTS:
        return simplify(cp.getContents());
      case SELECTED:
        return cp.isSelected();
      case PATH:
        return cp;
      default:
        return "";
    }
  }

  @Override
  public void setValueAt(Object value, int row, int column) {
    LwrtResource cp = list.get(row);
    switch (Column.values()[column]) {
      case SELECTED:
        cp.setSelected((boolean) value);
        fireTableCellUpdated(row, column);
        return;
      default:
    }
  }

  private List<String> getContentsList(Path path) {
    if (path.toString().endsWith(".vpk")) {
      return cl.getVpkContents(cfg.getTfPath(), path);
    } else if (Files.isDirectory(path)) {
      ListFilesVisitor visitor = new ListFilesVisitor(path);
      try {
        Set<FileVisitOption> set = new HashSet<>();
        set.add(FileVisitOption.FOLLOW_LINKS);
        Files.walkFileTree(path, set, 3, visitor);
        return visitor.getFiles();
      } catch (IllegalArgumentException | IOException e) {
        log.warn("Could not walk through the file tree", e);
      }
    }
    return Collections.emptyList();
  }

  public void update(LwrtResource cp) {
    EnumSet<PathContents> c = cp.getContents();
    Path path = cp.getPath();
    if (!c.contains(PathContents.READONLY)) {
      c.retainAll(EnumSet.of(PathContents.DEFAULT));
      List<String> files = getContentsList(path);
      for (String file : files) {
        if (file.startsWith("resource/") || file.startsWith("scripts/")) {
          c.add(PathContents.HUD);
        } else if (file.startsWith("cfg/") && file.endsWith(".cfg")) {
          c.add(PathContents.CONFIG);
        } else if (file.startsWith("materials/skybox/")) {
          c.add(PathContents.SKYBOX);
        }
      }
    }
  }

  private void addRow(LwrtResource cp) {
    int row = getRowCount();
    list.add(cp);
    update(cp);
    fireTableRowsInserted(row, row);
  }

  private void insertRow(int index, LwrtResource e) {
    list.add(index, e);
    update(e);
    fireTableRowsInserted(index, index);
  }

  public LwrtResource getPath(int index) {
    return list.get(index);
  }

  public void clear() {
    int size = list.size();
    if (size > 0) {
      list.clear();
      fireTableRowsDeleted(0, size - 1);
    }
  }

  public void insertPath(int index, Path path) throws IOException {
    if (accept(path)) {
      LwrtResource cp = defaultPaths.get(path);
      if (cp == null) {
        cp = new LwrtResource(path);
      }
      if (index > 0) {
        insertRow(index, cp);
      } else {
        addRow(cp);
      }
    }
  }

  public void addPath(Path path) throws IOException {
    insertPath(-1, path);
  }

  public void addPaths(Path dir) {
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
      for (Path path : stream) {
        addPath(path);
      }
    } catch (NoSuchFileException e) {
      log.debug(dir + " does not exist, not scanning this path");
    } catch (IOException e) {
      log.warn("Problem while loading custom paths", e);
    }
  }

  public void updatePath(Path path) {
    int i = 0;
    for (LwrtResource cp : list) {
      if (cp.getPath().equals(path)) {
        update(cp);
        break;
      }
      i++;
    }
    fireTableRowsUpdated(i, i);
  }

  public void removePath(Path path) {
    LwrtResource toremove = null;
    int i = 0;
    for (LwrtResource cp : list) {
      if (cp.getPath().equals(path)) {
        toremove = cp;
        break;
      }
      i++;
    }
    if (toremove != null) {
      // don't remove "default" or "readonly" resources
      EnumSet<PathContents> set = toremove.getContents();
      if (!set.contains(PathContents.DEFAULT) && !set.contains(PathContents.READONLY)) {
        if (toremove.isSelected()) {
          toremove.setSelected(false);
          fireTableCellUpdated(i, Column.SELECTED.ordinal());
        }
        list.remove(i);
        fireTableRowsDeleted(i, i);
      }
    }
  }

  // for each "required" custom path, extract it from jar and add it
  // in case the user deleted the custom vpks/folders included with lawena
  public void validateRequired() {
    for (LwrtResource cp : defaultPaths.values()) {
      if (cp.getContents().contains(PathContents.DEFAULT) && !list.contains(cp)) {
        Path filename = cp.getPath().getFileName();
        Path destdir = Paths.get("custom");
        unpackFileFromJar(Paths.get("lwrtvpks.jar"), filename.toString(), destdir);
        if (Files.exists(destdir.resolve(filename))) {
          addRow(cp);
        }
      }
    }
  }

  private void unpackFileFromJar(Path jarpath, String name, Path destpath) {
    JarFile jar;
    try {
      jar = new JarFile(jarpath.toFile());
      JarEntry entry = jar.getJarEntry(name);
      if (entry != null) {
        try (InputStream is = jar.getInputStream(entry);
            FileOutputStream fos = new FileOutputStream(destpath.resolve(name).toFile())) {
          while (is.available() > 0) {
            fos.write(is.read());
          }
        } catch (IOException e) {
          log.warn("Problem while unpacking file: " + e);
        }
      } else {
        log.warn("File " + name + " does not exist in " + jarpath);
      }
      jar.close();
    } catch (IOException e) {
      log.warn("Could not unpack file " + name + " from jar: " + jarpath, e);
    }
  }

  public void loadResourceSettings() {
    List<String> selected = cfg.getCustomResources();
    Path tfpath = cfg.getTfPath();
    int i = 0;
    for (LwrtResource cp : list) {
      Path path = cp.getPath();
      if (!cp.getContents().contains(PathContents.READONLY) && !selected.isEmpty()) {
        String key = (path.startsWith(tfpath) ? "tf*" : "");
        key += path.getFileName().toString();
        cp.setSelected(selected.contains(key));
        fireTableRowsUpdated(i, i);
      }
      i++;
    }
  }

}
