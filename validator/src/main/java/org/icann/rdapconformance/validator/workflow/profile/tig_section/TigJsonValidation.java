package org.icann.rdapconformance.validator.workflow.profile.tig_section;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public abstract class TigJsonValidation extends TigValidation {

  protected final String rdapResponse;

  public TigJsonValidation(String rdapResponse, RDAPValidatorResults results) {
    super(results);
    this.rdapResponse = rdapResponse;
  }
}
