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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;
import java.lang.reflect.Method;
import java.net.URI;
import org.icann.rdapconformance.validator.workflow.rdap.HttpTestingUtils;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationStatus;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class RDAPHttpQueryTest extends HttpTestingUtils {

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

  @Ignore
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
  public void test_NoContentType_ReturnsErrorStatus5() {
    givenUri("http");
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json;encoding=UTF-8")
            .withBody(RDAP_RESPONSE)));

    assertThat(rdapHttpQuery.run()).isFalse();
    assertThat(rdapHttpQuery.getErrorStatus()).isEqualTo(RDAPValidationStatus.WRONG_CONTENT_TYPE);
  }

  @Test
  public void test_InvalidJson_ReturnsErrorStatus6() {
    String response = "{\"objectClassName\"}";

    givenUri("http");
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(response)));

    assertThat(rdapHttpQuery.run()).isFalse();
    assertThat(rdapHttpQuery.getErrorStatus()).isEqualTo(RDAPValidationStatus.RESPONSE_INVALID);
  }

  @Ignore("revoked.badssl.com has an expired certificate nowadays, so this test will always fails")
  @Test(dataProvider = "tlsErrors")
  public void test_WithHttpsCertificateError_ReturnsAppropriateErrorStatus(String url,
      RDAPValidationStatus expectedStatus) {
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
  public void checkWithQueryType_NoObjectClassNameInJsonResponse_ReturnsErrorStatus8() {
    givenUri("http");
    String response = "{\"NoObjectClassName\": \"domain\"}";

    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(response)));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.checkWithQueryType(RDAPQueryType.DOMAIN)).isFalse();
    assertThat(rdapHttpQuery.getErrorStatus())
        .isEqualTo(RDAPValidationStatus.EXPECTED_OBJECT_NOT_FOUND);

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
  public void checkWithQueryType_JsonResponseIsNotAnArray_ReturnsErrorStatus8() {
    String path = "/nameservers?ip=.*";
    String response = "{\"nameserverSearchResults\": { \"objectClassName\":\"nameserver\" }}";

    givenUri("http", path);
    stubFor(get(urlEqualTo(path))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(response)));

    assertThat(rdapHttpQuery.run()).isTrue();
    assertThat(rdapHttpQuery.checkWithQueryType(RDAPQueryType.NAMESERVERS)).isFalse();
    assertThat(rdapHttpQuery.getErrorStatus())
        .isEqualTo(RDAPValidationStatus.EXPECTED_OBJECT_NOT_FOUND);

  }
}