package org.icann.rdapconformance.validator.workflow.rdap.http;

import static java.net.HttpURLConnection.HTTP_OK;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

public class MultiCertHttpsTestServer {
    public static final int EXPIRED_CERT_PORT = 8444;
    public static final int INVALID_CERT_PORT = 8445;
    public static final int UNTRUSTED_ROOT_CERT_PORT = 8446;
    public static final String EXPIRED = "expired";
    public static final String INVALID_HOST = "invalidhost";
    public static final String UNTRUSTED = "untrusted";

    private static final List<MultiCertHttpsTestServer.ServerInstance> servers = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        startHttpsServer(EXPIRED_CERT_PORT, EXPIRED);
        startHttpsServer(INVALID_CERT_PORT, INVALID_HOST);
        startHttpsServer(UNTRUSTED_ROOT_CERT_PORT, UNTRUSTED);
    }

    public static void startHttpsServer(int port, String certName) throws Exception {
        HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(port), 0);
        SSLContext sslContext = createSSLContext(certName);
        httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext));
        httpsServer.createContext("/", new MultiCertHttpsTestServer.SimpleHandler(certName));
        httpsServer.setExecutor(null);
        new Thread(httpsServer::start).start();

        servers.add(new MultiCertHttpsTestServer.ServerInstance(port, httpsServer));
    }

    public static void stopAll() {
        for (MultiCertHttpsTestServer.ServerInstance s : servers) {
            s.server.stop(0);
        }
    }

    private static SSLContext createSSLContext(String name) throws Exception {
        String keystoreFile = "keystores/" + name + ".p12";  // Created using OpenSSL or keytool
        char[] password = "password".toCharArray();

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ClassLoader classLoader = MultiCertHttpsTestServer.class.getClassLoader();
        try (FileInputStream fis = new FileInputStream(new File(
            Objects.requireNonNull(classLoader.getResource(keystoreFile)).getFile()))) {
            ks.load(fis, password);
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, password);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);
        return sslContext;
    }

    static class SimpleHandler implements HttpHandler {
        private final String certName;

        public SimpleHandler(String certName) {
            this.certName = certName;
        }

        public void handle(HttpExchange exchange) throws IOException {
            String response = "Served by cert: " + certName;
            exchange.sendResponseHeaders(HTTP_OK, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    static class ServerInstance {
        int port;
        HttpsServer server;

        ServerInstance(int port, HttpsServer server) {
            this.port = port;
            this.server = server;
        }
    }
}
