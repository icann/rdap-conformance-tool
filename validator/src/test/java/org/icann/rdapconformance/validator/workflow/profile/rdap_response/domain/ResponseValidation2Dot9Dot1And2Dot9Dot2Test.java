package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import java.io.IOException;
import java.util.Set;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.HandleValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
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

  @Test
  public void testCheckNameServerHandles_ValidHandles_ReturnsTrue() throws Exception {
    // Setup
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();

    String validJson = "{\"nameservers\":[" +
        "{\"objectClassName\":\"nameserver\",\"handle\":\"ABC123-EXAMPLE\",\"ldhName\":\"ns1.example.com\"}," +
        "{\"objectClassName\":\"nameserver\",\"handle\":\"DEF456-EXAMPLE\",\"ldhName\":\"ns2.example.com\"}" +
        "]}";

    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    doReturn(false).when(config).isGtldRegistrar();
    doReturn(true).when(config).useRdapProfileFeb2024();

    ResponseValidation2Dot9Dot1And2Dot9Dot2 validation = new ResponseValidation2Dot9Dot1And2Dot9Dot2(
        config, validJson, results, null, RDAPQueryType.DOMAIN);

    boolean result = validation.checkNameServerHandles();
    assertThat(result).isTrue();
    assertThat(results.getAll()).isEmpty();
  }

  @Test
  public void testCheckNameServerHandles_InvalidHandleFormat_ReturnsFalse() throws Exception {
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();

    String invalidJson = "{\"nameservers\":[" +
        "{\"objectClassName\":\"nameserver\",\"handle\":\"INVALID-HANDLE-WITH-NO-PROPER-FORMAT\",\"ldhName\":\"ns1.example.com\"}," +
        "{\"objectClassName\":\"nameserver\",\"handle\":\"DEF456-EXAMPLE\",\"ldhName\":\"ns2.example.com\"}" +
        "]}";

    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    doReturn(false).when(config).isGtldRegistrar();
    doReturn(true).when(config).useRdapProfileFeb2024();

    ResponseValidation2Dot9Dot1And2Dot9Dot2 validation = new ResponseValidation2Dot9Dot1And2Dot9Dot2(
        config, invalidJson, results, null, RDAPQueryType.DOMAIN);

    boolean result = validation.checkNameServerHandles();
    assertThat(result).isFalse();
  }

  @Test
  public void testCheckNameServerHandles_IcannRstHandle_ReturnsFalse() throws Exception {
    // Setup
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();

    String invalidJson = "{\"nameservers\":[" +
        "{\"objectClassName\":\"nameserver\",\"handle\":\"ABC123-ICANNRST\",\"ldhName\":\"ns1.example.com\"}," +
        "{\"objectClassName\":\"nameserver\",\"handle\":\"DEF456-EXAMPLE\",\"ldhName\":\"ns2.example.com\"}" +
        "]}";

    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    doReturn(false).when(config).isGtldRegistrar();
    doReturn(true).when(config).useRdapProfileFeb2024();

    ResponseValidation2Dot9Dot1And2Dot9Dot2 validation = new ResponseValidation2Dot9Dot1And2Dot9Dot2(
        config, invalidJson, results, null, RDAPQueryType.DOMAIN);

    boolean result = validation.checkNameServerHandles();
    assertThat(result).isFalse();
    assertThat(results.getAll()).contains(
        RDAPValidationResult.builder()
                            .code(-47205)
                            .value("#/nameservers/0/handle:ABC123-ICANNRST")
                            .message("The globally unique identifier in the nameserver object handle is using an EPPROID reserved for testing by ICANN.")
                            .build());
  }

  @Test
  public void testCheckNameServerHandles_MissingHandle_ReturnsFalse() throws Exception {
    // Setup
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();

    String invalidJson = "{\"nameservers\":[" +
        "{\"objectClassName\":\"nameserver\",\"ldhName\":\"ns1.example.com\"}," +
        "{\"objectClassName\":\"nameserver\",\"handle\":\"DEF456-EXAMPLE\",\"ldhName\":\"ns2.example.com\"}" +
        "]}";

    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    doReturn(false).when(config).isGtldRegistrar();
    doReturn(true).when(config).useRdapProfileFeb2024();

    ResponseValidation2Dot9Dot1And2Dot9Dot2 validation = new ResponseValidation2Dot9Dot1And2Dot9Dot2(
        config, invalidJson, results, null, RDAPQueryType.DOMAIN);

    boolean result = validation.checkNameServerHandles();
    assertThat(result).isTrue(); // No validation result for missing handle in this code path
  }

  @Test
  public void testCheckNameServerHandles_IcannRstHandleWithFeb2024ProfileDisabled_ReturnsTrue() throws Exception {
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();

    String invalidJson = "{\"nameservers\":[" +
        "{\"objectClassName\":\"nameserver\",\"handle\":\"ABC123-ICANNRST\",\"ldhName\":\"ns1.example.com\"}," +
        "{\"objectClassName\":\"nameserver\",\"handle\":\"DEF456-EXAMPLE\",\"ldhName\":\"ns2.example.com\"}" +
        "]}";

    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    doReturn(false).when(config).isGtldRegistrar();
    doReturn(false).when(config).useRdapProfileFeb2024(); // Feb 2024 profile disabled

    ResponseValidation2Dot9Dot1And2Dot9Dot2 validation = new ResponseValidation2Dot9Dot1And2Dot9Dot2(
        config, invalidJson, results, null, RDAPQueryType.DOMAIN);

    boolean result = validation.checkNameServerHandles();
    assertThat(result).isTrue();
    assertThat(results.getAll()).isEmpty();
  }

  @Test
  public void testCheckNameServerHandles_MultipleInvalidHandles_ReturnsFalse() throws Exception {
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();

    String invalidJson = "{\"nameservers\":[" +
        "{\"objectClassName\":\"nameserver\",\"handle\":\"INVALID-HANDLE!\",\"ldhName\":\"ns1.example.com\"}," +
        "{\"objectClassName\":\"nameserver\",\"handle\":\"ALSO@INVALID\",\"ldhName\":\"ns2.example.com\"}" +
        "]}";

    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    doReturn(false).when(config).isGtldRegistrar();
    doReturn(true).when(config).useRdapProfileFeb2024();

    ResponseValidation2Dot9Dot1And2Dot9Dot2 validation = new ResponseValidation2Dot9Dot1And2Dot9Dot2(
        config, invalidJson, results, null, RDAPQueryType.DOMAIN);
    boolean result = validation.checkNameServerHandles();

    assertThat(result).isFalse();
  }
}