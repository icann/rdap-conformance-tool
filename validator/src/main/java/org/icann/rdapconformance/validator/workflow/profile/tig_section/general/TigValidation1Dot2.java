package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.icann.rdapconformance.validator.CommonUtils.HTTP;
import static org.icann.rdapconformance.validator.CommonUtils.HTTPS;
import static org.icann.rdapconformance.validator.CommonUtils.HTTPS_PREFIX;
import static org.icann.rdapconformance.validator.CommonUtils.HTTP_PREFIX;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.Map.Entry;

import org.icann.rdapconformance.validator.ConnectionStatus;
import org.icann.rdapconformance.validator.ConnectionTracker;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TigValidation1Dot2 extends ProfileValidation {

  private static final Logger logger = LoggerFactory.getLogger(TigValidation1Dot2.class);

  private static final ObjectMapper mapper = new ObjectMapper();
  private static final RDAPJsonComparator jsonComparator = new RDAPJsonComparator();
  private final HttpResponse<String> rdapResponse;
  private final RDAPValidatorConfiguration config;

  public TigValidation1Dot2(HttpResponse<String> rdapResponse,
      RDAPValidatorConfiguration config,
      RDAPValidatorResults results) {
    super(results);
    this.rdapResponse = rdapResponse;
    this.config = config;
  }

  @Override
  public String getGroupName() {
    return "tigSection_1_2_Validation";
  }
  @Override
  public boolean doValidate() {
    boolean isValid = true;
    if (rdapResponse.uri().getScheme().equals(HTTP)) {
      results.add(RDAPValidationResult.builder()
                                      .code(-20100)
                                      .value(rdapResponse.uri().toString())
                                      .message(
                                          "The URL is HTTP, per section 1.2 of the RDAP_Technical_Implementation_Guide_2_1 "
                                              + "shall be HTTPS only.")
                                      .build());
      isValid = false;
    }
    if (rdapResponse.uri().getScheme().equals(HTTPS)) {
      try {
        URI uri = URI.create(rdapResponse.uri().toString().replaceFirst(HTTPS_PREFIX, HTTP_PREFIX));
        HttpResponse<String> httpResponse = RDAPHttpRequest
            .makeHttpGetRequest(uri, config.getTimeout());
        JsonNode httpResponseJson = mapper.readTree(httpResponse.body());
        JsonNode httpsResponseJson = mapper.readTree(rdapResponse.body());
        if (!httpResponse.uri().getScheme().equals(HTTPS) // if redirect to https, do not validate
            && jsonComparator.compare(httpResponseJson,
            httpsResponseJson) == 0) {
          results.add(RDAPValidationResult.builder()
                                          .code(-20101)
                                          .value(httpResponse.body() + "\n/\n" + rdapResponse.body())
                                          .message("The RDAP response was provided over HTTP, per section 1.2 of the "
                                              + "RDAP_Technical_Implementation_Guide_2_1 shall be HTTPS only.")
                                          .build());
          isValid = false;
        }
      } catch (Exception e) {
        logger.info("Exception when making HTTP request in order to check [tigSection_1_2_Validation]", e);
        ConnectionTracker.getInstance().completeCurrentConnection(ZERO, ConnectionStatus.CONNECTION_FAILED); // this SHOULD be a failure
        isValid = false; // Mark as failed but do not throw the exception
      }
    }
    return isValid;
  }

  /**
   * JSON comparator for RDAP.
   *
   * <p>Ignore list ordering except in vcard and ignore update events.</p>
   */
  public static class RDAPJsonComparator implements Comparator<JsonNode> {

    /**
     * Ignore event with action "last updateof RDAP database"
     */
    private boolean shouldAddElement(JsonNode node) {
      if (!node.isArray()) {
        if (node instanceof ObjectNode) {
          Map<String, Object> event = mapper
              .convertValue(node, new TypeReference<>() {
              });
          return !event.getOrDefault("eventAction", "").equals("last update of RDAP database");
        }
      }
      return true;
    }

    /**
     * ObjectNode comparator taken from ObjectNode.equals method but updated for our need.
     */
    private int compareObjectNodes(ObjectNode o1, ObjectNode o2) {
      Map<String, JsonNode> m1 = mapper.convertValue(o1, new TypeReference<>() {
      });
      Map<String, JsonNode> m2 = mapper.convertValue(o2, new TypeReference<>() {
      });
      int len = m1.size();
      if (m2.size() == len) {
        Iterator<Entry<String, JsonNode>> var7 = m1.entrySet().iterator();

        Entry<String, JsonNode> entry;
        JsonNode v2;
        do {
          if (!var7.hasNext()) {
            return 0;
          }

          entry = var7.next();
          v2 = m2.get(entry.getKey());
        } while (v2 != null && compareNested(entry.getKey(), entry.getValue(), v2) == 0);
      }
      return 1;
    }

    /**
     * Use our comparator instead of in jCards.
     */
    private int compareNested(String key, JsonNode n1, JsonNode n2) {
      if (key.equals("vcardArray")) {
        return n1.equals(n2) ? 0 : 1;
      }
      return jsonComparator.compare(n1, n2);
    }

    @Override
    public int compare(JsonNode n1, JsonNode n2) {
      if (!n1.isArray() || !n2.isArray()) {
        if (n1 instanceof ObjectNode && n2 instanceof ObjectNode) {
          return compareObjectNodes((ObjectNode) n1, (ObjectNode) n2);
        } else {
          try {
            var string1 = String.valueOf(n1);
            var string2 = String.valueOf(n2);
            return string1.equalsIgnoreCase(string2) ? 0 : 1;
          } catch (Exception e) {
            return n1.equals(n2) ? 0 : 1;
          }
        }
      }

      ArrayNode a1 = (ArrayNode) n1;
      ArrayNode a2 = (ArrayNode) n2;
      Set<JsonNode> s1 = new HashSet<>();
      Set<JsonNode> s2 = new HashSet<>();
      a1.elements().forEachRemaining(e -> {
        if (shouldAddElement(e)) {
          s1.add(e);
        }
      });
      a2.elements().forEachRemaining(e -> {
        if (shouldAddElement(e)) {
          s2.add(e);
        }
      });
      if (s1.size() != s2.size()) {
        return 1;
      }
      for (JsonNode nn1 : s1) {
        boolean match = false;
        for (JsonNode nn2 : s2) {
          if (jsonComparator.compare(nn1, nn2) == 0) {
            match = true;
            break;
          }
        }
        if (!match) {
          return 1;
        }
      }
      return 0;
    }
  }
}
