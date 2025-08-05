package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;


import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
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

    @Test
    public void testIsInvalid_ValidAllocatedAddress_ShouldReturnFalse() {
        // Test with an address in the allocated range (1.0.0.0/8)
        boolean result = ipv4AddressSpace.isInvalid("1.2.3.4");
        
        assertThat(result).isFalse();
    }
    
    @Test
    public void testIsInvalid_ValidLegacyAddress_ShouldReturnFalse() {
        // Test with an address in the legacy range (3.0.0.0/8)
        boolean result = ipv4AddressSpace.isInvalid("3.4.5.6");
        
        assertThat(result).isFalse();
    }

    @Test
    public void testIsInvalid_ReservedAddress_ShouldReturnTrue() {
        // Test with an address in the reserved range (0.0.0.0/8)
        boolean result = ipv4AddressSpace.isInvalid("0.1.2.3");
        
        assertThat(result).isTrue();
    }
    
    @Test
    public void testIsInvalid_UnallocatedAddress_ShouldReturnTrue() {
        // Test with an address that's not allocated or legacy
        boolean result = ipv4AddressSpace.isInvalid("250.1.2.3");
        
        assertThat(result).isTrue();
    }

    @Test
    public void testParse_ShouldCompleteWithoutException() throws Throwable {
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        
        // Should complete without throwing an exception
        ipv4AddressSpace.parse(inputStream);
    }

    @Test
    public void testIpv4AddressSpaceRecord_Constructor() {
        Ipv4AddressSpace.Ipv4AddressSpaceRecord record = 
            new Ipv4AddressSpace.Ipv4AddressSpaceRecord("192.168.1.0/24", "ALLOCATED");
        
        assertThat(record.getPrefix()).isEqualTo("192.168.1.0/24");
        assertThat(record.getStatus()).isEqualTo("ALLOCATED");
    }
    
    @Test
    public void testIpv4AddressSpaceRecord_DefaultConstructor() {
        Ipv4AddressSpace.Ipv4AddressSpaceRecord record = 
            new Ipv4AddressSpace.Ipv4AddressSpaceRecord();
        
        assertThat(record).isNotNull();
    }
    
    @Test
    public void testIpv4AddressSpaceRecord_AfterUnmarshal() {
        Ipv4AddressSpace.Ipv4AddressSpaceRecord record = 
            new Ipv4AddressSpace.Ipv4AddressSpaceRecord("192/24", "ALLOCATED");
        
        // Simulate JAXB afterUnmarshal processing
        record.afterUnmarshal(null, null);
        
        assertThat(record.getPrefix()).isEqualTo("192.0.0.0/8");
    }

    @Test  
    public void testIsInvalid_WithManualRecords() throws Exception {
        // Create a test instance with known records
        Ipv4AddressSpace testSpace = new Ipv4AddressSpace();
        
        // Use reflection to set up test records
        Field recordsField = Ipv4AddressSpace.class.getDeclaredField("records");
        recordsField.setAccessible(true);
        
        List<Ipv4AddressSpace.Ipv4AddressSpaceRecord> testRecords = new ArrayList<>();
        testRecords.add(new Ipv4AddressSpace.Ipv4AddressSpaceRecord("10.0.0.0/8", "ALLOCATED"));
        testRecords.add(new Ipv4AddressSpace.Ipv4AddressSpaceRecord("172.16.0.0/12", "ALLOCATED"));
        testRecords.add(new Ipv4AddressSpace.Ipv4AddressSpaceRecord("192.168.0.0/16", "LEGACY"));
        
        recordsField.set(testSpace, testRecords);
        
        // Test various scenarios
        assertThat(testSpace.isInvalid("10.1.1.1")).isFalse(); // ALLOCATED
        assertThat(testSpace.isInvalid("192.168.1.1")).isFalse(); // LEGACY
        assertThat(testSpace.isInvalid("8.8.8.8")).isTrue(); // Not in any allocated/legacy range
        assertThat(testSpace.isInvalid("172.20.1.1")).isFalse(); // Within ALLOCATED range
    }
}