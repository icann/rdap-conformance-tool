package org.icann.rdapconformance.validator;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import org.icann.rdapconformance.validator.RDAPValidator.RDAPHttpException;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
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
  public void setUp() {
    String keyStorePath = this.getClass().getResource("/mykeystore/ca-cert.jks").toString();
    wireMockServer = new WireMockServer(wireMockConfig()
        .dynamicHttpsPort()
        .bindAddress(wiremockHost)
        .keystorePath(keyStorePath)
        .keystorePassword("rdapct")
        .keyManagerPassword("rdapct"));
    wireMockServer.start();
  }

  @AfterMethod
  public void tearDown() {
    wireMockServer.stop();
  }

  @Test
  public void testGetHttpResponse_WithSelfSignedHttps() throws RDAPHttpException {
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
}