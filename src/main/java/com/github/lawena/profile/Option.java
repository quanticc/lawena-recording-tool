package com.github.lawena.profile;

import com.google.gson.reflect.TypeToken;


public class Option<T> {

  private TypeToken<T> type;
  private String key;
  private Validator<T> validator = new Validator<T>() {

    @Override
    public ValidationResult validate(T value) {
      return ValidationResult.ok("No validator present");
    }
  };
  private T defaultValue;
  
  Option(TypeToken<T> type) {
    this.type = type;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public T getValue(ValueProvider provider) {
    return provider.get(type, key);
  }

  public T getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(T defaultValue) {
    this.defaultValue = defaultValue;
  }

  public ValidationResult setValue(ValueProvider provider, T value) {
    ValidationResult result = validator.validate(value);
    if (result.isValid()) {
      provider.set(key, value);
    }
    return result;
  }

  public void setValueEx(ValueProvider provider, T value) {
    ValidationResult result = validator.validate(value);
    if (result.isValid()) {
      provider.set(key, value);
    } else {
      throw result.getCause();
    }
  }

  public Option<T> validatedBy(Validator<T> validator) {
    this.validator = validator;
    return this;
  }

  public Validator<T> getValidator() {
    return validator;
  }

}
