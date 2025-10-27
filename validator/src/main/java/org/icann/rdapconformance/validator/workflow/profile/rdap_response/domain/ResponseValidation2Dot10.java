package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public final class ResponseValidation2Dot10 extends ProfileJsonValidation {

  private final RDAPQueryType queryType;

  public ResponseValidation2Dot10(QueryContext qctx) {
    super(qctx.getRdapResponseData(), qctx.getResults());
    this.queryType = qctx.getQueryType();
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

    if (getPointerFromJPath("$.secureDNS.delegationSigned").isEmpty()) {
      results.add(RDAPValidationResult.builder()
          .code(-46801)
          .value(jsonObject.toString())
          .message("The delegationSigned element does not exist.")
          .build());
      return false;
    }

    JSONObject secureDNS = jsonObject.getJSONObject("secureDNS");
    if (secureDNS.getBoolean("delegationSigned") &&
        !secureDNS.has("dsData") &&
        !secureDNS.has("keyData")) {
      results.add(RDAPValidationResult.builder()
          .code(-46802)
          .value(jsonObject.toString())
          .message("delegationSigned value is true, but no dsData nor keyData "
              + "name/value pair exists.")
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
