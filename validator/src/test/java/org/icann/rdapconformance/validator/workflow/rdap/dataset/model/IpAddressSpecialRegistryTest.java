package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class IpAddressSpecialRegistryTest extends BaseUnmarshallingTest<IpAddressSpecialRegistry> {

    // Concrete implementation for testing
    private static class TestIpAddressSpecialRegistry extends IpAddressSpecialRegistry {
        // No additional implementation needed for testing
    }

    private TestIpAddressSpecialRegistry registry;

    @BeforeMethod
    public void setUp() {
        registry = new TestIpAddressSpecialRegistry();
    }

    @Test
    public void testIsInvalid_WithSpecialAddress_ShouldReturnTrue() throws Exception {
        // Set up test data using reflection
        Field recordsField = IpAddressSpecialRegistry.class.getDeclaredField("ipAddressSpecialRecords");
        recordsField.setAccessible(true);
        
        List<IpAddressSpecialRegistry.IpAddressSpecialRecord> testRecords = new ArrayList<>();
        
        // Create test record
        IpAddressSpecialRegistry.IpAddressSpecialRecord record = 
            new IpAddressSpecialRegistry.IpAddressSpecialRecord();
        
        // Set address field using reflection
        Field addressField = IpAddressSpecialRegistry.IpAddressSpecialRecord.class.getDeclaredField("address");
        addressField.setAccessible(true);
        addressField.set(record, "127.0.0.0/8");
        
        testRecords.add(record);
        recordsField.set(registry, testRecords);
        
        // Trigger afterUnmarshal to initialize the records set
        registry.afterUnmarshal(null, null);
        
        // Test with IP in special range
        boolean result = registry.isInvalid("127.0.0.1");
        assertThat(result).isTrue();
    }

    @Test
    public void testIsInvalid_WithNormalAddress_ShouldReturnFalse() throws Exception {
        // Set up test data using reflection
        Field recordsField = IpAddressSpecialRegistry.class.getDeclaredField("ipAddressSpecialRecords");
        recordsField.setAccessible(true);
        
        List<IpAddressSpecialRegistry.IpAddressSpecialRecord> testRecords = new ArrayList<>();
        
        // Create test record for localhost
        IpAddressSpecialRegistry.IpAddressSpecialRecord record = 
            new IpAddressSpecialRegistry.IpAddressSpecialRecord();
        
        // Set address field using reflection
        Field addressField = IpAddressSpecialRegistry.IpAddressSpecialRecord.class.getDeclaredField("address");
        addressField.setAccessible(true);
        addressField.set(record, "127.0.0.0/8");
        
        testRecords.add(record);
        recordsField.set(registry, testRecords);
        
        // Trigger afterUnmarshal to initialize the records set
        registry.afterUnmarshal(null, null);
        
        // Test with IP not in special range
        boolean result = registry.isInvalid("8.8.8.8");
        assertThat(result).isFalse();
    }

    @Test
    public void testIsInvalid_WithMultipleSpecialRanges_ShouldDetectCorrectly() throws Exception {
        // Set up test data using reflection
        Field recordsField = IpAddressSpecialRegistry.class.getDeclaredField("ipAddressSpecialRecords");
        recordsField.setAccessible(true);
        
        List<IpAddressSpecialRegistry.IpAddressSpecialRecord> testRecords = new ArrayList<>();
        
        // Create multiple test records
        IpAddressSpecialRegistry.IpAddressSpecialRecord record1 = 
            new IpAddressSpecialRegistry.IpAddressSpecialRecord();
        IpAddressSpecialRegistry.IpAddressSpecialRecord record2 = 
            new IpAddressSpecialRegistry.IpAddressSpecialRecord();
        
        // Set address fields using reflection
        Field addressField = IpAddressSpecialRegistry.IpAddressSpecialRecord.class.getDeclaredField("address");
        addressField.setAccessible(true);
        addressField.set(record1, "127.0.0.0/8");    // Localhost
        addressField.set(record2, "10.0.0.0/8");     // Private range
        
        testRecords.add(record1);
        testRecords.add(record2);
        recordsField.set(registry, testRecords);
        
        // Trigger afterUnmarshal to initialize the records set
        registry.afterUnmarshal(null, null);
        
        // Test various addresses
        assertThat(registry.isInvalid("127.0.0.1")).isTrue();     // Localhost
        assertThat(registry.isInvalid("10.1.1.1")).isTrue();      // Private
        assertThat(registry.isInvalid("8.8.8.8")).isFalse();      // Public
        assertThat(registry.isInvalid("192.168.1.1")).isFalse();  // Not in our test special ranges
    }

    @Test
    public void testIpAddressSpecialRecord_GetValue() throws Exception {
        IpAddressSpecialRegistry.IpAddressSpecialRecord record = 
            new IpAddressSpecialRegistry.IpAddressSpecialRecord();
        
        // Set address field using reflection
        Field addressField = IpAddressSpecialRegistry.IpAddressSpecialRecord.class.getDeclaredField("address");
        addressField.setAccessible(true);
        addressField.set(record, "192.168.0.0/16");
        
        String value = record.getValue();
        assertThat(value).isEqualTo("192.168.0.0/16");
    }

    @Test
    public void testIgnoreInnerTagAdapter_UnmarshalWithNode() throws Exception {
        IpAddressSpecialRegistry.IgnoreInnerTagAdapter adapter = 
            new IpAddressSpecialRegistry.IgnoreInnerTagAdapter();
        
        // Create a test XML node
        String xml = "<test>192.168.1.0/24</test>";
        Document doc = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(new ByteArrayInputStream(xml.getBytes()));
        
        Node node = doc.getDocumentElement();
        
        String result = adapter.unmarshal(node);
        assertThat(result).isEqualTo("192.168.1.0/24");
    }

    @Test
    public void testIgnoreInnerTagAdapter_UnmarshalWithInvalidInput_ShouldThrowException() {
        IpAddressSpecialRegistry.IgnoreInnerTagAdapter adapter = 
            new IpAddressSpecialRegistry.IgnoreInnerTagAdapter();
        
        assertThatThrownBy(() -> adapter.unmarshal("not a node"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Input object is not a Node");
    }

    @Test
    public void testIgnoreInnerTagAdapter_Marshal() {
        IpAddressSpecialRegistry.IgnoreInnerTagAdapter adapter = 
            new IpAddressSpecialRegistry.IgnoreInnerTagAdapter();
        
        String input = "192.168.1.0/24";
        String result = adapter.marshal(input);
        
        assertThat(result).isEqualTo(input);
    }

    @Test
    public void testGetValueRecords_ShouldReturnIpAddressSpecialRecords() throws Exception {
        // Set up test data
        Field recordsField = IpAddressSpecialRegistry.class.getDeclaredField("ipAddressSpecialRecords");
        recordsField.setAccessible(true);
        
        List<IpAddressSpecialRegistry.IpAddressSpecialRecord> testRecords = new ArrayList<>();
        recordsField.set(registry, testRecords);
        
        // Access protected method via reflection
        java.lang.reflect.Method getValueRecordsMethod = 
            IpAddressSpecialRegistry.class.getDeclaredMethod("getValueRecords");
        getValueRecordsMethod.setAccessible(true);
        
        List<IpAddressSpecialRegistry.IpAddressSpecialRecord> result = 
            (List<IpAddressSpecialRegistry.IpAddressSpecialRecord>) getValueRecordsMethod.invoke(registry);
        
        assertThat(result).isSameAs(testRecords);
    }
}