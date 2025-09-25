package org.icann.rdapconformance.validator;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NetworkInfoTest {

    @BeforeMethod
    @AfterMethod
    public void resetNetworkInfo() {
        NetworkInfo.setAcceptHeaderToApplicationJson();
        NetworkInfo.setHttpMethod(null);
        NetworkInfo.setServerIpAddress(null);
        NetworkInfo.setNetworkProtocol(NetworkProtocol.IPv4);
    }
    
    @Test
    public void testGetInstance_ReturnsSingleton() {
        NetworkInfo instance1 = NetworkInfo.getInstance();
        NetworkInfo instance2 = NetworkInfo.getInstance();
        
        assertThat(instance1).isSameAs(instance2);
        assertThat(instance1).isNotNull();
    }
    
    @Test
    public void testDefaultValues() {
        assertThat(NetworkInfo.getAcceptHeader()).isEqualTo("application/json");
        assertThat(NetworkInfo.getHttpMethod()).isEqualTo("-");
        assertThat(NetworkInfo.getServerIpAddress()).isEqualTo("-");
        assertThat(NetworkInfo.getNetworkProtocol()).isEqualTo(NetworkProtocol.IPv4);
        assertThat(NetworkInfo.getNetworkProtocolAsString()).isEqualTo("IPv4");
    }
    
    @Test
    public void testSetAcceptHeaderToApplicationJson() {
        NetworkInfo.setAcceptHeaderToApplicationRdapJson();
        
        NetworkInfo.setAcceptHeaderToApplicationJson();
        
        assertThat(NetworkInfo.getAcceptHeader()).isEqualTo("application/json");
    }
    
    @Test
    public void testSetAcceptHeaderToApplicationRdapJson() {
        NetworkInfo.setAcceptHeaderToApplicationRdapJson();
        
        assertThat(NetworkInfo.getAcceptHeader()).isEqualTo("application/rdap+json");
    }
    
    @Test
    public void testSetHttpMethod() {
        NetworkInfo.setHttpMethod("GET");
        
        assertThat(NetworkInfo.getHttpMethod()).isEqualTo("GET");
    }
    
    @Test
    public void testSetHttpMethod_Null_ReturnsDash() {
        NetworkInfo.setHttpMethod(null);
        
        assertThat(NetworkInfo.getHttpMethod()).isEqualTo("-");
    }
    
    @Test
    public void testSetHttpMethod_Empty_ReturnsDash() {
        NetworkInfo.setHttpMethod("");
        
        assertThat(NetworkInfo.getHttpMethod()).isEqualTo("-");
    }
    
    @Test
    public void testSetServerIpAddress() {
        NetworkInfo.setServerIpAddress("192.168.1.1");
        
        assertThat(NetworkInfo.getServerIpAddress()).isEqualTo("192.168.1.1");
    }
    
    @Test
    public void testSetServerIpAddress_Null_ReturnsDash() {
        NetworkInfo.setServerIpAddress(null);
        
        assertThat(NetworkInfo.getServerIpAddress()).isEqualTo("-");
    }
    
    @Test
    public void testSetServerIpAddress_Empty_ReturnsDash() {
        NetworkInfo.setServerIpAddress("");
        
        assertThat(NetworkInfo.getServerIpAddress()).isEqualTo("-");
    }
    
    @Test
    public void testSetNetworkProtocol_IPv4() {
        NetworkInfo.setNetworkProtocol(NetworkProtocol.IPv4);
        
        assertThat(NetworkInfo.getNetworkProtocol()).isEqualTo(NetworkProtocol.IPv4);
        assertThat(NetworkInfo.getNetworkProtocolAsString()).isEqualTo("IPv4");
    }
    
    @Test
    public void testSetNetworkProtocol_IPv6() {
        NetworkInfo.setNetworkProtocol(NetworkProtocol.IPv6);
        
        assertThat(NetworkInfo.getNetworkProtocol()).isEqualTo(NetworkProtocol.IPv6);
        assertThat(NetworkInfo.getNetworkProtocolAsString()).isEqualTo("IPv6");
    }
    
    @Test
    public void testSetNetworkProtocol_Null_ReturnsDash() {
        NetworkInfo.setNetworkProtocol(null);
        
        assertThat(NetworkInfo.getNetworkProtocol()).isNull();
        assertThat(NetworkInfo.getNetworkProtocolAsString()).isEqualTo("-");
    }
    
    @Test
    public void testSetStackToV6() {
        NetworkInfo.setStackToV6();
        assertThat(NetworkInfo.getNetworkProtocol()).isEqualTo(NetworkProtocol.IPv6);
    }
    
    @Test
    public void testSetStackToV4() {
        NetworkInfo.setStackToV4();
        assertThat(NetworkInfo.getNetworkProtocol()).isEqualTo(NetworkProtocol.IPv4);
    }
    
    @Test
    public void testSingletonBehavior() {
        NetworkInfo.setHttpMethod("POST");
        NetworkInfo.setServerIpAddress("10.0.0.1");
        
        NetworkInfo instance1 = NetworkInfo.getInstance();
        NetworkInfo instance2 = NetworkInfo.getInstance();
        
        assertThat(instance1).isSameAs(instance2);
        assertThat(NetworkInfo.getHttpMethod()).isEqualTo("POST");
        assertThat(NetworkInfo.getServerIpAddress()).isEqualTo("10.0.0.1");
    }
    
}