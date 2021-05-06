package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.icann.rdapconformance.validator.workflow.profile.tig_section.TigValidationTestBase.validateNotOk;
import static org.icann.rdapconformance.validator.workflow.profile.tig_section.TigValidationTestBase.validateOk;
import static org.mockito.Mockito.mock;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.HttpTestingUtils;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Validation1Dot6Test extends HttpTestingUtils {

  private RDAPValidatorResults results;

  @Override
  @BeforeMethod
  public void setUp() {
    super.setUp();
    WireMockConfiguration wmConfig = wireMockConfig()
        .dynamicHttpsPort()
        .bindAddress(WIREMOCK_HOST);
    prepareWiremock(wmConfig);
    results = mock(RDAPValidatorResults.class);
  }

  @Test
  public void testValidate_HttpHeadStatusSameAsGet_IsOk() {
    // configure wiremock for HTTP as we will make an HTTP request
    givenUri("http");
    stubFor(head(urlEqualTo(REQUEST_PATH))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")));

    validateOk(new Validation1Dot6(200, config, results), results);
  }

  @Test
  public void testValidate_HttpHeadStatusDifferentThanGet_AddResults20300() {
    // configure wiremock for HTTP as we will make an HTTP request
    givenUri("http");
    stubFor(head(urlEqualTo(REQUEST_PATH))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withStatus(404)));

    validateNotOk(new Validation1Dot6(200, config, results), results, -20300,
        200 + "\n/\n" + 404,
        "The HTTP Status code obtained when using the HEAD method is different from the "
            + "GET method. See section 1.6 of the RDAP_Technical_Implementation_Guide_2_1.");
  }
}