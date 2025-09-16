package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.apache.commons.lang3.StringUtils;
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

public final class ResponseValidation2Dot2_1_2024 extends ProfileJsonValidation {

  private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot2_1_2024.class);
  public static final String HANDLE_PATH = "#/handle";
  private static final String REDACTED_PATH = "$.redacted[*]";
  private Set<String> redactedPointersValue = null;
  private final RDAPDatasetService datasetService;

  public ResponseValidation2Dot2_1_2024(String rdapResponse, RDAPValidatorResults results,
                                        RDAPDatasetService datasetService) {
    super(rdapResponse, results);
    this.datasetService = datasetService;
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_2_1_Validation";
  }

  @Override
  protected boolean doValidate() {
    boolean isValid = true;
      var handleObject = validateHandleInTopMostObject();
      isValid = handleObject.isValid();
      if(StringUtils.isBlank(handleObject.handleValue()) && isValid) {
          var redactedObject = validateHandleInRedactedObject();
          isValid = redactedObject.isValid();
          if(isValid) {
              isValid = validateRedactedProperties(redactedObject);
              if(isValid) {
                  isValid = validateMethodProperty(redactedObject);
              }
          }
      }

    return isValid;
  }

 private HandleObjectToValidate validateHandleInTopMostObject() {
      String handleValue = null;
        try {
           var handleObject = jsonObject.get("handle");
           if(handleObject instanceof String handle) {
               handleValue = handle;
               if (!handle.matches(CommonUtils.HANDLE_PATTERN)) {
                   results.add(RDAPValidationResult.builder()
                           .code(-46200)
                           .value(getResultValue(HANDLE_PATH))
                           .message(String.format("The handle in the domain object does not comply with the format "
                                   + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730."))
                           .build());
                   return new HandleObjectToValidate(handle, false);
               } else {
                   String roid = handle.substring(handle.indexOf(DASH) + 1);
                   EPPRoid eppRoid = datasetService.get(EPPRoid.class);
                   if (eppRoid.isInvalid(roid)) {
                       results.add(RDAPValidationResult.builder()
                               .code(-46201)
                               .value(getResultValue(HANDLE_PATH))
                               .message("The globally unique identifier in the domain object handle is not registered in EPPROID.")
                               .build());
                       return new HandleObjectToValidate(handle, false);
                   }
                }
           } else {
               handleValue = "not valid";
               return new HandleObjectToValidate(handleValue, false);
           }
       } catch (Exception e) {
           logger.info("handle is not in the top most object, next validations apply");
           return new HandleObjectToValidate(handleValue, true);
       }

     return new HandleObjectToValidate(handleValue, true);
 }

 private RedactedHandleObjectToValidate validateHandleInRedactedObject() {
   JSONObject redactedRegistryDomain = null;
   redactedPointersValue = getPointerFromJPath(REDACTED_PATH);
   for (String redactedJsonPointer : redactedPointersValue) {
     JSONObject redacted = (JSONObject) jsonObject.query(redactedJsonPointer);
     JSONObject name = (JSONObject) redacted.get("name");

     try {
         var nameValue = name.get("type");
         if(nameValue instanceof String redactedName) {
             if(redactedName.trim().equalsIgnoreCase("Registry Domain ID")) {
                 redactedRegistryDomain = redacted;
                 break; // Found the Registry Domain ID redaction, no need to continue
             }
         }
     } catch (Exception e) {
         // FIXED: Don't fail immediately when encountering an exception
         // Real-world redacted arrays contain mixed objects:
         // - Some have name.type (e.g., "Registry Domain ID", "Registrant Phone") 
         // - Some have name.description (e.g., "Administrative Contact", "Technical Contact")
         // - The exception occurs when trying to extract "type" from objects that only have "description"
         // We should skip these objects and continue searching, not fail the entire validation
         logger.debug("Redacted object at {} does not have extractable type property, skipping: {}", 
                    redactedJsonPointer, e.getMessage());
         continue; // Continue checking other redacted objects instead of failing
     }
   }

   if(Objects.isNull(redactedRegistryDomain)) {
     results.add(RDAPValidationResult.builder()
             .code(-46202)
             .value(getResultValue(redactedPointersValue))
             .message("a redaction of type Registry Domain ID is required.")
             .build());

     return new RedactedHandleObjectToValidate(redactedRegistryDomain, false);
   }

   return new RedactedHandleObjectToValidate(redactedRegistryDomain, true);
 }

 private boolean validateRedactedProperties(RedactedHandleObjectToValidate redactedHandleObject) {
    Object pathLangValue;

    // If the pathLang property is either absent or is present as a JSON string of “jsonpath” verify prePath
    try {
      logger.info("pathLang is found");
      pathLangValue = redactedHandleObject.registryRedacted().get("pathLang");
      if(pathLangValue instanceof String pathLang) {
        if (pathLang.trim().equalsIgnoreCase("jsonpath")) {
          return validatePrePathBasedOnPathLang(redactedHandleObject.registryRedacted());
        } else {
          results.add(RDAPValidationResult.builder()
                  .code(-46203)
                  .value(getResultValue(redactedPointersValue))
                  .message("jsonpath is invalid for Registry Domain ID.")
                  .build());
          return false;
        }
      } else {
          results.add(RDAPValidationResult.builder()
                  .code(-46203)
                  .value(getResultValue(redactedPointersValue))
                  .message("jsonpath is invalid for Registry Domain ID.")
                  .build());
          return false;
      }
    } catch (Exception e) {
      logger.info("pathLang is not found");
      return validatePrePathBasedOnPathLang(redactedHandleObject.registryRedacted());
    }
 }

 // Verify that the prePath property is either absent or is present as a JSON string of “$.handle”.
 private boolean validatePrePathBasedOnPathLang(JSONObject registryRedacted) {
    try {
      var prePathValue = registryRedacted.get("prePath");
      logger.info("pathPath property is found, so verify value");
      if(prePathValue instanceof String prePath) {
        if(!prePath.trim().equalsIgnoreCase("$.handle")) {
          results.add(RDAPValidationResult.builder()
                  .code(-46203)
                  .value(getResultValue(redactedPointersValue))
                  .message("jsonpath is invalid for Registry Domain ID.")
                  .build());
          return false;
        }
      }
    } catch (Exception e) {
      logger.info("prePath property is not found, so validation is true");
    }

    return true;
 }

  // Verify that the method property is either absent or is present as is a JSON string of “removal”.
 private boolean validateMethodProperty(RedactedHandleObjectToValidate redactedHandleObject) {
  try {
    var methodValue = redactedHandleObject.registryRedacted().get("method");
    logger.info("method property is found, so verify value");
    if(methodValue instanceof String method) {
      if(!method.trim().equalsIgnoreCase("removal")) {
        results.add(RDAPValidationResult.builder()
                .code(-46204)
                .value(getResultValue(redactedPointersValue))
                .message("Registry Domain ID redaction method must be removal if present")
                .build());
        return false;
      }
    }
  } catch (Exception e) {
    logger.info("method property is not found, so validation is true");
  }

   return true;
 }
}

record RedactedHandleObjectToValidate(JSONObject registryRedacted, boolean isValid){}
record HandleObjectToValidate(String handleValue, boolean isValid){}

