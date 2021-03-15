package org.icann.rdapconformance.validator.validators.field;

import java.util.List;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.json.JSONObject;

public abstract class FieldValidation {

  protected String name;
  protected int erroCode;
  protected String icannErrorMsg;

  public FieldValidation(String name,
      int erroCode) {
    this.name = name;
    this.erroCode = erroCode;
  }

  public String getName() {
    return name;
  }

  public abstract void validate(String errorMsg, List<RDAPValidationResult> results,
      JSONObject jsonObject);
}
