package com.github.lawena.profile;

public class ValidationResult {

  public enum ResultType {
    VALID, INVALID;
  }

  public static ValidationResult OK = new ValidationResult(ResultType.VALID, "OK");

  public static ValidationResult ok(String message) {
    return new ValidationResult(ResultType.VALID, message);
  }

  public static ValidationResult invalid(RuntimeException t) {
    return new ValidationResult(ResultType.INVALID, t.getMessage(), t);
  }

  private ResultType status;
  private String message;
  private RuntimeException cause;

  private ValidationResult(ResultType status) {
    this(status, "", null);
  }

  private ValidationResult(ResultType status, String message) {
    this(status, message, null);
  }

  private ValidationResult(ResultType status, String message, RuntimeException cause) {
    this.status = status;
    this.message = message;
    this.cause = cause;
  }

  public ResultType getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }

  public RuntimeException getCause() {
    return cause;
  }

  public boolean isValid() {
    return equals(OK);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ValidationResult other = (ValidationResult) obj;
    if (status != other.status)
      return false;
    return true;
  }

}
