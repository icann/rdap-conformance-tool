package org.icann.rdapconformance.validator.workflow;

import static org.icann.rdapconformance.validator.CommonUtils.GET;
import static org.icann.rdapconformance.validator.CommonUtils.ONE;
import static org.icann.rdapconformance.validator.CommonUtils.SLASH;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.lang.UCharacter;
import java.net.URI;
import java.net.http.HttpResponse;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot2.RDAPJsonComparator;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DomainCaseFoldingValidation extends ProfileValidation {

  private static final Logger logger = LoggerFactory.getLogger(DomainCaseFoldingValidation.class);
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final RDAPJsonComparator jsonComparator = new RDAPJsonComparator();
  private final HttpResponse<String> rdapResponse;
  private final RDAPValidatorConfiguration config;
  private final String domainName;
  private final RDAPQueryType queryType;
  private final QueryContext queryContext;


  public DomainCaseFoldingValidation(HttpResponse<String> rdapResponse,
      QueryContext queryContext,
      RDAPQueryType queryType) {
    super(queryContext.getResults());
    this.rdapResponse = rdapResponse;
    this.config = queryContext.getConfig();
    this.queryType = queryType;
    this.queryContext = queryContext;
    String path = this.rdapResponse.uri().getPath();
    domainName = path.substring(path.lastIndexOf(SLASH) + ONE);
  }

  @Override
  public String getGroupName() {
    return "domainCaseFoldingValidation";
  }

  @Override
  protected boolean doValidate() throws Exception {
    String newDomain = foldDomain();
    // if it is not foldeable:
    if (domainName.equals(newDomain)) {
      return true;
    }

    URI uri = URI.create(rdapResponse.uri().toString().replace(domainName, newDomain));
    try {
      HttpResponse<String> httpResponse = null;

      if (queryContext != null) {
        // Use QueryContext-aware request for proper IPv6/IPv4 protocol handling
        httpResponse = RDAPHttpRequest.makeRequest(queryContext, uri, config.getTimeout(), GET);
      } else {
        httpResponse = RDAPHttpRequest.makeHttpGetRequestWithRedirects(uri, config.getTimeout(), config.getMaxRedirects());
      }

      // Check if we got a non-200 response first
      if (httpResponse.statusCode() != rdapResponse.statusCode()) {
        results.add(RDAPValidationResult.builder()
                                        .queriedURI(uri.toString())
                                        .httpStatusCode(httpResponse.statusCode())
                                        .httpMethod(GET)
                                        .code(-10403)
                                        .value(uri.toString())
                                        .message("RDAP responses do not match when handling domain label case folding.")
                                        .build());
        return false;
      }

      // Try to parse as JSON
      JsonNode httpResponseJson = mapper.readTree(httpResponse.body());
      JsonNode httpsResponseJson = mapper.readTree(rdapResponse.body());

      if (jsonComparator.compare(httpResponseJson, httpsResponseJson) != ZERO) {
        results.add(RDAPValidationResult.builder()
                                        .queriedURI(uri.toString())
                                        .httpStatusCode(httpResponse.statusCode())
                                        .httpMethod("GET")
                                        .code(-10403)
                                        .value(uri.toString())
                                        .message("RDAP responses do not match when handling domain label case folding.")
                                        .build());
        return false;
      }
    } catch (JsonProcessingException e) {
      logger.debug(
          "Exception when processing JSON in [domainCaseFoldingValidation]",
          e);
      results.add(RDAPValidationResult.builder()
                                      .queriedURI(uri.toString())
                                      .httpMethod(GET)
                                      .code(-10403)
                                      .value(uri.toString())
                                      .message("RDAP responses do not match when handling domain label case folding.")
                                      .build());
      return false;
    }
    return true;
  }

  @Override
  public boolean doLaunch() {
    return this.queryType.equals(RDAPQueryType.DOMAIN);
  }

  String foldDomain() {
    StringBuilder newDomain = new StringBuilder();
    boolean fold = false;
    for (char c : domainName.toCharArray()) {
      if (fold) {
        if (UCharacter.isULowercase(c)) {
          newDomain.append(Character.toString(UCharacter.toUpperCase(c)));
        } else {
          newDomain.append(Character.toString(UCharacter.toLowerCase(c)));
        }
      } else {
        newDomain.append(c);
      }
      fold = !fold;
    }
    return newDomain.toString();
  }
}
