package org.icann.rdapconformance.validator.workflow.profile;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDAPProfile {

  private static final Logger logger = LoggerFactory.getLogger(RDAPProfile.class);
  private final List<ProfileValidation> validations;
  private final List<ProfileValidation> parallelValidations;
  private final List<ProfileValidation> sequentialValidations;

  public RDAPProfile(List<ProfileValidation> validations) {
    this.validations = validations;
    this.parallelValidations = null;
    this.sequentialValidations = null;
  }

  public RDAPProfile(List<ProfileValidation> parallelValidations, List<ProfileValidation> sequentialValidations) {
    this.validations = null;
    this.parallelValidations = parallelValidations;
    this.sequentialValidations = sequentialValidations;
  }

  public boolean validate() {
    boolean result = true;
    
    // Simple sequential execution - just like master branch
    if (validations != null) {
      for (ProfileValidation validation : validations) {
        logger.info("Validating: {}", validation.getGroupName());
        result &= validation.validate();
      }
    } else if (parallelValidations != null || sequentialValidations != null) {
      // If someone is using the new constructor, combine all validations and run sequentially
      List<ProfileValidation> allValidations = new ArrayList<>();
      if (parallelValidations != null) {
        allValidations.addAll(parallelValidations);
      }
      if (sequentialValidations != null) {
        allValidations.addAll(sequentialValidations);
      }
      
      for (ProfileValidation validation : allValidations) {
        logger.info("Validating: {}", validation.getGroupName());
        result &= validation.validate();
      }
    }
    
    return result;
  }
}
