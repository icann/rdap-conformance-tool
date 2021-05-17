package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;

import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidation2Dot7Dot1DotXAndRelated2Test extends
    ResponseValidation2Dot7Dot1DotXAndRelatedTest {

  @Override
  @BeforeMethod
  public void setUp() throws java.io.IOException {
    super.setUp();
    String validVcardJson =
        getResource(
            "/validators/profile/rdap_response/domain/entities/8.8.1.2/validVcardArray.json");
    jsonObject
        .getJSONArray("entities")
        .getJSONObject(0)
        .put("vcardArray", new JSONArray(validVcardJson));
  }

  @Override
  public ProfileValidation getProfileValidation() {
    return new ResponseValidation2Dot7Dot1DotXAndRelated2(jsonObject.toString(), results,
        queryType, config);
  }

  @Test
  public void withoutFnWithoutRedactedForPrivacyTitle() {
    validateWithoutProperty("fn");
  }

  @Test
  public void withoutAdrWithoutRedactedForPrivacyTitle() {
    validateWithoutProperty("adr");
  }

  @Test
  public void withoutTelWithoutRedactedForPrivacyTitle() {
    validateWithoutProperty("tel");
  }

  @Test
  public void withoutEmailWithoutRedactedForPrivacyTitle() {
    validateWithoutProperty("email");
  }

  private void validateWithoutProperty(String property) {
    entitiesWithRole("registrant");
    remarkMemberIs("title", "NOT REDACTED FOR PRIVACY");
    removeKey("$.['entities'][0]['vcardArray'][1][*][?(@ == '" + property + "')]");
    validate(-52101, "#/entities/0:" + jsonObject.query("#/entities/0"),
        "An entity with the registrant, administrative, technical or "
            + "billing role with a remarks members with the title \"REDACTED FOR PRIVACY\" was "
            + "found, but the description and type does not contain the value in 2.7.4.3 of the "
            + "RDAP_Response_Profile_2_1.");
  }
}