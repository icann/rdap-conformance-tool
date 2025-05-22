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
import static org.icann.rdapconformance.validator.CommonUtils.parseRemoteAddress;

import java.io.EOFException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateRevokedException;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.DefaultHttpClientConnectionOperator;
import org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory;
import org.apache.hc.client5.http.io.HttpClientConnectionOperator;
import org.apache.hc.client5.http.io.ManagedHttpClientConnection;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.EndpointDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.util.TimeValue;
import org.icann.rdapconformance.validator.ConnectionStatus;
import org.icann.rdapconformance.validator.ConnectionTracker;
import org.icann.rdapconformance.validator.DNSCacheResolver;
import org.icann.rdapconformance.validator.NetworkInfo;
import org.icann.rdapconformance.validator.NetworkProtocol;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.client5.http.impl.routing.DefaultRoutePlanner;
import org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory;
import org.apache.hc.core5.http.config.Registry;

import org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory;
import org.apache.hc.core5.http.config.Registry;

import java.net.Socket;
import java.net.Socket;





import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.net.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

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
import java.net.URI;
import java.net.InetAddress;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;




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
        return makeRequest(originalUri, timeoutSeconds, method, isMain, true);
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

        // Determine remote IP
        InetAddress remoteAddress = NetworkInfo.getNetworkProtocol() == NetworkProtocol.IPv6
            ? DNSCacheResolver.getFirstV6Address(host)
            : DNSCacheResolver.getFirstV4Address(host);

        if (remoteAddress == null) {
            tracker.completeCurrentConnection(ZERO, ConnectionStatus.UNKNOWN_HOST);
            return new SimpleHttpResponse(trackingId, ZERO, EMPTY_STRING, originalUri, new Header[ZERO]);
        }

        // Determine remote port
        int port = originalUri.getPort() == -1
            ? (originalUri.getScheme().equalsIgnoreCase(HTTPS) ? HTTPS_PORT : HTTP_PORT)
            : originalUri.getPort();

        // Build Host-based URI
        URI ipUri = new URI(
            originalUri.getScheme(),
            null,
//            remoteAddress.getHostAddress(),
            host, // now we set the host name
            port,
            originalUri.getRawPath(),
            originalUri.getRawQuery(),
            originalUri.getRawFragment()
        );

        // Log connection info
        NetworkInfo.setServerIpAddress(remoteAddress.getHostAddress());
        logger.info("Connecting to: {} using {}", remoteAddress.getHostAddress(), NetworkInfo.getNetworkProtocol());
        tracker.updateServerIpOnConnection(trackingId, remoteAddress.getHostAddress());

        // Create request
        HttpUriRequestBase request = method.equals(GET) ? new HttpGet(ipUri) : new HttpHead(ipUri);
        request.setHeader(ACCEPT, NetworkInfo.getAcceptHeader());
        request.setHeader(HOST, originalUri.getHost());  // Set original hostname in Host header
        request.setHeader(CONNECTION, CLOSE);

        // Create SSL Context with default trust manager
        SSLContext sslContext = SSLContextBuilder.create().build();

        // Create request config
        RequestConfig config = RequestConfig.custom()
                                            .setConnectTimeout(Timeout.of(timeoutSeconds, TimeUnit.SECONDS))
                                            .setResponseTimeout(Timeout.of(timeoutSeconds, TimeUnit.SECONDS))
                                            .build();
        request.setConfig(config);

        // Build the HTTP client before the retry loop
        CloseableHttpClient client = buildHttpClient(sslContext, request);


        int maxRetries = MAX_RETRIES;
        int attempt = 0;

        while (attempt <= maxRetries) {
            try {
                ClassicHttpResponse response = executeRequest(client, request);
//                System.out.println("[IPConn] " + request.getUri().getHost());

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

    public static ClassicHttpResponse executeRequest(CloseableHttpClient client, HttpUriRequestBase request) throws Exception {
        HttpClientContext context = HttpClientContext.create();
        ClassicHttpResponse response = client.execute(request, context);
        return response;
    }
//    public static ClassicHttpResponse executeRequest(CloseableHttpClient client, HttpUriRequestBase request) throws IOException {
//        return client.execute(request);
//    }

    private static CertificateException findCertificateException(Throwable throwable) {
        while (throwable != null) {
            if (throwable instanceof CertificateException) {
                return (CertificateException) throwable;
            }
            throwable = throwable.getCause();
        }
        return null;
    }

    /**
     * Handle exceptions that occur during the HTTP request.
     */
    public static ConnectionStatus handleRequestException(IOException e, boolean recordError) {
        if (e instanceof UnknownHostException) {
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.UNKNOWN_HOST);
            return ConnectionStatus.UNKNOWN_HOST;
        }

        CertificateException certEx = findCertificateException(e);
        if (certEx != null) {
            String msg = certEx.getMessage();
            System.out.println("!!!!Certificate error: " + msg);
            if ("EXPIRED_CERT".equals(msg)) {
                if (recordError) {
                    addErrorToResultsFile(ZERO, -13011, "no response available", "Expired certificate.");
                }
                ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.EXPIRED_CERTIFICATE);
                return ConnectionStatus.EXPIRED_CERTIFICATE;
            } else if ("REVOKED_CERT".equals(msg)) {
                if (recordError) {
                    addErrorToResultsFile(ZERO, -13010, "no response available", "Revoked TLS certificate.");
                }
                ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.REVOKED_CERTIFICATE);
                return ConnectionStatus.REVOKED_CERTIFICATE;
            } else if ("INVALID_CERT".equals(msg)) {
                if (recordError) {
                    addErrorToResultsFile(ZERO, -13009, "no response available", "Invalid TLS certificate.");
                }
                ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.INVALID_CERTIFICATE);
                return ConnectionStatus.INVALID_CERTIFICATE;
            } else if ("NOT_YET_VALID".equals(msg)) {
                System.out.println("!!!!Certificate not yet valid");
                if (recordError) {
                    addErrorToResultsFile(ZERO, -13012, "no response available", "Certificate not yet valid.");
                }
                ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.CERTIFICATE_ERROR);
                return ConnectionStatus.CERTIFICATE_ERROR;
            } else {
                if (recordError) {
                    addErrorToResultsFile(ZERO, -13012, "no response available", "TLS certificate error.");
                }
                ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.CERTIFICATE_ERROR);
                return ConnectionStatus.CERTIFICATE_ERROR;
            }
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

         if (hasCause(e, "javax.net.ssl.SSLHandshakeException") || e.toString().contains("SSLHandshakeException")) {
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

    // Add these two methods to determine the outbound IP address
    public static InetAddress getDefaultIPv4Address() throws IOException {
        System.out.println(">> getDefaultIPv4Address");
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 53);
            InetAddress localAddress = socket.getLocalAddress();
            if (localAddress instanceof Inet4Address) {
                System.out.println(">> using local IP Address: " + localAddress.getHostAddress());
                logger.info(">> using local IP Address: " + localAddress.getHostAddress());
                return localAddress;
            } else {
                System.out.println("No IPv4 address found (IPv6 returned instead)");
                throw new IOException("No IPv4 address found (IPv6 returned instead)");
            }
        }
    }

    public static InetAddress getDefaultIPv6Address() throws IOException {
        System.out.println(">> getDefaultIPv6Address");
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("2001:4860:4860::8888"), 53);
            InetAddress localAddress = socket.getLocalAddress();
            if (localAddress instanceof Inet6Address) {
                System.out.println(">> using local IP Address: " + localAddress.getHostAddress());
                logger.info(">> using local IP Address: " + localAddress.getHostAddress());
                return localAddress;
            } else {
                System.out.println("No IPv6 address found (IPv4 returned instead)");
                throw new IOException("No IPv6 address found (IPv4 returned instead)");
            }
        }
    }

    public static CloseableHttpClient buildHttpClient(SSLContext sslContext, HttpUriRequestBase request) {
        SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                                                                                       .setSslContext(sslContext)
                                                                                       .setTlsVersions(TLS.V_1_3, TLS.V_1_2)
                                                                                       .build();

        // Create registry with connection socket factories
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                                                                    .register("https", sslSocketFactory)
                                                                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                                                                    .build();

        // Create connection factory
        ManagedHttpClientConnectionFactory connectionFactory = new ManagedHttpClientConnectionFactory() {
            @Override
            public ManagedHttpClientConnection createConnection(final Socket socket) throws IOException {
                final ManagedHttpClientConnection conn = super.createConnection(socket);
                return new ManagedHttpClientConnection() {
                    @Override
                    public void bind(final Socket socket) throws IOException {
                        try {
                            conn.bind(socket);
//                        System.out.println("[XIPConn]  " + socket.getInetAddress().getHostAddress());

                            // Get endpoint details after connection is bound
                            EndpointDetails details = conn.getEndpointDetails();
                            if (details != null) {
                                SocketAddress remoteAddress = details.getRemoteAddress();
                                System.out.println("[XIPConn details dump]  " + details);
                                String remoteAddressStr = remoteAddress != null ? remoteAddress.toString() : "(null)";
                                NetworkInfo.setServerIpAddress(parseRemoteAddress(remoteAddressStr));
                                logger.info("Connected to: {}", remoteAddressStr);
                                System.out.println("[XIPConn]  " + remoteAddressStr);
                            } else {
                                System.out.println("[XIPConn]  No endpoint details available -> null");
                            }
                        } catch (IOException e) {
                            System.out.println("[XIPConn]  Failed to bind socket: " + e.getMessage());
                            throw e;
                        }
                    }

                    // Delegate all other methods to conn
                    @Override
                    public void close(CloseMode closeMode) { conn.close(closeMode); }

                    @Override
                    public boolean isDataAvailable(Timeout timeout) throws IOException { return conn.isDataAvailable(timeout); }

                    @Override
                    public boolean isStale() throws IOException { return conn.isStale(); }

                    @Override
                    public void flush() throws IOException { conn.flush(); }

                    @Override
                    public boolean isConsistent() { return conn.isConsistent(); }

                    @Override
                    public void sendRequestHeader(ClassicHttpRequest request) throws HttpException, IOException {
                        conn.sendRequestHeader(request);
                    }

                    @Override
                    public void terminateRequest(ClassicHttpRequest request) throws HttpException, IOException {
                        conn.terminateRequest(request);
                    }

                    @Override
                    public void sendRequestEntity(ClassicHttpRequest request) throws HttpException, IOException {
                        conn.sendRequestEntity(request);
                    }

                    @Override
                    public ClassicHttpResponse receiveResponseHeader() throws HttpException, IOException {
                        return conn.receiveResponseHeader();
                    }

                    @Override
                    public void receiveResponseEntity(ClassicHttpResponse response) throws HttpException, IOException {
                        conn.receiveResponseEntity(response);
                    }

                    @Override
                    public Socket getSocket() { return conn.getSocket(); }

                    @Override
                    public void close() throws IOException { conn.close(); }

                    @Override
                    public boolean isOpen() { return conn.isOpen(); }

                    @Override
                    public Timeout getSocketTimeout() { return conn.getSocketTimeout(); }

                    @Override
                    public void setSocketTimeout(Timeout timeout) { conn.setSocketTimeout(timeout); }

                    @Override
                    public EndpointDetails getEndpointDetails() { return conn.getEndpointDetails(); }

                    @Override
                    public SocketAddress getRemoteAddress() { return conn.getRemoteAddress(); }

                    @Override
                    public ProtocolVersion getProtocolVersion() { return conn.getProtocolVersion(); }

                    @Override
                    public SocketAddress getLocalAddress() { return conn.getLocalAddress(); }

                    @Override
                    public SSLSession getSSLSession() { return conn.getSSLSession(); }

                    @Override
                    public void passivate() { conn.passivate(); }

                    @Override
                    public void activate() { conn.activate(); }
                };
            }
        };
        // Build connection manager with registry and factory
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
            registry,
            connectionFactory);
        connectionManager.setDefaultSocketConfig(SocketConfig.custom()
                                                             .setSoTimeout(Timeout.ofSeconds(20))
                                                             .build());

        return HttpClients.custom()
                          .setConnectionManager(connectionManager)
                          .setDefaultRequestConfig(request.getConfig())
                          .setRoutePlanner(new DefaultRoutePlanner(DefaultSchemePortResolver.INSTANCE) {
                              @Override
                              protected InetAddress determineLocalAddress(final HttpHost firstHop, final HttpContext context) throws HttpException {
                                  try {
                                      return NetworkInfo.getNetworkProtocol() == NetworkProtocol.IPv6
                                          ? getDefaultIPv6Address()
                                          : getDefaultIPv4Address();
                                  } catch (IOException e) {
                                      logger.info("Failed to determine local address {}", e.getMessage());
                                      throw new HttpException("Failed to determine local address", e);
                                  }
                              }
                          })
                          .disableRedirectHandling()
                          .build();
    }

//    public static CloseableHttpClient buildHttpClient(SSLContext sslContext, HttpUriRequestBase request) {
//        System.out.println(">> Inside Building HTTP client");
//        SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
//                                                                                       .setSslContext(sslContext)
//                                                                                       .setTlsVersions(TLS.V_1_3, TLS.V_1_2)
//                                                                                       .build();
//
//        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
//                                                                                                        .setSSLSocketFactory(sslSocketFactory)
//                                                                                                        .build();
//
//        return HttpClients.custom()
//                          .setConnectionManager(connectionManager)
//                          .setDefaultRequestConfig(request.getConfig())  // Get config from request
//                          .setRoutePlanner(new DefaultRoutePlanner(DefaultSchemePortResolver.INSTANCE) {
//                              @Override
//                              protected InetAddress determineLocalAddress(final HttpHost firstHop, final HttpContext context) throws HttpException {
//                                  try {
//                                      System.out.println(">> Determining local address");
//                                      return NetworkInfo.getNetworkProtocol() == NetworkProtocol.IPv6
//                                          ? getDefaultIPv6Address()
//                                          : getDefaultIPv4Address();
//                                  } catch (IOException e) {
//                                      System.out.println(">>Failed to determine local address" + e.getMessage());
//                                      logger.info(">>Failed to determine local address {}", e.getMessage());
//                                      throw new HttpException("!!!!Failed to determine local address", e);
//                                  }
//                              }
//                          })
//                          .disableRedirectHandling()
//                          .build();
//    }

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