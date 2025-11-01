package org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Set;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ResponseNameserverStatusValidationTest extends ProfileJsonValidationTestBase {

  public ResponseNameserverStatusValidationTest() {
    super("/validators/nameserver/valid.json", "nameserver_status");
  }

  @DataProvider(name = "invalidStatus")
  public static Object[][] invalidStatus() {
    return new Object[][]{{Set.of("active", "any")},
        {Set.of("pending delete", "client delete prohibited")},
        {Set.of("pending delete", "server delete prohibited")},
        {Set.of("pending update", "client update prohibited")},
        {Set.of("pending update", "server update prohibited")},
        {Set.of("pending create", "pending delete")},
        {Set.of("pending create", "pending renew")},
        {Set.of("pending create", "pending transfer")},
        {Set.of("pending create", "pending update")},
        {Set.of("pending delete", "pending renew")},
        {Set.of("pending delete", "pending transfer")},
        {Set.of("pending delete", "pending update")},
        {Set.of("pending renew", "pending transfer")},
        {Set.of("pending renew", "pending update")},
        {Set.of("pending transfer", "pending update")},
    };
  }

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
  }

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
    return new ResponseNameserverStatusValidation(nameserverContext);
  }

  @Test(dataProvider = "invalidStatus")
  public void testValidate_InvalidStatusCombination_AddResults47204(Set<String> status) {
    replaceValue("status", status);
    validate(-49300,
        "#/status:[\"" + String.join("\",\"", status) + "\"]",
        "The values of the status data structure does not comply with RFC5732.");
  }

  @Test
  public void testValidate_NoStatus_IsOk() {
    removeKey("status");
    validate();
  }

  @Test
  public void testDoLaunch() {
    // Test HELP query type
    QueryContext helpContext = new QueryContext(
        queryContext.getQueryId(),
        queryContext.getConfig(),
        queryContext.getDatasetService(),
        queryContext.getQuery(),
        queryContext.getResults(),
        RDAPQueryType.HELP
    );
    helpContext.setRdapResponseData(queryContext.getRdapResponseData());
    ResponseNameserverStatusValidation helpValidation = new ResponseNameserverStatusValidation(helpContext);
    assertThat(helpValidation.doLaunch()).isFalse();

    // Test NAMESERVERS query type
    QueryContext nameserversContext = new QueryContext(
        queryContext.getQueryId(),
        queryContext.getConfig(),
        queryContext.getDatasetService(),
        queryContext.getQuery(),
        queryContext.getResults(),
        RDAPQueryType.NAMESERVERS
    );
    nameserversContext.setRdapResponseData(queryContext.getRdapResponseData());
    ResponseNameserverStatusValidation nameserversValidation = new ResponseNameserverStatusValidation(nameserversContext);
    assertThat(nameserversValidation.doLaunch()).isFalse();

    // Test DOMAIN query type
    QueryContext domainContext = new QueryContext(
        queryContext.getQueryId(),
        queryContext.getConfig(),
        queryContext.getDatasetService(),
        queryContext.getQuery(),
        queryContext.getResults(),
        RDAPQueryType.DOMAIN
    );
    domainContext.setRdapResponseData(queryContext.getRdapResponseData());
    ResponseNameserverStatusValidation domainValidation = new ResponseNameserverStatusValidation(domainContext);
    assertThat(domainValidation.doLaunch()).isFalse();

    // Test ENTITY query type
    QueryContext entityContext = new QueryContext(
        queryContext.getQueryId(),
        queryContext.getConfig(),
        queryContext.getDatasetService(),
        queryContext.getQuery(),
        queryContext.getResults(),
        RDAPQueryType.ENTITY
    );
    entityContext.setRdapResponseData(queryContext.getRdapResponseData());
    ResponseNameserverStatusValidation entityValidation = new ResponseNameserverStatusValidation(entityContext);
    assertThat(entityValidation.doLaunch()).isFalse();

    // Test NAMESERVER query type (should return true)
    QueryContext nameserverContext = new QueryContext(
        queryContext.getQueryId(),
        queryContext.getConfig(),
        queryContext.getDatasetService(),
        queryContext.getQuery(),
        queryContext.getResults(),
        RDAPQueryType.NAMESERVER
    );
    nameserverContext.setRdapResponseData(queryContext.getRdapResponseData());
    ResponseNameserverStatusValidation nameserverValidation = new ResponseNameserverStatusValidation(nameserverContext);
    assertThat(nameserverValidation.doLaunch()).isTrue();
  }
}