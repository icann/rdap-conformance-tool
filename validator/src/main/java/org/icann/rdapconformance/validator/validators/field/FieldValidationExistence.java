package org.icann.rdapconformance.validator.validators.field;

import java.util.List;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.json.JSONObject;

public class FieldValidationExistence extends FieldValidation {

  public FieldValidationExistence(String name, int erroCode) {
    super(name, erroCode);
  }

  @Override
  public void validate(String errorMsg, List<RDAPValidationResult> results, JSONObject jsonObject) {
    if (jsonObject.get(name).equals(null)) {
      results.add(RDAPValidationResult.builder()
          .code(erroCode)
          .value(jsonObject.toString())
          .message("The " + name + " element does not exist.")
          .build());
    }
  }
}
