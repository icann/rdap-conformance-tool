package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.icann.rdapconformance.validator.workflow.rdap.HttpTestingUtils.givenChainedHttpRedirects;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidationTestBase;
import org.icann.rdapconformance.validator.workflow.rdap.HttpTestingUtils.RedirectData;
import org.testng.annotations.Test;

public class Validation1Dot13Test extends ProfileValidationTestBase {

  @Override
  @Test
  public void testValidate() {
    HttpResponse<String> httpResponse = mock(HttpResponse.class);

    doReturn(HttpHeaders
        .of(Map.of("Access-Control-Allow-Origin", List.of("value", "*")), (f1, f2) -> true))
        .when(httpResponse).headers();

    validateOk(new Validation1Dot13(httpResponse, results));
  }

  @Test
  public void testValidate_NoHeader_AddResult20100() {
    HttpResponse<String> httpResponse = mock(HttpResponse.class);

    doReturn(HttpHeaders.of(Map
            .of("Test-Header", List.of("value1", "value2"), "Access-Control-Allow-Origin",
                List.of("domain")),
        (f1, f2) -> true)).when(httpResponse).headers();

    validateNotOk(new Validation1Dot13(httpResponse, results), -20500,
        "Access-Control-Allow-Origin=[domain], Test-Header=[value1, value2]",
        "The HTTP header \"Access-Control-Allow-Origin: *\" is not included in the "
            + "HTTP headers. See section 1.13 of the RDAP_Technical_Implementation_Guide_2_1.");
  }

  @Test
  public void testValidate_UriNotHttpsInOneRedirect_AddResult20100() {
    RedirectData redirectData = givenChainedHttpRedirects();

    doReturn(HttpHeaders.of(Map
            .of("Test-Header", List.of("value1", "value2"), "Access-Control-Allow-Origin",
                List.of("domain")),
        (f1, f2) -> true)).when(redirectData.endingResponse).headers();

    validateNotOk(new Validation1Dot13(redirectData.startingResponse, results), -20500,
        "Access-Control-Allow-Origin=[domain], Test-Header=[value1, value2]",
        "The HTTP header \"Access-Control-Allow-Origin: *\" is not included in the "
            + "HTTP headers. See section 1.13 of the RDAP_Technical_Implementation_Guide_2_1.");
  }
}