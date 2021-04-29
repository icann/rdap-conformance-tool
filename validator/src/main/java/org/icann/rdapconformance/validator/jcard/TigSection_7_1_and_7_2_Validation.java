package org.icann.rdapconformance.validator.jcard;

import java.util.Set;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

public class TigSection_7_1_and_7_2_Validation {

  private final String jsonExceptionPointer;
  private final RDAPValidatorResults results;
  private final Set<String> authorizedPhoneType = Set.of("voice", "fax");

  public TigSection_7_1_and_7_2_Validation(String jsonExceptionPointer,
      RDAPValidatorResults results) {
    this.jsonExceptionPointer = jsonExceptionPointer;
    this.results = results;
  }

  public void validate(String category, JSONArray categorieJsonArray) {
    if (category.equals("tel")) {
      Object phoneType = categorieJsonArray.get(1);
      if (!(phoneType instanceof JSONObject)) {
        logError(phoneType);
        return;
      }

      if (!authorizedPhoneType.contains(((JSONObject)phoneType).get("type"))) {
        logError(phoneType);
        return;
      }
    }
  }

  private void logError(Object value) {
    results.add(RDAPValidationResult.builder()
        .code(-20900)
        .value(jsonExceptionPointer + ":" + value.toString())
        .message(
            "An entity with a tel property without a voice or fax type was found. See section 7.1 and 7.2 of the TIG.")
        .build());
  }
}
