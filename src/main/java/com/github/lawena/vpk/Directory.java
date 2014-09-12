package com.github.lawena.vpk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a directory inside a VPK archive.
 * 
 * @author Connor Haigh
 * @see <a href="https://github.com/Contron/JavaVPK">GitHub Repository</a>
 */
public class Directory {
  /**
   * Creates a new VPK directory.
   * 
   * @param path the path of the directory
   */
  public Directory(String path) {
    this.path = path.trim();
    this.entries = new ArrayList<Entry>();
  }

  /**
   * Returns the path of this directory.
   * 
   * @return the path
   */
  public String getPath() {
    return this.path;
  }

  /**
   * Returns the full path for an entry in this directory.
   * 
   * @param entry the entry
   * @return the full path
   */
  public String getPathFor(Entry entry) {
    return (this.path + File.separator + entry.getFullName());
  }

  /**
   * Adds an entry to this directory.
   * 
   * @param entry the entry
   */
  public void addEntry(Entry entry) {
    this.entries.add(entry);
  }

  /**
   * Removes an entry from this directory.
   * 
   * @param entry the entry
   */
  public void removeEntry(Entry entry) {
    this.entries.remove(entry);
  }

  /**
   * Returns the list of entries in this directory.
   * 
   * @return the list of entries
   */
  public List<Entry> getEntries() {
    return this.entries;
  }

  private String path;
  private ArrayList<Entry> entries;
}
