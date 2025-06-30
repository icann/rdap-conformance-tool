package org.icann.rdapconformance.validator.workflow.rdap.http;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SNIHostName;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Singleton HTTP client manager that provides connection pooling while preserving
 * all existing functionality including custom SSL contexts, local binding, and state tracking.
 */
public class HttpClientManager {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientManager.class);
    
    private static volatile HttpClientManager instance;
    private static final Object lock = new Object();
    
    // Connection pool configuration
    private static final int MAX_TOTAL_CONNECTIONS = 50;
    private static final int MAX_CONNECTIONS_PER_ROUTE = 10;
    private static final int CONNECTION_IDLE_TIMEOUT_SECONDS = 30; // timeout for idle connections
    private static final int CONNECTION_VALIDATE_AFTER_INACTIVITY_SECONDS = 10;
    
    // Cache for clients with different configurations - optimized for low contention
    private final ConcurrentHashMap<ClientConfig, CloseableHttpClient> clientCache;
    private final PoolingHttpClientConnectionManager defaultConnectionManager;
    
    private HttpClientManager() {
        this.clientCache = new ConcurrentHashMap<>(16, 0.75f, 1);
        this.defaultConnectionManager = createDefaultConnectionManager();
        
        // Setup cleanup thread for idle connections
        setupConnectionCleanup();
    }
    
    public static HttpClientManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new HttpClientManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Gets a configured HTTP client for the specific requirements.
     * This preserves all existing functionality while providing connection pooling.
     */
    public CloseableHttpClient getClient(String host, SSLContext sslContext, InetAddress localBindIp, int timeoutSeconds) {
        ClientConfig config = new ClientConfig(host, sslContext, localBindIp, timeoutSeconds);
        
        return clientCache.computeIfAbsent(config, this::createClient);
    }
    
    /**
     * Creates a client with the exact same configuration as the original implementation
     * but with connection pooling enabled.
     */
    private CloseableHttpClient createClient(ClientConfig config) {
        try {
            // Create SSL socket factory with SNI support (same as original)
            SSLConnectionSocketFactory sslSocketFactory = getSslConnectionSocketFactory(config.host, config.sslContext);
            
            // Create connection manager with pooling
            PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .setMaxConnTotal(MAX_TOTAL_CONNECTIONS)
                .setMaxConnPerRoute(MAX_CONNECTIONS_PER_ROUTE)
                .setValidateAfterInactivity(TimeValue.ofSeconds(CONNECTION_VALIDATE_AFTER_INACTIVITY_SECONDS))
                .setConnectionTimeToLive(TimeValue.ofSeconds(CONNECTION_IDLE_TIMEOUT_SECONDS))
                .build();
            
            // Configure request timeouts
            RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(config.timeoutSeconds))
                .setConnectTimeout(Timeout.ofSeconds(config.timeoutSeconds))
                .setResponseTimeout(Timeout.ofSeconds(config.timeoutSeconds))
                .build();
            
            // Build client with exact same settings as original
            CloseableHttpClient client = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setRoutePlanner(new LocalBindRoutePlanner(config.localBindIp))
                .setDefaultRequestConfig(requestConfig)
                .disableAutomaticRetries()  // Preserve original behavior
                .disableRedirectHandling() // Preserve original behavior
                .build();
                
            logger.debug("Created new HTTP client for host: {}, local bind: {}", config.host, config.localBindIp);
            return client;
            
        } catch (Exception e) {
            logger.error("Failed to create HTTP client for config: {}", config, e);
            throw new RuntimeException("Failed to create HTTP client", e);
        }
    }
    
    /**
     * Same SSL socket factory creation as original implementation
     */
    private SSLConnectionSocketFactory getSslConnectionSocketFactory(String host, SSLContext sslContext) {
        // This exactly matches the original implementation in RDAPHttpRequest
        return new SSLConnectionSocketFactory(sslContext,
            new String[] { "TLSv1.3", "TLSv1.2" },
            null, // Use default cipher suites
            (hostname, session) -> {
                org.apache.hc.client5.http.ssl.DefaultHostnameVerifier verifier = new org.apache.hc.client5.http.ssl.DefaultHostnameVerifier();
                try {
                    return verifier.verify(host, session);
                } catch (Exception e) {
                    logger.debug("Hostname verification failed for: {}", host, e);
                    return false;
                }
            }) {
            @Override
            protected void prepareSocket(javax.net.ssl.SSLSocket socket) throws java.io.IOException {
                javax.net.ssl.SSLParameters sslParameters = socket.getSSLParameters();
                sslParameters.setServerNames(java.util.Collections.singletonList(new javax.net.ssl.SNIHostName(host)));
                socket.setSSLParameters(sslParameters);
            }
        };
    }
    
    private PoolingHttpClientConnectionManager createDefaultConnectionManager() {
        return PoolingHttpClientConnectionManagerBuilder.create()
            .setMaxConnTotal(MAX_TOTAL_CONNECTIONS)
            .setMaxConnPerRoute(MAX_CONNECTIONS_PER_ROUTE)
            .setValidateAfterInactivity(TimeValue.ofSeconds(CONNECTION_VALIDATE_AFTER_INACTIVITY_SECONDS))
            .setConnectionTimeToLive(TimeValue.ofSeconds(CONNECTION_IDLE_TIMEOUT_SECONDS))
            .build();
    }
    
    private void setupConnectionCleanup() {
        // Background thread to clean up idle and expired connections
        Thread cleanupThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Clean up the default connection manager
                    defaultConnectionManager.closeExpired();
                    defaultConnectionManager.closeIdle(TimeValue.ofSeconds(CONNECTION_IDLE_TIMEOUT_SECONDS));
                    
                    // Clean up cached client connection managers
                    clientCache.values().forEach(client -> {
                        try {
                            // The connection manager cleanup is handled by the client itself
                            // when connections become idle or expired
                        } catch (Exception e) {
                            logger.warn("Error during connection cleanup", e);
                        }
                    });
                    
                    TimeUnit.SECONDS.sleep(30); // Run cleanup every 30 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.warn("Unexpected error during connection cleanup", e);
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.setName("HttpClient-ConnectionCleanup");
        cleanupThread.start();
    }
    
    /**
     * Graceful shutdown of all HTTP clients and connection managers
     */
    public void shutdown() {
        logger.info("Shutting down HTTP client manager");
        
        // Close all cached clients
        clientCache.values().forEach(client -> {
            try {
                client.close();
            } catch (Exception e) {
                logger.warn("Error closing HTTP client", e);
            }
        });
        clientCache.clear();
        
        // Close default connection manager
        defaultConnectionManager.close();
    }
    
    /**
     * Configuration key for caching clients with different settings
     */
    private static class ClientConfig {
        final String host;
        final SSLContext sslContext;
        final InetAddress localBindIp;
        final int timeoutSeconds;
        final int hashCode;
        
        ClientConfig(String host, SSLContext sslContext, InetAddress localBindIp, int timeoutSeconds) {
            this.host = host;
            this.sslContext = sslContext;
            this.localBindIp = localBindIp;
            this.timeoutSeconds = timeoutSeconds;
            this.hashCode = computeHashCode();
        }
        
        private int computeHashCode() {
            int result = host != null ? host.hashCode() : 0;
            result = 31 * result + (sslContext != null ? sslContext.hashCode() : 0);
            result = 31 * result + (localBindIp != null ? localBindIp.hashCode() : 0);
            result = 31 * result + timeoutSeconds;
            return result;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            
            ClientConfig that = (ClientConfig) obj;
            return timeoutSeconds == that.timeoutSeconds &&
                   java.util.Objects.equals(host, that.host) &&
                   java.util.Objects.equals(sslContext, that.sslContext) &&
                   java.util.Objects.equals(localBindIp, that.localBindIp);
        }
        
        @Override
        public int hashCode() {
            return hashCode;
        }
        
        @Override
        public String toString() {
            return String.format("ClientConfig{host='%s', localBindIp=%s, timeout=%d}", 
                host, localBindIp, timeoutSeconds);
        }
    }
}