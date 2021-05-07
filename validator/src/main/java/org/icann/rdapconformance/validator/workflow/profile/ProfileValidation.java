package org.icann.rdapconformance.validator.workflow.profile;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public abstract class ProfileValidation {

  protected final RDAPValidatorResults results;

  public ProfileValidation(RDAPValidatorResults results) {
    this.results = results;
  }

  public boolean validate() {
    if (!doLaunch()) {
      return true;
    }
    results.addGroup(getGroupName());
    if (doValidate()) {
      return true;
    }
    results.addGroupErrorWarning(getGroupName());
    return false;
  }

  public abstract String getGroupName();

  protected abstract boolean doValidate();

  public boolean doLaunch() {
    return true;
  }
}