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
        
        // Execute network validations with HTTP/HTTPS separation
        if (!networkValidations.isEmpty()) {
          logger.info("Executing {} network validations with timeout-prone separation", networkValidations.size());
          
          // Separate timeout-prone from normal validations
          NetworkValidationCoordinator.NetworkValidationGroups groups = 
              NetworkValidationCoordinator.categorizeNetworkValidations(networkValidations);
          
          List<ProfileValidation> timeoutProneValidations = groups.getTimeoutProneValidations();
          List<ProfileValidation> normalValidations = groups.getNormalValidations();
          
          if (!timeoutProneValidations.isEmpty() || !normalValidations.isEmpty()) {
            logger.info("Separated validations: {} timeout-prone (async), {} normal (sync)", 
                       timeoutProneValidations.size(), normalValidations.size());
            
            // Extract timeout from any validation that has config access
            int timeoutSeconds = extractTimeoutFromValidations(timeoutProneValidations, normalValidations);
            result &= NetworkValidationCoordinator.executeHttpAndHttpsValidations(timeoutProneValidations, normalValidations, timeoutSeconds);
          }
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
          logger.info("Executing {} network validations with HTTP/HTTPS separation", sequentialValidations.size());
          
          // Separate HTTP from HTTPS validations
          NetworkValidationCoordinator.NetworkValidationGroups groups = 
              NetworkValidationCoordinator.categorizeNetworkValidations(sequentialValidations);
          
          List<ProfileValidation> httpValidations = groups.getHttpValidations();
          List<ProfileValidation> httpsValidations = groups.getHttpsValidations();
          
          if (!httpValidations.isEmpty() || !httpsValidations.isEmpty()) {
            logger.info("Separated validations: {} HTTP (async), {} HTTPS (sync)", 
                       httpValidations.size(), httpsValidations.size());
            
            // Extract timeout from any validation that has config access
            int timeoutSeconds = extractTimeoutFromValidations(httpValidations, httpsValidations);
            result &= NetworkValidationCoordinator.executeHttpAndHttpsValidations(httpValidations, httpsValidations, timeoutSeconds);
          }
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
  
  /**
   * Extracts timeout from validations that have config access.
   * Uses reflection to access the config field in validation classes.
   * Returns a default of 20 seconds if no config is found.
   */
  private int extractTimeoutFromValidations(List<ProfileValidation> timeoutProneValidations, List<ProfileValidation> normalValidations) {
    // Try to extract timeout from any validation that has a config field
    List<ProfileValidation> allValidations = new ArrayList<>();
    if (timeoutProneValidations != null) allValidations.addAll(timeoutProneValidations);
    if (normalValidations != null) allValidations.addAll(normalValidations);
    
    for (ProfileValidation validation : allValidations) {
      try {
        // Use reflection to access the config field
        java.lang.reflect.Field configField = validation.getClass().getDeclaredField("config");
        configField.setAccessible(true);
        Object config = configField.get(validation);
        
        if (config != null) {
          // Call getTimeout() method on the config object
          java.lang.reflect.Method getTimeoutMethod = config.getClass().getMethod("getTimeout");
          Object timeoutObj = getTimeoutMethod.invoke(config);
          if (timeoutObj instanceof Integer) {
            return (Integer) timeoutObj;
          }
        }
      } catch (Exception e) {
        // Continue to next validation if reflection fails
        logger.debug("Could not extract timeout from validation: {}", validation.getGroupName());
      }
    }
    
    // Default timeout if no config found (20 seconds is typical default)
    return 20;
  }
}
