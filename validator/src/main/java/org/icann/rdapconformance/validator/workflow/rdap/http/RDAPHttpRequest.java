package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.icann.rdapconformance.validator.CommonUtils.EMPTY_STRING;
import static org.icann.rdapconformance.validator.CommonUtils.GET;
import static org.icann.rdapconformance.validator.CommonUtils.HEAD;
import static org.icann.rdapconformance.validator.CommonUtils.HTTPS;
import static org.icann.rdapconformance.validator.CommonUtils.HTTPS_PORT;
import static org.icann.rdapconformance.validator.CommonUtils.HTTP_PORT;
import static org.icann.rdapconformance.validator.CommonUtils.LOCALHOST;
import static org.icann.rdapconformance.validator.CommonUtils.LOCAL_IPv4;
import static org.icann.rdapconformance.validator.CommonUtils.addErrorToResultsFile;

import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;
import javax.imageio.IIOException;
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
import java.security.cert.X509Certificate;
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
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.core5.util.TimeValue;


public class RDAPHttpRequest {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RDAPHttpRequest.class);
    public static final String HOST = "Host";
    public static final String ACCEPT = "Accept";
    public static final String CONNECTION = "Connection";
    public static final String CLOSE = "close";


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
        NetworkInfo.setHttpMethod(method); // set this first before anything

        if (originalUri == null) {
            throw new IllegalArgumentException("The provided URI is null. Ensure the URI is properly set before making the request.");
        }
        String host;

       // Check if the host is "localhost" and replace it with "127.0.0.1"
        host = originalUri.getHost();
        if (LOCALHOST.equalsIgnoreCase(host)) {
            host = LOCAL_IPv4;
        } else {
            host = originalUri.getHost();
        }

        // if host is not 127.0.0.1 or localhost, we need to resolve the host
        if(DNSCacheResolver.hasNoAddresses(host)) {
            logger.info("No IP address found for host: " + host);
            throw new UnknownHostException("No IP address found for host: " + host);
        }

        int port = originalUri.getPort() == -1
            ? (originalUri.getScheme().equalsIgnoreCase(HTTPS) ? HTTPS_PORT : HTTP_PORT)
            : originalUri.getPort();

        InetAddress remoteAddress = null;
        if (NetworkInfo.getNetworkProtocol() == NetworkProtocol.IPv6) {
            remoteAddress = DNSCacheResolver.getFirstV6Address(host);
        } else if (NetworkInfo.getNetworkProtocol() == NetworkProtocol.IPv4) {
            remoteAddress = DNSCacheResolver.getFirstV4Address(host);
        }

        if(remoteAddress == null) {
            throw new UnknownHostException("IP address lookup failed for host: " + host);
        }

        // set the url to the ip address
        URI ipUri = new URI(
            originalUri.getScheme(),
            null,
            remoteAddress.getHostAddress(),
            port,
            originalUri.getRawPath(),
            originalUri.getRawQuery(),
            originalUri.getRawFragment()
        );

        // Ensure we update NetworkInfo on what we are doing
        NetworkInfo.setServerIpAddress(remoteAddress.getHostAddress());
        logger.info("Connecting to: {} using {} with header `{}`" , remoteAddress.getHostAddress(), NetworkInfo.getNetworkProtocol(), NetworkInfo.getAcceptHeader());

        // determine which of the two methods to use
        HttpUriRequestBase request = method.equals(GET)
            ? new HttpGet(ipUri)
            : new HttpHead(ipUri);

        // set what we need on the request
        request.setHeader(HOST, host); // Super important, breaks without it
        request.setHeader(ACCEPT, NetworkInfo.getAcceptHeader());
        request.setHeader(CONNECTION, CLOSE);

        RequestConfig config = RequestConfig.custom()
                                            .setConnectTimeout(Timeout.of(timeoutSeconds, TimeUnit.SECONDS))
                                            .setResponseTimeout(Timeout.of(timeoutSeconds, TimeUnit.SECONDS))
                                            .build();

        request.setConfig(config);

        TrustStrategy acceptAll = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext = SSLContextBuilder.create()
                                                 .loadTrustMaterial(null, acceptAll) // yes, we have to trust everyone b/c we are using the IP adder and this will break otherwise
                                                 .build();

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                                                                                                        .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
                                                                                                                                                              .setSslContext(sslContext)
                                                                                                                                                              .setTlsVersions(TLS.V_1_3, TLS.V_1_2)
                                                                                                                                                              .setHostnameVerifier((hostname, session) -> true)
                                                                                                                                                              .build())
                                                                                                        .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
                                                                                                        .setConnPoolPolicy(PoolReusePolicy.LIFO)
                                                                                                        .setDefaultConnectionConfig(org.apache.hc.client5.http.config.ConnectionConfig.custom()
                                                                                                                                                                                      .setSocketTimeout(Timeout.ofMinutes(1))
                                                                                                                                                                                      .setConnectTimeout(Timeout.ofMinutes(1))
                                                                                                                                                                                      .setTimeToLive(TimeValue.ofMinutes(10))
                                                                                                                                                                                      .build())
                                                                                                        .build();

        CloseableHttpClient client = HttpClientBuilder.create()
                                                      .setConnectionManager(connectionManager)
                                                      .disableRedirectHandling()
                                                      .build();

        ConnectionTracker tracker = ConnectionTracker.getInstance();
        String trackingId = tracker.startTrackingNewConnection(originalUri, isMain);

        try (ClassicHttpResponse response = client.execute(request)) {
            String body = response.getEntity() != null
                ? EntityUtils.toString(response.getEntity())
                : EMPTY_STRING;
            int statusCode = response.getCode();
            logger.info("Response status code: {}", statusCode);
            tracker.completeCurrentConnection(statusCode, ConnectionStatus.SUCCESS);
            return new SimpleHttpResponse(trackingId, statusCode, body, originalUri, response.getHeaders());
        } catch (IOException ioe) {
            logger.error("Error during HTTP request: {}", ioe.getMessage());
            System.out.println("[trackingID] " + trackingId + " - Error during HTTP request: " + ioe.getMessage());
            tracker.completeCurrentConnection(0, ConnectionStatus.CONNECTION_FAILED);
            handleRequestException(ioe);
            return new SimpleHttpResponse(trackingId, 0, EMPTY_STRING, originalUri, null);
        }
    }

    /**
     * Handle exceptions that occur during the HTTP request.
     */
    private static void handleRequestException(IOException e) {
        if (e instanceof UnknownHostException) {
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.UNKNOWN_HOST);
            return;
        }

        if (e instanceof ConnectException || e instanceof HttpTimeoutException) {
            if (hasCause(e, "java.nio.channels.UnresolvedAddressException")) {
                addErrorToResultsFile(-13016, "no response available", "Network send fail");
                ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.NETWORK_SEND_FAIL);
            } else {
                addErrorToResultsFile(-13007, "no response available", "Failed to connect to server.");
                ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.CONNECTION_FAILED);
            }
            return;
        }

        if (hasCause(e, "java.security.cert.CertificateExpiredException")) {
            addErrorToResultsFile(-13011, "no response available", "Expired certificate.");
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.EXPIRED_CERTIFICATE);
            return;
        } else if (hasCause(e, "java.security.cert.CertificateRevokedException")) {
            addErrorToResultsFile(-13010, "no response available", "Revoked TLS certificate.");
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.REVOKED_CERTIFICATE);
            return;
        } else if (hasCause(e, "java.security.cert.CertificateException")) {
            if (e.getMessage().contains("No name matching") ||
                e.getMessage().contains("No subject alternative DNS name matching")) {
                addErrorToResultsFile(-13009, "no response available", "Invalid TLS certificate.");
                ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.INVALID_CERTIFICATE);
                return;
            }
            addErrorToResultsFile(-13012, "no response available", "TLS certificate error.");
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.CERTIFICATE_ERROR);
            return;
        } else if (hasCause(e, "javax.net.ssl.SSLHandshakeException") || e.toString().contains("SSLHandshakeException")) {
            addErrorToResultsFile(-13008, "no response available", "TLS handshake failed.");
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.HANDSHAKE_FAILED);
            return;
        } else if (hasCause(e, "sun.security.validator.ValidatorException")) {
            addErrorToResultsFile(-13012, "no response available", "TLS certificate error.");
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.CERTIFICATE_ERROR);
            return;
        }

        // Differentiates between  NETWORK_SEND_FAIL and NETWORK_RECEIVE_FAIL
        if (e instanceof SocketTimeoutException) {
            if (e.getMessage().contains("Read timed out")) {
                addErrorToResultsFile(-13017, "no response available", "Network receive fail");
                ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.NETWORK_RECEIVE_FAIL);
            } else {
                addErrorToResultsFile(-13016, "no response available", "Network send fail");
                ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.NETWORK_SEND_FAIL);
            }
            return;
        } else if (e instanceof EOFException) {
            addErrorToResultsFile(-13017, "no response available", "Network receive fail");
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.NETWORK_RECEIVE_FAIL);
            return;
        } else if (e.getMessage().contains("Connection reset") || e.getMessage().contains("Connection closed by peer")) {
            addErrorToResultsFile(-13017, "no response available", "Network receive fail");
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.NETWORK_RECEIVE_FAIL);
            return;
        }

        // Default to CONNECTION_FAILED if no specific cause identified
        addErrorToResultsFile(-13007, "no response available", "Failed to connect to server.");
        ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.CONNECTION_FAILED);
    }

    private static boolean hasCause(Throwable e, String causeClassName) {
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