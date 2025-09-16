package org.icann.rdapconformance.validator.workflow.profile;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProfileValidation {

  private static final Logger logger = LoggerFactory.getLogger(ProfileValidation.class);
  protected final RDAPValidatorResults results;
  protected final RDAPValidatorConfiguration config;

  public ProfileValidation(RDAPValidatorResults results,
                           RDAPValidatorConfiguration config) {
    this.results = results;
    this.config = config;
  }

  public boolean validate() {
    if (!doLaunch()) {
      return true;
    }
    results.addGroup(getGroupName());
    try {
      if (doValidate()) {
        return true;
      }
    } catch (Exception e) {
      logger.info("Exception during validation of : {} \n details: {}",
          this.getClass().getSimpleName(), e);
    }
    results.addGroupErrorWarning(getGroupName());
    return false;
  }

  public abstract String getGroupName();

  protected abstract boolean doValidate() throws Exception;

  public boolean doLaunch() {
    return true;
  }
}