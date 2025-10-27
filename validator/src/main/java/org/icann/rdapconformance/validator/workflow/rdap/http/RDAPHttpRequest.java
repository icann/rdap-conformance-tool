package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.icann.rdapconformance.validator.CommonUtils.EMPTY_STRING;
import static org.icann.rdapconformance.validator.CommonUtils.GET;
import static org.icann.rdapconformance.validator.CommonUtils.HEAD;
import static org.icann.rdapconformance.validator.CommonUtils.HTTPS_PORT;
import static org.icann.rdapconformance.validator.CommonUtils.HTTP_PORT;
import static org.icann.rdapconformance.validator.CommonUtils.HTTP_TOO_MANY_REQUESTS;
import static org.icann.rdapconformance.validator.CommonUtils.LOCALHOST;
import static org.icann.rdapconformance.validator.CommonUtils.LOCAL_IPv4;
import static org.icann.rdapconformance.validator.CommonUtils.LOCAL_IPv6;
import static org.icann.rdapconformance.validator.CommonUtils.ONE;
import static org.icann.rdapconformance.validator.CommonUtils.PAUSE;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;
// REMOVED: addErrorToResultsFile import - use queryContext.addError() instead

import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;

import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.MalformedChunkCodingException;
import org.apache.hc.core5.http.MessageConstraintException;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.TruncatedChunkException;
import org.apache.hc.core5.http.ConnectionClosedException;

import org.icann.rdapconformance.validator.ConnectionStatus;
import org.icann.rdapconformance.validator.ConnectionTracker;
import org.icann.rdapconformance.validator.DNSCacheResolver;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.NetworkInfo;
import org.icann.rdapconformance.validator.NetworkProtocol;

import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;

/**
 * HTTP request handler for RDAP validation with comprehensive connection management and error handling.
 *
 * <p>This class provides the core HTTP functionality for RDAP validation, including connection pooling,
 * SSL/TLS certificate validation, IPv4/IPv6 dual-stack support, retry logic, and detailed error
 * classification. It handles all network communication aspects required for RDAP conformance testing.</p>
 *
 * <p>Key features include:</p>
 * <ul>
 *   <li>HTTP GET and HEAD request support with configurable timeouts</li>
 *   <li>Automatic retry handling for 429 (Too Many Requests) responses</li>
 *   <li>IPv4/IPv6 dual-stack networking with proper local address binding</li>
 *   <li>Custom SSL/TLS certificate validation for RDAP requirements</li>
 *   <li>Comprehensive error classification and reporting</li>
 *   <li>Connection pooling and tracking for performance and debugging</li>
 *   <li>SNI (Server Name Indication) support for proper hostname verification</li>
 * </ul>
 *
 * <p>The class integrates tightly with the ConnectionTracker for monitoring connection
 * state and the NetworkInfo configuration.
 * All requests are tracked with unique identifiers for debugging and correlation.</p>
 *
 * <p>SSL/TLS validation is customized to focus on leaf certificate validation while
 * handling self-signed and untrusted root scenarios common in RDAP testing environments.
 * The class also performs proper hostname verification using SNI.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * URI rdapUri = URI.create("https://rdap.example.com/domain/test.com");
 * HttpResponse&lt;String&gt; response = RDAPHttpRequest.makeHttpGetRequest(rdapUri, 30);
 * int statusCode = response.statusCode();
 * String responseBody = response.body();
 * </pre>
 *
 * @see ConnectionTracker
 * @see NetworkInfo
 * @see HttpClientManager
 * @since 1.0.0
 */
public class RDAPHttpRequest {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RDAPHttpRequest.class);
    public static final String HOST = "Host";
    public static final String ACCEPT = "Accept";
    public static final String CONNECTION = "Connection";
    public static final String CLOSE = "close";

    public static final String RETRY_AFTER = "Retry-After";
    public static final int MAX_RETRY_TIME = 120;

    // NOTE: Mutable for testing purposes - allows test timeouts to be reduced from 30 seconds to 1 second
    // This field is not modified in production code, only in test environments
    public static int DEFAULT_BACKOFF_SECS = 30;
    public static final int MAX_RETRIES = 1;
    public static final int DNS_PORT = 53;
    public static final String OUTGOING_IPV4 = "9.9.9.9";
    public static final String OUTGOING_V6 = "2620:fe::9";

    /**
     * Creates and executes an HTTP GET request to the specified URI.
     *
     * <p>This convenience method creates a GET request with the specified timeout
     * and delegates to the main request method.</p>
     *
     * @param uri the URI to send the GET request to
     * @param timeoutSeconds the timeout in seconds for both connection and response
     * @return HttpResponse containing the response data and metadata
     * @throws Exception if the request fails due to network or other issues
     */
    public static HttpResponse<String> makeHttpGetRequest(URI uri, int timeoutSeconds) throws Exception {
        return makeRequest(null, uri, timeoutSeconds, GET);
    }

    /**
     * Creates and executes an HTTP HEAD request to the specified URI.
     *
     * <p>This convenience method creates a HEAD request with the specified timeout
     * and delegates to the main request method. HEAD requests return only headers
     * without a response body.</p>
     *
     * @param uri the URI to send the HEAD request to
     * @param timeoutSeconds the timeout in seconds for both connection and response
     * @return HttpResponse containing the response headers and metadata (no body)
     * @throws Exception if the request fails due to network or other issues
     */
    public static HttpResponse<String> makeHttpHeadRequest(URI uri, int timeoutSeconds) throws Exception {
        return makeRequest(null, uri, timeoutSeconds, HEAD);
    }




    private static SSLConnectionSocketFactory getSslConnectionSocketFactory(String host, SSLContext sslContext) {
        // This is critical - use the original hostname for verification
        // Set the SNI hostname to the original hostname, not the IP
        return new SSLConnectionSocketFactory(sslContext,
            new String[] { "TLSv1.3", "TLSv1.2" },
            null, // Use default cipher suites
            (hostname, session) -> {
                DefaultHostnameVerifier verifier = new DefaultHostnameVerifier();
                try {
                    return verifier.verify(host, session);
                } catch (Exception e) {
                    logger.debug("Hostname verification failed for: {}", host, e);
                    return false;
                }
            }) {
            @Override
            protected void prepareSocket(SSLSocket socket) throws IOException {
                SSLParameters sslParameters = socket.getSSLParameters();
                sslParameters.setServerNames(Collections.singletonList(new SNIHostName(host)));
                socket.setSSLParameters(sslParameters);
            }

        };
    }

    /**
     * Executes an HTTP request using the provided client and request configuration.
     *
     * <p>This method performs the actual HTTP request execution using Apache HttpClient.
     * It's separated out to allow for easier testing and potential customization of
     * the request execution process.</p>
     *
     * @param client the HTTP client to use for the request
     * @param request the configured HTTP request to execute
     * @return the HTTP response from the server
     * @throws IOException if the request execution fails due to I/O issues
     */
    public static ClassicHttpResponse executeRequest(CloseableHttpClient client, HttpUriRequestBase request) throws IOException {
        return client.execute(request);
    }

    /**
     * Handles and classifies exceptions that occur during HTTP request execution.
     *
     * <p>This method provides comprehensive exception handling and classification for
     * all types of network, SSL/TLS, and HTTP protocol errors that can occur during
     * RDAP validation. It maps specific exception types to appropriate ConnectionStatus
     * values and optionally records errors in the validation results.</p>
     *
     * <p>Handled exception categories include:</p>
     * <ul>
     *   <li>HTTP protocol errors (malformed responses, protocol violations)</li>
     *   <li>SSL/TLS certificate issues (expired, revoked, invalid, handshake failures)</li>
     *   <li>Network connectivity problems (timeouts, connection refused, host unreachable)</li>
     *   <li>DNS resolution failures (unknown host)</li>
     * </ul>
     *
     * @param queryContext the QueryContext for thread-safe error reporting and connection tracking
     * @param e the exception that occurred during request execution
     * @param recordError whether to record the error in validation results
     * @return ConnectionStatus representing the classified error type
     */
    public static ConnectionStatus handleRequestException(QueryContext queryContext, Exception e, boolean recordError) {
        String exceptionString = e.toString();

        if (e instanceof NoHttpResponseException ||
            e instanceof ProtocolException ||
            e instanceof MalformedChunkCodingException ||
            e instanceof MessageConstraintException ||
            e instanceof TruncatedChunkException ||
            // Also check causes for wrapped exceptions
            (e.getCause() instanceof ProtocolException) ||
            (e.getCause() instanceof MalformedChunkCodingException) ||
            (e.getCause() instanceof MessageConstraintException) ||
            (e.getCause() instanceof TruncatedChunkException)) {
            if (recordError) {
                queryContext.addError(ZERO, -13014, "no response available", "HTTP error.");
            }
            queryContext.getConnectionTracker().updateCurrentConnection(ConnectionStatus.HTTP_ERROR);
            return ConnectionStatus.HTTP_ERROR;
        }

        if (e instanceof UnknownHostException) {
            queryContext.getConnectionTracker().updateCurrentConnection(ConnectionStatus.UNKNOWN_HOST);
            return ConnectionStatus.UNKNOWN_HOST;
        }

        if (exceptionString.contains("Connection refused")) {
            if (recordError) {
                queryContext.addError(ZERO, -13021, "no response available", "Connection refused by host.");
            }
            queryContext.getConnectionTracker().updateCurrentConnection(ConnectionStatus.CONNECTION_REFUSED);
            return ConnectionStatus.CONNECTION_REFUSED;
        }

        if (e instanceof ConnectException || e instanceof HttpTimeoutException) {
                if(recordError) {
                    queryContext.addError(ZERO, -13007, "no response available", "Failed to connect to server.");
                }
                queryContext.getConnectionTracker().updateCurrentConnection(ConnectionStatus.CONNECTION_FAILED);
                return ConnectionStatus.CONNECTION_FAILED;
        }

        // SSL and TLS related exceptions
        if (hasCause(e, "java.security.cert.CertificateExpiredException")) {
            if(recordError) {
                queryContext.addError(ZERO, -13011, "no response available", "Expired certificate.");
            }
            queryContext.getConnectionTracker().updateCurrentConnection(ConnectionStatus.EXPIRED_CERTIFICATE);
            return ConnectionStatus.EXPIRED_CERTIFICATE;
        } else if (hasCause(e, "java.security.cert.CertificateRevokedException") || exceptionString.contains("CertificateRevokedException") ||  exceptionString.contains("Certificate revoked")) {
            if(recordError) {
                queryContext.addError(ZERO, -13010, "no response available", "Revoked TLS certificate.");
            }
            queryContext.getConnectionTracker().updateCurrentConnection(ConnectionStatus.REVOKED_CERTIFICATE);
            return ConnectionStatus.REVOKED_CERTIFICATE;
        }
        else if (hasCause(e, "javax.net.ssl.SSLHandshakeException") || e.toString().contains("SSLHandshakeException")) {
            if(recordError) {
                queryContext.addError(ZERO, -13008, "no response available", "TLS handshake failed.");
            }
            queryContext.getConnectionTracker().updateCurrentConnection(ConnectionStatus.HANDSHAKE_FAILED);
            return ConnectionStatus.HANDSHAKE_FAILED;
        }
        else if (hasCause(e, "javax.net.ssl.SSLPeerUnverifiedException") || e.toString().contains("SSLPeerUnverifiedException")) {
                if(recordError) {
                    queryContext.addError(ZERO, -13009, "no response available", "Invalid TLS certificate.");
                }
                queryContext.getConnectionTracker().updateCurrentConnection(ConnectionStatus.INVALID_CERTIFICATE);
                return ConnectionStatus.INVALID_CERTIFICATE;
        } else if (hasCause(e, "sun.security.validator.ValidatorException") || hasCause(e, "java.security.cert.CertificateException") ) {
            // else it's just a generic certificate error and falls under the certificate error category
            queryContext.addError(ZERO,-13012, "no response available", "TLS certificate error.");
            queryContext.getConnectionTracker().updateCurrentConnection(ConnectionStatus.CERTIFICATE_ERROR);
            return ConnectionStatus.CERTIFICATE_ERROR;
        }

        // Handle network timeouts and connection issues
        if (e instanceof SocketTimeoutException) {
            boolean isReadTimeout = e.getMessage() != null && e.getMessage().contains("Read timed out");

            if (isReadTimeout) {
                // Read timeout = network receive failure (-13017)
                if (recordError) {
                    queryContext.addError(ZERO, -13017, "no response available", "Network receive fail");
                }
                queryContext.getConnectionTracker().updateCurrentConnection(ConnectionStatus.NETWORK_RECEIVE_FAIL);
                return ConnectionStatus.NETWORK_RECEIVE_FAIL;
            } else {
                // Other socket timeouts = network send failure (-13016)
                if (recordError) {
                    queryContext.addError(ZERO, -13016, "no response available", "Network send fail");
                }
                queryContext.getConnectionTracker().updateCurrentConnection(ConnectionStatus.NETWORK_SEND_FAIL);
                return ConnectionStatus.NETWORK_SEND_FAIL;
            }
        }

        // Network failures
        if (e instanceof EOFException || e instanceof ConnectionClosedException ||
            (exceptionString != null && (
                exceptionString.contains("Connection reset") ||
                    exceptionString.contains("Connection closed by peer")
            ))) {

            if (recordError) {
                queryContext.addError(ZERO, -13017, "no response available", "Network receive fail");
            }
            queryContext.getConnectionTracker().updateCurrentConnection(ConnectionStatus.NETWORK_RECEIVE_FAIL);
            return ConnectionStatus.NETWORK_RECEIVE_FAIL;
        }
        // we are at the fall through point, which means we have not identified a specific cause, and it gets classified as a connection failure
        if(recordError) {
            queryContext.addError(ZERO,-13007, "no response available", "Failed to connect to server.");
        }
        queryContext.getConnectionTracker().updateCurrentConnection(ConnectionStatus.CONNECTION_FAILED);
        return ConnectionStatus.CONNECTION_FAILED;
    }

    /**
     * Checks if an exception's cause chain contains a specific exception class.
     *
     * <p>This utility method traverses the exception cause chain to determine if
     * any cause in the chain matches the specified class name. This is useful
     * for identifying wrapped exceptions that may be buried several levels deep.</p>
     *
     * @param e the exception to examine
     * @param causeClassName the fully qualified class name to search for
     * @return true if the cause chain contains the specified exception class, false otherwise
     */
    public static boolean hasCause(Throwable e, String causeClassName) {
        while (e.getCause() != null) {
            if (e.getCause().getClass().getName().equals(causeClassName)) {
                return true;
            }
            e = e.getCause();
        }
        return false;
    }

    /**
     * Determines the default IPv4 address for outbound connections.
     *
     * <p>This method uses a dummy UDP connection to an external address to determine
     * the local IPv4 address that would be used for outbound connections. This is
     * essential for proper local address binding in dual-stack environments.</p>
     *
     * @return the default IPv4 address for outbound connections, or null if IPv4 is not available
     * @throws IOException if the address determination fails
     */
    public static InetAddress getDefaultIPv4Address() throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName(OUTGOING_IPV4), DNS_PORT);
            InetAddress localAddress = socket.getLocalAddress();
            if (localAddress instanceof Inet4Address) {
                logger.debug("using local IPv4 Address: {}", localAddress.getHostAddress());
                return localAddress;
            } else {
                throw new IOException("No IPv4 address found");
            }
        } catch (Exception e) {
            logger.warn("Failed to get IPv4 address: {}, skipping IPv4 checks", e.getMessage());
            return null;
        }
    }

 /**
  * Determines the default IPv6 address for outbound connections.
  *
  * <p>This method uses a dummy UDP connection to an external IPv6 address to determine
  * the local IPv6 address that would be used for outbound connections. This is
  * essential for proper local address binding in dual-stack environments.</p>
  *
  * @return the default IPv6 address for outbound connections, or null if IPv6 is not available
  * @throws IOException if the address determination fails
  */
 public static InetAddress getDefaultIPv6Address() throws IOException {
     try (DatagramSocket socket = new DatagramSocket()) {
         socket.connect(InetAddress.getByName(OUTGOING_V6), DNS_PORT);
         InetAddress localAddress = socket.getLocalAddress();
         if (localAddress instanceof Inet6Address) {
             logger.debug("using local IPv6 Address: {}", localAddress.getHostAddress());
             return localAddress;
         } else {
             throw new IOException("No IPv6 address found");
         }
     } catch (Exception e) {
         logger.warn("Failed to get IPv6 address: {}, skipping IPv6 checks", e.getMessage());
         return null;
     }
 }

    private static long getBackoffTime(org.apache.hc.core5.http.Header[] headers) {
        String retryAfter = headers == null ? null :
            java.util.Arrays.stream(headers)
                            .filter(header -> RETRY_AFTER.equalsIgnoreCase(header.getName()))
                            .map(org.apache.hc.core5.http.Header::getValue)
                            .findFirst()
                            .orElse(null);
        if (retryAfter != null) {
            try {
                long value = Long.parseLong(retryAfter);
                if (value > ZERO) {
                    if(value > MAX_RETRY_TIME) {
                        value = MAX_RETRY_TIME; // Cap the retry-after to MAX_RETRY_TIME(120) seconds
                    }
                    value  = value + ONE; // no matter what, we add 1 second to the retry-after value
                    logger.debug("Received 429 with retry-after header. Waiting {} seconds to requery.", value);
                    return value;
                }
            } catch (NumberFormatException ignored) {}
        }
        logger.debug("Received 429 but no retry-after header was offered. Waiting {} seconds.", DEFAULT_BACKOFF_SECS);
        return DEFAULT_BACKOFF_SECS;
    }

    /**
     * Creates a custom X.509 trust manager for RDAP certificate validation.
     *
     * <p>This trust manager is specifically designed for RDAP validation scenarios
     * where leaf certificate validation is required but self-signed and untrusted
     * root certificates should be handled gracefully. It performs comprehensive
     * validation including expiration checks, revocation status, and SAN validation.</p>
     *
     * <p>Validation features:</p>
     * <ul>
     *   <li>Leaf certificate expiration validation</li>
     *   <li>Certificate revocation checking when possible</li>
     *   <li>Subject Alternative Name (SAN) extraction and logging</li>
     *   <li>Graceful handling of self-signed certificates</li>
     *   <li>Detailed certificate information logging for debugging</li>
     * </ul>
     *
     * @return X509TrustManager configured for RDAP validation requirements
     * @throws Exception if trust manager creation fails
     */
    public static X509TrustManager createLeafValidatingTrustManager() throws Exception {
        // Get the default trust manager
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore) null);
        X509TrustManager defaultTm = null;
        for (TrustManager tm : tmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                defaultTm = (X509TrustManager) tm;
                break;
            }
        }

        final X509TrustManager finalDefaultTm = defaultTm;

        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                // Skip client certs (unused in typical HTTPS)
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                if (chain == null || chain.length == ZERO) {
                    throw new CertificateException("Empty certificate chain");
                }

                X509Certificate leaf = chain[0];

                logger.debug("---> Leaf Cert <---");
                logger.debug("Subject: " + leaf.getSubjectX500Principal());
                logger.debug("Issuer:  " + leaf.getIssuerX500Principal());
                logger.debug("Not Before: " + leaf.getNotBefore());
                logger.debug("Not After:  " + leaf.getNotAfter());

                // Check expiration
                try {
                    leaf.checkValidity(new Date());
                } catch (CertificateExpiredException e) {
                    throw new CertificateExpiredException("Leaf certificate expired: " +
                        leaf.getSubjectX500Principal().getName());
                }

                // Optionally check SANs
                try {
                    Collection<List<?>> sans = leaf.getSubjectAlternativeNames();
                    if (sans != null) {
                        for (List<?> san : sans) {
                            logger.debug("SAN: " + san.get(1));
                        }
                    }
                } catch (CertificateParsingException ex) {
                    logger.debug("Failed to parse SANs from leaf cert"); // don't care, log and move on
                }

                // Check revocation status, but catch and rethrow only revocation errors
                try {
                    // Use the default trust manager to check the cert chain
                    finalDefaultTm.checkServerTrusted(chain, authType);
                } catch (Exception e) {
                    // If it's a revocation error, rethrow it
                    if (e.getMessage() != null && e.getMessage().contains("revoked")) {
                        throw new CertificateException("Certificate revoked: " +
                            leaf.getSubjectX500Principal().getName());
                    }

                    // If it contains "path building" or "unknown CA", those are untrusted root errors, ignore them
                    if (e.getMessage() != null &&
                        (e.getMessage().contains("PKIX path building failed") ||
                            e.getMessage().contains("unable to find valid certification path") ||
                            e.getMessage().contains("unknown CA") ||
                            e.getMessage().contains("self signed certificate"))) {
                        // Ignore untrusted root errors
                        logger.debug("Ignoring untrusted root error: {}", e.getMessage());
                    } else {
                        // Rethrow other errors (like hostname verification issues)
                        throw e;
                    }
                }
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[ZERO];
            }
        };
    }

    private static RDAPHttpRequest.Header[] convertHeaders(org.apache.hc.core5.http.Header[] headers) {
        if (headers == null) return new RDAPHttpRequest.Header[0];
        RDAPHttpRequest.Header[] result = new RDAPHttpRequest.Header[headers.length];
        for (int i = ZERO; i < headers.length; i++) {
            result[i] = new RDAPHttpRequest.Header(headers[i].getName(), headers[i].getValue());
        }
        return result;
    }

    /**
     * Simple implementation of HttpResponse for RDAP validation requirements.
     *
     * <p>This class provides a lightweight HttpResponse implementation that captures
     * all the essential information needed for RDAP validation including status code,
     * response body, headers, and connection tracking information. It supports case-
     * insensitive header handling and integrates with the connection tracking system.</p>
     *
     * <p>Key features:</p>
     * <ul>
     *   <li>Case-insensitive HTTP header handling</li>
     *   <li>Connection status tracking integration</li>
     *   <li>Support for all standard HttpResponse methods</li>
     *   <li>Lightweight implementation optimized for RDAP validation</li>
     * </ul>
     */
    public static class SimpleHttpResponse implements HttpResponse<String> {
        private final int statusCode;
        private ConnectionStatus connectionStatus;
        private final String body;
        private final URI uri;
        private final Map<String, List<String>> headers;
        private final String trackingId;

        /**
         * Creates a new SimpleHttpResponse with the provided response information.
         *
         * @param trackingId the unique identifier for tracking this connection
         * @param statusCode the HTTP status code from the response
         * @param body the response body content
         * @param uri the URI that was requested
         * @param headers the HTTP headers from the response
         */
        public SimpleHttpResponse(String trackingId, int statusCode, String body, URI uri, Header[] headers) {
            this.statusCode = statusCode;
            this.body = body;
            this.uri = uri;
            this.trackingId = trackingId;

            // Use case-insensitive header map to handle servers that send headers with different cases
            Map<String, List<String>> headersMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            if (headers != null) {
                for (Header header : headers) {
                    headersMap.computeIfAbsent(header.getName(), k -> new ArrayList<>())
                              .add(header.getValue());
                }
            }
            this.headers = headersMap;
        }

        /**
         * Returns the unique tracking identifier for this HTTP response.
         *
         * @return the tracking ID used for connection monitoring and correlation
         */
        public String getTrackingId() { return trackingId; }

        /**
         * Sets the connection status for this response.
         *
         * @param status the ConnectionStatus indicating the result of the connection attempt
         */
        public void setConnectionStatusCode(ConnectionStatus status) {
            this.connectionStatus = status;
        }

        /**
         * Returns the connection status for this response.
         *
         * @return the ConnectionStatus indicating the result of the connection attempt
         */
        public ConnectionStatus getConnectionStatusCode() {
           return connectionStatus;
        }

        @Override
        public int statusCode() {
            return statusCode;
        }

        @Override
        public String body() {
            return body;
        }

        @Override
        public HttpRequest request() {
            return null; // Not implemented
        }

        @Override
        public Optional<HttpResponse<String>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            // The headers map is now case-insensitive, so duplicate header names with different cases
            // (e.g., "set-cookie" and "Set-Cookie") are automatically merged into a single entry
            return HttpHeaders.of(headers, (k, v) -> true);
        }

        @Override
        public URI uri() {
            return uri;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty(); // Not implemented
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1; // Default version
        }
    }

    /**
     * Simple representation of an HTTP header name-value pair.
     *
     * <p>This class provides a basic container for HTTP header information
     * used within the RDAP HTTP request/response handling. It stores the
     * header name and value as immutable strings.</p>
     */
    public static class Header {
        private final String name;
        private final String value;

        /**
         * Creates a new HTTP header with the specified name and value.
         *
         * @param name the header name
         * @param value the header value
         */
        public Header(String name, String value) {
            this.name = name;
            this.value = value;
        }

        /**
         * Returns the header name.
         *
         * @return the header name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the header value.
         *
         * @return the header value
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * Makes an HTTP GET request with optional redirect following.
     * This is specifically for case folding validation where redirects need to be followed.
     */
    public static HttpResponse<String> makeHttpGetRequestWithRedirects(URI uri, int timeoutSeconds, int maxRedirects) throws Exception {
        // TEMPORARY: Simple implementation without redirect following - redirects are now handled in RDAPHttpQuery
        return makeRequest(null, uri, timeoutSeconds, GET, false, true);
    }



    // ========================================
    // QUERYCONTEXT-ENABLED METHODS (PHASE 2)
    // ========================================

    /**
     * QueryContext-enabled version of makeRequest.
     * Uses QueryContext's ConnectionTracker and NetworkInfo.
     */
    public static HttpResponse<String> makeRequest(QueryContext qctx, URI originalUri, int timeoutSeconds, String method) throws Exception {
        return makeRequest(qctx, originalUri, timeoutSeconds, method, false);
    }

    /**
     * QueryContext-enabled version of makeRequest with isMain flag.
     */
    public static HttpResponse<String> makeRequest(QueryContext qctx, URI originalUri, int timeoutSeconds, String method, boolean isMain) throws Exception {
        return makeRequest(qctx, originalUri, timeoutSeconds, method, isMain, true);
    }

    /**
     * QueryContext-enabled version of makeRequest with full parameters.
     * This method uses QueryContext services for thread-safe operations.
     */
    public static HttpResponse<String> makeRequest(QueryContext qctx, URI originalUri, int timeoutSeconds, String method, boolean isMain, boolean canRecordError) throws Exception {
        if (originalUri == null) throw new IllegalArgumentException("The provided URI is null.");

        // Use QueryContext's ConnectionTracker
        ConnectionTracker tracker = qctx.getConnectionTracker();
        String trackingId = tracker.startTrackingNewConnection(originalUri, method, isMain, qctx.getNetworkProtocol());

        String host = originalUri.getHost();
        if (LOCALHOST.equalsIgnoreCase(host)) {
            host = LOCAL_IPv4; // only do v4, no dual-stack binding
        }

        int port = originalUri.getPort() == -1 ? (originalUri.getScheme().equalsIgnoreCase("https") ? HTTPS_PORT : HTTP_PORT) : originalUri.getPort();
        logger.debug("Port calculation: originalUri.getPort()={}, scheme={}, calculated port={}", originalUri.getPort(), originalUri.getScheme(), port);

        if (qctx.getDnsResolver().hasNoAddresses(host)) {
            logger.debug("No IP address found for host: " + host);
            tracker.completeTrackingById(trackingId, ZERO, ConnectionStatus.UNKNOWN_HOST);
            SimpleHttpResponse resp = new SimpleHttpResponse(trackingId,ZERO, EMPTY_STRING, originalUri, new Header[ZERO]);
            resp.setConnectionStatusCode(ConnectionStatus.UNKNOWN_HOST);
            return resp;
        }

        // Use QueryContext's NetworkProtocol
        NetworkProtocol protocol = qctx.getNetworkProtocol();
        InetAddress localBindIp = (protocol == NetworkProtocol.IPv6)
                ? getDefaultIPv6Address()
                : getDefaultIPv4Address();

        InetAddress remoteAddress = (protocol == NetworkProtocol.IPv6)
                ? qctx.getDnsResolver().getFirstV6Address(host)
                : qctx.getDnsResolver().getFirstV4Address(host);

        // If remote address or local bind IP is null, treat as unknown host
        if (remoteAddress == null || localBindIp == null) {
            logger.debug("Unable to resolve addresses for {} - protocol {}, localBindIp: {}, remoteAddress: {}",
                host, protocol, localBindIp, remoteAddress);
            tracker.completeTrackingById(trackingId, ZERO, ConnectionStatus.UNKNOWN_HOST);
            SimpleHttpResponse resp = new SimpleHttpResponse(trackingId, ZERO, EMPTY_STRING, originalUri, new Header[ZERO]);
            resp.setConnectionStatusCode(ConnectionStatus.UNKNOWN_HOST);
            return resp;
        }

        // Set network info in QueryContext
        qctx.setServerIpAddress(remoteAddress.getHostAddress());
        qctx.setHttpMethod(method);

        logger.debug("Connecting to: {} using {} (local bind: {})", remoteAddress.getHostAddress(), protocol, localBindIp.getHostAddress());

        // Continue with rest of method logic using QueryContext services...
        // (For now, delegate to existing method until we fully refactor the rest)
        return makeRequestInternal(qctx, originalUri, timeoutSeconds, method, isMain, canRecordError, tracker, trackingId, localBindIp, remoteAddress, port);
    }

    /**
     * Internal method to complete the QueryContext-enabled request.
     * This will contain the rest of the HTTP processing logic.
     */
    private static HttpResponse<String> makeRequestInternal(QueryContext qctx, URI originalUri, int timeoutSeconds, String method,
            boolean isMain, boolean canRecordError, ConnectionTracker tracker, String trackingId,
            InetAddress localBindIp, InetAddress remoteAddress, int port) throws Exception {

        logger.debug("makeRequestInternal called with protocol: {}, localBindIp: {}, remoteAddress: {}", qctx.getNetworkProtocol(), localBindIp, remoteAddress);

        // Use QueryContext services
        String host = originalUri.getHost();
        if (LOCALHOST.equalsIgnoreCase(host)) {
            host = LOCAL_IPv4; // only do v4, no dual-stack binding
        }

        // Get SSL context and create HTTP client using QueryContext services
        X509TrustManager leafCheckingTm = createLeafValidatingTrustManager();
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[] { leafCheckingTm }, new SecureRandom());

        // Use QueryContext's HttpClientManager
        CloseableHttpClient client = qctx.getHttpClientManager().getClient(host, sslContext, localBindIp, timeoutSeconds);

        // Continue with the HTTP request logic using QueryContext services
        return executeHttpRequest(qctx, client, originalUri, host, method, tracker, trackingId, localBindIp, remoteAddress, port, timeoutSeconds);
    }

    /**
     * Executes the HTTP request using QueryContext services.
     */
    private static HttpResponse<String> executeHttpRequest(QueryContext qctx, CloseableHttpClient client, URI originalUri,
            String host, String method, ConnectionTracker tracker, String trackingId, InetAddress localBindIp, InetAddress remoteAddress, int port, int timeoutSeconds) throws Exception {

        logger.debug("executeHttpRequest called with protocol: {}, localBindIp: {}, remoteAddress: {}", qctx.getNetworkProtocol(), localBindIp, remoteAddress);

        // Update QueryContext network info
        qctx.setServerIpAddress(remoteAddress.getHostAddress());
        qctx.setHttpMethod(method);
        tracker.updateIPAddressById(trackingId, remoteAddress.getHostAddress());
        logger.debug("Connecting to: {} using {} (local bind: {})", remoteAddress.getHostAddress(), qctx.getNetworkProtocol(), localBindIp.getHostAddress());

        // Create URI with IP address (like the working implementation)
        URI ipUri;
        try {
            ipUri = new URI(
                originalUri.getScheme(),
                null,
                remoteAddress.getHostAddress(),
                port,
                originalUri.getRawPath(),
                originalUri.getRawQuery(),
                originalUri.getRawFragment()
            );
            logger.debug("Created IP URI: {}", ipUri);
        } catch (Exception e) {
            logger.debug("Failed to create IP URI, using original: {}", e.getMessage());
            ipUri = originalUri;
        }

        // Create request with original URI, then set IP URI (like working implementation)
        HttpUriRequestBase request = method.equals(GET) ? new HttpGet(originalUri) : new HttpHead(originalUri);
        request.setHeader(HOST, host);  // Use original hostname for Host header
        request.setHeader(ACCEPT, qctx.getAcceptHeader());
        request.setHeader("Connection", "close");
        request.setUri(ipUri);  // Set the IP URI for actual connection

        // Set request configuration for timeouts (like working implementation)
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(Timeout.of(timeoutSeconds, TimeUnit.SECONDS))
                .setResponseTimeout(Timeout.of(timeoutSeconds, TimeUnit.SECONDS))
                .build();
        request.setConfig(config);

        int attempt = ZERO;
        while (attempt <= MAX_RETRIES) {
            try {
                CloseableHttpResponse response = client.execute(request);
                int statusCode = response.getCode();

                // Get response body
                String responseBody = EMPTY_STRING;
                if (response.getEntity() != null) {
                    responseBody = EntityUtils.toString(response.getEntity());
                }

                // Get response headers and convert them
                RDAPHttpRequest.Header[] headers = convertHeaders(response.getHeaders());

                tracker.completeTrackingById(trackingId, statusCode, ConnectionStatus.SUCCESS);
                return new SimpleHttpResponse(trackingId, statusCode, responseBody, originalUri, headers);

            } catch (Exception e) {
                logger.debug("HTTP request failed on attempt {}: {}", attempt + 1, e.getMessage(), e);
                attempt++;
                if (attempt > MAX_RETRIES) {
                    tracker.completeTrackingById(trackingId, ZERO, ConnectionStatus.CONNECTION_FAILED);
                    return new SimpleHttpResponse(trackingId, ZERO, EMPTY_STRING, originalUri, new RDAPHttpRequest.Header[ZERO]);
                }
            }
        }

        tracker.completeTrackingById(trackingId, ZERO, ConnectionStatus.CONNECTION_FAILED);
        return new SimpleHttpResponse(trackingId, ZERO, EMPTY_STRING, originalUri, new RDAPHttpRequest.Header[ZERO]);
    }
}