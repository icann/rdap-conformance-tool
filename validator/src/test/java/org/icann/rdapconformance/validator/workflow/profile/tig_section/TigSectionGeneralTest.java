package org.icann.rdapconformance.validator.workflow.profile.tig_section;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Optional;
import org.icann.rdapconformance.validator.workflow.rdap.HttpTestingUtils;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

public class TigSectionGeneralTest extends HttpTestingUtils {


  @Test
  public void testValidate_UriNotHttps_AddResult20100() {
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);
    ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor
        .forClass(RDAPValidationResult.class);
    HttpResponse<String> httpsResponse = mock(HttpResponse.class);

    doReturn(URI.create("http://domain/test.example")).when(config).getUri();
    doReturn(config.getUri()).when(httpsResponse).uri();

    assertThat(TigSectionGeneral.validate(httpsResponse, config, results)).isFalse();
    verify(results).add(resultCaptor.capture());
    RDAPValidationResult result = resultCaptor.getValue();
    assertThat(result).hasFieldOrPropertyWithValue("code", -20100)
        .hasFieldOrPropertyWithValue("value", config.getUri().toString())
        .hasFieldOrPropertyWithValue("message", "The URL is HTTP, per section 1.2 of "
            + "the RDAP_Technical_Implementation_Guide_2_1 shall be HTTPS only.");
  }

  @Test
  public void testValidate_UriNotHttpsInOneRedirect_AddResult20100() {
    String path1 = "https://domain/test1.example";
    String path2 = "https://domain/test2.example";
    String path3 = "http://domain/test3.example";
    HttpResponse<String> httpsResponse1 = mock(HttpResponse.class);
    HttpResponse<String> httpsResponse2 = mock(HttpResponse.class);
    HttpResponse<String> httpsResponse3 = mock(HttpResponse.class);
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);
    ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor
        .forClass(RDAPValidationResult.class);

    // set URI as being an HTTP request to avoid going through HTTP test for coce -20101
    doReturn(URI.create("http://domain/test.example")).when(config).getUri();
    // prepare chained HTTP response with one HTTP redirect
    doReturn(URI.create(path1)).when(httpsResponse1).uri();
    doReturn(URI.create(path2)).when(httpsResponse2).uri();
    doReturn(URI.create(path3)).when(httpsResponse3).uri();
    doReturn(Optional.of(httpsResponse2)).when(httpsResponse1).previousResponse();
    doReturn(Optional.of(httpsResponse3)).when(httpsResponse2).previousResponse();

    assertThat(TigSectionGeneral.validate(httpsResponse1, config, results)).isFalse();
    verify(results).add(resultCaptor.capture());
    RDAPValidationResult result = resultCaptor.getValue();
    assertThat(result).hasFieldOrPropertyWithValue("code", -20100)
        .hasFieldOrPropertyWithValue("value", path3)
        .hasFieldOrPropertyWithValue("message", "The URL is HTTP, per section 1.2 of "
            + "the RDAP_Technical_Implementation_Guide_2_1 shall be HTTPS only.");
  }

  @Test
  public void testValidate_HttpResponseEqualsHttpsResponse_AddResult20101() {
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);
    ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor
        .forClass(RDAPValidationResult.class);
    HttpResponse<String> httpsResponse = mock(HttpResponse.class);

    WireMockConfiguration wmConfig = wireMockConfig()
        .dynamicHttpsPort()
        .bindAddress(WIREMOCK_HOST);
    prepareWiremock(wmConfig);

    // configure wiremock for HTTP as we will make an HTTP request
    givenUri("http");
    doReturn(RDAP_RESPONSE).when(httpsResponse).body();
    // replace configuration URI by https because we need to enter the tests for an HTTPS uri
    doReturn(URI.create(config.getUri().toString().replace("http", "https"))).when(config).getUri();
    doReturn(config.getUri()).when(httpsResponse).uri();
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(RDAP_RESPONSE)));

    assertThat(TigSectionGeneral.validate(httpsResponse, config, results)).isFalse();
    verify(results).add(resultCaptor.capture());
    RDAPValidationResult result = resultCaptor.getValue();
    assertThat(result).hasFieldOrPropertyWithValue("code", -20101)
        .hasFieldOrPropertyWithValue("value", RDAP_RESPONSE + "\n/\n" + RDAP_RESPONSE)
        .hasFieldOrPropertyWithValue("message",
            "The RDAP response was provided over HTTP, per section 1.2 of the "
                + "RDAP_Technical_Implementation_Guide_2_1shall be HTTPS only.");
  }
}