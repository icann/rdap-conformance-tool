package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icann.rdapconformance.validator.workflow.rdap.HttpTestingUtils.givenChainedHttpRedirects;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import org.icann.rdapconformance.validator.workflow.rdap.HttpTestingUtils.RedirectData;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

public class Validation1Dot13Test {

  @Test
  public void testValidate() {
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);
    HttpResponse<String> httpResponse = mock(HttpResponse.class);

    doReturn(HttpHeaders
        .of(Map.of("Access-Control-Allow-Origin", List.of("value", "*")), (f1, f2) -> true))
        .when(httpResponse).headers();

    assertThat(Validation1Dot13.validate(httpResponse, results)).isTrue();
    verify(results).addGroup("tigSection_1_13_Validation", false);
    verifyNoMoreInteractions(results);
  }

  @Test
  public void testValidate_NoHeader_AddResult20100() {
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);
    ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor
        .forClass(RDAPValidationResult.class);
    HttpResponse<String> httpResponse = mock(HttpResponse.class);

    doReturn(HttpHeaders.of(Map
            .of("Test-Header", List.of("value1", "value2"), "Access-Control-Allow-Origin",
                List.of("domain")),
        (f1, f2) -> true)).when(httpResponse).headers();

    assertThat(Validation1Dot13.validate(httpResponse, results)).isFalse();
    verify(results).add(resultCaptor.capture());
    RDAPValidationResult result = resultCaptor.getValue();
    assertThat(result).hasFieldOrPropertyWithValue("code", -20500)
        .hasFieldOrPropertyWithValue("value",
            "Access-Control-Allow-Origin=[domain], Test-Header=[value1, value2]")
        .hasFieldOrPropertyWithValue("message",
            "The HTTP header \"Access-Control-Allow-Origin: *\" is not included in the "
                + "HTTP headers. See section 1.13 of the RDAP_Technical_Implementation_Guide_2_1.");
    verify(results).addGroup("tigSection_1_13_Validation", true);
  }

  @Test
  public void testValidate_UriNotHttpsInOneRedirect_AddResult20100() {
    RedirectData redirectData = givenChainedHttpRedirects();
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);
    ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor
        .forClass(RDAPValidationResult.class);

    doReturn(HttpHeaders.of(Map
            .of("Test-Header", List.of("value1", "value2"), "Access-Control-Allow-Origin",
                List.of("domain")),
        (f1, f2) -> true)).when(redirectData.endingResponse).headers();

    assertThat(Validation1Dot13.validate(redirectData.startingResponse, results)).isFalse();
    verify(results).add(resultCaptor.capture());
    RDAPValidationResult result = resultCaptor.getValue();
    assertThat(result).hasFieldOrPropertyWithValue("code", -20500)
        .hasFieldOrPropertyWithValue("value",
            "Access-Control-Allow-Origin=[domain], Test-Header=[value1, value2]")
        .hasFieldOrPropertyWithValue("message",
            "The HTTP header \"Access-Control-Allow-Origin: *\" is not included in the "
                + "HTTP headers. See section 1.13 of the RDAP_Technical_Implementation_Guide_2_1.");
    verify(results).addGroup("tigSection_1_13_Validation", true);
  }
}