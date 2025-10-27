package org.icann.rdapconformance.validator.workflow.rdap.http;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.X509TrustManager;
import java.util.Date;
import java.util.List;
import java.net.DatagramSocket;

import org.apache.hc.core5.http.MalformedChunkCodingException;
import org.apache.hc.core5.http.MessageConstraintException;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.TruncatedChunkException;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.commons.lang3.tuple.Pair;

import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.ConnectionStatus;
import org.icann.rdapconformance.validator.ConnectionTracker;
import org.icann.rdapconformance.validator.DNSCacheResolver;
import org.icann.rdapconformance.validator.NetworkInfo;
import org.icann.rdapconformance.validator.NetworkProtocol;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.testng.Assert;

import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icann.rdapconformance.validator.CommonUtils.EMPTY_STRING;
import static org.icann.rdapconformance.validator.CommonUtils.GET;
import static org.icann.rdapconformance.validator.CommonUtils.HTTP_TOO_MANY_REQUESTS;
import static org.icann.rdapconformance.validator.CommonUtils.LOCAL_IPv4;
import static org.icann.rdapconformance.validator.CommonUtils.ONE;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;



public class RDAPHttpRequestTest {

    public static final String RETRY_AFTER = "Retry-After";
    public static final int HTTP_HIGH_PORT = 8080;
    private final URI testUri = URI.create("http://example.com/path");
    private final int timeout = 10;

    // Test QueryContext for deprecated method replacement
    private static QueryContext createTestQueryContext() {
        RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
        when(config.isGtldRegistrar()).thenReturn(true);
        return QueryContext.forTesting(config);
    }

    @Test
    public void testMakeRequest_UnknownHost() throws Exception {
        URI unknownHostUri = URI.create("http://unknownhost.example/path");

        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<ConnectionTracker> trackerMock = Mockito.mockStatic(ConnectionTracker.class)) {

            ConnectionTracker mockTracker = mock(ConnectionTracker.class);
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

            // Simulate no addresses for the host
            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses("unknownhost.example")).thenReturn(true);

            // Call the real method (no need to mock executeRequest, as it should not be called)
            HttpResponse<String> response = RDAPHttpRequest.makeRequest(createTestQueryContext(), unknownHostUri, timeout, GET);

            assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(response.statusCode()).isEqualTo(ZERO);
            assertThat(response.body()).isEmpty();
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.UNKNOWN_HOST);

            verify(mockTracker).startTrackingNewConnection(eq(unknownHostUri), eq(GET), eq(false), eq(NetworkProtocol.IPv4));
        }
    }


    @Test
    public void testMakeRequest_ConnectionClosedByPeer() throws Exception {
        URI uri = URI.create("http://example.com/path");

        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<ConnectionTracker> trackerMock = Mockito.mockStatic(ConnectionTracker.class);
            MockedStatic<org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest> execMock =
                Mockito.mockStatic(org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest.class, Mockito.CALLS_REAL_METHODS)) {

            // Mock DNS resolution
            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

            ConnectionTracker mockTracker = mock(ConnectionTracker.class);
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

            // Only mock executeRequest to throw IOException
            execMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                    .thenThrow(new IOException("Connection closed by peer"));

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(createTestQueryContext(), uri, timeout, GET);

            assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);

            verify(mockTracker).startTrackingNewConnection(eq(uri), eq(GET), eq(false), eq(NetworkProtocol.IPv4));
        }
    }

    @Test
    public void testMakeRequest_ConnectException() throws Exception {
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
             MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<ConnectionTracker> trackerMock = mockStatic(ConnectionTracker.class);
             MockedStatic<NetworkInfo> networkInfoMock = mockStatic(NetworkInfo.class)) {

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);


            // NetworkInfo mocking removed - tests use defaults

            ConnectionTracker mockTracker = mock(ConnectionTracker.class);
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                          .thenThrow(new ConnectException("Connection refused"));


            HttpResponse<String> response = RDAPHttpRequest.makeRequest(createTestQueryContext(), testUri, timeout, GET);

            assertThat(response).isNotNull()
                              .isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.CONNECTION_REFUSED);

            verify(mockTracker).startTrackingNewConnection(eq(testUri), eq(GET), eq(false), eq(NetworkProtocol.IPv4));
        }
    }

@Test
public void testMakeRequest_SocketTimeoutException_ReadTimeout() throws Exception {
    try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
         MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class, Mockito.CALLS_REAL_METHODS);

         MockedStatic<ConnectionTracker> trackerMock = mockStatic(ConnectionTracker.class);
         MockedStatic<NetworkInfo> networkInfoMock = mockStatic(NetworkInfo.class)) {


        dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
        InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
        dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

        // NetworkInfo mocking removed - tests use defaults

        ConnectionTracker mockTracker = mock(ConnectionTracker.class);
        when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id");
        trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

        // Throw SocketTimeoutException on executeRequest
        httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                      .thenThrow(new SocketTimeoutException("Read timed out"));

        HttpResponse<String> response = RDAPHttpRequest.makeRequest(createTestQueryContext(), testUri, timeout, GET);

        assertThat(response).isNotNull()
                          .isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
        assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
            .isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);

        verify(mockTracker).startTrackingNewConnection(eq(testUri), eq(GET), eq(false), eq(NetworkProtocol.IPv4));
    }
}

@Test
public void testMakeRequest_SocketTimeoutException_ConnectTimeout() throws Exception {
    try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
         MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class, Mockito.CALLS_REAL_METHODS);
         MockedStatic<ConnectionTracker> trackerMock = mockStatic(ConnectionTracker.class);
         MockedStatic<NetworkInfo> networkInfoMock = mockStatic(NetworkInfo.class)) {

        dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
        InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
        dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

        // NetworkInfo mocking removed - tests use defaults

        ConnectionTracker mockTracker = mock(ConnectionTracker.class);
        when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id");
        trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

        httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                      .thenThrow(new SocketTimeoutException("connect timed out"));


        HttpResponse<String> response = RDAPHttpRequest.makeRequest(createTestQueryContext(), testUri, timeout, GET);

        assertThat(response).isNotNull()
                          .isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
        assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
            .isEqualTo(ConnectionStatus.NETWORK_SEND_FAIL);

        verify(mockTracker).startTrackingNewConnection(eq(testUri), eq(GET), eq(false), eq(NetworkProtocol.IPv4));
    }
}

@Test
public void testMakeRequest_EOFException() throws Exception {
    try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
         MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class, Mockito.CALLS_REAL_METHODS);
         MockedStatic<ConnectionTracker> trackerMock = mockStatic(ConnectionTracker.class);
         MockedStatic<NetworkInfo> networkInfoMock = mockStatic(NetworkInfo.class)) {

        dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
        InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
        dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

        // NetworkInfo mocking removed - tests use defaults

        ConnectionTracker mockTracker = mock(ConnectionTracker.class);
        when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id");
        trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

        httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                      .thenThrow(new EOFException("EOF"));

        HttpResponse<String> response = RDAPHttpRequest.makeRequest(createTestQueryContext(), testUri, timeout, GET);

        assertThat(response).isNotNull()
                          .isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
        assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
            .isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);

        verify(mockTracker).startTrackingNewConnection(eq(testUri), eq(GET), eq(false), eq(NetworkProtocol.IPv4));
    }
}

   @Test
   public void testMakeRequest_ConnectionReset() throws Exception {
       try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class, CALLS_REAL_METHODS);
            MockedStatic<NetworkInfo> networkInfoMock = mockStatic(NetworkInfo.class);
            MockedStatic<ConnectionTracker> trackerMock = mockStatic(ConnectionTracker.class)) {

           dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
           InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
           dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);
           // NetworkInfo mocking removed - tests use defaults

           ConnectionTracker mockTracker = mock(ConnectionTracker.class);
           when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id");
           trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

           IOException resetException = new IOException("Connection reset by peer");
           httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenThrow(resetException);

           HttpResponse<String> response = RDAPHttpRequest.makeRequest(createTestQueryContext(), new URI("http://example.com/path"), 30, GET);

           assertThat(response).isNotNull()
                             .isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
           assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
               .isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);

           verify(mockTracker).startTrackingNewConnection(any(), eq(GET), eq(false), any(NetworkProtocol.class));
       }
   }


  @Test
  public void testMakeRequest_MalformedResponseChunk() throws Exception {
      try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
           MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class, CALLS_REAL_METHODS);
           MockedStatic<NetworkInfo> networkInfoMock = mockStatic(NetworkInfo.class);
           MockedStatic<ConnectionTracker> trackerMock = mockStatic(ConnectionTracker.class)) {

          dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
          InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
          dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

          // NetworkInfo mocking removed - tests use defaults
          ConnectionTracker mockTracker = mock(ConnectionTracker.class);
          when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id");
          trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

          IOException malformedChunkException = new IOException("Malformed chunk");
          httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenThrow(malformedChunkException);

          URI testUri = new URI("http://example.com/path");
          HttpResponse<String> response = RDAPHttpRequest.makeRequest(createTestQueryContext(), testUri, 30, GET);

          assertThat(response).isNotNull();
          assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode());

          verify(mockTracker).startTrackingNewConnection(eq(testUri), eq(GET), eq(false), eq(NetworkProtocol.IPv4));
      }
  }

  @Test
  public void testMakeRequest_ExpiredCertificate() throws Exception {
      try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
           MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class, CALLS_REAL_METHODS);
           MockedStatic<NetworkInfo> networkInfoMock = mockStatic(NetworkInfo.class);
           MockedStatic<ConnectionTracker> trackerMock = mockStatic(ConnectionTracker.class)) {

          dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
          InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
          dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);
          // NetworkInfo mocking removed - tests use defaults

          ConnectionTracker mockTracker = mock(ConnectionTracker.class);
          when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id");
          trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

          // Create an SSL exception that simulates an expired certificate
          Exception expiredException = new javax.net.ssl.SSLHandshakeException(
              "PKIX path validation failed: java.security.cert.CertificateExpiredException: NotAfter: past date"
          );
          expiredException.initCause(new java.security.cert.CertificateExpiredException());

          // Mock the executeRequest to throw the SSL exception
          httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenThrow(expiredException);

          URI testUri = new URI("http://example.com/path");
          HttpResponse<String> response = RDAPHttpRequest.makeRequest(createTestQueryContext(), testUri, 30, GET);

          assertThat(response).isNotNull();
          assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
              .isEqualTo(ConnectionStatus.EXPIRED_CERTIFICATE);

          verify(mockTracker).startTrackingNewConnection(eq(testUri), eq(GET), eq(false), eq(NetworkProtocol.IPv4));
      }
  }

  @Test
  public void testMakeRequest_SSLHandshakeException() throws Exception {
      try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
           MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class, CALLS_REAL_METHODS);
           MockedStatic<NetworkInfo> networkInfoMock = mockStatic(NetworkInfo.class);
           MockedStatic<ConnectionTracker> trackerMock = mockStatic(ConnectionTracker.class)) {

          dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
          InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
          dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);
          // NetworkInfo mocking removed - tests use defaults

          ConnectionTracker mockTracker = mock(ConnectionTracker.class);
          trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

          // Create SSL handshake exception
          javax.net.ssl.SSLHandshakeException sslEx = new javax.net.ssl.SSLHandshakeException("SSL handshake failed");
          IOException ioEx = new IOException("Connection failed", sslEx);

          // Mock executeRequest to throw the exception
          httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenThrow(ioEx);

          URI testUri = new URI("http://example.com/path");
          HttpResponse<String> response = RDAPHttpRequest.makeRequest(createTestQueryContext(), testUri, 30, GET);

          assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
              .isEqualTo(ConnectionStatus.HANDSHAKE_FAILED);

          verify(mockTracker).startTrackingNewConnection(eq(testUri), eq(GET), eq(false), eq(NetworkProtocol.IPv4));
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
            ClassicHttpResponse retryResponse = mock(ClassicHttpResponse.class);
            when(retryResponse.getCode()).thenReturn(HTTP_TOO_MANY_REQUESTS);
            when(retryResponse.getEntity()).thenReturn(null);
            Header retryAfterHeader = new TestHeader(RETRY_AFTER, "1");
            when(retryResponse.getFirstHeader(RETRY_AFTER)).thenReturn(retryAfterHeader);
            when(retryResponse.getHeaders()).thenReturn(new Header[] { retryAfterHeader });

            // Mock for 200
            ClassicHttpResponse successResponse = mock(ClassicHttpResponse.class);
            when(successResponse.getCode()).thenReturn(HTTP_OK);
            when(successResponse.getEntity()).thenReturn(null);
            when(successResponse.getHeaders()).thenReturn(new Header[ZERO]);

            // Simulate one  429,, then a 200
            final int[] callCount = {ZERO};
            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenAnswer(invocation -> {
                if (callCount[ZERO] < 1) {
                    callCount[ZERO]++;
                    return retryResponse;
                } else {
                    return successResponse;
                }
            });

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(createTestQueryContext(), testUri, timeout, GET);
            assertThat(response.statusCode()).isEqualTo(HTTP_OK);
            assertThat(response.body()).isEqualTo("");
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.SUCCESS);
            assertThat(callCount[ZERO]).isEqualTo(1); // Two retries before success
        }
    }

    @Test
    public void testMakeRequest_HttpTooManyRequests_ExceedsMaxRetries() throws Exception {
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class, Mockito.CALLS_REAL_METHODS)) {

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

            // Mock for 429
            ClassicHttpResponse retryResponse = mock(ClassicHttpResponse.class);
            when(retryResponse.getCode()).thenReturn(HTTP_TOO_MANY_REQUESTS);
            when(retryResponse.getEntity()).thenReturn(null);
            Header retryAfterHeader = new TestHeader(RETRY_AFTER, "1");
            when(retryResponse.getFirstHeader(RETRY_AFTER)).thenReturn(retryAfterHeader);
            when(retryResponse.getHeaders()).thenReturn(new Header[] { retryAfterHeader });

            // No matter what, return a 429
            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenReturn(retryResponse);

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(createTestQueryContext(), testUri, timeout, GET);
            assertThat(response.statusCode()).isEqualTo(HTTP_TOO_MANY_REQUESTS);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.TOO_MANY_REQUESTS);
        }
    }

    @Test
    public void testMakeRequest_NullUri() {
        try {
            RDAPHttpRequest.makeRequest(createTestQueryContext(), null, timeout, GET);
            // Should not reach here
            Assert.fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("The provided URI is null.");
        } catch (Exception e) {
            Assert.fail("Expected IllegalArgumentException but got " + e.getClass().getName());
        }
    }

    @Test
    public void testMakeRequest_LocalhostResolution() throws Exception {
        URI localhostUri = URI.create("http://localhost/path");

        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class);
            MockedStatic<ConnectionTracker> trackerMock = Mockito.mockStatic(ConnectionTracker.class)) {

            ConnectionTracker mockTracker = mock(ConnectionTracker.class);
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

            // Setup DNS resolution for 127.0.0.1 instead of localhost
            InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(LOCAL_IPv4)).thenReturn(false);
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(LOCAL_IPv4)).thenReturn(mockAddress);

            // Allow the real makeRequest to handle the localhost to 127.0.0.1 conversion
            // but mock the rest of the execution
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                           .thenReturn(mock(ClassicHttpResponse.class));

            // Verify the call correctly resolves localhost to 127.0.0.1
            RDAPHttpRequest.makeRequest(createTestQueryContext(), localhostUri, timeout, GET, true, true);

            // Verify that getFirstV4Address was called with 127.0.0.1 and not localhost
            dnsResolverMock.verify(() -> DNSCacheResolver.getFirstV4Address(eq(LOCAL_IPv4)), times(1));
        }
    }

    @Test
    public void testMakeRequest_NoAddressesForHost() throws Exception {
        URI noAddressUri = URI.create("http://nonexistent.example.com/path");

        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class);
            MockedStatic<ConnectionTracker> trackerMock = Mockito.mockStatic(ConnectionTracker.class)) {

            ConnectionTracker mockTracker = mock(ConnectionTracker.class);
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

            // Set up DNS resolver to return no addresses for the host
            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses("nonexistent.example.com")).thenReturn(true);

            // Only allow real method implementation for makeRequest variants
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();

            RDAPHttpRequest.SimpleHttpResponse mockResponse = new RDAPHttpRequest.SimpleHttpResponse("test-id", ZERO, EMPTY_STRING, noAddressUri, new RDAPHttpRequest.Header[0]);
            mockResponse.setConnectionStatusCode(ConnectionStatus.UNKNOWN_HOST);

            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), noAddressUri, timeout, GET))
                           .thenReturn(mockResponse);

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(createTestQueryContext(), noAddressUri, timeout, GET);

            assertThat(response).isNotNull()
                                .isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(response.statusCode()).isEqualTo(ZERO);
            assertThat(response.body()).isEmpty();
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.UNKNOWN_HOST);

            verify(mockTracker).startTrackingNewConnection(eq(noAddressUri), eq(GET), eq(false), eq(NetworkProtocol.IPv4));

        }
    }


    @Test
    public void testMakeRequest_IPv6NetworkProtocol() throws Exception {
        URI validUri = URI.create("http://example.com/path");

        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class);
            MockedStatic<ConnectionTracker> trackerMock = Mockito.mockStatic(ConnectionTracker.class);
            MockedStatic<NetworkInfo> networkInfoMock = Mockito.mockStatic(NetworkInfo.class)) {

            ConnectionTracker mockTracker = mock(ConnectionTracker.class);
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

            // Set up for IPv6 protocol - make sure this is called before any DNS resolution
            // NetworkInfo mocking removed - tests use defaults

            // Set up DNS resolution mocks
            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses("example.com")).thenReturn(false);
            InetAddress ipv6Address = InetAddress.getByName("::1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV6Address("example.com")).thenReturn(ipv6Address);
            InetAddress localV6 = InetAddress.getByName("::1");
            httpRequestMock.when(RDAPHttpRequest::getDefaultIPv6Address).thenReturn(localV6);

            // Mock HTTP execution to return success
            ClassicHttpResponse mockResponse = mock(ClassicHttpResponse.class);
            when(mockResponse.getCode()).thenReturn(200);
            when(mockResponse.getHeaders()).thenReturn(new Header[0]);
            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                           .thenReturn(mockResponse);

            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(createTestQueryContext(), validUri, timeout, GET);

            // Verify the IPv6 resolution was called
            dnsResolverMock.verify(() -> DNSCacheResolver.getFirstV6Address("example.com"));
            dnsResolverMock.verify(() -> DNSCacheResolver.getFirstV4Address(anyString()), never());

            assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.SUCCESS);
        }
    }

   @Test
   public void testMakeRequest_HttpsScheme_DefaultPort() throws Exception {
       try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class);
            MockedStatic<NetworkInfo> networkInfoMock = mockStatic(NetworkInfo.class);
            MockedStatic<ConnectionTracker> trackerMock = mockStatic(ConnectionTracker.class)) {

           InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
           dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
           dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);
           // NetworkInfo mocking removed - tests use defaults

           ConnectionTracker mockTracker = mock(ConnectionTracker.class);
           trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

           // Mock IPv4 address
           httpRequestMock.when(RDAPHttpRequest::getDefaultIPv4Address).thenReturn(mockAddress);

           ClassicHttpResponse mockResponse = mock(ClassicHttpResponse.class);
           when(mockResponse.getCode()).thenReturn(200);

           // Important: Mock executeRequest before makeRequest
           httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenReturn(mockResponse);

           // Don't mock the actual makeRequest calls - let them execute
           httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(URI.class), anyInt(), anyString()))
               .thenCallRealMethod();
           httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(URI.class), anyInt(), anyString(), anyBoolean()))
               .thenCallRealMethod();
           httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(URI.class), anyInt(), anyString(), anyBoolean(), anyBoolean()))
               .thenCallRealMethod();

           URI testUri = new URI("https://example.com/path");
           HttpResponse<String> response = RDAPHttpRequest.makeRequest(createTestQueryContext(), testUri, 10, GET);

           httpRequestMock.verify(() -> RDAPHttpRequest.executeRequest(any(), any()));
           verify(mockTracker).startTrackingNewConnection(eq(testUri), eq(GET), eq(false), eq(NetworkProtocol.IPv4));
       }
   }

   // TODO: fix?
   @Ignore
@Test
public void testMakeRequest_HttpScheme_DefaultPort() throws Exception {
    try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
         MockedStatic<NetworkInfo> networkInfoMock = mockStatic(NetworkInfo.class)) {

        InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
        dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
        dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);
        // NetworkInfo mocking removed - tests use defaults

        ClassicHttpResponse mockResponse = mock(ClassicHttpResponse.class);
        when(mockResponse.getCode()).thenReturn(200);
        CloseableHttpClient mockClient = mock(CloseableHttpClient.class);
        when(mockClient.execute(any(HttpUriRequestBase.class))).thenReturn((CloseableHttpResponse) mockResponse);

        ConnectionTracker mockTracker = mock(ConnectionTracker.class);
        when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id");

        try (MockedStatic<ConnectionTracker> trackerMock = mockStatic(ConnectionTracker.class);
             MockedStatic<RDAPHttpRequest> requestMock = mockStatic(RDAPHttpRequest.class)) {

            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);
            requestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenReturn(mockResponse);

            // Create test URI and make request
            URI testUri = new URI("http://example.com/path");
            HttpResponse<String> response = RDAPHttpRequest.makeRequest(createTestQueryContext(), testUri, 10, GET);

            // Verify the interactions
            verify(mockTracker).startTrackingNewConnection(eq(testUri), eq(GET), eq(false), eq(NetworkProtocol.IPv4));
        }
    }
}

@Ignore //TODO: fix?
    @Test
    public void testMakeRequest_CustomPort() throws Exception {
        // Test with URI that specifies a custom port
        URI customPortUri = URI.create("http://example.com:8080/path");

        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class);
            MockedStatic<ConnectionTracker> trackerMock = Mockito.mockStatic(ConnectionTracker.class);
            MockedStatic<NetworkInfo> networkInfoMock = Mockito.mockStatic(NetworkInfo.class)) {

            ConnectionTracker mockTracker = mock(ConnectionTracker.class);
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);
            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses("example.com")).thenReturn(false);
            // NetworkInfo mocking removed - tests use defaults
            InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address("example.com")).thenReturn(mockAddress);
            ClassicHttpResponse mockResponse = mock(ClassicHttpResponse.class);
            when(mockResponse.getCode()).thenReturn(HTTP_OK);
            when(mockResponse.getHeaders()).thenReturn(new Header[ZERO]);

            ArgumentCaptor<HttpUriRequestBase> requestCaptor = ArgumentCaptor.forClass(HttpUriRequestBase.class);
            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), requestCaptor.capture()))
                           .thenReturn(mockResponse);

            // Real method implementations
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(createTestQueryContext(), customPortUri, timeout, GET);
            HttpUriRequestBase capturedRequest = requestCaptor.getValue();

            assertThat(capturedRequest.getUri().getPort()).isEqualTo(HTTP_HIGH_PORT);
            assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(response.statusCode()).isEqualTo(HTTP_OK);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.SUCCESS);
        }
    }

  @Test
  public void testHandleRequestException_UnknownHostExceptionFull() throws Exception {
      try (
          MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
          MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class, Mockito.CALLS_REAL_METHODS);
          MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class);
          MockedStatic<ConnectionTracker> trackerMock = mockStatic(ConnectionTracker.class);
          MockedStatic<NetworkInfo> networkInfoMock = mockStatic(NetworkInfo.class)
      ) {

          dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
          InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
          dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);
          // NetworkInfo mocking removed - tests use defaults

          ConnectionTracker mockTracker = mock(ConnectionTracker.class);
          when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id");
          trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

          UnknownHostException ex = new UnknownHostException("Unknown host");
          httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenThrow(ex);

          RDAPHttpRequest.SimpleHttpResponse mockResponse =
              new RDAPHttpRequest.SimpleHttpResponse("test-id", 0, "", testUri, new RDAPHttpRequest.Header[0]);
          mockResponse.setConnectionStatusCode(ConnectionStatus.UNKNOWN_HOST);

          httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(URI.class), anyInt(), anyString()))
                        .thenCallRealMethod();
          httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(URI.class), anyInt(), anyString(), anyBoolean()))
                        .thenCallRealMethod();
          httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(URI.class), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                        .thenCallRealMethod();
          httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any(), anyBoolean()))
                        .thenCallRealMethod();
          httpRequestMock.when(() -> RDAPHttpRequest.hasCause(any(), any()))
                        .thenCallRealMethod();

          HttpResponse<String> response = RDAPHttpRequest.makeRequest(createTestQueryContext(), testUri, timeout, GET);

          assertThat(response).isNotNull()
                            .isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
          assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
              .isEqualTo(ConnectionStatus.UNKNOWN_HOST);

          verify(mockTracker).startTrackingNewConnection(eq(testUri), eq(GET), eq(false), eq(NetworkProtocol.IPv4));
      }
  }

    @Test
    public void testHandleRequestException_ConnectExceptionFull() throws Exception {
        URI testUri = URI.create("http://example.com/path");
        int timeout = 10;

        try (
            MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class, Mockito.CALLS_REAL_METHODS);
            MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class);
            MockedStatic<ConnectionTracker> trackerMock = mockStatic(ConnectionTracker.class);
            MockedStatic<NetworkInfo> networkInfoMock = mockStatic(NetworkInfo.class)
        ) {

            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);
            // NetworkInfo mocking removed - tests use defaults

            ConnectionTracker mockTracker = mock(ConnectionTracker.class);
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id");

            // Throw ConnectException("Connection refused") on executeRequest
            ConnectException ex = new ConnectException("Connection refused");
            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenThrow(ex);

            // Call the real ones
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(URI.class), anyInt(), anyString())).thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(URI.class), anyInt(), anyString(), anyBoolean())).thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(URI.class), anyInt(), anyString(), anyBoolean(), anyBoolean())).thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any(), anyBoolean())).thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.hasCause(any(), any())).thenCallRealMethod();

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(createTestQueryContext(), testUri, timeout, GET);

            assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse) response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.CONNECTION_REFUSED);

            commonUtilsMock.verify(() ->
                CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13021), eq("no response available"), eq("Connection refused by host."))
            );
        }
    }

@Test
public void testHandleRequestException_SocketTimeoutReadTimeoutFull() throws Exception {
    try (
        MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
        MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class, Mockito.CALLS_REAL_METHODS);
        MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class);
        MockedStatic<ConnectionTracker> trackerMock = mockStatic(ConnectionTracker.class);
        MockedStatic<NetworkInfo> networkInfoMock = mockStatic(NetworkInfo.class)
    ) {

        dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
        InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
        dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

        // NetworkInfo mocking removed - tests use defaults

        ConnectionTracker mockTracker = mock(ConnectionTracker.class);
        trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);
        when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id");

        // Throw SocketTimeoutException("Read timed out") on executeRequest
        SocketTimeoutException ex = new SocketTimeoutException("Read timed out");
        httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenThrow(ex);

        httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(URI.class), anyInt(), anyString())).thenCallRealMethod();
        httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(URI.class), anyInt(), anyString(), anyBoolean())).thenCallRealMethod();
        httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(URI.class), anyInt(), anyString(), anyBoolean(), anyBoolean())).thenCallRealMethod();
        httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any(), anyBoolean())).thenCallRealMethod();
        httpRequestMock.when(() -> RDAPHttpRequest.hasCause(any(), any())).thenCallRealMethod();


        HttpResponse<String> response = RDAPHttpRequest.makeRequest(createTestQueryContext(), testUri, timeout, GET);


        assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
        assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
            .isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);

        commonUtilsMock.verify(() ->
            CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13017), eq("no response available"), eq("Network receive fail"))
        );
    }
}


@Test
public void testHandleRequestException_SocketTimeoutConnectTimeoutFull() throws Exception {
    try (
        MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
        MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class, Mockito.CALLS_REAL_METHODS);
        MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class);
        MockedStatic<ConnectionTracker> trackerMock = mockStatic(ConnectionTracker.class);
        MockedStatic<NetworkInfo> networkInfoMock = mockStatic(NetworkInfo.class)
    ) {
        dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
        InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
        dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);

        // NetworkInfo mocking removed - tests use defaults

        ConnectionTracker mockTracker = mock(ConnectionTracker.class);
        trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);
        when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id");

        // Throw SocketTimeoutException("Connect timed out") on executeRequest
        SocketTimeoutException ex = new SocketTimeoutException("Connect timed out");
        httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenThrow(ex);

        httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(URI.class), anyInt(), anyString())).thenCallRealMethod();
        httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(URI.class), anyInt(), anyString(), anyBoolean())).thenCallRealMethod();
        httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(createTestQueryContext(), any(URI.class), anyInt(), anyString(), anyBoolean(), anyBoolean())).thenCallRealMethod();
        httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any(), anyBoolean())).thenCallRealMethod();
        httpRequestMock.when(() -> RDAPHttpRequest.hasCause(any(), any())).thenCallRealMethod();

        HttpResponse<String> response = RDAPHttpRequest.makeRequest(createTestQueryContext(), testUri, timeout, GET);

        assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
        assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
            .isEqualTo(ConnectionStatus.NETWORK_SEND_FAIL);

        // Optionally verify error logging
        commonUtilsMock.verify(() ->
            CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13016), eq("no response available"), eq("Network send fail"))
        );
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

            ConnectException ex = new ConnectException("Connection refused");
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ex, true);

            assertThat(status).isEqualTo(ConnectionStatus.CONNECTION_REFUSED);

            commonUtilsMock.verify(() ->
                    CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13021), eq("no response available"),
                        eq("Connection refused by host.")),
                times(ONE));

            status = RDAPHttpRequest.handleRequestException(ex, false);

            assertThat(status).isEqualTo(ConnectionStatus.CONNECTION_REFUSED);
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

            java.net.http.HttpTimeoutException ex = new java.net.http.HttpTimeoutException("Request timed out");
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ex, true);

            assertThat(status).isEqualTo(ConnectionStatus.CONNECTION_FAILED);
            commonUtilsMock.verify(() ->
                    CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13007), eq("no response available"),
                        eq("Failed to connect to server.")),
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

            httpRequestMock.when(() ->
                               RDAPHttpRequest.hasCause(eq(ioEx), eq("java.security.cert.CertificateExpiredException")))
                           .thenReturn(true);
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ioEx, true);

            assertThat(status).isEqualTo(ConnectionStatus.EXPIRED_CERTIFICATE);

            commonUtilsMock.verify(() ->
                    CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13011), eq("no response available"),
                        eq("Expired certificate.")),
                times(ONE));

            // Now ... do it again, but this time don't record the error
            status = RDAPHttpRequest.handleRequestException(ioEx, false);

            // Verify!
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

            // Verify we get the REVOKED_CERTIFICATE status
            assertThat(status).isEqualTo(ConnectionStatus.REVOKED_CERTIFICATE);

            // Verify!
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

            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ioEx, true);
            assertThat(status).isEqualTo(ConnectionStatus.CERTIFICATE_ERROR);
            commonUtilsMock.verify(() ->
                    CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13012), eq("no response available"),
                        eq("TLS certificate error.")),
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

            // Create an exception for a General Certificate Error
            IOException ioEx = new IOException("Certificate error");
            ioEx.initCause(new java.security.cert.CertificateException("General certificate error"));

            // Mock hasCause to return true for CertificateException
            httpRequestMock.when(() ->
                               RDAPHttpRequest.hasCause(eq(ioEx), eq("java.security.cert.CertificateException")))
                           .thenReturn(true);

            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ioEx, true);
            assertThat(status).isEqualTo(ConnectionStatus.CERTIFICATE_ERROR);

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

            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ioEx, true);

            assertThat(status).isEqualTo(ConnectionStatus.HANDSHAKE_FAILED);
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

            httpRequestMock.when(() ->
                               RDAPHttpRequest.hasCause(eq(ioEx), eq("sun.security.validator.ValidatorException")))
                           .thenReturn(true);

            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ioEx, true);

            assertThat(status).isEqualTo(ConnectionStatus.CERTIFICATE_ERROR);
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

            SocketTimeoutException ex = new SocketTimeoutException("Read timed out");
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ex, true);

            assertThat(status).isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);

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

            SocketTimeoutException ex = new SocketTimeoutException("Connect timed out");
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ex, true);

            assertThat(status).isEqualTo(ConnectionStatus.NETWORK_SEND_FAIL);

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

            EOFException ex = new EOFException("Unexpected end of file");
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ex, true);

            assertThat(status).isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);

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

            IOException ex = new IOException("Connection reset");
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ex, true);

            assertThat(status).isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);

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

            IOException ex = new IOException("Connection closed by peer");
            ConnectionStatus status = RDAPHttpRequest.handleRequestException(ex, true);

            assertThat(status).isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);

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

@Test
public void testSimpleHttpResponse() {
    URI testUri = URI.create("http://example.com");
    RDAPHttpRequest.Header[] headers = new RDAPHttpRequest.Header[] {
        new RDAPHttpRequest.Header("Content-Type", "application/json"),
        new RDAPHttpRequest.Header("Content-Length", "123")
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

@Test
public void testMakeRequest_HttpProtocolErrors() throws Exception {
    try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
         MockedStatic<RDAPHttpRequest> httpRequestMock = mockStatic(RDAPHttpRequest.class, CALLS_REAL_METHODS);
         MockedStatic<NetworkInfo> networkInfoMock = mockStatic(NetworkInfo.class);
         MockedStatic<ConnectionTracker> trackerMock = mockStatic(ConnectionTracker.class);
         MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class)) {

        dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(anyString())).thenReturn(false);
        InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
        dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(anyString())).thenReturn(mockAddress);
        // NetworkInfo mocking removed - tests use defaults

        ConnectionTracker mockTracker = mock(ConnectionTracker.class);
        when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id");
        trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

        // Test cases for each exception type
        List<Pair<IOException, String>> testCases = List.of(
            Pair.of(new NoHttpResponseException("The target server failed to respond"), "NoHttpResponseException"),
            Pair.of(new IOException(new ProtocolException("Protocol violation")), "ProtocolException"),
            Pair.of(new IOException(new MalformedChunkCodingException("Malformed chunk")), "MalformedChunkCodingException"),
            Pair.of(new IOException(new MessageConstraintException("Message constraint violation")), "MessageConstraintException"),
            Pair.of(new IOException(new TruncatedChunkException("Truncated chunk")), "TruncatedChunkException")
        );

        for (Pair<IOException, String> testCase : testCases) {
            // Reset mocks for each test case
            clearInvocations(mockTracker);
            commonUtilsMock.clearInvocations();

            // Mock throwing the specific exception
            IOException exception = testCase.getLeft();
            String exceptionName = testCase.getRight();

            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenThrow(exception);
            HttpResponse<String> response = RDAPHttpRequest.makeRequest(createTestQueryContext(), testUri, timeout, GET);

            assertThat(response).isNotNull()
                .isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .as("Testing " + exceptionName)
                .isEqualTo(ConnectionStatus.HTTP_ERROR);

            commonUtilsMock.verify(() ->
                CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13014), eq("no response available"), eq("HTTP error.")),
                times(1));

            verify(mockTracker).startTrackingNewConnection(eq(testUri), eq(GET), eq(false), eq(NetworkProtocol.IPv4));
        }
    }
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

    @Test
    public void testGetDefaultIPv4Address() throws Exception {
        InetAddress ipv4Address = RDAPHttpRequest.getDefaultIPv4Address();
        
        if (ipv4Address != null) {
            assertThat(ipv4Address).isInstanceOf(Inet4Address.class);
            assertThat(ipv4Address.getHostAddress()).isNotEmpty();
        }
    }

    @Test
    public void testGetDefaultIPv6Address() throws Exception {
        InetAddress ipv6Address = RDAPHttpRequest.getDefaultIPv6Address();
        
        if (ipv6Address != null) {
            assertThat(ipv6Address).isInstanceOf(Inet6Address.class);
            assertThat(ipv6Address.getHostAddress()).isNotEmpty();
        }
    }


    @Test
    public void testCreateLeafValidatingTrustManager() throws Exception {
        X509TrustManager trustManager = RDAPHttpRequest.createLeafValidatingTrustManager();
        
        assertThat(trustManager).isNotNull();
        
        // Test with null chain - should throw exception
        org.testng.Assert.assertThrows(CertificateException.class, () -> {
            trustManager.checkServerTrusted(null, "RSA");
        });
        
        // Test with empty chain - should throw exception
        org.testng.Assert.assertThrows(CertificateException.class, () -> {
            trustManager.checkServerTrusted(new X509Certificate[0], "RSA");
        });
    }

    @Test
    public void testCreateLeafValidatingTrustManager_ExpiredCertificate() throws Exception {
        X509TrustManager trustManager = RDAPHttpRequest.createLeafValidatingTrustManager();
        
        // Mock an expired certificate
        X509Certificate expiredCert = mock(X509Certificate.class);
        when(expiredCert.getSubjectX500Principal()).thenReturn(new javax.security.auth.x500.X500Principal("CN=example.com"));
        when(expiredCert.getIssuerX500Principal()).thenReturn(new javax.security.auth.x500.X500Principal("CN=Test CA"));
        when(expiredCert.getNotBefore()).thenReturn(new Date(System.currentTimeMillis() - 86400000)); // 1 day ago
        when(expiredCert.getNotAfter()).thenReturn(new Date(System.currentTimeMillis() - 3600000)); // 1 hour ago (expired)
        
        // Mock the checkValidity to throw CertificateExpiredException
        doThrow(new java.security.cert.CertificateExpiredException("Certificate expired"))
                .when(expiredCert).checkValidity(any(Date.class));
        
        X509Certificate[] chain = {expiredCert};
        
        org.testng.Assert.assertThrows(CertificateException.class, () -> {
            trustManager.checkServerTrusted(chain, "RSA");
        });
    }

    @Test
    public void testCreateLeafValidatingTrustManager_ClientTrusted() throws Exception {
        X509TrustManager trustManager = RDAPHttpRequest.createLeafValidatingTrustManager();
        
        // Client certificate checking should not throw exceptions (it's a no-op)
        trustManager.checkClientTrusted(new X509Certificate[0], "RSA");
        trustManager.checkClientTrusted(null, "RSA");
    }

    @Test
    public void testCreateLeafValidatingTrustManager_GetAcceptedIssuers() throws Exception {
        X509TrustManager trustManager = RDAPHttpRequest.createLeafValidatingTrustManager();
        
        X509Certificate[] acceptedIssuers = trustManager.getAcceptedIssuers();
        
        // Should return the default trust manager's accepted issuers
        assertThat(acceptedIssuers).isNotNull();
    }

    @Test
    public void testSimpleHttpResponse_DuplicateSetCookieHeaders() {
        URI testUri = URI.create("http://example.com");
        
        // Create headers with both "set-cookie" and "Set-Cookie" (case difference would cause duplicate key issue)
        RDAPHttpRequest.Header[] headers = new RDAPHttpRequest.Header[] {
            new RDAPHttpRequest.Header("Content-Type", "application/json"),
            new RDAPHttpRequest.Header("set-cookie", "sessionid=abc123; path=/; HttpOnly"),
            new RDAPHttpRequest.Header("Set-Cookie", "__cf_bm=xyz789; path=/; expires=Mon, 04-Aug-25 21:08:35 GMT; domain=.example.com; HttpOnly; Secure; SameSite=None")
        };

        RDAPHttpRequest.SimpleHttpResponse response =
            new RDAPHttpRequest.SimpleHttpResponse("track-123", 406, "", testUri, headers);

        // After the fix, this should work without throwing an exception
        HttpHeaders httpHeaders = response.headers();
        
        // Verify we can access the headers
        assertThat(httpHeaders).isNotNull();
        assertThat(httpHeaders.firstValue("Content-Type")).isPresent();
        assertThat(httpHeaders.firstValue("Content-Type").get()).isEqualTo("application/json");
        
        // Both Set-Cookie header values should be preserved and accessible
        assertThat(httpHeaders.allValues("set-cookie")).hasSize(2);
        assertThat(httpHeaders.allValues("Set-Cookie")).hasSize(2); // Case-insensitive access
        assertThat(httpHeaders.allValues("set-cookie")).contains("sessionid=abc123; path=/; HttpOnly");
        assertThat(httpHeaders.allValues("set-cookie")).contains("__cf_bm=xyz789; path=/; expires=Mon, 04-Aug-25 21:08:35 GMT; domain=.example.com; HttpOnly; Secure; SameSite=None");
    }

    @Test
    public void testSimpleHttpResponse_MultipleDuplicateHeaders() {
        URI testUri = URI.create("http://example.com");
        
        // Test with multiple headers having various case combinations
        RDAPHttpRequest.Header[] headers = new RDAPHttpRequest.Header[] {
            new RDAPHttpRequest.Header("set-cookie", "cookie1=value1"),
            new RDAPHttpRequest.Header("Set-Cookie", "cookie2=value2"),
            new RDAPHttpRequest.Header("SET-COOKIE", "cookie3=value3"),
            new RDAPHttpRequest.Header("Content-Type", "application/json"),
            new RDAPHttpRequest.Header("content-type", "text/html"), // This should merge with Content-Type
            new RDAPHttpRequest.Header("CONTENT-TYPE", "application/xml")
        };

        RDAPHttpRequest.SimpleHttpResponse response =
            new RDAPHttpRequest.SimpleHttpResponse("track-123", 200, "", testUri, headers);

        HttpHeaders httpHeaders = response.headers();
        
        // Verify all set-cookie values are preserved
        assertThat(httpHeaders.allValues("set-cookie")).hasSize(3);
        assertThat(httpHeaders.allValues("set-cookie")).contains("cookie1=value1", "cookie2=value2", "cookie3=value3");
        
        // Verify all content-type values are preserved  
        assertThat(httpHeaders.allValues("content-type")).hasSize(3);
        assertThat(httpHeaders.allValues("content-type")).contains("application/json", "text/html", "application/xml");
        
        // Verify case-insensitive access works
        assertThat(httpHeaders.allValues("SET-COOKIE")).hasSize(3);
        assertThat(httpHeaders.allValues("Content-Type")).hasSize(3);
    }

    @Test
    public void testMakeHttpGetRequestWithRedirects_SingleRedirect() throws Exception {
        WireMockServer wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
             MockedStatic<ConnectionTracker> trackerMock = mockStatic(ConnectionTracker.class)) {
            
            // Mock DNS resolution
            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses("127.0.0.1")).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address("127.0.0.1")).thenReturn(mockAddress);
            
            // Mock connection tracker
            ConnectionTracker mockTracker = mock(ConnectionTracker.class);
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id-1", "test-id-2");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);
            
            // Setup WireMock stubs
            String originalPath = "/domain/original";
            String redirectPath = "/domain/final";
            
            // Original request returns 301 redirect
            wireMock.stubFor(get(urlEqualTo(originalPath))
                .willReturn(aResponse()
                    .withStatus(301)
                    .withHeader("Location", "http://127.0.0.1:" + wireMock.port() + redirectPath)));
            
            // Final request returns 200 OK
            wireMock.stubFor(get(urlEqualTo(redirectPath))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/rdap+json")
                    .withBody("{\"objectClassName\": \"domain\"}"))); 
            
            URI originalUri = URI.create("http://127.0.0.1:" + wireMock.port() + originalPath);
            
            HttpResponse<String> response = RDAPHttpRequest.makeHttpGetRequestWithRedirects(originalUri, 10, 3);
            
            // Should get final response
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("objectClassName");
            
            // Verify both connections were tracked
            verify(mockTracker, times(2)).startTrackingNewConnection(any(), eq(GET), anyBoolean(), any(NetworkProtocol.class));
        } finally {
            wireMock.stop();
        }
    }
    
    @Test
    public void testMakeHttpGetRequestWithRedirects_CrossHostRedirectRejected() throws Exception {
        WireMockServer wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
             MockedStatic<ConnectionTracker> trackerMock = mockStatic(ConnectionTracker.class)) {
            
            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses("127.0.0.1")).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address("127.0.0.1")).thenReturn(mockAddress);
            
            ConnectionTracker mockTracker = mock(ConnectionTracker.class);
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);
            
            // Setup redirect to different host (should be rejected)
            wireMock.stubFor(get(urlEqualTo("/domain/test"))
                .willReturn(aResponse()
                    .withStatus(301)
                    .withHeader("Location", "http://evil.example.com/domain/test")));
            
            URI originalUri = URI.create("http://127.0.0.1:" + wireMock.port() + "/domain/test");
            
            HttpResponse<String> response = RDAPHttpRequest.makeHttpGetRequestWithRedirects(originalUri, 10, 3);
            
            // Should return the redirect response (301) since cross-host redirect is rejected
            assertThat(response.statusCode()).isEqualTo(301);
            
            // Only one connection should be tracked (original request)
            verify(mockTracker, times(1)).startTrackingNewConnection(any(), eq(GET), anyBoolean(), any(NetworkProtocol.class));
        } finally {
            wireMock.stop();
        }
    }
    
    @Test
    public void testMakeHttpGetRequestWithRedirects_NoLocationHeader() throws Exception {
        WireMockServer wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
             MockedStatic<ConnectionTracker> trackerMock = mockStatic(ConnectionTracker.class)) {
            
            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses("127.0.0.1")).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address("127.0.0.1")).thenReturn(mockAddress);
            
            ConnectionTracker mockTracker = mock(ConnectionTracker.class);
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);
            
            // Setup 301 response without Location header
            wireMock.stubFor(get(urlEqualTo("/domain/test"))
                .willReturn(aResponse()
                    .withStatus(301)
                    .withBody("Moved Permanently")));
            
            URI originalUri = URI.create("http://127.0.0.1:" + wireMock.port() + "/domain/test");
            
            HttpResponse<String> response = RDAPHttpRequest.makeHttpGetRequestWithRedirects(originalUri, 10, 3);
            
            // Should return the 301 response since there's no Location header to follow
            assertThat(response.statusCode()).isEqualTo(301);
            assertThat(response.body()).isEqualTo("Moved Permanently");
            
            verify(mockTracker, times(1)).startTrackingNewConnection(any(), eq(GET), anyBoolean(), any(NetworkProtocol.class));
        } finally {
            wireMock.stop();
        }
    }
    
    @Test
    public void testMakeHttpGetRequestWithRedirects_MaxRedirectsExceeded() throws Exception {
        WireMockServer wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
             MockedStatic<ConnectionTracker> trackerMock = mockStatic(ConnectionTracker.class)) {
            
            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses("127.0.0.1")).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address("127.0.0.1")).thenReturn(mockAddress);
            
            ConnectionTracker mockTracker = mock(ConnectionTracker.class);
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id-1", "test-id-2");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);
            
            // Setup infinite redirect loop
            wireMock.stubFor(get(urlEqualTo("/domain/test1"))
                .willReturn(aResponse()
                    .withStatus(302)
                    .withHeader("Location", "http://127.0.0.1:" + wireMock.port() + "/domain/test2")));
            
            wireMock.stubFor(get(urlEqualTo("/domain/test2"))
                .willReturn(aResponse()
                    .withStatus(302)
                    .withHeader("Location", "http://127.0.0.1:" + wireMock.port() + "/domain/test1")));
            
            URI originalUri = URI.create("http://127.0.0.1:" + wireMock.port() + "/domain/test1");
            
            // Set max redirects to 1
            HttpResponse<String> response = RDAPHttpRequest.makeHttpGetRequestWithRedirects(originalUri, 10, 1);
            
            // Should return the last redirect response (302) when max redirects exceeded
            assertThat(response.statusCode()).isEqualTo(302);
            
            // Should have made exactly maxRedirects + 1 requests (original + 1 redirect)
            verify(mockTracker, times(2)).startTrackingNewConnection(any(), eq(GET), anyBoolean(), any(NetworkProtocol.class));
        } finally {
            wireMock.stop();
        }
    }
    
    @Test
    public void testMakeRequestWithRedirects_AllowedRedirectCodes() throws Exception {
        WireMockServer wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
             MockedStatic<ConnectionTracker> trackerMock = mockStatic(ConnectionTracker.class)) {
            
            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses("127.0.0.1")).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address("127.0.0.1")).thenReturn(mockAddress);
            
            ConnectionTracker mockTracker = mock(ConnectionTracker.class);
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id-1", "test-id-2");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);
            
            // Test each redirect status code
            int[] redirectCodes = {301, 302, 303, 307, 308};
            
            for (int code : redirectCodes) {
                String path = "/test" + code;
                String finalPath = "/final" + code;
                
                wireMock.stubFor(get(urlEqualTo(path))
                    .willReturn(aResponse()
                        .withStatus(code)
                        .withHeader("Location", "http://127.0.0.1:" + wireMock.port() + finalPath)));
                
                wireMock.stubFor(get(urlEqualTo(finalPath))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Success for " + code)));
                
                URI originalUri = URI.create("http://127.0.0.1:" + wireMock.port() + path);
                
                HttpResponse<String> response = RDAPHttpRequest.makeHttpGetRequestWithRedirects(originalUri, 10, 3);
                
                assertThat(response.statusCode()).as("Testing redirect code " + code).isEqualTo(200);
                assertThat(response.body()).as("Testing redirect code " + code).isEqualTo("Success for " + code);
            }
        } finally {
            wireMock.stop();
        }
    }
    
    @Test
    public void testMakeRequestWithRedirects_NonRedirectStatus() throws Exception {
        WireMockServer wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class);
             MockedStatic<ConnectionTracker> trackerMock = mockStatic(ConnectionTracker.class)) {
            
            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses("127.0.0.1")).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address("127.0.0.1")).thenReturn(mockAddress);
            
            ConnectionTracker mockTracker = mock(ConnectionTracker.class);
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean(), any(NetworkProtocol.class))).thenReturn("test-id");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);
            
            // Setup 200 response (no redirect)
            wireMock.stubFor(get(urlEqualTo("/domain/test"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("Success")));
            
            URI originalUri = URI.create("http://127.0.0.1:" + wireMock.port() + "/domain/test");
            
            HttpResponse<String> response = RDAPHttpRequest.makeHttpGetRequestWithRedirects(originalUri, 10, 3);
            
            // Should return the original 200 response
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).isEqualTo("Success");
            
            // Only one connection should be tracked
            verify(mockTracker, times(1)).startTrackingNewConnection(any(), eq(GET), anyBoolean(), any(NetworkProtocol.class));
        } finally {
            wireMock.stop();
        }
    }
    
    @Test
    public void testLinkRedirectConnections() throws Exception {
        // Test that redirects are properly linked in the connection tracker
        // We'll test this by verifying the actual connection records are linked
        
        WireMockServer wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class)) {
            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses("127.0.0.1")).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address("127.0.0.1")).thenReturn(mockAddress);
            
            // Clear connection tracker
            ConnectionTracker tracker = ConnectionTracker.getInstance();
            tracker.reset();
            
            // Setup redirect
            wireMock.stubFor(get(urlEqualTo("/original"))
                .willReturn(aResponse()
                    .withStatus(301)
                    .withHeader("Location", "http://127.0.0.1:" + wireMock.port() + "/final")));
            
            wireMock.stubFor(get(urlEqualTo("/final"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("Success")));
            
            URI originalUri = URI.create("http://127.0.0.1:" + wireMock.port() + "/original");
            
            HttpResponse<String> response = RDAPHttpRequest.makeHttpGetRequestWithRedirects(originalUri, 10, 3);
            
            // Verify the response is successful
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).isEqualTo("Success");
            
            // Verify connections are linked properly
            List<ConnectionTracker.ConnectionRecord> connections = tracker.getConnections();
            assertThat(connections).hasSize(2); // Original + follow-up
            
            ConnectionTracker.ConnectionRecord redirectRecord = connections.get(0);
            ConnectionTracker.ConnectionRecord followRecord = connections.get(1);
            
            // The redirect record should have redirectedToId pointing to follow record
            assertThat(redirectRecord.getRedirectedToId()).isEqualTo(followRecord.getTrackingId());
            
            // The follow record should have parentTrackingId pointing to redirect record
            assertThat(followRecord.getParentTrackingId()).isEqualTo(redirectRecord.getTrackingId());
            assertThat(followRecord.isRedirectFollow()).isTrue();
        } finally {
            wireMock.stop();
        }
    }
    
    @Test
    public void testRedirectConnectionTracking_TreeDisplayFormat() throws Exception {
        // Test that redirect connections are displayed with proper tree-style formatting
        WireMockServer wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        
        try (MockedStatic<DNSCacheResolver> dnsResolverMock = mockStatic(DNSCacheResolver.class)) {
            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses("127.0.0.1")).thenReturn(false);
            InetAddress mockAddress = InetAddress.getByName("127.0.0.1");
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address("127.0.0.1")).thenReturn(mockAddress);
            
            ConnectionTracker tracker = ConnectionTracker.getInstance();
            tracker.reset();
            
            // Setup redirect chain: original -> intermediate -> final
            wireMock.stubFor(get(urlEqualTo("/chain/original"))
                .willReturn(aResponse()
                    .withStatus(301)
                    .withHeader("Location", "http://127.0.0.1:" + wireMock.port() + "/chain/intermediate")));
            
            wireMock.stubFor(get(urlEqualTo("/chain/intermediate"))
                .willReturn(aResponse()
                    .withStatus(302)
                    .withHeader("Location", "http://127.0.0.1:" + wireMock.port() + "/chain/final")));
            
            wireMock.stubFor(get(urlEqualTo("/chain/final"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("Final result")));
            
            URI originalUri = URI.create("http://127.0.0.1:" + wireMock.port() + "/chain/original");
            
            HttpResponse<String> response = RDAPHttpRequest.makeHttpGetRequestWithRedirects(originalUri, 10, 3);
            
            // Verify final response
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).isEqualTo("Final result");
            
            // Get all connections and verify tree structure
            List<ConnectionTracker.ConnectionRecord> connections = tracker.getConnections();
            assertThat(connections).hasSize(3); // original -> intermediate -> final
            
            // Verify the redirect chain linking
            ConnectionTracker.ConnectionRecord originalRecord = connections.get(0);
            ConnectionTracker.ConnectionRecord intermediateRecord = connections.get(1);
            ConnectionTracker.ConnectionRecord finalRecord = connections.get(2);
            
            // Original -> Intermediate
            assertThat(originalRecord.getRedirectedToId()).isEqualTo(intermediateRecord.getTrackingId());
            assertThat(intermediateRecord.getParentTrackingId()).isEqualTo(originalRecord.getTrackingId());
            assertThat(intermediateRecord.isRedirectFollow()).isTrue();
            
            // Intermediate -> Final
            assertThat(intermediateRecord.getRedirectedToId()).isEqualTo(finalRecord.getTrackingId());
            assertThat(finalRecord.getParentTrackingId()).isEqualTo(intermediateRecord.getTrackingId());
            assertThat(finalRecord.isRedirectFollow()).isTrue();
            
            // Test toString() formatting shows redirect relationships
            String trackerOutput = tracker.toString();
            
            // Should contain redirect indicators in the output
            assertThat(trackerOutput).contains("[REDIRECTED]");
            assertThat(trackerOutput).contains("[REDIRECT_FOLLOW]");
            
            // Should contain tree-style arrows for redirect relationships
            assertThat(trackerOutput).contains("└─►");
            
            // Verify connection tracking report mentions all URLs in the chain
            assertThat(trackerOutput).contains("/chain/original");
            assertThat(trackerOutput).contains("/chain/intermediate");  
            assertThat(trackerOutput).contains("/chain/final");
        } finally {
            wireMock.stop();
        }
    }
}