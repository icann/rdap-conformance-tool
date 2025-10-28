package org.icann.rdapconformance.validator.workflow.profile.rdap_response.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.util.Map;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ResponseValidation3Dot2Test extends ProfileJsonValidationTestBase {

  private RDAPQueryType queryType;
  private RDAPValidatorConfiguration config;


  public ResponseValidation3Dot2Test() {
    super("/validators/entity/valid.json", "rdapResponseProfile_3_2_Validation");
  }

  @DataProvider(name = "roleVcardIndex")
  public static Object[][] roleVcardIndex() {
    return new Object[][]{{"administrative", 1}, {"administrative", 2}, {"administrative", 3},
        {"technical", 1}, {"technical", 2}, {"technical", 3}};
  }

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    queryType = RDAPQueryType.ENTITY;
    config = mock(RDAPValidatorConfiguration.class);
    doReturn(true).when(config).isGtldRegistry();
  }

  public ProfileValidation getProfileValidation() {
    return new ResponseValidation3Dot2(jsonObject.toString(), results, queryType, config);
  }

  @Test(dataProvider = "roleVcardIndex")
  public void testValidate_vcardDoesNotContainMemberButNotARelevantRole_isOk(String role,
      int index) {
    removeKey(String.format("$['entities'][0]['vcardArray'][1][%d]", index));
    validate();
  }

  @Test(dataProvider = "roleVcardIndex")
  public void testValidate_vcardDoesNotContainMember_AddResults60101(String role, int index) {
    JSONObject entity = new JSONObject(JsonPath
        .parse(new JSONObject((Map) getValue("$['entities'][0]")).toString())
        .delete(String.format("$['vcardArray'][1][%d]", index))
        .set("$['roles'][0]", role)
        .jsonString());  // json object to get the same order as the validation
    replaceValue("$['entities'][0]['roles'][0]", role);
    removeKey(String.format("$['entities'][0]['vcardArray'][1][%d]", index));
    validate(-60200, "#/entities/0:" + entity,
        "The required members for entities with the administrative and technical roles "
            + "were not found. See section 3.2 of the RDAP_Response_Profile_2_1.");
  }

  @Test
  public void testDoLaunch() {
    queryType = RDAPQueryType.HELP;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.NAMESERVERS;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.NAMESERVER;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.DOMAIN;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.ENTITY;
    assertThat(getProfileValidation().doLaunch()).isTrue();
    doReturn(false).when(config).isGtldRegistry();
    assertThat(getProfileValidation().doLaunch()).isFalse();
  }

}