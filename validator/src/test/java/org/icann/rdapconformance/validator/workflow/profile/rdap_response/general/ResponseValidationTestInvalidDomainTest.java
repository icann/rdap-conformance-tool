package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPQuery;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryTypeProcessor;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResultFile;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;

import org.testng.annotations.Test;

import java.net.URI;
import java.util.Optional;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ResponseValidationTestInvalidDomainTest {

    public static final String TEST_INVALID = "test.invalid";


    @Test
    public void testIsRedirectingTestDotInvalidToItself_RedirectsToItself() {
        RDAPValidatorResults results = new RDAPValidatorResultsImpl();
        URI currentUri = URI.create("http://example.com/test.invalid");
        URI redirectUri = URI.create("http://example.com/test.invalid");

        boolean result = ResponseValidationTestInvalidDomain.isRedirectingTestDotInvalidToItself(results, currentUri, redirectUri);

        assertThat(result).isTrue();
        assertThat(results.getAll()).contains(
            RDAPValidationResult.builder()
                                .code(-13005)
                                .value("<location header value>")
                                .message("Server responded with a redirect to itself for domain 'test.invalid'.")
                                .build());
    }

    @Test
    public void testIsRedirectingTestDotInvalidToItself_RedirectsToItselfWithSlash() {
        RDAPValidatorResults results = new RDAPValidatorResultsImpl();
        URI currentUri = URI.create("http://example.com/test.invalid");
        URI redirectUri = URI.create("/test.invalid");

        boolean result = ResponseValidationTestInvalidDomain.isRedirectingTestDotInvalidToItself(results, currentUri, redirectUri);

        assertThat(result).isTrue();
        assertThat(results.getAll()).contains(
            RDAPValidationResult.builder()
                                .code(-13005)
                                .value("<location header value>")
                                .message("Server responded with a redirect to itself for domain 'test.invalid'.")
                                .build());
    }

    @Test
    public void testIsRedirectingTestDotInvalidToItself_DoesNotRedirectToItself() {
        RDAPValidatorResults results = new RDAPValidatorResultsImpl();
        URI currentUri = URI.create("http://example.com/test.invalid");
        URI redirectUri = URI.create("http://example.com/other");

        boolean result = ResponseValidationTestInvalidDomain.isRedirectingTestDotInvalidToItself(results, currentUri, redirectUri);

        assertThat(result).isFalse();
        assertThat(results.getAll()).isEmpty();
    }

    @Test
    public void testIsRedirectingTestDotInvalidToItself_DoesNotContainTestInvalid() {
        RDAPValidatorResults results = new RDAPValidatorResultsImpl();
        URI currentUri = URI.create("http://example.com/other");
        URI redirectUri = URI.create("http://example.com/test.invalid");

        boolean result = ResponseValidationTestInvalidDomain.isRedirectingTestDotInvalidToItself(results, currentUri, redirectUri);

        assertThat(result).isFalse();
        assertThat(results.getAll()).isEmpty();
    }
    @Test
    public void testIsHttpOKAndTestDotInvalid() {
        RDAPQuery query = mock(RDAPQuery.class);
        RDAPQueryTypeProcessor queryTypeProcessor = mock(RDAPQueryTypeProcessor.class);
        RDAPValidatorResults results = new RDAPValidatorResultsImpl();
        RDAPValidationResultFile rdapValidationResultFile = mock(RDAPValidationResultFile.class);

        when(queryTypeProcessor.getQueryType()).thenReturn(RDAPQueryType.DOMAIN);
        when(query.getData()).thenReturn(TEST_INVALID);
        when(query.getStatusCode()).thenReturn(Optional.of(HTTP_OK));

        assertThat(ResponseValidationTestInvalidDomain.isHttpOKAndTestDotInvalid(query, queryTypeProcessor, results, rdapValidationResultFile)).isTrue();
    }

    @Test
    public void testIsHttp404AndTestDotInvalid() {
        RDAPQuery query = mock(RDAPQuery.class);
        RDAPQueryTypeProcessor queryTypeProcessor = mock(RDAPQueryTypeProcessor.class);
        RDAPValidatorResults results = new RDAPValidatorResultsImpl();
        RDAPValidationResultFile rdapValidationResultFile = mock(RDAPValidationResultFile.class);

        when(queryTypeProcessor.getQueryType()).thenReturn(RDAPQueryType.DOMAIN);
        when(query.getData()).thenReturn(TEST_INVALID);
        when(query.getStatusCode()).thenReturn(Optional.of(HTTP_NOT_FOUND));

        assertThat(ResponseValidationTestInvalidDomain.isHttpOKAndTestDotInvalid(query, queryTypeProcessor, results, rdapValidationResultFile)).isFalse();
    }

    @Test
    public void testIsHttp404WithEntityTestDotInvalid() {
        RDAPQuery query = mock(RDAPQuery.class);
        RDAPQueryTypeProcessor queryTypeProcessor = mock(RDAPQueryTypeProcessor.class);
        RDAPValidatorResults results = new RDAPValidatorResultsImpl();
        RDAPValidationResultFile rdapValidationResultFile = mock(RDAPValidationResultFile.class);

        when(queryTypeProcessor.getQueryType()).thenReturn(RDAPQueryType.ENTITY);
        when(query.getData()).thenReturn(TEST_INVALID);
        when(query.getStatusCode()).thenReturn(Optional.of(HTTP_NOT_FOUND));

        assertThat(ResponseValidationTestInvalidDomain.isHttpOKAndTestDotInvalid(query, queryTypeProcessor, results, rdapValidationResultFile)).isFalse();
    }

    @Test
    public void testIsHttp404WithEntityAndFooTestDotInvalid() {
        RDAPQuery query = mock(RDAPQuery.class);
        RDAPQueryTypeProcessor queryTypeProcessor = mock(RDAPQueryTypeProcessor.class);
        RDAPValidatorResults results = new RDAPValidatorResultsImpl();
        RDAPValidationResultFile rdapValidationResultFile = mock(RDAPValidationResultFile.class);

        when(queryTypeProcessor.getQueryType()).thenReturn(RDAPQueryType.ENTITY);
        when(query.getData()).thenReturn("foo");
        when(query.getStatusCode()).thenReturn(Optional.of(HTTP_NOT_FOUND));

        assertThat(ResponseValidationTestInvalidDomain.isHttpOKAndTestDotInvalid(query, queryTypeProcessor, results, rdapValidationResultFile)).isFalse();
    }
}