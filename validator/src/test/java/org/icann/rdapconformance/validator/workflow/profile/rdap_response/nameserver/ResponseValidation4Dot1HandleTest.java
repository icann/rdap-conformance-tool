package org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver;

import org.icann.rdapconformance.validator.workflow.profile.rdap_response.HandleValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;

public class ResponseValidation4Dot1HandleTest extends
    HandleValidationTest<ResponseValidation4Dot1Handle> {

  public ResponseValidation4Dot1HandleTest() {
    super("/validators/domain/valid.json", "rdapResponseProfile_4_1_Validation",
        RDAPQueryType.NAMESERVER, ResponseValidation4Dot1Handle.class);
  }

  @Override
  protected String givenInvalidHandle() {
    replaceValue("handle", "ABCD");
    return "#/handle:ABCD";
  }

  @Override
  protected String getValidValueWithRoidExmp() {
    return "#/handle:2138514_NS_COM-EXMP";
  }

}