package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import java.util.Collections;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.testng.annotations.Test;

public class ResponseValidation2Dot6Dot1Test extends ResponseDomainValidationTestBase {

  public ResponseValidation2Dot6Dot1Test() {
    super("rdapResponseProfile_2_6_1_Validation");
  }

  @Override
  public ProfileValidation getProfileValidation() {
    return new ResponseValidation2Dot6Dot1(jsonObject.toString(), results, queryType);
  }

  @Test
  public void testValidate_EmptyStatus_AddResults47100() {
    replaceValue("status", Collections.emptyList());
    validate(-47100, "#/status:[]",
        "The status member does not contain at least one value.");
  }

  @Test
  public void testValidate_NoStatus_AddResults47100() {
    removeKey("status");
    validate(-47100, "#/status:null",
        "The status member does not contain at least one value.");
  }
}