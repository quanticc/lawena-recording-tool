package com.github.lawena.app.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.vpk.Archive;
import com.github.lawena.vpk.ArchiveException;
import com.github.lawena.vpk.Directory;
import com.github.lawena.vpk.Entry;
import com.github.lawena.vpk.EntryException;

public class Resources extends AbstractTableModel {

  public enum Column {
    ENABLED, NAME, TAGS;
  }

  private static final long serialVersionUID = 1L;
  private static final Logger log = LoggerFactory.getLogger(Resources.class);

  private final Map<String, String> alias = new HashMap<>();
  private final Map<String, ResourceFolder> folders = new HashMap<>();
  private final List<Resource> list = new ArrayList<>();

  public Resources() {
    alias.put("no_announcer_voices.vpk", "Disable announcer voices");
    alias.put("no_applause_sounds.vpk", "Disable applause sounds");
    alias.put("no_domination_sounds.vpk", "Disable domination/revenge sounds");
  }

  @Override
  public boolean isCellEditable(int row, int column) {
    return column == Column.ENABLED.ordinal();
  }

  @Override
  public Class<?> getColumnClass(int column) {
    switch (Column.values()[column]) {
      case ENABLED:
        return Boolean.class;
      case NAME:
        return Resource.class;
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
      case TAGS:
        return "Tags";
      case ENABLED:
        return "";
      case NAME:
        return "Resource";
      default:
        return "";
    }
  }

  @Override
  public Object getValueAt(int row, int column) {
    Resource r = list.get(row);
    switch (Column.values()[column]) {
      case TAGS:
        return cleanCollection(r.getTags());
      case ENABLED:
        return r.isEnabled();
      case NAME:
        return r;
      default:
        return "";
    }
  }

  @Override
  public void setValueAt(Object value, int row, int column) {
    Resource r = list.get(row);
    switch (Column.values()[column]) {
      case ENABLED:
        boolean v = (boolean) value;
        if (!v || isEnableAllowed(r)) {
          r.setEnabled(v);
          fireTableCellUpdated(row, column);
        }
        return;
      default:
    }
  }

  private boolean isEnableAllowed(Resource resource) {
    Set<String> tags = resource.getTags();
    boolean isHud = tags.contains(Resource.HUD);
    int enabledHuds = enabledHudCount();
    return !isHud || (enabledHuds == 1 && resource.isEnabled()) || enabledHuds == 0;
  }

  private int enabledHudCount() {
    int count = 0;
    for (Resource resource : list) {
      if (resource.isEnabled() && resource.getTags().contains(Resource.HUD))
        count++;
    }
    return count;
  }

  private String cleanCollection(Collection<?> l) {
    return l.toString().replaceAll("\\[|\\]", "").replaceAll(", ", "\\+");
  }

  public ResourceFolder addFolder(File file, boolean forceLoad) {
    ResourceFolder f = new ResourceFolder(file, forceLoad);
    folders.remove(f);
    folders.put(file.toPath().toAbsolutePath().toString(), f);
    log.debug("Adding resources folder: {}", file.getAbsolutePath());
    refreshFolder(f.getFile());
    return f;
  }

  public void refreshFolder(File file) {
    List<Resource> newResources = scanResourceFolder(file.toPath());
    for (Resource r : newResources) {
      int index = list.indexOf(r);
      boolean enabled;
      if (index > 0) {
        log.debug("Updating '{}' from {}", r, r.getAbsolutePath());
        Resource old = list.get(index);
        list.remove(index);
        list.add(index, r);
        enabled = old.isEnabled();
        fireTableRowsUpdated(index, index);
      } else {
        log.debug("Adding '{}' from {}", r, r.getAbsolutePath());
        list.add(r);
        enabled = false;
        int row = list.size() - 1;
        fireTableRowsInserted(row, row);
      }
      if (!enabled || isEnableAllowed(r)) {
        r.setEnabled(enabled);
      } else {
        log.warn("Maximum HUD count selected");
      }
    }
  }

  private List<Resource> scanResourceFolder(Path dir) {
    List<Resource> newResourceList = new ArrayList<>();
    // path must be a folder or a vpk file
    if (!Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS)) {
      log.info("Skipping path {} since it does not exist or is not a folder", dir);
    } else {
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
        for (Path path : stream) {
          // each resource must be a folder or a vpk file
          newResourceList.add(newResource(path));
        }
      } catch (NoSuchFileException e) {
        log.debug("{} does not exist, not scanning this path", dir);
      } catch (IOException e) {
        log.warn("Problem while loading custom paths: " + e);
      }
    }
    return newResourceList;
  }

  private Resource newResource(final Path start) throws IOException {
    Resource resource = new Resource();
    File file = start.toFile();
    resource.setName(getAlias(file.getName()));
    resource.setFile(file);
    updateTags(resource);
    return resource;
  }

  public void updateTags(Resource resource) throws IOException {
    Set<String> tags = new HashSet<>();
    boolean hasResourceUiFolder = false;
    boolean hasScriptsFolder = false;
    for (String content : contents(resource)) {
      if (content.startsWith("resource/ui")) {
        hasResourceUiFolder = true;
      } else if (content.startsWith("scripts/")) {
        hasScriptsFolder = true;
      } else if (content.startsWith("cfg/") && content.endsWith(".cfg")) {
        tags.add(Resource.CONFIG);
      } else if (content.startsWith("materials/skybox/")) {
        tags.add(Resource.SKYBOX);
      }
    }
    if (hasResourceUiFolder && hasScriptsFolder) {
      tags.add(Resource.HUD);
    }
    log.trace("Tagging '{}' with: {}", resource, tags);
    resource.setTags(tags);
  }

  private List<String> contents(Resource resource) throws IOException {
    final Path start = resource.getAbsolutePath();
    final List<String> contents = new ArrayList<>();
    if (Files.isDirectory(start)) {
      Files.walkFileTree(start, EnumSet.noneOf(FileVisitOption.class), 3,
          new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
              contents.add(start.toAbsolutePath().relativize(file.toAbsolutePath()).toString()
                  .replace('\\', '/'));
              return FileVisitResult.CONTINUE;
            }
          });
    } else if (start.toString().endsWith(".vpk")) {
      Archive vpk = new Archive(start.toFile());
      try {
        vpk.load();
        for (Directory dir : vpk.getDirectories()) {
          for (Entry entry : dir.getEntries()) {
            contents.add(dir.getPath() + "/" + entry.getFullName());
          }
        }
      } catch (ArchiveException | EntryException e) {
        throw new IOException("Invalid VPK archive", e);
      }
    }
    log.trace("Contents found for {}: {}", start, contents);
    return contents;
  }

  private String getAlias(String name) {
    return (alias.containsKey(name) ? alias.get(name) : name);
  }

  public List<Resource> getEnabledResources() {
    List<Resource> enabled = new ArrayList<>();
    for (Resource resource : list) {
      ResourceFolder folder = folders.get(resource.getParentPath().toString());
      if (folder == null) {
        log.warn("Parent folder of '{}' was not found on record", folder);
      } else {
        if (folder.isEnabled() && folder.isForceLoad()) {
          resource.setEnabled(true);
        } else if (!folder.isEnabled()) {
          resource.setEnabled(false);
        }
      }
      if (resource.isEnabled()) {
        enabled.add(resource);
      }
    }
    return enabled;
  }

  public boolean isParentForcefullyLoaded(Resource resource) {
    ResourceFolder folder = folders.get(resource.getParentPath().toString());
    if (folder == null) {
      log.warn("Parent folder of '{}' was not found on record", folder);
    } else {
      return folder.isForceLoad();
    }
    return false;
  }

  /**
   * Enable resources from a list of absolute or relative paths.
   * 
   * @param toEnable
   */
  public void enableFromList(List<String> toEnable) {
    for (String pathname : toEnable) {
      try {
        Path path = Paths.get(pathname).toAbsolutePath();
        for (int i = 0; i < list.size(); i++) {
          Resource resource = list.get(i);
          if (resource.getAbsolutePath().equals(path)) {
            resource.setEnabled(true);
            fireTableCellUpdated(i, Column.ENABLED.ordinal());
          }
        }
      } catch (InvalidPathException e) {
        log.warn("{} is not a valid path", pathname);
      }
    }
  }

  public List<Resource> getResourceList() {
    return list;
  }

  public List<String> getEnabledStringList() {
    List<String> stringList = new ArrayList<>();
    for (Resource resource : list) {
      if (resource.isEnabled()) {
        stringList.add(resource.getAbsolutePath().toString());
      }
    }
    return stringList;
  }
}
