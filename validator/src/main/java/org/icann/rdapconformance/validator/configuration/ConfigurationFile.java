package org.icann.rdapconformance.validator.configuration;

import java.util.List;

public class ConfigurationFile {

  /**
   * A required JSON string that identifies the configuration definition file.
   *
   * <p>The string is copied verbatim to the definitionIdentifier element of the results file.</p>
   */
  private final String definitionIdentifier;
  /**
   * An optional JSON array of objects.
   *
   * <p>
   * Each object contains the following elements:
   * <ul>
   *   <li>code: a required JSON number that identifies a single test.</li>
   *   <li>notes: a required JSON string that is copied verbatim if the test fails, generating an
   *              entry in the results section in the results file.</li>
   * </ul>
   * </p>
   */
  private final List<DefinitionAlerts> definitionError;
  /**
   * An optional JSON array of objects.
   *
   * <p>
   * Each object contains the following elements:
   * <ul>
   *   <li>code: a required JSON number that identifies a single test.</li>
   *   <li>notes: a required JSON string that is copied verbatim if the test fails, generating an
   *              entry in the results section in the results file.</li>
   * </ul>
   * </p>
   */
  private final List<DefinitionAlerts> definitionWarning;
  /**
   * An optional JSON array of single test identifiers that are ignored (i.e. not tested for). The
   * contents of this element are copied verbatim to the ignore section in the results file.
   */
  private final List<Integer> definitionIgnore;
  /**
   * An optional JSON array of strings that are copied verbatim to the notes section in the results
   * file.
   */
  private final List<String> definitionNotes;

  public ConfigurationFile(String definitionIdentifier,
      List<DefinitionAlerts> definitionError,
      List<DefinitionAlerts> definitionWarning, List<Integer> definitionIgnore,
      List<String> definitionNotes) {
    this.definitionIdentifier = definitionIdentifier;
    this.definitionError = definitionError;
    this.definitionWarning = definitionWarning;
    this.definitionIgnore = definitionIgnore;
    this.definitionNotes = definitionNotes;
  }

  public String getDefinitionIdentifier() {
    return definitionIdentifier;
  }

  public List<DefinitionAlerts> getDefinitionError() {
    return definitionError;
  }

  public List<DefinitionAlerts> getDefinitionWarning() {
    return definitionWarning;
  }

  public List<Integer> getDefinitionIgnore() {
    return definitionIgnore;
  }

  public List<String> getDefinitionNotes() {
    return definitionNotes;
  }

  public static class Builder {

    private String definitionIdentifier;
    private List<DefinitionAlerts> definitionError;
    private List<DefinitionAlerts> definitionWarning;
    private List<Integer> definitionIgnore;
    private List<String> definitionNotes;

    public Builder definitionIdentifier(String definitionIdentifier) {
      this.definitionIdentifier = definitionIdentifier;
      return this;
    }

    public Builder definitionError(List<DefinitionAlerts> definitionError) {
      this.definitionError = definitionError;
      return this;
    }

    public Builder definitionWarning(List<DefinitionAlerts> definitionWarning) {
      this.definitionWarning = definitionWarning;
      return this;
    }

    public Builder definitionIgnore(List<Integer> definitionIgnore) {
      this.definitionIgnore = definitionIgnore;
      return this;
    }

    public Builder definitionNotes(List<String> definitionNotes) {
      this.definitionNotes = definitionNotes;
      return this;
    }

    public ConfigurationFile build() {
      return new ConfigurationFile(this.definitionIdentifier, this.definitionError,
          this.definitionWarning, this.definitionIgnore, this.definitionNotes);
    }
  }
}
