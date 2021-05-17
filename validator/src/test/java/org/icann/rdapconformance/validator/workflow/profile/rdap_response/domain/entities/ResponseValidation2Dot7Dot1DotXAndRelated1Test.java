package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.testng.annotations.Test;

public class ResponseValidation2Dot7Dot1DotXAndRelated1Test extends
    ResponseValidation2Dot7Dot1DotXAndRelatedTest {

  @Override
  public ProfileValidation getProfileValidation() {
    return new ResponseValidation2Dot7Dot1DotXAndRelated1(jsonObject.toString(), results,
        queryType, config);
  }

  @Test
  public void typeValidForRoleRegistrantAndRemarksRedactedForPrivacy() {
    entitiesWithRole("registrant");
    remarkMemberIs("title", "REDACTED FOR PRIVACY");
    remarkMemberIs("type", "object redacted due to authorization");
    validateOk(results);
  }

  @Test
  public void typeInvalidForRoleRegistrantAndRemarksRedactedForPrivacy() {
    remarkMemberIs("type", "wrong type");
    validateRemark();
  }

  @Test
  public void typeUnexistingForRoleRegistrantAndRemarksRedactedForPrivacy() {
    validateRemark();
  }

  private void validateRemark() {
    entitiesWithRole("registrant");
    remarkMemberIs("title", "REDACTED FOR PRIVACY");
    // validate that the type member is "object redacted due to authorization":
    validate(-52100, "#/entities/0:" + jsonObject.query("#/entities/0"),
        "An entity with the registrant, administrative, technical or "
            + "billing role with a remarks members with the title \"REDACTED FOR PRIVACY\" was "
            + "found, but the description and type does not contain the value in 2.7.4.3 of the "
            + "RDAP_Response_Profile_2_1.");
  }
}