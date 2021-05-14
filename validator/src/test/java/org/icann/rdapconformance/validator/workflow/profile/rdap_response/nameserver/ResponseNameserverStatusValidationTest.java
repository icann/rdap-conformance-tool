package org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Set;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ResponseNameserverStatusValidationTest extends ProfileJsonValidationTestBase {

  private RDAPQueryType queryType;

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
    queryType = RDAPQueryType.NAMESERVER;
  }


  @Override
  public ProfileValidation getProfileValidation() {
    return new ResponseNameserverStatusValidation(jsonObject.toString(), results, queryType);
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
    queryType = RDAPQueryType.HELP;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.NAMESERVERS;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.DOMAIN;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.ENTITY;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.NAMESERVER;
    assertThat(getProfileValidation().doLaunch()).isTrue();
  }
}