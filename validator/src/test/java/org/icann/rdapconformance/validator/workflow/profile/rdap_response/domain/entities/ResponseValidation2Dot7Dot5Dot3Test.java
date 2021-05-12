package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.testng.annotations.Test;

public class ResponseValidation2Dot7Dot5Dot3Test extends ProfileJsonValidationTestBase {

  public ResponseValidation2Dot7Dot5Dot3Test() {
    super(
        "/validators/profile/rdap_response/domain/entities/rdapResponseProfile_2_7_5_3_Validation.json",
        "rdapResponseProfile_2_7_5_3_Validation");
  }

  @Test
  public void remarkInvalidForRoleRegistrant() {
    // replace with target role:
    replaceValue("$['entities'][0]['roles'][0]", "registrant");
    // there is no email, nor remark, so we have an invalid object, continue...
    validate(-55000, "#/entities/0:" + jsonObject.query("#/entities/0"),
        "An entity with the administrative, technical, or billing role "
        + "without a valid \"EMAIL REDACTED FOR PRIVACY\" remark was found. See section 2.7.5.3 "
        + "of the RDAP_Response_Profile_2_1.");
  }

  @Test
  public void remarkInvalidForRoleRegistrantButWithEmail() {
    // replace with target role:
    replaceValue("$['entities'][0]['entities'][0]['roles'][0]", "registrant");
    // but email is not omitted
    validateOk(results);
  }

  @Override
  public ProfileValidation getProfileValidation() {
    return new ResponseValidation2Dot7Dot5Dot3(jsonObject.toString(), results);
  }
}
