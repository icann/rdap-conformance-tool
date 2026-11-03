package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.icann.rdapconformance.validator.CommonUtils.TIMEOUT_IN_5SECS;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.icann.rdapconformance.validator.DNSCacheResolver;
import org.icann.rdapconformance.validator.NetworkInfo;
import org.icann.rdapconformance.validator.NetworkProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of SSLValidator that performs actual SSL connections
 * and validation against real servers.
 */
public class DefaultSSLValidator implements SSLValidator {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSSLValidator.class);
    private final NetworkProtocol networkProtocol;
    private final DNSCacheResolver dnsResolver;

    /**
     * Default constructor using IPv4 protocol for backward compatibility.
     */
    public DefaultSSLValidator() {
        this(NetworkProtocol.IPv4, new DNSCacheResolver());
    }

    /**
     * Constructor that allows specifying the network protocol to use.
     */
    public DefaultSSLValidator(NetworkProtocol networkProtocol) {
        this(networkProtocol, new DNSCacheResolver());
    }

    /**
     * Constructor with DNS resolver dependency injection for QueryContext integration.
     */
    public DefaultSSLValidator(NetworkProtocol networkProtocol, DNSCacheResolver dnsResolver) {
        this.networkProtocol = networkProtocol;
        this.dnsResolver = dnsResolver;
    }
    
    @Override
    public SSLValidationResult validateSSL(String hostname, int port) {
        try {
            SSLContext sslContext = SSLContext.getDefault();
            
            // Resolve the hostname to an IP address
            InetAddress ipAddress = resolveHostname(hostname);
            if (ipAddress == null) {
                return SSLValidationResult.failure(
                    "Cannot resolve correct v4 or v6 host address for " + hostname, null);
            }
            
            // Create socket connection and get enabled protocols
            List<String> enabledProtocols = getEnabledProtocols(sslContext, hostname, ipAddress, port);
            
            // TLS 1.2 cipher suite validation is now handled through the public method
            
            return SSLValidationResult.success(enabledProtocols);
            
        } catch (NoSuchAlgorithmException e) {
            logger.info("Cannot create SSL context", e);
            return SSLValidationResult.failure("Cannot create SSL context", e);
        } catch (IOException e) {
            logger.debug("Error during SSL connection setup", e);
            return SSLValidationResult.failure("Error during SSL connection setup", e);
        }
    }
    
    private InetAddress resolveHostname(String hostname) {
        if (networkProtocol == NetworkProtocol.IPv6) {
            InetAddress ipAddress = dnsResolver.getFirstV6Address(hostname);
            logger.info("Using IPv6 address {} for host {}", ipAddress, hostname);
            return ipAddress;
        } else {
            InetAddress ipAddress = dnsResolver.getFirstV4Address(hostname);
            logger.info("Using IPv4 address {} for host {}", ipAddress, hostname);
            return ipAddress;
        }
    }
    
    private List<String> getEnabledProtocols(SSLContext sslContext, String hostname, 
                                           InetAddress ipAddress, int port) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ipAddress, port), TIMEOUT_IN_5SECS);
            logger.info("Connected to {} ({})", hostname, ipAddress.getHostAddress());
            
            try (SSLSocket sslSocket = (SSLSocket) sslContext.getSocketFactory()
                    .createSocket(socket, hostname, port, true)) {
                sslSocket.startHandshake();
                List<String> enabledProtocols = Arrays.asList(sslSocket.getEnabledProtocols());
                logger.debug("Enabled protocols: {}", enabledProtocols);
                return enabledProtocols;
            }
        }
    }
    
    @Override
    public CertificateExpirationResult checkCertificateExpiration(String hostname, int port) {
        InetAddress ipAddress = resolveHostname(hostname);
        if (ipAddress == null) {
            return CertificateExpirationResult.failure(
                "Cannot resolve correct v4 or v6 host address for " + hostname);
        }
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ipAddress, port), TIMEOUT_IN_5SECS);
            try (SSLSocket sslSocket = (SSLSocket) inspectionOnlySslContext().getSocketFactory()
                    .createSocket(socket, hostname, port, true)) {
                SSLParameters params = sslSocket.getSSLParameters();
                params.setServerNames(List.of(new SNIHostName(hostname)));
                sslSocket.setSSLParameters(params);
                sslSocket.startHandshake();
                Certificate[] chain = sslSocket.getSession().getPeerCertificates();
                if (chain.length == 0 || !(chain[0] instanceof X509Certificate leaf)) {
                    return CertificateExpirationResult.failure("No X.509 server certificate presented");
                }
                return isExpired(leaf) ? CertificateExpirationResult.expired() : CertificateExpirationResult.valid();
            }
        } catch (GeneralSecurityException | IOException e) {
            logger.debug("Error retrieving server certificate from {}:{}", hostname, port, e);
            return CertificateExpirationResult.failure("Error retrieving server certificate: " + e.getMessage());
        }
    }

    static boolean isExpired(X509Certificate certificate) {
        return certificate.getNotAfter().before(new Date());
    }

    /**
     * SSLContext whose trust manager accepts any chain so the handshake completes and the
     * server certificate can be read even when it is expired. The socket is closed right
     * after the handshake and no application data is ever exchanged over it, so skipping
     * chain validation here does not weaken any trust decision.
     */
    private static SSLContext inspectionOnlySslContext() throws GeneralSecurityException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[] {new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                // not used: this context never acts as a server
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                // accept: the chain is inspected after the handshake, see Javadoc
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }}, null);
        return context;
    }

    @Override
    public CipherValidationResult validateTLS12CipherSuites(String hostname, int port) {
        try {
            SSLContext sslContext = SSLContext.getDefault();
            
            try (SSLSocket sslSocket = (SSLSocket) sslContext.getSocketFactory()
                    .createSocket(hostname, port)) {
                sslSocket.setEnabledProtocols(new String[]{"TLSv1.2"});
                sslSocket.startHandshake();
                SSLSession sslSession = sslSocket.getSession();
                
                String protocol = sslSession.getProtocol();
                String cipher = sslSession.getCipherSuite();
                logger.debug("cipher for protocol {} is {}", protocol, cipher);
                
                return CipherValidationResult.success(protocol, cipher);
            }
        } catch (NoSuchAlgorithmException e) {
            logger.info("Cannot create SSL context for TLS 1.2 cipher validation", e);
            return CipherValidationResult.failure("Cannot create SSL context for TLS 1.2 cipher validation", e);
        } catch (IOException e) {
            logger.debug("Connection error during TLS 1.2 cipher validation", e);
            return CipherValidationResult.failure("Connection error during TLS 1.2 cipher validation", e);
        }
    }
}