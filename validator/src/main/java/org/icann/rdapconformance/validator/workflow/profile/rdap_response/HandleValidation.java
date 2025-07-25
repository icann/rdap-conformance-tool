package org.icann.rdapconformance.validator.workflow.profile.rdap_response;

import static org.icann.rdapconformance.validator.CommonUtils.DASH;

import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.EPPRoid;


/**
 * Used by the following validations:
 *  ResponseValidation2Dot2
 *  ResponseValidation2Dot7Dot1DotXAndRelated3And4 (via SimpleHandleValidation)
 *  ResponseValidation2Dot9Dot1And2Dot9Dot2
 *  ResponseValidation4Dot1Handle
 */
public abstract class HandleValidation extends ProfileJsonValidation {

  private final RDAPDatasetService datasetService;
  protected final RDAPQueryType queryType;
  protected final RDAPValidatorConfiguration config;
  final int code;
  final String objectName;

  public HandleValidation(RDAPValidatorConfiguration config, String rdapResponse, RDAPValidatorResults results,
                          RDAPDatasetService datasetService, RDAPQueryType queryType, int code, String objectName) {
    super(rdapResponse, results);
    this.datasetService = datasetService;
    this.queryType = queryType;
    this.code = code;
    this.config = config;
    this.objectName = objectName;
  }

  protected boolean validateHandle(String handleJsonPointer) {
    String handle = null;

    Object obj = jsonObject.query(handleJsonPointer);
    if (obj != null) {
      // have to use .toString() instead of cast (String),
      // because if the value is JSONObject.NULL, it won't cast
      // added testValidate_HandleIsNull_AddErrorCode unit test for this
      handle = obj.toString();
    }

    if (handle == null || !handle.matches(CommonUtils.HANDLE_PATTERN)) {
      results.add(RDAPValidationResult.builder()
          .code(code)
          .value(getResultValue(handleJsonPointer))
          .message(String.format("The handle in the %s object does not comply with the format "
              + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.", objectName))
          .build());
      return false;
    }

    String roid = handle.substring(handle.indexOf(DASH) + 1);
    EPPRoid eppRoid = datasetService.get(EPPRoid.class);
    if (eppRoid.isInvalid(roid)) {
      results.add(RDAPValidationResult.builder()
          .code(code - 1)
          .value(getResultValue(handleJsonPointer))
          .message(String.format("The globally unique identifier in the %s object handle is not "
              + "registered in EPPROID.", objectName))
          .build());
      return false;
    }
    return true;
  }
}
