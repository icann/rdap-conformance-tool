package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidation2Dot10Test extends ProfileJsonValidationTestBase {

  private RDAPQueryType queryType;

  public ResponseValidation2Dot10Test() {
    super("/validators/domain/valid.json", "rdapResponseProfile_2_10_Validation");
  }

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    queryType = RDAPQueryType.DOMAIN;
  }

  @Override
  public ProfileValidation getProfileValidation() {
    return new ResponseValidation2Dot10(jsonObject.toString(), results, queryType);
  }

  @Test
  public void testDoLaunch() {
    queryType = RDAPQueryType.HELP;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.NAMESERVERS;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.NAMESERVER;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.ENTITY;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.DOMAIN;
    assertThat(getProfileValidation().doLaunch()).isTrue();
  }

  @Test
  public void testValidate_SecureDNSAbsent_AddResults46800() {
    removeKey("secureDNS");
    validate(-46800, jsonObject.toString(),
        "A secureDNS member does not appear in the domain object.");
  }
}