package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.List;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.SsrfHostBlocklist;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
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
  public void validateTLS12CipherSuites_connectionRefused_returnsFailure() throws Exception {
    doReturn(InetAddress.getByName("127.0.0.1")).when(dnsResolver).getFirstV4Address(anyString());
    DefaultSSLValidator validator = new DefaultSSLValidator(NetworkProtocol.IPv4, dnsResolver);

    CipherValidationResult result = validator.validateTLS12CipherSuites("127.0.0.1", closedPort);

    assertThat(result.isSuccessful()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo("Connection error during TLS 1.2 cipher validation");
    assertThat(result.getException()).isInstanceOf(IOException.class);
    assertThat(result.getProtocol()).isNull();
    assertThat(result.getCipherSuite()).isNull();
  }

  /**
   * TLS 1.2 probing must use the injected resolver (pinned address), never the JDK's own
   * hostname lookup — otherwise it could reach an address the HTTP layer refused.
   */
  @Test
  public void validateTLS12CipherSuites_unresolvable_returnsFailureWithoutConnecting() {
    DefaultSSLValidator validator = new DefaultSSLValidator(NetworkProtocol.IPv4, dnsResolver);

    CipherValidationResult result = validator.validateTLS12CipherSuites("localhost", closedPort);

    assertThat(result.isSuccessful()).isFalse();
    assertThat(result.getErrorMessage()).startsWith("Cannot resolve correct v4 or v6 host address");
    verify(dnsResolver).getFirstV4Address("localhost");
  }

  @Test
  public void validateSSL_ssrfPolicyBlocks_returnsFailureWithoutConnecting() throws Exception {
    doReturn(InetAddress.getByName("127.0.0.1")).when(dnsResolver).getFirstV4Address(anyString());
    DefaultSSLValidator validator = new DefaultSSLValidator(NetworkProtocol.IPv4, dnsResolver,
        (host, addr) -> true);

    SSLValidationResult result = validator.validateSSL("blocked.example", closedPort);

    assertThat(result.isSuccessful()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo("Connection blocked by SSRF protection for blocked.example");
    assertThat(result.getException()).isNull();
  }

  @Test
  public void validateTLS12CipherSuites_ssrfPolicyBlocks_returnsFailureWithoutConnecting() throws Exception {
    doReturn(InetAddress.getByName("::1")).when(dnsResolver).getFirstV6Address(anyString());
    DefaultSSLValidator validator = new DefaultSSLValidator(NetworkProtocol.IPv6, dnsResolver,
        (host, addr) -> addr.isLoopbackAddress());

    CipherValidationResult result = validator.validateTLS12CipherSuites("blocked.example", closedPort);

    assertThat(result.isSuccessful()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo("Connection blocked by SSRF protection for blocked.example");
    assertThat(result.getException()).isNull();
  }

  /** Hostname policy runs before DNS: a blocklisted name is refused without a single lookup. */
  @Test
  public void validateSSL_hostnamePolicyBlocks_returnsFailureWithoutResolving() {
    DefaultSSLValidator validator = new DefaultSSLValidator(NetworkProtocol.IPv4, dnsResolver,
        (host, addr) -> false, SsrfHostBlocklist.of(List.of("*.icann.org"))::isBlocked);

    SSLValidationResult result = validator.validateSSL("rdap.icann.org", closedPort);

    assertThat(result.isSuccessful()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo("Connection blocked by SSRF protection for rdap.icann.org");
    verifyNoInteractions(dnsResolver);
  }

  @Test
  public void validateTLS12CipherSuites_hostnamePolicyBlocks_returnsFailureWithoutResolving() {
    DefaultSSLValidator validator = new DefaultSSLValidator(NetworkProtocol.IPv6, dnsResolver,
        (host, addr) -> false, SsrfHostBlocklist.of(List.of("rdap.icann.org"))::isBlocked);

    CipherValidationResult result = validator.validateTLS12CipherSuites("RDAP.ICANN.ORG.", closedPort);

    assertThat(result.isSuccessful()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo("Connection blocked by SSRF protection for RDAP.ICANN.ORG.");
    verifyNoInteractions(dnsResolver);
  }

  /** The QueryContext-bound validator inherits the context's hostname blocklist and allowlist. */
  @Test
  public void queryContextConstructor_usesContextHostnameBlocklistAndAllowlist() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    doReturn(java.net.URI.create("https://rdap.example.com/domain/test.example")).when(config).getUri();
    QueryContext qctx = QueryContext.forTesting(config);
    qctx.setSsrfProtectionEnabled(true);
    qctx.setSsrfBlockedHosts(List.of("*.icann.invalid"));
    DefaultSSLValidator validator = new DefaultSSLValidator(qctx);

    SSLValidationResult blocked = validator.validateSSL("rdap.icann.invalid", closedPort);
    assertThat(blocked.isSuccessful()).isFalse();
    assertThat(blocked.getErrorMessage()).isEqualTo("Connection blocked by SSRF protection for rdap.icann.invalid");

    // Allowlisted: the hostname gate lets it through and we reach the (unresolvable) DNS step
    qctx.addSsrfAllowedHost("rdap.icann.invalid");
    SSLValidationResult allowed = validator.validateSSL("rdap.icann.invalid", closedPort);
    assertThat(allowed.isSuccessful()).isFalse();
    assertThat(allowed.getErrorMessage()).startsWith("Cannot resolve correct v4 or v6 host address");
  }
}
