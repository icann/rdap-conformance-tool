package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import org.icann.rdapconformance.validator.DNSCacheResolver;
import org.icann.rdapconformance.validator.NetworkProtocol;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.SSLValidator.CipherValidationResult;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.SSLValidator.SSLValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultSSLValidatorTest {

  private DNSCacheResolver dnsResolver;
  private int closedPort;

  @BeforeEach
  public void setUp() throws IOException {
    dnsResolver = mock(DNSCacheResolver.class);
    // Reserve a port and release it so nothing is listening on it
    try (ServerSocket socket = new ServerSocket(0)) {
      closedPort = socket.getLocalPort();
    }
  }

  @Test
  public void defaultConstructors_doNotFail() {
    assertThat(new DefaultSSLValidator()).isNotNull();
    assertThat(new DefaultSSLValidator(NetworkProtocol.IPv6)).isNotNull();
  }

  @Test
  public void validateSSL_unresolvableHostIpv4_returnsFailure() {
    doReturn(null).when(dnsResolver).getFirstV4Address(anyString());
    DefaultSSLValidator validator = new DefaultSSLValidator(NetworkProtocol.IPv4, dnsResolver);

    SSLValidationResult result = validator.validateSSL("unresolvable.example", 443);

    assertThat(result.isSuccessful()).isFalse();
    assertThat(result.getErrorMessage())
        .isEqualTo("Cannot resolve correct v4 or v6 host address for unresolvable.example");
    assertThat(result.getException()).isNull();
    assertThat(result.getEnabledProtocols()).isNull();
    verify(dnsResolver).getFirstV4Address("unresolvable.example");
  }

  @Test
  public void validateSSL_unresolvableHostIpv6_returnsFailure() {
    doReturn(null).when(dnsResolver).getFirstV6Address(anyString());
    DefaultSSLValidator validator = new DefaultSSLValidator(NetworkProtocol.IPv6, dnsResolver);

    SSLValidationResult result = validator.validateSSL("unresolvable.example", 443);

    assertThat(result.isSuccessful()).isFalse();
    verify(dnsResolver).getFirstV6Address("unresolvable.example");
  }

  @Test
  public void validateSSL_connectionRefusedIpv4_returnsFailureWithException() throws IOException {
    doReturn(InetAddress.getByName("127.0.0.1")).when(dnsResolver).getFirstV4Address(anyString());
    DefaultSSLValidator validator = new DefaultSSLValidator(NetworkProtocol.IPv4, dnsResolver);

    SSLValidationResult result = validator.validateSSL("localhost", closedPort);

    assertThat(result.isSuccessful()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo("Error during SSL connection setup");
    assertThat(result.getException()).isInstanceOf(IOException.class);
  }

  @Test
  public void validateSSL_connectionRefusedIpv6_returnsFailureWithException() throws IOException {
    doReturn(InetAddress.getByName("::1")).when(dnsResolver).getFirstV6Address(anyString());
    DefaultSSLValidator validator = new DefaultSSLValidator(NetworkProtocol.IPv6, dnsResolver);

    SSLValidationResult result = validator.validateSSL("localhost", closedPort);

    assertThat(result.isSuccessful()).isFalse();
    assertThat(result.getException()).isInstanceOf(IOException.class);
  }

  @Test
  public void validateSSL_plainTextServer_handshakeFails() throws IOException {
    doReturn(InetAddress.getByName("127.0.0.1")).when(dnsResolver).getFirstV4Address(anyString());
    DefaultSSLValidator validator = new DefaultSSLValidator(NetworkProtocol.IPv4, dnsResolver);

    try (ServerSocket plainServer = new ServerSocket(0)) {
      Thread responder = new Thread(() -> {
        try (var client = plainServer.accept()) {
          client.getOutputStream().write("not tls\n".getBytes());
          client.getOutputStream().flush();
        } catch (IOException ignored) {
          // server closed
        }
      });
      responder.setDaemon(true);
      responder.start();

      SSLValidationResult result = validator.validateSSL("localhost", plainServer.getLocalPort());

      assertThat(result.isSuccessful()).isFalse();
      assertThat(result.getErrorMessage()).isEqualTo("Error during SSL connection setup");
      assertThat(result.getException()).isInstanceOf(IOException.class);
    }
  }

  @Test
  public void validateTLS12CipherSuites_connectionRefused_returnsFailure() {
    DefaultSSLValidator validator = new DefaultSSLValidator(NetworkProtocol.IPv4, dnsResolver);

    CipherValidationResult result = validator.validateTLS12CipherSuites("127.0.0.1", closedPort);

    assertThat(result.isSuccessful()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo("Connection error during TLS 1.2 cipher validation");
    assertThat(result.getException()).isInstanceOf(IOException.class);
    assertThat(result.getProtocol()).isNull();
    assertThat(result.getCipherSuite()).isNull();
  }
}
