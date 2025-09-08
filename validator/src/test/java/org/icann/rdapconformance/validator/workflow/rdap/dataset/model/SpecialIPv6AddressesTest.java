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
}