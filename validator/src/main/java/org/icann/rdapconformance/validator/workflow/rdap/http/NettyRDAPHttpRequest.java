package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.icann.rdapconformance.validator.CommonUtils.*;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.CharsetUtil;

import java.net.http.HttpClient.Version;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.net.*;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.icann.rdapconformance.validator.NetworkInfo;
import org.icann.rdapconformance.validator.NetworkProtocol;

public class NettyRDAPHttpRequest {
    private static final String GET = "GET";
    private static final String HEAD = "HEAD";
    private static final String ACCEPT = "Accept";

    public static CompletableFuture<HttpResponse<String>> makeHttpGetRequest(URI uri, int timeout)
        throws Exception {
        return makeRequest(uri.toString(), timeout, GET);
    }

    public static CompletableFuture<HttpResponse<String>> makeHttpHeadRequest(URI uri, int timeout)
        throws Exception {
        return makeRequest(uri.toString(), timeout, HEAD);
    }

    public static CompletableFuture<HttpResponse<String>> makeRequest(String urlString, int timeoutSeconds, String method) throws Exception {
        URI uri = URI.create(urlString);
        String host = uri.getHost();
        String path = uri.getRawPath().isEmpty() ? SLASH : uri.getRawPath();
        boolean isHttps = uri.getScheme().equalsIgnoreCase(HTTPS);
        int port = uri.getPort() == -1 ? (isHttps ? HTTPS_PORT : HTTP_PORT) : uri.getPort();

        InetAddress remoteAddress = null;
        for (InetAddress addr : InetAddress.getAllByName(host)) {
            if ((NetworkInfo.getNetworkProtocol() == NetworkProtocol.IPv6) && addr instanceof Inet6Address) {
                remoteAddress = addr;
                break;
            } else if ((NetworkInfo.getNetworkProtocol() == NetworkProtocol.IPv4) && addr instanceof Inet4Address) {
                remoteAddress = addr;
                break;
            }
        }

        if (remoteAddress == null) {
            throw new RuntimeException("No matching " + NetworkInfo.getNetworkProtocol() + " address found for host: " + host);
        }

        System.out.println("Connecting to: " + remoteAddress.getHostAddress() + " using " + NetworkInfo.getNetworkProtocol());

        EventLoopGroup group = new NioEventLoopGroup();
        CompletableFuture<HttpResponse<String>> responseFuture = new CompletableFuture<>();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                 .channel(NioSocketChannel.class)
                 .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutSeconds * 1000)
                 .handler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     protected void initChannel(SocketChannel ch) throws SSLException {
                         ChannelPipeline p = ch.pipeline();

                         if (isHttps) {
                             SslContext sslCtx = SslContextBuilder.forClient()
                                                                  .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                                                  .build();
                             p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                         }

                         p.addLast(new HttpClientCodec());
                         p.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
                         p.addLast(new SimpleChannelInboundHandler<FullHttpResponse>() {
                             @Override
                             protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) {
                                 String body = msg.content().toString(CharsetUtil.UTF_8);
                                 responseFuture.complete(new NettyHttpResponse(msg.status().code(), body, uri));
                                 ctx.close();
                             }

                             @Override
                             public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                 responseFuture.completeExceptionally(cause);
                                 ctx.close();
                             }

                             @Override
                             public void channelActive(ChannelHandlerContext ctx) {
                                 FullHttpRequest request = new DefaultFullHttpRequest(
                                     HttpVersion.HTTP_1_1, HttpMethod.valueOf(method), path);
                                 request.headers().set(HttpHeaderNames.HOST, host);
                                 request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
                                 request.headers().set(ACCEPT, RDAP_JSON_APPLICATION_JSON);
                                 ctx.writeAndFlush(request);
                             }
                         });
                     }
                 });

        bootstrap.connect(new InetSocketAddress(remoteAddress, port))
                 .addListener((ChannelFutureListener) future -> {
                     if (!future.isSuccess()) {
                         responseFuture.completeExceptionally(future.cause());
                     }
                 });

        responseFuture.whenComplete((resp, err) -> group.shutdownGracefully());
        return responseFuture;
    }

    public static class NettyHttpResponse implements HttpResponse<String> {
        private final int statusCode;
        private final String body;
        private final URI uri;

        public NettyHttpResponse(int statusCode, String body, URI uri) {
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
        public HttpHeaders headers() {
            return HttpHeaders.of(Map.of(), (k, v) -> true);
        }

        @Override
        public Optional<HttpResponse<String>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return uri;
        }

        @Override
        public HttpRequest request() {
            return null;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public Version version() {
            return Version.HTTP_1_1;
        }
    }
}
