package org.icann.rdapconformance.validator.workflow.rdap;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class HttpTestingUtils {

  protected final static String REQUEST_PATH = "/domain/test.example";
  protected final static String RDAP_RESPONSE = "{\"objectClassName\": \"domain\"}";

  protected final static String WIREMOCK_HOST = "localhost";
  protected final RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
  protected WireMockServer wireMockServer;

  public static RedirectData givenChainedHttpRedirects() {
    String path1 = "https://domain1/test1.example";
    String path2 = "https://domain2/test2.example";
    String path3 = "http://domain3/test3.example";
    HttpResponse<String> httpsResponse1 = mock(HttpResponse.class);
    HttpResponse<String> httpsResponse2 = mock(HttpResponse.class);
    HttpResponse<String> httpsResponse3 = mock(HttpResponse.class);

    // prepare chained HTTP response with one HTTP redirect
    doReturn(URI.create(path1)).when(httpsResponse1).uri();
    doReturn(URI.create(path2)).when(httpsResponse2).uri();
    doReturn(URI.create(path3)).when(httpsResponse3).uri();
    doReturn(Optional.of(httpsResponse2)).when(httpsResponse1).previousResponse();
    doReturn(Optional.of(httpsResponse3)).when(httpsResponse2).previousResponse();
    doReturn(HttpHeaders.of(Map.of("Access-Control-Allow-Origin", List.of("*")), (f1, f2) -> true))
        .when(httpsResponse1).headers();
    doReturn(HttpHeaders.of(Map.of("Access-Control-Allow-Origin", List.of("*")), (f1, f2) -> true))
        .when(httpsResponse2).headers();

    return new RedirectData(httpsResponse1, httpsResponse3);
  }

  @BeforeMethod
  public void setUp() {
    doReturn(10).when(config).getTimeout();
    doReturn(3).when(config).getMaxRedirects();
  }

  @AfterMethod
  public void tearDown() {
    if (null != wireMockServer && wireMockServer.isRunning()) {
      wireMockServer.stop();
    }
    System.clearProperty("javax.net.ssl.trustStore");
    System.clearProperty("javax.net.ssl.trustStorePassword");
    System.clearProperty("javax.net.ssl.trustStoreType");
  }

  protected void prepareWiremock(WireMockConfiguration wmConfig) {
    wireMockServer = new WireMockServer(wmConfig);
    wireMockServer.start();
  }

  protected void setHttpsTrustStore(WireMockConfiguration wmConfig) {
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

  protected void givenUri(String scheme) {
    givenUri(scheme, REQUEST_PATH);
  }

  protected void givenUri(String scheme, String path) {
    int port = scheme.equals("https") ? wireMockServer.httpsPort() : wireMockServer.port();
    configureFor(scheme, WIREMOCK_HOST, port);
    doReturn(URI.create(
        String.format(scheme + "://%s:%s%s", WIREMOCK_HOST, port, path)))
        .when(config).getUri();
  }

  public static class RedirectData {

    public HttpResponse<String> endingResponse;
    public HttpResponse<String> startingResponse;

    public RedirectData(HttpResponse<String> startingResponse,
        HttpResponse<String> endingResponse) {
      this.endingResponse = endingResponse;
      this.startingResponse = startingResponse;
    }
  }
}
