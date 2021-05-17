package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.json.JSONArray;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidation2Dot7Dot1DotXAndRelatedTest extends ProfileJsonValidationTestBase {

  RDAPQueryType queryType;
  RDAPValidatorConfiguration config;

  public ResponseValidation2Dot7Dot1DotXAndRelatedTest() {
    super("/validators/domain/valid.json",
        "rdapResponseProfile_2_7_1_X_and_2_7_2_X_and_2_7_3_X_and_2_7_4_X_Validation");
  }

  @Override
  @BeforeMethod
  public void setUp() throws java.io.IOException {
    super.setUp();
    queryType = RDAPQueryType.DOMAIN;
    config = mock(RDAPValidatorConfiguration.class);
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
    return new ResponseValidation2Dot7Dot1DotXAndRelated(jsonObject.toString(), results,
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

  /**
   * 8.8.1.5
   */
  @Test
  public void moreThanOneEntityWithRegistrantRole() {
    entitiesWithRole("registrant");
    // add again the registrant
    jsonObject.getJSONArray("entities").put(jsonObject.getJSONArray("entities").getJSONObject(0));
    // verify we have two entity with registrant role:
    assertThat((List<String>) getValue("$.entities[*].roles[?(@ == 'registrant')]")).hasSize(2);

    String twoRegistrants = "#/entities/0:" + jsonObject.query("#/entities/0") + ", " +
        "#/entities/1:" + jsonObject.query("#/entities/1");
    validate(-52104, twoRegistrants, "More than one entity with the following roles were found: "
        + "registrant, administrative, technical and billing.");
  }

  /**
   * 8.8.1.6
   */
  @Test
  public void ccParameterNotIncluded() {
    entitiesWithRole("registrant");
    removeKey("$.['entities'][0]['vcardArray'][1][*][*]['cc']");
    validate(-52105, "#/entities/0:" + jsonObject.query("#/entities/0"),
        "An entity with the registrant role without the CC parameter "
            + "was found. See section 2.7.4.1 of the RDAP_Response_Profile_2_1.");
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

  protected void remarkMemberIs(String key, String value) {
    if (jsonObject.query("#/entities/0/remarks") == null) {
      putValue("$['entities'][0]",
          "remarks",
          List.of(Map.of(key, value)));
    }

    putValue("$['entities'][0]['remarks'][0]", key, value);
    assertThat((String) getValue("$['entities'][0]['remarks'][0]['" + key + "']")).isEqualTo(value);
  }

  protected void entitiesWithRole(String role) {
    replaceValue("$['entities'][0]['roles'][0]", role);
    assertThat((String) getValue("$['entities'][0]['roles'][0]")).isEqualTo(role);
  }
}