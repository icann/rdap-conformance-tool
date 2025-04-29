package org.icann.rdapconformance.validator.workflow.rdap.http;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.temporaryRedirect;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icann.rdapconformance.validator.CommonUtils.HTTP;
import static org.icann.rdapconformance.validator.CommonUtils.PAUSE;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.net.http.HttpResponse;
import org.icann.rdapconformance.validator.ConnectionStatus;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.net.ConnectException;
import java.net.http.HttpTimeoutException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.util.Optional;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.HttpTestingUtils;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;

public class RDAPHttpQueryTest extends HttpTestingUtils {
  public static final String HTTPS_LOCALHOST = "https://127.0.0.1:";
  public static final String HTTP_TEST_EXAMPLE = "http://test.example";
  public static final String LOCAL_8080 = "http://127.0.0.1:8080";
  public static final int TIMEOUT_SECONDS = 10;
  public static final int REDIRECT = 302;
  public static final String LOCATION = "Location";
  private RDAPHttpQuery rdapHttpQuery;

  @DataProvider(name = "fault")
  public static Object[][] serverFault() {
    return new Object[][]{{Fault.EMPTY_RESPONSE},
        {Fault.RANDOM_DATA_THEN_CLOSE},
        {Fault.MALFORMED_RESPONSE_CHUNK},
        {Fault.CONNECTION_RESET_BY_PEER}};
  }

  @DataProvider(name = "tlsErrors")
  public static Object[][] serverTlsErrors() {
    // TODO the following data rely on web resources that may change without notice, should create our own certificates, CRL, etc.
    // Note: we have our own certs now, the only issue is the revoked cert. This is kept in case we need to rollback to the original test cases.
    return new Object[][]{
        {"https://expired.badssl.com", ConnectionStatus.EXPIRED_CERTIFICATE},
        {"https://revoked.badssl.com", ConnectionStatus.REVOKED_CERTIFICATE},
        {"https://wrong.host.badssl.com", ConnectionStatus.INVALID_CERTIFICATE},
        {"https://untrusted-root.badssl.com", ConnectionStatus.HANDSHAKE_FAILED}
        };
  }

  @BeforeMethod
  public void setUp(Method method) {
    super.setUp();
    WireMockConfiguration wmConfig = wireMockConfig()
        .dynamicHttpsPort()
        .bindAddress(WIREMOCK_HOST);
    if (method.getName().contains("LocalTrustStore")) {
      setHttpsTrustStore(wmConfig);
    }
    prepareWiremock(wmConfig);

    rdapHttpQuery = new RDAPHttpQuery(config);
  }

  @Test
  @Ignore("System properties are not taken into account when launched among other tests, works as a standalone test though")
  public void test_WithHttps_LocalTrustStore() {
    givenUri("https");
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withPort(wireMockServer.httpsPort())
        .withScheme("https")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(RDAP_RESPONSE)));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.getData()).isEqualTo(RDAP_RESPONSE);
    assertThat(rdapHttpQuery.getStatusCode()).isPresent().get().isEqualTo(200);
    assertThat(rdapHttpQuery.hasNameserverSearchResults()).isFalse();
  }

  @Test(dataProvider = "fault")
  @Ignore("System properties are not taken into account when launched among other tests, works as a standalone test though")
  public void test_ServerFaultWithHttps_LocalTrustStore(Fault fault) {
    givenUri("https");
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme("https")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withFault(fault)));

    assertThat(rdapHttpQuery.run()).isFalse();
    assertThat(rdapHttpQuery.getErrorStatus())
        .isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);
  }

  @Test
  public void test_WithHttp() {
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    givenUri(HTTP);
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme(HTTP)
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(RDAP_RESPONSE)));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.getData()).isEqualTo(RDAP_RESPONSE);
    assertThat(rdapHttpQuery.getStatusCode()).isPresent().get().isEqualTo(200);
    assertThat(rdapHttpQuery.hasNameserverSearchResults()).isFalse();
  }

  @Test
  public void test_WithJsonArray() {
    String path = "/nameservers?ip=.*";
    String response = "{\"nameserverSearchResults\": [ {\"objectClassName\":\"nameserver\"} ]}";
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    givenUri(HTTP, path);
    stubFor(get(urlEqualTo(path))
        .withScheme(HTTP)
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(response)));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.getData()).isEqualTo(response);
    assertThat(rdapHttpQuery.getStatusCode()).isPresent().get().isEqualTo(200);
    assertThat(rdapHttpQuery.hasNameserverSearchResults()).isTrue();
  }

  @Test(dataProvider = "fault")
  public void test_ServerFault_ReturnsErrorStatus20(Fault fault) {
    givenUri(HTTP);
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme(HTTP)
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withFault(fault)));

    assertThat(rdapHttpQuery.run()).isFalse();
    assertThat(rdapHttpQuery.getErrorStatus())
        .isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);
  }

  @Test
  public void test_NetworkSendFail_ReturnsErrorStatus19() {
    doReturn(URI.create(HTTP_TEST_EXAMPLE )).when(config).getUri();

    assertThat(rdapHttpQuery.run()).isFalse();
    assertThat(rdapHttpQuery.getErrorStatus()).isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);
  }

  @Test
  public void test_ConnectionTimeout_ReturnsErrorStatus10() {
    givenUri(HTTP);
    doReturn(1).when(config).getTimeout();
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme(HTTP)
        .willReturn(aResponse()
            .withFixedDelay(2000)
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(RDAP_RESPONSE)));

    assertThat(rdapHttpQuery.run()).isFalse();
    assertThat(rdapHttpQuery.getErrorStatus()).isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);
  }

  @Test
  public void test_ServerRedirectLessThanRetries_Returns200() {
    RDAPValidatorResults results =RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    String path1 = "/domain/test1.example";
    String path2 = "/domain/test2.example";
    String path3 = "/domain/test3.example";

    givenUri(HTTP, path1);
    stubFor(get(urlEqualTo(path1))
        .withScheme(HTTP)
        .willReturn(temporaryRedirect(path2)));
    stubFor(get(urlEqualTo(path2))
        .withScheme(HTTP)
        .willReturn(temporaryRedirect(path3)));
    stubFor(get(urlEqualTo(path3))
        .withScheme(HTTP)
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(RDAP_RESPONSE)));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.getData()).isEqualTo(RDAP_RESPONSE);
    assertThat(rdapHttpQuery.getStatusCode()).isPresent().get().isEqualTo(HTTP_OK);

    verify(exactly(1), getRequestedFor(urlEqualTo(path1)));
    verify(exactly(1), getRequestedFor(urlEqualTo(path2)));
    verify(exactly(1), getRequestedFor(urlEqualTo(path3)));

  }

  @Test
  public void test_ServerRedirectMoreThanRetries_ReturnsErrorStatus16() throws Exception {
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    String path1 = "/domain/test1.example";
    String path2 = "/domain/test2.example";
    String path3 = "/domain/test3.example";
    String path4 = "/domain/test4.example";

    URI uri1 = URI.create(LOCAL_8080 + path1);
    URI uri2 = URI.create(LOCAL_8080 + path2);
    URI uri3 = URI.create(LOCAL_8080 + path3);
    URI uri4 = URI.create(LOCAL_8080 + path4);

    // Set the initial URI and max redirects in the config mock
    doReturn(uri1).when(config).getUri();
    doReturn(2).when(config).getMaxRedirects(); // Only allow 2 redirects

    // Mock the static method
    try (MockedStatic<RDAPHttpRequest> mockedStatic = mockStatic(RDAPHttpRequest.class)) {
      // Create responses with appropriate headers for redirects
      HttpHeaders headers1 = HttpHeaders.of(Map.of(LOCATION, List.of(uri2.toString())), (k, v) -> true);
      HttpHeaders headers2 = HttpHeaders.of(Map.of(LOCATION, List.of(uri3.toString())), (k, v) -> true);
      HttpHeaders headers3 = HttpHeaders.of(Map.of(LOCATION, List.of(uri4.toString())), (k, v) -> true);

      // Mock responses
      HttpResponse<String> response1 = mock(HttpResponse.class);
      when(response1.statusCode()).thenReturn(REDIRECT);
      when(response1.body()).thenReturn("");
      when(response1.uri()).thenReturn(uri1);
      when(response1.headers()).thenReturn(headers1);

      HttpResponse<String> response2 = mock(HttpResponse.class);
      when(response2.statusCode()).thenReturn(REDIRECT);
      when(response2.body()).thenReturn("");
      when(response2.uri()).thenReturn(uri2);
      when(response2.headers()).thenReturn(headers2);

      HttpResponse<String> response3 = mock(HttpResponse.class);
      when(response3.statusCode()).thenReturn(REDIRECT);
      when(response3.body()).thenReturn("");
      when(response3.uri()).thenReturn(uri3);
      when(response3.headers()).thenReturn(headers3);

      // Setup the mock calls
      mockedStatic.when(() -> RDAPHttpRequest.makeHttpGetRequest(uri1, TIMEOUT_SECONDS))
                  .thenReturn(response1);
      mockedStatic.when(() -> RDAPHttpRequest.makeHttpGetRequest(uri2, TIMEOUT_SECONDS))
                  .thenReturn(response2);
      mockedStatic.when(() -> RDAPHttpRequest.makeHttpGetRequest(uri3, TIMEOUT_SECONDS))
                  .thenReturn(response3);

      // Run the query - it should fail due to too many redirects
      assertThat(rdapHttpQuery.run()).isFalse();
      assertThat(rdapHttpQuery.getErrorStatus())
          .isEqualTo(ConnectionStatus.TOO_MANY_REDIRECTS);

      // The redirects list should contain the first two redirects
      List<URI> redirects = rdapHttpQuery.getRedirects();
      assertThat(redirects).containsExactly(uri2, uri3);
    }
  }


  @Test
  public void testIsBlindlyCopyingParams() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    RDAPHttpQuery rdapHttpQuery = new RDAPHttpQuery(config);
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    HttpHeaders headers = mock(HttpHeaders.class);
    URI originalUri = URI.create("http://example.com?param=value");


    when(config.getUri()).thenReturn(originalUri);
    when(headers.firstValue(LOCATION)).thenReturn(Optional.of("http://example.com/redirected?param=value"));

    boolean result = rdapHttpQuery.isBlindlyCopyingParams(headers);

    assertThat(result).isTrue();
    assertThat(results.getAll()).contains(
        RDAPValidationResult.builder()
                            .code(-13004)
                            .value("<location header value>")
                            .message("Response redirect contained query parameters copied from the request.")
                            .build());
  }

  @Test
  public void testIsBlindlyCopyingParams_NotCopied() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    RDAPHttpQuery rdapHttpQuery = new RDAPHttpQuery(config);
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    HttpHeaders headers = mock(HttpHeaders.class);
    URI originalUri = URI.create("http://example.com?param=value");

    rdapHttpQuery.setResults(results);
    when(config.getUri()).thenReturn(originalUri);
    when(headers.firstValue(LOCATION)).thenReturn(Optional.of("http://example.com/redirected"));

    boolean result = rdapHttpQuery.isBlindlyCopyingParams(headers);

    assertThat(result).isFalse();
    assertThat(results.getAll()).doesNotContain(
        RDAPValidationResult.builder()
                            .code(-13004)
                            .value("<location header value>")
                            .message("Response redirect contained query parameters copied from the request.")
                            .build());
  }

  @Test
  public void testIsBlindlyCopyingParams_WithMockedResponses() throws Exception {
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    String path1 = "/domain/test1.example";
    String path2 = "/domain/test2.example";

    URI uri1 = URI.create(LOCAL_8080 + path1 + "?param=value");
    URI uri2 = URI.create(LOCAL_8080 + path2 + "?param=value");

    // Set the initial URI with query parameters
    doReturn(uri1).when(config).getUri();

    // Mock the static method
    try (MockedStatic<RDAPHttpRequest> mockedStatic = mockStatic(RDAPHttpRequest.class)) {
      // Create response with Location header that copies the query parameter
      HttpHeaders headers1 = HttpHeaders.of(Map.of(LOCATION, List.of(uri2.toString())), (k, v) -> true);

      // Mock the first response with redirect
      HttpResponse<String> response1 = mock(HttpResponse.class);
      when(response1.statusCode()).thenReturn(REDIRECT);
      when(response1.body()).thenReturn("");
      when(response1.uri()).thenReturn(uri1);
      when(response1.headers()).thenReturn(headers1);

      // Setup the mock call
      mockedStatic.when(() -> RDAPHttpRequest.makeHttpGetRequest(uri1, TIMEOUT_SECONDS))
                  .thenReturn(response1);

      // Run the query - it should fail because of blindly copied parameters
      assertThat(rdapHttpQuery.run()).isFalse();

      // The redirects list should contain the first redirect
      List<URI> redirects = rdapHttpQuery.getRedirects();
      assertThat(redirects).containsExactly(uri2);

      // Verify that the validation result contains the -13004 error code
      assertThat(results.getAll()).contains(
          RDAPValidationResult.builder()
                              .code(-13004)
                              .value("<location header value>")
                              .message("Response redirect contained query parameters copied from the request.")
                              .build());
    }
  }

  @Test
  public void test_RedirectWithoutLocationHeader_BreaksLoop() {
    String path1 = "/domain/test1.example";

    givenUri(HTTP, path1);
    stubFor(get(urlEqualTo(path1))
        .withScheme(HTTP)
        .willReturn(aResponse()
            .withStatus(REDIRECT))); // Redirect status without Location header

    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    assertThat(rdapHttpQuery.run()).isFalse();

    List<URI> redirects = rdapHttpQuery.getRedirects();

    assertThat(redirects).isEmpty(); // No redirects should be followed
  }

  @Test
  public void test_NoContentType_ErrorCode13000AddedInResults() throws Exception {
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    URI uri = URI.create(LOCAL_8080 + REQUEST_PATH);
    doReturn(uri).when(config).getUri();

    // Mock the static method
    try (MockedStatic<RDAPHttpRequest> mockedStatic = mockStatic(RDAPHttpRequest.class)) {
      // Create response with Content-Type header
      HttpHeaders headers = HttpHeaders.of(
          Map.of("Content-Type", List.of("application/json;encoding=UTF-8")),
          (k, v) -> true
      );

      // Mock the HTTP response
      HttpResponse<String> response = mock(HttpResponse.class);
      when(response.statusCode()).thenReturn(200);
      when(response.body()).thenReturn(RDAP_RESPONSE);
      when(response.uri()).thenReturn(uri);
      when(response.headers()).thenReturn(headers);

      // Set up the mock call
      mockedStatic.when(() -> RDAPHttpRequest.makeHttpGetRequest(uri, TIMEOUT_SECONDS))
                  .thenReturn(response);

      // Run the query and verify
      assertThat(rdapHttpQuery.run()).isTrue();
      assertThat(results.getAll()).contains(
          RDAPValidationResult.builder()
                              .code(-13000)
                              .value("application/json;encoding=UTF-8")
                              .message("The content-type header does not contain the application/rdap+json media type.")
                              .build());
    }
  }

  @Test
  public void test_InvalidJson_ErrorCode13001AddedInResults() {
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);
    String response = "{\"objectClassName\"}";

    givenUri(HTTP);
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme(HTTP)
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(response)));

    assertThat(rdapHttpQuery.run()).isFalse();
    assertThat(results.getAll()).contains(
            RDAPValidationResult.builder()
                    .code(-13001)
                    .value("response body not given")
                    .message("The response was not valid JSON.")
                    .build());
  }

  @Test
  public void test_InvalidHttpStatus_ErrorCode13002AddedInResults() {
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    String path = "/nameservers?ip=.*";
    String response = "{\"nameserverSearchResults\": [ {\"objectClassName\":\"nameserver\"} ]}";
    givenUri(HTTP);
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme(HTTP)
        .willReturn(aResponse().withStatus(403)
                               .withBody(response)));

    assertThat(rdapHttpQuery.run()).isFalse();
    assertThat(results.getAll()).contains(
        RDAPValidationResult.builder()
            .code(-13002)
            .value("403")
            .message("The HTTP status code was neither 200 nor 404.")
            .build());
  }

  // Note: we no longer do this, we now host our own
  @Ignore
  @Test(dataProvider = "tlsErrors")
  public void test_WithHttpsCertificateError_ReturnsAppropriateErrorStatus(String url,
      ConnectionStatus expectedStatus) {
    doReturn(URI.create(url)).when(config).getUri();

    assertThat(rdapHttpQuery.run()).isFalse();
    assertThat(rdapHttpQuery.getErrorStatus())
        .isEqualTo(expectedStatus);
  }

  @Test
  public void checkWithQueryType_StatusNot200_IsOk() {
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    givenUri(HTTP);
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme(HTTP)
        .willReturn(notFound()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody("{}")));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.checkWithQueryType(RDAPQueryType.DOMAIN)).isTrue();
  }

  @Test
  public void checkWithQueryType_ObjectClassNameInJsonResponse_IsOk() {
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    givenUri(HTTP);
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme(HTTP)
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(RDAP_RESPONSE)));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.checkWithQueryType(RDAPQueryType.DOMAIN)).isTrue();

  }

  @Test
  public void checkWithQueryType_NoObjectClassNameInJsonResponse_ReturnsErrorCode13003InResults() {
    givenUri(HTTP);
    String response = "{\"NoObjectClassName\": \"domain\"}";
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme(HTTP)
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(response)));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.checkWithQueryType(RDAPQueryType.DOMAIN)).isTrue();
    assertThat(results.getAll()).contains(
            RDAPValidationResult.builder()
                    .code(-13003)
                    .value(response)
                    .message("The response does not have an objectClassName string.")
                    .build());
  }

  @Test
  public void checkWithQueryType_JsonResponseIsAnArray_IsOk() {
    String path = "/nameservers?ip=.*";
    String response = "{\"nameserverSearchResults\": [ {\"objectClassName\":\"nameserver\"} ]}";
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    givenUri(HTTP, path);
    stubFor(get(urlEqualTo(path))
        .withScheme(HTTP)
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(response)));

    // XXX
    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.checkWithQueryType(RDAPQueryType.NAMESERVERS)).isTrue();
  }

  @Test
  public void checkWithQueryType_JsonResponseIsNotAnArray_ReturnsErrorCode13003InResults() {
    String path = "/nameservers?ip=.*";
    String response = "{\"nameserverSearchResults\": { \"objectClassName\":\"nameserver\" }}";
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    givenUri(HTTP, path);
    stubFor(get(urlEqualTo(path))
        .withScheme(HTTP)
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(response)));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.checkWithQueryType(RDAPQueryType.NAMESERVERS)).isTrue();
    assertThat(results.getAll()).contains(
            RDAPValidationResult.builder()
                    .code(-13003)
                    .value(response)
                    .message("The response does not have an objectClassName string.")
                    .build());
  }

  @Test
  public void checkWithQueryType_JsonResponseIsNotAnArray_ReturnsErrorCode12610InResults() {
    String path = "/nameservers?ip=.*";
    String response = "{\"nameserverSearchResults\": { \"objectClassName\":\"nameserver\" }}";
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    // enable 2024 profile
    doReturn(true).when(config).useRdapProfileFeb2024();

    givenUri(HTTP, path);
    stubFor(get(urlEqualTo(path))
        .withScheme(HTTP)
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(response)));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.checkWithQueryType(RDAPQueryType.NAMESERVERS)).isTrue();
    assertThat(results.getAll()).contains(
        RDAPValidationResult.builder()
            .code(-12610)
            .value(response)
            .message("The nameserverSearchResults structure is required.")
            .build());

    // disable 2024 profile after testing
    doReturn(false).when(config).useRdapProfileFeb2024();
  }

  @Test
  public void jsonResponseValid_ReturnsFalseUsingNoObjectClassName() {
    String path = "/nameservers?ip=.*";
    String response = "{\"nameserverSearchResults\": { \"objectClassName\":\"nameserver\" }}";


    givenUri(HTTP, path);
    stubFor(get(urlEqualTo(path))
            .withScheme(HTTP)
            .willReturn(aResponse()
                    .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
                    .withBody(response)));

    assertThat(rdapHttpQuery.jsonResponseValid()).isFalse();
  }

  @Test
  public void jsonResponseValid_TopLevelObjectClassNameMissing_ReturnsFalse() {
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    String response = "{\"entities\": [{\"objectClassName\": \"entity\"}]}";
    givenUri(HTTP);
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme(HTTP)
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(response)));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.jsonResponseValid()).isFalse();
  }

  @Test
  public void jsonResponseValid_EntitiesListInvalid_ReturnsFalse() {
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    String response = "{\"objectClassName\": \"domain\", \"entities\": [\"invalidElement\"]}";
    givenUri(HTTP);
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme(HTTP)
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(response)));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.jsonResponseValid()).isFalse();
  }

  @Test
  public void jsonResponseValid_NameserversListInvalid_ReturnsFalse() {
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();

    rdapHttpQuery.setResults(results);

    String response = "{\"objectClassName\": \"domain\", \"nameservers\": [\"invalidElement\"]}";
    givenUri(HTTP);
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme(HTTP)
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(response)));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.jsonResponseValid()).isFalse();
  }

  @Test
  public void jsonResponseValid_ValidTopLevelAndNestedEntities_ReturnsTrue() {
    RDAPValidatorResults results =RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    String response = "{\"objectClassName\": \"domain\", \"entities\": [{\"objectClassName\": \"entity\"}]}";
    givenUri(HTTP);
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme(HTTP)
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(response)));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.jsonResponseValid()).isTrue();
  }

  @Test
  public void jsonResponseValid_ValidTopLevelAndNestedNameservers_ReturnsTrue() {
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    String response = "{\"objectClassName\": \"domain\", \"nameservers\": [{\"objectClassName\": \"nameserver\"}]}";
    givenUri(HTTP);
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme(HTTP)
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(response)));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.jsonResponseValid()).isTrue();
  }

  @Test
  public void testGetRedirects_NoRedirects() {
    doReturn(URI.create("http://example.com")).when(config).getUri();
    stubFor(get(urlEqualTo("/"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody("{}")));
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    rdapHttpQuery.run();

    List<URI> redirects = rdapHttpQuery.getRedirects();
    assertThat(redirects).isEmpty();
  }

  @Test
  public void testGetRedirects_WithRedirects() throws Exception {
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    String path1 = "/domain/test1.example";
    String path2 = "/domain/test2.example";
    String path3 = "/domain/test3.example";

    URI uri1 = URI.create(LOCAL_8080 + path1);
    URI uri2 = URI.create(LOCAL_8080 + path2);
    URI uri3 = URI.create(LOCAL_8080 + path3);

    // Set the initial URI in the config mock
    doReturn(uri1).when(config).getUri();

    // Mock the static method
    try (MockedStatic<RDAPHttpRequest> mockedStatic = mockStatic(RDAPHttpRequest.class)) {
      // Create responses with appropriate headers for redirects
      HttpHeaders headers1 = HttpHeaders.of(Map.of(LOCATION, List.of(uri2.toString())), (k, v) -> true);
      HttpHeaders headers2 = HttpHeaders.of(Map.of(LOCATION, List.of(uri3.toString())), (k, v) -> true);
      HttpHeaders headers3 = HttpHeaders.of(Map.of("Content-Type", List.of("application/rdap+JSON")), (k, v) -> true);

      // Create response objects with custom headers
      HttpResponse<String> response1 = mock(HttpResponse.class);
      when(response1.statusCode()).thenReturn(REDIRECT);
      when(response1.body()).thenReturn("");
      when(response1.uri()).thenReturn(uri1);
      when(response1.headers()).thenReturn(headers1);

      HttpResponse<String> response2 = mock(HttpResponse.class);
      when(response2.statusCode()).thenReturn(REDIRECT);
      when(response2.body()).thenReturn("");
      when(response2.uri()).thenReturn(uri2);
      when(response2.headers()).thenReturn(headers2);
      when(response2.previousResponse()).thenReturn(Optional.empty());

      HttpResponse<String> response3 = mock(HttpResponse.class);
      when(response3.statusCode()).thenReturn(200);
      when(response3.body()).thenReturn(RDAP_RESPONSE);
      when(response3.uri()).thenReturn(uri3);
      when(response3.headers()).thenReturn(headers3);
      when(response3.previousResponse()).thenReturn(Optional.empty());

      // Setup the mock calls
      mockedStatic.when(() -> RDAPHttpRequest.makeHttpGetRequest(uri1, TIMEOUT_SECONDS))
                  .thenReturn(response1);
      mockedStatic.when(() -> RDAPHttpRequest.makeHttpGetRequest(uri2, TIMEOUT_SECONDS))
                  .thenReturn(response2);
      mockedStatic.when(() -> RDAPHttpRequest.makeHttpGetRequest(uri3, TIMEOUT_SECONDS))
                  .thenReturn(response3);

      // Run the query
      assertThat(rdapHttpQuery.run()).isTrue();
      assertThat(rdapHttpQuery.getData()).isEqualTo(RDAP_RESPONSE);
      assertThat(rdapHttpQuery.getStatusCode()).isPresent().get().isEqualTo(200);

      // Validate the redirects
      List<URI> redirects = rdapHttpQuery.getRedirects();
      assertThat(redirects).containsExactly(uri2, uri3);
    }
  }


  @Test
  public void test_HandleRequestException_ConnectionFailed() throws IOException, InterruptedException {
    doReturn(URI.create(HTTP_TEST_EXAMPLE)).when(config).getUri();
    doReturn(PAUSE).when(config).getTimeout();

    RDAPHttpQuery query = new RDAPHttpQuery(config);

    // Mock the static method
    try (MockedStatic<RDAPHttpRequest> mockedStatic = mockStatic(RDAPHttpRequest.class)) {
      // Simulate a ConnectException
      mockedStatic.when(() -> RDAPHttpRequest.makeHttpGetRequest(URI.create(HTTP_TEST_EXAMPLE), PAUSE))
                  .thenThrow(new ConnectException("Connection failed"));
      query.makeRequest(URI.create(HTTP_TEST_EXAMPLE));
      assertThat(query.getErrorStatus()).isEqualTo(ConnectionStatus.CONNECTION_FAILED);
    }
  }

  @Test
  public void test_HandleRequestException_Timeout() throws IOException, InterruptedException {
    doReturn(URI.create(HTTP_TEST_EXAMPLE)).when(config).getUri();
    doReturn(1).when(config).getTimeout();

    RDAPHttpQuery query = new RDAPHttpQuery(config);

    try (MockedStatic<RDAPHttpRequest> mockedStatic = mockStatic(RDAPHttpRequest.class)) {
      // Simulate a HttpTimeoutException
      mockedStatic.when(() -> RDAPHttpRequest.makeHttpGetRequest(URI.create(HTTP_TEST_EXAMPLE), 1))
                  .thenThrow(new HttpTimeoutException("Timeout"));

      query.makeRequest(URI.create(HTTP_TEST_EXAMPLE));
      assertThat(query.getErrorStatus()).isEqualTo(ConnectionStatus.CONNECTION_FAILED);
    }
  }

  @Test
  public void test_AnalyzeIOException_ExpiredCertificate() throws IOException, InterruptedException {
    doReturn(URI.create(HTTP_TEST_EXAMPLE)).when(config).getUri();
    doReturn(1).when(config).getTimeout();

    RDAPHttpQuery query = new RDAPHttpQuery(config);

    try (MockedStatic<RDAPHttpRequest> mockedStatic = mockStatic(RDAPHttpRequest.class)) {
      // Simulate a CertificateExpiredException
      mockedStatic.when(() -> RDAPHttpRequest.makeHttpGetRequest(URI.create(HTTP_TEST_EXAMPLE), 1))
                  .thenThrow(new IOException(new java.security.cert.CertificateExpiredException("Expired certificate")));

      query.makeRequest(URI.create(HTTP_TEST_EXAMPLE));
      assertThat(query.getErrorStatus()).isEqualTo(ConnectionStatus.EXPIRED_CERTIFICATE);
    }
  }

  // XXX We need to come back and figure out why this one doesn't work - throws a null pointer exception
  @Ignore
  @Test
  public void test_AnalyzeIOException_RevokedCertificate() throws IOException, InterruptedException {
    doReturn(URI.create(HTTP_TEST_EXAMPLE)).when(config).getUri();
    doReturn(1).when(config).getTimeout();
    RDAPHttpQuery query = new RDAPHttpQuery(config);

    // Initialize the results field
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    query.setResults(results);

    try (MockedStatic<RDAPHttpRequest> mockedStatic = mockStatic(RDAPHttpRequest.class)) {
      // Simulate a CertificateRevokedException with non-null parameters
      mockedStatic.when(() -> RDAPHttpRequest.makeHttpGetRequest(URI.create(HTTP_TEST_EXAMPLE), 1))
                  .thenThrow(new IOException(new java.security.cert.CertificateRevokedException(
                      new Date(), null, null, Map.of())));

      query.makeRequest(URI.create(HTTP_TEST_EXAMPLE));
      assertThat(query.getErrorStatus()).isEqualTo(ConnectionStatus.REVOKED_CERTIFICATE);
    }
  }

//  @Ignore
//  @Test
//  public void test_WithLocalHttpsCertificateError_ReturnsAppropriateErrorStatus() throws Exception {
//    // 🔥 Disable OCSP checking for test certs
//    System.setProperty("com.sun.net.ssl.checkRevocation", "false");
//
//    KeyStore trustStore = KeyStore.getInstance("JKS");
//    try (InputStream is = getClass().getClassLoader().getResourceAsStream("keystores/truststore.jks")) {
//      trustStore.load(is, "password".toCharArray());
//    }
//
//    // this stuff is essential
//    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//    tmf.init(trustStore);
//
//    // Again, painful, but we have to set this just to get the errors
//    SSLContext sslContext = SSLContext.getInstance("TLS");
//    sslContext.init(null, tmf.getTrustManagers(), null);
//    SSLContext.setDefault(sslContext);
//    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
//
//    // Start HTTPS servers with different certificates
//    MultiCertHttpsTestServer.startHttpsServer(EXPIRED_CERT_PORT, EXPIRED);
//    MultiCertHttpsTestServer.startHttpsServer(INVALID_CERT_PORT, INVALID_HOST);
//    MultiCertHttpsTestServer.startHttpsServer(UNTRUSTED_ROOT_CERT_PORT, UNTRUSTED);
//    // we need to sleep before we can start testing it
//    Thread.sleep(PAUSE);
//
//    try {
//      RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
//      results.clear();
//      rdapHttpQuery.setResults(results);
//
//      // Test expired certificate
//      doReturn(URI.create(HTTPS_LOCALHOST + EXPIRED_CERT_PORT)).when(config).getUri();
//      assertThat(rdapHttpQuery.run()).isFalse();
//      ConnectionStatus errorStatus = rdapHttpQuery.getErrorStatus();
//      assertThat(errorStatus).isEqualTo(RDAPValidationStatus.EXPIRED_CERTIFICATE);
//
//      // Test invalid host certificate
//      doReturn(URI.create(HTTPS_LOCALHOST + INVALID_CERT_PORT)).when(config).getUri();
//      assertThat(rdapHttpQuery.run()).isFalse();
//      assertThat(rdapHttpQuery.getErrorStatus()).isEqualTo(RDAPValidationStatus.INVALID_CERTIFICATE);
//
//      // Test untrusted certificate
//      doReturn(URI.create(HTTPS_LOCALHOST + UNTRUSTED_ROOT_CERT_PORT)).when(config).getUri();
//      assertThat(rdapHttpQuery.run()).isFalse();
//      assertThat(rdapHttpQuery.getErrorStatus()).isEqualTo(RDAPValidationStatus.HANDSHAKE_FAILED);
//
//      // Revoked
//      // Note: we can't do this b/c of how we are hosting our own revoked certs, and we modified the trust manager to do that.
//      // doReturn(URI.create("https://revoked.badssl.com")).when(config).getUri();
//      //  assertThat(rdapHttpQuery.run()).isFalse();
//      //  assertThat(rdapHttpQuery.getErrorStatus()).isEqualTo(RDAPValidationStatus.REVOKED_CERTIFICATE);
//
//    } finally {
//      // Stop all HTTPS servers
//      MultiCertHttpsTestServer.stopAll();
//    }
//  }

  @Test
  public void testValidateIfContainsErrorCode_HttpStatus200_ReturnsTrue() {
    String response = "{\"errorCode\":404,\"rdapConformance\":[\"rdap_level_0\"]}";
    assertThat(rdapHttpQuery.validateIfContainsErrorCode(200, response)).isTrue();
  }

  @Test
  public void testValidateIfContainsErrorCode_NullResponse_ReturnsFalse() {
    assertThat(rdapHttpQuery.validateIfContainsErrorCode(404, null)).isFalse();
  }

  @Test
  public void testValidateIfContainsErrorCode_BlankResponse_ReturnsFalse() {
    assertThat(rdapHttpQuery.validateIfContainsErrorCode(404, " ")).isFalse();
  }

  @Test
  public void testValidateIfContainsErrorCode_ValidJsonWithoutRequiredKeys_ReturnsFalse() {
    String response = "{\"lang\":\"en-US\",\"title\":\"Error processing request\"}";
    assertThat(rdapHttpQuery.validateIfContainsErrorCode(404, response)).isFalse();
  }

  @Test
  public void testValidateIfContainsErrorCode_ValidJsonWithRequiredKeys_ReturnsTrue() {
    String response = "{\"errorCode\":404,\"rdapConformance\":[\"rdap_level_0\"]}";
    assertThat(rdapHttpQuery.validateIfContainsErrorCode(404, response)).isTrue();
  }

  @Test
  public void testValidateIfContainsErrorCode_InvalidJson_ReturnsFalse() {
    String response = "{\"errorCode\":404,\"rdapConformance\":";
    assertThat(rdapHttpQuery.validateIfContainsErrorCode(404, response)).isFalse();
  }

  @Test
  public void testValidateIfContainsErrorCode500_InvalidJson_ReturnsFalse() {
    String response = "{\"errorCode\":500,\"rdapConformance\":";
    assertThat(rdapHttpQuery.validateIfContainsErrorCode(500, response)).isFalse();
  }



  // More code coverage for verifyIfObjectClassPropExits
  @Test
  public void testVerifyIfObjectClassPropExits_EmptyList_ReturnsTrue() {
    List<?> propertyCollection = List.of();
    assertThat(rdapHttpQuery.verifyIfObjectClassPropExits(propertyCollection, "entities")).isTrue();
  }

  @Test
  public void testVerifyIfObjectClassPropExits_ValidObjects_ReturnsTrue() {
    List<Map<String, Object>> propertyCollection = List.of(
        Map.of("objectClassName", "domain", "entities", List.of(Map.of("objectClassName", "entity"))),
        Map.of("objectClassName", "nameserver")
    );
    assertThat(rdapHttpQuery.verifyIfObjectClassPropExits(propertyCollection, "entities")).isTrue();
  }

  @Test
  public void testVerifyIfObjectClassPropExits_MissingObjectClassName_ReturnsFalse() {
    List<Map<String, Object>> propertyCollection = List.of(
        Map.of("entities", List.of(Map.of("objectClassName", "entity"))),
        Map.of("objectClassName", "nameserver")
    );
    assertThat(rdapHttpQuery.verifyIfObjectClassPropExits(propertyCollection, "entities")).isFalse();
  }

  @Test
  public void testVerifyIfObjectClassPropExits_InvalidNestedStructure_ReturnsFalse() {
    List<Map<String, Object>> propertyCollection = List.of(
        Map.of("objectClassName", "domain", "entities", List.of(Map.of("invalidKey", "value"))),
        Map.of("objectClassName", "nameserver")
    );
    assertThat(rdapHttpQuery.verifyIfObjectClassPropExits(propertyCollection, "entities")).isFalse();
  }

  @Test
  public void testVerifyIfObjectClassPropExits_NonMapElement_ReturnsFalse() {
    List<?> propertyCollection = List.of(
        Map.of("objectClassName", "domain"),
        "invalidElement"
    );
    assertThat(rdapHttpQuery.verifyIfObjectClassPropExits(propertyCollection, "entities")).isFalse();
  }
}