package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.EventAction;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.TopMostEventActionValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public final class ResponseValidation2Dot3Dot1Dot2 extends TopMostEventActionValidation {

  public ResponseValidation2Dot3Dot1Dot2(String rdapResponse, RDAPValidatorResults results, RDAPQueryType queryType) {
    super(rdapResponse, results, queryType,
        -46400,
        "An eventAction of type expiration does not exists in the topmost events data structure. "
            + "See section 2.3.1.2 of the RDAP_Response_Profile_2_1.",
            EventAction.EXPIRATION);
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_3_1_2_Validation";
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.DOMAIN);
  }
}
