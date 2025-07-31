package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.mockito.Mockito.*;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TigValidation1Dot5_2024Test {

    private HttpResponse<String> httpResponse;
    private RDAPValidatorConfiguration config;
    private RDAPValidatorResults results;
    private SSLValidator mockSSLValidator;
    private TigValidation1Dot5_2024 validation;

    @BeforeMethod
    public void setUp() throws IOException {
        httpResponse = mock(HttpResponse.class);
        config = mock(RDAPValidatorConfiguration.class);
        results = mock(RDAPValidatorResults.class);
        mockSSLValidator = mock(SSLValidator.class);

        when(config.getUri()).thenReturn(URI.create("https://example.com"));

        // Set up default mock behavior for SSL cipher validation to prevent NPE
        SSLValidator.CipherValidationResult defaultCipherResult = 
            SSLValidator.CipherValidationResult.success("TLSv1.2", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
        when(mockSSLValidator.validateTLS12CipherSuites(anyString(), anyInt())).thenReturn(defaultCipherResult);

        // Use constructor that allows dependency injection
        validation = new TigValidation1Dot5_2024(httpResponse, config, results, mockSSLValidator);
    }

    @Test
    public void testDoValidate_NonHttpsUri() throws Exception {
        // Test with HTTP (non-HTTPS) URI - should return true without SSL validation
        when(httpResponse.uri()).thenReturn(URI.create("http://example.com"));

        boolean isValid = validation.doValidate();
        assertTrue("Validation should pass for non-HTTPS URI", isValid);
        
        // Verify no SSL validation was performed
        verify(mockSSLValidator, never()).validateSSL(anyString(), anyInt());
        verify(results, never()).add(any());
    }

    @Test
    public void testGetGroupName() {
        String groupName = validation.getGroupName();
        assertTrue("Group name should be tigSection_1_5_Validation", 
                   "tigSection_1_5_Validation".equals(groupName));
    }

    @Test
    public void testDoValidate_ValidTLSProtocols() throws Exception {
        // Test with valid TLS protocols - should return true
        when(httpResponse.uri()).thenReturn(URI.create("https://example.com"));
        
        List<String> validProtocols = Arrays.asList("TLSv1.2", "TLSv1.3");
        SSLValidator.SSLValidationResult successResult = SSLValidator.SSLValidationResult.success(validProtocols);
        when(mockSSLValidator.validateSSL("example.com", 443)).thenReturn(successResult);
        
        // Mock cipher validation for TLS 1.2
        SSLValidator.CipherValidationResult cipherResult = 
            SSLValidator.CipherValidationResult.success("TLSv1.2", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
        when(mockSSLValidator.validateTLS12CipherSuites("example.com", 443)).thenReturn(cipherResult);

        boolean isValid = validation.doValidate();
        assertTrue("Validation should pass for valid TLS protocols", isValid);
        
        verify(mockSSLValidator).validateSSL("example.com", 443);
        verify(results, never()).add(any()); // No errors should be added
    }

    @Test
    public void testDoValidate_InvalidTLSProtocol() throws Exception {
        // Test with invalid TLS protocol - should return false and add error
        when(httpResponse.uri()).thenReturn(URI.create("https://example.com"));
        
        List<String> invalidProtocols = Arrays.asList("TLSv1.1"); // Invalid protocol
        SSLValidator.SSLValidationResult successResult = SSLValidator.SSLValidationResult.success(invalidProtocols);
        when(mockSSLValidator.validateSSL("example.com", 443)).thenReturn(successResult);

        boolean isValid = validation.doValidate();
        assertFalse("Validation should fail for invalid TLS protocol", isValid);
        
        verify(mockSSLValidator).validateSSL("example.com", 443);
        verify(results).add(any()); // Error should be added for invalid protocol
    }

    @Test
    public void testDoValidate_SSLValidationFailure() throws Exception {
        // Test when SSL validation itself fails
        when(httpResponse.uri()).thenReturn(URI.create("https://example.com"));
        
        SSLValidator.SSLValidationResult failureResult = 
            SSLValidator.SSLValidationResult.failure("Connection failed", new IOException("Connection timeout"));
        when(mockSSLValidator.validateSSL("example.com", 443)).thenReturn(failureResult);

        boolean isValid = validation.doValidate();
        assertFalse("Validation should fail when SSL validation fails", isValid);
        
        verify(mockSSLValidator).validateSSL("example.com", 443);
        // No results should be added when SSL validation itself fails
        verify(results, never()).add(any());
    }

    @Test
    public void testDoValidate_MixedValidAndInvalidProtocols() throws Exception {
        // Test with mixed valid and invalid protocols
        when(httpResponse.uri()).thenReturn(URI.create("https://example.com"));
        
        List<String> mixedProtocols = Arrays.asList("TLSv1.1", "TLSv1.2", "TLSv1.3"); // One invalid, two valid
        SSLValidator.SSLValidationResult successResult = SSLValidator.SSLValidationResult.success(mixedProtocols);
        when(mockSSLValidator.validateSSL("example.com", 443)).thenReturn(successResult);
        
        // Mock cipher validation for TLS 1.2 (since TLS 1.2 is present)
        SSLValidator.CipherValidationResult cipherResult = 
            SSLValidator.CipherValidationResult.success("TLSv1.2", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
        when(mockSSLValidator.validateTLS12CipherSuites("example.com", 443)).thenReturn(cipherResult);

        boolean isValid = validation.doValidate();
        assertFalse("Validation should fail when any invalid protocol is present", isValid);
        
        verify(mockSSLValidator).validateSSL("example.com", 443);
        verify(results).add(any()); // Error should be added for the invalid protocol
    }
    
    @Test
    public void testDoValidate_ValidTLS12CipherSuite() throws Exception {
        // Test with TLS 1.2 and valid cipher suite
        when(httpResponse.uri()).thenReturn(URI.create("https://example.com"));
        
        List<String> protocols = Arrays.asList("TLSv1.2");
        SSLValidator.SSLValidationResult successResult = SSLValidator.SSLValidationResult.success(protocols);
        when(mockSSLValidator.validateSSL("example.com", 443)).thenReturn(successResult);
        
        SSLValidator.CipherValidationResult cipherResult = 
            SSLValidator.CipherValidationResult.success("TLSv1.2", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
        when(mockSSLValidator.validateTLS12CipherSuites("example.com", 443)).thenReturn(cipherResult);

        boolean isValid = validation.doValidate();
        assertTrue("Validation should pass for valid TLS 1.2 cipher suite", isValid);
        
        verify(mockSSLValidator).validateSSL("example.com", 443);
        verify(mockSSLValidator).validateTLS12CipherSuites("example.com", 443);
        verify(results, never()).add(any()); // No errors should be added
    }
    
    @Test
    public void testDoValidate_InvalidTLS12CipherSuite() throws Exception {
        // Test with TLS 1.2 and invalid cipher suite
        when(httpResponse.uri()).thenReturn(URI.create("https://example.com"));
        
        List<String> protocols = Arrays.asList("TLSv1.2");
        SSLValidator.SSLValidationResult successResult = SSLValidator.SSLValidationResult.success(protocols);
        when(mockSSLValidator.validateSSL("example.com", 443)).thenReturn(successResult);
        
        SSLValidator.CipherValidationResult cipherResult = 
            SSLValidator.CipherValidationResult.success("TLSv1.2", "TLS_RSA_WITH_AES_128_CBC_SHA"); // Invalid cipher
        when(mockSSLValidator.validateTLS12CipherSuites("example.com", 443)).thenReturn(cipherResult);

        boolean isValid = validation.doValidate();
        assertFalse("Validation should fail for invalid TLS 1.2 cipher suite", isValid);
        
        verify(mockSSLValidator).validateSSL("example.com", 443);
        verify(mockSSLValidator).validateTLS12CipherSuites("example.com", 443);
        verify(results).add(any()); // Error should be added for invalid cipher
    }
    
    @Test
    public void testDoValidate_CipherValidationFailure() throws Exception {
        // Test when cipher validation itself fails
        when(httpResponse.uri()).thenReturn(URI.create("https://example.com"));
        
        List<String> protocols = Arrays.asList("TLSv1.2");
        SSLValidator.SSLValidationResult successResult = SSLValidator.SSLValidationResult.success(protocols);
        when(mockSSLValidator.validateSSL("example.com", 443)).thenReturn(successResult);
        
        SSLValidator.CipherValidationResult cipherResult = 
            SSLValidator.CipherValidationResult.failure("Connection failed", new IOException("Timeout"));
        when(mockSSLValidator.validateTLS12CipherSuites("example.com", 443)).thenReturn(cipherResult);

        boolean isValid = validation.doValidate();
        assertFalse("Validation should fail when cipher validation fails", isValid);
        
        verify(mockSSLValidator).validateSSL("example.com", 443);
        verify(mockSSLValidator).validateTLS12CipherSuites("example.com", 443);
        verify(results, never()).add(any()); // No results should be added when cipher validation itself fails
    }
}