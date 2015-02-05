package com.github.lawena.app.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
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

import com.github.lawena.Messages;
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
    alias.put("no_announcer_voices.vpk", Messages.getString("Resources.disableAnnouncerVoices")); //$NON-NLS-1$ //$NON-NLS-2$
    alias.put("no_applause_sounds.vpk", Messages.getString("Resources.disableApplauseSounds")); //$NON-NLS-1$ //$NON-NLS-2$
    alias.put("no_domination_sounds.vpk", Messages.getString("Resources.disableDomRevengeSounds")); //$NON-NLS-1$ //$NON-NLS-2$
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
        return "Tags"; //$NON-NLS-1$
      case ENABLED:
        return ""; //$NON-NLS-1$
      case NAME:
        return "Resource"; //$NON-NLS-1$
      default:
        return ""; //$NON-NLS-1$
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
        return ""; //$NON-NLS-1$
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

  private static String cleanCollection(Collection<?> l) {
    return l.toString().replaceAll("\\[|\\]", "").replaceAll(", ", "\\+"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  }

  public ResourceFolder addFolder(File file, boolean forceLoad) {
    ResourceFolder f = new ResourceFolder(file, forceLoad);
    folders.put(file.toPath().toAbsolutePath().toString(), f);
    log.debug("Adding resources folder: {}", file.getAbsolutePath()); //$NON-NLS-1$
    refreshFolder(f.getFile());
    return f;
  }

  public void refreshFolder(File file) {
    List<Resource> newResources = scanResourceFolder(file.toPath());
    for (Resource r : newResources) {
      int index = list.indexOf(r);
      boolean enabled;
      if (index > 0) {
        log.debug("Updating '{}' from {}", r, r.getAbsolutePath()); //$NON-NLS-1$
        Resource old = list.get(index);
        list.remove(index);
        list.add(index, r);
        enabled = old.isEnabled();
        fireTableRowsUpdated(index, index);
      } else {
        log.debug("Adding '{}' from {}", r, r.getAbsolutePath()); //$NON-NLS-1$
        list.add(r);
        enabled = false;
        int row = list.size() - 1;
        fireTableRowsInserted(row, row);
      }
      if (!enabled || isEnableAllowed(r)) {
        r.setEnabled(enabled);
      } else {
        log.warn("Maximum HUD count selected"); //$NON-NLS-1$
      }
    }
  }

  private List<Resource> scanResourceFolder(Path dir) {
    List<Resource> newResourceList = new ArrayList<>();
    // path must be a folder or a vpk file
    if (!Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS)) {
      log.info("Skipping invalid or non existing path: {}", dir); //$NON-NLS-1$
    } else {
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
        for (Path path : stream) {
          // each resource must be a folder or a vpk file
          newResourceList.add(newResource(path));
        }
      } catch (IOException e) {
        log.warn("Problem while loading custom paths: {}", e.toString()); //$NON-NLS-1$
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

  public static void updateTags(Resource resource) throws IOException {
    Set<String> tags = new HashSet<>();
    boolean hasResourceUiFolder = false;
    boolean hasScriptsFolder = false;
    for (String content : contents(resource)) {
      if (content.startsWith("resource/ui")) { //$NON-NLS-1$
        hasResourceUiFolder = true;
      } else if (content.startsWith("scripts/")) { //$NON-NLS-1$
        hasScriptsFolder = true;
      } else if (content.startsWith("cfg/") && content.endsWith(".cfg")) { //$NON-NLS-1$ //$NON-NLS-2$
        tags.add(Resource.CONFIG);
      } else if (content.startsWith("materials/skybox/")) { //$NON-NLS-1$
        tags.add(Resource.SKYBOX);
      }
    }
    if (hasResourceUiFolder && hasScriptsFolder) {
      tags.add(Resource.HUD);
    }
    log.trace("Tagging '{}' with: {}", resource, tags); //$NON-NLS-1$
    resource.setTags(tags);
  }

  private static List<String> contents(Resource resource) throws IOException {
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
    } else if (start.toString().endsWith(".vpk")) { //$NON-NLS-1$
      Archive vpk = new Archive(start.toFile());
      try {
        vpk.load();
        for (Directory dir : vpk.getDirectories()) {
          for (Entry entry : dir.getEntries()) {
            contents.add(dir.getPath() + "/" + entry.getFullName()); //$NON-NLS-1$
          }
        }
      } catch (ArchiveException | EntryException e) {
        throw new IOException("Invalid VPK archive", e); //$NON-NLS-1$
      }
    }
    log.trace("Contents found for {}: {}", start, contents); //$NON-NLS-1$
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
        log.warn("Parent of resource not found: {}", resource); //$NON-NLS-1$
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
      log.warn("Parent of resource not found: {}", resource); //$NON-NLS-1$
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
        log.warn("Invalid path: {}", pathname); //$NON-NLS-1$
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
