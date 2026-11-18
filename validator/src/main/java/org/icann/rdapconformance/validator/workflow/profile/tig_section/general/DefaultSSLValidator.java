package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.icann.rdapconformance.validator.CommonUtils.TIMEOUT_IN_5SECS;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.icann.rdapconformance.validator.DNSCacheResolver;
import org.icann.rdapconformance.validator.NetworkProtocol;
import org.icann.rdapconformance.validator.QueryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of SSLValidator that performs actual SSL connections
 * and validation against real servers.
 *
 * <p>Every socket is opened against an address resolved through the configured
 * {@link DNSCacheResolver} for the active {@link NetworkProtocol} and, when built from a
 * {@link QueryContext}, checked with {@link QueryContext#isSsrfBlockedConnection} first.
 * The JDK's own hostname resolution is never used, so the TLS probes cannot reach an
 * address the HTTP layer would have refused.</p>
 */
public class DefaultSSLValidator implements SSLValidator {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSSLValidator.class);
    private static final String SSRF_BLOCKED_MESSAGE = "Connection blocked by SSRF protection for ";

    private final NetworkProtocol networkProtocol;
    private final DNSCacheResolver dnsResolver;
    /** (hostname, resolved address) -> true when the connection must be refused. */
    private final BiPredicate<String, InetAddress> ssrfBlockedPolicy;

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
     * Constructor with DNS resolver dependency injection. No SSRF policy is applied.
     */
    public DefaultSSLValidator(NetworkProtocol networkProtocol, DNSCacheResolver dnsResolver) {
        this(networkProtocol, dnsResolver, (host, addr) -> false);
    }

    /**
     * Builds a validator bound to a {@link QueryContext}: same address family, same DNS
     * resolver and same SSRF allowlist/blocklist as the HTTP requests of that context.
     */
    public DefaultSSLValidator(QueryContext queryContext) {
        this(queryContext.getNetworkProtocol(), queryContext.getDnsResolver(),
                queryContext::isSsrfBlockedConnection);
    }

    DefaultSSLValidator(NetworkProtocol networkProtocol, DNSCacheResolver dnsResolver,
                        BiPredicate<String, InetAddress> ssrfBlockedPolicy) {
        this.networkProtocol = networkProtocol;
        this.dnsResolver = dnsResolver;
        this.ssrfBlockedPolicy = ssrfBlockedPolicy;
    }

    @Override
    public SSLValidationResult validateSSL(String hostname, int port) {
        try {
            SSLContext sslContext = SSLContext.getDefault();

            InetAddress ipAddress = resolveHostname(hostname);
            if (ipAddress == null) {
                return SSLValidationResult.failure(
                    "Cannot resolve correct v4 or v6 host address for " + hostname, null);
            }
            if (ssrfBlockedPolicy.test(hostname, ipAddress)) {
                return SSLValidationResult.failure(SSRF_BLOCKED_MESSAGE + hostname, null);
            }

            List<String> enabledProtocols = getEnabledProtocols(sslContext, hostname, ipAddress, port);
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

    /** Opens a TCP socket to the pinned address and layers TLS on top, keeping SNI = hostname. */
    private SSLSocket openPinnedSslSocket(SSLContext sslContext, String hostname,
                                          InetAddress ipAddress, int port) throws IOException {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(ipAddress, port), TIMEOUT_IN_5SECS);
            logger.info("Connected to {} ({})", hostname, ipAddress.getHostAddress());
            return (SSLSocket) sslContext.getSocketFactory().createSocket(socket, hostname, port, true);
        } catch (IOException e) {
            socket.close();
            throw e;
        }
    }

    private List<String> getEnabledProtocols(SSLContext sslContext, String hostname,
                                           InetAddress ipAddress, int port) throws IOException {
        try (SSLSocket sslSocket = openPinnedSslSocket(sslContext, hostname, ipAddress, port)) {
            sslSocket.startHandshake();
            List<String> enabledProtocols = Arrays.asList(sslSocket.getEnabledProtocols());
            logger.debug("Enabled protocols: {}", enabledProtocols);
            return enabledProtocols;
        }
    }

    @Override
    public CipherValidationResult validateTLS12CipherSuites(String hostname, int port) {
        try {
            SSLContext sslContext = SSLContext.getDefault();

            InetAddress ipAddress = resolveHostname(hostname);
            if (ipAddress == null) {
                return CipherValidationResult.failure(
                    "Cannot resolve correct v4 or v6 host address for " + hostname, null);
            }
            if (ssrfBlockedPolicy.test(hostname, ipAddress)) {
                return CipherValidationResult.failure(SSRF_BLOCKED_MESSAGE + hostname, null);
            }

            try (SSLSocket sslSocket = openPinnedSslSocket(sslContext, hostname, ipAddress, port)) {
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
