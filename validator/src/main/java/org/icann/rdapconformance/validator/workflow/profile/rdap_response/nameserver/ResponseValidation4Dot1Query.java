package org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.QueryValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public final class ResponseValidation4Dot1Query extends QueryValidation {

  public ResponseValidation4Dot1Query(String rdapResponse, RDAPValidatorResults results,
      RDAPValidatorConfiguration config, RDAPQueryType queryType) {
    super(rdapResponse, results, config, queryType, "4.1", -49100);
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_4_1_Validation";
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.NAMESERVER);
  }
}
