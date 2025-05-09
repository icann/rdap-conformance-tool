package org.icann.rdapconformance.validator.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.lang.UCharacter;
import java.net.URI;
import java.net.http.HttpResponse;
import org.icann.rdapconformance.validator.ConnectionStatus;
import org.icann.rdapconformance.validator.ConnectionTracker;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot2.RDAPJsonComparator;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;

public class DomainCaseFoldingValidation extends ProfileValidation {

  private static final Logger logger = LoggerFactory.getLogger(DomainCaseFoldingValidation.class);
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final RDAPJsonComparator jsonComparator = new RDAPJsonComparator();
  private final HttpResponse<String> rdapResponse;
  private final RDAPValidatorConfiguration config;
  private final String domainName;
  private final RDAPQueryType queryType;

  public DomainCaseFoldingValidation(HttpResponse<String> rdapResponse,
      RDAPValidatorConfiguration config,
      RDAPValidatorResults results,
      RDAPQueryType queryType) {
    super(results);
    this.rdapResponse = rdapResponse;
    this.config = config;
    this.queryType = queryType;
    String path = this.rdapResponse.uri().getPath();
    domainName = path.substring(path.lastIndexOf("/") + 1);
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
      HttpResponse<String> httpResponse = RDAPHttpRequest.makeHttpGetRequest(uri, config.getTimeout());

      // Check if we got a non-200 response first
      if (httpResponse.statusCode() != rdapResponse.statusCode()) {
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

      // Try to parse as JSON
      JsonNode httpResponseJson = mapper.readTree(httpResponse.body());
      JsonNode httpsResponseJson = mapper.readTree(rdapResponse.body());

      if (jsonComparator.compare(httpResponseJson, httpsResponseJson) != 0) {
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
    } catch (JsonParseException | JsonMappingException e) {
      logger.error(
          "Exception when parsing JSON response in [domainCaseFoldingValidation]",
          e);
      results.add(RDAPValidationResult.builder()
                                      .queriedURI(uri.toString())
                                      .httpMethod("GET")
                                      .code(-10403)
                                      .value(uri.toString())
                                      .message("RDAP responses do not match when handling domain label case folding.")
                                      .build());
      return false;
    } catch (JsonProcessingException e) {
      logger.error(
          "Exception when processing JSON in [domainCaseFoldingValidation]",
          e);
      results.add(RDAPValidationResult.builder()
                                      .queriedURI(uri.toString())
                                      .httpMethod("GET")
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
