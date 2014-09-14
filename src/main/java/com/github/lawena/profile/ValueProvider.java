package com.github.lawena.profile;

import com.google.gson.reflect.TypeToken;

public interface ValueProvider {
  
  public <T> T get(TypeToken<T> type, String key);
  
  public <T> void set(String key, T value);

}
