package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class RealWorldIpv6ValidationTest extends BaseUnmarshallingTest<SpecialIPv6Addresses> {

    private Ipv6AddressSpace ipv6AddressSpace;
    private SpecialIPv6Addresses specialIPv6Addresses;
    private ObjectMapper objectMapper;

    @BeforeMethod
    public void setUp() throws Exception {
        // Load both datasets
        this.specialIPv6Addresses = unmarshal("/dataset/iana-ipv6-special-registry.xml", SpecialIPv6Addresses.class);
        
        // Create a separate test instance for IPv6AddressSpace
        Ipv6AddressSpaceTest ipv6Test = new Ipv6AddressSpaceTest();
        ipv6Test.setUp();  
        this.ipv6AddressSpace = ipv6Test.getIpv6AddressSpace();
        
        this.objectMapper = new ObjectMapper();
    }

    @Test
    public void testRealWorldRdapResponseWithIpv6Addresses() throws Exception {
        // Load the real RDAP response JSON
        InputStream jsonStream = getClass().getResourceAsStream("/dataset/ipv6-test-data.json");
        assertThat(jsonStream).isNotNull();
        
        JsonNode rdapResponse = objectMapper.readTree(jsonStream);
        
        // Extract IPv6 addresses from nameservers
        JsonNode nameservers = rdapResponse.get("nameservers");
        assertThat(nameservers).isNotNull();
        assertThat(nameservers.isArray()).isTrue();
        
        // Test each nameserver's IPv6 addresses
        for (JsonNode nameserver : nameservers) {
            JsonNode ipAddresses = nameserver.get("ipAddresses");
            if (ipAddresses != null && ipAddresses.has("v6")) {
                JsonNode v6Addresses = ipAddresses.get("v6");
                for (JsonNode v6Address : v6Addresses) {
                    String ipv6 = v6Address.asText();
                    System.out.println("Testing IPv6 address: " + ipv6);
                    
                    // Test address space validation (step 7.1.2.2)
                    boolean failsAddressSpaceValidation = ipv6AddressSpace.isInvalid(ipv6);
                    System.out.println("  Address Space validation (isInvalid): " + failsAddressSpaceValidation);
                    
                    // Test special-purpose validation (step 7.1.2.3)
                    boolean failsSpecialPurposeValidation = specialIPv6Addresses.isInvalid(ipv6);
                    System.out.println("  Special Purpose validation (isInvalid): " + failsSpecialPurposeValidation);
                    
                    // Analyze the results
                    if (ipv6.equals("2001:df0:8::a153") || ipv6.equals("2001:df0:8::a253")) {
                        // These addresses are in 2000::/3 (Global Unicast) so should pass address space validation
                        assertThat(failsAddressSpaceValidation)
                            .as("Address " + ipv6 + " should pass Global Unicast validation (in 2000::/3)")
                            .isFalse();
                            
                        // But they might be in special-purpose registry - let's see what happens
                        System.out.println("  Expected: Pass address space, check special purpose registry");
                    }
                }
            }
        }
    }
    
    @Test
    public void testSpecificIpv6AddressesFromRealResponse() {
        // Test the specific addresses we found in the real RDAP response
        String[] realWorldAddresses = {
            "2001:df0:8::a153",
            "2001:df0:8::a253",  
            "2001:218:3001::a153",
            "2001:218:3001::a253"
        };
        
        System.out.println("\n=== Testing Real-World IPv6 Addresses ===");
        
        for (String ipv6 : realWorldAddresses) {
            System.out.println("Testing: " + ipv6);
            
            // Test address space validation
            boolean failsAddressSpace = ipv6AddressSpace.isInvalid(ipv6);
            System.out.println("  Address Space (Global Unicast) validation -> isInvalid: " + failsAddressSpace);
            
            // Test special-purpose validation  
            boolean failsSpecialPurpose = specialIPv6Addresses.isInvalid(ipv6);
            System.out.println("  Special Purpose validation -> isInvalid: " + failsSpecialPurpose);
            
            // All these addresses start with 2001: so they're in Global Unicast range (2000::/3)
            assertThat(failsAddressSpace)
                .as("Address " + ipv6 + " should pass Global Unicast validation (in 2000::/3)") 
                .isFalse();
                
            // Now we'll see if our special-purpose validation catches any of these
            System.out.println("  Result: " + 
                (failsAddressSpace ? "REJECTED (not Global Unicast)" : "PASSED Global Unicast") + 
                ", " +
                (failsSpecialPurpose ? "REJECTED (special purpose)" : "PASSED special purpose"));
            System.out.println();
        }
    }
    
    @Test
    public void testKnownInvalidIpv6Addresses() {
        // Test addresses that should definitely fail our validation
        String[] invalidAddresses = {
            "fc00::1",     // Unique Local Unicast (not Global Unicast)
            "fe80::1",     // Link-Local Unicast (not Global Unicast)  
            "ff00::1",     // Multicast (not Global Unicast)
            "::1",         // Loopback (not Global Unicast)
            "2001:db8::1"  // Documentation (Global Unicast but special purpose)
        };
        
        System.out.println("\n=== Testing Known Invalid IPv6 Addresses ===");
        
        for (String ipv6 : invalidAddresses) {
            System.out.println("Testing: " + ipv6);
            
            boolean failsAddressSpace = ipv6AddressSpace.isInvalid(ipv6);
            boolean failsSpecialPurpose = specialIPv6Addresses.isInvalid(ipv6);
            
            System.out.println("  Address Space validation -> isInvalid: " + failsAddressSpace);
            System.out.println("  Special Purpose validation -> isInvalid: " + failsSpecialPurpose);
            
            // These addresses should fail at least one validation step
            boolean shouldBeRejected = failsAddressSpace || failsSpecialPurpose;
            assertThat(shouldBeRejected)
                .as("Address " + ipv6 + " should be rejected by either address space or special purpose validation")
                .isTrue();
                
            System.out.println("  Result: " + (shouldBeRejected ? "CORRECTLY REJECTED" : "INCORRECTLY PASSED"));
            System.out.println();
        }
    }
}