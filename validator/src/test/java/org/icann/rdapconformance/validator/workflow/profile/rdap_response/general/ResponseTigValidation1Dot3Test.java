package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidation;
import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidationTest;

public class ResponseTigValidation1Dot3Test extends
    RDAPConformanceValidationTest {

  protected ResponseTigValidation1Dot3Test() {
    super("rdapResponseProfile_1_3_Validation");
  }

  @Override
  public RDAPConformanceValidation getTigValidation() {
    return new ResponseValidation1Dot3(jsonObject.toString(), results);
  }
}