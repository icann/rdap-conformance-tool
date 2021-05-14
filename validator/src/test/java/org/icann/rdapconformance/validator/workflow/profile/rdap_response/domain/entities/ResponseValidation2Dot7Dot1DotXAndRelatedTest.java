package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
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

  private void remarkMemberIs(String key, String value) {
    if (jsonObject.query("#/entities/0/remarks") == null) {
      putValue("$['entities'][0]",
          "remarks",
          List.of(Map.of(key, value)));
    }

    putValue("$['entities'][0]['remarks'][0]", key, value);
    assertThat((String) getValue("$['entities'][0]['remarks'][0]['" + key + "']")).isEqualTo(value);
  }

  private void entitiesWithRole(String role) {
    replaceValue("$['entities'][0]['roles'][0]", role);
    assertThat((String) getValue("$['entities'][0]['roles'][0]")).isEqualTo(role);
  }
}