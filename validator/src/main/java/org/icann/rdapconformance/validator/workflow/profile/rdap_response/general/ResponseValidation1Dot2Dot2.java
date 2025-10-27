package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ResponseValidation1Dot2Dot2 extends ProfileJsonValidation {

  private static final Logger logger = LoggerFactory.getLogger(ResponseValidation1Dot2Dot2.class);
  public static final String WORD_MATCHED = "script";

  /*  (?i) enables case-insensitivity
      .* uses every character except line breaks
   */
  public static final String WORD_MATCHED_REGEX="<(?i).*" + WORD_MATCHED + ".*>";
  private static final Pattern WORD_MATCHED_PATTERN = Pattern.compile(WORD_MATCHED_REGEX);
  private static final ObjectMapper mapper = org.icann.rdapconformance.validator.workflow.JsonMapperUtil.getSharedMapper();
  private final String rdapResponse;

  public ResponseValidation1Dot2Dot2(QueryContext qctx) {
    super(qctx.getRdapResponseData(), qctx.getResults());
    this.rdapResponse = qctx.getRdapResponseData();
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_1_2_2_Validation";
  }

  @Override
  protected boolean doValidate() {
    final Matcher matcher = WORD_MATCHED_PATTERN.matcher(rdapResponse);
    if(matcher.find()) {
      addResult();
      return false;
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
