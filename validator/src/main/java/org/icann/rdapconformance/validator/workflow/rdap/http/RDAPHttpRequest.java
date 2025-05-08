package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.icann.rdapconformance.validator.CommonUtils.EMPTY_STRING;
import static org.icann.rdapconformance.validator.CommonUtils.GET;
import static org.icann.rdapconformance.validator.CommonUtils.HEAD;
import static org.icann.rdapconformance.validator.CommonUtils.HTTPS;
import static org.icann.rdapconformance.validator.CommonUtils.HTTPS_PORT;
import static org.icann.rdapconformance.validator.CommonUtils.HTTP_PORT;
import static org.icann.rdapconformance.validator.CommonUtils.LOCALHOST;
import static org.icann.rdapconformance.validator.CommonUtils.LOCAL_IPv4;
import static org.icann.rdapconformance.validator.CommonUtils.PAUSE;
import static org.icann.rdapconformance.validator.CommonUtils.TOO_MANY_REQUESTS;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import java.net.UnknownHostException;
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



    public static HttpResponse<String> makeHttpGetRequest(URI uri, int timeoutSeconds) throws Exception {
        return makeRequest(uri, timeoutSeconds, GET);
    }

    public static HttpResponse<String> makeHttpHeadRequest(URI uri, int timeoutSeconds) throws Exception {
        return makeRequest(uri, timeoutSeconds, HEAD);
    }

    //   ConnectionTracker tracker = ConnectionTracker.getInstance();
    //        tracker.startTrackingNewConnection(originalUri);

    public static HttpResponse<String> makeRequest(URI originalUri, int timeoutSeconds, String method) throws Exception {
        NetworkInfo.setHttpMethod(method);
        ConnectionTracker tracker = ConnectionTracker.getInstance();
        tracker.startTrackingNewConnection(originalUri);

        if (originalUri == null) {
            logger.error("The provided URI is null. Ensure the URI is properly set before making the request.");
            tracker.completeCurrentConnection(0, ConnectionStatus.CONNECTION_FAILED);
            return new SimpleHttpResponse(0, EMPTY_STRING, originalUri, new Header[0]);
        }

        int maxRetries = 3;
        int attempt = 0;

        while (attempt <= maxRetries) {

            String host = originalUri.getHost();
            if (LOCALHOST.equalsIgnoreCase(host)) {
                host = LOCAL_IPv4;
            }

            if (DNSCacheResolver.hasNoAddresses(host)) {
                logger.info("No IP address found for host: " + host);
                tracker.completeCurrentConnection(0, ConnectionStatus.UNKNOWN_HOST);
                return new SimpleHttpResponse(0, EMPTY_STRING, originalUri, new Header[0]);
            }

            int port = originalUri.getPort() == -1
                ? (originalUri.getScheme().equalsIgnoreCase(HTTPS) ? HTTPS_PORT : HTTP_PORT)
                : originalUri.getPort();

            InetAddress remoteAddress = NetworkInfo.getNetworkProtocol() == NetworkProtocol.IPv6
                ? DNSCacheResolver.getFirstV6Address(host)
                : DNSCacheResolver.getFirstV4Address(host);

            if (remoteAddress == null) {
                logger.warn("IP address lookup failed for host: " + host);
                tracker.completeCurrentConnection(0, ConnectionStatus.UNKNOWN_HOST);
                return new SimpleHttpResponse(0, EMPTY_STRING, originalUri, new Header[0]);
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
            logger.info("Connecting to: {} using {} with header `{}`", remoteAddress.getHostAddress(), NetworkInfo.getNetworkProtocol(), NetworkInfo.getAcceptHeader());

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

            try (CloseableHttpClient client = HttpClientBuilder.create()
                                                               .setConnectionManager(connectionManager)
                                                               .disableRedirectHandling()
                                                               .build();
                ClassicHttpResponse response = client.execute(request)) {

                int statusCode = response.getCode();
                String body = response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : EMPTY_STRING;

                if (statusCode == 429) {
                    long backoffSeconds = getBackoffTime(response);
                    logger.warn("429 Too Many Requests. Backing off {} seconds. Attempt {}/{}", backoffSeconds, attempt + 1, maxRetries);
                    attempt++;

                    if (attempt > maxRetries) {
                        tracker.completeCurrentConnection(statusCode, ConnectionStatus.TOO_MANY_REQUESTS);
                        return new SimpleHttpResponse(statusCode, body, originalUri, response.getHeaders());
                    }

                    Thread.sleep(backoffSeconds * 1000L);
                    continue;
                }

                tracker.completeCurrentConnection(statusCode, ConnectionStatus.SUCCESS);
                return new SimpleHttpResponse(statusCode, body, originalUri, response.getHeaders());
            }
        }

        // This should only be hit if loop exits without a response
        tracker.completeCurrentConnection(429, ConnectionStatus.TOO_MANY_REQUESTS);
        return new SimpleHttpResponse(429, EMPTY_STRING, originalUri, new Header[0]);
    }

    private static long getBackoffTime(ClassicHttpResponse response) {
        Header retryAfter = response.getFirstHeader("Retry-After");
        if (retryAfter != null) {
            try {
                return Long.parseLong(retryAfter.getValue());
            } catch (NumberFormatException ignored) {}
        }

        Header reset = response.getFirstHeader("X-Ratelimit-Reset");
        if (reset != null) {
            try {
                return Long.parseLong(reset.getValue());
            } catch (NumberFormatException ignored) {}
        }

        return 5; // default backoff
    }

    public static class SimpleHttpResponse implements HttpResponse<String> {
        private final int statusCode;
        private final String body;
        private final URI uri;
        private final Map<String, List<String>> headers;

        public SimpleHttpResponse(int statusCode, String body, URI uri, Header[] headers) {
            this.statusCode = statusCode;
            this.body = body;
            this.uri = uri;

            Map<String, List<String>> headersMap = new HashMap<>();
            if (headers != null) {
                for (Header header : headers) {
                    headersMap.computeIfAbsent(header.getName(), k -> new ArrayList<>())
                              .add(header.getValue());
                }
            }
            this.headers = headersMap;
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