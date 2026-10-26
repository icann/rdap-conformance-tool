package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidationTestBase;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

public class ResponseValidationGtldProfile3DnssecTest extends ProfileValidationTestBase {

    private static final String MESSAGE =
        "The DNS resource records for the RDAP service are not DNSSEC signed "
            + "or the DNSSEC chain of trust is not valid.";

    /**
     * Default spy with a valid DNSSEC chain, so the inherited testValidate_ok passes
     * without network access.
     */
    @Override
    public ProfileValidation getProfileValidation() {
        return spyWithDnssecResult(true);
    }

    private ResponseValidationGtldProfile3Dnssec spyWithDnssecResult(boolean valid) {
        ResponseValidationGtldProfile3Dnssec validation =
            spy(new ResponseValidationGtldProfile3Dnssec(queryContext));
        doReturn(valid).when(validation).isDnssecChainOfTrustValid(any());
        return validation;
    }

    @Test
    public void testValidate_ValidChainOfTrust_NoError() {
        ResponseValidationGtldProfile3Dnssec validation = spyWithDnssecResult(true);

        assertThat(validation.validate()).isTrue();
        verify(results, never()).add(any());
        verify(validation).isDnssecChainOfTrustValid("example.com");
    }

    @Test
    public void testValidate_InvalidChainOfTrust_66002() {
        ResponseValidationGtldProfile3Dnssec validation = spyWithDnssecResult(false);

        assertThat(validation.validate()).isFalse();

        ArgumentCaptor<RDAPValidationResult> captor = ArgumentCaptor.forClass(RDAPValidationResult.class);
        verify(results).add(captor.capture());
        RDAPValidationResult result = captor.getValue();
        assertThat(result.getCode()).isEqualTo(-66002);
        assertThat(result.getValue()).isEqualTo("example.com");
        assertThat(result.getMessage()).isEqualTo(MESSAGE);
    }

    @Test
    public void testValidate_NoHostname_SkipsValidation() {
        when(config.getUri()).thenReturn(URI.create("file:///tmp/response.json"));
        ResponseValidationGtldProfile3Dnssec validation = spyWithDnssecResult(false);

        assertThat(validation.validate()).isTrue();
        verify(results, never()).add(any());
        verify(validation, never()).isDnssecChainOfTrustValid(any());
    }

    @Test
    public void testValidate_NullUri_SkipsValidation() {
        when(config.getUri()).thenReturn(null);
        ResponseValidationGtldProfile3Dnssec validation = spyWithDnssecResult(false);

        assertThat(validation.validate()).isTrue();
        verify(results, never()).add(any());
    }

    @Test
    public void testRootTrustAnchors_ParseableByValidatingResolver() throws Exception {
        // Guards against typos in the embedded IANA root trust anchors.
        org.xbill.DNS.dnssec.ValidatingResolver resolver =
            new org.xbill.DNS.dnssec.ValidatingResolver(new org.xbill.DNS.SimpleResolver());
        resolver.loadTrustAnchors(new java.io.ByteArrayInputStream(
            ResponseValidationGtldProfile3Dnssec.ROOT_TRUST_ANCHORS
                .getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }
}
