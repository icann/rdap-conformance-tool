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
import static org.icann.rdapconformance.validator.CommonUtils.addErrorToResultsFile;

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
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;

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
 * state and the NetworkInfo singleton for protocol selection and header configuration.
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
        return makeRequest(uri, timeoutSeconds, GET);
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
        return makeRequest(uri, timeoutSeconds, HEAD);
    }

    /**
     * Creates and executes an HTTP request with the specified method.
     *
     * <p>This method delegates to the main request handler with default settings
     * for main request flag and error recording.</p>
     *
     * @param originalUri the URI to send the request to
     * @param timeoutSeconds the timeout in seconds for both connection and response
     * @param method the HTTP method to use (GET, HEAD, etc.)
     * @return HttpResponse containing the response data and metadata
     * @throws Exception if the request fails due to network or other issues
     */
    public static HttpResponse<String> makeRequest(URI originalUri, int timeoutSeconds, String method) throws Exception {
        return makeRequest(originalUri, timeoutSeconds, method, false);
    }

    /**
     * Creates and executes an HTTP request with the specified method and main request flag.
     *
     * <p>This method delegates to the full request handler with default error recording enabled.</p>
     *
     * @param originalUri the URI to send the request to
     * @param timeoutSeconds the timeout in seconds for both connection and response
     * @param method the HTTP method to use (GET, HEAD, etc.)
     * @param isMain whether this is a main request (affects tracking and logging)
     * @return HttpResponse containing the response data and metadata
     * @throws Exception if the request fails due to network or other issues
     */
    public static HttpResponse<String> makeRequest(URI originalUri, int timeoutSeconds, String method, boolean isMain) throws Exception {
        return makeRequest(originalUri, timeoutSeconds, method, isMain, true);
    }

    /**
     * Creates and executes an HTTP request with full configuration options.
     *
     * <p>This is the main request method that handles all aspects of HTTP communication
     * including DNS resolution, IPv4/IPv6 selection, SSL/TLS validation, retry logic,
     * and comprehensive error handling. The method automatically selects appropriate
     * local and remote IP addresses based on the current network protocol configuration.</p>
     *
     * <p>Key processing steps:</p>
     * <ul>
     *   <li>DNS resolution using the configured network protocol (IPv4/IPv6)</li>
     *   <li>Local bind address selection based on protocol and availability</li>
     *   <li>SSL/TLS context creation with custom certificate validation</li>
     *   <li>HTTP request execution with connection pooling</li>
     *   <li>Automatic retry handling for 429 responses with backoff</li>
     *   <li>Connection tracking and status reporting</li>
     * </ul>
     *
     * @param originalUri the URI to send the request to (must not be null)
     * @param timeoutSeconds the timeout in seconds for both connection and response
     * @param method the HTTP method to use (GET, HEAD, etc.)
     * @param isMain whether this is a main request (affects tracking and logging)
     * @param canRecordError whether to record errors in the validation results
     * @return HttpResponse containing the response data and metadata
     * @throws Exception if the request fails due to network or other issues
     * @throws IllegalArgumentException if originalUri is null
     */
    public static HttpResponse<String> makeRequest(URI originalUri, int timeoutSeconds, String method, boolean isMain, boolean canRecordError) throws Exception {
        if (originalUri == null) throw new IllegalArgumentException("The provided URI is null.");

        ConnectionTracker tracker = ConnectionTracker.getInstance();
        String trackingId = tracker.startTrackingNewConnection(originalUri, method, isMain);

        String host = originalUri.getHost();
        if (LOCALHOST.equalsIgnoreCase(host)) {
            host = LOCAL_IPv4; // only do v4, no dual-stack binding
        }

        int port = originalUri.getPort() == -1 ? (originalUri.getScheme().equalsIgnoreCase("https") ? HTTPS_PORT : HTTP_PORT) : originalUri.getPort();

        if (DNSCacheResolver.hasNoAddresses(host)) {
            logger.debug("No IP address found for host: " + host);
            tracker.completeTrackingById(trackingId, ZERO, ConnectionStatus.UNKNOWN_HOST);
            SimpleHttpResponse resp = new SimpleHttpResponse(trackingId,ZERO, EMPTY_STRING, originalUri, new Header[ZERO]);
            resp.setConnectionStatusCode(ConnectionStatus.UNKNOWN_HOST);
            return resp;
        }

        NetworkProtocol protocol = NetworkInfo.getNetworkProtocol();
        InetAddress localBindIp = (protocol == NetworkProtocol.IPv6)
            ? getDefaultIPv6Address()
            : getDefaultIPv4Address();

        InetAddress remoteAddress = (protocol == NetworkProtocol.IPv6)
            ? DNSCacheResolver.getFirstV6Address(host)
            : DNSCacheResolver.getFirstV4Address(host);

        // If remote address or local bind IP is null, treat as unknown host
        if (remoteAddress == null || localBindIp == null) {
            tracker.completeTrackingById(trackingId, ZERO, ConnectionStatus.UNKNOWN_HOST);
            return new SimpleHttpResponse(trackingId, ZERO, EMPTY_STRING, originalUri, new Header[ZERO]);
        }

        // Special case: if remote is 127.0.0.1, bind to 127.0.0.1
        if (remoteAddress.getHostAddress().equals(LOCAL_IPv4)) {
            localBindIp = InetAddress.getByName(LOCAL_IPv4);
        }

        URI ipUri = new URI(
            originalUri.getScheme(),
            null,
            remoteAddress.getHostAddress(),
            port,
            originalUri.getRawPath(),
            originalUri.getRawQuery(),
            originalUri.getRawFragment()
        );

        NetworkInfo.setServerIpAddress(remoteAddress.getHostAddress());
        tracker.updateIPAddressById(trackingId, remoteAddress.getHostAddress());
        logger.debug("Connecting to: {} using {}", remoteAddress.getHostAddress(), NetworkInfo.getNetworkProtocol());

        HttpUriRequestBase request = method.equals(GET) ? new HttpGet(originalUri) : new HttpHead(originalUri);
        request.setHeader(HOST, host);
        request.setHeader(ACCEPT, NetworkInfo.getAcceptHeader());
        request.setHeader(CONNECTION, CLOSE);
        request.setUri(ipUri);

        RequestConfig config = RequestConfig.custom()
                                            .setConnectTimeout(Timeout.of(timeoutSeconds, TimeUnit.SECONDS))
                                            .setResponseTimeout(Timeout.of(timeoutSeconds, TimeUnit.SECONDS))
                                            .build();
        request.setConfig(config);

        X509TrustManager leafCheckingTm = createLeafValidatingTrustManager();
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[] { leafCheckingTm }, new SecureRandom());

        // Use the pooled HTTP client manager instead of creating a new client each time
        CloseableHttpClient client = HttpClientManager.getInstance().getClient(host, sslContext, localBindIp, timeoutSeconds);

        // Set the local bind address for the request
        int attempt = ZERO;

        while (attempt <= MAX_RETRIES) {
            ClassicHttpResponse response = null;
            int statusCode = ZERO;
            String body = EMPTY_STRING;

            try {
                response = executeRequest(client, request);
                statusCode = response.getCode();
                body = response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : EMPTY_STRING;
            } catch (Exception e) {
                ConnectionStatus status = handleRequestException(e, canRecordError);
                tracker.completeTrackingById(trackingId, statusCode, status);
                SimpleHttpResponse simpleHttpResponse = new SimpleHttpResponse(
                    trackingId, statusCode, body, originalUri, new Header[ZERO]
                );
                simpleHttpResponse.setConnectionStatusCode(status);
                return simpleHttpResponse;
            }

            if (statusCode == HTTP_TOO_MANY_REQUESTS) {
                long backoffSeconds = getBackoffTime(response.getHeaders());

                if (attempt >= MAX_RETRIES) {
                    logger.debug("Requeried using retry-after wait time but result was a 429.");
                    tracker.completeTrackingById(trackingId, statusCode, ConnectionStatus.TOO_MANY_REQUESTS);

                    SimpleHttpResponse simpleHttpResponse = new SimpleHttpResponse(
                        trackingId, statusCode, body, originalUri, convertHeaders(response.getHeaders())
                    );

                    simpleHttpResponse.setConnectionStatusCode(ConnectionStatus.TOO_MANY_REQUESTS);
                    return simpleHttpResponse;
                }
                attempt++;
                // in-line the sleep method to avoid unnecessary complexity
                if (backoffSeconds > ZERO) {
                    try {
                        Thread.sleep(backoffSeconds * PAUSE); // Convert seconds to milliseconds
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }
                continue;
            }

            // Successful response
            tracker.completeTrackingById(trackingId, statusCode, ConnectionStatus.SUCCESS);
            SimpleHttpResponse simpleHttpResponse = new SimpleHttpResponse(
                trackingId, statusCode, body, originalUri, convertHeaders(response.getHeaders())
            );

            simpleHttpResponse.setConnectionStatusCode(ConnectionStatus.SUCCESS);
            return simpleHttpResponse;
        }

        // If all retries are exhausted
        tracker.completeTrackingById(trackingId, HTTP_TOO_MANY_REQUESTS, ConnectionStatus.TOO_MANY_REQUESTS);
        return new SimpleHttpResponse(trackingId, HTTP_TOO_MANY_REQUESTS, EMPTY_STRING, originalUri, new Header[ZERO]);
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
     * @param e the exception that occurred during request execution
     * @param recordError whether to record the error in validation results
     * @return ConnectionStatus representing the classified error type
     */
    public static ConnectionStatus handleRequestException(Exception e, boolean recordError) {
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
                addErrorToResultsFile(ZERO, -13014, "no response available", "HTTP error.");
            }
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.HTTP_ERROR);
            return ConnectionStatus.HTTP_ERROR;
        }

        if (e instanceof UnknownHostException) {
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.UNKNOWN_HOST);
            return ConnectionStatus.UNKNOWN_HOST;
        }

        if (exceptionString.contains("Connection refused")) {
            if (recordError) {
                addErrorToResultsFile(ZERO, -13021, "no response available", "Connection refused by host.");
            }
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.CONNECTION_REFUSED);
            return ConnectionStatus.CONNECTION_REFUSED;
        }

        if (e instanceof ConnectException || e instanceof HttpTimeoutException) {
                if(recordError) {
                    addErrorToResultsFile(ZERO, -13007, "no response available", "Failed to connect to server.");
                }
                ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.CONNECTION_FAILED);
                return ConnectionStatus.CONNECTION_FAILED;
        }

        // SSL and TLS related exceptions
        if (hasCause(e, "java.security.cert.CertificateExpiredException")) {
            if(recordError) {
                addErrorToResultsFile(ZERO, -13011, "no response available", "Expired certificate.");
            }
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.EXPIRED_CERTIFICATE);
            return ConnectionStatus.EXPIRED_CERTIFICATE;
        } else if (hasCause(e, "java.security.cert.CertificateRevokedException") || exceptionString.contains("CertificateRevokedException") ||  exceptionString.contains("Certificate revoked")) {
            if(recordError) {
                addErrorToResultsFile(ZERO, -13010, "no response available", "Revoked TLS certificate.");
            }
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.REVOKED_CERTIFICATE);
            return ConnectionStatus.REVOKED_CERTIFICATE;
        }
        else if (hasCause(e, "javax.net.ssl.SSLHandshakeException") || e.toString().contains("SSLHandshakeException")) {
            if(recordError) {
                addErrorToResultsFile(ZERO, -13008, "no response available", "TLS handshake failed.");
            }
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.HANDSHAKE_FAILED);
            return ConnectionStatus.HANDSHAKE_FAILED;
        }
        else if (hasCause(e, "javax.net.ssl.SSLPeerUnverifiedException") || e.toString().contains("SSLPeerUnverifiedException")) {
                if(recordError) {
                    addErrorToResultsFile(ZERO, -13009, "no response available", "Invalid TLS certificate.");
                }
                ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.INVALID_CERTIFICATE);
                return ConnectionStatus.INVALID_CERTIFICATE;
        } else if (hasCause(e, "sun.security.validator.ValidatorException") || hasCause(e, "java.security.cert.CertificateException") ) {
            // else it's just a generic certificate error and falls under the certificate error category
            addErrorToResultsFile(ZERO,-13012, "no response available", "TLS certificate error.");
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.CERTIFICATE_ERROR);
            return ConnectionStatus.CERTIFICATE_ERROR;
        }

        // Handle network timeouts and connection issues
        if (e instanceof SocketTimeoutException) {
            boolean isReadTimeout = e.getMessage() != null && e.getMessage().contains("Read timed out");

            if (isReadTimeout) {
                // Read timeout = network receive failure (-13017)
                if (recordError) {
                    addErrorToResultsFile(ZERO, -13017, "no response available", "Network receive fail");
                }
                ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.NETWORK_RECEIVE_FAIL);
                return ConnectionStatus.NETWORK_RECEIVE_FAIL;
            } else {
                // Other socket timeouts = network send failure (-13016)
                if (recordError) {
                    addErrorToResultsFile(ZERO, -13016, "no response available", "Network send fail");
                }
                ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.NETWORK_SEND_FAIL);
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
                addErrorToResultsFile(ZERO, -13017, "no response available", "Network receive fail");
            }
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.NETWORK_RECEIVE_FAIL);
            return ConnectionStatus.NETWORK_RECEIVE_FAIL;
        }
        // we are at the fall through point, which means we have not identified a specific cause, and it gets classified as a connection failure
        if(recordError) {
            addErrorToResultsFile(ZERO,-13007, "no response available", "Failed to connect to server.");
        }
        ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.CONNECTION_FAILED);
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
}