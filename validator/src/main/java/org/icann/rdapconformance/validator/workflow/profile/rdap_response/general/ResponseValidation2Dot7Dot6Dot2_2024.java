package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

public final class ResponseValidation2Dot7Dot6Dot2_2024 extends ProfileJsonValidation {

  private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot7Dot6Dot2_2024.class);
  public static final String TEL_VOICE_PATH = "$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]";
  public static final String ENTITY_TECHNICAL_ROLE_PATH = "$.entities[?(@.roles[0]=='technical')]";
  private static final String REDACTED_PATH = "$.redacted[*]";
  private Set<String> redactedPointersValue = null;

  public ResponseValidation2Dot7Dot6Dot2_2024(String rdapResponse, RDAPValidatorResults results) {
    super(rdapResponse, results);
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_7_6_2_Validation";
  }

  @Override
  protected boolean doValidate() {
    boolean isValid = true;
    var redactedObject = validateTelVoicePropertyObject();
    isValid = redactedObject.isValid();
    if(isValid && Objects.nonNull(redactedObject.registryRedacted())) {
        isValid = validateRedactedProperties(redactedObject);
        if(isValid) {
            isValid = validateRedactedProperties(redactedObject);
            if(isValid) {
                isValid = validateMethodProperty(redactedObject);
            }
        }
    }
    return isValid;
  }

 private RedactedHandleObjectToValidate validateTelVoicePropertyObject() {
     if(getPointerFromJPath(ENTITY_TECHNICAL_ROLE_PATH).isEmpty()) {
         return new RedactedHandleObjectToValidate(null, true);
     }

    try {
        Set<String> telPointersValue = getPointerFromJPath(TEL_VOICE_PATH);
        logger.info("telVoicePointer size: {}", telPointersValue.size());
        if(telPointersValue.isEmpty()) {
            return validateRedactedArrayForEmptyTelVoice();
        }

        return new RedactedHandleObjectToValidate(null, true);

    } catch (Exception e) {
       logger.info("tel voice is not found, same validations into redacted array runs");
        return validateRedactedArrayForEmptyTelVoice();
    }
 }

 private RedactedHandleObjectToValidate validateRedactedArrayForEmptyTelVoice() {
     JSONObject redactedTechPhone = null;
     redactedPointersValue = getPointerFromJPath(REDACTED_PATH);
     for (String redactedJsonPointer : redactedPointersValue) {
         JSONObject redacted = (JSONObject) jsonObject.query(redactedJsonPointer);
         JSONObject name = (JSONObject) redacted.get("name");
         try {
             var nameValue = name.get("type");
             if(nameValue instanceof String redactedName) {
                 if(redactedName.trim().equalsIgnoreCase("Tech Phone")) {
                     redactedTechPhone = redacted;
                 }
             }
         } catch (Exception e) {
             logger.info("Extract type from name is not possible by {}", e.getMessage());
             results.add(RDAPValidationResult.builder()
                     .code(-65100)
                     .value(getResultValue(redactedPointersValue))
                     .message("a redaction of type Tech Phone is required.")
                     .build());

             return new RedactedHandleObjectToValidate(null, false);
         }
     }

     if(Objects.isNull(redactedTechPhone)) {
         results.add(RDAPValidationResult.builder()
                 .code(-65100)
                 .value(getResultValue(redactedPointersValue))
                 .message("a redaction of type Tech Phone is required.")
                 .build());

         return new RedactedHandleObjectToValidate(null, false);
     }

     return new RedactedHandleObjectToValidate(redactedTechPhone, true);
 }

 private boolean validateRedactedProperties(RedactedHandleObjectToValidate redactedHandleObject) {
    Object pathLangValue;

    // If the pathLang property is either absent or is present as a JSON string of “jsonpath” verify prePath
    try {
      logger.info("Extracting pathLang...");
      pathLangValue = redactedHandleObject.registryRedacted().get("pathLang");
      if(pathLangValue instanceof String pathLang) {
        if (pathLang.trim().equalsIgnoreCase("jsonpath")) {
          return validatePrePathBasedOnPathLang(redactedHandleObject.registryRedacted());
        } else {
          results.add(RDAPValidationResult.builder()
                  .code(-65101)
                  .value(getResultValue(redactedPointersValue))
                  .message("jsonpath is invalid for Tech Phone.")
                  .build());
          return false;
        }
      } else {
          results.add(RDAPValidationResult.builder()
                  .code(-65101)
                  .value(getResultValue(redactedPointersValue))
                  .message("jsonpath is invalid for Tech Phone.")
                  .build());
          return false;
      }
    } catch (Exception e) {
      logger.info("pathLang is not found");
      return validatePrePathBasedOnPathLang(redactedHandleObject.registryRedacted());
    }
 }

 // Verify that the prePath property is either absent or is present with a valid JSONPath expression.
 private boolean validatePrePathBasedOnPathLang(JSONObject registryRedacted) {
    try {
      var prePathValue = registryRedacted.get("prePath");
      logger.info("pathPath property is found, so verify value");
      if(prePathValue instanceof String prePath) {
        try {
            var prePathPointer = getPointerFromJPath(prePath);
            logger.info("prePath pointer with size {}", prePathPointer.size());
            if(!prePathPointer.isEmpty()) {
                results.add(RDAPValidationResult.builder()
                        .code(-65102)
                        .value(getResultValue(redactedPointersValue))
                        .message("jsonpath must evaluate to a zero set for redaction by removal of Tech Phone.")
                        .build());
                return false;
            }
        } catch (Exception e) {
            // prePath is not a valid JSONPath expression
            results.add(RDAPValidationResult.builder()
                    .code(-65101)
                    .value(getResultValue(redactedPointersValue))
                    .message("jsonpath is invalid for Tech Phone.")
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
                    .code(-65103)
                    .value(getResultValue(redactedPointersValue))
                    .message("Tech Phone redaction method must be removal if present")
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

