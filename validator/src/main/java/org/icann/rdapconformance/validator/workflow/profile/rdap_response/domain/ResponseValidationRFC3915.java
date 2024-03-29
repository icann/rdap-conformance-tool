package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import java.util.HashSet;
import java.util.Set;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public final class ResponseValidationRFC3915 extends ProfileJsonValidation {

  private final RDAPQueryType queryType;

  public ResponseValidationRFC3915(String rdapResponse,
      RDAPValidatorResults results,
      RDAPQueryType queryType) {
    super(rdapResponse, results);
    this.queryType = queryType;
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_rfc3915_Validation";
  }

  @Override
  protected boolean doValidate() {
    Set<String> status = new HashSet<>();
    jsonObject.optJSONArray("status").forEach(s -> status.add((String) s));

    status.remove("pending delete");
    if ((status.contains("redemption period") || status.contains("pending restore"))
        && status.size() > 1) {
      results.add(RDAPValidationResult.builder()
          .code(-47000)
          .value(getResultValue("#/status"))
          .message("The values of the status data structure does not comply with RFC3915.")
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
