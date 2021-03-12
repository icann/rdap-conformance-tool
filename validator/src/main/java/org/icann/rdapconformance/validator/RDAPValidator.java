package org.icann.rdapconformance.validator;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.ConfigurationFileParser;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.validators.Validator;
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

    RDAPValidatorContext context= new RDAPValidatorContext(configurationFile);

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
      // TODO domainNameValidation
    }

    /* Connect to the RDAP server, and if there is a connection error, exit with the corresponding
     * return code.
     */
    HttpRequest request = HttpRequest.newBuilder()
        .uri(this.config.getUri())
        .timeout(Duration.of(this.config.getTimeout(), SECONDS))
        .GET()
        .build();
    HttpResponse<String> httpResponse;
    try {
      httpResponse = HttpClient.newBuilder()
          .connectTimeout(Duration.of(this.config.getTimeout(), SECONDS))
          // TODO
          //        .followRedirects()
          .build()
          .send(request, HttpResponse.BodyHandlers.ofString());
      // TODO
//    } catch (IOException e) {
//      e.printStackTrace();
//    } catch (InterruptedException e) {
//      e.printStackTrace();
    } catch (Exception e) {
      return RDAPValidationStatus.CONNECTION_FAILED.getValue();
    }

    /* If a response is available to the tool, and the header Content-Type is not
     * application/rdap+JSON, exit with a return code of 5.
     */
    /* TODO */

    /*  If a response is available to the tool, but it's not syntactically valid JSON object, exit
     * with a return code of 6.
     */
    /* TODO */

    /* If a response is available to the tool, but the HTTP status code is not 200 nor 404, exit
     * with a return code of 7.
     */
    /* TODO */

    /* If a response is available to the tool, but the expected objectClassName in the topmost
     * object was not found for a lookup query (i.e. domain/<domain name>,
     * nameserver/<nameserver name> and entity/<handle>) nor the expected JSON array
     * (i.e. nameservers?ip=<nameserver search pattern>, just the JSON array should exist,
     * not validation on the contents) for a search query, exit with an return code of 8.
     */
    /* TODO */

    /*  If the HTTP status code is 404, perform Error Response Body
     * [stdRdapErrorResponseBodyValidation].
     */
    /* TODO */

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
    /* TODO */
    String rdapResponse = httpResponse.body();
    Validator validator = null;
    if (RDAPQueryType.DOMAIN.equals(queryType)) {
      validator = context.getValidator("stdRdapDomainLookupValidation");
    }
    // TODO elif...
    assert null != validator;
    validator.validate(rdapResponse);

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

    public String getValue(String query) {
      Matcher matcher = this.pattern.matcher(query);
      if (matcher.find()) {
        return matcher.group(1);
      }
      return "";
    }
  }

}
