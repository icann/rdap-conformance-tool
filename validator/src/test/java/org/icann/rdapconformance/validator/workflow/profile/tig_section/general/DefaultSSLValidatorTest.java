package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.security.cert.X509Certificate;
import java.util.Date;
import org.icann.rdapconformance.validator.DNSCacheResolver;
import org.icann.rdapconformance.validator.NetworkProtocol;
import org.testng.annotations.Test;

public class DefaultSSLValidatorTest {

    @Test
    public void isExpired_NotAfterInThePast_True() {
        assertThat(DefaultSSLValidator.isExpired(certificateExpiring(-60_000L))).isTrue();
    }

    @Test
    public void isExpired_NotAfterInTheFuture_False() {
        assertThat(DefaultSSLValidator.isExpired(certificateExpiring(60_000L))).isFalse();
    }

    @Test
    public void checkCertificateExpiration_UnresolvableHost_Failure() {
        DNSCacheResolver resolver = mock(DNSCacheResolver.class);
        doReturn(null).when(resolver).getFirstV4Address("unresolvable.invalid");
        DefaultSSLValidator validator = new DefaultSSLValidator(NetworkProtocol.IPv4, resolver);

        SSLValidator.CertificateExpirationResult result =
            validator.checkCertificateExpiration("unresolvable.invalid", 443);

        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.isExpired()).isFalse();
        assertThat(result.getErrorMessage()).contains("unresolvable.invalid");
    }

    private static X509Certificate certificateExpiring(long offsetMillis) {
        X509Certificate certificate = mock(X509Certificate.class);
        doReturn(new Date(System.currentTimeMillis() + offsetMillis)).when(certificate).getNotAfter();
        return certificate;
    }
}
