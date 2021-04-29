package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.HttpTestingUtils;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

public class Validation1Dot6Test extends HttpTestingUtils {

  @Test
  public void testValidate_HttpHeadStatusSameAsGet_IsOk() {
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);

    WireMockConfiguration wmConfig = wireMockConfig()
        .dynamicHttpsPort()
        .bindAddress(WIREMOCK_HOST);
    prepareWiremock(wmConfig);

    // configure wiremock for HTTP as we will make an HTTP request
    givenUri("http");
    stubFor(head(urlEqualTo(REQUEST_PATH))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")));

    assertThat(Validation1Dot6.validate(200, config, results)).isTrue();
    verify(results).addGroup("tigSection_1_6_Validation", false);
    verifyNoMoreInteractions(results);
  }

  @Test
  public void testValidate_HttpHeadStatusDifferentThanGet_AddResults20300() {
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);
    ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor
        .forClass(RDAPValidationResult.class);

    WireMockConfiguration wmConfig = wireMockConfig()
        .dynamicHttpsPort()
        .bindAddress(WIREMOCK_HOST);
    prepareWiremock(wmConfig);

    // configure wiremock for HTTP as we will make an HTTP request
    givenUri("http");
    stubFor(head(urlEqualTo(REQUEST_PATH))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withStatus(404)));

    assertThat(Validation1Dot6.validate(200, config, results)).isFalse();
    verify(results).add(resultCaptor.capture());
    RDAPValidationResult result = resultCaptor.getValue();
    assertThat(result).hasFieldOrPropertyWithValue("code", -20300)
        .hasFieldOrPropertyWithValue("value", 200 + "\n/\n" + 404)
        .hasFieldOrPropertyWithValue("message",
            "The HTTP Status code obtained when using the HEAD method is different from the "
                + "GET method. See section 1.6 of the RDAP_Technical_Implementation_Guide_2_1.");
    verify(results).addGroup("tigSection_1_6_Validation", true);
  }
}