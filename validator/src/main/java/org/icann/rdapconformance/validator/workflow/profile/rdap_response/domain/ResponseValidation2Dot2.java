package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.EPPRoid;

public class ResponseValidation2Dot2 extends ProfileJsonValidation {

  private final RDAPDatasetService datasetService;
  private final RDAPQueryType queryType;

  public ResponseValidation2Dot2(String rdapResponse, RDAPValidatorResults results,
      RDAPDatasetService datasetService, RDAPQueryType queryType) {
    super(rdapResponse, results);
    this.datasetService = datasetService;
    this.queryType = queryType;
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_2_Validation";
  }

  @Override
  protected boolean doValidate() {
    String handle = jsonObject.getString("handle");
    if (!handle.matches("(\\w|_){1,80}-\\w{1,8}")) {
      results.add(RDAPValidationResult.builder()
          .code(-46200)
          .value(getResultValue("#/handle"))
          .message("The handle in the domain object does not comply with the format "
              + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.")
          .build());
      return false;
    }
    String roid = handle.substring(handle.indexOf("-") + 1);
    EPPRoid eppRoid = datasetService.get(EPPRoid.class);
    if (eppRoid.isInvalid(roid)) {
      results.add(RDAPValidationResult.builder()
          .code(-46201)
          .value(getResultValue("#/handle"))
          .message("The globally unique identifier in the domain object handle is not registered "
              + "in EPPROID.")
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
