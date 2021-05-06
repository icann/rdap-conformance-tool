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
    if (doValidate()) {
      results.addGroup(getGroupName(), false);
      return true;
    }
    results.addGroup(getGroupName(), true);
    return false;
  }

  public abstract String getGroupName();

  protected abstract boolean doValidate();

  protected boolean doLaunch() {
    return true;
  }
}