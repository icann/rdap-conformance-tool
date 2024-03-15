package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SpecialIPv4AddressesTest extends BaseUnmarshallingTest<SpecialIPv4Addresses> {
    private SpecialIPv4Addresses ipAddressSpecialRegistry;

    @BeforeMethod
    public void setUp() {
        this.ipAddressSpecialRegistry = unmarshal("/dataset/iana-ipv4-special-registry.xml", SpecialIPv4Addresses.class);
    }

    @Test
    public void givenIpAddressSpecialRegistry_whenUnmarshalling_thenSpecialIPv4AddressesAndSpecialIPv6AddressesAreUnmarshalled() {
        assertThat(ipAddressSpecialRegistry.getValues()).hasSize(24);
        assertThat(ipAddressSpecialRegistry.getValues()).contains("255.255.255.255/32",
                "192.31.196.0/24",
                "192.0.0.8/32",
                "198.51.100.0/24",
                "100.64.0.0/10",
                "192.0.0.9/32",
                "192.0.0.170/32, 192.0.0.171/32",
                "192.0.2.0/24",
                "192.0.0.0/29",
                "127.0.0.0/8",
                "192.88.99.0/24",
                "192.175.48.0/24",
                "0.0.0.0/8",
                "192.0.0.0/24 ",
                "192.168.0.0/16",
                "192.0.0.10/32",
                "192.52.193.0/24",
                "10.0.0.0/8",
                "203.0.113.0/24",
                "198.18.0.0/15",
                "0.0.0.0/32",
                "169.254.0.0/16",
                "240.0.0.0/4",
                "172.16.0.0/12");
    }
}