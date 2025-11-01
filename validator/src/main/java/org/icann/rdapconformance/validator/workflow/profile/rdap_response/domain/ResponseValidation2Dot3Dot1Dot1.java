package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.EventAction;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.TopMostEventActionValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public final class ResponseValidation2Dot3Dot1Dot1 extends TopMostEventActionValidation {

  public ResponseValidation2Dot3Dot1Dot1(QueryContext qctx) {
    super(qctx,
        -46300,
        "An eventAction of type registration does not exists in the topmost events data structure. "
            + "See section 2.3.1.1 of the RDAP_Response_Profile_2_1.",
        EventAction.REGISTRATION);
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_3_1_1_Validation";
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.DOMAIN);
  }
}
