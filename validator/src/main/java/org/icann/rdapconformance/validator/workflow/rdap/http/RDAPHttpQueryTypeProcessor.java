package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.icann.rdapconformance.validator.CommonUtils.EMPTY_STRING;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.ToolResult;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryTypeProcessor;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDAPHttpQueryTypeProcessor implements RDAPQueryTypeProcessor {

  private static final Logger logger = LoggerFactory.getLogger(RDAPHttpQueryTypeProcessor.class);
  private final RDAPValidatorConfiguration config;
  private ToolResult status = null;
  private RDAPHttpQueryType queryType = null;

  public RDAPHttpQueryTypeProcessor(RDAPValidatorConfiguration config) {
    this.config = config;
  }

  /* Verify the URI represent one of the following RDAP queries, and if not, exit with a return
   * code of 3:
   *  * domain/<domain name>
   *    Note: the domain name must pass Domain Name validation [domainNameValidation], however,
   *    A-labels and U-labels (see IDNA_RFCs) shall not be mixed. If A-labels and U-labels are
   *    mixed, the tool shall exit with the return code of 4.
   *  * nameserver/<nameserver name>
   *    Note: the nameserver name must pass Domain Name validation [domainNameValidation],
   *    however A-labels and U-labels (see IDNA_RFCs) shall not be mixed. If A-labels and
   *    U-labels are mixed, the tool shall exit with the return code of 4.
   *  * entity/<handle>
   *  * help
   *  * nameservers?ip=<nameserver search pattern>
   */
 @Override
 public boolean check(RDAPDatasetService datasetService) {
     queryType = RDAPHttpQueryType.getType(this.config.getUri().toString());
     if (queryType == null) {
         logger.error("Unknown RDAP query type for URI {}", this.config.getUri());
         status = ToolResult.UNSUPPORTED_QUERY;
         return false;
     }

     if (Set.of(RDAPHttpQueryType.DOMAIN, RDAPHttpQueryType.NAMESERVER).contains(queryType)) {
         String domainName = queryType.getValue(this.config.getUri().toString());
         System.out.println("----->  Validating domain name: " + domainName);

         // Check for mixed labels first
         if (hasMixedLabels(domainName)) {
             logger.error("Mixed label format detected in domain name: {}", domainName);
             status = ToolResult.MIXED_LABEL_FORMAT;
             return false;
         }

         String domainNameJson = String.format("{\"domain\": \"%s\"}", domainName);
         System.out.println("----->  Domain name JSON: " + domainNameJson);
         RDAPValidatorResults testDomainResults = RDAPValidatorResultsImpl.getInstance();
         SchemaValidator validator = new SchemaValidator("rdap_domain_name.json", testDomainResults,
             datasetService);
         if (!validator.validate(domainNameJson)) {
             System.out.println("----->  Domain name validation failed");
         } else {
             System.out.println("----->  Domain name validation passed");
         }
     }

     return true;
 }

  @Override
  public ToolResult getErrorStatus() {
    return this.status;
  }

  @Override
  public RDAPQueryType getQueryType() {
    return queryType.getQueryType();
  }

  /**
   * Checks if the domain name contains mixed A-labels and U-labels.
   * A-labels are prefixed with "xn--" and U-labels contain non-ASCII characters.
   * ASCII-only labels (like "example" or "com") are allowed with either type.
   *
   * @param domainName The domain name to check
   * @return true if the domain name contains mixed labels, false otherwise
   */
  public boolean hasMixedLabels(String domainName) {
    if (domainName == null || domainName.isEmpty()) {
      return false;
    }

    String[] labels = domainName.split("\\.");
    boolean hasALabel = false;
    boolean hasULabel = false;

    for (String label : labels) {
      if (label.toLowerCase().startsWith("xn--")) {
        hasALabel = true;
      } else if (!isAscii(label)) {
        hasULabel = true;
      }

      // If we found both types, we have mixed labels
      if (hasALabel && hasULabel) {
        logger.error("Domain name contains mixed A-labels and U-labels: {}", domainName);
        return true;
      }
    }

    return false;
  }

  /**
   * Checks if a string contains only ASCII characters.
   *
   * @param s The string to check
   * @return true if the string contains only ASCII characters, false otherwise
   */
  public boolean isAscii(String s) {
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) > 127) {
        return false;
      }
    }
    return true;
  }

  public enum RDAPHttpQueryType {
    DOMAIN(RDAPQueryType.DOMAIN, Pattern.compile("/domain/([^/]+)$")),
    NAMESERVER(RDAPQueryType.NAMESERVER, Pattern.compile("/nameserver/([^/]+)$")),
    ENTITY(RDAPQueryType.ENTITY, Pattern.compile("/entity/([^/]+)$")),
    AUTNUM(RDAPQueryType.AUTNUM, Pattern.compile("/autnum/([^/]+)$")),
    IP(RDAPQueryType.IP_NETWORK, Pattern.compile("/ip/([^/]+)$")),
    HELP(RDAPQueryType.HELP, Pattern.compile("/help$")),
    NAMESERVERS(RDAPQueryType.NAMESERVERS, Pattern.compile("/nameservers\\?ip=([^/]+)$"));

    private final RDAPQueryType queryType;
    private final Pattern pattern;

    RDAPHttpQueryType(RDAPQueryType queryType, Pattern pattern) {
      this.queryType = queryType;
      this.pattern = pattern;
    }

    public static RDAPHttpQueryType getType(String query) {
      for (RDAPHttpQueryType qt : RDAPHttpQueryType.values()) {
        Matcher matcher = qt.pattern.matcher(query);
        if (matcher.find()) {
          return qt;
        }
      }
      return null;
    }

    RDAPQueryType getQueryType() {
      return this.queryType;
    }

    public String getValue(String query) {
      Matcher matcher = this.pattern.matcher(query);
      if (matcher.find()) {
        return matcher.group(1);
      }
      return EMPTY_STRING;
    }
  }
}
