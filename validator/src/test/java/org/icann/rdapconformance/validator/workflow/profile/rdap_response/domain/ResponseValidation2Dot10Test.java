package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.Test;

public class ResponseValidation2Dot10Test extends ResponseDomainValidationTestBase {


  public ResponseValidation2Dot10Test() {
    super("rdapResponseProfile_2_10_Validation");
  }

  @Override
  public ProfileValidation getProfileValidation() {
    QueryContext domainContext = new QueryContext(
        queryContext.getQueryId(),
        queryContext.getConfig(),
        queryContext.getDatasetService(),
        queryContext.getQuery(),
        queryContext.getResults(),
        RDAPQueryType.DOMAIN
    );
    domainContext.setRdapResponseData(queryContext.getRdapResponseData());
    return new ResponseValidation2Dot10(domainContext);
  }

  @Override
  public void testDoLaunch() {
    // Test doLaunch behavior by creating validation instances with different query types
    // Domain validations should return true only when query type is DOMAIN

    QueryContext helpContext = new QueryContext(queryContext.getQueryId(),
        queryContext.getConfig(), queryContext.getDatasetService(),
        queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.HELP);
    helpContext.setRdapResponseData(queryContext.getRdapResponseData());
    assertThat(new ResponseValidation2Dot10(helpContext).doLaunch()).isFalse();

    QueryContext nameserversContext = new QueryContext(queryContext.getQueryId(),
        queryContext.getConfig(), queryContext.getDatasetService(),
        queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.NAMESERVERS);
    nameserversContext.setRdapResponseData(queryContext.getRdapResponseData());
    assertThat(new ResponseValidation2Dot10(nameserversContext).doLaunch()).isFalse();

    QueryContext nameserverContext = new QueryContext(queryContext.getQueryId(),
        queryContext.getConfig(), queryContext.getDatasetService(),
        queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.NAMESERVER);
    nameserverContext.setRdapResponseData(queryContext.getRdapResponseData());
    assertThat(new ResponseValidation2Dot10(nameserverContext).doLaunch()).isFalse();

    QueryContext entityContext = new QueryContext(queryContext.getQueryId(),
        queryContext.getConfig(), queryContext.getDatasetService(),
        queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.ENTITY);
    entityContext.setRdapResponseData(queryContext.getRdapResponseData());
    assertThat(new ResponseValidation2Dot10(entityContext).doLaunch()).isFalse();

    QueryContext domainContext = new QueryContext(queryContext.getQueryId(),
        queryContext.getConfig(), queryContext.getDatasetService(),
        queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.DOMAIN);
    domainContext.setRdapResponseData(queryContext.getRdapResponseData());
    assertThat(new ResponseValidation2Dot10(domainContext).doLaunch()).isTrue();
  }


  @Test
  public void testValidate_SecureDNSAbsent_AddResults46800() {
    removeKey("secureDNS");
    validate(-46800, jsonObject.toString(),
        "A secureDNS member does not appear in the domain object.");
  }

  @Test
  public void testValidate_delegationSignedAbsent_AddResults46801() {
    removeKey("secureDNS.delegationSigned");
    validate(-46801, jsonObject.toString(),
        "The delegationSigned element does not exist.");
  }

  @Test
  public void testValidate_dsDataAndKeyDataAbsent_AddResults46802() {
    replaceValue("secureDNS.delegationSigned", true);
    removeKey("secureDNS.dsData");
    validate(-46802, jsonObject.toString(),
        "delegationSigned value is true, but no dsData nor keyData "
            + "name/value pair exists.");
  }
}