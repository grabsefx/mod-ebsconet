package org.folio.ebsconet.error;

public enum ErrorCode {
  UNKNOWN_ERROR("Unknown error"),
  VALIDATION_ERROR("Validation error"),
  NOT_FOUND_ERROR("Not found"),
  INTERNAL_SERVER_ERROR("Internal server error");

  private final String description;

  ErrorCode(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
