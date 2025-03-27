package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.mockito.Mockito.*;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Arrays;
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

    public boolean doValidate() {
        try {
            SSLSocketFactory factory = (SSLSocketFactory) SSLContext.getDefault().getSocketFactory();
            SSLSocket socket = (SSLSocket) factory.createSocket(config.getUri().getHost(), 443);
            socket.startHandshake();
            String[] enabledProtocols = socket.getEnabledProtocols();
            String cipherSuite = socket.getSession().getCipherSuite();

            System.out.println("Enabled protocols: " + Arrays.toString(enabledProtocols));
            System.out.println("Cipher suite: " + cipherSuite);

            if (Arrays.asList(enabledProtocols).contains("TLSv1.2") || Arrays.asList(enabledProtocols).contains("TLSv1.3")) {
                return true;
            } else {
                results.add("Invalid TLS protocol");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            results.add("Exception during validation: " + e.getMessage());
            return false;
        }
    }

    @Test
    public void testDoValidate_InvalidTLSProtocol() throws Exception {
        // Mocking SSLContext and SSLSocket
        SSLContext sslContext = mock(SSLContext.class);
        SSLSocketFactory sslSocketFactory = mock(SSLSocketFactory.class);
        SSLSocket sslSocket = mock(SSLSocket.class);

        when(sslContext.getSocketFactory()).thenReturn(sslSocketFactory);
        when(sslSocketFactory.createSocket(anyString(), anyInt())).thenReturn(sslSocket);
        when(sslSocket.getEnabledProtocols()).thenReturn(new String[]{"TLSv1.1"});
        when(sslSocket.getSession()).thenReturn(mock(SSLSession.class));

        // Mocking the SSLContext.getDefault() method
        try (MockedStatic<SSLContext> mockedStatic = mockStatic(SSLContext.class)) {
            mockedStatic.when(SSLContext::getDefault).thenReturn(sslContext);

            // Mocking the SSL handshake
            doNothing().when(sslSocket).startHandshake();

            // Mocking the response URI
            when(httpResponse.uri()).thenReturn(URI.create("https://example.com"));

            // Running the validation
            boolean isValid = validation.doValidate();

            // Asserting the validation result
            assertFalse(isValid);
            verify(results).add(any());
        }
    }
}