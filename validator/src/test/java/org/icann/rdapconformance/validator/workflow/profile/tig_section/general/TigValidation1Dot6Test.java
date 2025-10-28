package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.HttpTestingUtils;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.ValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TigValidation1Dot6Test extends HttpTestingUtils implements ValidationTest {

  private RDAPValidatorResults results;

  @BeforeMethod
  public void setUp() {
    super.setUp();
    WireMockConfiguration wmConfig = wireMockConfig()
        .dynamicHttpsPort()
        .bindAddress(WIREMOCK_HOST);
    prepareWiremock(wmConfig);
    results = mock(RDAPValidatorResults.class);
    // Override queryContext to use our mock results
    queryContext = QueryContext.forTesting("{}", results, config);
  }

  public ProfileValidation getProfileValidation() {
    return new TigValidation1Dot6(200, queryContext);
  }

  @Test
  public void testValidate_HttpHeadStatusSameAsGet_IsOk() throws Exception {
    givenUri("http");
    stubFor(head(urlEqualTo(REQUEST_PATH))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withStatus(200)));

    validateOk(results);
  }

  @Test
  public void testValidate_HttpHeadStatusDifferentThanGet_AddResults20300() throws Exception {
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

  @Test
  public void testValidate_HttpHead400Error_HandledProperly() throws Exception {

      givenUri("http");
      stubFor(head(urlEqualTo(REQUEST_PATH))
          .withScheme("http")
          .willReturn(aResponse()
              .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
              .withStatus(400)));

    // When GET returns 400 and HEAD also returns 400, TigValidation1Dot6 should handle it gracefully
    // This test verifies that TigValidation1Dot6 doesn't add an error when both GET and HEAD return the same 4xx status
    TigValidation1Dot6 validation = new TigValidation1Dot6(400, queryContext);
    assertThat(validation.validate()).isTrue();
  }

  @Test
  public void testValidate_GetReturns400HeadReturns200_AddResults20300() throws Exception {
    givenUri("http");
      stubFor(head(urlEqualTo(REQUEST_PATH))
          .withScheme("http")
          .willReturn(aResponse()
              .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
              .withStatus(200)));

      // When GET returns 400 but HEAD returns 200, this indicates a server implementation issue
      TigValidation1Dot6 validation = new TigValidation1Dot6(400, queryContext);
      assertThat(validation.validate()).isFalse();
      
      // Should record the error -20300
      ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor.forClass(RDAPValidationResult.class);
      verify(results).add(resultCaptor.capture());
      RDAPValidationResult result = resultCaptor.getValue();
      assertThat(result).hasFieldOrPropertyWithValue("code", -20300)
          .hasFieldOrPropertyWithValue("value", 400 + "\n/\n" + 200)
          .hasFieldOrPropertyWithValue("message", 
              "The HTTP Status code obtained when using the HEAD method is different from the "
              + "GET method. See section 1.6 of the RDAP_Technical_Implementation_Guide_2_1.");
  }
}