package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.workflow.profile.rdap_response.QueryValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;

public class ResponseValidation2Dot1Test extends
    QueryValidationTest<ResponseValidation2Dot1> {

  public ResponseValidation2Dot1Test() {
    super("/validators/domain/valid.json", "rdapResponseProfile_2_1_Validation",
        RDAPQueryType.DOMAIN, ResponseValidation2Dot1.class);
  }
}