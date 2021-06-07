package org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver;

import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.QueryValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;

public class ResponseValidation4Dot1QueryTest extends QueryValidationTest {

  public ResponseValidation4Dot1QueryTest() {
    super("/validators/nameserver/valid.json", "rdapResponseProfile_4_1_Validation",
        RDAPQueryType.NAMESERVER);
  }

  @Override
  public ProfileValidation getProfileValidation() {
    return new ResponseValidation4Dot1Query(jsonObject.toString(), results, config, queryType);
  }
}