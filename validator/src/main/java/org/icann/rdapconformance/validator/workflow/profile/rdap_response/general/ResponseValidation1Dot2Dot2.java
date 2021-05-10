package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.StringEscapeUtils;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResponseValidation1Dot2Dot2 extends ProfileJsonValidation {

  private static final Logger logger = LoggerFactory.getLogger(ResponseValidation1Dot2Dot2.class);
  private static final ObjectMapper mapper = new ObjectMapper();
  private final String rdapResponse;

  public ResponseValidation1Dot2Dot2(String rdapResponse, RDAPValidatorResults results) {
    super(rdapResponse, results);
    this.rdapResponse = rdapResponse;
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_1_2_2_Validation";
  }

  @Override
  protected boolean doValidate() {
    PolicyFactory policy = new HtmlPolicyBuilder().toFactory();
    String rdapResponseSanitized = StringEscapeUtils.unescapeHtml4(policy.sanitize(rdapResponse));
    try {
      JsonNode beforeSanitizing = mapper.readTree(rdapResponse);
      JsonNode afterSanitizing;
      try {
        afterSanitizing = mapper.readTree(rdapResponseSanitized);
      } catch (JsonParseException e) {
        addResult();
        return false;
      }
      if (!beforeSanitizing.equals(afterSanitizing)) {
        addResult();
        return false;
      }
    } catch (JsonProcessingException e) {
      logger.error(
          "Exception when making HTTP request in order to check [tigSection_1_2_Validation]", e);
    }
    return true;
  }

  private void addResult() {
    results.add(RDAPValidationResult.builder()
        .code(-40100)
        .value(rdapResponse)
        .message("The RDAP response contains browser executable code (e.g., JavaScript). "
            + "See section 1.2.2 of the RDAP_Response_Profile_2_1.")
        .build());
  }

}
