package org.icann.rdapconformance.validator;

import static org.icann.rdapconformance.validator.CommonUtils.DASH;

/**
 * Singleton class that manages network configuration state for RDAP validation.
 *
 * <p>This class provides centralized management of network settings used during
 * RDAP validation, including:</p>
 * <ul>
 *   <li>Accept header configuration (application/json vs application/rdap+json)</li>
 *   <li>Network protocol selection (IPv4 vs IPv6)</li>
 *   <li>HTTP method tracking for connection logging</li>
 *   <li>Server IP address tracking for debugging</li>
 * </ul>
 *
 * <p>The class uses static methods to provide global access to network configuration
 * and maintains state that affects how HTTP requests are made and tracked during
 * validation operations.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Thread-safe singleton pattern for global state management</li>
 *   <li>Support for both standard JSON and RDAP-specific content types</li>
 *   <li>IPv4/IPv6 protocol switching for dual-stack validation</li>
 *   <li>Integration with connection tracking for detailed logging</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * NetworkInfo.setStackToV6();
 * NetworkInfo.setAcceptHeaderToApplicationRdapJson();
 * String currentHeader = NetworkInfo.getAcceptHeader();
 * NetworkProtocol protocol = NetworkInfo.getNetworkProtocol();
 * </pre>
 *
 * @see NetworkProtocol
 * @see ConnectionTracker
 * @since 1.0.0
 */
public class NetworkInfo {
    private static final NetworkInfo instance = new NetworkInfo();

    private AcceptHeader acceptHeader = AcceptHeader.APPLICATION_JSON;
    private String httpMethod;
    private String serverIpAddress;
    private NetworkProtocol networkProtocol = NetworkProtocol.IPv4;

    private NetworkInfo() {}

    public static NetworkInfo getInstance() {
        return instance;
    }

    // Enum for AcceptHeader
    private enum AcceptHeader {
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

    // Static Getters
    public static String getAcceptHeader() {
        return instance.acceptHeader.getValue();
    }

    public static String getHttpMethod() {
        return (instance.httpMethod == null || instance.httpMethod.isEmpty()) ? DASH : instance.httpMethod;
    }

    public static String getServerIpAddress() {
        return (instance.serverIpAddress == null || instance.serverIpAddress.isEmpty()) ? DASH : instance.serverIpAddress;
    }

    public static NetworkProtocol getNetworkProtocol() {
        return instance.networkProtocol;
    }

    public static String getNetworkProtocolAsString() {
        return (instance.networkProtocol == null) ? DASH : instance.networkProtocol.name();
    }

    // Static Setters
    public static void setAcceptHeaderToApplicationJson() {
        instance.acceptHeader = AcceptHeader.APPLICATION_JSON;
    }

    public static void setAcceptHeaderToApplicationRdapJson() {
        instance.acceptHeader = AcceptHeader.APPLICATION_RDAP_JSON;
    }

    public static void setHttpMethod(String httpMethod) {
        instance.httpMethod = httpMethod;
    }

    public static void setServerIpAddress(String serverIpAddress) {
        instance.serverIpAddress = serverIpAddress;
    }

    public static void setNetworkProtocol(NetworkProtocol protocol) {
        instance.networkProtocol = protocol;
    }

    public static void setStackToV6() {
        setNetworkProtocol(NetworkProtocol.IPv6);
        System.setProperty("java.net.preferIPv4Addresses", "false");
        System.setProperty("java.net.preferIPv4Stack", "false");
        System.setProperty("java.net.preferIPv6Addresses", "true");
        System.setProperty("java.net.preferIPv6Stack", "true");
    }

    public static void setStackToV4() {
        setNetworkProtocol(NetworkProtocol.IPv4);
        System.setProperty("java.net.preferIPv6Addresses", "false");
        System.setProperty("java.net.preferIPv6Stack", "false");
        System.setProperty("java.net.preferIPv4Addresses", "true");
        System.setProperty("java.net.preferIPv4Stack", "true");
    }
}