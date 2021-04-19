package org.icann.rdapconformance.validator.workflow.profile.tig_section;

import static org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest.makeHttpRequest;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Optional;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TigSectionGeneral {

  private static final Logger logger = LoggerFactory.getLogger(TigSectionGeneral.class);

  private final RDAPValidatorConfiguration config;
  private final RDAPValidatorResults results;

  public TigSectionGeneral(
      RDAPValidatorConfiguration config,
      RDAPValidatorResults results) {
    this.config = config;
    this.results = results;
  }

  public static boolean validate(HttpResponse<String> rdapResponse,
      RDAPValidatorConfiguration config,
      RDAPValidatorResults results) {
    boolean overallResult = true;
    Optional<HttpResponse<String>> responseOpt = Optional.of(rdapResponse);
    while (responseOpt.isPresent()) {
      HttpResponse<String> response = responseOpt.get();
      if (response.uri().getScheme().equals("http")) {
        results.add(RDAPValidationResult.builder()
            .code(-20100)
            .value(response.uri().toString())
            .message(
                "The URL is HTTP, per section 1.2 of the RDAP_Technical_Implementation_Guide_2_1 "
                    + "shall be HTTPS only.")
            .build());
        overallResult = false;
        break;
      }
      responseOpt = response.previousResponse();
    }
    if (config.getUri().getScheme().equals("https")) {
      try {
        URI uri  = URI.create(config.getUri().toString().replaceFirst("https://", "http://"));
        HttpResponse<String> httpResponse = makeHttpRequest(uri, config.getTimeout());
        // TODO update comparison with json comparison to ignore update date and possible different linebreaks, etc
        if (httpResponse.body().equals(rdapResponse.body())) {
          results.add(RDAPValidationResult.builder()
              .code(-20101)
              .value(httpResponse.body() + "\n/\n" + rdapResponse.body())
              .message("The RDAP response was provided over HTTP, per section 1.2 of the "
                  + "RDAP_Technical_Implementation_Guide_2_1shall be HTTPS only.")
              .build());
          overallResult = false;
        }
      } catch (Exception e) {
        logger.error(
            "Exception when making HTTP request in order to check [tigSection_1_2_Validation]", e);
      }
    }
    return overallResult;
  }
}
