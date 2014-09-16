package com.github.lawena.profile;

/**
 * Describes a simple validation mechanism for a determined type, using a result object to report
 * validation status.
 * 
 * @author Ivan
 *
 * @param <T> the type of this validator
 */
public interface Validator<T> {

  /**
   * Verifies that the supplied value is permitted by this validator.
   * 
   * @param value - the value attempted to set
   * @return a <code>ValidationResult</code> indicating the status of this operation
   */
  public ValidationResult validate(T value);

}
