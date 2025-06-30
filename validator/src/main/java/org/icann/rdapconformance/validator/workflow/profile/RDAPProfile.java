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
    
    // Check if aggressive network parallelization is enabled
    boolean aggressiveNetworkParallel = "true".equals(System.getProperty("rdap.parallel.network", "false"));
    
    if (validations != null) {
      // Original single list approach
      if (aggressiveNetworkParallel) {
        // Separate network from non-network validations for parallel execution
        List<ProfileValidation> networkValidations = new ArrayList<>();
        List<ProfileValidation> nonNetworkValidations = new ArrayList<>();
        
        for (ProfileValidation validation : validations) {
          if (isNetworkValidation(validation)) {
            networkValidations.add(validation);
          } else {
            nonNetworkValidations.add(validation);
          }
        }
        
        // Execute non-network validations sequentially (fast)
        for (ProfileValidation validation : nonNetworkValidations) {
          logger.debug("Validating (non-network): {}", validation.getGroupName());
          result &= validation.validate();
        }
        
        // Execute network validations aggressively in parallel
        if (!networkValidations.isEmpty()) {
          logger.info("Executing {} network validations with aggressive parallelization", networkValidations.size());
          result &= NetworkValidationCoordinator.executeNetworkValidations(networkValidations);
        }
      } else {
        // Sequential execution (default)
        for (ProfileValidation validation : validations) {
          logger.info("Validating: {}", validation.getGroupName());
          result &= validation.validate();
        }
      }
    } else if (parallelValidations != null || sequentialValidations != null) {
      // New separated approach
      if (parallelValidations != null && !parallelValidations.isEmpty()) {
        logger.debug("Executing {} non-network validations sequentially", parallelValidations.size());
        for (ProfileValidation validation : parallelValidations) {
          logger.debug("Validating (non-network): {}", validation.getGroupName());
          result &= validation.validate();
        }
      }
      
      if (sequentialValidations != null && !sequentialValidations.isEmpty()) {
        if (aggressiveNetworkParallel) {
          logger.info("Executing {} network validations with aggressive parallelization", sequentialValidations.size());
          result &= NetworkValidationCoordinator.executeNetworkValidations(sequentialValidations);
        } else {
          logger.info("Executing {} network validations sequentially", sequentialValidations.size());
          for (ProfileValidation validation : sequentialValidations) {
            logger.info("Validating (sequential): {}", validation.getGroupName());
            result &= validation.validate();
          }
        }
      }
    }
    
    return result;
  }
  
  /**
   * Determines if a validation is network-dependent based on its class name.
   */
  private boolean isNetworkValidation(ProfileValidation validation) {
    String className = validation.getClass().getSimpleName();
    return className.contains("Tig") && (
        className.equals("TigValidation1Dot2") ||
        className.equals("TigValidation1Dot3") ||
        className.equals("TigValidation1Dot5_2024") ||
        className.equals("TigValidation1Dot6") ||
        className.equals("TigValidation1Dot8") ||
        className.equals("TigValidation1Dot11Dot1") ||
        className.equals("TigValidation1Dot13")
    ) || className.contains("ResponseValidation") && (
        className.equals("ResponseValidationHelp_2024") ||
        className.equals("ResponseValidationDomainInvalid_2024") ||
        className.equals("ResponseValidationTestInvalidRedirect_2024")
    );
  }
}
