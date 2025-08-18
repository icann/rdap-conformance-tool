package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.mockito.Mockito;

/**
 * Focused security test for validHrefUri method to achieve 95%+ coverage
 * This test directly tests the public validHrefUri method without inheritance issues
 */
public class ResponseValidation2Dot4Dot6_2024_UriSecurityTest {

    private ResponseValidation2Dot4Dot6_2024 validation;
    private RDAPValidatorResultsImpl results;

    @BeforeMethod
    public void setup() {
        results = RDAPValidatorResultsImpl.getInstance();
        results.clear();
        
        // Create validation instance
        String mockJson = "{}";
        RDAPDatasetServiceMock datasets = new RDAPDatasetServiceMock();
        RDAPQueryType queryType = RDAPQueryType.DOMAIN;
        RDAPValidatorConfiguration config = Mockito.mock(RDAPValidatorConfiguration.class);
        
        validation = new ResponseValidation2Dot4Dot6_2024(mockJson, results, datasets, queryType, config);
    }

    // Null and empty input tests

    @Test
    public void testValidHrefUri_NullInput() {
        boolean result = validation.validHrefUri(null, "/test");
        assertThat(result).isFalse();
        assertThat(results.getAll()).hasSize(1);
        assertThat(results.getAll().iterator().next().getCode()).isEqualTo(-47703);
    }

    @Test
    public void testValidHrefUri_EmptyInput() {
        boolean result = validation.validHrefUri("", "/test");
        assertThat(result).isFalse();
        assertThat(results.getAll()).hasSize(1);
        assertThat(results.getAll().iterator().next().getCode()).isEqualTo(-47703);
    }

    @Test
    public void testValidHrefUri_WhitespaceInput() {
        boolean result = validation.validHrefUri("   \\t\\n   ", "/test");
        assertThat(result).isFalse();
        assertThat(results.getAll()).hasSize(1);
        assertThat(results.getAll().iterator().next().getCode()).isEqualTo(-47703);
    }

    // Uri syntax tests (step 1 - equivalent to -10400)

    @Test
    public void testValidHrefUri_InvalidSyntax() {
        String[] invalidUris = {
            "not-a-uri",
            "://example.com",
            "http://",
            "http:///path",
            ":",
            ":/",
            "://",
            ":///",
        };

        for (String invalidUri : invalidUris) {
            results.clear();
            boolean result = validation.validHrefUri(invalidUri, "/test");
            assertThat(result).as("Should reject: " + invalidUri).isFalse();
            assertThat(results.getAll()).hasSize(1);
            assertThat(results.getAll().iterator().next().getCode()).isEqualTo(-47703);
        }
    }

    // Scheme validation tests (step 2 - equivalent to -10401)

    @Test
    public void testValidHrefUri_InvalidSchemes() {
        String[] invalidSchemes = {
            "ftp://example.com",
            "ldap://example.com",
            "mailto://user@example.com",
            "file://path/to/file",
            "hpsps://example.com",  // Original user's typo
            "javascript://alert(1)", 
            "data://text/html,<script>",
        };

        for (String invalidScheme : invalidSchemes) {
            results.clear();
            boolean result = validation.validHrefUri(invalidScheme, "/test");
            assertThat(result).as("Should reject scheme: " + invalidScheme).isFalse();
            assertThat(results.getAll()).hasSize(1);
            assertThat(results.getAll().iterator().next().getCode()).isEqualTo(-47703);
        }
    }

    @Test
    public void testValidHrefUri_ValidSchemes() {
        String[] validSchemes = {
            "http://example.com",
            "https://example.com",
            "HTTP://EXAMPLE.COM",    // Case insensitive
            "HTTPS://EXAMPLE.COM",   // Case insensitive
            "Http://Example.Com",    // Mixed case
            "Https://Example.Com",   // Mixed case
        };

        for (String validScheme : validSchemes) {
            results.clear();
            boolean result = validation.validHrefUri(validScheme, "/test");
            assertThat(result).as("Should accept scheme: " + validScheme).isTrue();
            assertThat(results.getAll()).hasSize(0);
        }
    }

    // Host validation tests (step 3 - equivalent to -10402)

    @Test
    public void testValidHrefUri_InvalidHosts() {
        String[] invalidHosts = {
            "https://",                    // no host
            "https:///path",              // empty host
            "https:// ",                  // space host
            "https://\\t",                // tab host
            "https://host with spaces.com", // spaces
            "https://.example.com",       // starts with dot
            "https://example.com.",       // ends with dot
            "https://double..dots.com",   // double dots
            "https://triple...dots.com",  // triple dots
        };

        for (String invalidHost : invalidHosts) {
            results.clear();
            boolean result = validation.validHrefUri(invalidHost, "/test");
            assertThat(result).as("Should reject host: " + invalidHost).isFalse();
            assertThat(results.getAll()).hasSize(1);
            assertThat(results.getAll().iterator().next().getCode()).isEqualTo(-47703);
        }
    }

    @Test
    public void testValidHrefUri_ValidHosts() {
        String[] validHosts = {
            "https://example.com",
            "https://www.example.com",
            "https://sub.domain.example.com",
            "https://192.168.1.1",           // IPv4
            "https://127.0.0.1",             // Loopback
            "https://[::1]",                 // IPv6 loopback
            "https://[2001:db8::1]",         // IPv6
            "https://example.com:8080",      // with port
            "https://localhost",             // localhost
            "https://a.b",                   // minimal domain
        };

        for (String validHost : validHosts) {
            results.clear();
            boolean result = validation.validHrefUri(validHost, "/test");
            assertThat(result).as("Should accept host: " + validHost).isTrue();
            assertThat(results.getAll()).hasSize(0);
        }
    }

    // Security vulnerability tests

    @Test
    public void testValidHrefUri_SecurityAttacks() {
        String[] securityAttacks = {
            "javascript://alert(1)",
            "data:text/html,<script>alert(1)</script>",
            "vbscript://msgbox(1)",
            "file:///etc/passwd",
            "file://C:\\\\Windows\\\\System32",
        };

        for (String attack : securityAttacks) {
            results.clear();
            boolean result = validation.validHrefUri(attack, "/test");
            assertThat(result).as("Should block security attack: " + attack).isFalse();
            assertThat(results.getAll()).hasSize(1);
            assertThat(results.getAll().iterator().next().getCode()).isEqualTo(-47703);
        }
    }

    // Edge cases and null scheme protection

    @Test
    public void testValidHrefUri_NullSchemeProtection() {
        // Test edge cases that might create null schemes
        String[] edgeCases = {
            "",
            "   ",
            ":",
            ":/",
            "://",
            ":///",
        };

        for (String edgeCase : edgeCases) {
            results.clear();
            boolean result = validation.validHrefUri(edgeCase, "/test");
            assertThat(result).as("Should handle edge case: '" + edgeCase + "'").isFalse();
            assertThat(results.getAll()).hasSize(1);
            assertThat(results.getAll().iterator().next().getCode()).isEqualTo(-47703);
        }
    }

    @Test
    public void testValidHrefUri_ComplexValidCases() {
        // Test complex but valid URIs to ensure we don't over-restrict
        String[] complexValid = {
            "https://example.com/path/to/resource",
            "https://example.com:443/secure/path",
            "http://example.com:80/standard/path",
            "https://example.com/path?query=value&other=123",
            "https://example.com/path#fragment",
            "https://user:pass@example.com/authenticated",
            "https://example.com/path%20with%20encoded%20spaces",
            "https://subdomain.example.com/deep/path/structure",
        };

        for (String complexUri : complexValid) {
            results.clear();
            boolean result = validation.validHrefUri(complexUri, "/test");
            assertThat(result).as("Should accept complex valid URI: " + complexUri).isTrue();
            assertThat(results.getAll()).hasSize(0);
        }
    }

    // Performance and boundary tests

    @Test
    public void testValidHrefUri_LongInput() {
        // Test with very long input to check for performance issues
        String longHost = "sub." + "domain.".repeat(50) + "example.com";
        String longPath = "/path/" + "segment/".repeat(100);
        String longUri = "https://" + longHost + longPath;
        
        boolean result = validation.validHrefUri(longUri, "/test");
        assertThat(result).isTrue();
        assertThat(results.getAll()).hasSize(0);
    }

    @Test 
    public void testValidHrefUri_HostEdgeCases() {
        // Test specific host validation edge cases to improve coverage
        String[] hostEdgeCases = {
            "https://host..double.dots.com",    // Double dots in middle
            "https://host...triple.dots.com",   // Triple dots
            "https://..start.with.dots.com",    // Start with dots
            "https://end.with.dots..com",       // End with dots
            "https://host .with.space.com",     // Space in host
            "https://host\\backslash.com",      // Backslash in host
        };

        for (String hostEdgeCase : hostEdgeCases) {
            results.clear();
            boolean result = validation.validHrefUri(hostEdgeCase, "/test");
            assertThat(result).as("Should reject host edge case: " + hostEdgeCase).isFalse();
            assertThat(results.getAll()).hasSize(1);
            assertThat(results.getAll().iterator().next().getCode()).isEqualTo(-47703);
        }
    }

    // Specific coverage improvement tests

    @Test
    public void testValidHrefUri_SpecificCoverageTests() {
        // These tests target specific lines/branches that might be missed
        
        // Test 1: URI with null host (should be caught in step 1, but test step 3 logic)
        results.clear();
        boolean result1 = validation.validHrefUri("https://", "/test");
        assertThat(result1).isFalse();
        assertThat(results.getAll()).hasSize(1);
        
        // Test 2: Host that becomes null/empty after trim
        results.clear();
        boolean result2 = validation.validHrefUri("https://   ", "/test");
        assertThat(result2).isFalse();
        assertThat(results.getAll()).hasSize(1);
        
        // Test 3: Valid minimal cases
        results.clear();
        boolean result3 = validation.validHrefUri("http://a", "/test");
        assertThat(result3).isTrue();
        assertThat(results.getAll()).hasSize(0);
        
        // Test 4: Case sensitivity in scheme
        results.clear();
        boolean result4 = validation.validHrefUri("HTTPS://EXAMPLE.COM", "/test");
        assertThat(result4).isTrue();
        assertThat(results.getAll()).hasSize(0);
    }
}