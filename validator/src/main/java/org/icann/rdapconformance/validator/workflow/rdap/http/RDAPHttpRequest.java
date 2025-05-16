package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.icann.rdapconformance.validator.CommonUtils.EMPTY_STRING;
import static org.icann.rdapconformance.validator.CommonUtils.GET;
import static org.icann.rdapconformance.validator.CommonUtils.HEAD;
import static org.icann.rdapconformance.validator.CommonUtils.HTTPS;
import static org.icann.rdapconformance.validator.CommonUtils.HTTPS_PORT;
import static org.icann.rdapconformance.validator.CommonUtils.HTTP_PORT;
import static org.icann.rdapconformance.validator.CommonUtils.LOCALHOST;
import static org.icann.rdapconformance.validator.CommonUtils.LOCAL_IPv4;
import static org.icann.rdapconformance.validator.CommonUtils.HTTP_TOO_MANY_REQUESTS;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;
import static org.icann.rdapconformance.validator.CommonUtils.addErrorToResultsFile;

import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;
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
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.util.Timeout;


public class RDAPHttpRequest {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RDAPHttpRequest.class);
    public static final String HOST = "Host";
    public static final String ACCEPT = "Accept";
    public static final String CONNECTION = "Connection";
    public static final String CLOSE = "close";
    public static final String RETRY_AFTER = "Retry-After";
    public static final String X_RATELIMIT_RESET = "X-Ratelimit-Reset";

    public static final int DEFAULT_BACKOFF_SECS = 5;
    public static final int MAX_RETRIES = 3;

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
        return makeRequest(originalUri, timeoutSeconds, method, false, true);
    }


    public static HttpResponse<String> makeRequest(URI originalUri, int timeoutSeconds, String method, boolean isMain, boolean canRecordError) throws Exception {
        if (originalUri == null) {
            throw new IllegalArgumentException("The provided URI is null.");
        }

        ConnectionTracker tracker = ConnectionTracker.getInstance();
        String trackingId = tracker.startTrackingNewConnection(originalUri, method, isMain);

        String host = originalUri.getHost();
        if (LOCALHOST.equalsIgnoreCase(host)) {
            host = LOCAL_IPv4;
        }

        if (DNSCacheResolver.hasNoAddresses(host)) {
            logger.info("No IP address found for host: " + host);
            tracker.completeCurrentConnection(ZERO, ConnectionStatus.UNKNOWN_HOST);
            return new SimpleHttpResponse(trackingId, ZERO, EMPTY_STRING, originalUri, new Header[ZERO]);
        }

        int port = originalUri.getPort() == -1
            ? (originalUri.getScheme().equalsIgnoreCase(HTTPS) ? HTTPS_PORT : HTTP_PORT)
            : originalUri.getPort();

        InetAddress remoteAddress = NetworkInfo.getNetworkProtocol() == NetworkProtocol.IPv6
            ? DNSCacheResolver.getFirstV6Address(host)
            : DNSCacheResolver.getFirstV4Address(host);

        if (remoteAddress == null) {
            tracker.completeCurrentConnection(ZERO, ConnectionStatus.UNKNOWN_HOST);
            return new SimpleHttpResponse(trackingId,ZERO, EMPTY_STRING, originalUri, new Header[ZERO]);
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
        logger.info("Connecting to: {} using {}", remoteAddress.getHostAddress(), NetworkInfo.getNetworkProtocol());

        HttpUriRequestBase request = method.equals(GET) ? new HttpGet(ipUri) : new HttpHead(ipUri);
        request.setHeader(HOST, host);
        request.setHeader(ACCEPT, NetworkInfo.getAcceptHeader());
        request.setHeader(CONNECTION, CLOSE);

        RequestConfig config = RequestConfig.custom()
                                            .setConnectTimeout(Timeout.of(timeoutSeconds, TimeUnit.SECONDS))
                                            .setResponseTimeout(Timeout.of(timeoutSeconds, TimeUnit.SECONDS))
                                            .build();
        request.setConfig(config);

        TrustStrategy acceptAll = (chain, authType) -> true;
        SSLContext sslContext = SSLContextBuilder.create()
                                                 .loadTrustMaterial(null, acceptAll)
                                                 .build();

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                                                                                                        .setSSLSocketFactory(
                                                                                                            SSLConnectionSocketFactoryBuilder.create()
                                                                                                                                             .setSslContext(sslContext)
                                                                                                                                             .setTlsVersions(TLS.V_1_3, TLS.V_1_2)
                                                                                                                                             .setHostnameVerifier((hostname, session) -> true)
                                                                                                                                             .build()
                                                                                                        ).build();

        CloseableHttpClient client = HttpClientBuilder.create()
                                                      .setConnectionManager(connectionManager)
                                                      .disableRedirectHandling()
                                                      .build();

        int maxRetries = MAX_RETRIES;
        int attempt = ZERO;

        while (attempt <= maxRetries) {
            try {
                ClassicHttpResponse response = executeRequest(client, request);
                int statusCode = response.getCode();
                String body = response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : EMPTY_STRING;

                if (statusCode == HTTP_TOO_MANY_REQUESTS) {
                    long backoffSeconds = getBackoffTime(response);
                    logger.info("[429] Too Many Requests. Backing off for {} seconds. Attempt {}/{}", backoffSeconds, attempt + 1, maxRetries);
                    attempt++;

                    if (attempt > maxRetries) {
                        tracker.completeCurrentConnection(statusCode, ConnectionStatus.TOO_MANY_REQUESTS);
                        SimpleHttpResponse simpleHttpResponse = new SimpleHttpResponse(trackingId, statusCode, body, originalUri, response.getHeaders());
                        simpleHttpResponse.setConnectionStatusCode(ConnectionStatus.TOO_MANY_REQUESTS);
                        return simpleHttpResponse;
                    }

                    sleep(backoffSeconds);
                    continue;
                }

                // Successful response
                tracker.completeCurrentConnection(statusCode, ConnectionStatus.SUCCESS);
                SimpleHttpResponse simpleHttpResponse = new SimpleHttpResponse(trackingId, statusCode, body, originalUri, response.getHeaders());
                simpleHttpResponse.setConnectionStatusCode(ConnectionStatus.SUCCESS);
                return simpleHttpResponse;

            } catch (IOException ioe) {
                logger.info("[trackingID: {}] Error during HTTP request: {}", trackingId, ioe.getMessage());
                ConnectionStatus connStatus = handleRequestException(ioe, canRecordError);
                tracker.completeCurrentConnection(ZERO, connStatus);

                SimpleHttpResponse simpleHttpResponse = new SimpleHttpResponse(trackingId, ZERO, EMPTY_STRING, originalUri, null);
                simpleHttpResponse.setConnectionStatusCode(connStatus);
                return simpleHttpResponse;
            }
        }

        tracker.completeCurrentConnection(HTTP_TOO_MANY_REQUESTS, ConnectionStatus.TOO_MANY_REQUESTS);
        return new SimpleHttpResponse(trackingId, HTTP_TOO_MANY_REQUESTS, EMPTY_STRING, originalUri, new Header[0]);
    }


    private static long getBackoffTime(ClassicHttpResponse response) {
        Header retryAfter = response.getFirstHeader(RETRY_AFTER);
        if (retryAfter != null) {
            try {
                return Long.parseLong(retryAfter.getValue());
            } catch (NumberFormatException ignored) {} // ignore and continue
        }

        Header resetHeader = response.getFirstHeader(X_RATELIMIT_RESET);
        if (resetHeader != null) {
            try {
                return Long.parseLong(resetHeader.getValue());
            } catch (NumberFormatException ignored) {} // ignore and go with default
        }

        return DEFAULT_BACKOFF_SECS;
    }

    private static void sleep(long seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    public static ClassicHttpResponse executeRequest(CloseableHttpClient client, HttpUriRequestBase request) throws IOException {
        return client.execute(request);
    }


    /**
     * Handle exceptions that occur during the HTTP request.
     */
    public static ConnectionStatus handleRequestException(IOException e, boolean recordError) {
        if (e instanceof UnknownHostException) {
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.UNKNOWN_HOST);
            return ConnectionStatus.UNKNOWN_HOST;
        }

        if (e instanceof ConnectException || e instanceof HttpTimeoutException || e instanceof org.apache.hc.client5.http.ConnectTimeoutException) {
            if (hasCause(e, "java.nio.channels.UnresolvedAddressException")) {
                if(recordError) {
                    addErrorToResultsFile(ZERO,-13016, "no response available", "Network send fail");
                }
                ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.NETWORK_SEND_FAIL);
                return ConnectionStatus.NETWORK_SEND_FAIL;
            } else {
                if(recordError) {
                    addErrorToResultsFile(ZERO, -13007, "no response available", "Failed to connect to server.");
                }
                ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.CONNECTION_FAILED);
                return ConnectionStatus.CONNECTION_FAILED;
            }
        }

        if (hasCause(e, "java.security.cert.CertificateExpiredException")) {
            if(recordError) {
                addErrorToResultsFile(ZERO, -13011, "no response available", "Expired certificate.");
            }
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.EXPIRED_CERTIFICATE);
            return ConnectionStatus.EXPIRED_CERTIFICATE;
        } else if (hasCause(e, "java.security.cert.CertificateRevokedException")) {
            if(recordError) {
                addErrorToResultsFile(ZERO, -13010, "no response available", "Revoked TLS certificate.");
            }
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.REVOKED_CERTIFICATE);
            return ConnectionStatus.REVOKED_CERTIFICATE;
        } else if (hasCause(e, "java.security.cert.CertificateException")) {
            if (e.getMessage().contains("No name matching") ||
                e.getMessage().contains("No subject alternative DNS name matching")) {
                if(recordError) {
                    addErrorToResultsFile(0, -13009, "no response available", "Invalid TLS certificate.");
                }
                ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.INVALID_CERTIFICATE);
                return ConnectionStatus.INVALID_CERTIFICATE;
            }
            if(recordError) {
                addErrorToResultsFile(ZERO, -13012, "no response available", "TLS certificate error.");
            }
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.CERTIFICATE_ERROR);
            return ConnectionStatus.CERTIFICATE_ERROR;
        } else if (hasCause(e, "javax.net.ssl.SSLHandshakeException") || e.toString().contains("SSLHandshakeException")) {
            if(recordError) {
                addErrorToResultsFile(ZERO, -13008, "no response available", "TLS handshake failed.");
            }
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.HANDSHAKE_FAILED);
            return ConnectionStatus.HANDSHAKE_FAILED;
        } else if (hasCause(e, "sun.security.validator.ValidatorException")) {
            addErrorToResultsFile(ZERO,-13012, "no response available", "TLS certificate error.");
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.CERTIFICATE_ERROR);
            return ConnectionStatus.CERTIFICATE_ERROR;
        }

        // Differentiates between  NETWORK_SEND_FAIL and NETWORK_RECEIVE_FAIL
        if (e instanceof SocketTimeoutException) {
            if (e.getMessage().contains("Read timed out")) {
                if(recordError) {
                    addErrorToResultsFile(ZERO, -13017, "no response available", "Network receive fail");
                }
                ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.NETWORK_RECEIVE_FAIL);
                return ConnectionStatus.NETWORK_RECEIVE_FAIL;
            } else {
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

        // Default to CONNECTION_FAILED if no specific cause identified
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
}