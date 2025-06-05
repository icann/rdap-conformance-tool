package org.icann.rdapconformance.validator.workflow.rdap.http;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.temporaryRedirect;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.util.Optional;

import static org.icann.rdapconformance.validator.CommonUtils.HTTP;
import static org.icann.rdapconformance.validator.CommonUtils.PAUSE;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import org.icann.rdapconformance.validator.ConnectionStatus;
import org.icann.rdapconformance.validator.DNSCacheResolver;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.HttpTestingUtils;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;

public class RDAPHttpQueryTest extends HttpTestingUtils {
  public static final String HTTP_TEST_EXAMPLE = "http://test.example";
  public static final String LOCAL_8080 = "http://127.0.0.1:8080";
  public static final int REDIRECT = 302;
  public static final String LOCATION = "Location";
  public static final String NAMESERVERS = "/nameservers";
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


  // disable until this is understood
  @Ignore
  @Test(dataProvider = "fault")
  public void test_ServerFault_ReturnsErrorStatus20(Fault fault) {
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    givenUri(HTTP);
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme(HTTP)
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withFault(fault)));

    assertThat(rdapHttpQuery.run()).isFalse();

    // Different fault types should map to different connection statuses
    ConnectionStatus expectedStatus;
    RDAPValidationResult expectedResult;

    switch (fault) {
      case EMPTY_RESPONSE:
      case RANDOM_DATA_THEN_CLOSE:
      case MALFORMED_RESPONSE_CHUNK:
        expectedStatus = ConnectionStatus.CONNECTION_FAILED;
        expectedResult = RDAPValidationResult.builder()
                                             .code(-13007)
                                             .httpStatusCode(ZERO)
                                             .value("no response available")
                                             .message("Failed to connect to server.")
                                             .build();
        break;
      case CONNECTION_RESET_BY_PEER:
        expectedStatus = ConnectionStatus.NETWORK_RECEIVE_FAIL;
        expectedResult = RDAPValidationResult.builder()
                                             .code(-13017)
                                             .httpStatusCode(ZERO)
                                             .value("no response available")
                                             .message("Network receive fail")
                                             .build();
        break;
      default:
        expectedStatus = ConnectionStatus.NETWORK_RECEIVE_FAIL;
        expectedResult = RDAPValidationResult.builder()
                                             .code(-13017)
                                             .httpStatusCode(ZERO)
                                             .value("no response available")
                                             .message("Network receive fail")
                                             .build();
    }

    assertThat(rdapHttpQuery.getErrorStatus()).isEqualTo(expectedStatus);
    assertThat(results.getAll()).contains(expectedResult);
  }

  @Test
  public void test_ConnectionTimeout_ReturnsErrorStatus10() {
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

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
    assertThat(results.getAll()).contains(
        RDAPValidationResult.builder()
                            .code(-13017)
                            .httpStatusCode(ZERO)
                            .value("no response available")
                            .message("Network receive fail")
                            .build());
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

    doReturn(uri1).when(config).getUri();
    doReturn(2).when(config).getMaxRedirects(); // Only allow 2 redirects

    try (MockedStatic<RDAPHttpRequest> mockedStatic = mockStatic(RDAPHttpRequest.class)) {
      RDAPHttpRequest.SimpleHttpResponse response1 = mock(RDAPHttpRequest.SimpleHttpResponse.class);
      when(response1.statusCode()).thenReturn(REDIRECT);
      when(response1.body()).thenReturn("");
      when(response1.uri()).thenReturn(uri1);
      when(response1.headers()).thenReturn(HttpHeaders.of(Map.of(LOCATION, List.of(uri2.toString())), (k, v) -> true));
      when(response1.getConnectionStatusCode()).thenReturn(ConnectionStatus.SUCCESS);

      RDAPHttpRequest.SimpleHttpResponse response2 = mock(RDAPHttpRequest.SimpleHttpResponse.class);
      when(response2.statusCode()).thenReturn(REDIRECT);
      when(response2.body()).thenReturn("");
      when(response2.uri()).thenReturn(uri2);
      when(response2.headers()).thenReturn(HttpHeaders.of(Map.of(LOCATION, List.of(uri3.toString())), (k, v) -> true));
      when(response2.getConnectionStatusCode()).thenReturn(ConnectionStatus.SUCCESS);

      RDAPHttpRequest.SimpleHttpResponse response3 = mock(RDAPHttpRequest.SimpleHttpResponse.class);
      when(response3.statusCode()).thenReturn(REDIRECT);
      when(response3.body()).thenReturn("");
      when(response3.uri()).thenReturn(uri3);
      when(response3.headers()).thenReturn(HttpHeaders.of(Map.of(LOCATION, List.of(uri4.toString())), (k, v) -> true));
      when(response3.getConnectionStatusCode()).thenReturn(ConnectionStatus.SUCCESS);

      mockedStatic.when(() -> RDAPHttpRequest.makeRequest(eq(uri1), anyInt(), eq("GET"), eq(true)))
                  .thenReturn(response1);
      mockedStatic.when(() -> RDAPHttpRequest.makeRequest(eq(uri2), anyInt(), eq("GET"), eq(true)))
                  .thenReturn(response2);
      mockedStatic.when(() -> RDAPHttpRequest.makeRequest(eq(uri3), anyInt(), eq("GET"), eq(true)))
                  .thenReturn(response3);


      boolean result = rdapHttpQuery.run();

      assertThat(rdapHttpQuery.getErrorStatus()).isEqualTo(ConnectionStatus.TOO_MANY_REDIRECTS);
      assertThat(result).isFalse();
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

    doReturn(uri1).when(config).getUri();

    RDAPHttpRequest.SimpleHttpResponse response1 = mock(RDAPHttpRequest.SimpleHttpResponse.class);

    HttpHeaders headers1 = mock(HttpHeaders.class);
    when(headers1.firstValue(LOCATION)).thenReturn(Optional.of(uri2.toString()));

    when(response1.statusCode()).thenReturn(REDIRECT);
    when(response1.headers()).thenReturn(headers1);
    when(response1.body()).thenReturn("{}");
    when(response1.getConnectionStatusCode()).thenReturn(ConnectionStatus.SUCCESS);

    try (MockedStatic<RDAPHttpRequest> mockedStatic = mockStatic(RDAPHttpRequest.class)) {
      mockedStatic.when(() -> RDAPHttpRequest.makeRequest(eq(uri1), anyInt(), eq("GET"), eq(true)))
                  .thenReturn(response1);

      rdapHttpQuery.run();

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
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    URI testUri = URI.create("https://example.com/domain/example.com");
    when(config.getUri()).thenReturn(testUri);
    when(config.getTimeout()).thenReturn(30);
    when(config.getMaxRedirects()).thenReturn(5);

    RDAPValidatorResults results =RDAPValidatorResultsImpl.getInstance();
    results.clear();

    try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
        MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class)) {

      dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
      InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
      dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

      Map<String, List<String>> headers = new HashMap<>();
      headers.put("Content-Type", List.of("application/json")); // Not application/rdap+JSON

      String responseBody = "{\"objectClassName\": \"domain\"}";

      RDAPHttpRequest.SimpleHttpResponse response = mock(RDAPHttpRequest.SimpleHttpResponse.class);
      when(response.statusCode()).thenReturn(200);
      when(response.body()).thenReturn(responseBody);
      when(response.headers()).thenReturn(HttpHeaders.of(headers, (k, v) -> true));
      when(response.getConnectionStatusCode()).thenReturn(ConnectionStatus.SUCCESS);

      httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                     .thenReturn(response);

      RDAPHttpQuery query = new RDAPHttpQuery(config);
      query.setResults(results);
      query.run();

      assertThat(results.getAll()).contains(
          RDAPValidationResult.builder()
                              .code(-13000)
                              .value("application/json")
                              .message("The content-type header does not contain the application/rdap+json media type.")
                              .build()
      );

      assertThat(results.getAll()).hasSize(1);
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

    doReturn(uri1).when(config).getUri();

    RDAPHttpRequest.SimpleHttpResponse response1 = mock(RDAPHttpRequest.SimpleHttpResponse.class);
    RDAPHttpRequest.SimpleHttpResponse response2 = mock(RDAPHttpRequest.SimpleHttpResponse.class);
    RDAPHttpRequest.SimpleHttpResponse response3 = mock(RDAPHttpRequest.SimpleHttpResponse.class);

    HttpHeaders headers1 = mock(HttpHeaders.class);
    HttpHeaders headers2 = mock(HttpHeaders.class);
    HttpHeaders headers3 = mock(HttpHeaders.class);

    when(response1.statusCode()).thenReturn(302);
    when(response1.headers()).thenReturn(headers1);
    when(response1.getConnectionStatusCode()).thenReturn(ConnectionStatus.SUCCESS);
    when(headers1.firstValue(LOCATION)).thenReturn(Optional.of(uri2.toString()));

    when(response2.statusCode()).thenReturn(302);
    when(response2.headers()).thenReturn(headers2);
    when(response2.getConnectionStatusCode()).thenReturn(ConnectionStatus.SUCCESS);
    when(headers2.firstValue(LOCATION)).thenReturn(Optional.of(uri3.toString()));

    when(response3.statusCode()).thenReturn(HTTP_OK);
    when(response3.headers()).thenReturn(headers3);
    when(response3.body()).thenReturn("{}");
    when(response3.getConnectionStatusCode()).thenReturn(ConnectionStatus.SUCCESS);
    when(headers3.allValues("Content-Type")).thenReturn(List.of("application/rdap+JSON"));

    try (MockedStatic<RDAPHttpRequest> mockedStatic = mockStatic(RDAPHttpRequest.class)) {
      mockedStatic.when(() -> RDAPHttpRequest.makeRequest(uri1, config.getTimeout(), "GET", true))
                  .thenReturn(response1);
      mockedStatic.when(() -> RDAPHttpRequest.makeRequest(uri2, config.getTimeout(), "GET", true))
                  .thenReturn(response2);
      mockedStatic.when(() -> RDAPHttpRequest.makeRequest(uri3, config.getTimeout(), "GET", true))
                  .thenReturn(response3);

      rdapHttpQuery.run();
      List<URI> redirects = rdapHttpQuery.getRedirects();
      assertThat(redirects).containsExactly(uri2, uri3);
    }
  }

  @Test
  public void test_HandleRequestException_Timeout()  {
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    doReturn(URI.create(HTTP_TEST_EXAMPLE)).when(config).getUri();
    doReturn(1).when(config).getTimeout();

    try (MockedStatic<RDAPHttpRequest> mockedStatic = mockStatic(RDAPHttpRequest.class)) {
      RDAPHttpRequest.SimpleHttpResponse timeoutResponse = mock(RDAPHttpRequest.SimpleHttpResponse.class);
      when(timeoutResponse.getConnectionStatusCode()).thenReturn(ConnectionStatus.CONNECTION_FAILED);

      mockedStatic.when(() -> RDAPHttpRequest.makeRequest(
                      any(URI.class), anyInt(), any(String.class), any(Boolean.class)))
                  .thenReturn(timeoutResponse);

      rdapHttpQuery.run();
      assertThat(rdapHttpQuery.getErrorStatus()).isEqualTo(ConnectionStatus.CONNECTION_FAILED);
    }
  }

  @Test
  public void test_AnalyzeIOException_ExpiredCertificate()  {
    RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    rdapHttpQuery.setResults(results);

    doReturn(URI.create(HTTP_TEST_EXAMPLE)).when(config).getUri();
    doReturn(PAUSE).when(config).getTimeout();

    try (MockedStatic<RDAPHttpRequest> mockedStatic = mockStatic(RDAPHttpRequest.class)) {
      RDAPHttpRequest.SimpleHttpResponse expiredCertResponse = mock(RDAPHttpRequest.SimpleHttpResponse.class);
      when(expiredCertResponse.getConnectionStatusCode()).thenReturn(ConnectionStatus.EXPIRED_CERTIFICATE);

      mockedStatic.when(() -> RDAPHttpRequest.makeRequest(
                      any(URI.class), anyInt(), any(String.class), any(Boolean.class)))
                  .thenReturn(expiredCertResponse);

      rdapHttpQuery.run();
      assertThat(rdapHttpQuery.getErrorStatus()).isEqualTo(ConnectionStatus.EXPIRED_CERTIFICATE);
    }
  }


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