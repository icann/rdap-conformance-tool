package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public abstract class TigValidation {

  private final RDAPValidatorResults results;

  public TigValidation(RDAPValidatorResults results) {
    this.results = results;
  }

  public boolean validate() {
    if (doValidate()) {
      results.addGroup(getGroupName(), false);
      return true;
    }
    results.addGroup(getGroupName(), true);
    return false;
  }

  protected abstract String getGroupName();

  protected abstract boolean doValidate();
}
