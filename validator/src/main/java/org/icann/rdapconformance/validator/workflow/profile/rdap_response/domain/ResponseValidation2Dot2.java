package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.HandleValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public final class ResponseValidation2Dot2 extends HandleValidation {

  // Modern QueryContext constructor
  public ResponseValidation2Dot2(QueryContext queryContext) {
    super(queryContext, -46200, "domain");
  }


  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_2_Validation";
  }

  @Override
  protected boolean doValidate() {
    return validateHandle("#/handle");
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.DOMAIN);
  }
}
