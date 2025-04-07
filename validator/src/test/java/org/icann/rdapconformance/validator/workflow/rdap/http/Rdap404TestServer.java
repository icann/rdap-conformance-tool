package org.icann.rdapconformance.validator.workflow.rdap.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Rdap404TestServer {

    public static final int RDAP_404_PORT = 8404;
    private static final List<ServerInstance> servers = new ArrayList<>();
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_RDAP_JSON = "application/rdap+json";

    public static void start() throws IOException {
        startServer(RDAP_404_PORT);
    }

    public static void stopAll() {
        for (ServerInstance s : servers) {
            s.server.stop(0);
        }
        servers.clear();
    }

    private static void startServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new Error404Handler());
        server.setExecutor(null); // default executor
        new Thread(server::start).start();
        servers.add(new ServerInstance(port, server));
    }

    static class Error404Handler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String json = """
                {
                  "rdapConformance": [ "rdap_level_0" ],
                  "errorCode": 404,
                  "title": "Not Found",
                  "description": [
                    "The requested resource was not found on this RDAP server."
                  ]
                }
                """;

            byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set(CONTENT_TYPE, APPLICATION_RDAP_JSON);
            exchange.sendResponseHeaders(HTTP_NOT_FOUND, responseBytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }

    static class ServerInstance {
        int port;
        HttpServer server;

        ServerInstance(int port, HttpServer server) {
            this.port = port;
            this.server = server;
        }
    }
}