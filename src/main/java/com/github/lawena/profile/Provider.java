package com.github.lawena.profile;

import com.google.gson.reflect.TypeToken;

/**
 * Represents a class that offers reading and writing to values given a key.
 * 
 * @author Ivan
 *
 */
public interface Provider {

  public <T> T get(TypeToken<T> type, String key);

  public <T> void set(String key, T value);
  
  public <T> T getDefault(TypeToken<T> type, String key);

}
