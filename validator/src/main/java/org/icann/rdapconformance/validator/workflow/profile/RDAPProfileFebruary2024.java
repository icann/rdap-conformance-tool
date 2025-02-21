package org.icann.rdapconformance.validator.workflow.profile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RDAPProfileFebruary2024 {

  private static final Logger logger = LoggerFactory.getLogger(RDAPProfileFebruary2024.class);
  private final List<ProfileValidation> validations;

  public RDAPProfileFebruary2024(List<ProfileValidation> validations) {
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
