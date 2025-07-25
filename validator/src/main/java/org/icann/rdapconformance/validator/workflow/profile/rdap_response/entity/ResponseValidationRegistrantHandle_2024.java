package org.icann.rdapconformance.validator.workflow.profile.rdap_response.entity;

import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.EPPRoid;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

import static org.icann.rdapconformance.validator.CommonUtils.DASH;

public final class ResponseValidationRegistrantHandle_2024 extends ProfileJsonValidation {
  private static final Logger logger = LoggerFactory.getLogger(ResponseValidationRegistrantHandle_2024.class);
  public static final String ENTITY_ROLE_PATH = "$.entities[?(@.roles contains 'registrant')]";
  public static final String ENTITY_REGISTRANT_PATH = "$.entities[?(@.roles[0]=='registrant')]";
  private static final String REDACTED_PATH = "$.redacted[*]";
  private Set<String> redactedPointersValue = null;
  private final RDAPDatasetService datasetService;

  public ResponseValidationRegistrantHandle_2024(String rdapResponse,
                                                 RDAPValidatorResults results,
                                                 RDAPDatasetService datasetService) {
    super(rdapResponse, results);
    this.datasetService = datasetService;
  }


  @Override
  public String getGroupName() {
    return "rdapResponseProfile_registrant_handle_Validation";
  }

  public boolean doValidate() {
    return validateEntityPropertyObject();
  }

  private boolean validateEntityPropertyObject() {
    if(getPointerFromJPath(ENTITY_ROLE_PATH).isEmpty()) {
      return true;
    }

    try {
      Set<String> entityHandleJsonPointers = getPointerFromJPath(ENTITY_REGISTRANT_PATH);

      if(entityHandleJsonPointers.isEmpty()) {
        logger.info("Handle is NOT in the entity object with the registrant, redacted validations");
        return validateRedactedArrayForHandle();
      }

      for (String jsonPointer : entityHandleJsonPointers) {
        JSONObject entity = (JSONObject) jsonObject.query(jsonPointer);
        if(entity.get("handle") instanceof String handle) {
          if (!handle.matches(CommonUtils.HANDLE_PATTERN)) {
            results.add(RDAPValidationResult.builder()
                    .code(-63100)
                    .value(getResultValue(entityHandleJsonPointers))
                    .message("The handle of the registrant does not comply with the format "
                            + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.")
                    .build());
            return false;
          } else {
            String roid = handle.substring(handle.indexOf(DASH) + 1);
            EPPRoid eppRoid = datasetService.get(EPPRoid.class);
            if (eppRoid.isInvalid(roid)) {
              results.add(RDAPValidationResult.builder()
                      .code(-63101)
                      .value(getResultValue(entityHandleJsonPointers))
                      .message("The globally unique identifier in the registrant handle is not registered in EPPROID.")
                      .build());
              return false;
            }
          }
        }
      }
    } catch (Exception e) {
      logger.info("Entity handle is not found, next validations, Error: {}", e.getMessage());
      return validateRedactedArrayForHandle();
    }

    return true;
  }

  private boolean validateRedactedArrayForHandle() {
    JSONObject redactedHandleName = null;
    redactedPointersValue = getPointerFromJPath(REDACTED_PATH);
    for (String redactedJsonPointer : redactedPointersValue) {
      JSONObject redacted = (JSONObject) jsonObject.query(redactedJsonPointer);
      JSONObject name = (JSONObject) redacted.get("name");
      try {
        var nameValue = name.get("type");
        if(nameValue instanceof String redactedName) {
          if(redactedName.trim().equalsIgnoreCase("Registry Registrant ID")) {
            redactedHandleName = redacted;
          }
        }
      } catch (Exception e) {
        logger.info("Extract type from name is not possible by {}", e.getMessage());
        results.add(RDAPValidationResult.builder()
                .code(-63102)
                .value(getResultValue(redactedPointersValue))
                .message("a redaction of type Registry Registrant ID is required.")
                .build());

        return false;
      }

    }

    if(Objects.isNull(redactedHandleName)) {
      results.add(RDAPValidationResult.builder()
              .code(-63102)
              .value(getResultValue(redactedPointersValue))
              .message("a redaction of type Registry Registrant ID is required.")
              .build());

      return false;
    }

    return validateRedactedProperties(redactedHandleName);
  }

  private boolean validateRedactedProperties(JSONObject redactedHandleName) {
    if(Objects.isNull(redactedHandleName)) {
      logger.info("redactedHandleName object is null");
      return true;
    }

    Object pathLangValue;

    // If the pathLang property is either absent or is present as a JSON string of “jsonpath” verify prePath
    try {
      logger.info("Extracting pathLang...");
      pathLangValue = redactedHandleName.get("pathLang");
      if(pathLangValue instanceof String pathLang) {
        if (pathLang.trim().equalsIgnoreCase("jsonpath")) {
          return validatePostPathBasedOnPathLang(redactedHandleName);
        }
      }
      return true;
    } catch (Exception e) {
      logger.error("pathLang is not found due to {}", e.getMessage());
      return validatePostPathBasedOnPathLang(redactedHandleName);
    }
  }

  // Verify that the prePath property is either absent or is present with a valid JSONPath expression.
  private boolean validatePostPathBasedOnPathLang(JSONObject redactedRegistrantName) {
    if(Objects.isNull(redactedRegistrantName)) {
      logger.info("redactedRegistrantName object for postPath validations is null");
      return true;
    }

    try {
      var prePathValue = redactedRegistrantName.get("prePath");
      logger.info("prePath property is found, so verify value");
      if(prePathValue instanceof String prePath) {
        try {
          if(!isValidJsonPath(prePath)) {
            logger.info("prePath is not a valid JSONPath expression");
            results.add(RDAPValidationResult.builder()
                    .code(-63103)
                    .value(getResultValue(redactedPointersValue))
                    .message("jsonpath is invalid for Registry Registrant ID.")
                    .build());
            return false;
          }

          var prePathPointer = getPointerFromJPath(prePath);
          logger.info("prePath pointer with size {}", prePathPointer.size());

        } catch (Exception e) {
          logger.info("prePath is not a valid JSONPath expression, Error: {}", e.getMessage());
          results.add(RDAPValidationResult.builder()
                  .code(-63103)
                  .value(getResultValue(redactedPointersValue))
                  .message("jsonpath is invalid for Registry Registrant ID.")
                  .build());
          return false;
        }
      }
    } catch (Exception e) {
      logger.error("prePath property is not found, no validations defined. Error: {}", e.getMessage());
    }

    return true;
  }
}
