package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.IOException;
import java.net.URI;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.SSLValidator;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.SSLValidator.CertificateExpirationResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidationGtldProfile3TlsCertificateTest extends ProfileValidationTestBase {

    private static final String MESSAGE = "The TLS certificate has expired.";
    private static final String URI_VALUE = "https://example.com/domain/test.example";

    private SSLValidator sslValidator;

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
        sslValidator = mock(SSLValidator.class);
        givenCertificate(CertificateExpirationResult.valid());
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidationGtldProfile3TlsCertificate(queryContext, sslValidator);
    }

    private void givenCertificate(CertificateExpirationResult result) {
        doReturn(result).when(sslValidator).checkCertificateExpiration(anyString(), anyInt());
    }

    @Test
    public void testValidate_ExpiredCertificate_AddResult66009() {
        givenCertificate(CertificateExpirationResult.expired());
        validate(-66009, URI_VALUE, MESSAGE);
    }

    @Test
    public void testValidate_UsesHostAndDefaultHttpsPort() {
        validate();
        verify(sslValidator).checkCertificateExpiration("example.com", 443);
    }

    @Test
    public void testValidate_UsesExplicitPort() {
        doReturn(URI.create("https://example.com:8443/domain/test.example")).when(config).getUri();
        validate();
        verify(sslValidator).checkCertificateExpiration("example.com", 8443);
    }

    @Test
    public void testValidate_CertificateCannotBeRetrieved_NoResult() {
        givenCertificate(CertificateExpirationResult.failure("connection refused"));
        validate();
    }

    @Test
    public void testDoLaunch_HttpScheme_NotLaunched() {
        doReturn(URI.create("http://example.com/domain/test.example")).when(config).getUri();
        assertThat(getProfileValidation().doLaunch()).isFalse();
        verifyNoInteractions(sslValidator);
    }

    @Test
    public void testDoLaunch_HttpsSchemeCaseInsensitive_Launched() {
        doReturn(URI.create("HTTPS://example.com/domain/test.example")).when(config).getUri();
        assertThat(getProfileValidation().doLaunch()).isTrue();
    }
}
