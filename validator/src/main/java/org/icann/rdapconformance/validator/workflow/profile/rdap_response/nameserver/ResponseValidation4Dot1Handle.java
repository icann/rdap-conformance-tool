package org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.HandleValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public final class ResponseValidation4Dot1Handle extends HandleValidation {

  public ResponseValidation4Dot1Handle(RDAPValidatorConfiguration config, String rdapResponse, RDAPValidatorResults results,
                                       RDAPDatasetService datasetService, RDAPQueryType queryType) {
    super(config, rdapResponse, results, datasetService, queryType, -49102);
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_4_1_Validation";
  }

  @Override
  protected boolean doValidate() {
    return validateHandle("#/handle");
  }

   @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.NAMESERVER);
  }
}
