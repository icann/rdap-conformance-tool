package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import java.io.IOException;
import java.util.Set;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.HandleValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ResponseValidation2Dot9Dot1And2Dot9Dot2Test extends
    HandleValidationTest<ResponseValidation2Dot9Dot1And2Dot9Dot2> {
  public ResponseValidation2Dot9Dot1And2Dot9Dot2Test() {
    super("/validators/domain/valid.json", "rdapResponseProfile_2_9_1_and_2_9_2_Validation",
        RDAPQueryType.DOMAIN, ResponseValidation2Dot9Dot1And2Dot9Dot2.class);
  }

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    config = mock(RDAPValidatorConfiguration.class);
    doReturn(true).when(config).isGtldRegistrar();
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

  @Test
  public void testValidate_NoLdhNameInNameserver_AddResults47200() {
    removeKey("$['nameservers'][0]['ldhName']");
    validate(-47200, "#/nameservers/0:{\"objectClassName\":\"nameserver\","
            + "\"handle\":\"2138514_NS1_DOMAIN_COM-EXMP\",\"status\":[\"active\"]}",
        "A nameserver object without ldhName was found.");
  }

  @Override
  protected String givenInvalidHandle() {
    replaceValue("$['nameservers'][0]['handle']", "ABCD");
    return "#/nameservers/0/handle:ABCD";
  }

  @Override
  protected String getValidValueWithRoidExmp() {
    return "#/nameservers/0/handle:2138514_NS1_DOMAIN_COM-EXMP";
  }

  @Test
  public void testValidate_OneNameserverWithoutHandle_AddResults47203() {
    removeKey("$['nameservers'][0]['handle']");
    validate(-47203,
        "#/nameservers/0:{\"objectClassName\":\"nameserver\","
            + "\"ldhName\":\"NS1.EXAMPLE.COM\",\"status\":[\"active\"]}",
        "The handle or status in the nameserver object is not included.");
  }

  @Test
  public void testValidate_OneNameserverWithoutStatus_AddResults47203() {
    removeKey("$['nameservers'][0]['status']");
    validate(-47203,
        "#/nameservers/0:{\"objectClassName\":\"nameserver\","
            + "\"handle\":\"2138514_NS1_DOMAIN_COM-EXMP\",\"ldhName\":\"NS1.EXAMPLE.COM\"}",
        "The handle or status in the nameserver object is not included.");
  }

  @Test(dataProvider = "invalidStatus")
  public void testValidate_InvalidStatusCombination_AddResults47204(Set<String> status) {
    replaceValue("$['nameservers'][0]['status']", status);
    validate(-47204,
        "#/nameservers/0/status:[\"" + String.join("\",\"", status) + "\"]",
        "The values of the status data structure does not comply with RFC5732.");
  }
}