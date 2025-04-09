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
    var statusError = 0;

    // Status -47000 was added into ignored list and changed for new codes -47001 and -47002
    if(!status.contains("pending delete")) {
      if (status.contains("redemption period")) {
        results.add(RDAPValidationResult.builder()
                .code(-47001)
                .value(getResultValue("#/status"))
                .message("'redemption period' is only valid with a status of 'pending delete'")
                .build());
        statusError ++;
      }

      if (status.contains("pending restore")) {
        results.add(RDAPValidationResult.builder()
                .code(-47002)
                .value(getResultValue("#/status"))
                .message("'pending restore' is only valid with a status of 'pending delete'")
                .build());
        statusError ++;
      }

      if(statusError > 0) {
        return false;
      }
    }

    return true;
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.DOMAIN);
  }
}
