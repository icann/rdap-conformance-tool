package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.icann.rdapconformance.validator.CommonUtils.GET;
import static org.icann.rdapconformance.validator.CommonUtils.HEAD;
import static org.icann.rdapconformance.validator.CommonUtils.HTTPS_PORT;
import static org.icann.rdapconformance.validator.CommonUtils.HTTP_PORT;
import static org.icann.rdapconformance.validator.CommonUtils.LOCALHOST;
import static org.icann.rdapconformance.validator.CommonUtils.LOCAL_IPv4;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import javax.net.ssl.SSLSession;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
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

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.InetAddress;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import org.icann.rdapconformance.validator.NetworkInfo;
import org.icann.rdapconformance.validator.NetworkProtocol;


public class RDAPHttpRequest {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RDAPHttpRequest.class);


    public static HttpResponse<String> makeHttpGetRequest(URI uri, int timeoutSeconds) throws Exception {
        return makeRequest(uri, timeoutSeconds, GET);
    }

    public static HttpResponse<String> makeHttpHeadRequest(URI uri, int timeoutSeconds) throws Exception {
        return makeRequest(uri, timeoutSeconds, HEAD);
    }

    public static HttpResponse<String> makeRequest(URI originalUri, int timeoutSeconds, String method) throws Exception {
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

        int port = originalUri.getPort() == -1
            ? (originalUri.getScheme().equalsIgnoreCase("https") ? HTTPS_PORT : HTTP_PORT)
            : originalUri.getPort();

        InetAddress remoteAddress = null;
        InetAddress[] addresses = InetAddress.getAllByName(host);

        for (InetAddress addr : addresses) {
            if ((NetworkInfo.getNetworkProtocol() == NetworkProtocol.IPv6) && addr instanceof Inet6Address) {
                remoteAddress = addr;
                break;
            } else if ((NetworkInfo.getNetworkProtocol() == NetworkProtocol.IPv4) && addr instanceof Inet4Address) {
                remoteAddress = addr;
                break;
            }
        }
        // If we didn't find a match for the preferred protocol, use any available address
        if (remoteAddress == null && addresses.length > ZERO) {
            remoteAddress = addresses[ZERO];
        }
        // Check if we have a valid address before proceeding
        if (remoteAddress == null) {
            throw new RuntimeException("No IP address found for host: " + host);
        }


//        NetworkInfo.setServerIpAddress(remoteAddress.getHostAddress());

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
        NetworkInfo.setHttpMethod(method);
        logger.info("Connecting to: {} using {}" , remoteAddress.getHostAddress(), NetworkInfo.getNetworkProtocol());

        HttpUriRequestBase request = method.equals(GET)
            ? new HttpGet(ipUri)
            : new HttpHead(ipUri);

        request.setHeader("Host", host);
        request.setHeader("Accept", NetworkInfo.getAcceptHeader());
        request.setHeader("Connection", "close");

        RequestConfig config = RequestConfig.custom()
                                            .setConnectTimeout(Timeout.of(timeoutSeconds, TimeUnit.SECONDS))
                                            .setResponseTimeout(Timeout.of(timeoutSeconds, TimeUnit.SECONDS))
                                            .build();

        request.setConfig(config);

        TrustStrategy acceptAll = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext = SSLContextBuilder.create()
                                                 .loadTrustMaterial(null, acceptAll)
                                                 .build();

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                                                                                                        .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
                                                                                                                                                              .setSslContext(sslContext)
                                                                                                                                                              .setTlsVersions(TLS.V_1_3)
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
                                                      .build();

        try (ClassicHttpResponse response = (ClassicHttpResponse) client.execute(request)) {
            String body = response.getEntity() != null
                ? EntityUtils.toString(response.getEntity())
                : "";
            int statusCode = response.getCode();

            return new SimpleHttpResponse(statusCode, body, originalUri);
        }
    }

    public static class SimpleHttpResponse implements HttpResponse<String> {
        private final int statusCode;
        private final String body;
        private final URI uri;

        public SimpleHttpResponse(int statusCode, String body, URI uri) {
            this.statusCode = statusCode;
            this.body = body;
            this.uri = uri;
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
            return HttpHeaders.of(java.util.Map.of(), (k, v) -> true); // Empty headers
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