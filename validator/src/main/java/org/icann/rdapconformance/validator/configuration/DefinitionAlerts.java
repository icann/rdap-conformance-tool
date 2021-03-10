package org.icann.rdapconformance.validator.configuration;

public class DefinitionAlerts {

  /**
   * A required JSON number that identifies a single test.
   */
  private String code;

  /**
   * A required JSON string that is copied verbatim if the test fails, generating an entry in the
   * results section in the results file.
   */
  private String notes;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
