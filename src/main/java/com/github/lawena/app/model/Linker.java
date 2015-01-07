package com.github.lawena.app.model;

import com.github.lawena.util.LawenaException;

/**
 * Represents a strategy to operate with user files.
 * 
 * @author Ivan
 *
 */
public interface Linker {

  public void setModel(MainModel model);

  /**
   * Create links between original resources and launch folders and afterwards between launch
   * folders and game folders.
   * 
   * @throws LawenaException
   */
  public void link() throws LawenaException;

  /**
   * Removes links created by {@link #link()} and restores folder structure to original state.
   * 
   * @return <code>true</code> if the operation was successful, <code>false</code> otherwise.
   */
  public boolean unlink();
}
