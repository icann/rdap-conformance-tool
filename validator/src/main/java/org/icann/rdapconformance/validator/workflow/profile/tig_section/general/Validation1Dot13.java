package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public class Validation1Dot13 {

  public static boolean validate(HttpResponse<String> rdapResponse,
      RDAPValidatorResults results) {
    boolean overallResult = true;
    Optional<HttpResponse<String>> responseOpt = Optional.of(rdapResponse);
    while (responseOpt.isPresent()) {
      HttpResponse<String> response = responseOpt.get();
      if (!response.headers().allValues("Access-Control-Allow-Origin").contains("*")) {
        results.add(RDAPValidationResult.builder()
            .code(-20500)
            .value(response.headers().map().entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue().toString())
                .collect(Collectors.joining(", ")))
            .message("The HTTP header \"Access-Control-Allow-Origin: *\" is not included in the "
                + "HTTP headers. See section 1.13 of the RDAP_Technical_Implementation_Guide_2_1.")
            .build());
        overallResult = false;
      }
      responseOpt = response.previousResponse();
    }
    results.addGroup("tigSection_1_13_Validation", !overallResult);
    return overallResult;
  }
}
