package com.github.lawena.profile;

public interface Validator<T> {
  
  public ValidationResult validate(T value);

}
