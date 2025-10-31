package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.stream.Collectors;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public final class TigValidation1Dot13 extends ProfileValidation {

  private final HttpResponse<String> rdapResponse;
  private final QueryContext queryContext;

  public TigValidation1Dot13(QueryContext queryContext) {
    super(queryContext.getResults());
    this.rdapResponse = (HttpResponse<String>) queryContext.getQuery().getRawResponse();
    this.queryContext = queryContext;
  }

  /**
   * @deprecated Use TigValidation1Dot13(QueryContext) instead
   * TODO: Migrate to QueryContext-only constructor
   */
  @Deprecated
  public TigValidation1Dot13(HttpResponse<String> rdapResponse, RDAPValidatorResults results) {
    super(results);
    this.rdapResponse = rdapResponse;
    this.queryContext = null; // Not available in deprecated constructor
  }

  @Override
  public String getGroupName() {
    return "tigSection_1_13_Validation";
  }

  @Override
  public boolean doValidate() {
    boolean isValid = true;
    Optional<HttpResponse<String>> responseOpt = Optional.of(rdapResponse);
    while (responseOpt.isPresent()) {
      HttpResponse<String> response = responseOpt.get();
      if (!response.headers().allValues("Access-Control-Allow-Origin").contains("*")) {
        RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
                   .code(-20500)
                   .value(response.headers().map().entrySet().stream()
                   .map(e -> e.getKey() + "=" + e.getValue().toString())
                   .collect(Collectors.joining(", ")))
                   .message("The HTTP header \"Access-Control-Allow-Origin: *\" is not included in the "
                + "HTTP headers. See section 1.13 of the RDAP_Technical_Implementation_Guide_2_1.");

        if (queryContext != null) {
          results.add(builder.build(queryContext));
        } else {
          results.add(builder.build()); // Fallback for deprecated constructor
        }
        isValid = false;
      }
      responseOpt = response.previousResponse();
    }
    return isValid;
  }
}
