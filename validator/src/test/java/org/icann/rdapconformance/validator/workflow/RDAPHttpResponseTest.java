package org.icann.rdapconformance.validator.workflow;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.temporaryRedirect;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;
import java.lang.reflect.Method;
import java.net.URI;
import org.icann.rdapconformance.validator.RDAPValidationStatus;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class RDAPHttpResponseTest {

  private final String wiremockHost = "localhost";
  private WireMockServer wireMockServer;

  @DataProvider(name = "fault")
  public static Object[][] serverFault() {
    return new Object[][]{{Fault.EMPTY_RESPONSE},
        {Fault.RANDOM_DATA_THEN_CLOSE},
        {Fault.MALFORMED_RESPONSE_CHUNK},
        {Fault.CONNECTION_RESET_BY_PEER}};
  }

  @BeforeMethod
  public void setUp(Method method) {
    WireMockConfiguration wmConfig = wireMockConfig()
        .dynamicHttpsPort()
        .bindAddress(wiremockHost);
    if (method.getName().equals("test_WithHttps")) {
      String keyStorePath = this.getClass().getResource("/mykeystore/out/ca-cert.jks").toString();
      String trustStorePath = this.getClass().getResource("/mykeystore/out/server.jks").toString();
      System.setProperty("javax.net.ssl.trustStore", trustStorePath.replace("file:", ""));
      System.setProperty("javax.net.ssl.trustStorePassword", "rdapct");
      System.setProperty("javax.net.ssl.trustStoreType", "JKS");
      wmConfig.trustStorePath(trustStorePath)
          .trustStorePassword("rdapct")
          .keystorePath(keyStorePath)
          .keystorePassword("rdapct")
          .keyManagerPassword("rdapct");
    }
    wireMockServer = new WireMockServer(wmConfig);
    wireMockServer.start();
  }

  @AfterMethod
  public void tearDown() {
    wireMockServer.stop();
    System.clearProperty("javax.net.ssl.trustStore");
    System.clearProperty("javax.net.ssl.trustStorePassword");
    System.clearProperty("javax.net.ssl.trustStoreType");
  }

  @Test
  @Ignore("System properties are not taken into account when launched among other tests, works as a standalone test though")
  public void test_WithHttps() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    String path = "/domain/test.example";
    String response = "{\"objectClassName\": \"domain\"}";

    doReturn(true).when(config).check();
    doReturn(10).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create(
        String.format("https://%s:%s%s", wiremockHost, wireMockServer.httpsPort(), path)))
        .when(config).getUri();

    configureFor("https", wiremockHost, wireMockServer.httpsPort());
    stubFor(get(urlEqualTo(path))
        .withPort(wireMockServer.httpsPort())
        .withScheme("https")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(response)));

    RDAPHttpResponse rdapHttpResponse = new RDAPHttpResponse(config);
    assertThat(rdapHttpResponse.hasError()).isFalse();
    assertThat(rdapHttpResponse.getHttpResponseBody()).isEqualTo(response);
    assertThat(rdapHttpResponse.getHttpStatusCode()).isEqualTo(200);
    assertThat(rdapHttpResponse.jsonResponseHasKey("objectClassName")).isTrue();
    assertThat(rdapHttpResponse.jsonResponseIsArray()).isFalse();
}

  @Test
  public void test_WithHttp() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    String path = "/domain/test.example";
    String response = "{\"objectClassName\": \"domain\"}";

    doReturn(true).when(config).check();
    doReturn(10).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create(
        String.format("http://%s:%s%s", wiremockHost, wireMockServer.port(), path)))
        .when(config).getUri();

    configureFor(wiremockHost, wireMockServer.port());
    stubFor(get(urlEqualTo(path))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(response)));

    RDAPHttpResponse rdapHttpResponse = new RDAPHttpResponse(config);
    assertThat(rdapHttpResponse.hasError()).isFalse();
    assertThat(rdapHttpResponse.getHttpResponseBody()).isEqualTo(response);
    assertThat(rdapHttpResponse.getHttpStatusCode()).isEqualTo(200);
    assertThat(rdapHttpResponse.jsonResponseHasKey("objectClassName")).isTrue();
    assertThat(rdapHttpResponse.jsonResponseIsArray()).isFalse();
  }

  @Test
  public void test_WithJsonArray() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    String path = "/nameservers?ip=.*";
    String response = "[{\"objectClassName\": \"nameserver\"}]";

    doReturn(true).when(config).check();
    doReturn(10).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create(
        String.format("http://%s:%s%s", wiremockHost, wireMockServer.port(), path)))
        .when(config).getUri();

    configureFor(wiremockHost, wireMockServer.port());
    stubFor(get(urlEqualTo(path))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(response)));

    RDAPHttpResponse rdapHttpResponse = new RDAPHttpResponse(config);
    assertThat(rdapHttpResponse.hasError()).isFalse();
    assertThat(rdapHttpResponse.getHttpResponseBody()).isEqualTo(response);
    assertThat(rdapHttpResponse.getHttpStatusCode()).isEqualTo(200);
    assertThat(rdapHttpResponse.jsonResponseIsArray()).isTrue();
  }


  @Test(dataProvider = "fault")
  public void test_ServerFault_ReturnsErrorStatus20(Fault fault) {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    String path = "/domain/test.example";

    doReturn(true).when(config).check();
    doReturn(10).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create(
        String.format("http://%s:%s%s", wiremockHost, wireMockServer.port(), path)))
        .when(config).getUri();

    configureFor(wiremockHost, wireMockServer.port());
    stubFor(get(urlEqualTo(path))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withFault(fault)));

    RDAPHttpResponse rdapHttpResponse = new RDAPHttpResponse(config);
    assertThat(rdapHttpResponse.hasError()).isTrue();
    assertThat(rdapHttpResponse.getErrorStatus())
        .isEqualTo(RDAPValidationStatus.NETWORK_RECEIVE_FAIL);
  }

  @Test
  public void test_NetworkSendFail_ReturnsErrorStatus19() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);

    doReturn(true).when(config).check();
    doReturn(1).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create("http://unknown")).when(config).getUri();

    RDAPHttpResponse rdapHttpResponse = new RDAPHttpResponse(config);
    assertThat(rdapHttpResponse.hasError()).isTrue();
    assertThat(rdapHttpResponse.getErrorStatus()).isEqualTo(RDAPValidationStatus.NETWORK_SEND_FAIL);
  }

  @Test
  public void test_ConnectionTimeout_ReturnsErrorStatus10() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    String path = "/domain/test.example";
    String response = "{\"objectClassName\": \"domain\"}";

    doReturn(true).when(config).check();
    doReturn(1).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create(
        String.format("http://%s:%s%s", wiremockHost, wireMockServer.port(), path)))
        .when(config).getUri();

    configureFor(wiremockHost, wireMockServer.port());
    stubFor(get(urlEqualTo(path))
        .withScheme("http")
        .willReturn(aResponse()
            .withFixedDelay(2000)
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(response)));

    RDAPHttpResponse rdapHttpResponse = new RDAPHttpResponse(config);
    assertThat(rdapHttpResponse.hasError()).isTrue();
    assertThat(rdapHttpResponse.getErrorStatus()).isEqualTo(RDAPValidationStatus.CONNECTION_FAILED);
  }

  @Test
  public void test_ServerRedirectLessThanRetries_Returns200() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    String path1 = "/domain/test1.example";
    String path2 = "/domain/test2.example";
    String path3 = "/domain/test3.example";
    String response = "{\"objectClassName\": \"domain\"}";

    doReturn(true).when(config).check();
    doReturn(10).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create(
        String.format("http://%s:%s%s", wiremockHost, wireMockServer.port(), path1)))
        .when(config).getUri();

    configureFor(wiremockHost, wireMockServer.port());
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
            .withBody(response)));

    RDAPHttpResponse rdapHttpResponse = new RDAPHttpResponse(config);
    assertThat(rdapHttpResponse.hasError()).isFalse();
    assertThat(rdapHttpResponse.getHttpResponseBody()).isEqualTo(response);
    assertThat(rdapHttpResponse.getHttpStatusCode()).isEqualTo(200);

    verify(exactly(1), getRequestedFor(urlEqualTo(path1)));
    verify(exactly(1), getRequestedFor(urlEqualTo(path2)));
    verify(exactly(1), getRequestedFor(urlEqualTo(path3)));

  }

  @Test
  public void test_ServerRedirectMoreThanRetries_ReturnsErrorStatus16() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    String path1 = "/domain/test1.example";
    String path2 = "/domain/test2.example";
    String path3 = "/domain/test3.example";
    String path4 = "/domain/test4.example";
    String response = "{\"objectClassName\": \"domain\"}";

    doReturn(true).when(config).check();
    doReturn(10).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create(
        String.format("http://%s:%s%s", wiremockHost, wireMockServer.port(), path1)))
        .when(config).getUri();

    configureFor(wiremockHost, wireMockServer.port());
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
            .withBody(response)));

    RDAPHttpResponse rdapHttpResponse = new RDAPHttpResponse(config);
    assertThat(rdapHttpResponse.hasError()).isTrue();
    assertThat(rdapHttpResponse.getErrorStatus())
        .isEqualTo(RDAPValidationStatus.TOO_MANY_REDIRECTS);

    verify(exactly(1), getRequestedFor(urlEqualTo(path1)));
    verify(exactly(1), getRequestedFor(urlEqualTo(path2)));
    verify(exactly(1), getRequestedFor(urlEqualTo(path3)));
    verify(exactly(0), getRequestedFor(urlEqualTo(path4)));
  }
  
  @Test
  public void test_NoContentType_ReturnsErrorStatus5() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    String path = "/domain/test.example";
    String response = "{\"objectClassName\": \"domain\"}";

    doReturn(true).when(config).check();
    doReturn(1).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create(
        String.format("http://%s:%s%s", wiremockHost, wireMockServer.port(), path)))
        .when(config).getUri();

    configureFor(wiremockHost, wireMockServer.port());
    stubFor(get(urlEqualTo(path))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json;encoding=UTF-8")
            .withBody(response)));

    RDAPHttpResponse rdapHttpResponse = new RDAPHttpResponse(config);
    assertThat(rdapHttpResponse.hasError()).isTrue();
    assertThat(rdapHttpResponse.getErrorStatus()).isEqualTo(RDAPValidationStatus.WRONG_CONTENT_TYPE);
  }
  
  @Test
  public void test_InvalidJson_ReturnsErrorStatus6() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    String path = "/domain/test.example";
    String response = "{\"objectClassName\"}";

    doReturn(true).when(config).check();
    doReturn(10).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create(
        String.format("http://%s:%s%s", wiremockHost, wireMockServer.port(), path)))
        .when(config).getUri();

    configureFor(wiremockHost, wireMockServer.port());
    stubFor(get(urlEqualTo(path))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(response)));

    RDAPHttpResponse rdapHttpResponse = new RDAPHttpResponse(config);
    assertThat(rdapHttpResponse.hasError()).isTrue();
    assertThat(rdapHttpResponse.getErrorStatus()).isEqualTo(RDAPValidationStatus.RESPONSE_INVALID);
  }

  // TODO the following tests rely on web resources that may change without notice, should
  // create our own certificates, CRL, etc.

  @Test
  public void test_WithHttpsCertificateExpired_ReturnsErrorStatus14() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);

    doReturn(true).when(config).check();
    doReturn(100).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create("https://expired.badssl.com")).when(config).getUri();

    RDAPHttpResponse rdapHttpResponse = new RDAPHttpResponse(config);
    assertThat(rdapHttpResponse.hasError()).isTrue();
    assertThat(rdapHttpResponse.getErrorStatus())
        .isEqualTo(RDAPValidationStatus.EXPIRED_CERTIFICATE);
  }

  @Test
  public void test_WithHttpsCertificateRevoked_ReturnsErrorStatus13() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);

    doReturn(true).when(config).check();
    doReturn(100).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create("https://revoked.badssl.com")).when(config).getUri();

    RDAPHttpResponse rdapHttpResponse = new RDAPHttpResponse(config);
    assertThat(rdapHttpResponse.hasError()).isTrue();
    assertThat(rdapHttpResponse.getErrorStatus())
        .isEqualTo(RDAPValidationStatus.REVOKED_CERTIFICATE);
  }

  @Test
  public void test_WithHttpsCertificateInvalid_ReturnsErrorStatus12() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);

    doReturn(true).when(config).check();
    doReturn(100).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create("https://wrong.host.badssl.com")).when(config).getUri();

    RDAPHttpResponse rdapHttpResponse = new RDAPHttpResponse(config);
    assertThat(rdapHttpResponse.hasError()).isTrue();
    assertThat(rdapHttpResponse.getErrorStatus())
        .isEqualTo(RDAPValidationStatus.INVALID_CERTIFICATE);
  }

  @Test
  public void test_WithHttpsCertificateError_ReturnsErrorStatus15() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);

    doReturn(true).when(config).check();
    doReturn(100).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create("https://untrusted-root.badssl.com")).when(config).getUri();

    RDAPHttpResponse rdapHttpResponse = new RDAPHttpResponse(config);
    assertThat(rdapHttpResponse.hasError()).isTrue();
    assertThat(rdapHttpResponse.getErrorStatus()).isEqualTo(RDAPValidationStatus.CERTIFICATE_ERROR);
  }
}