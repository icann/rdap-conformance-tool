package org.icann.rdapconformance.validator.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ConfigurationFile {

  /**
   * A required JSON string that identifies the configuration definition file.
   *
   * <p>The string is copied verbatim to the definitionIdentifier element of the results file.</p>
   */
  @JsonProperty(required = true)
  String definitionIdentifier;

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
  @JsonProperty
  List<DefinitionAlerts> definitionError;

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
  @JsonProperty
  List<DefinitionAlerts> definitionWarning;

  /**
   * An optional JSON array of single test identifiers that are ignored (i.e. not tested for). The
   * contents of this element are copied verbatim to the ignore section in the results file.
   */
  @JsonProperty
  List<Integer> definitionIgnore;

  /**
   * An optional JSON array of strings that are copied verbatim to the notes section in the results
   * file.
   */
  @JsonProperty
  List<String> definitionNotes;

  public List<Integer> getDefinitionIgnore() {
    return definitionIgnore;
  }
}
