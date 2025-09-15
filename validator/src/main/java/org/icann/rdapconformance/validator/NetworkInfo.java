package org.icann.rdapconformance.validator;

import static org.icann.rdapconformance.validator.CommonUtils.DASH;
import org.icann.rdapconformance.validator.workflow.profile.IPVersionContext;

public class NetworkInfo {
    private static final NetworkInfo instance = new NetworkInfo();
    
    // Thread-local storage for parallel execution
    private static final ThreadLocal<NetworkInfo> threadLocalInstance = 
        ThreadLocal.withInitial(NetworkInfo::new);
    
    // Feature flag to enable thread-local mode
    private static final boolean USE_THREAD_LOCAL = 
        "true".equals(System.getProperty("rdap.parallel.ipversions", "false"));

    private AcceptHeader acceptHeader = AcceptHeader.APPLICATION_JSON;
    private String httpMethod;
    private String serverIpAddress;
    private NetworkProtocol networkProtocol = NetworkProtocol.IPv4;

    private NetworkInfo() {}

    public static NetworkInfo getInstance() {
        if (USE_THREAD_LOCAL) {
            IPVersionContext context = IPVersionContext.current();
            if (context != null) {
                return threadLocalInstance.get();
            }
        }
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
        return getInstance().acceptHeader.getValue();
    }

    public static String getHttpMethod() {
        NetworkInfo info = getInstance();
        return (info.httpMethod == null || info.httpMethod.isEmpty()) ? DASH : info.httpMethod;
    }

    public static String getServerIpAddress() {
        NetworkInfo info = getInstance();
        return (info.serverIpAddress == null || info.serverIpAddress.isEmpty()) ? DASH : info.serverIpAddress;
    }

    public static NetworkProtocol getNetworkProtocol() {
        return getInstance().networkProtocol;
    }

    public static String getNetworkProtocolAsString() {
        NetworkInfo info = getInstance();
        return (info.networkProtocol == null) ? DASH : info.networkProtocol.name();
    }

    // Static Setters
    public static void setAcceptHeaderToApplicationJson() {
        getInstance().acceptHeader = AcceptHeader.APPLICATION_JSON;
    }

    public static void setAcceptHeaderToApplicationRdapJson() {
        getInstance().acceptHeader = AcceptHeader.APPLICATION_RDAP_JSON;
    }

    public static void setHttpMethod(String httpMethod) {
        getInstance().httpMethod = httpMethod;
    }

    public static void setServerIpAddress(String serverIpAddress) {
        getInstance().serverIpAddress = serverIpAddress;
    }

    public static void setNetworkProtocol(NetworkProtocol protocol) {
        getInstance().networkProtocol = protocol;
    }

    public static void setStackToV6() {
        setNetworkProtocol(NetworkProtocol.IPv6);
        // Only set system properties in non-thread-local mode
        // In thread-local mode, IPVersionContext handles system properties
        if (!USE_THREAD_LOCAL || IPVersionContext.current() == null) {
            System.setProperty("java.net.preferIPv4Addresses", "false");
            System.setProperty("java.net.preferIPv4Stack", "false");
            System.setProperty("java.net.preferIPv6Addresses", "true");
            System.setProperty("java.net.preferIPv6Stack", "true");
        }
    }

    public static void setStackToV4() {
        setNetworkProtocol(NetworkProtocol.IPv4);
        // Only set system properties in non-thread-local mode
        // In thread-local mode, IPVersionContext handles system properties
        if (!USE_THREAD_LOCAL || IPVersionContext.current() == null) {
            System.setProperty("java.net.preferIPv6Addresses", "false");
            System.setProperty("java.net.preferIPv6Stack", "false");
            System.setProperty("java.net.preferIPv4Addresses", "true");
            System.setProperty("java.net.preferIPv4Stack", "true");
        }
    }
}