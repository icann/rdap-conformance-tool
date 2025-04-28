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
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationStatus;
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

  @Override
  public boolean check(
      RDAPDatasetService datasetService) {
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
    queryType = RDAPHttpQueryType.getType(this.config.getUri().toString());
    if (queryType == null) {
      logger.error("Unknown RDAP query type for URI {}", this.config.getUri());
      status = ToolResult.UNSUPPORTED_QUERY;
      return false;
    }
    if (Set.of(RDAPHttpQueryType.DOMAIN, RDAPHttpQueryType.NAMESERVER).contains(queryType)) {
      String domainName = queryType.getValue(this.config.getUri().toString());
      String domainNameJson = String.format("{\"domain\": \"%s\"}", domainName);
      RDAPValidatorResults testDomainResults = RDAPValidatorResultsImpl.getInstance();
      SchemaValidator validator = new SchemaValidator("rdap_domain_name.json", testDomainResults,
          datasetService);
      if (!validator.validate(domainNameJson)) {
        // TODO check if A-labels and U-labels are mixed: is this OK?
        if (testDomainResults.getAll().stream()
            .map(RDAPValidationResult::getCode)
            .anyMatch(c -> c == -10303)) {
          status =  ToolResult.MIXED_LABEL_FORMAT;
          return false;
        }
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


  private enum RDAPHttpQueryType {
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

    static RDAPHttpQueryType getType(String query) {
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
