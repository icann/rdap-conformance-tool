package org.icann.rdapconformance.validator.workflow.profile.rdap_response.miscellaneous;

import org.icann.rdapconformance.validator.EventAction;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.TopMostEventActionValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public class ResponseValidationLastUpdateEvent extends TopMostEventActionValidation {

  public ResponseValidationLastUpdateEvent(String rdapResponse, RDAPValidatorResults results,
                                          RDAPQueryType queryType) {
    super(rdapResponse, results, queryType,
        -43100,
        "An eventAction type last update of RDAP database does not "
            + "exists in the topmost events data structure. See section 2.3.1.3, 2.7.6, 3.3 and "
            + "4.4 of the RDAP_Response_Profile_2_1.",
            EventAction.LAST_UPDATE_OF_RDAP_DATABASE);
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_3_1_3_and_2_7_6_and_3_3_and_4_4_Validation";
  }

  @Override
  public boolean doLaunch() {
    return queryType.isLookupQuery();
  }
}
