package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;


import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class Ipv4AddressSpaceTest extends BaseUnmarshallingTest<Ipv4AddressSpace> {

    private Ipv4AddressSpace ipv4AddressSpace;

    @BeforeMethod
    public void setUp() {
        this.ipv4AddressSpace = unmarshal("/dataset/ipv4-address-space.xml", Ipv4AddressSpace.class);
    }

    @Test
    public void givenValidIpv4AddressSpaceXml_whenUnmarshalling_thenReturnIpv4AddressSpace() throws
                                                                                             NoSuchFieldException,
                                                                                             IllegalAccessException {
        Field field = Ipv4AddressSpace.class.getDeclaredField("records");
        field.setAccessible(true);

        // Get the value of the private field
        List<Ipv4AddressSpace.Ipv4AddressSpaceRecord> records = (List<Ipv4AddressSpace.Ipv4AddressSpaceRecord>) field.get(ipv4AddressSpace);

        assertThat(records).hasSize(256);
        assertThat(records).extracting("prefix", "status").contains(tuple("0.0.0.0/8", "RESERVED"),
                tuple("1.0.0.0/8", "ALLOCATED"),
                tuple("2.0.0.0/8", "ALLOCATED"),
                tuple("3.0.0.0/8", "LEGACY"),
                tuple("4.0.0.0/8", "LEGACY"),
                tuple("5.0.0.0/8", "ALLOCATED"),
                tuple("6.0.0.0/8", "LEGACY"),
                tuple("238.0.0.0/8", "RESERVED"));
    }
}