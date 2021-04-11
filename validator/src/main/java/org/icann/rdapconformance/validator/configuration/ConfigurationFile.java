package org.icann.rdapconformance.validator.configuration;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

  private final Set<Integer> errorCodes;
  private final Set<Integer> warningCodes;

  public ConfigurationFile(String definitionIdentifier,
      List<DefinitionAlerts> definitionError,
      List<DefinitionAlerts> definitionWarning, List<Integer> definitionIgnore,
      List<String> definitionNotes) {
    this.definitionIdentifier = definitionIdentifier;
    this.definitionError = definitionError;
    this.definitionWarning = definitionWarning;
    this.definitionIgnore = definitionIgnore;
    this.definitionNotes = definitionNotes;
    this.errorCodes = definitionError.stream()
        .map(DefinitionAlerts::getCode)
        .collect(Collectors.toSet());
    this.warningCodes = definitionWarning.stream()
        .map(DefinitionAlerts::getCode)
        .collect(Collectors.toSet());
  }

  public String getDefinitionIdentifier() {
    return definitionIdentifier;
  }

  public boolean isError(int code) {
    return this.errorCodes.contains(code);
  }

  public boolean isWarning(int code) {
    return this.warningCodes.contains(code);
  }

  public String getAlertNotes(int code) {
    if (isError(code)) {
      return this.definitionError.stream()
          .filter(a -> a.getCode() == code)
          .findFirst()
          .map(DefinitionAlerts::getNotes)
          .orElse("");
    } else if (isWarning(code)) {
      return this.definitionWarning.stream()
          .filter(a -> a.getCode() == code)
          .findFirst()
          .map(DefinitionAlerts::getNotes)
          .orElse("");
    }
    return "";
  }

  public List<Integer> getDefinitionIgnore() {
    return definitionIgnore;
  }

  public List<String> getDefinitionNotes() {
    return definitionNotes;
  }

  public static class Builder {

    private String definitionIdentifier;
    private List<DefinitionAlerts> definitionError = Collections.emptyList();
    private List<DefinitionAlerts> definitionWarning = Collections.emptyList();
    private List<Integer> definitionIgnore = Collections.emptyList();
    private List<String> definitionNotes = Collections.emptyList();

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
