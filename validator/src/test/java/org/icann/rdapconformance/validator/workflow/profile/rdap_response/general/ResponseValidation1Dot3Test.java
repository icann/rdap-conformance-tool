package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidation;
import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidationTest;

public class ResponseValidation1Dot3Test extends RDAPConformanceValidationTest {

  protected ResponseValidation1Dot3Test() {
    super("rdapResponseProfile_1_3_Validation");
  }

  public RDAPConformanceValidation getProfileValidation() {
    return new ResponseValidation1Dot3(queryContext);
  }
}