package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.ResponseDomainValidationTestBase;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidation2Dot7Dot5Dot3Test extends ResponseDomainValidationTestBase {

  private RDAPValidatorConfiguration config;

  public ResponseValidation2Dot7Dot5Dot3Test() {
    super("rdapResponseProfile_2_7_5_3_Validation");
  }

  @Override
  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    config = mock(RDAPValidatorConfiguration.class);
    doReturn(true).when(config).isGtldRegistry();
    doReturn(false).when(config).isThin();
  }

  @Test
  public void remarkInvalidForRoleRegistrant() {
    // replace with target role:
    replaceValue("$['entities'][0]['roles'][0]", "registrant");

    // there is no email, nor remark, so we have an invalid object:
    List<String> vcardArrayElement = getValue("$.entities[0].vcardArray[*][*][*]");
    assertThat(vcardArrayElement).doesNotContain("email");
    Object remarks = jsonObject.query("#/entities/0/remarks");
    assertThat(remarks).isNull();

    validate(-55000, "#/entities/0:" + jsonObject.query("#/entities/0"),
        "An entity with the administrative, technical, or billing role "
            + "without a valid \"EMAIL REDACTED FOR PRIVACY\" remark was found. "
            + "See section 2.7.5.3 of the RDAP_Response_Profile_2_1.");
  }

  @Test
  public void remarkInvalidForRoleRegistrantButWithEmail() {
    // replace with target role:
    replaceValue("$['entities'][0]['entities'][0]['roles'][0]", "registrant");
    // but email is not omitted
    validateOk(results);
  }

  @Test
  public void testDoLaunch() {
    super.testDoLaunch();
    queryType = RDAPQueryType.DOMAIN;
    doReturn(false).when(config).isGtldRegistry();
    assertThat(getProfileValidation().doLaunch()).isFalse();
  }

  @Override
  public ProfileValidation getProfileValidation() {
    return new ResponseValidation2Dot7Dot5Dot3(jsonObject.toString(), results, queryType, config);
  }
}
