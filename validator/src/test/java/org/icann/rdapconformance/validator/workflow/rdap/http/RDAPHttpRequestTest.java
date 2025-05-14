package org.icann.rdapconformance.validator.workflow.rdap.http;

import java.net.InetAddress;
import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpResponse;
import java.security.cert.CertificateExpiredException;
import javax.net.ssl.SSLHandshakeException;

import org.apache.hc.core5.http.Header;

import org.icann.rdapconformance.validator.ConnectionStatus;
import org.icann.rdapconformance.validator.DNSCacheResolver;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RDAPHttpRequestTest {

    private final URI testUri = URI.create("http://example.com/path");
    private final int timeout = 10;

    @BeforeMethod
    public void setup() {
        // Reset any static state if needed
    }

    @Test
    public void testMakeRequest_UnknownHost() throws Exception {
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class)) {

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                           .thenThrow(new UnknownHostException("Unknown host"));

            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any()))
                           .thenCallRealMethod();

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, "GET");
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.UNKNOWN_HOST);
        }
    }

    @Test
    public void testMakeRequest_ConnectionClosedByPeer() throws Exception {
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class)) {

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                           .thenThrow(new IOException("Connection closed by peer"));

            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any()))
                           .thenCallRealMethod();

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, "GET");
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);
        }
    }

    @Test
    public void testMakeRequest_ConnectException() throws Exception {
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class)) {

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                           .thenThrow(new ConnectException("Connection refused"));

            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();

            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any()))
                           .thenCallRealMethod();

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, "GET", true);

            assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.CONNECTION_FAILED);
        }
    }


    @Test
    public void testMakeRequest_SocketTimeoutException_ReadTimeout() throws Exception {
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class)) {

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                           .thenThrow(new SocketTimeoutException("Read timed out"));

            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any()))
                           .thenCallRealMethod();

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, "GET");

            assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);
        }
    }

    @Test
    public void testMakeRequest_SocketTimeoutException_ConnectTimeout() throws Exception {
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class)) {

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                           .thenThrow(new SocketTimeoutException("Connect timed out"));


            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any()))
                           .thenCallRealMethod();

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, "GET");

            assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.NETWORK_SEND_FAIL);
        }
    }

    @Test
    public void testMakeRequest_EOFException() throws Exception {
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class)) {

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                           .thenThrow(new EOFException("Unexpected end of file"));

            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any()))
                           .thenCallRealMethod();

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, "GET");

            assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);
        }
    }

    @Test
    public void testMakeRequest_ConnectionReset() throws Exception {
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class)) {

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                           .thenThrow(new IOException("Connection reset"));

            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any()))
                           .thenCallRealMethod();

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, "GET");

            assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);
        }
    }

    @Test
    public void testMakeRequest_MalformedResponseChunk() throws Exception {
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class)) {

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);


            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                           .thenThrow(new IOException("Malformed chunk"));

            // Allow makeRequest methods to call through to real implementation
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any()))
                           .thenCallRealMethod();

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, "GET");

            assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.CONNECTION_FAILED);
        }
    }

    @Test
    public void testMakeRequest_ExpiredCertificate() throws Exception {
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class)) {

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

            // Create a proper exception chain
            CertificateExpiredException certEx = new CertificateExpiredException("Certificate expired");
            SSLHandshakeException sslEx = new SSLHandshakeException("Certificate validation failed");
            sslEx.initCause(certEx);
            IOException ioEx = new IOException("SSL Handshake failed");
            ioEx.initCause(sslEx);

            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                           .thenThrow(ioEx);

            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.hasCause(any(),any()))
                           .thenCallRealMethod();

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, "GET");

            assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.EXPIRED_CERTIFICATE);
        }
    }

    @Test
    public void testMakeRequest_SSLHandshakeException() throws Exception {
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class)) {

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

            // Create a properly structured exception chain for SSL handshake failure
            SSLHandshakeException sslEx = new SSLHandshakeException("PKIX path building failed");
            IOException ioEx = new IOException("SSL handshake failed");
            ioEx.initCause(sslEx);

            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                           .thenThrow(ioEx);

            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.hasCause(any(), any()))
                           .thenCallRealMethod();

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, "GET");

            assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.HANDSHAKE_FAILED);
        }
    }
    
    @Test
    public void testMakeRequest_HttpTooManyRequests_RetriesAndSucceeds() throws Exception {
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class, Mockito.CALLS_REAL_METHODS)) {

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

            // First two attempts: 429, third attempt: 200
            RDAPHttpRequest.SimpleHttpResponse retryResponse = new RDAPHttpRequest.SimpleHttpResponse(
                "track-1", 429, "", testUri, new Header[] { new TestHeader("Retry-After", "1") });
            RDAPHttpRequest.SimpleHttpResponse successResponse = new RDAPHttpRequest.SimpleHttpResponse(
                "track-2", 200, "{\"ok\":true}", testUri, new Header[0]);

            // Use an array to simulate call count
            final int[] callCount = {0};
            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenAnswer(invocation -> {
                if (callCount[0] < 2) {
                    callCount[0]++;
                    return retryResponse;
                } else {
                    return successResponse;
                }
            });

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, "GET");
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).isEqualTo("{\"ok\":true}");
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.SUCCESS);
            assertThat(callCount[0]).isEqualTo(2); // Two retries before success
        }
    }

    @Test
    public void testMakeRequest_HttpTooManyRequests_ExceedsMaxRetries() throws Exception {
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class, Mockito.CALLS_REAL_METHODS)) {

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

            RDAPHttpRequest.SimpleHttpResponse retryResponse = new RDAPHttpRequest.SimpleHttpResponse(
                "track-1", 429, "", testUri, new Header[] { new TestHeader("Retry-After", "1") });

            // Always return 429
            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenReturn(retryResponse);

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, "GET");
            assertThat(response.statusCode()).isEqualTo(429);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.TOO_MANY_REQUESTS);
        }
    }

    // TODO - redo this for THIS class using the MULTI Cert Server
//    @Test
//    public void test_WithLocalHttpsCertificateErrors_ReturnsAppropriateErrorStatus() throws Exception {
//        // Force certificate validation
//        System.setProperty("com.sun.net.ssl.checkRevocation", "true");
//        System.setProperty("com.sun.security.enableCRLDP", "true");
//        System.setProperty("javax.net.ssl.trustStore", getClass().getClassLoader().getResource("keystores/truststore.jks").getPath());
//        System.setProperty("javax.net.ssl.trustStorePassword", "password");
//
//        // Create a custom SSL context with strict validation
//        SSLContext sslContext = SSLContext.getInstance("TLS");
//        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//        KeyStore ks = KeyStore.getInstance("JKS");
//        try (InputStream is = getClass().getClassLoader().getResourceAsStream("keystores/truststore.jks")) {
//            ks.load(is, "password".toCharArray());
//        }
//        tmf.init(ks);
//
//        // Configure the SSL context
//        sslContext.init(null, tmf.getTrustManagers(), null);
//        SSLContext.setDefault(sslContext);
//        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
//
//        // Start HTTPS servers with different certificates
//        MultiCertHttpsTestServer.startHttpsServer(EXPIRED_CERT_PORT, EXPIRED);
//        MultiCertHttpsTestServer.startHttpsServer(INVALID_CERT_PORT, INVALID_HOST);
//        MultiCertHttpsTestServer.startHttpsServer(UNTRUSTED_ROOT_CERT_PORT, UNTRUSTED);
//        Thread.sleep(PAUSE);
//
//        try {
//            // Test expired certificate
//            try (MockedStatic<RDAPHttpRequest> mockedStatic = mockStatic(RDAPHttpRequest.class)) {
//                IOException certificateException = new IOException("Certificate expired");
//                certificateException.initCause(new java.security.cert.CertificateExpiredException("Certificate has expired"));
//                URI expiredCertUri = URI.create(HTTPS_LOCALHOST + EXPIRED_CERT_PORT);
//
//                mockedStatic.when(() -> RDAPHttpRequest.makeHttpGetRequest(expiredCertUri, TIMEOUT_SECONDS))
//                            .thenThrow(certificateException);
//
//                RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
//                results.clear();
//
//                RDAPHttpQuery query = new RDAPHttpQuery(config);
//                query.setResults(results);
//                doReturn(expiredCertUri).when(config).getUri();
//
//                query.run();
//                assertThat(query.getErrorStatus()).isEqualTo(ConnectionStatus.EXPIRED_CERTIFICATE);
//            }
//
//            // Test invalid host certificate
//            try (MockedStatic<RDAPHttpRequest> mockedStatic = mockStatic(RDAPHttpRequest.class)) {
//                IOException certificateException = new IOException("No name matching");
//                certificateException.initCause(new java.security.cert.CertificateException("No subject alternative DNS name matching"));
//                URI invalidCertUri = URI.create(HTTPS_LOCALHOST + INVALID_CERT_PORT);
//
//                mockedStatic.when(() -> RDAPHttpRequest.makeHttpGetRequest(invalidCertUri, TIMEOUT_SECONDS))
//                            .thenThrow(certificateException);
//
//                RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
//                results.clear();
//
//                RDAPHttpQuery query = new RDAPHttpQuery(config);
//                query.setResults(results);
//                doReturn(invalidCertUri).when(config).getUri();
//
//                query.run();
//                assertThat(query.getErrorStatus()).isEqualTo(ConnectionStatus.INVALID_CERTIFICATE);
//            }
//
//            // Test untrusted certificate
//            try (MockedStatic<RDAPHttpRequest> mockedStatic = mockStatic(RDAPHttpRequest.class)) {
//                IOException certificateException = new IOException("SSL handshake failed");
//                certificateException.initCause(new javax.net.ssl.SSLHandshakeException("PKIX path building failed"));
//                URI untrustedCertUri = URI.create(HTTPS_LOCALHOST + UNTRUSTED_ROOT_CERT_PORT);
//
//                mockedStatic.when(() -> RDAPHttpRequest.makeHttpGetRequest(untrustedCertUri, TIMEOUT_SECONDS))
//                            .thenThrow(certificateException);
//
//                RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
//                results.clear();
//
//                RDAPHttpQuery query = new RDAPHttpQuery(config);
//                query.setResults(results);
//                doReturn(untrustedCertUri).when(config).getUri();
//
//                query.run();
//                assertThat(query.getErrorStatus()).isEqualTo(ConnectionStatus.HANDSHAKE_FAILED);
//            }
//        } finally {
//            // Clean up
//            MultiCertHttpsTestServer.stopAll();
//            // Reset system properties
//            System.clearProperty("com.sun.net.ssl.checkRevocation");
//            System.clearProperty("com.sun.security.enableCRLDP");
//            System.clearProperty("javax.net.ssl.trustStore");
//            System.clearProperty("javax.net.ssl.trustStorePassword");
//        }
//    }


    @Test
    public void testSimpleHttpResponse() {
        URI testUri = URI.create("http://example.com");
        Header[] headers = new Header[] {
            new TestHeader("Content-Type", "application/json"),
            new TestHeader("Content-Length", "123")
        };

        RDAPHttpRequest.SimpleHttpResponse response =
            new RDAPHttpRequest.SimpleHttpResponse("track-123", 200, "{\"key\":\"value\"}", testUri, headers);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("{\"key\":\"value\"}");
        assertThat(response.uri()).isEqualTo(testUri);
        assertThat(response.getTrackingId()).isEqualTo("track-123");

        response.setConnectionStatusCode(ConnectionStatus.SUCCESS);
        assertThat(response.getConnectionStatusCode()).isEqualTo(ConnectionStatus.SUCCESS);

        // Test HttpHeaders methods
        assertThat(response.headers().firstValue("Content-Type")).isPresent();
        assertThat(response.headers().firstValue("Content-Type").get()).isEqualTo("application/json");
        assertThat(response.headers().firstValue("Content-Length").get()).isEqualTo("123");
    }

    // Helper class for headers
    private static class TestHeader implements Header {
        private final String name;
        private final String value;

        public TestHeader(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public Header clone() {
            return new TestHeader(name, value);
        }

        @Override
        public boolean isSensitive() {
            return false;
        }
    }
}