package org.icann.rdapconformance.validator.workflow.profile;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDAPProfile {

  private static final Logger logger = LoggerFactory.getLogger(RDAPProfile.class);
  private final List<ProfileValidation> validations;

  public RDAPProfile(List<ProfileValidation> validations) {
    this.validations = validations;
  }

  public boolean validate() {
    boolean result = true;

    for (ProfileValidation validation : validations) {
      logger.info("Validating: {}", validation.getGroupName());
      result &= validation.validate();
    }

    return result;
  }
}
