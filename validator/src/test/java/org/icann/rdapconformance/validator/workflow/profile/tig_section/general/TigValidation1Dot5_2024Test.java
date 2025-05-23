package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.mockito.Mockito.*;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpResponse;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.mockito.MockedStatic;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TigValidation1Dot5_2024Test {

    private HttpResponse<String> httpResponse;
    private RDAPValidatorConfiguration config;
    private RDAPValidatorResults results;
    private TigValidation1Dot5_2024 validation;

    @BeforeMethod
    public void setUp() throws IOException {
        httpResponse = mock(HttpResponse.class);
        config = mock(RDAPValidatorConfiguration.class);
        results = mock(RDAPValidatorResults.class);

        when(config.getUri()).thenReturn(URI.create("https://example.com"));

        validation = new TigValidation1Dot5_2024(httpResponse, config, results);
    }

    @Test
    public void testDoValidate_ValidTLSProtocols() throws Exception {
        SSLContext sslContext = mock(SSLContext.class);
        SSLSocketFactory sslSocketFactory = mock(SSLSocketFactory.class);
        SSLSocket sslSocket = mock(SSLSocket.class);
        SSLSession sslSession = mock(SSLSession.class);

        when(sslContext.getSocketFactory()).thenReturn(sslSocketFactory);

        // Mock both createSocket() overloads
        when(sslSocketFactory.createSocket(any(Socket.class), anyString(), anyInt(), anyBoolean())).thenReturn(sslSocket);
        when(sslSocketFactory.createSocket(anyString(), anyInt())).thenReturn(sslSocket);

        when(sslSocket.getEnabledProtocols()).thenReturn(new String[]{"TLSv1.2", "TLSv1.3"});
        when(sslSocket.getSession()).thenReturn(sslSession);
        when(sslSession.getCipherSuite()).thenReturn("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
        when(sslSession.getProtocol()).thenReturn("TLSv1.2");

        try (MockedStatic<SSLContext> mockedStatic = mockStatic(SSLContext.class)) {
            mockedStatic.when(SSLContext::getDefault).thenReturn(sslContext);

            doNothing().when(sslSocket).startHandshake();
            when(httpResponse.uri()).thenReturn(URI.create("https://example.com"));

            boolean isValid = validation.doValidate();
            assertTrue("Validation should be successful for valid TLS protocols", isValid);
        }
    }

    @Test
    public void testDoValidate_ValidTLSProtocolButBadCipher() throws Exception {
        SSLContext sslContext = mock(SSLContext.class);
        SSLSocketFactory sslSocketFactory = mock(SSLSocketFactory.class);
        SSLSocket sslSocket = mock(SSLSocket.class);
        SSLSession sslSession = mock(SSLSession.class);

        when(sslContext.getSocketFactory()).thenReturn(sslSocketFactory);

        // Mock both createSocket() overloads
        when(sslSocketFactory.createSocket(any(Socket.class), anyString(), anyInt(), anyBoolean())).thenReturn(sslSocket);
        when(sslSocketFactory.createSocket(anyString(), anyInt())).thenReturn(sslSocket);

        when(sslSocket.getEnabledProtocols()).thenReturn(new String[]{"TLSv1.2", "TLSv1.3"});
        when(sslSocket.getSession()).thenReturn(sslSession);
        when(sslSession.getCipherSuite()).thenReturn("TLS_AES_128_GCM_SHA256");
        when(sslSession.getProtocol()).thenReturn("TLSv1.2");

        try (MockedStatic<SSLContext> mockedStatic = mockStatic(SSLContext.class)) {
            mockedStatic.when(SSLContext::getDefault).thenReturn(sslContext);

            doNothing().when(sslSocket).startHandshake();
            when(httpResponse.uri()).thenReturn(URI.create("https://example.com"));

            boolean isValid = validation.doValidate();
            assertFalse("Validation should not be successful for valid TLS protocol but a cipher", isValid);
        }
    }

    @Test
    public void testDoValidate_InvalidTLSProtocol() throws Exception {
        SSLContext sslContext = mock(SSLContext.class);
        SSLSocketFactory sslSocketFactory = mock(SSLSocketFactory.class);
        SSLSocket sslSocket = mock(SSLSocket.class);

        when(sslContext.getSocketFactory()).thenReturn(sslSocketFactory);
        when(sslSocketFactory.createSocket(any(Socket.class), anyString(), anyInt(), anyBoolean())).thenReturn(sslSocket);
        when(sslSocket.getEnabledProtocols()).thenReturn(new String[]{"TLSv1.1"});
        when(sslSocket.getSession()).thenReturn(mock(SSLSession.class));

        try (MockedStatic<SSLContext> mockedStatic = mockStatic(SSLContext.class)) {
            mockedStatic.when(SSLContext::getDefault).thenReturn(sslContext);

            doNothing().when(sslSocket).startHandshake();

            when(httpResponse.uri()).thenReturn(URI.create("https://example.com"));
            boolean isValid = validation.doValidate();

            assertFalse(isValid);
            verify(results).add(any());
        }
    }
}