package org.icann.rdapconformance.validator.workflow.profile.rdap_response;

import static org.json.JSONObject.NULL;

import com.ibm.icu.text.IDNA;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class QueryValidation extends ProfileJsonValidation {

  private static final Logger logger = LoggerFactory.getLogger(QueryValidation.class);

  protected final RDAPQueryType queryType;
  protected final int code;
  protected final String sectionName;
  private final RDAPValidatorConfiguration config;
  private final IDNA idna;
  private final QueryContext queryContext;


  // QueryContext constructor for production use
  public QueryValidation(QueryContext queryContext, String sectionName, int code) {
    super(queryContext.getRdapResponseData(), queryContext.getResults());
    this.config = queryContext.getConfig();
    this.queryType = queryContext.getQueryType();
    this.sectionName = sectionName;
    this.code = code;
    this.queryContext = queryContext;
    this.idna = IDNA.getUTS46Instance(IDNA.NONTRANSITIONAL_TO_ASCII
        | IDNA.NONTRANSITIONAL_TO_UNICODE
        | IDNA.CHECK_BIDI
        | IDNA.CHECK_CONTEXTJ
        | IDNA.CHECK_CONTEXTO
        | IDNA.USE_STD3_RULES);
  }

  // Deprecated constructor for tests
  @Deprecated
  public QueryValidation(String rdapResponse, RDAPValidatorResults results,
      RDAPValidatorConfiguration config, RDAPQueryType queryType, String sectionName, int code) {
    super(rdapResponse, results);
    this.config = config;
    this.queryType = queryType;
    this.sectionName = sectionName;
    this.code = code;
    this.queryContext = null; // Not available in deprecated constructor
    this.idna = IDNA.getUTS46Instance(IDNA.NONTRANSITIONAL_TO_ASCII
        | IDNA.NONTRANSITIONAL_TO_UNICODE
        | IDNA.CHECK_BIDI
        | IDNA.CHECK_CONTEXTJ
        | IDNA.CHECK_CONTEXTO
        | IDNA.USE_STD3_RULES);
  }

  @Override
  protected boolean doValidate() {
    boolean isValid = true;

    String domainName = config.getUri().toString()
        .substring(config.getUri().toString().lastIndexOf("/") + 1);
    StringBuilder ldhNameBuilder = new StringBuilder();
    IDNA.Info info = new IDNA.Info();
    idna.nameToASCII(domainName, ldhNameBuilder, info);
    if (info.hasErrors()) {
      logger.info("Invalid domain name");
      return false;
    }

    // Using equalsIgnoreCase based on nameToASCII method returns in lowercase domain name
    if (ldhNameBuilder.toString().equalsIgnoreCase(domainName)) {
      // URI contains only A-label or NR-LDH labels
      if (NULL.equals(jsonObject.opt("ldhName"))) {
        RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
            .code(code)
            .value(jsonObject.toString())
            .message(String.format("The RDAP Query URI contains only A-label or NR-LDH labels, "
                    + "the topmost %s object does not contain a ldhName member. "
                    + "See section %s of the RDAP_Response_Profile_2_1.",
                queryType.name().toLowerCase(), sectionName));

        results.add(builder.build(queryContext));
        isValid = false;
      }
    } else {
      // URI contains one or more U-label
      if (NULL.equals(jsonObject.opt("unicodeName"))) {
        RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
            .code(code - 1)  // CalculatedCode(s): -46101 (domain), -49101 (nameserver)
            .value(jsonObject.toString())
            .message(String.format("The RDAP Query URI contains one or more U-label, the topmost "
                    + "%s object does not contain a unicodeName member. "
                    + "See section %s of the RDAP_Response_Profile_2_1.",
                queryType.name().toLowerCase(), sectionName));

        results.add(builder.build(queryContext));
        isValid = false;
      }
    }

    return isValid;
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.DOMAIN);
  }
}
