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
    // Simple QueryContext bridge for singleton compatibility
    private static final ThreadLocal<QueryContext> currentQueryContext = new ThreadLocal<>();

    private AcceptHeader acceptHeader = AcceptHeader.APPLICATION_JSON;
    private String httpMethod;
    private String serverIpAddress;
    private NetworkProtocol networkProtocol = NetworkProtocol.IPv4;

    /**
     * Public constructor for QueryContext integration.
     */
    public NetworkInfo() {}

    /**
     * Bridge method for singleton compatibility with QueryContext.
     * Returns the NetworkInfo from the current QueryContext if available.
     */
    public static NetworkInfo getInstance() {
        QueryContext qctx = currentQueryContext.get();
        if (qctx != null) {
            return qctx.getNetworkInfo(); // Bridge to QueryContext instance
        }
        // Simple fallback for legacy code (testing/edge cases)
        return new NetworkInfo();
    }

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

    // Legacy static methods for backward compatibility with non-QueryContext code
    // These delegate to instance methods on the legacy singleton
    public static String getAcceptHeader() {
        return getInstance().getAcceptHeaderValue();
    }

    public static String getHttpMethod() {
        return getInstance().getHttpMethodValue();
    }

    public static String getServerIpAddress() {
        return getInstance().getServerIpAddressValue();
    }

    public static NetworkProtocol getNetworkProtocol() {
        return getInstance().getNetworkProtocolValue();
    }

    public static String getNetworkProtocolAsString() {
        return getInstance().getNetworkProtocolAsStringValue();
    }

    public static void setAcceptHeaderToApplicationJson() {
        getInstance().setAcceptHeaderToApplicationJsonValue();
    }

    public static void setAcceptHeaderToApplicationRdapJson() {
        getInstance().setAcceptHeaderToApplicationRdapJsonValue();
    }

    public static void setHttpMethod(String httpMethod) {
        getInstance().setHttpMethodValue(httpMethod);
    }

    public static void setServerIpAddress(String serverIpAddress) {
        getInstance().setServerIpAddressValue(serverIpAddress);
    }

    public static void setNetworkProtocol(NetworkProtocol protocol) {
        getInstance().setNetworkProtocolValue(protocol);
    }

    public static void setStackToV6() {
        getInstance().setStackToV6Value();
    }

    public static void setStackToV4() {
        getInstance().setStackToV4Value();
    }

    // Bridge methods that were removed but are still being called
    /**
     * Sets the current QueryContext for bridge pattern compatibility.
     * This allows singleton calls to delegate to the correct QueryContext instance.
     */
    public static void setCurrentQueryContext(QueryContext qctx) {
        currentQueryContext.set(qctx);
    }

    /**
     * Clears the current QueryContext to prevent memory leaks.
     * Should be called when validation completes.
     */
    public static void clearCurrentQueryContext() {
        currentQueryContext.remove();
    }
}