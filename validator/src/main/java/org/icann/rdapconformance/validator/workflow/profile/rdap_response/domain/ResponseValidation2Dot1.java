package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.json.JSONObject.NULL;

import com.ibm.icu.text.IDNA;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseValidation2Dot1 extends ProfileJsonValidation {

  private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot1.class);

  private final RDAPValidatorConfiguration config;
  private final RDAPQueryType queryType;
  private final IDNA idna;

  public ResponseValidation2Dot1(String rdapResponse, RDAPValidatorResults results,
      RDAPValidatorConfiguration config, RDAPQueryType queryType) {
    super(rdapResponse, results);
    this.config = config;
    this.queryType = queryType;
    this.idna = IDNA.getUTS46Instance(IDNA.NONTRANSITIONAL_TO_ASCII
        | IDNA.NONTRANSITIONAL_TO_UNICODE
        | IDNA.CHECK_BIDI
        | IDNA.CHECK_CONTEXTJ
        | IDNA.CHECK_CONTEXTO
        | IDNA.USE_STD3_RULES);
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_1_Validation";
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
      logger.error("Invalid domain name");
      return false;
    }
    if (ldhNameBuilder.toString().equals(domainName)) {
      // URI contains only A-label or NR-LDH labels
      if (NULL.equals(jsonObject.opt("ldhName"))) {
        results.add(RDAPValidationResult.builder()
            .code(-46100)
            .value(jsonObject.toString())
            .message("The RDAP Query URI contains only A-label or NR-LDH labels, the topmost "
                + "domain object does not contain a ldhName member. "
                + "See section 2.1 of the RDAP_Response_Profile_2_1.")
            .build());
        isValid = false;
      }
    } else {
      // URI contains one or more U-label
      if (NULL.equals(jsonObject.opt("unicodeName"))) {
        results.add(RDAPValidationResult.builder()
            .code(-46101)
            .value(jsonObject.toString())
            .message("The RDAP Query URI contains one or more U-label, the topmost domain "
                + "object does not contain a unicodeName member. "
                + "See section 2.1 of the RDAP_Response_Profile_2_1.")
            .build());
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
