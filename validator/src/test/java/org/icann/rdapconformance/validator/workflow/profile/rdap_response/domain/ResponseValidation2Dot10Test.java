package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.testng.annotations.Test;

public class ResponseValidation2Dot10Test extends ResponseDomainValidationTestBase {


  public ResponseValidation2Dot10Test() {
    super("rdapResponseProfile_2_10_Validation");
  }

  @Override
  public ProfileValidation getProfileValidation() {
    return new ResponseValidation2Dot10(jsonObject.toString(), results, queryType);
  }

  @Test
  public void testValidate_SecureDNSAbsent_AddResults46800() {
    removeKey("secureDNS");
    validate(-46800, jsonObject.toString(),
        "A secureDNS member does not appear in the domain object.");
  }
}