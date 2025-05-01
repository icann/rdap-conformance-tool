package org.icann.rdapconformance.validator.workflow.profile.rdap_response;

import static org.icann.rdapconformance.validator.CommonUtils.HYPHEN;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.EPPRoid;

public abstract class HandleValidation extends ProfileJsonValidation {

  public static final String ICANNRST = "ICANNRST";
  private final RDAPDatasetService datasetService;
  protected final RDAPQueryType queryType;
  private RDAPValidatorConfiguration config;
  final int code;

  public HandleValidation(RDAPValidatorConfiguration config, String rdapResponse, RDAPValidatorResults results,
                          RDAPDatasetService datasetService, RDAPQueryType queryType, int code) {
    super(rdapResponse, results);
    this.datasetService = datasetService;
    this.queryType = queryType;
    this.code = code;
    this.config = config;
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

    if (handle == null || !handle.matches("(\\w|_){1,80}-\\w{1,8}")) {
      results.add(RDAPValidationResult.builder()
          .code(code)
          .value(getResultValue(handleJsonPointer))
          .message(String.format("The handle in the entity object does not comply with the format "
              + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730."))
          .build());
      return false;
    }

    String roid = handle.substring(handle.indexOf(HYPHEN) + 1);
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

    if (roid.contains(ICANNRST) && this.queryType.equals(RDAPQueryType.NAMESERVER) && this.config.useRdapProfileFeb2024()) {
      results.add(RDAPValidationResult.builder()
                                      .code(-49104)
                                      .value(getResultValue(handleJsonPointer))
                                      .message(
                                          "The globally unique identifier in the nameserver object handle is using an EPPROID reserved for testing by ICANN.")
                                      .build());
      return false;
    }


    if (roid.endsWith(ICANNRST) && this.queryType.equals(RDAPQueryType.DOMAIN) && this.config.useRdapProfileFeb2024()) {
      results.add(RDAPValidationResult.builder()
                                      .code(-46202)
                                      .value(getResultValue(handleJsonPointer))
                                      .message(
                                          "The globally unique identifier in the domain object handle is using an EPPROID reserved for testing by ICANN.")
                                      .build());
      return false;
    }

    return true;
  }
}
