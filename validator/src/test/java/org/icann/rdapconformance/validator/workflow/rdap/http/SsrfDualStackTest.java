package org.icann.rdapconformance.validator.workflow.rdap.http;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.List;

import org.icann.rdapconformance.validator.ConnectionStatus;
import org.icann.rdapconformance.validator.DNSCacheResolver;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for the SSRF protocol-scoped check in RDAPHttpRequest.
 *
 * <p>Covers the dual-stack scenario: a hostname that resolves to both a private IPv4
 * address and a public IPv6 address. Verifies that:
 * <ul>
 *   <li>An IPv6 round is NOT blocked (the private IPv4 should be ignored).</li>
 *   <li>An IPv4 round IS blocked (the private IPv4 is in the active family).</li>
 * </ul>
 */
public class SsrfDualStackTest {

    private static final String TEST_HOST = "dual-stack.test.example";
    private static final String REQUEST_PATH = "/domain/test.example";
    private static final String RDAP_RESPONSE = "{\"objectClassName\": \"domain\"}";

    // Private IPv4 (RFC 1918) — should be blocked on IPv4 round
    private static final String PRIVATE_IPv4 = "10.0.0.1";
    // Public IPv6 — should NOT be blocked on IPv6 round
    private static final String PUBLIC_IPv6 = "2001:db8::1";

    private WireMockServer wireMockServer;
    private RDAPValidatorConfiguration config;

    @BeforeMethod
    public void setUp() {
        config = mock(RDAPValidatorConfiguration.class);
        doReturn(10).when(config).getTimeout();
        doReturn(3).when(config).getMaxRedirects();
        doReturn(List.of()).when(config).getSsrfAllowedHosts();

        WireMockConfiguration wmConfig = wireMockConfig().dynamicPort().bindAddress("127.0.0.1");
        wireMockServer = new WireMockServer(wmConfig);
        wireMockServer.start();
    }

    @AfterMethod
    public void tearDown() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    /**
     * IPv6 round with dual-stack host (private IPv4 + public IPv6):
     * the request must NOT be blocked by SSRF protection because only
     * the active address family (IPv6) is evaluated, and the IPv6 address is public.
     *
     * <p>Because we cannot bind to a real IPv6 address in CI, we verify the SSRF
     * decision logic directly via makeRequest and assert the response is not
     * blocked (status != UNKNOWN_HOST / zero status code).
     */
    @Test
    public void ssrf_IPv6Round_DualStackHost_PrivateIPv4_PublicIPv6_IsNotBlocked() throws Exception {
        URI uri = URI.create("http://" + TEST_HOST + ":" + wireMockServer.port() + REQUEST_PATH);
        doReturn(uri).when(config).getUri();

        RDAPValidatorResults results = new RDAPValidatorResultsImpl();
        RDAPDatasetServiceMock datasetService = new RDAPDatasetServiceMock();
        datasetService.download(true);

        QueryContext qctx = QueryContext.forTesting("", results, config, datasetService);
        qctx.setSsrfProtectionEnabled(true);
        qctx.setStackToV6(); // simulate --no-ipv4-queries (IPv6 round)

        // Inject a fake DNS resolver that returns:
        //   A   → 10.0.0.1 (private IPv4)
        //   AAAA → 2001:db8::1 (public IPv6)
        DNSCacheResolver fakeResolver = buildFakeDnsResolver(
                TEST_HOST,
                InetAddress.getByName(PRIVATE_IPv4),   // private IPv4
                InetAddress.getByName(PUBLIC_IPv6)     // public IPv6
        );
        injectDnsResolver(qctx, fakeResolver);

        // The SSRF check must allow the connection (IPv6 address is public).
        // We cannot actually connect to 2001:db8::1 in a unit test, but we can
        // verify the SSRF gate itself does not return UNKNOWN_HOST by inspecting
        // the response connection status.
        HttpResponse<String> response = RDAPHttpRequest.makeRequest(
                qctx, uri, 5, "GET", false, false);

        RDAPHttpRequest.SimpleHttpResponse simple = (RDAPHttpRequest.SimpleHttpResponse) response;

        // SSRF block would produce statusCode=0 + UNKNOWN_HOST.
        // Any other status (including a real network failure to 2001:db8::1) means
        // the SSRF gate was passed — which is the behaviour under test.
        assertThat(simple.getConnectionStatusCode())
                .as("IPv6 round must not be blocked by SSRF due to a private IPv4 sibling address")
                .isNotEqualTo(ConnectionStatus.UNKNOWN_HOST)
                .withFailMessage("Expected the IPv6 connection attempt to pass the SSRF gate, "
                        + "but it was blocked with UNKNOWN_HOST");
    }

    /**
     * IPv4 round with a private IPv4 address:
     * the request MUST be blocked by SSRF protection because the active
     * address family (IPv4) resolves to a private address.
     */
    @Test
    public void ssrf_IPv4Round_PrivateIPv4_IsBlocked() throws Exception {
        URI uri = URI.create("http://" + TEST_HOST + ":" + wireMockServer.port() + REQUEST_PATH);
        doReturn(uri).when(config).getUri();

        RDAPValidatorResults results = new RDAPValidatorResultsImpl();
        RDAPDatasetServiceMock datasetService = new RDAPDatasetServiceMock();
        datasetService.download(true);

        QueryContext qctx = QueryContext.forTesting("", results, config, datasetService);
        qctx.setSsrfProtectionEnabled(true);
        qctx.setStackToV4(); // simulate --no-ipv6-queries (IPv4 round)

        // Inject a fake DNS resolver that returns only a private IPv4
        DNSCacheResolver fakeResolver = buildFakeDnsResolver(
                TEST_HOST,
                InetAddress.getByName(PRIVATE_IPv4), // private IPv4
                null                                  // no IPv6
        );
        injectDnsResolver(qctx, fakeResolver);

        HttpResponse<String> response = RDAPHttpRequest.makeRequest(
                qctx, uri, 5, "GET", false, false);

        RDAPHttpRequest.SimpleHttpResponse simple = (RDAPHttpRequest.SimpleHttpResponse) response;

        assertThat(simple.statusCode())
                .as("IPv4 round with private IPv4 must be blocked (status code 0)")
                .isEqualTo(0);
        assertThat(simple.getConnectionStatusCode())
                .as("IPv4 round with private IPv4 must produce UNKNOWN_HOST")
                .isEqualTo(ConnectionStatus.UNKNOWN_HOST);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Builds a DNSCacheResolver stub that returns the supplied addresses for
     * the given hostname without performing real DNS lookups.
     *
     * @param hostname   the hostname to pre-populate
     * @param v4Address  IPv4 address to return, or null for none
     * @param v6Address  IPv6 address to return, or null for none
     */
    private DNSCacheResolver buildFakeDnsResolver(String hostname,
                                                  InetAddress v4Address,
                                                  InetAddress v6Address) throws Exception {
        // Access the package-private caches via a thin subclass to avoid reflection
        return new DNSCacheResolver() {
            {
                // Seed the internal caches directly
                String fqdn = hostname.endsWith(".") ? hostname : hostname + ".";
                if (v4Address != null) {
                    getCacheV4().put(fqdn, List.of(v4Address));
                } else {
                    getCacheV4().put(fqdn, List.of());
                }
                if (v6Address != null) {
                    getCacheV6().put(fqdn, List.of(v6Address));
                } else {
                    getCacheV6().put(fqdn, List.of());
                }
            }
        };
    }

    /**
     * Injects the given DNSCacheResolver into the QueryContext via reflection,
     * since DNSCacheResolver is a final field.
     */
    private void injectDnsResolver(QueryContext qctx, DNSCacheResolver resolver) throws Exception {
        java.lang.reflect.Field field = QueryContext.class.getDeclaredField("dnsResolver");
        field.setAccessible(true);
        field.set(qctx, resolver);
    }
}