package org.icann.rdapconformance.validator.workflow.profile;

import java.util.List;

public class RDAPProfileFebruary2019 {

  private final List<ProfileValidation> validations;

  public RDAPProfileFebruary2019(List<ProfileValidation> validations) {
    this.validations = validations;
  }

  public boolean validate() {
    boolean result = true;
    for (ProfileValidation validation : validations) {
      result &= validation.validate();
    }

    return result;
  }
}
