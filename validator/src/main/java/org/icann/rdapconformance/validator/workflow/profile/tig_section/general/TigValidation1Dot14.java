package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

public class TigValidation1Dot14 extends RDAPConformanceValidation {


  public TigValidation1Dot14(String rdapResponse, RDAPValidatorResults results) {
    super(rdapResponse, results, "icann_rdap_technical_implementation_guide_0", -20600,
        "The RDAP Conformance data structure does not include icann_rdap_technical_implementation_guide_0. "
            + "See section 1.14 of the RDAP_Technical_Implementation_Guide_2_1.");
  }


  @Override
  protected boolean doValidate() {
    String jsonPointer = "#/rdapConformance";
    JSONArray rdapConformance = (JSONArray) new JSONObject(jsonObject.toString())
        .query(jsonPointer);

    if (rdapConformance == null) {
      results.add(RDAPValidationResult.builder()
                                      .code(-20600)
                                      .value(getResultValue(jsonPointer))
                                      .message("The RDAP Conformance data structure does not include icann_rdap_technical_implementation_guide_0. "
                                          + "See section 1.14 of the RDAP_Technical_Implementation_Guide_2_1.")
                                      .build());
      return false;
    }

    boolean found = false;
    for (Object v : rdapConformance) {
      if (v instanceof String && ((String) v).matches("icann_rdap_technical_implementation_guide_\\d+")) {
        found = true;
        break;
      }
    }

    if (!found) {
      results.add(RDAPValidationResult.builder()
                                      .code(-20600)
                                      .value(getResultValue(jsonPointer))
                                      .message("The RDAP Conformance data structure does not include icann_rdap_technical_implementation_guide_0. "
                                          + "See section 1.14 of the RDAP_Technical_Implementation_Guide_2_1.")
                                      .build());
      return false;
    }

    return true;
  }

  @Override
  public String getGroupName() {
    return "tigSection_1_14_Validation";
  }
}
