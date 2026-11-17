package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import java.util.Set;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ResponseValidationRFC3915Test extends ResponseDomainValidationTestBase {

  public ResponseValidationRFC3915Test() {
    super("rdapResponseProfile_rfc3915_Validation");
  }

  public static Object[][] invalidStatusRedemption() {
    return new Object[][]{
        {Set.of("redemption period", "any")}
    };
  }

  public static Object[][] invalidStatusPendingRestore() {
    return new Object[][]{
            {Set.of("pending restore", "any")}
    };
  }

  public static Object[][] validStatus() {
    return new Object[][]{{Set.of("redemption period", "any", "pending delete")},
        {Set.of("pending restore", "pending delete")}
    };
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
    return new ResponseValidationRFC3915(domainContext);
  }

  @Test
  @Override
  public void testDoLaunch() {
    // Test doLaunch behavior by creating validation instances with different query types
    // Domain validations should return true only when query type is DOMAIN

    QueryContext helpContext = new QueryContext(queryContext.getQueryId(),
        queryContext.getConfig(), queryContext.getDatasetService(),
        queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.HELP);
    helpContext.setRdapResponseData(queryContext.getRdapResponseData());
    assertThat(new ResponseValidationRFC3915(helpContext).doLaunch()).isFalse();

    QueryContext nameserversContext = new QueryContext(queryContext.getQueryId(),
        queryContext.getConfig(), queryContext.getDatasetService(),
        queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.NAMESERVERS);
    nameserversContext.setRdapResponseData(queryContext.getRdapResponseData());
    assertThat(new ResponseValidationRFC3915(nameserversContext).doLaunch()).isFalse();

    QueryContext nameserverContext = new QueryContext(queryContext.getQueryId(),
        queryContext.getConfig(), queryContext.getDatasetService(),
        queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.NAMESERVER);
    nameserverContext.setRdapResponseData(queryContext.getRdapResponseData());
    assertThat(new ResponseValidationRFC3915(nameserverContext).doLaunch()).isFalse();

    QueryContext entityContext = new QueryContext(queryContext.getQueryId(),
        queryContext.getConfig(), queryContext.getDatasetService(),
        queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.ENTITY);
    entityContext.setRdapResponseData(queryContext.getRdapResponseData());
    assertThat(new ResponseValidationRFC3915(entityContext).doLaunch()).isFalse();

    QueryContext domainContext = new QueryContext(queryContext.getQueryId(),
        queryContext.getConfig(), queryContext.getDatasetService(),
        queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.DOMAIN);
    domainContext.setRdapResponseData(queryContext.getRdapResponseData());
    assertThat(new ResponseValidationRFC3915(domainContext).doLaunch()).isTrue();
  }


  @ParameterizedTest(name = "{0}")
  @MethodSource("validStatus")
  public void testValidate_ok(Set<String> status) {
    replaceValue("status", status);
    super.testValidate_ok();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidStatusRedemption")
  public void testValidate_InvalidStatusCombination_AddResults47001(Set<String> status) {
    replaceValue("status", status);
    validate(-47001, "#/status:[\"" + String.join("\",\"", status) + "\"]",
        "'redemption period' is only valid with a status of 'pending delete'");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidStatusPendingRestore")
  public void testValidate_InvalidStatusCombination_AddResults47002(Set<String> status) {
    replaceValue("status", status);
    validate(-47002, "#/status:[\"" + String.join("\",\"", status) + "\"]",
            "'pending restore' is only valid with a status of 'pending delete'");
  }
}