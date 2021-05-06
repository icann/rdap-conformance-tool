package org.icann.rdapconformance.validator.workflow.profile;

import java.util.List;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.TigValidation;

public class RDAPProfileFebruary2019 {

  private final List<TigValidation> validations;

  public RDAPProfileFebruary2019(List<TigValidation> validations) {
    this.validations = validations;
  }

  public boolean validate() {
    boolean result = true;
    for (TigValidation validation : validations) {
      result &= validation.validate();
    }

    return result;
  }
}
