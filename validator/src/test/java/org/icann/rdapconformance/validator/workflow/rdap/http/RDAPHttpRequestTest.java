package org.icann.rdapconformance.validator.workflow.rdap.http;

import io.netty.bootstrap.Bootstrap;
import java.net.InetAddress;
import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpResponse;
import java.security.cert.CertificateExpiredException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.net.ssl.SSLHandshakeException;

import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.ConnectionStatus;
import org.icann.rdapconformance.validator.ConnectionTracker;
import org.icann.rdapconformance.validator.DNSCacheResolver;
import org.icann.rdapconformance.validator.NetworkInfo;
import org.icann.rdapconformance.validator.NetworkProtocol;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;

import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest.Header;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icann.rdapconformance.validator.CommonUtils.EMPTY_STRING;
import static org.icann.rdapconformance.validator.CommonUtils.GET;
import static org.icann.rdapconformance.validator.CommonUtils.HTTPS_PORT;
import static org.icann.rdapconformance.validator.CommonUtils.HTTP_PORT;
import static org.icann.rdapconformance.validator.CommonUtils.HTTP_TOO_MANY_REQUESTS;
import static org.icann.rdapconformance.validator.CommonUtils.LOCAL_IPv4;
import static org.icann.rdapconformance.validator.CommonUtils.ONE;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;


public class RDAPHttpRequestTest {

    public static final String RETRY_AFTER = "Retry-After";
    public static final int HTTP_HIGH_PORT = 8080;
    private final URI testUri = URI.create("http://example.com/path");
    private final int timeout = 10;


    @Test
    public void testMakeRequest_UnknownHost() throws Exception {
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class)) {

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(Mockito.any())).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(Mockito.any())).thenReturn(mockAddress);

            // Mock the exception
            UnknownHostException exception = new UnknownHostException("Unknown host");
            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any()))
                           .thenThrow(exception);

            // Allow the real method implementation for handleRequestException
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(Mockito.any(), Mockito.anyBoolean()))
                           .thenCallRealMethod();

            // Make sure all real makeRequest implementations are called
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(Mockito.any(URI.class), Mockito.anyInt(), Mockito.any()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(Mockito.any(URI.class), Mockito.anyInt(), Mockito.any(), Mockito.anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(Mockito.any(URI.class), Mockito.anyInt(), Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                           .thenCallRealMethod();

            // Also allow hasCause to work properly
            httpRequestMock.when(() -> RDAPHttpRequest.hasCause(Mockito.any(), Mockito.any()))
                           .thenCallRealMethod();

            // Execute the test
            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, "GET", true);

            // Verify the status code is set correctly
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

            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any()))
                           .thenThrow(new IOException("Connection closed by peer"));


            // Add all versions of makeRequest
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any(), anyBoolean()))
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

            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any()))
                           .thenThrow(new ConnectException("Connection refused"));

            // Add mocks for all versions of makeRequest
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any(), anyBoolean()))
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

            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any()))
                           .thenThrow(new SocketTimeoutException("Read timed out"));

            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any(), anyBoolean()))
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
            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any()))
                           .thenThrow(new SocketTimeoutException("Connect timed out"));


            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any(), anyBoolean()))
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

            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any()))
                           .thenThrow(new EOFException("Unexpected end of file"));

            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any(), anyBoolean()))
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

            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any()))
                           .thenThrow(new IOException("Connection reset"));

            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any(), anyBoolean()))
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

            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any()))
                           .thenThrow(new IOException("Malformed chunk"));

            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any(), anyBoolean()))
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

            CertificateExpiredException certEx = new CertificateExpiredException("Certificate expired");
            SSLHandshakeException sslEx = new SSLHandshakeException("Certificate validation failed");
            sslEx.initCause(certEx);
            IOException ioEx = new IOException("SSL Handshake failed");
            ioEx.initCause(sslEx);

            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any()))
                           .thenThrow(ioEx);

            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any(), anyBoolean()))
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

            SSLHandshakeException sslEx = new SSLHandshakeException("PKIX path building failed");
            IOException ioEx = new IOException("SSL handshake failed");
            ioEx.initCause(sslEx);

            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any()))
                           .thenThrow(ioEx);

            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any(), anyBoolean()))
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
            InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

            // Mock for 429
            RDAPHttpRequest.SimpleHttpResponse retryResponse = mock(RDAPHttpRequest.SimpleHttpResponse.class);
            when(retryResponse.statusCode()).thenReturn(429);
            when(retryResponse.body()).thenReturn("");
            when(retryResponse.getConnectionStatusCode()).thenReturn(ConnectionStatus.TOO_MANY_REQUESTS);
            // Simulate Retry-After header
            Map<String, List<String>> retryHeaders = Map.of(RETRY_AFTER, List.of("1"));
            when(retryResponse.headers()).thenReturn(java.net.http.HttpHeaders.of(retryHeaders, (k, v) -> true));

            // Mock for 200
            RDAPHttpRequest.SimpleHttpResponse successResponse = mock(RDAPHttpRequest.SimpleHttpResponse.class);
            when(successResponse.statusCode()).thenReturn(200);
            when(successResponse.body()).thenReturn("");
            when(successResponse.getConnectionStatusCode()).thenReturn(ConnectionStatus.SUCCESS);
            when(successResponse.headers()).thenReturn(java.net.http.HttpHeaders.of(Map.of(), (k, v) -> true));

            // Simulate two 429s, then a 200
            final int[] callCount = {ZERO};
            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), anyInt(), any(), any(), any(), anyInt(), any(), any()))
                           .thenAnswer(invocation -> {
                               if (callCount[ZERO] < 2) {
                                   callCount[ZERO]++;
                                   return retryResponse;
                               } else {
                                   return successResponse;
                               }
                           });

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, GET);
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).isEqualTo("");
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.SUCCESS);
            assertThat(callCount[ZERO]).isEqualTo(2); // Two retries before success
        }
    }

    @Test
    public void testMakeRequest_HttpTooManyRequests_ExceedsMaxRetries() throws Exception {
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class, Mockito.CALLS_REAL_METHODS)) {

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

            // Always return a 429 response
            RDAPHttpRequest.SimpleHttpResponse retryResponse = mock(RDAPHttpRequest.SimpleHttpResponse.class);
            when(retryResponse.statusCode()).thenReturn(429);
            when(retryResponse.getConnectionStatusCode()).thenReturn(ConnectionStatus.TOO_MANY_REQUESTS);

            // Mock executeRequest to always return 429
            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(
                               any(URI.class), anyInt(), anyString(), any(), any(), anyInt(), anyString(), any()))
                           .thenReturn(retryResponse);

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, GET);
            assertThat(response.statusCode()).isEqualTo(429);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse) response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.TOO_MANY_REQUESTS);
        }
    }

    @Test
    public void testMakeRequest_NullUri() {
        // If you need to verify the message, use a try-catch approach
        try {
            RDAPHttpRequest.makeRequest(null, timeout, GET);
            // Should not reach here
            Assert.fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("The provided URI is null.");
        } catch (Exception e) {
            Assert.fail("Expected IllegalArgumentException but got " + e.getClass().getName());
        }
    }


    @Test
    public void testMakeRequest_NoAddressesForHost() throws Exception {
        URI noAddressUri = URI.create("http://nonexistent.example.com/path");

        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class);
            MockedStatic<ConnectionTracker> trackerMock = Mockito.mockStatic(ConnectionTracker.class)) {

            // Mock the onnection tracker
            ConnectionTracker mockTracker = mock(ConnectionTracker.class);
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

            // Set up DNS resolver to return no addresses for the host
            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses("nonexistent.example.com")).thenReturn(true);

            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();

            // execute it!
            HttpResponse<String> response = RDAPHttpRequest.makeRequest(noAddressUri, timeout, GET);

            // Verify response
            assertThat(response).isNotNull()
                                .isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(response.statusCode()).isEqualTo(ZERO);
            assertThat(response.body()).isEmpty();
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.UNKNOWN_HOST);

            // Verify that the tracker picked it up as well
            verify(mockTracker).startTrackingNewConnection(eq(noAddressUri), eq(GET), eq(false));
            verify(mockTracker).completeCurrentConnection(eq(ZERO), eq(ConnectionStatus.UNKNOWN_HOST));
        }
    }


    @Test
    public void testMakeRequest_IPv6NetworkProtocol() throws Exception {
        URI validUri = URI.create("http://example.com/path");

        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<ConnectionTracker> trackerMock = Mockito.mockStatic(ConnectionTracker.class);
            MockedStatic<NetworkInfo> networkInfoMock = Mockito.mockStatic(NetworkInfo.class);
            MockedStatic<RDAPHttpRequest> executeRequestMock = Mockito.mockStatic(RDAPHttpRequest.class, Mockito.CALLS_REAL_METHODS)) {

            ConnectionTracker mockTracker = mock(ConnectionTracker.class);
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

            networkInfoMock.when(NetworkInfo::getNetworkProtocol).thenReturn(NetworkProtocol.IPv6);

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses("example.com")).thenReturn(false);
            InetAddress ipv6Address = InetAddress.getByName("::1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV6Address("example.com")).thenReturn(ipv6Address);

            // Only mock executeRequest to return a successful response
            RDAPHttpRequest.SimpleHttpResponse mockResponse =
                new RDAPHttpRequest.SimpleHttpResponse("test-id", 200, "OK", validUri, new RDAPHttpRequest.Header[0]);
            mockResponse.setConnectionStatusCode(ConnectionStatus.SUCCESS);

            executeRequestMock.when(() -> RDAPHttpRequest.executeRequest(
                                  any(), anyInt(), anyString(), any(), any(), anyInt(), anyString(), any()))
                              .thenReturn(mockResponse);

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(validUri, 10, "GET");

            dnsResolverMock.verify(() -> DNSCacheResolver.getFirstV6Address("example.com"));
            dnsResolverMock.verify(() -> DNSCacheResolver.getFirstV4Address(anyString()), never());

            assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse) response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.SUCCESS);
        }
    }


    @Test
    public void testMakeRequest_HttpsScheme_DefaultPort() throws Exception {
        URI httpsUri = URI.create("https://example.com/path");

        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<ConnectionTracker> trackerMock = Mockito.mockStatic(ConnectionTracker.class);
            MockedStatic<NetworkInfo> networkInfoMock = Mockito.mockStatic(NetworkInfo.class)) {

            // Mock tracker
            ConnectionTracker mockTracker = mock(ConnectionTracker.class);
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

            // DNS and network protocol
            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses("example.com")).thenReturn(false);
            networkInfoMock.when(NetworkInfo::getNetworkProtocol).thenReturn(NetworkProtocol.IPv4);
            InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address("example.com")).thenReturn(mockAddress);

            // Mock getDefaultIPv4Address to return our mockAddress
            MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class, Mockito.CALLS_REAL_METHODS);
            httpRequestMock.when(RDAPHttpRequest::getDefaultIPv4Address).thenReturn(mockAddress);

            // Mock executeRequest to simulate a successful response
            RDAPHttpRequest.SimpleHttpResponse mockResponse = new RDAPHttpRequest.SimpleHttpResponse(
                "test-id", 200, "body", httpsUri, new RDAPHttpRequest.Header[0]);
            mockResponse.setConnectionStatusCode(ConnectionStatus.SUCCESS);

            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(
                               any(), anyInt(), anyString(), any(), any(), anyInt(), anyString(), any()))
                           .thenReturn(mockResponse);

            // Call the real makeRequest methods
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();

            // Execute the test
            HttpResponse<String> response = RDAPHttpRequest.makeRequest(httpsUri, 10, "GET");

            // Verify port is 443 (default for https)
            assertThat(httpsUri.getPort() == -1 ? HTTPS_PORT : httpsUri.getPort()).isEqualTo(HTTPS_PORT);

            // Verify the response
            assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse) response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.SUCCESS);

            httpRequestMock.close();
        }
    }


    @Test
    public void testMakeRequest_HttpScheme_DefaultPort() throws Exception {
        URI httpUri = URI.create("http://example.com/path");

        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<NetworkInfo> networkInfoMock = Mockito.mockStatic(NetworkInfo.class);
            MockedStatic<ConnectionTracker> trackerMock = Mockito.mockStatic(ConnectionTracker.class)) {

            ConnectionTracker mockTracker = mock(ConnectionTracker.class);
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses("example.com")).thenReturn(false);
            networkInfoMock.when(NetworkInfo::getNetworkProtocol).thenReturn(NetworkProtocol.IPv4);
            InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address("example.com")).thenReturn(mockAddress);

            // Mock the Netty-based response
            RDAPHttpRequest.SimpleHttpResponse mockResponse = mock(RDAPHttpRequest.SimpleHttpResponse.class);
            when(mockResponse.statusCode()).thenReturn(HTTP_OK);
            when(mockResponse.getConnectionStatusCode()).thenReturn(ConnectionStatus.SUCCESS);

            // Use a spy to call the real method but intercept executeRequest
            try (MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class, Mockito.CALLS_REAL_METHODS)) {
                httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(
                                   any(), anyInt(), anyString(), any(), any(), anyInt(), anyString(), any()))
                               .thenReturn(mockResponse);

                HttpResponse<String> response = RDAPHttpRequest.makeRequest(httpUri, timeout, GET);

                // Verify the port logic
                assertThat(httpUri.getPort() == -1 ? HTTP_PORT : httpUri.getPort()).isEqualTo(HTTP_PORT);

                // Verify response
                assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
                assertThat(response.statusCode()).isEqualTo(HTTP_OK);
                assertThat(((RDAPHttpRequest.SimpleHttpResponse) response).getConnectionStatusCode())
                    .isEqualTo(ConnectionStatus.SUCCESS);
            }
        }
    }

    @Test
    public void testMakeRequest_CustomPort() throws Exception {
        URI customPortUri = URI.create("http://example.com:8080/path");

        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<NetworkInfo> networkInfoMock = Mockito.mockStatic(NetworkInfo.class)) {

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses("example.com")).thenReturn(false);
            networkInfoMock.when(NetworkInfo::getNetworkProtocol).thenReturn(NetworkProtocol.IPv4);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address("example.com")).thenReturn(mockAddress);

            // Mock the response as if Netty returned it
            RDAPHttpRequest.SimpleHttpResponse response = new RDAPHttpRequest.SimpleHttpResponse(
                "test-id", 200, "body", customPortUri, new RDAPHttpRequest.Header[0]);
            response.setConnectionStatusCode(ConnectionStatus.SUCCESS);

            try (MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class, Mockito.CALLS_REAL_METHODS)) {
                httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(eq(customPortUri), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                               .thenReturn(response);

                HttpResponse<String> actualResponse = RDAPHttpRequest.makeRequest(customPortUri, 10, "GET");

                assertThat(actualResponse).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
                assertThat(actualResponse.statusCode()).isEqualTo(200);
                assertThat(((RDAPHttpRequest.SimpleHttpResponse) actualResponse).getConnectionStatusCode())
                    .isEqualTo(ConnectionStatus.SUCCESS);
                assertThat(actualResponse.uri().getPort()).isEqualTo(8080);
            }
        }
    }

    @Test
    public void testHandleRequestException_UnknownHostExceptionFull() throws Exception {
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class);
            MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class)) {

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

            UnknownHostException ex = new UnknownHostException("Unknown host");
            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any()))
                           .thenThrow(ex);
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.hasCause(any(), any()))
                           .thenCallRealMethod();

            // call it
            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, GET);

            // Verifications
            assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.UNKNOWN_HOST);
        }
    }

    @Test
    public void testHandleRequestException_ConnectExceptionFull() throws Exception {
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class);
            MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class)) {

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

            ConnectException ex = new ConnectException("Connection refused");
            // Use doThrow/when pattern which handles matchers better
            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any()))
                           .thenThrow(ex);

            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(eq(testUri), eq(timeout), eq(GET)))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(eq(testUri), eq(timeout), eq(GET), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(eq(testUri), eq(timeout), eq(GET), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any(IOException.class), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.hasCause(any(Throwable.class), anyString()))
                           .thenCallRealMethod();

            // call makeRequest
            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, GET);

            // Verify
            assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.CONNECTION_FAILED);

            // Verify that the error was added - use specific arguments instead of matchers in the lambda
            commonUtilsMock.verify(() ->
                CommonUtils.addErrorToResultsFile(0, -13007, "no response available",
                    "Failed to connect to server."));
        }
    }


    @Test
    public void testHandleRequestException_SocketTimeoutReadTimeoutFull() throws Exception {
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class);
            MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class)) {

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

            SocketTimeoutException ex = new SocketTimeoutException("Read timed out");
            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any()))
                           .thenThrow(ex);
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.hasCause(any(), any()))
                           .thenCallRealMethod();

            // Run it
            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, GET);

            // Verify all
            assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);

            // Check that the error was added
            commonUtilsMock.verify(() ->
                CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13017), eq("no response available"),
                    eq("Network receive fail")), times(ONE));
        }
    }

    @Test
    public void testHandleRequestException_SocketTimeoutConnectTimeoutFull() throws Exception {
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class);
            MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class)) {

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

            SocketTimeoutException ex = new SocketTimeoutException("Connect timed out");
            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.any(),
                               Mockito.anyInt(),
                               Mockito.any(),
                               Mockito.any()))
                           .thenThrow(ex);
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.hasCause(any(), any()))
                           .thenCallRealMethod();

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, GET);

            assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.NETWORK_SEND_FAIL);
            commonUtilsMock.verify(() ->
                CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13016), eq("no response available"),
                    eq("Network send fail")), times(1));
        }
    }



    // NOTE: Below here is a yet another set of tests so we can be sure we are covering all the exceptions we can
    @Test
    public void testHandleRequestException_UnknownHostExceptionNotRecorded() {
        try (MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class)) {
            RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
            results.clear();

            // Test with recordError = true
            UnknownHostException ex = new UnknownHostException("Unknown host");
            // For WHATEVER reason, we don't have an error for this -- though it would have been caught in the original DNS check
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ex, true);

            // Verify correct status is returned
            assertThat(status).isEqualTo(ConnectionStatus.UNKNOWN_HOST);

            // Let's do it again but with recordError = false this time
            ex = new UnknownHostException("Unknown host");
            status = RDAPHttpRequest.handleRequestException(ex, false);

            // Verify correct status is returned but no error recorded
            assertThat(status).isEqualTo(ConnectionStatus.UNKNOWN_HOST);

            // We shouldn't have anything in the errors
            commonUtilsMock.verify(() ->
                    CommonUtils.addErrorToResultsFile(anyInt(), anyInt(), anyString(), anyString()),
                never());
        }
    }

    @Test
    public void testHandleRequestException_ConnectException() {
        try (MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class)) {
            RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
            results.clear();

            // Test with recordError = true
            ConnectException ex = new ConnectException("Connection refused");
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ex, true);

            // Verify correct status is returned
            assertThat(status).isEqualTo(ConnectionStatus.CONNECTION_FAILED);

            // Verify this specific area was added
            commonUtilsMock.verify(() ->
                    CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13007), eq("no response available"),
                        eq("Failed to connect to server.")),
                times(ONE));

            // This time we test yet again, but with recordError = false
            status = RDAPHttpRequest.handleRequestException(ex, false);

            // Verify the correct status is returned but no additional error recorded
            assertThat(status).isEqualTo(ConnectionStatus.CONNECTION_FAILED);
            commonUtilsMock.verify(() ->
                    CommonUtils.addErrorToResultsFile(anyInt(), anyInt(), anyString(), anyString()),
                times(ONE)); // Still just 1 call from before
        }
    }

    @Test
    public void testHandleRequestException_HttpTimeoutException() {
        try (MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class)) {
            RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
            results.clear();

            // Test with recordError = true
            java.net.http.HttpTimeoutException ex = new java.net.http.HttpTimeoutException("Request timed out");
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ex, true);

            // Verify correct status is returned
            assertThat(status).isEqualTo(ConnectionStatus.CONNECTION_FAILED);

            // Verify  the correct error is added with the correct code
            commonUtilsMock.verify(() ->
                    CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13007), eq("no response available"),
                        eq("Failed to connect to server.")),
                times(ONE));
        }
    }

    @Test
    public void testHandleRequestException_UnresolvedAddressException() {
        try (MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class,
                invocation -> {
                    if (!invocation.getMethod().getName().equals("hasCause")) {
                        return invocation.callRealMethod();
                    }
                    return null;
                })) {

            RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
            results.clear();

            // Create a custom exception with the right cause for UnresolvedAddressException
            IOException ioEx = new ConnectException("Connection failed");
            Exception unresolvedEx = new Exception("java.nio.channels.UnresolvedAddressException");
            ioEx.initCause(unresolvedEx);

            // Mock hasCause to return true for this specific cause
            httpRequestMock.when(() -> RDAPHttpRequest.hasCause(eq(ioEx), eq("java.nio.channels.UnresolvedAddressException")))
                           .thenReturn(true);

            // Test with recordError = true
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ioEx, true);

            // Verify correct status is returned
            assertThat(status).isEqualTo(ConnectionStatus.NETWORK_SEND_FAIL);

            // Verify correct error is added
            commonUtilsMock.verify(() ->
                    CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13016), eq("no response available"),
                        eq("Network send fail")),
                times(ONE));
        }
    }

    @Test
    public void testHandleRequestException_CertificateExpiredException() {
        try (MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class,
                invocation -> {
                    if (!invocation.getMethod().getName().equals("hasCause")) {
                        return invocation.callRealMethod();
                    }
                    return null;
                })) {

            RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
            results.clear();

            // Create a chain of exceptions for expired certificate
            CertificateExpiredException certEx = new CertificateExpiredException("Certificate expired");
            SSLHandshakeException sslEx = new SSLHandshakeException("Certificate validation failed");
            sslEx.initCause(certEx);
            IOException ioEx = new IOException("SSL handshake failed");
            ioEx.initCause(sslEx);

            // Mock hasCause method
            httpRequestMock.when(() ->
                               RDAPHttpRequest.hasCause(eq(ioEx), eq("java.security.cert.CertificateExpiredException")))
                           .thenReturn(true);

            // Test with recordError = true
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ioEx, true);

            // Verify the correct status is returned....
            assertThat(status).isEqualTo(ConnectionStatus.EXPIRED_CERTIFICATE);

            // Verify the correct error is added
            commonUtilsMock.verify(() ->
                    CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13011), eq("no response available"),
                        eq("Expired certificate.")),
                times(ONE));

            // Now ... do it again, but this time don't record the error
            status = RDAPHttpRequest.handleRequestException(ioEx, false);

            // Verify
            assertThat(status).isEqualTo(ConnectionStatus.EXPIRED_CERTIFICATE);
            commonUtilsMock.verify(() ->
                    CommonUtils.addErrorToResultsFile(anyInt(), anyInt(), anyString(), anyString()),
                times(ONE)); // Should still be 1 from before
        }
    }

    @Test
    public void testHandleRequestException_CertificateRevokedException() {
        try (MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class,
                invocation -> {
                    if (!invocation.getMethod().getName().equals("hasCause")) {
                        return invocation.callRealMethod();
                    }
                    return null;
                })) {

            RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
            results.clear();

            // Create an IOException with the right cause chain -> SSL w/ Revocation
            IOException ioEx = new IOException("SSL handshake failed");

            // Mock hasCause to return true for this specific cause
            httpRequestMock.when(() ->
                               RDAPHttpRequest.hasCause(eq(ioEx), eq("java.security.cert.CertificateRevokedException")))
                           .thenReturn(true);

            // Test with recordError = true
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ioEx, true);

            // Verify correct status is returned
            assertThat(status).isEqualTo(ConnectionStatus.REVOKED_CERTIFICATE);

            // Verify correct error is added
            commonUtilsMock.verify(() ->
                    CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13010), eq("no response available"),
                        eq("Revoked TLS certificate.")),
                times(ONE));
        }
    }

    @Test
    public void testHandleRequestException_InvalidCertificate() {
        try (MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class,
                invocation -> {
                    if (!invocation.getMethod().getName().equals("hasCause")) {
                        return invocation.callRealMethod();
                    }
                    return null;
                })) {

            RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
            results.clear();

            // Create the exception with correct message
            IOException ioEx = new IOException("No name matching example.com found");
            ioEx.initCause(new java.security.cert.CertificateException(
                "No subject alternative DNS name matching example.com found"));

            // Mock hasCause to return true for CertificateException
            httpRequestMock.when(() ->
                               RDAPHttpRequest.hasCause(eq(ioEx), eq("java.security.cert.CertificateException")))
                           .thenReturn(true);

            // Test with recordError = true
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ioEx, true);

            // Verify the cert is invalid
            assertThat(status).isEqualTo(ConnectionStatus.INVALID_CERTIFICATE);

            // Verify the error was added
            commonUtilsMock.verify(() ->
                    CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13009), eq("no response available"),
                        eq("Invalid TLS certificate.")),
                times(ONE));
        }
    }

    @Test
    public void testHandleRequestException_OtherCertificateError() {
        try (MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class,
                invocation -> {
                    if (!invocation.getMethod().getName().equals("hasCause")) {
                        return invocation.callRealMethod();
                    }
                    return null;
                })) {

            RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
            results.clear();

            // Create an exception for a general certificate error
            IOException ioEx = new IOException("Certificate error");
            ioEx.initCause(new java.security.cert.CertificateException("General certificate error"));

            // Mock hasCause to return true for CertificateException
            httpRequestMock.when(() ->
                               RDAPHttpRequest.hasCause(eq(ioEx), eq("java.security.cert.CertificateException")))
                           .thenReturn(true);

            // Test with recordError = true
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ioEx, true);

            // Verify correct status is returned
            assertThat(status).isEqualTo(ConnectionStatus.CERTIFICATE_ERROR);

            // Verify correct error is added
            commonUtilsMock.verify(() ->
                    CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13012), eq("no response available"),
                        eq("TLS certificate error.")),
                times(ONE));
        }
    }

    @Test
    public void testHandleRequestException_SSLHandshakeException() {
        try (MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class,
                invocation -> {
                    if (!invocation.getMethod().getName().equals("hasCause")) {
                        return invocation.callRealMethod();
                    }
                    return null;
                })) {

            RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
            results.clear();

            // Create an exception for SSL handshake failure
            SSLHandshakeException sslEx = new SSLHandshakeException("PKIX path building failed");
            IOException ioEx = new IOException("SSL handshake failed");
            ioEx.initCause(sslEx);

            // Mock hasCause to return true for SSLHandshakeException
            httpRequestMock.when(() ->
                               RDAPHttpRequest.hasCause(eq(ioEx), eq("javax.net.ssl.SSLHandshakeException")))
                           .thenReturn(true);

            // Test with recordError = true
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ioEx, true);

            // Verify correct status is returned
            assertThat(status).isEqualTo(ConnectionStatus.HANDSHAKE_FAILED);

            // Verify correct error is added
            commonUtilsMock.verify(() ->
                    CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13008), eq("no response available"),
                        eq("TLS handshake failed.")),
                times(ONE));
        }
    }

    @Test
    public void testHandleRequestException_ValidatorException() {
        try (MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class,
                invocation -> {
                    if (!invocation.getMethod().getName().equals("hasCause")) {
                        return invocation.callRealMethod();
                    }
                    return null;
                })) {

            RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
            results.clear();

            // Create an exception with ValidatorException in the chain
            IOException ioEx = new IOException("Certificate validation failed");

            // Mock hasCause to return true for ValidatorException
            httpRequestMock.when(() ->
                               RDAPHttpRequest.hasCause(eq(ioEx), eq("sun.security.validator.ValidatorException")))
                           .thenReturn(true);

            // Test with recordError = true
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ioEx, true);

            // Verify correct status is returned
            assertThat(status).isEqualTo(ConnectionStatus.CERTIFICATE_ERROR);

            // Verify correct error is added
            commonUtilsMock.verify(() ->
                    CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13012), eq("no response available"),
                        eq("TLS certificate error.")),
                times(ONE));
        }
    }

    @Test
    public void testHandleRequestException_SocketTimeoutReadTimeout() {
        try (MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class)) {
            RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
            results.clear();

            // Test with recordError = true
            SocketTimeoutException ex = new SocketTimeoutException("Read timed out");
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ex, true);

            // Verify correct status is returned
            assertThat(status).isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);

            // Verify correct error is added
            commonUtilsMock.verify(() ->
                    CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13017), eq("no response available"),
                        eq("Network receive fail")),
                times(ONE));
        }
    }

    @Test
    public void testHandleRequestException_SocketTimeoutConnectTimeout() {
        try (MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class)) {
            RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
            results.clear();

            // Test with recordError = true
            SocketTimeoutException ex = new SocketTimeoutException("Connect timed out");
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ex, true);

            // Verify correct status is returned
            assertThat(status).isEqualTo(ConnectionStatus.NETWORK_SEND_FAIL);

            // Verify correct error is added
            commonUtilsMock.verify(() ->
                    CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13016), eq("no response available"),
                        eq("Network send fail")),
                times(ONE));
        }
    }

    @Test
    public void testHandleRequestException_EOFException() {
        try (MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class)) {
            RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
            results.clear();

            // Test with recordError = true
            EOFException ex = new EOFException("Unexpected end of file");
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ex, true);

            // Verify correct status is returned
            assertThat(status).isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);

            // Verify correct error is added
            commonUtilsMock.verify(() ->
                    CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13017), eq("no response available"),
                        eq("Network receive fail")),
                times(ONE));
        }
    }

    @Test
    public void testHandleRequestException_ConnectionReset() {
        try (MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class)) {
            RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
            results.clear();

            // Test with recordError = true
            IOException ex = new IOException("Connection reset");
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ex, true);

            // Verify correct status is returned
            assertThat(status).isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);

            // Verify correct error is added
            commonUtilsMock.verify(() ->
                    CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13017), eq("no response available"),
                        eq("Network receive fail")),
                times(ONE));
        }
    }

    @Test
    public void testHandleRequestException_ConnectionClosedByPeer() {
        try (MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class)) {
            RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
            results.clear();

            // Test with recordError = true
            IOException ex = new IOException("Connection closed by peer");
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ex, true);

            // Verify correct status is returned
            assertThat(status).isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);

            // Verify correct error is added
            commonUtilsMock.verify(() ->
                    CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13017), eq("no response available"),
                        eq("Network receive fail")),
                times(ONE));
        }
    }

    @Test
    public void testHandleRequestException_GenericIOException() {
        try (MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class)) {

            RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
            results.clear();

            IOException ex = new IOException("Some other IO error");
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ex, true);

            assertThat(status).isEqualTo(ConnectionStatus.CONNECTION_FAILED);
            commonUtilsMock.verify(() ->
                    CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13007), eq("no response available"),
                        eq("Failed to connect to server.")),
                times(ONE));
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
            new Header("Content-Type", "application/json"),
            new Header("Content-Length", "123")
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
}