package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class ResponseDomainValidationTestBase extends ProfileJsonValidationTestBase {

  public ResponseDomainValidationTestBase(String testGroupName) {
    super("/validators/domain/valid.json", testGroupName);
  }

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    queryContext.setQueryType(RDAPQueryType.DOMAIN);
  }

  @Test
  public void testDoLaunch() {
    queryContext.setQueryType(RDAPQueryType.HELP);
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryContext.setQueryType(RDAPQueryType.NAMESERVERS);
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryContext.setQueryType(RDAPQueryType.NAMESERVER);
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryContext.setQueryType(RDAPQueryType.ENTITY);
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryContext.setQueryType(RDAPQueryType.DOMAIN);
    assertThat(getProfileValidation().doLaunch()).isTrue();
  }

}