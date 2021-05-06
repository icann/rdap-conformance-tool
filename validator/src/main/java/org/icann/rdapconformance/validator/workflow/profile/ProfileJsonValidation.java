package org.icann.rdapconformance.validator.workflow.profile;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public abstract class ProfileJsonValidation extends ProfileValidation {

  protected final String rdapResponse;

  public ProfileJsonValidation(String rdapResponse, RDAPValidatorResults results) {
    super(results);
    this.rdapResponse = rdapResponse;
  }
}
