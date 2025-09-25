package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.icann.rdapconformance.validator.CommonUtils.TIMEOUT_IN_5SECS;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
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
            logger.info("Error during SSL connection setup", e);
            return SSLValidationResult.failure("Error during SSL connection setup", e);
        }
    }
    
    private InetAddress resolveHostname(String hostname) {
        if (NetworkInfo.getNetworkProtocol() == NetworkProtocol.IPv6) {
            InetAddress ipAddress = DNSCacheResolver.getFirstV6Address(hostname);
            logger.info("Using IPv6 address {} for host {}", ipAddress, hostname);
            return ipAddress;
        } else {
            InetAddress ipAddress = DNSCacheResolver.getFirstV4Address(hostname);
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
            logger.info("Connection error during TLS 1.2 cipher validation", e);
            return CipherValidationResult.failure("Connection error during TLS 1.2 cipher validation", e);
        }
    }
}