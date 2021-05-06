package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public final class ResponseValidation1Dot3 extends RDAPConformanceValidation {

  public ResponseValidation1Dot3(String rdapResponse, RDAPValidatorResults results) {
    super(rdapResponse, results, "icann_rdap_response_profile_0", -40200,
        "The RDAP Conformance data structure does not include icann_rdap_response_profile_0. "
            + "See section 1.3 of the RDAP_Response_Profile_2_1.");
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_1_3_Validation";
  }
}
