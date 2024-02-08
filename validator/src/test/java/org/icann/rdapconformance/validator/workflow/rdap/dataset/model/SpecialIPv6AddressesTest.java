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
}