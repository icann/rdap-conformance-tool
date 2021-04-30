package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import java.net.http.HttpResponse;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Validation1Dot6 {

  private static final Logger logger = LoggerFactory.getLogger(Validation1Dot6.class);

  public static boolean validate(int rdapResponseStatusCode, RDAPValidatorConfiguration config,
      RDAPValidatorResults results) {
    boolean hasError = false;
    try {
      HttpResponse<String> httpResponse = RDAPHttpRequest
          .makeHttpHeadRequest(config.getUri(), config.getTimeout());
      if (httpResponse.statusCode() != rdapResponseStatusCode) {
        results.add(RDAPValidationResult.builder()
            .code(-20300)
            .value(rdapResponseStatusCode + "\n/\n" + httpResponse.statusCode())
            .message("The HTTP Status code obtained when using the HEAD method is different from "
                + "the GET method. See section 1.6 of the RDAP_Technical_Implementation_Guide_2_1.")
            .build());
        hasError = true;
      }
    } catch (Exception e) {
      logger.error(
          "Exception when making HTTP HEAD request in order to check [tigSection_1_6_Validation]",
          e);
    }
    results.addGroup("tigSection_1_6_Validation", hasError);
    return !hasError;
  }
}
