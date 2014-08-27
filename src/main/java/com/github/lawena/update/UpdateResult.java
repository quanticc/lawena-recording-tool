package com.github.lawena.update;

public class UpdateResult {

  public enum Status {
    UPDATE_AVAILABLE, NO_UPDATES_FOUND, ALREADY_LATEST_VERSION;
  }

  public static UpdateResult found(BuildInfo details) {
    return new UpdateResult(Status.UPDATE_AVAILABLE, details);
  }

  public static UpdateResult notFound(String message) {
    return new UpdateResult(Status.NO_UPDATES_FOUND, message);
  }

  public static UpdateResult latest(String message) {
    return new UpdateResult(Status.ALREADY_LATEST_VERSION, message);
  }

  private Status status;
  private BuildInfo details;
  private String message;

  private UpdateResult(Status status) {
    this(status, null, status.toString());
  }

  private UpdateResult(Status status, String message) {
    this(status, null, message);
  }

  private UpdateResult(Status status, BuildInfo details) {
    this(status, details, status.toString());
  }

  private UpdateResult(Status status, BuildInfo details, String message) {
    this.status = status;
    this.details = details;
    this.message = message;
  }

  public Status getStatus() {
    return status;
  }

  public BuildInfo getDetails() {
    return details;
  }

  public String getMessage() {
    return message;
  }
}
