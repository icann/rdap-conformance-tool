package org.icann.rdapconformance.validator.workflow.profile.rdap_response.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ResponseValidation3Dot1Test extends ProfileJsonValidationTestBase {

  private RDAPQueryType queryType;
  private RDAPValidatorConfiguration config;


  public ResponseValidation3Dot1Test() {
    super("/validators/entity/valid.json", "rdapResponseProfile_3_1_Validation");
  }

  @DataProvider(name = "vcardMemberIndex")
  public static Object[][] vcardMemberIndex() {
    return new Object[][]{{1}, {2}, {3}, {4}};
  }

  @DataProvider(name = "rddsFieldIndex")
  public static Object[][] rddsFieldIndex() {
    return new Object[][]{{2}, {3}, {6}};
  }


  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    queryType = RDAPQueryType.ENTITY;
    config = mock(RDAPValidatorConfiguration.class);
    doReturn(true).when(config).isGtldRegistry();
  }

  public ProfileValidation getProfileValidation() {
    return new ResponseValidation3Dot1(jsonObject.toString(), results, queryType, config);
  }

  @Test
  public void testValidate_NoRegistrarEntity_AddResults60100() {
    replaceValue("$['roles'][0]", "registrant");
    validate(-60100, jsonObject.toString(),
        "An entity with the registrar role was not found as the topmost object. "
            + "See section 3.1 of the RDAP_Response_Profile_2_1");
  }


  @Test(dataProvider = "vcardMemberIndex")
  public void testValidate_vcardDoesNotContainMember_AddResults60101(int index) {
    removeKey(String.format("$['vcardArray'][1][%d]", index));
    validate(-60101, jsonObject.toString(),
        "The required members for a registrar entity were not found. "
            + "See section 3.1 of the RDAP_Response_Profile_2_1.");
  }


  @Test(dataProvider = "rddsFieldIndex")
  public void testValidate_vcardAdrDoesNotContainRDDSField_AddResults60101(int index) {
    replaceValue(String.format("$['vcardArray'][1][2][3][%d]", index), "");
    validate(-60101, jsonObject.toString(),
        "The required members for a registrar entity were not found. "
            + "See section 3.1 of the RDAP_Response_Profile_2_1.");
  }

  @Test
  public void testValidate_NoHandle_AddResults60101() {
    removeKey("handle");
    validate(-60101, jsonObject.toString(),
        "The required members for a registrar entity were not found. "
            + "See section 3.1 of the RDAP_Response_Profile_2_1.");
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