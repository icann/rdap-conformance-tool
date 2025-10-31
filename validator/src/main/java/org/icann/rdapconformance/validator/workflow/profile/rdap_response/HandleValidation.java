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
import org.icann.rdapconformance.validator.QueryContext;


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
  protected final QueryContext queryContext;

  // Constructor with QueryContext for production use
  public HandleValidation(QueryContext queryContext, int code, String objectName) {
    super(queryContext.getRdapResponseData(), queryContext.getResults());
    this.datasetService = queryContext.getDatasetService();
    this.queryType = queryContext.getQueryType();
    this.code = code;
    this.config = queryContext.getConfig();
    this.objectName = objectName;
    this.queryContext = queryContext;
  }

  // Deprecated constructor for testing
  @Deprecated
  public HandleValidation(RDAPValidatorConfiguration config, String rdapResponse, RDAPValidatorResults results,
                          RDAPDatasetService datasetService, RDAPQueryType queryType, int code, String objectName) {
    super(rdapResponse, results);
    this.datasetService = datasetService;
    this.queryType = queryType;
    this.code = code;
    this.config = config;
    this.objectName = objectName;
    this.queryContext = null; // For tests - QueryContext not available
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
      RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
          .code(code)
          .value(getResultValue(handleJsonPointer))
          .message(String.format("The handle in the %s object does not comply with the format "
              + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.", objectName));

      results.add(builder.build(queryContext));
      return false;
    }

    String roid = handle.substring(handle.indexOf(DASH) + 1);
    EPPRoid eppRoid = datasetService.get(EPPRoid.class);
    if (eppRoid.isInvalid(roid)) {
      RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
          .code(code - 1)  // CalculatedCode(s): -47601 (entities), -46201 (domain), -49103 (nameserver) -47202 (nameserver)
          .value(getResultValue(handleJsonPointer))
          .message(String.format("The globally unique identifier in the %s object handle is not "
              + "registered in EPPROID.", objectName));

      results.add(builder.build(queryContext));
      return false;
    }
    return true;
  }
}
