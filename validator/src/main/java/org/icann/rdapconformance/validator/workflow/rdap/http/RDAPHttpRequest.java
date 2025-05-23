package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.icann.rdapconformance.validator.CommonUtils.GET;
import static org.icann.rdapconformance.validator.CommonUtils.HEAD;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;
import static org.icann.rdapconformance.validator.CommonUtils.addErrorToResultsFile;

import io.netty.handler.timeout.ReadTimeoutHandler;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
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
import java.net.URI;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.CharsetUtil;
import java.util.concurrent.CompletableFuture;


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
        if (originalUri == null) throw new IllegalArgumentException("The provided URI is null.");

        ConnectionTracker tracker = ConnectionTracker.getInstance();
        String trackingId = tracker.startTrackingNewConnection(originalUri, method, isMain);

        String host = originalUri.getHost();
        int port = originalUri.getPort() == -1 ? (originalUri.getScheme().equalsIgnoreCase("https") ? 443 : 80) : originalUri.getPort();

        InetAddress localBindIp = NetworkInfo.getNetworkProtocol() == NetworkProtocol.IPv6
            ? getDefaultIPv6Address()
            : getDefaultIPv4Address();

        InetAddress remoteIp = NetworkInfo.getNetworkProtocol() == NetworkProtocol.IPv6
            ? DNSCacheResolver.getFirstV6Address(host)
            : DNSCacheResolver.getFirstV4Address(host);

        if (remoteIp == null || localBindIp == null) {
            tracker.completeCurrentConnection(0, ConnectionStatus.UNKNOWN_HOST);
            return new SimpleHttpResponse(trackingId, 0, "", originalUri, new Header[0]);
        }

        NetworkInfo.setServerIpAddress(remoteIp.getHostAddress());
        tracker.updateServerIpOnConnection(trackingId, remoteIp.getHostAddress());

        boolean isHttps = originalUri.getScheme().equalsIgnoreCase("https");

        int maxRetries = 3;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            final int currentAttempt = attempt;
            EventLoopGroup group = new NioEventLoopGroup();
            CompletableFuture<SimpleHttpResponse> futureResponse = new CompletableFuture<>();

            try {
                SslContext sslCtx = isHttps ? SslContextBuilder.forClient().build() : null;

                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(group)
                         .channel(NioSocketChannel.class)
                         .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutSeconds * 1000)
                         .localAddress(new InetSocketAddress(localBindIp, 0))
                         .handler(new ChannelInitializer<io.netty.channel.socket.SocketChannel>() {
                             @Override
                             protected void initChannel(io.netty.channel.socket.SocketChannel ch) {
                                 ChannelPipeline p = ch.pipeline();
                                 if (sslCtx != null) {
                                     p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                                 }
                                 p.addLast(new HttpClientCodec());
                                 p.addLast(new ReadTimeoutHandler(timeoutSeconds, TimeUnit.SECONDS));
                                 p.addLast(new HttpObjectAggregator(10485760)); // Increase to 10MB
                                 p.addLast(new SimpleChannelInboundHandler<FullHttpResponse>() {
                                     @Override
                                     protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) {
                                         int statusCode = msg.status().code();
                                         String responseBody = msg.content().toString(CharsetUtil.UTF_8);

                                         // Convert Netty headers
                                         Header[] headersArr = msg.headers().entries().stream()
                                                                  .map(e -> new Header(e.getKey(), e.getValue()))
                                                                  .toArray(Header[]::new);

                                         if (statusCode == 429) {
                                             long backoff = getBackoffTime(msg.headers());
                                             logger.info("[429] Too Many Requests. Backing off for {} seconds. Attempt {}/{}", backoff, currentAttempt + 1, maxRetries);
                                             sleep(backoff);
                                             SimpleHttpResponse retryResponse = new SimpleHttpResponse(trackingId, 429, responseBody, originalUri, headersArr);
                                             retryResponse.setConnectionStatusCode(ConnectionStatus.TOO_MANY_REQUESTS);
                                             futureResponse.complete(retryResponse);
                                             ctx.close();
                                             return;
                                         }

                                         tracker.completeCurrentConnection(statusCode, ConnectionStatus.SUCCESS);
                                         SimpleHttpResponse response = new SimpleHttpResponse(trackingId, statusCode, responseBody, originalUri, headersArr);
                                         response.setConnectionStatusCode(ConnectionStatus.SUCCESS);
                                         futureResponse.complete(response);
                                         ctx.close();
                                     }

                                     @Override
                                     public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                         futureResponse.completeExceptionally(cause);
                                         ctx.close();
                                     }
                                 });
                             }
                         });

                    SimpleHttpResponse result = executeRequest(originalUri, timeoutSeconds, method, bootstrap,
                    remoteIp, port, host, futureResponse);

                if (result.statusCode() == 429 && currentAttempt < maxRetries) {
                    TimeUnit.SECONDS.sleep(DEFAULT_BACKOFF_SECS);
                    continue;
                }

                return result;

            } catch (IOException ioe) {
                logger.info("[trackingID: {}] Error during HTTP request: {}", trackingId, ioe.getMessage());
                ConnectionStatus connStatus = handleRequestException(ioe, canRecordError);
                tracker.completeCurrentConnection(0, connStatus);

                SimpleHttpResponse errorResponse = new SimpleHttpResponse(trackingId, 0, "", originalUri, null);
                errorResponse.setConnectionStatusCode(connStatus);
                return errorResponse;
            } catch (Exception ex) {
                logger.info("[trackingID: {}] General error during HTTP request: {}", trackingId, ex.getMessage());
                ConnectionStatus connStatus = handleRequestException(new IOException(ex), canRecordError);
                tracker.completeCurrentConnection(0, connStatus);

                SimpleHttpResponse errorResponse = new SimpleHttpResponse(trackingId, 0, "", originalUri, null);
                errorResponse.setConnectionStatusCode(connStatus);
                return errorResponse;
            } finally {
                group.shutdownGracefully();
            }
        }

        tracker.completeCurrentConnection(429, ConnectionStatus.TOO_MANY_REQUESTS);
        SimpleHttpResponse tooManyRequests = new SimpleHttpResponse(trackingId, 429, "", originalUri, new Header[0]);
        tooManyRequests.setConnectionStatusCode(ConnectionStatus.TOO_MANY_REQUESTS);
        return tooManyRequests;
    }

    public static SimpleHttpResponse executeRequest(URI originalUri,
                                                            int timeoutSeconds,
                                                            String method,
                                                            Bootstrap bootstrap,
                                                            InetAddress remoteIp,
                                                            int port,
                                                            String host,
                                                            CompletableFuture<SimpleHttpResponse> futureResponse)
        throws InterruptedException, ExecutionException, TimeoutException {
        ChannelFuture f = bootstrap.connect(new InetSocketAddress(remoteIp, port)).sync();

        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.valueOf(method), originalUri.getRawPath());
        request.headers().set(HttpHeaderNames.HOST, host);
        request.headers().set(HttpHeaderNames.ACCEPT, NetworkInfo.getAcceptHeader());
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);

        f.channel().writeAndFlush(request);
        f.channel().closeFuture().sync();

        logger.info("Using timeout of {} seconds for future response", timeoutSeconds);
        return  futureResponse.get(timeoutSeconds, TimeUnit.SECONDS);
    }


    /**
     * Handle exceptions that occur during the HTTP request.
     */
    public static ConnectionStatus handleRequestException(IOException e, boolean recordError) {
        if (e instanceof UnknownHostException) {
            ConnectionTracker.getInstance().updateCurrentConnection(ConnectionStatus.UNKNOWN_HOST);
            return ConnectionStatus.UNKNOWN_HOST;
        }

        if (e instanceof ConnectException || e instanceof HttpTimeoutException) {
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



    // Helper for Netty HttpHeaders
    private static long getBackoffTime(io.netty.handler.codec.http.HttpHeaders headers) {
        String retryAfter = headers.get(RETRY_AFTER);
        if (retryAfter != null) {
            try {
                return Long.parseLong(retryAfter);
            } catch (NumberFormatException ignored) {}
        }
        String resetHeader = headers.get(X_RATELIMIT_RESET);
        if (resetHeader != null) {
            try {
                return Long.parseLong(resetHeader);
            } catch (NumberFormatException ignored) {}
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