package org.icann.rdapconformance.validator.workflow.profile.rdap_response;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.EPPRoid;

public abstract class HandleValidation extends ProfileJsonValidation {

  private final RDAPDatasetService datasetService;
  protected final RDAPQueryType queryType;
  final int code;

  public HandleValidation(String rdapResponse, RDAPValidatorResults results,
      RDAPDatasetService datasetService, RDAPQueryType queryType, int code) {
    super(rdapResponse, results);
    this.datasetService = datasetService;
    this.queryType = queryType;
    this.code = code;
  }

  protected boolean validateHandle(String handleJsonPointer) {
    String handle = (String) jsonObject.query(handleJsonPointer);

    if (!handle.matches("(\\w|_){1,80}-\\w{1,8}")) {
      results.add(RDAPValidationResult.builder()
          .code(code)
          .value(getResultValue(handleJsonPointer))
          .message(String.format("The handle in the entity object does not comply with the format "
              + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730."))
          .build());
      return false;
    }
    String roid = handle.substring(handle.indexOf("-") + 1);
    EPPRoid eppRoid = datasetService.get(EPPRoid.class);
    if (eppRoid.isInvalid(roid)) {
      results.add(RDAPValidationResult.builder()
          .code(code - 1)
          .value(getResultValue(handleJsonPointer))
          .message(String.format("The globally unique identifier in the %s object handle is not "
              + "registered in EPPROID.", queryType.name().toLowerCase()))
          .build());
      return false;
    }
    return true;
  }
}
