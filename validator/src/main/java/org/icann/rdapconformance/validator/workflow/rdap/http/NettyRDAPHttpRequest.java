package org.icann.rdapconformance.validator.workflow.rdap.http;



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

import javax.net.ssl.SSLException;
import java.net.*;
import java.util.concurrent.CompletableFuture;
import org.icann.rdapconformance.validator.NetworkInfo;

public class NettyRDAPHttpRequest {

    public static CompletableFuture<SimpleHttpResponse> makeRequest(String urlString, int timeoutSeconds) throws Exception {
        URI uri = URI.create(urlString);
        String host = uri.getHost();
        String path = uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
        boolean isHttps = uri.getScheme().equalsIgnoreCase("https");
        int port = uri.getPort() == -1 ? (isHttps ? 443 : 80) : uri.getPort();

        // Decide whether to resolve IPv4 or IPv6
        String protocol = NetworkInfo.getNetworkProtocol(); // returns "IPv4" or "IPv6"
//        String protocol = "IPv4"; // or "IPv6" based on your requirement
        InetAddress remoteAddress = null;

        for (InetAddress addr : InetAddress.getAllByName(host)) {
            if (protocol.equals("IPv6") && addr instanceof Inet6Address) {
                remoteAddress = addr;
                break;
            } else if (protocol.equals("IPv4") && addr instanceof Inet4Address) {
                remoteAddress = addr;
                break;
            }
        }

        if (remoteAddress == null) {
            throw new RuntimeException("No matching " + protocol + " address found for host: " + host);
        }

        System.out.println("Connecting to: " + remoteAddress.getHostAddress() + " using " + protocol);

        EventLoopGroup group = new NioEventLoopGroup();
        CompletableFuture<SimpleHttpResponse> responseFuture = new CompletableFuture<>();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                 .channel(NioSocketChannel.class)
                 .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutSeconds * 1000)
                 .handler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     protected void initChannel(SocketChannel ch) throws SSLException {
                         ChannelPipeline p = ch.pipeline();

                         if (isHttps) {
                             // For testing, use insecure trust manager. Replace with a real one in production.
                             SslContext sslCtx = SslContextBuilder.forClient()
                                                                  .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                                                  .build();
                             p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                         }

                         p.addLast(new HttpClientCodec());
                         p.addLast(new HttpObjectAggregator(1048576));
                         p.addLast(new SimpleChannelInboundHandler<FullHttpResponse>() {
                             @Override
                             protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) {
                                 String body = msg.content().toString(CharsetUtil.UTF_8);
                                 responseFuture.complete(new SimpleHttpResponse(msg.status().code(), body));
                                 ctx.close();
                             }

                             @Override
                             public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                 responseFuture.completeExceptionally(cause);
                                 ctx.close();
                             }

                             @Override
                             public void channelActive(ChannelHandlerContext ctx) {
                                 HttpRequest request = new DefaultFullHttpRequest(
                                     HttpVersion.HTTP_1_1, HttpMethod.GET, path);
                                 request.headers().set(HttpHeaderNames.HOST, host);
                                 request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
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

    public static class SimpleHttpResponse {
        private final int statusCode;
        private final String body;

        public SimpleHttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int statusCode() {
            return statusCode;
        }

        public String body() {
            return body;
        }

        @Override
        public String toString() {
            return "Status: " + statusCode + "\n" + body;
        }
    }
}
