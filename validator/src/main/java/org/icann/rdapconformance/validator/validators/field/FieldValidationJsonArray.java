package org.icann.rdapconformance.validator.validators.field;

import java.util.List;
import java.util.regex.Matcher;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.json.JSONObject;

public class FieldValidationJsonArray extends FieldValidationBasicType {

  public FieldValidationJsonArray(String name, int erroCode) {
    super(name, erroCode);
  }

  @Override
  public void validate(String errorMsg, List<RDAPValidationResult> results, JSONObject jsonObject) {
    Matcher basicTypeMatcher = basicTypePattern.matcher(errorMsg);
    if (basicTypeMatcher.find() && basicTypeMatcher.group(1).equals("JSONArray")) {
      results.add(RDAPValidationResult.builder()
          .code(erroCode)
          .value(jsonObject.get(name).toString())
          .message("The " + name + " structure is not syntactically valid.")
          .build());
    }
  }
}
