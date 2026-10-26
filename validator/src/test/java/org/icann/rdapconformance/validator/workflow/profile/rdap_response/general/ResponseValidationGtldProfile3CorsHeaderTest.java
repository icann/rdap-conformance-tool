package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import static org.icann.rdapconformance.validator.workflow.rdap.HttpTestingUtils.givenChainedHttpRedirects;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidationTestBase;
import org.icann.rdapconformance.validator.workflow.rdap.HttpTestingUtils.RedirectData;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidationGtldProfile3CorsHeaderTest extends ProfileValidationTestBase {

    private static final String MESSAGE =
        "The RDAP response must contain exactly one Access-Control-Allow-Origin HTTP response header.";

    private HttpResponse<String> httpResponse;

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
        httpResponse = mock(HttpResponse.class);
        givenHeaders(Map.of("Access-Control-Allow-Origin", List.of("*")));
    }

    @Override
    public ProfileValidation getProfileValidation() {
        queryContext.setCurrentHttpResponse(httpResponse);
        return new ResponseValidationGtldProfile3CorsHeader(queryContext);
    }

    private void givenHeaders(Map<String, List<String>> headers) {
        doReturn(HttpHeaders.of(headers, (f1, f2) -> true)).when(httpResponse).headers();
    }

    @Test
    public void testValidate_HeaderNameCaseInsensitive_Ok() {
        givenHeaders(Map.of("access-control-allow-origin", List.of("*")));
        validate();
    }

    @Test
    public void testValidate_MissingHeader_AddResult66003() {
        givenHeaders(Map.of("Content-Type", List.of("application/rdap+json")));
        validate(-66003, "Content-Type=[application/rdap+json]", MESSAGE);
    }

    @Test
    public void testValidate_DuplicatedHeader_AddResult66003() {
        givenHeaders(Map.of("Access-Control-Allow-Origin", List.of("*", "*")));
        validate(-66003, "Access-Control-Allow-Origin=[*, *]", MESSAGE);
    }

    @Test
    public void testValidate_DuplicatedHeaderInRedirect_AddResult66003() {
        RedirectData redirectData = givenChainedHttpRedirects();
        doReturn(HttpHeaders.of(
            Map.of("Access-Control-Allow-Origin", List.of("*", "https://example.com")),
            (f1, f2) -> true)).when(redirectData.endingResponse).headers();

        httpResponse = redirectData.startingResponse;
        validate(-66003, "Access-Control-Allow-Origin=[*, https://example.com]", MESSAGE);
    }

    @Test
    public void testValidate_NullResponse_Skips() {
        httpResponse = null;
        validate();
    }
}
