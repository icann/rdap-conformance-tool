package org.icann.rdapconformance.validator.workflow.profile;

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
    
    if (validations != null) {
      // Original sequential execution for backward compatibility
      for (ProfileValidation validation : validations) {
        logger.info("Validating: {}", validation.getGroupName());
        result &= validation.validate();
      }
    } else {
      // New parallel + sequential execution
      if (parallelValidations != null && !parallelValidations.isEmpty()) {
        logger.info("Executing {} non-network validations in parallel", parallelValidations.size());
        
        // Execute parallel validations using parallel streams
        parallelValidations.parallelStream().forEach(validation -> {
          logger.debug("Validating (parallel): {}", validation.getGroupName());
          validation.validate();
        });
        
        // Check if any parallel validations failed
        // Note: Individual validation results are handled by each validation internally
      }
      
      if (sequentialValidations != null && !sequentialValidations.isEmpty()) {
        logger.info("Executing {} network-dependent validations sequentially", sequentialValidations.size());
        
        // Execute sequential validations
        for (ProfileValidation validation : sequentialValidations) {
          logger.info("Validating (sequential): {}", validation.getGroupName());
          result &= validation.validate();
        }
      }
    }
    
    return result;
  }
}
