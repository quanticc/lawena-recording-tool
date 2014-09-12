package com.github.lawena.vpk;

/**
 * @author Connor Haigh
 * @see <a href="https://github.com/Contron/JavaVPK">GitHub Repository</a>
 */
public class EntryException extends Exception {
  /**
   * Creates a new VPK archive entry exception.
   * 
   * @param message the message
   */
  public EntryException(String message) {
    super(message);
  }

  public static final long serialVersionUID = 1;
}
