package org.icann.rdapconformance.validator.workflow.profile;

import java.util.List;
import org.icann.rdapconformance.validator.SchemaValidator;
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
      try {
        result &= validation.validate();
      } catch (Exception e) {
        logger.info("Exception during validation of : {} \n details: {}",
            validation.getClass().getSimpleName(), e);
        result = false;
      }
    }

    return result;
  }
}
