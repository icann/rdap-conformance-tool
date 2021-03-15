package org.icann.rdapconformance.validator.validators.field;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.json.JSONObject;

public class FieldValidationBasicType extends FieldValidation {

  static Pattern basicTypePattern = Pattern.compile("expected type: (.+), found: (.+)");

  public FieldValidationBasicType(String name, int erroCode) {
    super(name, erroCode);
  }

  @Override
  public void validate(String errorMsg, List<RDAPValidationResult> results, JSONObject jsonObject) {
    Matcher basicTypeMatcher = basicTypePattern.matcher(errorMsg);
    if (basicTypeMatcher.find()) {
      String icannErrorMsg =
          "The JSON value is not a " + basicTypeMatcher.group(1).toLowerCase() + ".";
      String value = name + "/" + jsonObject.get(name).toString();
      if (basicTypeMatcher.group(1).equals("JSONArray")) {
        icannErrorMsg =
            "The " + name + " structure is not syntactically valid.";
        value = jsonObject.get(name).toString();
      }

      results.add(RDAPValidationResult.builder()
          .code(erroCode)
          .value(value)
          .message(icannErrorMsg)
          .build());
    }
  }
}
