package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class Ipv6AddressSpaceTest extends BaseUnmarshallingTest<Ipv6AddressSpace> {

    private Ipv6AddressSpace ipv6AddressSpace;

    @BeforeMethod
    public void setUp() {
        this.ipv6AddressSpace = unmarshal("/dataset/ipv6-address-space.xml", Ipv6AddressSpace.class);
    }

    public Ipv6AddressSpace getIpv6AddressSpace() {
        return ipv6AddressSpace;
    }

    @Test
    public void givenValidIpv6AddressSpaceXml_whenUnmarshalling_thenReturnIpv6AddressSpace() throws
                                                                                             NoSuchFieldException,
                                                                                             IllegalAccessException {
        // Create an instance of the class

        // Access the private field using Java Reflection
        Field field = Ipv6AddressSpace.class.getDeclaredField("records");
        field.setAccessible(true);

        // Get the value of the private field
        List<Ipv6AddressSpace.Ipv6AddressSpaceRecord> records = (List<Ipv6AddressSpace.Ipv6AddressSpaceRecord>) field.get(ipv6AddressSpace);

        assertThat(records).hasSize(20);
        assertThat(records).extracting("prefix", "description").contains(tuple("0000::/8", "Reserved by IETF"),
                tuple("0100::/8", "Reserved by IETF"),
                tuple("0200::/7", "Reserved by IETF"),
                tuple("0400::/6", "Reserved by IETF"),
                tuple("0800::/5", "Reserved by IETF"),
                tuple("1000::/4", "Reserved by IETF"),
                tuple("2000::/3", "Global Unicast"),
                tuple("4000::/3", "Reserved by IETF"),
                tuple("6000::/3", "Reserved by IETF"),
                tuple("8000::/3", "Reserved by IETF"),
                tuple("a000::/3", "Reserved by IETF"),
                tuple("c000::/3", "Reserved by IETF"),
                tuple("e000::/4", "Reserved by IETF"),
                tuple("f000::/5", "Reserved by IETF"),
                tuple("f800::/6", "Reserved by IETF"),
                tuple("fc00::/7", "Unique Local Unicast"),
                tuple("fe00::/9", "Reserved by IETF"),
                tuple("fe80::/10", "Link-Scoped Unicast"),
                tuple("fec0::/10", "Reserved by IETF"),
                tuple("ff00::/8", "Multicast"));
    }

    @Test
    public void testIsInvalid_globalUnicastAddresses() {
        // Test addresses that SHOULD be valid (return false) because they're in Global Unicast (2000::/3)
        
        assertThat(ipv6AddressSpace.isInvalid("2001:db8::1"))
            .as("2001:db8::1 should be valid (in Global Unicast 2000::/3)")
            .isFalse();
        
        assertThat(ipv6AddressSpace.isInvalid("2001:500:7967::30"))
            .as("2001:500:7967::30 should be valid (in Global Unicast 2000::/3)")
            .isFalse();
        
        assertThat(ipv6AddressSpace.isInvalid("2400:cb00::1"))
            .as("2400:cb00::1 should be valid (in Global Unicast 2000::/3)")
            .isFalse();
        
        assertThat(ipv6AddressSpace.isInvalid("2800::1"))
            .as("2800::1 should be valid (in Global Unicast 2000::/3)")
            .isFalse();
        
        assertThat(ipv6AddressSpace.isInvalid("3fff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"))
            .as("3fff:ffff:ffff:ffff:ffff:ffff:ffff:ffff should be valid (end of 2000::/3)")
            .isFalse();
    }
    
    @Test 
    public void testIsInvalid_nonGlobalUnicastAddresses() {
        // Test addresses that SHOULD be invalid (return true) because they're NOT in Global Unicast
        
        // Unique Local Unicast (fc00::/7)
        assertThat(ipv6AddressSpace.isInvalid("fc00::1"))
            .as("fc00::1 should be invalid (Unique Local Unicast, not Global Unicast)")
            .isTrue();
        
        assertThat(ipv6AddressSpace.isInvalid("fdff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"))
            .as("fdff:ffff:ffff:ffff:ffff:ffff:ffff:ffff should be invalid (Unique Local)")
            .isTrue();
        
        // Link-Scoped Unicast (fe80::/10)
        assertThat(ipv6AddressSpace.isInvalid("fe80::1"))
            .as("fe80::1 should be invalid (Link-Scoped Unicast, not Global Unicast)")
            .isTrue();
        
        assertThat(ipv6AddressSpace.isInvalid("febf:ffff:ffff:ffff:ffff:ffff:ffff:ffff"))
            .as("febf:ffff:ffff:ffff:ffff:ffff:ffff:ffff should be invalid (Link-Scoped)")
            .isTrue();
        
        // Multicast (ff00::/8)
        assertThat(ipv6AddressSpace.isInvalid("ff00::1"))
            .as("ff00::1 should be invalid (Multicast, not Global Unicast)")
            .isTrue();
        
        assertThat(ipv6AddressSpace.isInvalid("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"))
            .as("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff should be invalid (Multicast)")
            .isTrue();
        
        // Reserved by IETF ranges
        assertThat(ipv6AddressSpace.isInvalid("::1"))
            .as("::1 should be invalid (Reserved by IETF 0000::/8, not Global Unicast)")
            .isTrue();
        
        assertThat(ipv6AddressSpace.isInvalid("0100::1"))
            .as("0100::1 should be invalid (Reserved by IETF 0100::/8, not Global Unicast)")
            .isTrue();
        
        assertThat(ipv6AddressSpace.isInvalid("4000::1"))
            .as("4000::1 should be invalid (Reserved by IETF 4000::/3, not Global Unicast)")
            .isTrue();
        
        assertThat(ipv6AddressSpace.isInvalid("1fff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"))
            .as("1fff:ffff:ffff:ffff:ffff:ffff:ffff:ffff should be invalid (Reserved by IETF 1000::/4)")
            .isTrue();
    }
    
    @Test
    public void testIsInvalid_boundaryAddresses() {
        // Test addresses at the boundaries of Global Unicast range (2000::/3)
        
        // Just before Global Unicast
        assertThat(ipv6AddressSpace.isInvalid("1fff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"))
            .as("1fff:ffff:ffff:ffff:ffff:ffff:ffff:ffff should be invalid (just before 2000::/3)")
            .isTrue();
        
        // Start of Global Unicast range - use a well-formed address
        assertThat(ipv6AddressSpace.isInvalid("2000:0:0:0:0:0:0:1"))
            .as("2000:0:0:0:0:0:0:1 should be valid (in Global Unicast 2000::/3)")
            .isFalse();
        
        // End of Global Unicast
        assertThat(ipv6AddressSpace.isInvalid("3fff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"))
            .as("3fff:ffff:ffff:ffff:ffff:ffff:ffff:ffff should be valid (end of 2000::/3)")
            .isFalse();
        
        // Just after Global Unicast
        assertThat(ipv6AddressSpace.isInvalid("4000::"))
            .as("4000:: should be invalid (just after 2000::/3, starts 4000::/3)")
            .isTrue();
    }

}