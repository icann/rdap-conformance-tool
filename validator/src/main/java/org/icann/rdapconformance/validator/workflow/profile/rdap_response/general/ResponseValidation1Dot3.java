package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONArray;
import org.json.JSONObject;

public final class ResponseValidation1Dot3 extends RDAPConformanceValidation {

  public ResponseValidation1Dot3(String rdapResponse, RDAPValidatorResults results) {
    super(rdapResponse, results, "icann_rdap_response_profile_0", -40200,
        "The RDAP Conformance data structure does not include icann_rdap_response_profile_0. "
            + "See section 1.3 of the RDAP_Response_Profile_2_1.");
  }

  // Ensure that the digit at the end of the profile name can be higher than 0
@Override
protected boolean doValidate() {
    String jsonPointer = "#/rdapConformance";
    JSONArray rdapConformance = (JSONArray) new JSONObject(jsonObject.toString())
        .query(jsonPointer);

    if (rdapConformance == null) {
        results.add(RDAPValidationResult.builder()
            .code(-40200)
            .value(getResultValue(jsonPointer))
            .message("The RDAP Conformance data structure does not include icann_rdap_response_profile_0. "
                + "See section 1.3 of the RDAP_Response_Profile_2_1.")
            .build());
        return false;
    }

    boolean found = false;
    for (Object v : rdapConformance) {
        if (v instanceof String && ((String) v).matches("icann_rdap_response_profile_\\d+")) {
            found = true;
            break;
        }
    }

    if (!found) {
        results.add(RDAPValidationResult.builder()
            .code(-40200)
            .value(getResultValue(jsonPointer))
            .message("The RDAP Conformance data structure does not include icann_rdap_response_profile_0. "
                + "See section 1.3 of the RDAP_Response_Profile_2_1.")
            .build());
        return false;
    }

    return true;
}
  @Override
  public String getGroupName() {
    return "rdapResponseProfile_1_3_Validation";
  }
}
