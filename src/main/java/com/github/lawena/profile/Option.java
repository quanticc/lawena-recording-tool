package com.github.lawena.profile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

/**
 * Represents an application option of a determined type.
 * 
 * @author Ivan
 *
 * @param <T> the type of this option
 */
public class Option<T> {

  private static final Logger log = LoggerFactory.getLogger(Option.class);

  private final TypeToken<T> type;
  private final String key;
  private final T defaultValue;
  private Validator<T> validator = new Validator<T>() {

    @Override
    public ValidationResult validate(T value) {
      return ValidationResult.ok("No validator present");
    }
  };

  Option(TypeToken<T> type, String key, T defaultValue) {
    if (key == null)
      throw new IllegalArgumentException("Key must not be null");
    if (defaultValue == null)
      throw new IllegalArgumentException("The default value for " + key + " must not be null");
    if (type == null)
      throw new IllegalArgumentException("The type for " + key + " must not be null");
    this.type = type;
    this.key = key;
    this.defaultValue = defaultValue;
  }

  public String getKey() {
    return key;
  }

  /**
   * Retrieve the option value from the underlying provider.
   * 
   * @param provider - the <code>ValueProvider</code> that will handle the option storage
   * @return the value for this option. It is never <code>null</code>
   */
  @SuppressWarnings("unchecked")
  public T getValue(ValueProvider provider) {
    T value = provider.get(type, key);
    if (value == null) {
      log.warn("Key {} should not be null", key);
      setValueEx(provider, defaultValue);
      value = provider.get(type, key);
    }
    // handle Double to Integer conversion
    if (type.getRawType().equals(Integer.class) && (value instanceof Double)) {
      return (T) Integer.valueOf(((Double) value).intValue());
    }
    return value;
  }

  public T getDefaultValue() {
    return defaultValue;
  }

  /**
   * Attempts to set a value on this option, returning a result object detailing the status of this
   * operation.
   * 
   * @param provider - the <code>ValueProvider</code> that will handle the option storage
   * @param value - the value attempted to set
   * @return a <code>ValidationResult</code> describing the result of this operation.
   */
  public ValidationResult setValue(ValueProvider provider, T value) {
    ValidationResult result = validator.validate(value);
    if (result.isValid()) {
      provider.set(key, value);
    }
    return result;
  }

  /**
   * Attempts to set a value on this option, throwing an exception if the value is not permitted.
   * 
   * @param provider - the <code>ValueProvider</code> that will handle the option storage
   * @param value - the value attempted to set
   */
  public void setValueEx(ValueProvider provider, T value) {
    ValidationResult result = validator.validate(value);
    if (result.isValid()) {
      provider.set(key, value);
    } else {
      throw result.getCause();
    }
  }

  /**
   * Sets the default value for this option on the given provider.
   * 
   * @param provider - the <code>ValueProvider</code> that will handle the option storage
   */
  public void setDefaultValue(ValueProvider provider) {
    provider.set(key, defaultValue);
  }

  public Option<T> validatedBy(Validator<T> validator) {
    this.validator = validator;
    return this;
  }

  public Validator<T> getValidator() {
    return validator;
  }

}
