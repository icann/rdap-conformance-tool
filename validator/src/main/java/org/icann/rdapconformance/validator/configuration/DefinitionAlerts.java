package org.icann.rdapconformance.validator.configuration;

public class DefinitionAlerts {

  /**
   * A required JSON number that identifies a single test.
   */
  private final int code;
  /**
   * A required JSON string that is copied verbatim if the test fails, generating an entry in the
   * results section in the results file.
   */
  private final String notes;

  public DefinitionAlerts(int code, String notes) {
    this.code = code;
    this.notes = notes;
  }

  public static Builder builder() {
    return new Builder();
  }

  public int getCode() {
    return code;
  }

  public String getNotes() {
    return notes;
  }

  public static class Builder {

    private int code;
    private String notes;

    public Builder code(int code) {
      this.code = code;
      return this;
    }

    public Builder notes(String notes) {
      this.notes = notes;
      return this;
    }

    public DefinitionAlerts build() {
      return new DefinitionAlerts(this.code, this.notes);
    }
  }
}
