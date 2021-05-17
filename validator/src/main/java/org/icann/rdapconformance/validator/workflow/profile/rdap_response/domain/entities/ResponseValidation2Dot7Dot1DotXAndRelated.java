package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

/**
 * 8.8.1.1
 */
public abstract class ResponseValidation2Dot7Dot1DotXAndRelated extends
    EntitiesWithinDomainProfileJsonValidation {

  public ResponseValidation2Dot7Dot1DotXAndRelated(String rdapResponse,
      RDAPValidatorResults results,
      RDAPQueryType queryType,
      RDAPValidatorConfiguration config) {
    super(rdapResponse, results, queryType, config);
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_7_1_X_and_2_7_2_X_and_2_7_3_X_and_2_7_4_X_Validation";
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.DOMAIN)
        && ((config.isGtldRegistry() && !config.isThin())
        || config.isGtldRegistrar());
  }
}
