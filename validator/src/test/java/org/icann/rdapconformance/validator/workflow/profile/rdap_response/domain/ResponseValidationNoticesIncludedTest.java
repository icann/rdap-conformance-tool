package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.testng.annotations.Test;

public class ResponseValidationNoticesIncludedTest extends ResponseDomainValidationTestBase {


  public ResponseValidationNoticesIncludedTest() {
    super("rdapResponseProfile_notices_included_Validation");
  }

  public ProfileValidation getProfileValidation() {
    return new ResponseValidationNoticesIncluded(queryContext);
  }

  @Test
  public void testValidate_NoticesAbsent_AddResults46500() {
    removeKey("notices");
    validate(-46500, jsonObject.toString(),
        "A notices members does not appear in the RDAP response.");
  }
}