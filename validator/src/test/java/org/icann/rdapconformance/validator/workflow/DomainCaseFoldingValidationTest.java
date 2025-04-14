package org.icann.rdapconformance.validator.workflow;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.net.URI;
import java.net.http.HttpResponse;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.HttpTestingUtils;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.ValidationTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DomainCaseFoldingValidationTest extends HttpTestingUtils implements ValidationTest {

  private RDAPValidatorResults results;
  private HttpResponse<String> httpsResponse;

  @BeforeMethod
  public void setUp() {
    super.setUp();
    results = mock(RDAPValidatorResults.class);
    httpsResponse = mock(HttpResponse.class);
    doReturn(URI.create("http://domain/test.example")).when(httpsResponse).uri();
  }


  @Override
  public ProfileValidation getProfileValidation() {
    return new DomainCaseFoldingValidation(httpsResponse, config, results,
        RDAPQueryType.DOMAIN);
  }

  @Test
  public void testFoldDomain() {
    DomainCaseFoldingValidation validator = new DomainCaseFoldingValidation(httpsResponse, config
        , results, RDAPQueryType.DOMAIN);
    assertThat(validator.foldDomain()).isEqualTo("tEsT.ExAmPlE");
  }

  @Test
  public void testFoldingDigitAndDot() {
    doReturn(URI.create("http://domain/123.example")).when(httpsResponse).uri();
    DomainCaseFoldingValidation validator = new DomainCaseFoldingValidation(httpsResponse, config
        , results, RDAPQueryType.DOMAIN);
    assertThat(validator.foldDomain()).isEqualTo("123.eXaMpLe");
  }

  @Test
  public void testFoldingCharNotFoldeable() {
    doReturn(URI.create("http://domain/test.国xample")).when(httpsResponse).uri();
    DomainCaseFoldingValidation validator = new DomainCaseFoldingValidation(httpsResponse, config
        , results, RDAPQueryType.DOMAIN);
    assertThat(validator.foldDomain()).isEqualTo("tEsT.国xAmPlE");
  }

  @Test
  public void testNrLdhLabel() {
    WireMockConfiguration wmConfig = wireMockConfig()
        .dynamicHttpsPort()
        .bindAddress(WIREMOCK_HOST);
    prepareWiremock(wmConfig);

    givenUri("http");
    doReturn(config.getUri()).when(httpsResponse).uri();
    doReturn(RDAP_RESPONSE).when(httpsResponse).body();
    givenUriWithDifferentResponse("/domain/tEsT.ExAmPlE");

    validateNotOk(results,
        -10403, "http://127.0.0.1:8080/domain/tEsT.ExAmPlE",
        "RDAP responses do not match when handling domain label case folding.");
  }

  @Test
  public void testDoLaunch() {
    assertThat(getProfileValidation().doLaunch()).isTrue();
    assertThat(
        new DomainCaseFoldingValidation(
            httpsResponse,
            config,
            results,
            RDAPQueryType.NAMESERVER).doLaunch()).isFalse();
  }

  private void givenUriWithDifferentResponse(String path) {
    givenUri("http", path);
    stubFor(get(urlEqualTo(path))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody("{\"objectClassName\": \"domain\", \"unexpected\":\"property\"}")));
  }
}