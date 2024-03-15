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

}