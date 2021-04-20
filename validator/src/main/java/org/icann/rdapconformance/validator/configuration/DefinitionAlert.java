package org.icann.rdapconformance.validator.configuration;

public abstract class DefinitionAlert {

  /**
   * A required JSON number that identifies a single test.
   */
  private final int code;
  /**
   * A required JSON string that is copied verbatim if the test fails, generating an entry in the
   * results section in the results file.
   */
  private final String notes;

  public DefinitionAlert(int code, String notes) {
    this.code = code;
    this.notes = notes;
  }

  public int getCode() {
    return code;
  }

  public String getNotes() {
    return notes;
  }
}
