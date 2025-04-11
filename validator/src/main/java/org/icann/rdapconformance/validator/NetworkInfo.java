package org.icann.rdapconformance.validator;

import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

public class NetworkInfo {
    private static final NetworkInfo instance = new NetworkInfo();
    private String acceptHeader;
    private String httpMethod;
    private String serverIpAddress;
    private NetworkProtocol networkProtocol;


    private NetworkInfo() {}

    public static NetworkInfo getInstance() {
        return instance;
    }

    // Static Getters
    public static String getAcceptHeader() {
        return (instance.acceptHeader == null || instance.acceptHeader.isEmpty()) ? "-" : instance.acceptHeader;
    }

    public static String getHttpMethod() {
        return (instance.httpMethod == null || instance.httpMethod.isEmpty()) ? "-" : instance.httpMethod;
    }

    public static String getServerIpAddress() {
        return (instance.serverIpAddress == null || instance.serverIpAddress.isEmpty()) ? "-" : instance.serverIpAddress;
    }

    public static NetworkProtocol getNetworkProtocol() {
        return instance.networkProtocol;
    }

    public static String getNetworkProtocolAsString() {
        return (instance.networkProtocol == null) ? "-" : instance.networkProtocol.name();
    }

    // Static Setters
    public static void setAcceptHeader(String acceptHeader) {
        instance.acceptHeader = acceptHeader;
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


    public static void checkNetworkLayer()  {

        try {
            // Create a dummy outbound connection to a remote host
            // (this triggers real routing so we can see what the OS picks)
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName("google.com"), 80);
                InetAddress localAddress = socket.getLocalAddress();
                System.out.println("Local outbound IP address: " + localAddress.getHostAddress());

                if (localAddress instanceof Inet6Address) {
                    System.out.println("✅ Using IPv6 for outbound traffic.");
                } else if( localAddress instanceof Inet4Address) {
                    System.out.println("✅ Using IPv4 for outbound traffic.");
                } else {
                    System.out.println("❌ Unknown network layer.");
                }
            }
        } catch (Exception e) {
            System.out.println(" Not using IPv6.");
        }
    }
}