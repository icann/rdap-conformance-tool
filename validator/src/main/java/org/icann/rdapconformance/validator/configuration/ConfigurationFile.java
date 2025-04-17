package org.icann.rdapconformance.validator.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
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
  private final List<DefinitionError> definitionError;

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
  private final List<DefinitionWarning> definitionWarning;

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

  private final boolean gtldRegistrar;
  private final boolean gtldRegistry;
  private final boolean thinRegistry;
  private final boolean rdapProfileFebruary2019;
  private final boolean rdapProfileFebruary2024;

  @JsonCreator
  public ConfigurationFile(
      @JsonProperty(value = "definitionIdentifier", required = true) String definitionIdentifier,
      @JsonProperty(value = "definitionError") List<DefinitionError> definitionError,
      @JsonProperty(value = "definitionWarning") List<DefinitionWarning> definitionWarning,
      @JsonProperty(value = "definitionIgnore") List<Integer> definitionIgnore,
      @JsonProperty(value = "definitionNotes") List<String> definitionNotes,
      @JsonProperty(value = "gtldRegistrar") boolean gtldRegistrar,
      @JsonProperty(value = "gtldRegistry") boolean gtldRegistry,
      @JsonProperty(value = "thinRegistry") boolean thinRegistry,
      @JsonProperty(value = "rdapProfileFebruary2019") boolean rdapProfileFebruary2019,
      @JsonProperty(value = "rdapProfileFebruary2024") boolean rdapProfileFebruary2024) {
    this.definitionIdentifier = definitionIdentifier;
    this.definitionError = emptyListIfNull(definitionError);
    this.definitionWarning = emptyListIfNull(definitionWarning);
    this.definitionIgnore = emptyListIfNull(definitionIgnore);
    this.definitionNotes = emptyListIfNull(definitionNotes);
    this.gtldRegistrar = gtldRegistrar;
    this.gtldRegistry = gtldRegistry;
    this.thinRegistry = thinRegistry;
    this.rdapProfileFebruary2019 = rdapProfileFebruary2019;
    this.rdapProfileFebruary2024 = rdapProfileFebruary2024;
    if (null != definitionError) {
      this.errorCodes = definitionError.stream()
          .map(DefinitionAlert::getCode)
          .collect(Collectors.toSet());
    } else {
      this.errorCodes = Collections.emptySet();
    }
    if (null != definitionWarning) {
      this.warningCodes = definitionWarning.stream()
          .map(DefinitionAlert::getCode)
          .collect(Collectors.toSet());
    } else {
      this.warningCodes = Collections.emptySet();
    }
  }

  private <T> List<T> emptyListIfNull(List<T> list) {
    return list == null ? new ArrayList<>() : list;
  }

  public boolean isError(int code) {
    return this.errorCodes.contains(code);
  }

  public boolean isWarning(int code) {
    return this.warningCodes.contains(code);
  }

  public String getAlertNotes(int code) {
    if (this.isError(code)) {
      return this.definitionError.stream()
          .filter(a -> a.getCode() == code)
          .findFirst()
          .map(DefinitionError::getNotes)
          .orElse("");
    } else if (this.isWarning(code)) {
      return this.definitionWarning.stream()
          .filter(a -> a.getCode() == code)
          .findFirst()
          .map(DefinitionWarning::getNotes)
          .orElse("");
    }
    return "";
  }

  public String getDefinitionIdentifier() {
    return definitionIdentifier;
  }

  public List<Integer> getDefinitionIgnore() {
    return definitionIgnore;
  }

  public List<String> getDefinitionNotes() {
    return definitionNotes;
  }

  public boolean isGtldRegistrar() {
    return gtldRegistrar;
  }

  public boolean isGtldRegistry() {
    return gtldRegistry;
  }

  public boolean isThinRegistry() {
    return thinRegistry;
  }

  public boolean isRdapProfileFebruary2019() {
    return rdapProfileFebruary2019;
  }

  public boolean isRdapProfileFebruary2024() {
    return rdapProfileFebruary2024;
  }
}
