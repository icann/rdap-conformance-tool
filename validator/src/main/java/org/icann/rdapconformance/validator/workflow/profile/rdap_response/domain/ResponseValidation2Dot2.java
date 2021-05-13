package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public class ResponseValidation2Dot2 extends HandleValidation {

  public ResponseValidation2Dot2(String rdapResponse, RDAPValidatorResults results,
      RDAPDatasetService datasetService, RDAPQueryType queryType) {
    super(rdapResponse, results, datasetService, queryType, -46200, "domain");
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_2_Validation";
  }

  @Override
  protected boolean doValidate() {
    return validateHandle("#/handle");
  }

}
