package org.icann.rdapconformance.validator.workflow.rdap;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.net.URI;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class HttpTestingUtils {

  protected final static String REQUEST_PATH = "/domain/test.example";
  protected final static String RDAP_RESPONSE = "{\"objectClassName\": \"domain\"}";

  protected final static String WIREMOCK_HOST = "localhost";
  protected final RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
  protected WireMockServer wireMockServer;


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
}
