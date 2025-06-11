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
import java.util.List;

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

import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icann.rdapconformance.validator.CommonUtils.EMPTY_STRING;
import static org.icann.rdapconformance.validator.CommonUtils.GET;
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
        URI unknownHostUri = URI.create("http://unknownhost.example/path");

        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<ConnectionTracker> trackerMock = Mockito.mockStatic(ConnectionTracker.class)) {

            ConnectionTracker mockTracker = mock(ConnectionTracker.class);
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

            // Simulate no addresses for the host
            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses("unknownhost.example")).thenReturn(true);

            // Call the real method (no need to mock executeRequest, as it should not be called)
            HttpResponse<String> response = RDAPHttpRequest.makeRequest(unknownHostUri, timeout, GET);

            assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(response.statusCode()).isEqualTo(ZERO);
            assertThat(response.body()).isEmpty();
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.UNKNOWN_HOST);

            verify(mockTracker).startTrackingNewConnection(eq(unknownHostUri), eq(GET), eq(false));
            verify(mockTracker).completeCurrentConnection(eq(ZERO), eq(ConnectionStatus.UNKNOWN_HOST));
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
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

            // Only mock executeRequest to throw IOException
            execMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                    .thenThrow(new IOException("Connection closed by peer"));

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(uri, timeout, GET);

            assertThat(response).isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);

            verify(mockTracker).startTrackingNewConnection(eq(uri), eq(GET), eq(false));
            verify(mockTracker).completeCurrentConnection(eq(0), eq(ConnectionStatus.NETWORK_RECEIVE_FAIL));
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


            networkInfoMock.when(NetworkInfo::getNetworkProtocol).thenReturn(NetworkProtocol.IPv4);

            ConnectionTracker mockTracker = mock(ConnectionTracker.class);
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                          .thenThrow(new ConnectException("Connection refused"));


            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, GET);

            assertThat(response).isNotNull()
                              .isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.CONNECTION_REFUSED);

            verify(mockTracker).startTrackingNewConnection(eq(testUri), eq(GET), eq(false));
            verify(mockTracker).completeCurrentConnection(eq(0), eq(ConnectionStatus.CONNECTION_REFUSED));
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

        networkInfoMock.when(NetworkInfo::getNetworkProtocol).thenReturn(NetworkProtocol.IPv4);

        ConnectionTracker mockTracker = mock(ConnectionTracker.class);
        when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");
        trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

        // Throw SocketTimeoutException on executeRequest
        httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                      .thenThrow(new SocketTimeoutException("Read timed out"));

        HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, GET);

        assertThat(response).isNotNull()
                          .isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
        assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
            .isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);

        verify(mockTracker).startTrackingNewConnection(eq(testUri), eq(GET), eq(false));
        verify(mockTracker).completeCurrentConnection(eq(0), eq(ConnectionStatus.NETWORK_RECEIVE_FAIL));
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

        networkInfoMock.when(NetworkInfo::getNetworkProtocol).thenReturn(NetworkProtocol.IPv4);

        ConnectionTracker mockTracker = mock(ConnectionTracker.class);
        when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");
        trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

        httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                      .thenThrow(new SocketTimeoutException("connect timed out"));


        HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, GET);

        assertThat(response).isNotNull()
                          .isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
        assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
            .isEqualTo(ConnectionStatus.NETWORK_SEND_FAIL);

        verify(mockTracker).startTrackingNewConnection(eq(testUri), eq(GET), eq(false));
        verify(mockTracker).completeCurrentConnection(eq(0), eq(ConnectionStatus.NETWORK_SEND_FAIL));
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

        networkInfoMock.when(NetworkInfo::getNetworkProtocol).thenReturn(NetworkProtocol.IPv4);

        ConnectionTracker mockTracker = mock(ConnectionTracker.class);
        when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");
        trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

        httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                      .thenThrow(new EOFException("EOF"));

        HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, GET);

        assertThat(response).isNotNull()
                          .isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
        assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
            .isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);

        verify(mockTracker).startTrackingNewConnection(eq(testUri), eq(GET), eq(false));
        verify(mockTracker).completeCurrentConnection(eq(0), eq(ConnectionStatus.NETWORK_RECEIVE_FAIL));
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
           networkInfoMock.when(NetworkInfo::getNetworkProtocol).thenReturn(NetworkProtocol.IPv4);

           ConnectionTracker mockTracker = mock(ConnectionTracker.class);
           when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");
           trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

           IOException resetException = new IOException("Connection reset by peer");
           httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenThrow(resetException);

           HttpResponse<String> response = RDAPHttpRequest.makeRequest(new URI("http://example.com/path"), 30, GET);

           assertThat(response).isNotNull()
                             .isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
           assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
               .isEqualTo(ConnectionStatus.NETWORK_RECEIVE_FAIL);

           verify(mockTracker).startTrackingNewConnection(any(), eq(GET), eq(false));
           verify(mockTracker).completeCurrentConnection(eq(0), eq(ConnectionStatus.NETWORK_RECEIVE_FAIL));
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

          networkInfoMock.when(NetworkInfo::getNetworkProtocol).thenReturn(NetworkProtocol.IPv4);
          ConnectionTracker mockTracker = mock(ConnectionTracker.class);
          when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");
          trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

          IOException malformedChunkException = new IOException("Malformed chunk");
          httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenThrow(malformedChunkException);

          URI testUri = new URI("http://example.com/path");
          HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, 30, GET);

          assertThat(response).isNotNull();
          assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode());

          verify(mockTracker).startTrackingNewConnection(eq(testUri), eq(GET), eq(false));
          verify(mockTracker).completeCurrentConnection(eq(0), eq(ConnectionStatus.CONNECTION_FAILED));
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
          networkInfoMock.when(NetworkInfo::getNetworkProtocol).thenReturn(NetworkProtocol.IPv4);

          ConnectionTracker mockTracker = mock(ConnectionTracker.class);
          when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");
          trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

          // Create an SSL exception that simulates an expired certificate
          Exception expiredException = new javax.net.ssl.SSLHandshakeException(
              "PKIX path validation failed: java.security.cert.CertificateExpiredException: NotAfter: past date"
          );
          expiredException.initCause(new java.security.cert.CertificateExpiredException());

          // Mock the executeRequest to throw the SSL exception
          httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenThrow(expiredException);

          URI testUri = new URI("http://example.com/path");
          HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, 30, GET);

          assertThat(response).isNotNull();
          assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
              .isEqualTo(ConnectionStatus.EXPIRED_CERTIFICATE);

          verify(mockTracker).startTrackingNewConnection(eq(testUri), eq(GET), eq(false));
          verify(mockTracker).completeCurrentConnection(eq(0), eq(ConnectionStatus.EXPIRED_CERTIFICATE));
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
          networkInfoMock.when(NetworkInfo::getNetworkProtocol).thenReturn(NetworkProtocol.IPv4);

          ConnectionTracker mockTracker = mock(ConnectionTracker.class);
          trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

          // Create SSL handshake exception
          javax.net.ssl.SSLHandshakeException sslEx = new javax.net.ssl.SSLHandshakeException("SSL handshake failed");
          IOException ioEx = new IOException("Connection failed", sslEx);

          // Mock executeRequest to throw the exception
          httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenThrow(ioEx);

          URI testUri = new URI("http://example.com/path");
          HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, 30, GET);

          assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
              .isEqualTo(ConnectionStatus.HANDSHAKE_FAILED);

          verify(mockTracker).startTrackingNewConnection(eq(testUri), eq(GET), eq(false));
          verify(mockTracker).completeCurrentConnection(eq(0), eq(ConnectionStatus.HANDSHAKE_FAILED));
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

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, GET);
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

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, GET);
            assertThat(response.statusCode()).isEqualTo(HTTP_TOO_MANY_REQUESTS);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.TOO_MANY_REQUESTS);
        }
    }

    @Test
    public void testMakeRequest_NullUri() {
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
    public void testMakeRequest_LocalhostResolution() throws Exception {
        URI localhostUri = URI.create("http://localhost/path");

        try (MockedStatic<DNSCacheResolver> dnsResolverMock = Mockito.mockStatic(DNSCacheResolver.class);
            MockedStatic<RDAPHttpRequest> httpRequestMock = Mockito.mockStatic(RDAPHttpRequest.class);
            MockedStatic<ConnectionTracker> trackerMock = Mockito.mockStatic(ConnectionTracker.class)) {

            ConnectionTracker mockTracker = mock(ConnectionTracker.class);
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

            // Setup DNS resolution for 127.0.0.1 instead of localhost
            InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses(LOCAL_IPv4)).thenReturn(false);
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address(LOCAL_IPv4)).thenReturn(mockAddress);

            // Allow the real makeRequest to handle the localhost to 127.0.0.1 conversion
            // but mock the rest of the execution
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any()))
                           .thenReturn(mock(ClassicHttpResponse.class));

            // Verify the call correctly resolves localhost to 127.0.0.1
            RDAPHttpRequest.makeRequest(localhostUri, timeout, GET, true, true);

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
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

            // Set up DNS resolver to return no addresses for the host
            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses("nonexistent.example.com")).thenReturn(true);

            // Only allow real method implementation for makeRequest variants
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();

            RDAPHttpRequest.SimpleHttpResponse mockResponse = new RDAPHttpRequest.SimpleHttpResponse("test-id", ZERO, EMPTY_STRING, noAddressUri, new RDAPHttpRequest.Header[0]);
            mockResponse.setConnectionStatusCode(ConnectionStatus.UNKNOWN_HOST);

            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(noAddressUri, timeout, GET))
                           .thenReturn(mockResponse);

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(noAddressUri, timeout, GET);

            assertThat(response).isNotNull()
                                .isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(response.statusCode()).isEqualTo(ZERO);
            assertThat(response.body()).isEmpty();
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .isEqualTo(ConnectionStatus.UNKNOWN_HOST);

            verify(mockTracker).startTrackingNewConnection(eq(noAddressUri), eq(GET), eq(false));
            verify(mockTracker).completeCurrentConnection(eq(ZERO), eq(ConnectionStatus.UNKNOWN_HOST));
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
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

            // Set up for IPv6 protocol - make sure this is called before any DNS resolution
            networkInfoMock.when(NetworkInfo::getNetworkProtocol).thenReturn(NetworkProtocol.IPv6);

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

            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(validUri, timeout, GET);

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
           networkInfoMock.when(NetworkInfo::getNetworkProtocol).thenReturn(NetworkProtocol.IPv4);

           ConnectionTracker mockTracker = mock(ConnectionTracker.class);
           trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

           // Mock IPv4 address
           httpRequestMock.when(RDAPHttpRequest::getDefaultIPv4Address).thenReturn(mockAddress);

           ClassicHttpResponse mockResponse = mock(ClassicHttpResponse.class);
           when(mockResponse.getCode()).thenReturn(200);

           // Important: Mock executeRequest before makeRequest
           httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenReturn(mockResponse);

           // Don't mock the actual makeRequest calls - let them execute
           httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString()))
               .thenCallRealMethod();
           httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean()))
               .thenCallRealMethod();
           httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean(), anyBoolean()))
               .thenCallRealMethod();

           URI testUri = new URI("https://example.com/path");
           HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, 10, GET);

           httpRequestMock.verify(() -> RDAPHttpRequest.executeRequest(any(), any()));
           verify(mockTracker).startTrackingNewConnection(eq(testUri), eq(GET), eq(false));
           verify(mockTracker).completeCurrentConnection(eq(200), eq(ConnectionStatus.SUCCESS));
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
        networkInfoMock.when(NetworkInfo::getNetworkProtocol).thenReturn(NetworkProtocol.IPv4);

        ClassicHttpResponse mockResponse = mock(ClassicHttpResponse.class);
        when(mockResponse.getCode()).thenReturn(200);
        CloseableHttpClient mockClient = mock(CloseableHttpClient.class);
        when(mockClient.execute(any(HttpUriRequestBase.class))).thenReturn((CloseableHttpResponse) mockResponse);

        ConnectionTracker mockTracker = mock(ConnectionTracker.class);
        when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");

        try (MockedStatic<ConnectionTracker> trackerMock = mockStatic(ConnectionTracker.class);
             MockedStatic<RDAPHttpRequest> requestMock = mockStatic(RDAPHttpRequest.class)) {

            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);
            requestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenReturn(mockResponse);

            // Create test URI and make request
            URI testUri = new URI("http://example.com/path");
            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, 10, GET);

            // Verify the interactions
            verify(mockTracker).startTrackingNewConnection(eq(testUri), eq(GET), eq(false));
            verify(mockTracker).completeCurrentConnection(eq(200), eq(ConnectionStatus.SUCCESS));
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
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);
            dnsResolverMock.when(() -> DNSCacheResolver.hasNoAddresses("example.com")).thenReturn(false);
            networkInfoMock.when(NetworkInfo::getNetworkProtocol).thenReturn(NetworkProtocol.IPv4);
            InetAddress mockAddress = InetAddress.getByName(LOCAL_IPv4);
            dnsResolverMock.when(() -> DNSCacheResolver.getFirstV4Address("example.com")).thenReturn(mockAddress);
            ClassicHttpResponse mockResponse = mock(ClassicHttpResponse.class);
            when(mockResponse.getCode()).thenReturn(HTTP_OK);
            when(mockResponse.getHeaders()).thenReturn(new Header[ZERO]);

            ArgumentCaptor<HttpUriRequestBase> requestCaptor = ArgumentCaptor.forClass(HttpUriRequestBase.class);
            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), requestCaptor.capture()))
                           .thenReturn(mockResponse);

            // Real method implementations
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(), anyInt(), anyString()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(), anyInt(), anyString(), anyBoolean()))
                           .thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(), anyInt(), anyString(), anyBoolean(), anyBoolean()))
                           .thenCallRealMethod();

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(customPortUri, timeout, GET);
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
          networkInfoMock.when(NetworkInfo::getNetworkProtocol).thenReturn(NetworkProtocol.IPv4);

          ConnectionTracker mockTracker = mock(ConnectionTracker.class);
          when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");
          trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);

          UnknownHostException ex = new UnknownHostException("Unknown host");
          httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenThrow(ex);

          RDAPHttpRequest.SimpleHttpResponse mockResponse =
              new RDAPHttpRequest.SimpleHttpResponse("test-id", 0, "", testUri, new RDAPHttpRequest.Header[0]);
          mockResponse.setConnectionStatusCode(ConnectionStatus.UNKNOWN_HOST);

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

          assertThat(response).isNotNull()
                            .isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
          assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
              .isEqualTo(ConnectionStatus.UNKNOWN_HOST);

          verify(mockTracker).startTrackingNewConnection(eq(testUri), eq(GET), eq(false));
          verify(mockTracker).completeCurrentConnection(eq(0), eq(ConnectionStatus.UNKNOWN_HOST));
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
            networkInfoMock.when(NetworkInfo::getNetworkProtocol).thenReturn(NetworkProtocol.IPv4);

            ConnectionTracker mockTracker = mock(ConnectionTracker.class);
            trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);
            when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");

            // Throw ConnectException("Connection refused") on executeRequest
            ConnectException ex = new ConnectException("Connection refused");
            httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenThrow(ex);

            // Call the real ones
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString())).thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean())).thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean(), anyBoolean())).thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any(), anyBoolean())).thenCallRealMethod();
            httpRequestMock.when(() -> RDAPHttpRequest.hasCause(any(), any())).thenCallRealMethod();

            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, GET);

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

        networkInfoMock.when(NetworkInfo::getNetworkProtocol).thenReturn(NetworkProtocol.IPv4);

        ConnectionTracker mockTracker = mock(ConnectionTracker.class);
        trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);
        when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");

        // Throw SocketTimeoutException("Read timed out") on executeRequest
        SocketTimeoutException ex = new SocketTimeoutException("Read timed out");
        httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenThrow(ex);

        httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString())).thenCallRealMethod();
        httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean())).thenCallRealMethod();
        httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean(), anyBoolean())).thenCallRealMethod();
        httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any(), anyBoolean())).thenCallRealMethod();
        httpRequestMock.when(() -> RDAPHttpRequest.hasCause(any(), any())).thenCallRealMethod();


        HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, GET);


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

        networkInfoMock.when(NetworkInfo::getNetworkProtocol).thenReturn(NetworkProtocol.IPv4);

        ConnectionTracker mockTracker = mock(ConnectionTracker.class);
        trackerMock.when(ConnectionTracker::getInstance).thenReturn(mockTracker);
        when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");

        // Throw SocketTimeoutException("Connect timed out") on executeRequest
        SocketTimeoutException ex = new SocketTimeoutException("Connect timed out");
        httpRequestMock.when(() -> RDAPHttpRequest.executeRequest(any(), any())).thenThrow(ex);

        httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString())).thenCallRealMethod();
        httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean())).thenCallRealMethod();
        httpRequestMock.when(() -> RDAPHttpRequest.makeRequest(any(URI.class), anyInt(), anyString(), anyBoolean(), anyBoolean())).thenCallRealMethod();
        httpRequestMock.when(() -> RDAPHttpRequest.handleRequestException(any(), anyBoolean())).thenCallRealMethod();
        httpRequestMock.when(() -> RDAPHttpRequest.hasCause(any(), any())).thenCallRealMethod();

        HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, GET);

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
        networkInfoMock.when(NetworkInfo::getNetworkProtocol).thenReturn(NetworkProtocol.IPv4);

        ConnectionTracker mockTracker = mock(ConnectionTracker.class);
        when(mockTracker.startTrackingNewConnection(any(), anyString(), anyBoolean())).thenReturn("test-id");
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
            HttpResponse<String> response = RDAPHttpRequest.makeRequest(testUri, timeout, GET);

            assertThat(response).isNotNull()
                .isInstanceOf(RDAPHttpRequest.SimpleHttpResponse.class);
            assertThat(((RDAPHttpRequest.SimpleHttpResponse)response).getConnectionStatusCode())
                .as("Testing " + exceptionName)
                .isEqualTo(ConnectionStatus.HTTP_ERROR);

            commonUtilsMock.verify(() ->
                CommonUtils.addErrorToResultsFile(eq(ZERO), eq(-13014), eq("no response available"), eq("HTTP error.")),
                times(1));

            verify(mockTracker).startTrackingNewConnection(eq(testUri), eq(GET), eq(false));
            verify(mockTracker).completeCurrentConnection(eq(0), eq(ConnectionStatus.HTTP_ERROR));
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
}