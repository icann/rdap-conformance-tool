package org.icann.rdapconformance.validator;

import static org.icann.rdapconformance.validator.CommonUtils.DASH;

/**
 * Network configuration class integrated into QueryContext architecture.
 *
 * <p>This class provides network settings management for RDAP validation,
 * now integrated into the QueryContext "world object" pattern for thread-safe
 * operation in concurrent environments.</p>
 *
 * <p>Network settings managed:</p>
 * <ul>
 *   <li>Accept header configuration (application/json vs application/rdap+json)</li>
 *   <li>Network protocol selection (IPv4 vs IPv6)</li>
 *   <li>HTTP method tracking for connection logging</li>
 *   <li>Server IP address tracking for debugging</li>
 * </ul>
 *
 * <p>This class is now instantiated per QueryContext for thread-safe validation.</p>
 *
 * @see NetworkProtocol
 * @see QueryContext
 * @since 1.0.0
 */
public class NetworkInfo {
    private AcceptHeader acceptHeader = AcceptHeader.APPLICATION_JSON;
    private String httpMethod;
    private String serverIpAddress;
    private NetworkProtocol networkProtocol = NetworkProtocol.IPv4;

    /**
     * Public constructor for QueryContext integration.
     */
    public NetworkInfo() {}


    // Enum for AcceptHeader
    public enum AcceptHeader {
        APPLICATION_JSON("application/json"),
        APPLICATION_RDAP_JSON("application/rdap+json");

        private final String value;

        AcceptHeader(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    // Instance methods for QueryContext integration
    public String getAcceptHeaderValue() {
        return acceptHeader.getValue();
    }

    public String getHttpMethodValue() {
        return (httpMethod == null || httpMethod.isEmpty()) ? DASH : httpMethod;
    }

    public String getServerIpAddressValue() {
        return (serverIpAddress == null || serverIpAddress.isEmpty()) ? DASH : serverIpAddress;
    }

    public NetworkProtocol getNetworkProtocolValue() {
        return networkProtocol;
    }

    public String getNetworkProtocolAsStringValue() {
        return (networkProtocol == null) ? DASH : networkProtocol.name();
    }

    public void setAcceptHeaderToApplicationJsonValue() {
        this.acceptHeader = AcceptHeader.APPLICATION_JSON;
    }

    public void setAcceptHeaderToApplicationRdapJsonValue() {
        this.acceptHeader = AcceptHeader.APPLICATION_RDAP_JSON;
    }

    public void setHttpMethodValue(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void setServerIpAddressValue(String serverIpAddress) {
        this.serverIpAddress = serverIpAddress;
    }

    public void setNetworkProtocolValue(NetworkProtocol protocol) {
        this.networkProtocol = protocol;
    }

    public void setStackToV6Value() {
        setNetworkProtocolValue(NetworkProtocol.IPv6);
    }

    public void setStackToV4Value() {
        setNetworkProtocolValue(NetworkProtocol.IPv4);
    }

}