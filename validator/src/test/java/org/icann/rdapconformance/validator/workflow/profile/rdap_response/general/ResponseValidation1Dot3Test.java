package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import static org.assertj.core.api.Assertions.assertThat;

import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidation;
import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class ResponseValidation1Dot3Test extends RDAPConformanceValidationTest {

  protected ResponseValidation1Dot3Test() {
    super("rdapResponseProfile_1_3_Validation");
  }

  @Override
  public RDAPConformanceValidation getProfileValidation() {
    return new ResponseValidation1Dot3(jsonObject.toString(), results);
  }

  @Test
  void passesWithAnyProfileNumber() {
    JSONArray conformance = new JSONArray();
    conformance.put("icann_rdap_response_profile_2");
    JSONObject obj = new JSONObject();
    obj.put("rdapConformance", conformance);

    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    ResponseValidation1Dot3 validation = new ResponseValidation1Dot3(obj.toString(), results);

    assertThat(validation.doValidate()).isTrue();
    assertThat(results.getAll()).isEmpty();
  }

  @Test
  void failsWithoutProfile() {
    JSONArray conformance = new JSONArray();
    conformance.put("other_profile");
    JSONObject obj = new JSONObject();
    obj.put("rdapConformance", conformance);

    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    ResponseValidation1Dot3 validation = new ResponseValidation1Dot3(obj.toString(), results);

    assertThat(validation.doValidate()).isFalse();
    assertThat(results.getAll()).hasSize(1);
    assertThat(results.getAll()).contains(RDAPValidationResult.builder()
                                                              .code(-40200)
                                                              .value("#/rdapConformance:[\"other_profile\"]")
                                                              .message("The RDAP Conformance data structure does not include icann_rdap_response_profile_0. "
                                                                  + "See section 1.3 of the RDAP_Response_Profile_2_1.")
                                                              .build());
  }
}