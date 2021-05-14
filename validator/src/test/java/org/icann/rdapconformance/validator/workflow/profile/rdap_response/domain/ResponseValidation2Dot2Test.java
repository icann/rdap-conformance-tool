package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.workflow.profile.rdap_response.HandleValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;

public class ResponseValidation2Dot2Test extends HandleValidationTest<ResponseValidation2Dot2> {

  public ResponseValidation2Dot2Test() {
    super("/validators/domain/valid.json", "rdapResponseProfile_2_1_Validation",
        RDAPQueryType.DOMAIN, ResponseValidation2Dot2.class);
  }

  @Override
  protected String givenInvalidHandle() {
    replaceValue("handle", "ABCD");
    return "#/handle:ABCD";
  }

  @Override
  protected String getValidValueWithRoidExmp() {
    return "#/handle:2138514_DOMAIN_COM-EXMP";
  }
}