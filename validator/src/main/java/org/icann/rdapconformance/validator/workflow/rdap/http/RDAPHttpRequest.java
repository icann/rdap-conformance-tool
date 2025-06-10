package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.icann.rdapconformance.validator.CommonUtils.EMPTY_STRING;
import static org.icann.rdapconformance.validator.CommonUtils.GET;
import static org.icann.rdapconformance.validator.CommonUtils.HEAD;
import static org.icann.rdapconformance.validator.CommonUtils.HTTPS_PORT;
import static org.icann.rdapconformance.validator.CommonUtils.HTTP_PORT;
import static org.icann.rdapconformance.validator.CommonUtils.HTTP_TOO_MANY_REQUESTS;
import static org.icann.rdapconformance.validator.CommonUtils.LOCALHOST;
import static org.icann.rdapconformance.validator.CommonUtils.LOCAL_IPv4;
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

    public static HttpResponse<String> makeHttpGetRequest(URI uri, int timeoutSeconds) throws Exception {
        return makeRequest(uri, timeoutSeconds, GET);
    }

    public static HttpResponse<String> makeHttpHeadRequest(URI uri, int timeoutSeconds) throws Exception {
        return makeRequest(uri, timeoutSeconds, HEAD);
    }

    public static HttpResponse<String> makeRequest(URI originalUri, int timeoutSeconds, String method) throws Exception {
        return makeRequest(originalUri, timeoutSeconds, method, false);
    }

    public static HttpResponse<String> makeRequest(URI originalUri, int timeoutSeconds, String method, boolean isMain) throws Exception {
        return makeRequest(originalUri, timeoutSeconds, method, isMain, true);
    }

    public static HttpResponse<String> makeRequest(URI originalUri, int timeoutSeconds, String method, boolean isMain, boolean canRecordError) throws Exception {
        if (originalUri == null) throw new IllegalArgumentException("The provided URI is null.");

        ConnectionTracker tracker = ConnectionTracker.getInstance();
        String trackingId = tracker.startTrackingNewConnection(originalUri, method, isMain);

        String host = originalUri.getHost();
        if (LOCALHOST.equalsIgnoreCase(host)) {
            host = LOCAL_IPv4;
        }

        int port = originalUri.getPort() == -1 ? (originalUri.getScheme().equalsIgnoreCase("https") ? HTTPS_PORT : HTTP_PORT) : originalUri.getPort();

        if (DNSCacheResolver.hasNoAddresses(host)) {
            logger.info("No IP address found for host: " + host);
            tracker.completeCurrentConnection(ZERO, ConnectionStatus.UNKNOWN_HOST);
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
            tracker.completeCurrentConnection(ZERO, ConnectionStatus.UNKNOWN_HOST);
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
        tracker.updateIPAddressOnCurrentConnection(remoteAddress.getHostAddress());
        logger.info("Connecting to: {} using {}", remoteAddress.getHostAddress(), NetworkInfo.getNetworkProtocol());


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


        // Create the SSLConnectionSocketFactory with SNI support
        SSLConnectionSocketFactory sslSocketFactory = getSslConnectionSocketFactory(host, sslContext);

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                                                                                                        .setSSLSocketFactory(sslSocketFactory)
                                                                                                        .build();
        CloseableHttpClient client = HttpClientBuilder.create()
                                                      .setConnectionManager(connectionManager)
                                                      .setRoutePlanner(new LocalBindRoutePlanner(localBindIp))
                                                      .disableAutomaticRetries()
                                                      .disableRedirectHandling()
                                                      .build();

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
                tracker.completeCurrentConnection(statusCode, status);
                SimpleHttpResponse simpleHttpResponse = new SimpleHttpResponse(
                    trackingId, statusCode, body, originalUri, new Header[ZERO]
                );
                simpleHttpResponse.setConnectionStatusCode(status);
                return simpleHttpResponse;
            }

            if (statusCode == HTTP_TOO_MANY_REQUESTS) {
                long backoffSeconds = getBackoffTime(response.getHeaders());

                if (attempt >= MAX_RETRIES) {
                    logger.info("Requeried using retry-after wait time but result was a 429.");
                    tracker.completeCurrentConnection(statusCode, ConnectionStatus.TOO_MANY_REQUESTS);

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
            tracker.completeCurrentConnection(statusCode, ConnectionStatus.SUCCESS);
            SimpleHttpResponse simpleHttpResponse = new SimpleHttpResponse(
                trackingId, statusCode, body, originalUri, convertHeaders(response.getHeaders())
            );

            simpleHttpResponse.setConnectionStatusCode(ConnectionStatus.SUCCESS);
            return simpleHttpResponse;
        }

        // If all retries are exhausted
        tracker.completeCurrentConnection(HTTP_TOO_MANY_REQUESTS, ConnectionStatus.TOO_MANY_REQUESTS);
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

    public static ClassicHttpResponse executeRequest(CloseableHttpClient client, HttpUriRequestBase request) throws IOException {
        return client.execute(request);
    }

    /**
     * Handle exceptions that occur during the HTTP request.
     */
    public static ConnectionStatus handleRequestException(Exception e, boolean recordError) {
        String exceptionString = e.toString();

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

        // Differentiates between  NETWORK_SEND_FAIL and NETWORK_RECEIVE_FAIL
        if (e instanceof SocketTimeoutException) {
            if (e.getMessage() != null && e.getMessage().contains("Read timed out")) {
                if(recordError) {
                    addErrorToResultsFile(ZERO, -13017, "no response available", "Network receive fail");
                }
                ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.NETWORK_RECEIVE_FAIL);
                return ConnectionStatus.NETWORK_RECEIVE_FAIL;
            } else { // anything that isn't a read timeout with a SocketTimeoutException gets a  send failure
                if(recordError) {
                    addErrorToResultsFile(ZERO, -13016, "no response available", "Network send fail");
                }
                ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.NETWORK_SEND_FAIL);
                return ConnectionStatus.NETWORK_SEND_FAIL;
            }
        } else if (e instanceof EOFException) {
            if(recordError) {
                addErrorToResultsFile(ZERO, -13017, "no response available", "Network receive fail");
            }
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.NETWORK_RECEIVE_FAIL);
            return ConnectionStatus.NETWORK_RECEIVE_FAIL;
        } else if (e.getMessage().contains("Connection reset") || e.getMessage().contains("Connection closed by peer")) {
            if(recordError) {
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

    public static boolean hasCause(Throwable e, String causeClassName) {
        while (e.getCause() != null) {
            if (e.getCause().getClass().getName().equals(causeClassName)) {
                return true;
            }
            e = e.getCause();
        }
        return false;
    }

    // Add these two methods to determine the outbound IP address
    public static InetAddress getDefaultIPv4Address() throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName(OUTGOING_IPV4), DNS_PORT);
            InetAddress localAddress = socket.getLocalAddress();
            if (localAddress instanceof Inet4Address) {
                logger.info("using local IPv4 Address: {}", localAddress.getHostAddress());
                return localAddress;
            } else {
                throw new IOException("No IPv4 address found");
            }
        }
    }

    public static InetAddress getDefaultIPv6Address() throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName(OUTGOING_V6), DNS_PORT);
            InetAddress localAddress = socket.getLocalAddress();
            if (localAddress instanceof Inet6Address) {
                logger.info("using local IPv6 Address: {}", localAddress.getHostAddress());
                return localAddress;
            } else {
                throw new IOException("No IPv6 address found");
            }
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
                    logger.info("Received 429 with retry-after header. Waiting {} seconds to requery.", value);
                    return value;
                }
            } catch (NumberFormatException ignored) {}
        }
        logger.info("Received 429 but no retry-after header was offered. Waiting {} seconds.", DEFAULT_BACKOFF_SECS);
        return DEFAULT_BACKOFF_SECS;
    }

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

                logger.info("---> Leaf Cert <---");
                logger.info("Subject: " + leaf.getSubjectX500Principal());
                logger.info("Issuer:  " + leaf.getIssuerX500Principal());
                logger.info("Not Before: " + leaf.getNotBefore());
                logger.info("Not After:  " + leaf.getNotAfter());

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
                            logger.info("SAN: " + san.get(1));
                        }
                    }
                } catch (CertificateParsingException ex) {
                    logger.info("Failed to parse SANs from leaf cert"); // don't care, log and move on
                }

                // Check revocation status, but catch and rethrow only revocation errors
                try {
                    // Force revocation check through OCSP/CRL
                    Security.setProperty("ocsp.enable", "true");
                    System.setProperty("com.sun.security.enableCRLDP", "true");

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

    public static class SimpleHttpResponse implements HttpResponse<String> {
        private final int statusCode;
        private ConnectionStatus connectionStatus;
        private final String body;
        private final URI uri;
        private final Map<String, List<String>> headers;
        private final String trackingId;

        public SimpleHttpResponse(String trackingId, int statusCode, String body, URI uri, Header[] headers) {
            this.statusCode = statusCode;
            this.body = body;
            this.uri = uri;
            this.trackingId = trackingId;

            Map<String, List<String>> headersMap = new HashMap<>();
            if (headers != null) {
                for (Header header : headers) {
                    headersMap.computeIfAbsent(header.getName(), k -> new ArrayList<>())
                              .add(header.getValue());
                }
            }
            this.headers = headersMap;
        }

        public String getTrackingId() { return trackingId; }

        public void setConnectionStatusCode(ConnectionStatus status) {
            this.connectionStatus = status;
        }

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

    public static class Header {
        private final String name;
        private final String value;

        public Header(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }
}