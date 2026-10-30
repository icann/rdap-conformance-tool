package org.icann.rdapconformance.validator.workflow;

import static org.icann.rdapconformance.validator.CommonUtils.GET;
import static org.icann.rdapconformance.validator.CommonUtils.LOCATION;
import static org.icann.rdapconformance.validator.CommonUtils.ONE;
import static org.icann.rdapconformance.validator.CommonUtils.SLASH;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ibm.icu.lang.UCharacter;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;

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
      HttpResponse<String> httpResponse = null;

      // Use QueryContext-aware request with redirect handling
      httpResponse = makeRequestWithRedirects(queryContext, uri, config.getTimeout(), config.getMaxRedirects());

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

      // Links: "value" and "href" ignored; rel/type compared case-insensitively (CaseFoldingJsonComparator).
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
   * RDAP JSON comparator for -10403.
   *
   * <p>Rules (confirmed by Andy Newton): link "value" and "href" are ignored; link "rel" and
   * "type" are compared with equalsIgnoreCase; other link properties use the generic
   * (case-insensitive) comparison.
   * Arrays are compared as multisets without the strict pre-deduplication of the base class,
   * so elements that differ only by an ignored property are still paired one-to-one.</p>
   */
  static class CaseFoldingJsonComparator extends RDAPJsonComparator {

    private static final String LINKS = "links";
    private static final List<String> IGNORED_LINK_PROPERTIES = List.of("value", "href");
    private static final List<String> CASE_INSENSITIVE_LINK_PROPERTIES = List.of("rel", "type");

    @Override
    protected int compareNested(String key, JsonNode n1, JsonNode n2) {
      if (LINKS.equals(key) && n1.isArray() && n2.isArray()) {
        return matchAll(n1, n2, this::linksEqual);
      }
      return super.compareNested(key, n1, n2);
    }

    @Override
    protected int compareArrays(ArrayNode a1, ArrayNode a2) {
      return matchAll(a1, a2, (e1, e2) -> compare(e1, e2) == 0);
    }

    /** One-to-one matching of two arrays, ignoring order and skipping ignored events. */
    private int matchAll(JsonNode a1, JsonNode a2, BiPredicate<JsonNode, JsonNode> equal) {
      List<JsonNode> l1 = kept(a1);
      List<JsonNode> l2 = kept(a2);
      if (l1.size() != l2.size()) {
        return 1;
      }
      for (JsonNode e1 : l1) {
        Iterator<JsonNode> it = l2.iterator();
        boolean matched = false;
        while (it.hasNext()) {
          if (equal.test(e1, it.next())) {
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

    private List<JsonNode> kept(JsonNode array) {
      List<JsonNode> list = new ArrayList<>();
      array.forEach(e -> {
        if (shouldAddElement(e)) {
          list.add(e);
        }
      });
      return list;
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
      boolean absent1 = v1 == null || v1.isNull();
      boolean absent2 = v2 == null || v2.isNull();
      if (absent1 || absent2) {
        return absent1 && absent2;
      }
      return v1.asText().equalsIgnoreCase(v2.asText());
    }

    private static ObjectNode withoutLinkRuleProperties(ObjectNode link) {
      ObjectNode copy = link.deepCopy();
      copy.remove(IGNORED_LINK_PROPERTIES);
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
