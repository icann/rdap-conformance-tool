package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.assertj.core.api.Assertions.assertThat;

import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidation;
import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class TigValidation1Dot14Test extends RDAPConformanceValidationTest {

  protected TigValidation1Dot14Test() {
    super("tigSection_1_14_Validation");
  }

  @Override
  public RDAPConformanceValidation getProfileValidation() {
    return new TigValidation1Dot14(jsonObject.toString(), results);
  }


  @Test
  void passesWithAnyTigProfileNumber() {
    JSONArray conformance = new JSONArray();
    conformance.put("icann_rdap_technical_implementation_guide_3");
    JSONObject obj = new JSONObject();
    obj.put("rdapConformance", conformance);

    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    TigValidation1Dot14 validation = new TigValidation1Dot14(obj.toString(), results);

    assertThat(validation.doValidate()).isTrue();
    assertThat(results.getAll()).isEmpty();
  }

  @Test
  void failsWithoutTigProfile() {
    JSONArray conformance = new JSONArray();
    conformance.put("other_profile");
    JSONObject obj = new JSONObject();
    obj.put("rdapConformance", conformance);

    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    TigValidation1Dot14 validation = new TigValidation1Dot14(obj.toString(), results);

    assertThat(validation.doValidate()).isFalse();
    assertThat(results.getAll()).hasSize(1);
    assertThat(results.getAll()).contains(RDAPValidationResult.builder()
                                                              .code(-20600)
                                                              .value("#/rdapConformance:[\"other_profile\"]")
                                                              .message("The RDAP Conformance data structure does not include icann_rdap_technical_implementation_guide_0. "
                                                                  + "See section 1.14 of the RDAP_Technical_Implementation_Guide_2_1.")
                                                              .build());
  }

}
