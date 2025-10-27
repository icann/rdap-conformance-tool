package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.icann.rdapconformance.validator.CommonUtils.HEAD;

import java.net.http.HttpResponse;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TigValidation1Dot6 extends ProfileValidation {

  private static final Logger logger = LoggerFactory.getLogger(TigValidation1Dot6.class);
  private final int rdapResponseStatusCode;
  private final RDAPValidatorConfiguration config;
  private final QueryContext queryContext;

  public TigValidation1Dot6(int rdapResponseStatusCode, RDAPValidatorConfiguration config,
      RDAPValidatorResults results) {
    super(results);
    this.rdapResponseStatusCode = rdapResponseStatusCode;
    this.config = config;
    this.queryContext = null; // Legacy constructor for backward compatibility
  }

  public TigValidation1Dot6(int rdapResponseStatusCode, QueryContext queryContext) {
    super(queryContext.getResults());
    this.rdapResponseStatusCode = rdapResponseStatusCode;
    this.config = queryContext.getConfig();
    this.queryContext = queryContext;
  }

  @Override
  public String getGroupName() {
    return "tigSection_1_6_Validation";
  }

  public boolean doValidate() {
    try {
      HttpResponse<String> httpResponse = null;

      if (queryContext != null) {
        // Use QueryContext-aware request for proper IPv6/IPv4 protocol handling
        httpResponse = RDAPHttpRequest.makeRequest(queryContext, config.getUri(), config.getTimeout(), HEAD);
      } else {
        // Fallback to legacy singleton-based request
        httpResponse = RDAPHttpRequest.makeHttpHeadRequest(config.getUri(), config.getTimeout());
      }

      if (httpResponse.statusCode() != rdapResponseStatusCode) {
        results.add(RDAPValidationResult.builder()
            .httpStatusCode(httpResponse.statusCode())
            .code(-20300)
            .httpMethod(HEAD)
            .value(rdapResponseStatusCode + "\n/\n" + httpResponse.statusCode())
            .message("The HTTP Status code obtained when using the HEAD method is different from "
                + "the GET method. See section 1.6 of the RDAP_Technical_Implementation_Guide_2_1.")
            .build());
        return false;
      }
    } catch (Exception e) {
      logger.info(
          "Exception when making HTTP HEAD request in order to check [tigSection_1_6_Validation]",
          e);
      return false;
    }
    return true;
  }
}
