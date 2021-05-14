package org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver;

import org.icann.rdapconformance.validator.workflow.profile.rdap_response.QueryValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;

public class ResponseValidation4Dot1QueryTest extends
    QueryValidationTest<ResponseValidation4Dot1Query> {

  public ResponseValidation4Dot1QueryTest() {
    super("/validators/nameserver/valid.json", "rdapResponseProfile_4_1_Validation",
        RDAPQueryType.NAMESERVER, ResponseValidation4Dot1Query.class);
  }

}