package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.QueryValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public final class ResponseValidation2Dot1 extends QueryValidation {

  public ResponseValidation2Dot1(String rdapResponse, RDAPValidatorResults results,
      RDAPValidatorConfiguration config, RDAPQueryType queryType) {
    super(rdapResponse, results, config, queryType, "2.1", -46100);
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_1_Validation";
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.DOMAIN);
  }
}
