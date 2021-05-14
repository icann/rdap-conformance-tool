package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.workflow.profile.rdap_response.RegistrarEntityValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;

public class ResponseValidation2Dot4Dot2And2Dot4Dot3Test extends
    RegistrarEntityValidationTest<ResponseValidation2Dot4Dot2And2Dot4Dot3> {

  public ResponseValidation2Dot4Dot2And2Dot4Dot3Test() {
    super("/validators/domain/valid.json", "rdapResponseProfile_2_4_2_and_2_4_3_Validation",
        RDAPQueryType.DOMAIN, ResponseValidation2Dot4Dot2And2Dot4Dot3.class);
  }
}