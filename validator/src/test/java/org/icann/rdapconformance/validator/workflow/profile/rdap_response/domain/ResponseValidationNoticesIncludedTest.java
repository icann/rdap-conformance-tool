package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidationNoticesIncludedTest extends ProfileJsonValidationTestBase {

  private RDAPQueryType queryType;

  public ResponseValidationNoticesIncludedTest() {
    super("/validators/domain/valid.json", "rdapResponseProfile_notices_included_Validation");
  }

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    queryType = RDAPQueryType.DOMAIN;
  }

  @Override
  public ProfileValidation getProfileValidation() {
    return new ResponseValidationNoticesIncluded(jsonObject.toString(), results, queryType);
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
  public void testValidate_HandleFormatNotCompliant_AddResults46200() {
    removeKey("notices");
    validate(-46500, jsonObject.toString(),
        "A notices members does not appear in the RDAP response.");
  }
}