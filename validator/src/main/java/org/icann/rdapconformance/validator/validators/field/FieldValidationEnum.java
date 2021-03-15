package org.icann.rdapconformance.validator.validators.field;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.json.JSONObject;

public class FieldValidationEnum extends FieldValidation {

  static Pattern enumPattern = Pattern.compile("not part of the (.+) enum is not a valid enum "
      + "value");

  public FieldValidationEnum(String name, int erroCode) {
    super(name, erroCode);
  }

  @Override
  public void validate(String errorMsg, List<RDAPValidationResult> results, JSONObject jsonObject) {
    Matcher enumMatcher = enumPattern.matcher(errorMsg);
    if (enumMatcher.find()) {
      results.add(RDAPValidationResult.builder()
          .code(erroCode)
          .value(jsonObject.get(name).toString())
          .message(
              "The JSON string is not included as a Value with Type=\"" + enumMatcher.group(1)
                  + "\" in the "
                  + "RDAPJSONValues dataset.")
          .build());
    }
  }
}
