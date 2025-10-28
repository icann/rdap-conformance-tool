package org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver;

import static org.assertj.core.api.Assertions.assertThat;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.QueryValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;

public class ResponseValidation4Dot1QueryTest extends QueryValidationTest {

  public ResponseValidation4Dot1QueryTest() {
    super("/validators/nameserver/valid.json", "rdapResponseProfile_4_1_Validation",
        RDAPQueryType.NAMESERVER);
  }

  @Override
  public ProfileValidation getProfileValidation() {
    QueryContext nameserverContext = new QueryContext(
        queryContext.getQueryId(),
        queryContext.getConfig(),
        queryContext.getDatasetService(),
        queryContext.getQuery(),
        queryContext.getResults(),
        RDAPQueryType.NAMESERVER
    );
    nameserverContext.setRdapResponseData(queryContext.getRdapResponseData());
    return new ResponseValidation4Dot1Query(nameserverContext);
  }

  @Override
  public void testDoLaunch() {
    // Test doLaunch behavior by creating validation instances with different query types
    // Nameserver validations should return true only when query type is NAMESERVER

    QueryContext helpContext = new QueryContext(queryContext.getQueryId(),
        queryContext.getConfig(), queryContext.getDatasetService(),
        queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.HELP);
    helpContext.setRdapResponseData(queryContext.getRdapResponseData());
    assertThat(new ResponseValidation4Dot1Query(helpContext).doLaunch()).isFalse();

    QueryContext nameserversContext = new QueryContext(queryContext.getQueryId(),
        queryContext.getConfig(), queryContext.getDatasetService(),
        queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.NAMESERVERS);
    nameserversContext.setRdapResponseData(queryContext.getRdapResponseData());
    assertThat(new ResponseValidation4Dot1Query(nameserversContext).doLaunch()).isFalse();

    QueryContext domainContext = new QueryContext(queryContext.getQueryId(),
        queryContext.getConfig(), queryContext.getDatasetService(),
        queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.DOMAIN);
    domainContext.setRdapResponseData(queryContext.getRdapResponseData());
    assertThat(new ResponseValidation4Dot1Query(domainContext).doLaunch()).isFalse();

    QueryContext entityContext = new QueryContext(queryContext.getQueryId(),
        queryContext.getConfig(), queryContext.getDatasetService(),
        queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.ENTITY);
    entityContext.setRdapResponseData(queryContext.getRdapResponseData());
    assertThat(new ResponseValidation4Dot1Query(entityContext).doLaunch()).isFalse();

    QueryContext nameserverContext = new QueryContext(queryContext.getQueryId(),
        queryContext.getConfig(), queryContext.getDatasetService(),
        queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.NAMESERVER);
    nameserverContext.setRdapResponseData(queryContext.getRdapResponseData());
    assertThat(new ResponseValidation4Dot1Query(nameserverContext).doLaunch()).isTrue();
  }
}