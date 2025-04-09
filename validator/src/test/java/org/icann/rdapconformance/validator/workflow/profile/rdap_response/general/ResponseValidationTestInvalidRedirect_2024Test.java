package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.mockito.MockedStatic;

import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ResponseValidationTestInvalidRedirect_2024Test {

    private RDAPValidatorResults results;
    private RDAPValidatorConfiguration config;
    private ResponseValidationTestInvalidRedirect_2024 validation;

    @BeforeMethod
    public void setUp() {
        results = new RDAPValidatorResultsImpl();
        config = mock(RDAPValidatorConfiguration.class);
        when(config.getTimeout()).thenReturn(5000);
        when(config.getUri()).thenReturn(URI.create("http://example.com/rdap"));
        when(config.isGtldRegistrar()).thenReturn(true);
        when(config.isGtldRegistry()).thenReturn(true);
        when(config.useRdapProfileFeb2024()).thenReturn(true);
        validation = new ResponseValidationTestInvalidRedirect_2024(config, results);
    }

    @Test
    public void testDoValidate_RedirectsToItself() throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        HttpHeaders headers = mock(HttpHeaders.class);
        when(response.statusCode()).thenReturn(302);
        when(response.headers()).thenReturn(headers);
        when(headers.firstValue("Location")).thenReturn(Optional.of("http://example.com/rdap/domain/test.invalid"));

        MockedStatic<RDAPHttpRequest> mockRequest = mockStatic(RDAPHttpRequest.class);
        mockRequest.when(() -> RDAPHttpRequest.makeHttpGetRequest(any(), anyInt())).thenReturn(response);

        assertThat(validation.doValidate()).isFalse();

        ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor.forClass(RDAPValidationResult.class);
        assertThat(results.getAll().stream().anyMatch(result ->
            result.getCode() == -13005 &&
                result.getMessage().equals("Server responded with a redirect to itself for domain 'test.invalid'.")
        )).isTrue();

        mockRequest.close();
    }

    @Test
    public void testDoValidate_NoRedirect() throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);

        MockedStatic<RDAPHttpRequest> mockRequest = mockStatic(RDAPHttpRequest.class);
        mockRequest.when(() -> RDAPHttpRequest.makeHttpGetRequest(any(), anyInt())).thenReturn(response);

        assertThat(validation.doValidate()).isTrue();
        assertThat(results.getAll()).isEmpty();

        mockRequest.close();
    }

    @Test
    public void testHandleRedirect_RedirectsToItself() {
        HttpResponse<String> response = mock(HttpResponse.class);
        HttpHeaders headers = mock(HttpHeaders.class);
        when(response.headers()).thenReturn(headers);
        when(headers.firstValue("Location")).thenReturn(Optional.of("http://example.com/rdap/domain/test.invalid"));

        assertThat(validation.handleRedirect(response)).isFalse();
        assertThat(results.getAll().stream().anyMatch(result ->
            result.getCode() == -13005 &&
                result.getMessage().equals("Server responded with a redirect to itself for domain 'test.invalid'.")
        )).isTrue();
    }

    @Test
    public void testHandleRedirect_NoRedirectToItself() {
        HttpResponse<String> response = mock(HttpResponse.class);
        HttpHeaders headers = mock(HttpHeaders.class);
        when(response.headers()).thenReturn(headers);
        when(headers.firstValue("Location")).thenReturn(Optional.of("http://example.com/other"));

        assertThat(validation.handleRedirect(response)).isTrue();
        assertThat(results.getAll()).isEmpty();
    }
}