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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Objects;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.mockito.MockedStatic;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

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
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationStatus;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;

public class RDAPHttpQueryTest extends HttpTestingUtils {

  public static final int EXPIRED_CERT_PORT = 8444;
  public static final int INVALID_CERT_PORT = 8445;
  public static final int UNTRUSTED_ROOT_CERT_PORT = 8446;
  public static final int PAUSE = 1000;
  public static final String EXPIRED = "expired";
  public static final String INVALID_HOST = "invalidhost";
  public static final String UNTRUSTED = "untrusted";
  public static final String HTTPS_LOCALHOST = "https://localhost:";
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
    // TODO the following data rely on web resources that may change without notice, should
    // create our own certificates, CRL, etc.
    return new Object[][]{{"https://expired.badssl.com", RDAPValidationStatus.EXPIRED_CERTIFICATE},
        {"https://revoked.badssl.com", RDAPValidationStatus.REVOKED_CERTIFICATE},
        {"https://wrong.host.badssl.com", RDAPValidationStatus.INVALID_CERTIFICATE},
        {"https://untrusted-root.badssl.com", RDAPValidationStatus.HANDSHAKE_FAILED}};
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
    assertThat(rdapHttpQuery.jsonIsSearchResponse()).isFalse();
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
        .isEqualTo(RDAPValidationStatus.NETWORK_RECEIVE_FAIL);
  }

  @Test
  public void test_WithHttp() {
    givenUri("http");
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(RDAP_RESPONSE)));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.getData()).isEqualTo(RDAP_RESPONSE);
    assertThat(rdapHttpQuery.getStatusCode()).isPresent().get().isEqualTo(200);
    assertThat(rdapHttpQuery.jsonIsSearchResponse()).isFalse();
  }

  @Test
  public void test_WithJsonArray() {
    String path = "/nameservers?ip=.*";
    String response = "{\"nameserverSearchResults\": [ {\"objectClassName\":\"nameserver\"} ]}";

    givenUri("http", path);
    stubFor(get(urlEqualTo(path))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(response)));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.getData()).isEqualTo(response);
    assertThat(rdapHttpQuery.getStatusCode()).isPresent().get().isEqualTo(200);
    assertThat(rdapHttpQuery.jsonIsSearchResponse()).isTrue();
  }

  @Test(dataProvider = "fault")
  public void test_ServerFault_ReturnsErrorStatus20(Fault fault) {
    givenUri("http");
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withFault(fault)));

    assertThat(rdapHttpQuery.run()).isFalse();
    assertThat(rdapHttpQuery.getErrorStatus())
        .isEqualTo(RDAPValidationStatus.NETWORK_RECEIVE_FAIL);
  }

  @Test
  public void test_NetworkSendFail_ReturnsErrorStatus19() {
    doReturn(URI.create("http://test.example")).when(config).getUri();

    assertThat(rdapHttpQuery.run()).isFalse();
    assertThat(rdapHttpQuery.getErrorStatus()).isEqualTo(RDAPValidationStatus.NETWORK_SEND_FAIL);
  }

  @Test
  public void test_ConnectionTimeout_ReturnsErrorStatus10() {
    givenUri("http");
    doReturn(1).when(config).getTimeout();
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme("http")
        .willReturn(aResponse()
            .withFixedDelay(2000)
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(RDAP_RESPONSE)));

    assertThat(rdapHttpQuery.run()).isFalse();
    assertThat(rdapHttpQuery.getErrorStatus()).isEqualTo(RDAPValidationStatus.CONNECTION_FAILED);
  }

  @Test
  public void test_ServerRedirectLessThanRetries_Returns200() {
    String path1 = "/domain/test1.example";
    String path2 = "/domain/test2.example";
    String path3 = "/domain/test3.example";

    givenUri("http", path1);
    stubFor(get(urlEqualTo(path1))
        .withScheme("http")
        .willReturn(temporaryRedirect(path2)));
    stubFor(get(urlEqualTo(path2))
        .withScheme("http")
        .willReturn(temporaryRedirect(path3)));
    stubFor(get(urlEqualTo(path3))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(RDAP_RESPONSE)));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.getData()).isEqualTo(RDAP_RESPONSE);
    assertThat(rdapHttpQuery.getStatusCode()).isPresent().get().isEqualTo(200);

    verify(exactly(1), getRequestedFor(urlEqualTo(path1)));
    verify(exactly(1), getRequestedFor(urlEqualTo(path2)));
    verify(exactly(1), getRequestedFor(urlEqualTo(path3)));

  }

  @Test
  public void test_ServerRedirectMoreThanRetries_ReturnsErrorStatus16() {
    String path1 = "/domain/test1.example";
    String path2 = "/domain/test2.example";
    String path3 = "/domain/test3.example";
    String path4 = "/domain/test4.example";

    givenUri("http", path1);
    stubFor(get(urlEqualTo(path1))
        .withScheme("http")
        .willReturn(temporaryRedirect(path2)));
    stubFor(get(urlEqualTo(path2))
        .withScheme("http")
        .willReturn(temporaryRedirect(path3)));
    stubFor(get(urlEqualTo(path3))
        .withScheme("http")
        .willReturn(temporaryRedirect(path4)));
    stubFor(get(urlEqualTo(path4))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(RDAP_RESPONSE)));

    assertThat(rdapHttpQuery.run()).isFalse();
    assertThat(rdapHttpQuery.getErrorStatus())
        .isEqualTo(RDAPValidationStatus.TOO_MANY_REDIRECTS);

    verify(exactly(1), getRequestedFor(urlEqualTo(path1)));
    verify(exactly(1), getRequestedFor(urlEqualTo(path2)));
    verify(exactly(1), getRequestedFor(urlEqualTo(path3)));
    verify(exactly(0), getRequestedFor(urlEqualTo(path4)));
  }

  @Test
  public void testIsBlindlyCopyingParams() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    RDAPHttpQuery rdapHttpQuery = new RDAPHttpQuery(config);
    RDAPValidatorResults results = new RDAPValidatorResultsImpl();
    rdapHttpQuery.setResults(results);

    HttpHeaders headers = mock(HttpHeaders.class);
    URI originalUri = URI.create("http://example.com?param=value");


    when(config.getUri()).thenReturn(originalUri);
    when(headers.firstValue("Location")).thenReturn(Optional.of("http://example.com/redirected?param=value"));

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
    RDAPValidatorResults results = new RDAPValidatorResultsImpl();
    HttpHeaders headers = mock(HttpHeaders.class);
    URI originalUri = URI.create("http://example.com?param=value");

    rdapHttpQuery.setResults(results);
    when(config.getUri()).thenReturn(originalUri);
    when(headers.firstValue("Location")).thenReturn(Optional.of("http://example.com/redirected"));

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
  public void testIsBlindlyCopyingParams_Hit() {
    String path1 = "/domain/test1.example";
    String path2 = "/domain/test2.example?param=value";

    givenUri("http", path1 + "?param=value");
    stubFor(get(urlEqualTo(path1 + "?param=value"))
        .withScheme("http")
        .willReturn(temporaryRedirect(path2)));
    stubFor(get(urlEqualTo(path2))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "text/plain")
            .withBody("Redirected successfully"))); // No JSON needed

    RDAPValidatorResults results = new RDAPValidatorResultsImpl();
    rdapHttpQuery.setResults(results);

    assertThat(rdapHttpQuery.run()).isFalse();

    List<URI> redirects = rdapHttpQuery.getRedirects();

    assertThat(redirects).containsExactly(
        URI.create("http://localhost:8080/domain/test2.example?param=value")
    );
  }

  @Test
  public void test_RedirectWithoutLocationHeader_BreaksLoop() {
    String path1 = "/domain/test1.example";

    givenUri("http", path1);
    stubFor(get(urlEqualTo(path1))
        .withScheme("http")
        .willReturn(aResponse()
            .withStatus(302))); // Redirect status without Location header

    RDAPValidatorResults results = new RDAPValidatorResultsImpl();
    rdapHttpQuery.setResults(results);

    assertThat(rdapHttpQuery.run()).isFalse();

    List<URI> redirects = rdapHttpQuery.getRedirects();

    assertThat(redirects).isEmpty(); // No redirects should be followed
  }

  @Test
  public void test_NoContentType_ErrorCode13000AddedInResults() {
    RDAPValidatorResults results = new RDAPValidatorResultsImpl();
    rdapHttpQuery.setResults(results);

    givenUri("http");
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json;encoding=UTF-8")
            .withBody(RDAP_RESPONSE)));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(results.getAll()).contains(
            RDAPValidationResult.builder()
            .code(-13000)
            .value("application/json;encoding=UTF-8")
            .message("The content-type header does not contain the application/rdap+json media type.")
            .build());
  }

  @Test
  public void test_InvalidJson_ErrorCode13001AddedInResults() {
    RDAPValidatorResults results = new RDAPValidatorResultsImpl();
    rdapHttpQuery.setResults(results);
    String response = "{\"objectClassName\"}";

    givenUri("http");
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme("http")
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
    RDAPValidatorResults results = new RDAPValidatorResultsImpl();
    rdapHttpQuery.setResults(results);

    String path = "/nameservers?ip=.*";
    String response = "{\"nameserverSearchResults\": [ {\"objectClassName\":\"nameserver\"} ]}";
    givenUri("http");
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme("http")
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

  @Ignore
  @Test(dataProvider = "tlsErrors")
  public void test_WithHttpsCertificateError_ReturnsAppropriateErrorStatus(String url,
      RDAPValidationStatus expectedStatus) {
    System.out.println("checking url: " + url  + " for expected status of: " + expectedStatus.getDescription());
    doReturn(URI.create(url)).when(config).getUri();

    assertThat(rdapHttpQuery.run()).isFalse();
    assertThat(rdapHttpQuery.getErrorStatus())
        .isEqualTo(expectedStatus);
  }

  @Test
  public void checkWithQueryType_StatusNot200_IsOk() {
    givenUri("http");
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme("http")
        .willReturn(notFound()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody("{}")));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.checkWithQueryType(RDAPQueryType.DOMAIN)).isTrue();
  }

  @Test
  public void checkWithQueryType_ObjectClassNameInJsonResponse_IsOk() {
    givenUri("http");
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(RDAP_RESPONSE)));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.checkWithQueryType(RDAPQueryType.DOMAIN)).isTrue();

  }

  @Test
  public void checkWithQueryType_NoObjectClassNameInJsonResponse_ReturnsErrorCode13003InResults() {
    givenUri("http");
    String response = "{\"NoObjectClassName\": \"domain\"}";
    RDAPValidatorResults results = new RDAPValidatorResultsImpl();
    rdapHttpQuery.setResults(results);

    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme("http")
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

    givenUri("http", path);
    stubFor(get(urlEqualTo(path))
        .withScheme("http")
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
    RDAPValidatorResults results = new RDAPValidatorResultsImpl();
    rdapHttpQuery.setResults(results);

    givenUri("http", path);
    stubFor(get(urlEqualTo(path))
        .withScheme("http")
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
  public void testGetRedirects_NoRedirects() {
    doReturn(URI.create("http://example.com")).when(config).getUri();
    stubFor(get(urlEqualTo("/"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody("{}")));
    RDAPValidatorResults results = new RDAPValidatorResultsImpl();
    rdapHttpQuery.setResults(results);

    rdapHttpQuery.run();

    List<URI> redirects = rdapHttpQuery.getRedirects();
    assertThat(redirects).isEmpty();
  }

  @Test
  public void testGetRedirects_WithRedirects() {
    String path1 = "/domain/test1.example";
    String path2 = "/domain/test2.example";
    String path3 = "/domain/test3.example";

    givenUri("http", path1);
    stubFor(get(urlEqualTo(path1))
        .withScheme("http")
        .willReturn(temporaryRedirect(path2)));
    stubFor(get(urlEqualTo(path2))
        .withScheme("http")
        .willReturn(temporaryRedirect(path3)));
    stubFor(get(urlEqualTo(path3))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(RDAP_RESPONSE)));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.getData()).isEqualTo(RDAP_RESPONSE);
    assertThat(rdapHttpQuery.getStatusCode()).isPresent().get().isEqualTo(200);

    List<URI> redirects = rdapHttpQuery.getRedirects();

    assertThat(redirects).containsExactly(
        URI.create("http://localhost:8080/domain/test2.example"),
        URI.create("http://localhost:8080/domain/test3.example")
    );
  }

  @Test
  public void test_HandleRequestException_ConnectionFailed() throws IOException, InterruptedException {
    doReturn(URI.create("http://test.example")).when(config).getUri();
    doReturn(PAUSE).when(config).getTimeout();

    RDAPHttpQuery query = new RDAPHttpQuery(config);

    // Mock the static method
    try (MockedStatic<RDAPHttpRequest> mockedStatic = mockStatic(RDAPHttpRequest.class)) {
      // Simulate a ConnectException
      mockedStatic.when(() -> RDAPHttpRequest.makeHttpGetRequest(URI.create("http://test.example"), PAUSE))
                  .thenThrow(new ConnectException("Connection failed"));
      query.makeRequest();
      assertThat(query.getErrorStatus()).isEqualTo(RDAPValidationStatus.CONNECTION_FAILED);
    }
  }

  @Test
  public void test_HandleRequestException_Timeout() throws IOException, InterruptedException {
    doReturn(URI.create("http://test.example")).when(config).getUri();
    doReturn(1).when(config).getTimeout();

    RDAPHttpQuery query = new RDAPHttpQuery(config);

    try (MockedStatic<RDAPHttpRequest> mockedStatic = mockStatic(RDAPHttpRequest.class)) {
      // Simulate a HttpTimeoutException
      mockedStatic.when(() -> RDAPHttpRequest.makeHttpGetRequest(URI.create("http://test.example"), 1))
                  .thenThrow(new HttpTimeoutException("Timeout"));

      query.makeRequest();
      assertThat(query.getErrorStatus()).isEqualTo(RDAPValidationStatus.CONNECTION_FAILED);
    }
  }

  @Test
  public void test_AnalyzeIOException_ExpiredCertificate() throws IOException, InterruptedException {
    doReturn(URI.create("http://test.example")).when(config).getUri();
    doReturn(1).when(config).getTimeout();

    RDAPHttpQuery query = new RDAPHttpQuery(config);

    try (MockedStatic<RDAPHttpRequest> mockedStatic = mockStatic(RDAPHttpRequest.class)) {
      // Simulate a CertificateExpiredException
      mockedStatic.when(() -> RDAPHttpRequest.makeHttpGetRequest(URI.create("http://test.example"), 1))
                  .thenThrow(new IOException(new java.security.cert.CertificateExpiredException("Expired certificate")));

      query.makeRequest();
      assertThat(query.getErrorStatus()).isEqualTo(RDAPValidationStatus.EXPIRED_CERTIFICATE);
    }
  }

  // XXX We need to come back and figure out why this one doesn't work - throws a null pointer exception
  @Ignore
  @Test
  public void test_AnalyzeIOException_RevokedCertificate() throws IOException, InterruptedException {
    doReturn(URI.create("http://test.example")).when(config).getUri();
    doReturn(1).when(config).getTimeout();
    RDAPHttpQuery query = new RDAPHttpQuery(config);

    // Initialize the results field
    RDAPValidatorResults results = new RDAPValidatorResultsImpl();
    query.setResults(results);

    try (MockedStatic<RDAPHttpRequest> mockedStatic = mockStatic(RDAPHttpRequest.class)) {
      // Simulate a CertificateRevokedException with non-null parameters
      mockedStatic.when(() -> RDAPHttpRequest.makeHttpGetRequest(URI.create("http://test.example"), 1))
                  .thenThrow(new IOException(new java.security.cert.CertificateRevokedException(
                      new Date(), null, null, Map.of())));

      query.makeRequest();
      assertThat(query.getErrorStatus()).isEqualTo(RDAPValidationStatus.REVOKED_CERTIFICATE);
    }
  }

  @Test
  public void test_RedirectsToTestInvalid() {
    String path = "/domain/test.invalid";

    givenUri("http", path);
    stubFor(get(urlEqualTo(path))
        .withScheme("http")
        .willReturn(temporaryRedirect(path)));

    RDAPValidatorResults results = new RDAPValidatorResultsImpl();
    rdapHttpQuery.setResults(results);
    rdapHttpQuery.makeRequest();

    assertThat(results.getAll()).contains(
        RDAPValidationResult.builder()
                            .code(-13005)
                            .value("<location header value>")
                            .message("Server responded with a redirect to itself for domain 'test.invalid'.")
                            .build()
    );
  }


  public static class MultiCertHttpsTestServer {

    private static final List<ServerInstance> servers = new ArrayList<>();

    public static void main(String[] args) throws Exception {
      startHttpsServer(EXPIRED_CERT_PORT, EXPIRED);
      startHttpsServer(INVALID_CERT_PORT, INVALID_HOST);
      startHttpsServer(UNTRUSTED_ROOT_CERT_PORT, UNTRUSTED);
    }

    public static void startHttpsServer(int port, String certName) throws Exception {
      HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(port), 0);
      SSLContext sslContext = createSSLContext(certName);
      httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext));
      httpsServer.createContext("/", new SimpleHandler(certName));
      httpsServer.setExecutor(null);
      new Thread(httpsServer::start).start();

      servers.add(new ServerInstance(port, httpsServer));
    }

    public static void stopAll() {
      for (ServerInstance s : servers) {
        s.server.stop(0);
      }
    }

    private static SSLContext createSSLContext(String name) throws Exception {
      String keystoreFile = "keystores/" + name + ".p12";  // Created using OpenSSL or keytool
      char[] password = "password".toCharArray();

      KeyStore ks = KeyStore.getInstance("PKCS12");
      ClassLoader classLoader = MultiCertHttpsTestServer.class.getClassLoader();
      try (FileInputStream fis = new FileInputStream(new File(
          Objects.requireNonNull(classLoader.getResource(keystoreFile)).getFile()))) {
        ks.load(fis, password);
      }

      KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
      kmf.init(ks, password);

      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(kmf.getKeyManagers(), null, null);
      return sslContext;
    }

    static class SimpleHandler implements HttpHandler {
      private final String certName;

      public SimpleHandler(String certName) {
        this.certName = certName;
      }

      @Override
      public void handle(HttpExchange exchange) throws IOException {
        String response = "Served by cert: " + certName;
        exchange.sendResponseHeaders(HTTP_OK, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      }
    }

    static class ServerInstance {
      int port;
      HttpsServer server;

      ServerInstance(int port, HttpsServer server) {
        this.port = port;
        this.server = server;
      }
    }
  }

  @Test
  public void test_WithLocalHttpsCertificateError_ReturnsAppropriateErrorStatus() throws Exception {
    // 🔥 Disable OCSP checking for test certs
    System.setProperty("com.sun.net.ssl.checkRevocation", "false");

    KeyStore trustStore = KeyStore.getInstance("JKS");
    try (InputStream is = getClass().getClassLoader().getResourceAsStream("keystores/truststore.jks")) {
      trustStore.load(is, "password".toCharArray());
    }

    // this stuff is essential
    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(trustStore);

    // Again, painful, but we have to set this just to get the errors
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, tmf.getTrustManagers(), null);
    SSLContext.setDefault(sslContext);
    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

    // Start HTTPS servers with different certificates
    MultiCertHttpsTestServer.startHttpsServer(EXPIRED_CERT_PORT, EXPIRED);
    MultiCertHttpsTestServer.startHttpsServer(INVALID_CERT_PORT, INVALID_HOST);
    MultiCertHttpsTestServer.startHttpsServer(UNTRUSTED_ROOT_CERT_PORT, UNTRUSTED);
    // we need to sleep before we can start testing it
    Thread.sleep(PAUSE);

    try {
      RDAPValidatorResults results = new RDAPValidatorResultsImpl();
      rdapHttpQuery.setResults(results);

      // Test expired certificate
      doReturn(URI.create(HTTPS_LOCALHOST + EXPIRED_CERT_PORT)).when(config).getUri();
      assertThat(rdapHttpQuery.run()).isFalse();
      RDAPValidationStatus errorStatus = rdapHttpQuery.getErrorStatus();
      assertThat(errorStatus).isEqualTo(RDAPValidationStatus.EXPIRED_CERTIFICATE);

      // Test invalid host certificate
      doReturn(URI.create(HTTPS_LOCALHOST + INVALID_CERT_PORT)).when(config).getUri();
      assertThat(rdapHttpQuery.run()).isFalse();
      assertThat(rdapHttpQuery.getErrorStatus()).isEqualTo(RDAPValidationStatus.INVALID_CERTIFICATE);

      // Test untrusted certificate
      doReturn(URI.create(HTTPS_LOCALHOST + UNTRUSTED_ROOT_CERT_PORT)).when(config).getUri();
      assertThat(rdapHttpQuery.run()).isFalse();
      assertThat(rdapHttpQuery.getErrorStatus()).isEqualTo(RDAPValidationStatus.HANDSHAKE_FAILED);
    } finally {
      // Stop all HTTPS servers
      MultiCertHttpsTestServer.stopAll();
    }
  }
}