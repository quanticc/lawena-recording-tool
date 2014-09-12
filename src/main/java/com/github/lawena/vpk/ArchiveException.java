package com.github.lawena.vpk;

/**
 * @author Connor Haigh
 * @see <a href="https://github.com/Contron/JavaVPK">GitHub Repository</a>
 */
public class ArchiveException extends Exception {
  /**
   * Creates a new VPK archive exception.
   * 
   * @param message the message
   */
  public ArchiveException(String message) {
    super(message);
  }

  public static final long serialVersionUID = 1;
}
