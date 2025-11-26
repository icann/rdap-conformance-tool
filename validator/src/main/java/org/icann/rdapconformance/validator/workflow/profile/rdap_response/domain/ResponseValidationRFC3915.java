package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public final class ResponseValidationRFC3915 extends ProfileJsonValidation {
  private final RDAPQueryType queryType;
  private final QueryContext queryContext;

  public ResponseValidationRFC3915(QueryContext qctx) {
    super(qctx.getRdapResponseData(), qctx.getResults());
    this.queryType = qctx.getQueryType();
    this.queryContext = qctx;
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_rfc3915_Validation";
  }

  @Override
  protected boolean doValidate() {
    Set<String> status = new HashSet<>();
    JSONArray statusArray = jsonObject.optJSONArray("status");
    if (statusArray != null) {
      statusArray.forEach(s -> status.add((String) s));
    }
    var statusError = 0;

    // Status -47000 was added into ignored list and changed for new codes -47001 and -47002
    if(!status.contains("pending delete")) {
      if (status.contains("redemption period")) {
        results.add(RDAPValidationResult.builder()
                .code(-47001)
                .value(getResultValue("#/status"))
                .message("'redemption period' is only valid with a status of 'pending delete'")
                .build(queryContext));
        statusError ++;
      }

      if (status.contains("pending restore")) {
        results.add(RDAPValidationResult.builder()
                .code(-47002)
                .value(getResultValue("#/status"))
                .message("'pending restore' is only valid with a status of 'pending delete'")
                .build(queryContext));
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
