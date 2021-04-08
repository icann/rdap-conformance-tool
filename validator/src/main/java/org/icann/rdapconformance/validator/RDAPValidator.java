package org.icann.rdapconformance.validator;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.ConfigurationFileParser;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.RDAPHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDAPValidator {

  private static final Logger logger = LoggerFactory.getLogger(RDAPValidator.class);

  private final RDAPValidatorConfiguration config;


  public RDAPValidator(RDAPValidatorConfiguration config) {
    this.config = config;
    if (!this.config.check()) {
      logger.error("Please fix the configuration");
      throw new RuntimeException("Please fix the configuration");
    }
  }

  public int validate() {
    /* Parse the configuration definition file, and if the file is not parsable,
     * exit with a return code of 1.
     */
    ConfigurationFile configurationFile;
    try {
      ConfigurationFileParser configParser = new ConfigurationFileParser();
      configurationFile = configParser.parse(this.config.getConfigurationFile());
    } catch (Exception e) {
      logger.error("Configuration is invalid", e);
      return RDAPValidationStatus.CONFIG_INVALID.getValue();
    }

    RDAPValidatorContext context = new RDAPValidatorContext(configurationFile);

    /* If the parameter (--use-local-datasets) is set, use the datasets found in the filesystem,
     * download the datasets not found in the filesystem, and persist them in the filesystem.
     * If the parameter (--use-local-datasets) is not set, download all the datasets, and
     * overwrite the datasets in the filesystem.
     * If one or more datasets cannot be downloaded, exit with a return code of 2.
     */
    /* TODO */

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
    RDAPQueryType queryType = RDAPQueryType.getType(this.config.getUri().toString());
    if (queryType == null) {
      logger.error("Unknown RDAP query type for URI {}", this.config.getUri());
      return RDAPValidationStatus.UNSUPPORTED_QUERY.getValue();
    }
    if (Set.of(RDAPQueryType.DOMAIN, RDAPQueryType.NAMESERVER).contains(queryType)) {
      String domainName = queryType.getValue(this.config.getUri().toString());
      String domainNameJson = String.format("{\"domain\": \"%s\"}", domainName);
      SchemaValidator validator = new SchemaValidator("rdap_domain_name.json", context);
      if (!validator.validate(domainNameJson)) {
        // TODO check if A-labels and U-labels are mixed: is this OK?
        if (context.getResults().stream().map(RDAPValidationResult::getCode)
            .anyMatch(c -> c == -10303)) {
          return RDAPValidationStatus.MIXED_LABEL_FORMAT.getValue();
        }
      }
    }

    /*
     * Connect to the RDAP server, and if there is a connection error, exit with the corresponding
     * return code.
     */
    RDAPHttpResponse rdapHttpResponse = new RDAPHttpResponse(config);
    if (rdapHttpResponse.hasError()) {
      return rdapHttpResponse.getErrorStatus().getValue();
    }

    /*
     * If a response is available to the tool, but the expected objectClassName in the topmost
     * object was not found for a lookup query (i.e. domain/<domain name>,
     * nameserver/<nameserver name> and entity/<handle>) nor the expected JSON array
     * (i.e. nameservers?ip=<nameserver search pattern>, just the JSON array should exist,
     * not validation on the contents) for a search query, exit with an return code of 8.
     */
    if (rdapHttpResponse.getHttpStatusCode() == 200) {
      if (queryType.isLookupQuery() && !rdapHttpResponse.jsonResponseHasKey("objectClassName")) {
        logger.error("objectClassName was not found in the topmost object");
        return RDAPValidationStatus.EXPECTED_OBJECT_NOT_FOUND.getValue();
      } else if (queryType.equals(RDAPQueryType.NAMESERVERS) && !rdapHttpResponse
          .jsonResponseIsArray()) {
        logger.error("No JSON array in answer");
        return RDAPValidationStatus.EXPECTED_OBJECT_NOT_FOUND.getValue();
      }
    }

    /*  If the HTTP status code is 404, perform Error Response Body
     * [stdRdapErrorResponseBodyValidation].
     */

    /* If
     * the RDAP query is domain/<domain name>, perform Domain Lookup Validation
     * [stdRdapDomainLookupValidation].
     * the RDAP query is nameserver/<nameserver name>, perform Nameserver lookup validation
     * [stdRdapNameserverLookupValidation].
     * the RDAP query is entity/<handle>, perform Entity lookup validation
     * [stdRdapEntityLookupValidation] if the flag --thin is not set. If the flag --thin is set,
     * exit with a return code of 9.
     * the RDAP query is help, perform Help validation [stdRdapHelpValidation].
     * the RDAP query is nameservers?ip=<nameserver search pattern>, perform Nameservers search
     * validation [stdRdapNameserversSearchValidation].
     *
     *
     */
    context.reset();  // reset context as we may have performed some preliminary validation
    SchemaValidator validator = null;
    if (404 == rdapHttpResponse.getHttpStatusCode()) {
      validator = new SchemaValidator("rdap_error.json", context);
    } else if (RDAPQueryType.DOMAIN.equals(queryType)) {
      validator = new SchemaValidator("rdap_domain.json", context);
    } else if (RDAPQueryType.HELP.equals(queryType)) {
      validator = new SchemaValidator("rdap_help.json", context);
    } else if (RDAPQueryType.NAMESERVER.equals(queryType)) {
      validator = new SchemaValidator("rdap_nameserver.json", context);
    } else if (RDAPQueryType.NAMESERVERS.equals(queryType)) {
      validator = new SchemaValidator("rdap_nameservers.json", context);
    } else if (RDAPQueryType.ENTITY.equals(queryType)) {
      validator = new SchemaValidator("rdap_entities.json", context);
    }
    assert null != validator;
    validator.validate(rdapHttpResponse.getHttpResponseBody());

    // TODO create result file in context

    /*
     * Additionally, apply the relevant collection tests when the option
     * --use-rdap-profile-february-2019 is set.
     */
    /* TODO */

    return RDAPValidationStatus.SUCCESS.getValue();
  }


  private enum RDAPQueryType {
    DOMAIN(Pattern.compile("/domain/([^/]+)$")),
    NAMESERVER(Pattern.compile("/nameserver/([^/]+)$")),
    ENTITY(Pattern.compile("/entity/([^/]+)$")),
    HELP(Pattern.compile("/help$")),
    NAMESERVERS(Pattern.compile("/nameservers?ip=([^/]+)$"));

    private final Pattern pattern;

    RDAPQueryType(Pattern pattern) {
      this.pattern = pattern;
    }

    static RDAPQueryType getType(String query) {
      for (RDAPQueryType qt : RDAPQueryType.values()) {
        Matcher matcher = qt.pattern.matcher(query);
        if (matcher.find()) {
          return qt;
        }
      }
      return null;
    }

    public boolean isLookupQuery() {
      return Set.of(RDAPQueryType.DOMAIN, RDAPQueryType.NAMESERVER, RDAPQueryType.ENTITY)
          .contains(this);
    }

    public String getValue(String query) {
      Matcher matcher = this.pattern.matcher(query);
      if (matcher.find()) {
        return matcher.group(1);
      }
      return "";
    }
  }

}
