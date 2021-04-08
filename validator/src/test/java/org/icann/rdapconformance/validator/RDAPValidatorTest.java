package org.icann.rdapconformance.validator;

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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import org.icann.rdapconformance.validator.RDAPValidator.RDAPHttpException;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class RDAPValidatorTest {

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
    if (method.getName().equals("testGetHttpResponse_WithHttps")) {
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
  public void testGetHttpResponse_WithHttps() throws RDAPHttpException {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    String path = "/domain/test.example";
    String response = "{\"test\": \"value\"}";

    doReturn(true).when(config).check();
    doReturn(10).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create(
        String.format("https://%s:%s%s", wiremockHost, wireMockServer.httpsPort(), path)))
        .when(config).getUri();

    RDAPValidator validator = new RDAPValidator(config);

    configureFor("https", wiremockHost, wireMockServer.httpsPort());

    stubFor(get(urlEqualTo(path))
        .withPort(wireMockServer.httpsPort())
        .withScheme("https")
        .willReturn(aResponse()
            .withBody(response)));

    HttpResponse<String> httpResponse = validator.getHttpResponse();

    assertThat(httpResponse.body()).isEqualTo(response);
    assertThat(httpResponse.statusCode()).isEqualTo(200);
  }

  @Test
  public void testGetHttpResponse_WithHttp() throws RDAPHttpException {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    String path = "/domain/test.example";
    String response = "{\"test\": \"value\"}";

    doReturn(true).when(config).check();
    doReturn(10).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create(
        String.format("http://%s:%s%s", wiremockHost, wireMockServer.port(), path)))
        .when(config).getUri();

    RDAPValidator validator = new RDAPValidator(config);

    configureFor(wiremockHost, wireMockServer.port());

    stubFor(get(urlEqualTo(path))
        .withScheme("http")
        .willReturn(aResponse()
            .withBody(response)));

    HttpResponse<String> httpResponse = validator.getHttpResponse();

    assertThat(httpResponse.body()).isEqualTo(response);
    assertThat(httpResponse.statusCode()).isEqualTo(200);
  }

  @Test(dataProvider = "fault")
  public void testGetHttpResponse_ServerFault_ThrowExceptionWithStatus20(Fault fault) {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    String path = "/domain/test.example";

    doReturn(true).when(config).check();
    doReturn(10).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create(
        String.format("http://%s:%s%s", wiremockHost, wireMockServer.port(), path)))
        .when(config).getUri();

    RDAPValidator validator = new RDAPValidator(config);

    configureFor(wiremockHost, wireMockServer.port());

    stubFor(get(urlEqualTo(path))
        .withScheme("http")
        .willReturn(aResponse()
            .withFault(fault)));

    assertThatExceptionOfType(RDAPHttpException.class)
        .isThrownBy(validator::getHttpResponse)
        .withCauseInstanceOf(IOException.class)
        .withMessage(RDAPValidationStatus.NETWORK_RECEIVE_FAIL.getDescription());
  }

  @Test
  public void testGetHttpResponse_NetworkSendFail_ThrowExceptionWithStatus19() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);

    doReturn(true).when(config).check();
    doReturn(1).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create("http://unknown")).when(config).getUri();

    RDAPValidator validator = new RDAPValidator(config);

    assertThatExceptionOfType(RDAPHttpException.class)
        .isThrownBy(validator::getHttpResponse)
        .withCauseInstanceOf(ConnectException.class)
        .withMessage(RDAPValidationStatus.NETWORK_SEND_FAIL.getDescription());
  }

  @Test
  public void testGetHttpResponse_ConnectionTimeout_ThrowExceptionWithStatus10() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    String path = "/domain/test.example";
    String response = "{\"test\": \"value\"}";

    doReturn(true).when(config).check();
    doReturn(1).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create(
        String.format("http://%s:%s%s", wiremockHost, wireMockServer.port(), path)))
        .when(config).getUri();

    RDAPValidator validator = new RDAPValidator(config);

    configureFor(wiremockHost, wireMockServer.port());

    stubFor(get(urlEqualTo(path))
        .withScheme("http")
        .willReturn(aResponse()
            .withFixedDelay(2000)
            .withBody(response)));

    assertThatExceptionOfType(RDAPHttpException.class)
        .isThrownBy(validator::getHttpResponse)
        .withCauseInstanceOf(HttpTimeoutException.class)
        .withMessage(RDAPValidationStatus.CONNECTION_FAILED.getDescription());
  }

  @Test
  public void testGetHttpResponse_ServerRedirectLessThanRetries_Returns200()
      throws RDAPHttpException {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    String path1 = "/domain/test1.example";
    String path2 = "/domain/test2.example";
    String path3 = "/domain/test3.example";
    String response = "{\"test\": \"value\"}";

    doReturn(true).when(config).check();
    doReturn(10).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create(
        String.format("http://%s:%s%s", wiremockHost, wireMockServer.port(), path1)))
        .when(config).getUri();

    RDAPValidator validator = new RDAPValidator(config);

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
            .withBody(response)));

    HttpResponse<String> httpResponse = validator.getHttpResponse();

    assertThat(httpResponse.body()).isEqualTo(response);
    assertThat(httpResponse.statusCode()).isEqualTo(200);

    verify(exactly(1), getRequestedFor(urlEqualTo(path1)));
    verify(exactly(1), getRequestedFor(urlEqualTo(path2)));
    verify(exactly(1), getRequestedFor(urlEqualTo(path3)));

  }

  @Test
  public void testGetHttpResponse_ServerRedirectMoreThanRetries_ThrowExceptionWithStatus16()
      throws RDAPHttpException {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    String path1 = "/domain/test1.example";
    String path2 = "/domain/test2.example";
    String path3 = "/domain/test3.example";
    String path4 = "/domain/test4.example";
    String response = "{\"test\": \"value\"}";

    doReturn(true).when(config).check();
    doReturn(10).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create(
        String.format("http://%s:%s%s", wiremockHost, wireMockServer.port(), path1)))
        .when(config).getUri();

    RDAPValidator validator = new RDAPValidator(config);

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
            .withBody(response)));

    assertThatExceptionOfType(RDAPHttpException.class)
        .isThrownBy(validator::getHttpResponse)
        .withMessage(RDAPValidationStatus.TOO_MANY_REDIRECTS.getDescription());

    verify(exactly(1), getRequestedFor(urlEqualTo(path1)));
    verify(exactly(1), getRequestedFor(urlEqualTo(path2)));
    verify(exactly(1), getRequestedFor(urlEqualTo(path3)));
    verify(exactly(0), getRequestedFor(urlEqualTo(path4)));
  }

  // TODO the following tests rely on web resources that may change without notice, should
  // create our own certificates, CRL, etc.

  @Test
  public void testGetHttpResponse_WithHttpsCertificateExpired_ThrowExceptionWithStatus14() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);

    doReturn(true).when(config).check();
    doReturn(100).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create("https://expired.badssl.com")).when(config).getUri();

    RDAPValidator validator = new RDAPValidator(config);

    assertThatExceptionOfType(RDAPHttpException.class)
        .isThrownBy(validator::getHttpResponse)
        .withCauseInstanceOf(IOException.class)
        .withMessage(RDAPValidationStatus.EXPIRED_CERTIFICATE.getDescription());
  }

  @Test
  public void testGetHttpResponse_WithHttpsCertificateRevoked_ThrowExceptionWithStatus13() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);

    doReturn(true).when(config).check();
    doReturn(100).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create("https://revoked.badssl.com")).when(config).getUri();

    RDAPValidator validator = new RDAPValidator(config);

    assertThatExceptionOfType(RDAPHttpException.class)
        .isThrownBy(validator::getHttpResponse)
        .withCauseInstanceOf(IOException.class)
        .withMessage(RDAPValidationStatus.REVOKED_CERTIFICATE.getDescription());
  }

  @Test
  public void testGetHttpResponse_WithHttpsCertificateInvalid_ThrowExceptionWithStatus12() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);

    doReturn(true).when(config).check();
    doReturn(100).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create("https://wrong.host.badssl.com")).when(config).getUri();

    RDAPValidator validator = new RDAPValidator(config);

    assertThatExceptionOfType(RDAPHttpException.class)
        .isThrownBy(validator::getHttpResponse)
        .withCauseInstanceOf(IOException.class)
        .withMessage(RDAPValidationStatus.INVALID_CERTIFICATE.getDescription());
  }

  @Test
  public void testGetHttpResponse_WithHttpsCertificateError_ThrowExceptionWithStatus15() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);

    doReturn(true).when(config).check();
    doReturn(100).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
    doReturn(URI.create("https://untrusted-root.badssl.com")).when(config).getUri();

    RDAPValidator validator = new RDAPValidator(config);

    assertThatExceptionOfType(RDAPHttpException.class)
        .isThrownBy(validator::getHttpResponse)
        .withCauseInstanceOf(IOException.class)
        .withMessage(RDAPValidationStatus.CERTIFICATE_ERROR.getDescription());
  }
}