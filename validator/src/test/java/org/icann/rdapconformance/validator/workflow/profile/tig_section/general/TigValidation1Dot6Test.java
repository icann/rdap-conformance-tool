package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.net.InetAddress;
import org.icann.rdapconformance.validator.DNSCacheResolver;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.HttpTestingUtils;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.ValidationTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TigValidation1Dot6Test extends HttpTestingUtils implements ValidationTest {

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

  @Override
  public ProfileValidation getProfileValidation() {
    return new TigValidation1Dot6(200, config, results);
  }

  @Test
  public void testValidate_HttpHeadStatusSameAsGet_IsOk() throws Exception {
    try (var mockedStatic = mockStatic(DNSCacheResolver.class)) {
      mockedStatic.when(() -> DNSCacheResolver.getFirstV4Address("127.0.0.1"))
                  .thenReturn(InetAddress.getByName("127.0.0.1"));
      mockedStatic.when(() -> DNSCacheResolver.getFirstV6Address("127.0.0.1"))
                  .thenReturn(null);

      givenUri("http");
      stubFor(head(urlEqualTo(REQUEST_PATH))
          .withScheme("http")
          .willReturn(aResponse()
              .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
              .withStatus(200)));

      validateOk(results);
    }
  }

  @Test
  public void testValidate_HttpHeadStatusDifferentThanGet_AddResults20300() throws Exception {
    try (var mockedStatic = mockStatic(DNSCacheResolver.class)) {
      mockedStatic.when(() -> DNSCacheResolver.getFirstV4Address("127.0.0.1"))
                  .thenReturn(InetAddress.getByName("127.0.0.1"));
      mockedStatic.when(() -> DNSCacheResolver.getFirstV6Address("127.0.0.1"))
                  .thenReturn(null);

      givenUri("http");
      stubFor(head(urlEqualTo(REQUEST_PATH))
          .withScheme("http")
          .willReturn(aResponse()
              .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
              .withStatus(404)));

      validateNotOk(results, -20300,
          200 + "\n/\n" + 404,
          "The HTTP Status code obtained when using the HEAD method is different from the "
              + "GET method. See section 1.6 of the RDAP_Technical_Implementation_Guide_2_1.");
    }
  }
}