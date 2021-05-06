package org.icann.rdapconformance.validator.workflow.profile.tig_section;

import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public abstract class TigHttpValidation extends ProfileValidation {

  public TigHttpValidation(RDAPValidatorResults results) {
    super(results);
  }
}
