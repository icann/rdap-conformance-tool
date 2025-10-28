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

  protected RDAPQueryType queryType;

  public ResponseDomainValidationTestBase(String testGroupName) {
    super("/validators/domain/valid.json", testGroupName);
  }

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    queryType = RDAPQueryType.DOMAIN;
  }

  @Test
  public void testDoLaunch() {
    queryType = RDAPQueryType.HELP;
    updateQueryContext();
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.NAMESERVERS;
    updateQueryContext();
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.NAMESERVER;
    updateQueryContext();
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.ENTITY;
    updateQueryContext();
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.DOMAIN;
    updateQueryContext();
    assertThat(getProfileValidation().doLaunch()).isTrue();
  }

  private void updateQueryContext() {
    // Update QueryContext with new query type for proper validation behavior
    queryContext = new org.icann.rdapconformance.validator.QueryContext(
        queryContext.getQueryId(),
        queryContext.getConfig(),
        queryContext.getDatasetService(),
        queryContext.getQuery(),
        queryContext.getResults(),
        queryType);
    queryContext.setRdapResponseData(rdapContent);
  }

}