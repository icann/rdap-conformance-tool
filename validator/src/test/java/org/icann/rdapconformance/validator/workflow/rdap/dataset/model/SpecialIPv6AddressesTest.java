package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SpecialIPv6AddressesTest extends BaseUnmarshallingTest<SpecialIPv6Addresses> {
    private SpecialIPv6Addresses ipAddressSpecialRegistry;

    @BeforeMethod
    public void setUp() {
        this.ipAddressSpecialRegistry = unmarshal("/dataset/iana-ipv6-special-registry.xml", SpecialIPv6Addresses.class);
    }

    @Test
    public void givenIpAddressSpecialRegistry_whenUnmarshalling_thenSpecialIPv4AddressesAndSpecialIPv6AddressesAreUnmarshalled() {
        assertThat(ipAddressSpecialRegistry.getValues()).hasSize(21);
        assertThat(ipAddressSpecialRegistry.getValues()).contains("2001::/32",
                "2001:db8::/32",
                "100::/64",
                "::ffff:0:0/96",
                "2001:10::/28",
                "::/128",
                "2001:4:112::/48",
                "fc00::/7",
                "2001:3::/32",
                "2001:1::2/128",
                "64:ff9b::/96",
                "2620:4f:8000::/48",
                "2002::/16 ",
                "::1/128",
                "2001::/23",
                "2001:2::/48",
                "fe80::/10",
                "2001:30::/28 ",
                "64:ff9b:1::/48",
                "2001:1::1/128",
                "2001:20::/28");
    }

    @Test
    public void testIsInvalid_specialPurposeAddresses() {
        // Test addresses that SHOULD be invalid (return true) because they're in special-purpose registry
        
        // Documentation addresses (2001:db8::/32)
        assertThat(ipAddressSpecialRegistry.isInvalid("2001:db8::1"))
            .as("2001:db8::1 should be invalid (documentation address)")
            .isTrue();
        assertThat(ipAddressSpecialRegistry.isInvalid("2001:db8:1234:5678::abcd"))
            .as("2001:db8:1234:5678::abcd should be invalid (documentation address)")
            .isTrue();
        
        // Loopback (::1/128)
        assertThat(ipAddressSpecialRegistry.isInvalid("::1"))
            .as("::1 should be invalid (loopback)")
            .isTrue();
        
        // Link-Local (fe80::/10)
        assertThat(ipAddressSpecialRegistry.isInvalid("fe80::1"))
            .as("fe80::1 should be invalid (link-local)")
            .isTrue();
        assertThat(ipAddressSpecialRegistry.isInvalid("febf:ffff:ffff:ffff:ffff:ffff:ffff:ffff"))
            .as("febf:ffff:ffff:ffff:ffff:ffff:ffff:ffff should be invalid (link-local)")
            .isTrue();
        
        // IETF Protocol Assignments (2001::/32 but more specific than 2001::/23)
        assertThat(ipAddressSpecialRegistry.isInvalid("2001::1"))
            .as("2001::1 should be invalid (IETF protocol assignments)")
            .isTrue();
        
        // Unique Local (fc00::/7) - also in special registry
        assertThat(ipAddressSpecialRegistry.isInvalid("fc00::1"))
            .as("fc00::1 should be invalid (unique local)")
            .isTrue();
        assertThat(ipAddressSpecialRegistry.isInvalid("fdff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"))
            .as("fdff:ffff:ffff:ffff:ffff:ffff:ffff:ffff should be invalid (unique local)")
            .isTrue();
    }
    
    @Test
    public void testIsInvalid_regularGlobalUnicast() {
        // Test addresses that should be valid (return false) because they're NOT in special-purpose registry
        // These are regular Global Unicast addresses
        
        assertThat(ipAddressSpecialRegistry.isInvalid("2400:cb00::1"))
            .as("2400:cb00::1 should be valid (regular global unicast)")
            .isFalse();
        
        assertThat(ipAddressSpecialRegistry.isInvalid("2800::1"))
            .as("2800::1 should be valid (regular global unicast)")
            .isFalse();
        
        assertThat(ipAddressSpecialRegistry.isInvalid("3000::1"))
            .as("3000::1 should be valid (regular global unicast)")
            .isFalse();
        
        // Test an address that might be close to special ranges but not in them
        assertThat(ipAddressSpecialRegistry.isInvalid("2003::1"))
            .as("2003::1 should be valid (outside 2001::/32 and 2001:db8::/32)")
            .isFalse();
    }
    
    @Test
    public void testIsInvalid_realWorldCase() {
        // Test the specific case mentioned in the bug report
        String icannAddress = "2001:500:7967::30";
        
        // This address is NOT in the special-purpose registry based on the actual data
        // Even though 2001::/23 is in the registry, 2001:500:: appears to be outside that range
        assertThat(ipAddressSpecialRegistry.isInvalid(icannAddress))
            .as("2001:500:7967::30 should be valid (not in special-purpose registry)")
            .isFalse();
    }
    
    @Test
    public void testIsInvalid_QADiscoveredAddresses() {
        // Test the specific addresses that QA discovered triggering false positives
        // These are all legitimate Global Unicast addresses that should NOT be flagged as special-purpose
        String[] validGlobalUnicastAddresses = {
            "2001:df0:8::a153",        // ns1.nic.jprs - working correctly
            "2001:df0:8::a253",        // ns2.nic.jprs - working correctly
            "2001:218:3001::a153",     // ns3.nic.jprs - was triggering false positive with -10202
            "2001:218:3001::a253"      // ns4.nic.jprs - was triggering false positive with -10202
        };
        
        for (String addr : validGlobalUnicastAddresses) {
            assertThat(ipAddressSpecialRegistry.isInvalid(addr))
                .as("Address " + addr + " should be valid (legitimate Global Unicast, not special-purpose)")
                .isFalse();
        }
    }
    
    @Test
    public void testIsInvalid_Slash28PrefixBoundaryEdgeCases() {
        // Test edge cases for /28 prefix boundaries that were affected by IPAddressString library bug
        // See: https://github.com/seancfoley/IPAddress/issues/13
        // These addresses should be VALID (not in special-purpose ranges) but were incorrectly matched
        //
        // Note: 2001::/23 (IETF Protocol Assignments) covers 2001:0:: to 2001:1ff::
        // So we need to test addresses outside that broader range
        
        // Test addresses that should be valid but were causing false positives due to /28 boundary bugs
        // These are similar to the QA-discovered cases but test different /28 boundary scenarios
        String[] validAddressesOutsideSpecialRanges = {
            "2001:200::1",             // Outside 2001::/23, was incorrectly matching 2001:20::/28 
            "2001:300::1",             // Outside 2001::/23, was incorrectly matching 2001:30::/28
            "2001:400::1",             // Well outside any /28 special ranges
            "2001:500::1",             // Similar to the real ICANN case (2001:500:7967::30)
            "2001:2000::1",            // Much further out, but similar pattern
            "2400:cb00::1",            // Completely different prefix (Cloudflare)
            "2600:1f00::1",            // Different prefix entirely
        };
        
        // Test that these legitimate addresses are not flagged as special-purpose
        for (String addr : validAddressesOutsideSpecialRanges) {
            assertThat(ipAddressSpecialRegistry.isInvalid(addr))
                .as("Address " + addr + " should be valid (not in special-purpose registry)")
                .isFalse();
        }
        
        // Also verify that addresses that SHOULD be invalid still are detected correctly
        String[] shouldBeInvalid = {
            "2001:20::1",              // ORCHIDv2 (2001:20::/28)
            "2001:2f::1",              // Still within ORCHIDv2 range
            "2001:30::1",              // Drone Remote ID (2001:30::/28)  
            "2001:3f::1",              // Still within Drone Remote ID range
            "2001:db8::1",             // Documentation (2001:db8::/32)
            "::1",                     // Loopback
            "fc00::1"                  // Unique-Local
        };
        
        for (String addr : shouldBeInvalid) {
            assertThat(ipAddressSpecialRegistry.isInvalid(addr))
                .as("Address " + addr + " should be invalid (in special-purpose registry)")
                .isTrue();
        }
    }
}