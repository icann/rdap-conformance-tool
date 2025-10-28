package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.RegistrarEntityPublicIdsValidation;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.RegistrarEntityValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;

public class ResponseValidation2Dot4Dot2And2Dot4Dot3Test extends
    RegistrarEntityValidationTest {

  public ResponseValidation2Dot4Dot2And2Dot4Dot3Test() {
    super("/validators/domain/valid.json", "rdapResponseProfile_2_4_2_and_2_4_3_Validation",
        RDAPQueryType.DOMAIN);
  }

  @Override
  public RegistrarEntityPublicIdsValidation getProfileValidation() {
    QueryContext domainContext = new QueryContext(
        queryContext.getQueryId(),
        queryContext.getConfig(),
        queryContext.getDatasetService(),
        queryContext.getQuery(),
        queryContext.getResults(),
        RDAPQueryType.DOMAIN
    );
    domainContext.setRdapResponseData(queryContext.getRdapResponseData());
    return new ResponseValidation2Dot4Dot2And2Dot4Dot3(domainContext);
  }

  @Override
  public void testDoLaunch() {
    // Test doLaunch behavior by creating validation instances with different query types
    // Domain validations should return true only when query type is DOMAIN

    QueryContext helpContext = new QueryContext(queryContext.getQueryId(),
        queryContext.getConfig(), queryContext.getDatasetService(),
        queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.HELP);
    helpContext.setRdapResponseData(queryContext.getRdapResponseData());
    assertThat(new ResponseValidation2Dot4Dot2And2Dot4Dot3(helpContext).doLaunch()).isFalse();

    QueryContext nameserversContext = new QueryContext(queryContext.getQueryId(),
        queryContext.getConfig(), queryContext.getDatasetService(),
        queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.NAMESERVERS);
    nameserversContext.setRdapResponseData(queryContext.getRdapResponseData());
    assertThat(new ResponseValidation2Dot4Dot2And2Dot4Dot3(nameserversContext).doLaunch()).isFalse();

    QueryContext nameserverContext = new QueryContext(queryContext.getQueryId(),
        queryContext.getConfig(), queryContext.getDatasetService(),
        queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.NAMESERVER);
    nameserverContext.setRdapResponseData(queryContext.getRdapResponseData());
    assertThat(new ResponseValidation2Dot4Dot2And2Dot4Dot3(nameserverContext).doLaunch()).isFalse();

    QueryContext entityContext = new QueryContext(queryContext.getQueryId(),
        queryContext.getConfig(), queryContext.getDatasetService(),
        queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.ENTITY);
    entityContext.setRdapResponseData(queryContext.getRdapResponseData());
    assertThat(new ResponseValidation2Dot4Dot2And2Dot4Dot3(entityContext).doLaunch()).isFalse();

    QueryContext domainContext = new QueryContext(queryContext.getQueryId(),
        queryContext.getConfig(), queryContext.getDatasetService(),
        queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.DOMAIN);
    domainContext.setRdapResponseData(queryContext.getRdapResponseData());
    assertThat(new ResponseValidation2Dot4Dot2And2Dot4Dot3(domainContext).doLaunch()).isTrue();
  }
}