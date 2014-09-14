package com.github.lawena.profile;


public class RangeValidator implements Validator<Integer> {

  private int min;
  private int max;

  public RangeValidator(int min, int max) {
    this.min = Math.min(min, max);
    this.max = Math.max(min, max);
  }

  @Override
  public ValidationResult validate(Integer value) {
    if (value == null)
      return ValidationResult.invalid(new IllegalArgumentException("Value must not be null"));
    if (value < min || value > max) {
      return ValidationResult.invalid(new IllegalArgumentException("Value " + value
          + " is out of bounds. Must be between [" + min + ", " + max + "]"));
    } else {
      return ValidationResult.OK;
    }
  }

}
