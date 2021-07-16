package org.icann.rdapconformance.validator.workflow.profile;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDAPProfileFebruary2019 {

  private static final Logger logger = LoggerFactory.getLogger(RDAPProfileFebruary2019.class);
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
