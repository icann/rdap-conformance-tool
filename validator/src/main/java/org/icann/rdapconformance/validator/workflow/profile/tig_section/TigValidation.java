package org.icann.rdapconformance.validator.workflow.profile.tig_section;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public abstract class TigValidation {

  protected final RDAPValidatorResults results;

  public TigValidation(RDAPValidatorResults results) {
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

  protected abstract String getGroupName();

  protected abstract boolean doValidate();

  protected boolean doLaunch() {
    return true;
  }
}