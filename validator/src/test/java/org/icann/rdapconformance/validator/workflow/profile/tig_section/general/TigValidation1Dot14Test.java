package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidation;
import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidationTest;

public class TigValidation1Dot14Test extends RDAPConformanceValidationTest {

  protected TigValidation1Dot14Test() {
    super("tigSection_1_14_Validation");
  }

  public RDAPConformanceValidation getProfileValidation() {
    return new TigValidation1Dot14(queryContext);
  }
}
