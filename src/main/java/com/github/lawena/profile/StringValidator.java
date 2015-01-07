package com.github.lawena.profile;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("nls")
public class StringValidator implements Validator<String> {

  private List<String> allowedValues;

  public StringValidator(String... values) {
    if (values == null)
      throw new IllegalArgumentException("Must include a set of values to allow");
    this.allowedValues = Arrays.asList(values);
    if (allowedValues.isEmpty())
      throw new IllegalArgumentException("Must include at least one allowed value");
  }

  @Override
  public ValidationResult validate(String value) {
    if (value == null)
      return ValidationResult.invalid(new IllegalArgumentException("Value must not be null"));
    if (!allowedValues.contains(value)) {
      return ValidationResult.invalid(new IllegalArgumentException("Value " + value
          + " is not permitted. Only " + allowedValues + " are allowed"));
    } else {
      return ValidationResult.OK;
    }
  }

  public List<String> getAllowedValues() {
    return allowedValues;
  }

}
