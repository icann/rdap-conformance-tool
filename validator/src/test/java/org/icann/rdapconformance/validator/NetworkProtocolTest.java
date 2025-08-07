package org.icann.rdapconformance.validator;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NetworkProtocolTest {

    @Test
    public void testEnumValues() {
        NetworkProtocol[] values = NetworkProtocol.values();
        
        assertThat(values).hasSize(2);
        assertThat(values).contains(NetworkProtocol.IPv4, NetworkProtocol.IPv6);
    }
    
    @Test
    public void testIPv4() {
        NetworkProtocol protocol = NetworkProtocol.IPv4;
        
        assertThat(protocol.name()).isEqualTo("IPv4");
        assertThat(protocol.toString()).isEqualTo("IPv4");
        assertThat(protocol.ordinal()).isEqualTo(0);
    }
    
    @Test
    public void testIPv6() {
        NetworkProtocol protocol = NetworkProtocol.IPv6;
        
        assertThat(protocol.name()).isEqualTo("IPv6");
        assertThat(protocol.toString()).isEqualTo("IPv6");
        assertThat(protocol.ordinal()).isEqualTo(1);
    }
    
    @Test
    public void testToString() {
        assertThat(NetworkProtocol.IPv4.toString()).isEqualTo("IPv4");
        assertThat(NetworkProtocol.IPv6.toString()).isEqualTo("IPv6");
    }
    
    @Test
    public void testToStringEqualsName() {
        for (NetworkProtocol protocol : NetworkProtocol.values()) {
            assertThat(protocol.toString())
                .as("toString() should equal name() for %s", protocol)
                .isEqualTo(protocol.name());
        }
    }
    
    @Test
    public void testValueOf() {
        assertThat(NetworkProtocol.valueOf("IPv4")).isEqualTo(NetworkProtocol.IPv4);
        assertThat(NetworkProtocol.valueOf("IPv6")).isEqualTo(NetworkProtocol.IPv6);
    }
    
    @Test
    public void testValueOf_InvalidName_ThrowsException() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> NetworkProtocol.valueOf("IPv5"))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void testEnumEquality() {
        NetworkProtocol ipv4_1 = NetworkProtocol.IPv4;
        NetworkProtocol ipv4_2 = NetworkProtocol.valueOf("IPv4");
        
        assertThat(ipv4_1).isEqualTo(ipv4_2);
        assertThat(ipv4_1).isSameAs(ipv4_2);
    }
    
    @Test
    public void testEnumComparison() {
        assertThat(NetworkProtocol.IPv4.compareTo(NetworkProtocol.IPv6)).isLessThan(0);
        assertThat(NetworkProtocol.IPv6.compareTo(NetworkProtocol.IPv4)).isGreaterThan(0);
        assertThat(NetworkProtocol.IPv4.compareTo(NetworkProtocol.IPv4)).isEqualTo(0);
    }
    
    @Test
    public void testEnumOrdinals() {
        assertThat(NetworkProtocol.IPv4.ordinal()).isEqualTo(0);
        assertThat(NetworkProtocol.IPv6.ordinal()).isEqualTo(1);
    }
}