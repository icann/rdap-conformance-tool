package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;

public final class ResponseValidation2Dot6Dot1 extends ProfileJsonValidation {

  private final RDAPQueryType queryType;

  public ResponseValidation2Dot6Dot1(String rdapResponse,
      RDAPValidatorResults results,
      RDAPQueryType queryType) {
    super(rdapResponse, results);
    this.queryType = queryType;
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_6_1_Validation";
  }

  @Override
  protected boolean doValidate() {
    JSONArray status = jsonObject.optJSONArray("status");

    if (null == status || status.isEmpty()) {
      results.add(RDAPValidationResult.builder()
          .code(-47100)
          .value(getResultValue("#/status"))
          .message("The status member does not contain at least one value.")
          .build());
      return false;
    }
    return true;
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.DOMAIN);
  }
}
