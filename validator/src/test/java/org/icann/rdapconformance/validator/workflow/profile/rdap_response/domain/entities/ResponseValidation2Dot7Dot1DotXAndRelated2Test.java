package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;

import java.io.IOException;
import java.util.List;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidation2Dot7Dot1DotXAndRelated2Test extends
    ResponseValidation2Dot7Dot1DotXAndRelatedTest {

  @Override
  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    String validVcardJson = getResource(
        "/validators/profile/rdap_response/domain/entities/vcard/valid.json");
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

  @Test
  public void countryNotIncludedInAdrProperty() {
    // remove all elements including country:
    removeKey("$.['entities'][0]['vcardArray'][1][4][3][2:6]");
    assertThat((List<String>) getValue("$.['entities'][0]['vcardArray'][1][4][3]")).hasSize(3);
    validate52101();
  }

  private void validateWithoutProperty(String property) {
    removeKey("$.['entities'][0]['vcardArray'][1][*][?(@ == '" + property + "')]");
    validate52101();
  }

  private void validate52101() {
    entitiesWithRole("registrant");
    remarkMemberIs("title", "NOT REDACTED FOR PRIVACY");
    validate(-52101, "#/entities/0:" + jsonObject.query("#/entities/0"),
        "An entity with the registrant, administrative, technical or "
            + "billing role with a remarks members with the title \"REDACTED FOR PRIVACY\" was "
            + "not found, but the description and type does not contain the value in 2.7.4.3 of the "
            + "RDAP_Response_Profile_2_1.");
  }
}