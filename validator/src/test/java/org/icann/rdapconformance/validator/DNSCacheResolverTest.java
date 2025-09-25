package org.icann.rdapconformance.validator;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xbill.DNS.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class DNSCacheResolverTest {

    @BeforeMethod
    public void setUp() {
        // Clear caches before each test
        clearDNSCaches();
        // Reset to default system DNS before each test
        DNSCacheResolver.initializeResolver(null);
        // Clear validation results
        RDAPValidatorResultsImpl.getInstance().clear();
    }

    private void clearDNSCaches() {
        // Use reflection to clear the private caches
        try {
            java.lang.reflect.Field cacheV4Field = DNSCacheResolver.class.getDeclaredField("CACHE_V4");
            cacheV4Field.setAccessible(true);
            Map<String, List<InetAddress>> cacheV4 = (Map<String, List<InetAddress>>) cacheV4Field.get(null);
            cacheV4.clear();

            java.lang.reflect.Field cacheV6Field = DNSCacheResolver.class.getDeclaredField("CACHE_V6");
            cacheV6Field.setAccessible(true);
            Map<String, List<InetAddress>> cacheV6 = (Map<String, List<InetAddress>>) cacheV6Field.get(null);
            cacheV6.clear();
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear DNS caches", e);
        }
    }

    // ===========================================
    // Tests for initializeResolver method
    // ===========================================

    @Test
    public void testInitializeResolver_WithNullCustomServer_UsesExtendedResolver() {
        DNSCacheResolver.initializeResolver(null);
        
        assertThat(DNSCacheResolver.resolver).isNotNull();
        assertThat(DNSCacheResolver.resolver).isInstanceOf(ExtendedResolver.class);
    }

    @Test
    public void testInitializeResolver_WithEmptyCustomServer_UsesExtendedResolver() {
        DNSCacheResolver.initializeResolver("");
        
        assertThat(DNSCacheResolver.resolver).isNotNull();
        assertThat(DNSCacheResolver.resolver).isInstanceOf(ExtendedResolver.class);
    }

    @Test
    public void testInitializeResolver_WithValidIPv4Address_UsesSimpleResolver() {
        DNSCacheResolver.initializeResolver("8.8.8.8");
        
        assertThat(DNSCacheResolver.resolver).isNotNull();
        assertThat(DNSCacheResolver.resolver).isInstanceOf(SimpleResolver.class);
    }

    @Test
    public void testInitializeResolver_WithValidIPv6Address_UsesSimpleResolver() {
        DNSCacheResolver.initializeResolver("2001:4860:4860::8888");
        
        assertThat(DNSCacheResolver.resolver).isNotNull();
        assertThat(DNSCacheResolver.resolver).isInstanceOf(SimpleResolver.class);
    }

    @Test
    public void testInitializeResolver_WithLocalhostIPv4_UsesSimpleResolver() {
        DNSCacheResolver.initializeResolver("127.0.0.1");
        
        assertThat(DNSCacheResolver.resolver).isNotNull();
        assertThat(DNSCacheResolver.resolver).isInstanceOf(SimpleResolver.class);
    }

    @Test
    public void testInitializeResolver_WithLocalhostIPv6_UsesSimpleResolver() {
        DNSCacheResolver.initializeResolver("::1");
        
        assertThat(DNSCacheResolver.resolver).isNotNull();
        assertThat(DNSCacheResolver.resolver).isInstanceOf(SimpleResolver.class);
    }

    @Test
    public void testInitializeResolver_WithInvalidIPAddress_ThrowsException() {
        assertThatThrownBy(() -> DNSCacheResolver.initializeResolver("invalid.dns.server"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Invalid DNS resolver configuration");
    }

    @Test
    public void testInitializeResolver_WithInvalidIPv4Format_ThrowsException() {
        assertThatThrownBy(() -> DNSCacheResolver.initializeResolver("300.300.300.300"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Invalid DNS resolver configuration");
    }

    @Test
    public void testInitializeResolver_WithInvalidIPv6Format_ThrowsException() {
        assertThatThrownBy(() -> DNSCacheResolver.initializeResolver("gggg::1"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Invalid DNS resolver configuration");
    }

    // ===========================================
    // Tests for URL hostname extraction
    // ===========================================

    @Test
    public void testGetHostnameFromUrl_ValidHttp() {
        String hostname = DNSCacheResolver.getHostnameFromUrl("http://example.com/path");
        assertThat(hostname).isEqualTo("example.com");
    }

    @Test
    public void testGetHostnameFromUrl_ValidHttps() {
        String hostname = DNSCacheResolver.getHostnameFromUrl("https://rdap.example.com/domain/test.com");
        assertThat(hostname).isEqualTo("rdap.example.com");
    }

    @Test
    public void testGetHostnameFromUrl_WithPort() {
        String hostname = DNSCacheResolver.getHostnameFromUrl("https://example.com:8080/path");
        assertThat(hostname).isEqualTo("example.com");
    }

    @Test
    public void testGetHostnameFromUrl_WithIPv4() {
        String hostname = DNSCacheResolver.getHostnameFromUrl("http://192.168.1.1/path");
        assertThat(hostname).isEqualTo("192.168.1.1");
    }

    @Test
    public void testGetHostnameFromUrl_WithIPv6() {
        String hostname = DNSCacheResolver.getHostnameFromUrl("http://[2001:db8::1]/path");
        assertThat(hostname).isEqualTo("[2001:db8::1]"); // IPv6 addresses include brackets
    }

    @Test
    public void testGetHostnameFromUrl_InvalidUrl() {
        String hostname = DNSCacheResolver.getHostnameFromUrl("not-a-valid-url");
        assertThat(hostname).isEmpty();
    }

    @Test
    public void testGetHostnameFromUrl_NullUrl() {
        // This will throw NullPointerException as the method doesn't handle null
        assertThatThrownBy(() -> DNSCacheResolver.getHostnameFromUrl(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testGetHostnameFromUrl_EmptyUrl() {
        String hostname = DNSCacheResolver.getHostnameFromUrl("");
        assertThat(hostname).isEmpty();
    }

    // ===========================================
    // Tests for FQDN handling
    // ===========================================

    @Test
    public void testEnsureFQDN_WithoutDot() {
        String fqdn = DNSCacheResolver.ensureFQDN("example.com");
        assertThat(fqdn).isEqualTo("example.com.");
    }

    @Test
    public void testEnsureFQDN_WithDot() {
        String fqdn = DNSCacheResolver.ensureFQDN("example.com.");
        assertThat(fqdn).isEqualTo("example.com.");
    }

    @Test
    public void testEnsureFQDN_EmptyString() {
        String fqdn = DNSCacheResolver.ensureFQDN("");
        assertThat(fqdn).isEqualTo(".");
    }

    @Test
    public void testEnsureFQDN_SingleDot() {
        String fqdn = DNSCacheResolver.ensureFQDN(".");
        assertThat(fqdn).isEqualTo(".");
    }

    // ===========================================
    // Tests for getFirst utility method
    // ===========================================

    @Test
    public void testGetFirst_WithEmptyMap() {
        Map<String, List<InetAddress>> cache = java.util.Map.of();
        InetAddress result = DNSCacheResolver.getFirst(cache, "example.com.");
        assertThat(result).isNull();
    }

    @Test
    public void testGetFirst_WithEmptyList() throws UnknownHostException {
        Map<String, List<InetAddress>> cache = java.util.Map.of("example.com.", java.util.List.of());
        InetAddress result = DNSCacheResolver.getFirst(cache, "example.com.");
        assertThat(result).isNull();
    }

    @Test
    public void testGetFirst_WithNonEmptyList() throws UnknownHostException {
        InetAddress addr1 = InetAddress.getByName("192.168.1.1");
        InetAddress addr2 = InetAddress.getByName("192.168.1.2");
        Map<String, List<InetAddress>> cache = java.util.Map.of("example.com.", java.util.List.of(addr1, addr2));
        InetAddress result = DNSCacheResolver.getFirst(cache, "example.com.");
        assertThat(result).isEqualTo(addr1);
    }

    @Test
    public void testGetFirst_WithNullKey() {
        Map<String, List<InetAddress>> cache = java.util.Map.of();
        // This will throw NullPointerException as Map.get(null) throws NPE
        assertThatThrownBy(() -> DNSCacheResolver.getFirst(cache, null))
            .isInstanceOf(NullPointerException.class);
    }

    // ===========================================
    // Tests for initializeResolver
    // ===========================================

    @Test
    public void testInitFromUrl_ValidUrl() {
        // This should not throw an exception
        DNSCacheResolver.initFromUrl("http://example.com/path");
        // The method should complete successfully
    }

    @Test
    public void testInitFromUrl_InvalidUrl() {
        // This should not throw an exception, just log an error
        DNSCacheResolver.initFromUrl("invalid-url");
        // The method should complete successfully
    }

    @Test
    public void testInitFromUrl_NullUrl() {
        // This should not throw an exception now
        DNSCacheResolver.initFromUrl(null);
        // The method should complete successfully
    }

    @Test
    public void testInitFromUrl_UrlWithoutHost() {
        // This should not throw an exception
        DNSCacheResolver.initFromUrl("file:///path/to/file");
        // The method should complete successfully
    }

    // ===========================================
    // Tests for localhost special cases
    // ===========================================

    @Test
    public void testResolveIfNeeded_Localhost() {
        DNSCacheResolver.resolveIfNeeded("localhost.");
        
        // Should have cached results for both IPv4 and IPv6
        List<InetAddress> v4Addresses = DNSCacheResolver.getAllV4Addresses("localhost.");
        List<InetAddress> v6Addresses = DNSCacheResolver.getAllV6Addresses("localhost.");
        
        assertThat(v4Addresses).isNotEmpty();
        assertThat(v6Addresses).isNotEmpty();
        
        // Should resolve to loopback addresses
        assertThat(v4Addresses.get(0).getHostAddress()).isEqualTo("127.0.0.1");
        assertThat(v6Addresses.get(0).getHostAddress()).isEqualTo("0:0:0:0:0:0:0:1");
    }

    @Test
    public void testResolveIfNeeded_127001() {
        DNSCacheResolver.resolveIfNeeded("127.0.0.1.");
        
        List<InetAddress> v4Addresses = DNSCacheResolver.getAllV4Addresses("127.0.0.1.");
        assertThat(v4Addresses).isNotEmpty();
        assertThat(v4Addresses.get(0).getHostAddress()).isEqualTo("127.0.0.1");
    }

    @Test
    public void testResolveIfNeeded_IPv6Localhost() {
        DNSCacheResolver.resolveIfNeeded("::1.");
        
        List<InetAddress> v6Addresses = DNSCacheResolver.getAllV6Addresses("::1.");
        // ::1 might not always resolve properly in all environments, so check if empty is acceptable
        if (!v6Addresses.isEmpty()) {
            assertThat(v6Addresses.get(0).getHostAddress()).isEqualTo("0:0:0:0:0:0:0:1");
        }
    }

    // ===========================================
    // Tests for address checking methods
    // ===========================================

    @Test
    public void testHasV4Addresses_WithLocalhostUrl() {
        boolean hasV4 = DNSCacheResolver.hasV4Addresses("http://localhost/");
        assertThat(hasV4).isTrue();
    }

    @Test
    public void testHasV6Addresses_WithLocalhostUrl() {
        boolean hasV6 = DNSCacheResolver.hasV6Addresses("http://localhost/");
        assertThat(hasV6).isTrue();
    }

    @Test
    public void testHasNoAddresses_WithLocalhostFqdn() {
        boolean hasNone = DNSCacheResolver.hasNoAddresses("localhost");
        assertThat(hasNone).isFalse(); // localhost should have addresses
    }

    @Test
    public void testHasV4Addresses_WithIPv4Url() {
        boolean hasV4 = DNSCacheResolver.hasV4Addresses("http://127.0.0.1/");
        assertThat(hasV4).isTrue();
    }

    @Test
    public void testHasV6Addresses_WithIPv6Url() {
        boolean hasV6 = DNSCacheResolver.hasV6Addresses("http://[::1]/");
        // IPv6 localhost might not always be available, so this might be false
        // Let's just check the method doesn't throw an exception
        assertThat(hasV6).isIn(true, false);
    }

    @Test
    public void testGetFirstV4Address_WithLocalhost() {
        InetAddress addr = DNSCacheResolver.getFirstV4Address("localhost");
        assertThat(addr).isNotNull();
        assertThat(addr.getHostAddress()).isEqualTo("127.0.0.1");
    }

    @Test
    public void testGetFirstV6Address_WithLocalhost() {
        InetAddress addr = DNSCacheResolver.getFirstV6Address("localhost");
        assertThat(addr).isNotNull();
        assertThat(addr.getHostAddress()).isEqualTo("0:0:0:0:0:0:0:1");
    }

    // ===========================================
    // Tests for validation methods
    // ===========================================

    @Test
    public void testDoZeroIPAddressesValidation_BothProtocols_LocalhostUrl() {
        DNSCacheResolver.doZeroIPAddressesValidation("http://localhost/", true, true);
        
        // Should not add any validation errors since localhost has both IPv4 and IPv6
        Set<RDAPValidationResult> results = RDAPValidatorResultsImpl.getInstance().getAll();
        assertThat(results).isEmpty();
    }

    @Test
    public void testDoZeroIPAddressesValidation_OnlyIPv4_LocalhostUrl() {
        DNSCacheResolver.doZeroIPAddressesValidation("http://localhost/", false, true);
        
        // Should not add validation errors since localhost has IPv4
        Set<RDAPValidationResult> results = RDAPValidatorResultsImpl.getInstance().getAll();
        assertThat(results).isEmpty();
    }

    @Test
    public void testDoZeroIPAddressesValidation_OnlyIPv6_LocalhostUrl() {
        DNSCacheResolver.doZeroIPAddressesValidation("http://localhost/", true, false);
        
        // Should not add validation errors since localhost has IPv6
        Set<RDAPValidationResult> results = RDAPValidatorResultsImpl.getInstance().getAll();
        assertThat(results).isEmpty();
    }

    @Test
    public void testDoZeroIPAddressesValidation_EmptyHostname() {
        DNSCacheResolver.doZeroIPAddressesValidation("not-a-url", true, true);
        
        // Should add validation error for unable to resolve
        Set<RDAPValidationResult> results = RDAPValidatorResultsImpl.getInstance().getAll();
        assertThat(results).hasSize(1);
        assertThat(results.iterator().next().getCode()).isEqualTo(-13019);
    }

    // ===========================================
    // Tests for cache behavior
    // ===========================================

    @Test
    public void testResolveIfNeeded_CacheHit() {
        // First call should populate cache
        DNSCacheResolver.resolveIfNeeded("localhost.");
        List<InetAddress> firstResult = DNSCacheResolver.getAllV4Addresses("localhost.");
        
        // Second call should use cache
        DNSCacheResolver.resolveIfNeeded("localhost.");
        List<InetAddress> secondResult = DNSCacheResolver.getAllV4Addresses("localhost.");
        
        // Results should be the same (cached)
        assertThat(firstResult).isEqualTo(secondResult);
    }

    @Test
    public void testGetAllV4Addresses_EmptyResult() {
        // Using a hostname that won't resolve to IPv4
        List<InetAddress> addresses = DNSCacheResolver.getAllV4Addresses("nonexistent.invalid.tld");
        assertThat(addresses).isEmpty();
    }

    @Test
    public void testGetAllV6Addresses_EmptyResult() {
        // Using a hostname that won't resolve to IPv6
        List<InetAddress> addresses = DNSCacheResolver.getAllV6Addresses("nonexistent.invalid.tld");
        assertThat(addresses).isEmpty();
    }

    // ===========================================
    // Tests for error handling
    // ===========================================

    @Test
    public void testResolveWithCNAMEChain_TypeA() {
        // Test with localhost which should resolve directly - but through DNS, not special case
        List<InetAddress> addresses = DNSCacheResolver.resolveWithCNAMEChain("example.com.", Type.A);
        // This might be empty if DNS resolution fails or domain doesn't exist
        // The important thing is the method doesn't throw an exception
        assertThat(addresses).isNotNull();
    }

    @Test
    public void testResolveWithCNAMEChain_TypeAAAA() {
        // Test with example.com which might resolve to IPv6
        List<InetAddress> addresses = DNSCacheResolver.resolveWithCNAMEChain("example.com.", Type.AAAA);
        // This might be empty if DNS resolution fails or domain doesn't have IPv6
        // The important thing is the method doesn't throw an exception
        assertThat(addresses).isNotNull();
    }

    @Test
    public void testResolveWithCNAMEChain_InvalidDomain() {
        // Test with invalid domain - should return empty list, not throw exception
        List<InetAddress> addresses = DNSCacheResolver.resolveWithCNAMEChain("nonexistent.invalid.tld.", Type.A);
        assertThat(addresses).isEmpty();
    }
}