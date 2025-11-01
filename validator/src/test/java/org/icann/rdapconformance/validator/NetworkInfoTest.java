package org.icann.rdapconformance.validator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NetworkInfoTest {

    private NetworkInfo networkInfo;

    @BeforeMethod
    public void setUp() {
        networkInfo = new NetworkInfo();
    }

    @Test
    public void testDefaultValues() {
        assertThat(networkInfo.getAcceptHeaderValue()).isEqualTo("application/json");
        assertThat(networkInfo.getHttpMethodValue()).isEqualTo("-");
        assertThat(networkInfo.getServerIpAddressValue()).isEqualTo("-");
        assertThat(networkInfo.getNetworkProtocolValue()).isEqualTo(NetworkProtocol.IPv4);
        assertThat(networkInfo.getNetworkProtocolAsStringValue()).isEqualTo("IPv4");
    }

    @Test
    public void testSetAcceptHeaderToApplicationJson() {
        networkInfo.setAcceptHeaderToApplicationRdapJsonValue();

        networkInfo.setAcceptHeaderToApplicationJsonValue();

        assertThat(networkInfo.getAcceptHeaderValue()).isEqualTo("application/json");
    }

    @Test
    public void testSetAcceptHeaderToApplicationRdapJson() {
        networkInfo.setAcceptHeaderToApplicationRdapJsonValue();

        assertThat(networkInfo.getAcceptHeaderValue()).isEqualTo("application/rdap+json");
    }

    @Test
    public void testSetHttpMethod() {
        networkInfo.setHttpMethodValue("GET");

        assertThat(networkInfo.getHttpMethodValue()).isEqualTo("GET");
    }

    @Test
    public void testSetHttpMethod_Null_ReturnsDash() {
        networkInfo.setHttpMethodValue(null);

        assertThat(networkInfo.getHttpMethodValue()).isEqualTo("-");
    }

    @Test
    public void testSetHttpMethod_Empty_ReturnsDash() {
        networkInfo.setHttpMethodValue("");

        assertThat(networkInfo.getHttpMethodValue()).isEqualTo("-");
    }

    @Test
    public void testSetServerIpAddress() {
        networkInfo.setServerIpAddressValue("192.168.1.1");

        assertThat(networkInfo.getServerIpAddressValue()).isEqualTo("192.168.1.1");
    }

    @Test
    public void testSetServerIpAddress_Null_ReturnsDash() {
        networkInfo.setServerIpAddressValue(null);

        assertThat(networkInfo.getServerIpAddressValue()).isEqualTo("-");
    }

    @Test
    public void testSetServerIpAddress_Empty_ReturnsDash() {
        networkInfo.setServerIpAddressValue("");

        assertThat(networkInfo.getServerIpAddressValue()).isEqualTo("-");
    }

    @Test
    public void testSetNetworkProtocol_IPv4() {
        networkInfo.setNetworkProtocolValue(NetworkProtocol.IPv4);

        assertThat(networkInfo.getNetworkProtocolValue()).isEqualTo(NetworkProtocol.IPv4);
        assertThat(networkInfo.getNetworkProtocolAsStringValue()).isEqualTo("IPv4");
    }

    @Test
    public void testSetNetworkProtocol_IPv6() {
        networkInfo.setNetworkProtocolValue(NetworkProtocol.IPv6);

        assertThat(networkInfo.getNetworkProtocolValue()).isEqualTo(NetworkProtocol.IPv6);
        assertThat(networkInfo.getNetworkProtocolAsStringValue()).isEqualTo("IPv6");
    }

    @Test
    public void testSetNetworkProtocol_Null_ReturnsDash() {
        networkInfo.setNetworkProtocolValue(null);

        assertThat(networkInfo.getNetworkProtocolValue()).isNull();
        assertThat(networkInfo.getNetworkProtocolAsStringValue()).isEqualTo("-");
    }

    @Test
    public void testSetStackToV6() {
        networkInfo.setStackToV6Value();
        assertThat(networkInfo.getNetworkProtocolValue()).isEqualTo(NetworkProtocol.IPv6);
    }

    @Test
    public void testSetStackToV4() {
        networkInfo.setStackToV4Value();
        assertThat(networkInfo.getNetworkProtocolValue()).isEqualTo(NetworkProtocol.IPv4);
    }

    @Test
    public void testInstanceBehavior() {
        networkInfo.setHttpMethodValue("POST");
        networkInfo.setServerIpAddressValue("10.0.0.1");

        // Create another instance to verify they are independent
        NetworkInfo anotherInstance = new NetworkInfo();

        assertThat(networkInfo).isNotSameAs(anotherInstance);
        assertThat(networkInfo.getHttpMethodValue()).isEqualTo("POST");
        assertThat(networkInfo.getServerIpAddressValue()).isEqualTo("10.0.0.1");

        // Verify the other instance has default values
        assertThat(anotherInstance.getHttpMethodValue()).isEqualTo("-");
        assertThat(anotherInstance.getServerIpAddressValue()).isEqualTo("-");
    }
}