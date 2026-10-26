package org.icann.rdapconformance.validator.workflow;

import static org.icann.rdapconformance.validator.CommonUtils.GET;
import static org.icann.rdapconformance.validator.CommonUtils.LOCATION;
import static org.icann.rdapconformance.validator.CommonUtils.ONE;
import static org.icann.rdapconformance.validator.CommonUtils.SLASH;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ibm.icu.lang.UCharacter;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot2.RDAPJsonComparator;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DomainCaseFoldingValidation extends ProfileValidation {

  private static final Logger logger = LoggerFactory.getLogger(DomainCaseFoldingValidation.class);
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final RDAPJsonComparator jsonComparator = new CaseFoldingJsonComparator();
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
      // Use QueryContext-aware request with redirect handling
      HttpResponse<String> httpResponse =
          makeRequestWithRedirects(queryContext, uri, config.getTimeout(), config.getMaxRedirects());

      if (httpResponse == null) {
        results.add(RDAPValidationResult.builder()
                                        .queriedURI(uri.toString())
                                        .httpMethod(GET)
                                        .code(-10403)
                                        .value(uri.toString())
                                        .message("RDAP responses do not match when handling domain label case folding.")
                                        .build(queryContext));
        return false;
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
                                        .build(queryContext));
        return false;
      }

      // Try to parse as JSON
      JsonNode httpResponseJson = mapper.readTree(httpResponse.body());
      JsonNode httpsResponseJson = mapper.readTree(rdapResponse.body());

      // Links: "value" ignored; href/rel/type compared case-insensitively (CaseFoldingJsonComparator).
      if (jsonComparator.compare(httpResponseJson, httpsResponseJson) != ZERO) {
        results.add(RDAPValidationResult.builder()
                                        .queriedURI(uri.toString())
                                        .httpStatusCode(httpResponse.statusCode())
                                        .httpMethod("GET")
                                        .code(-10403)
                                        .value(uri.toString())
                                        .message("RDAP responses do not match when handling domain label case folding.")
                                        .build(queryContext));
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
                                      .build(queryContext));
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

  /**
   * RDAP JSON comparator for -10403 with link-specific rules:
   * "value" of every link is ignored; "href", "rel" and "type" are compared with
   * equalsIgnoreCase; any other link property falls back to the generic RDAP comparison.
   */
  static class CaseFoldingJsonComparator extends RDAPJsonComparator {

    private static final String LINKS = "links";
    private static final String IGNORED_LINK_PROPERTY = "value";
    private static final List<String> CASE_INSENSITIVE_LINK_PROPERTIES = List.of("href", "rel", "type");

    @Override
    protected int compareNested(String key, JsonNode n1, JsonNode n2) {
      if (LINKS.equals(key) && n1.isArray() && n2.isArray()) {
        return compareLinks(n1, n2);
      }
      return super.compareNested(key, n1, n2);
    }

    int compareLinks(JsonNode links1, JsonNode links2) {
      if (links1.size() != links2.size()) {
        return 1;
      }
      List<JsonNode> remaining = new ArrayList<>();
      links2.forEach(remaining::add);
      for (JsonNode link1 : links1) {
        boolean matched = false;
        Iterator<JsonNode> it = remaining.iterator();
        while (it.hasNext()) {
          if (linksEqual(link1, it.next())) {
            it.remove();
            matched = true;
            break;
          }
        }
        if (!matched) {
          return 1;
        }
      }
      return 0;
    }

    boolean linksEqual(JsonNode link1, JsonNode link2) {
      if (!link1.isObject() || !link2.isObject()) {
        return compare(link1, link2) == 0;
      }
      for (String property : CASE_INSENSITIVE_LINK_PROPERTIES) {
        if (!textEqualsIgnoreCase(link1.get(property), link2.get(property))) {
          return false;
        }
      }
      return compare(withoutLinkRuleProperties((ObjectNode) link1),
          withoutLinkRuleProperties((ObjectNode) link2)) == 0;
    }

    private static boolean textEqualsIgnoreCase(JsonNode v1, JsonNode v2) {
      if (v1 == null || v1.isNull()) {
        return v2 == null || v2.isNull();
      }
      if (v2 == null || v2.isNull()) {
        return false;
      }
      return v1.asText().equalsIgnoreCase(v2.asText());
    }

    private static ObjectNode withoutLinkRuleProperties(ObjectNode link) {
      ObjectNode copy = link.deepCopy();
      copy.remove(IGNORED_LINK_PROPERTY);
      copy.remove(CASE_INSENSITIVE_LINK_PROPERTIES);
      return copy;
    }
  }

  /**
   * Makes an HTTP request with redirect following capability using QueryContext.
   * This method follows redirects up to the specified maximum, ensuring same-host redirects only.
   */
  private HttpResponse<String> makeRequestWithRedirects(QueryContext qctx, URI currentUri, int timeoutSeconds, int maxRedirects) throws Exception {
    int remainingRedirects = maxRedirects;
    HttpResponse<String> response = null;

    while (remainingRedirects > ZERO) {
      response = RDAPHttpRequest.makeRequest(qctx, currentUri, timeoutSeconds, GET);
      int statusCode = response.statusCode();

      // Check if this is a redirect status code
      if (isRedirectStatus(statusCode)) {
        String location = response.headers().firstValue(LOCATION).orElse(null);
        if (location == null) {
          break; // No location header, can't follow redirect
        }

        URI redirectUri = URI.create(location);
        if (!redirectUri.isAbsolute()) {
          redirectUri = currentUri.resolve(redirectUri);
        }

        // Security check: only allow same-host redirects for case folding validation
        if (!currentUri.getHost().equals(redirectUri.getHost())) {
          logger.debug("Cross-host redirect blocked: {} -> {}", currentUri.getHost(), redirectUri.getHost());
          break; // Don't follow cross-host redirects
        }

        currentUri = redirectUri;
        remainingRedirects--;
      } else {
        break; // Not a redirect, we have our final response
      }
    }

    return response;
  }

  /**
   * Legacy redirect handling for cases without QueryContext.
   * Uses the old-style HTTP client without QueryContext for backwards compatibility.
   */
  private HttpResponse<String> makeRequestWithRedirectsLegacy(URI uri, int timeoutSeconds, int maxRedirects) throws Exception {
    // Since makeHttpGetRequestWithRedirects is broken (passes null QueryContext),
    // we'll just make a simple request without redirect following for legacy cases
    // This should be rare since most validation now uses QueryContext
    throw new UnsupportedOperationException("Legacy redirect handling not implemented - QueryContext is required");
  }

  /**
   * Checks if the given HTTP status code indicates a redirect.
   */
  private boolean isRedirectStatus(int statusCode) {
    return statusCode == 301 || statusCode == 302 || statusCode == 303 || statusCode == 307 || statusCode == 308;
  }
}
