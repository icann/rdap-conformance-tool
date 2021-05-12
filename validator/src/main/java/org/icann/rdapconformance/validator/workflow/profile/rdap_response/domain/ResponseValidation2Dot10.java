package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public class ResponseValidation2Dot10 extends ProfileJsonValidation {

  private final RDAPQueryType queryType;

  public ResponseValidation2Dot10(String rdapResponse,
      RDAPValidatorResults results,
      RDAPQueryType queryType) {
    super(rdapResponse, results);
    this.queryType = queryType;
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_notices_included_Validation";
  }

  @Override
  protected boolean doValidate() {
    if (getPointerFromJPath("$.secureDNS").isEmpty()) {
      results.add(RDAPValidationResult.builder()
          .code(-46800)
          .value(jsonObject.toString())
          .message("A secureDNS member does not appear in the domain object.")
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
