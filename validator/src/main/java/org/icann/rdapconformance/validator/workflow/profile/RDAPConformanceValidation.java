package org.icann.rdapconformance.validator.workflow.profile;

import java.util.HashSet;
import java.util.Set;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class RDAPConformanceValidation extends ProfileJsonValidation {

  final int code;
  final String message;
  private final String requiredValue;

  public RDAPConformanceValidation(String rdapResponse, RDAPValidatorResults results,
      String requiredValue, Integer code, String message) {
    super(rdapResponse, results);
    this.requiredValue = requiredValue;
    this.code = code;
    this.message = message;
  }

  @Override
  protected boolean doValidate() {
    String jsonPointer = "#/rdapConformance";
    JSONArray rdapConformance = (JSONArray) new JSONObject(jsonObject.toString())
        .query(jsonPointer);
    Set<String> values = new HashSet<>();
    rdapConformance.forEach(v -> values.add(v.toString()));
    if (!values.contains(requiredValue)) {
      results.add(RDAPValidationResult.builder()
          .code(code)
          .value(getResultValue(jsonPointer))
          .message(message)
          .build());
      return false;
    }
    return true;
  }

}
