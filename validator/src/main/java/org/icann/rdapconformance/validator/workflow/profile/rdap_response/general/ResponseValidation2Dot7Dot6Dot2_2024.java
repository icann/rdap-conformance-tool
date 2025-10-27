package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

public final class ResponseValidation2Dot7Dot6Dot2_2024 extends ProfileJsonValidation {

  private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot7Dot6Dot2_2024.class);
  public static final String ENTITY_TECHNICAL_ROLE_PATH = "$.entities[?(@.roles contains 'technical')]";
  private static final String REDACTED_PATH = "$.redacted[*]";
  private static final String TEL_PROPERTY = "tel";
  private static final String VOICE_TYPE = "voice";
  private static final String TECH_PHONE_TYPE = "Tech Phone";
  private static final String VCARD_ARRAY = "vcardArray";
  private Set<String> redactedPointersValue = null;

  public ResponseValidation2Dot7Dot6Dot2_2024(QueryContext qctx) {
    super(qctx.getRdapResponseData(), qctx.getResults());
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
            isValid = validateMethodProperty(redactedObject);
        }
    }
    return isValid;
  }

 private RedactedHandleObjectToValidate validateTelVoicePropertyObject() {
     if(getPointerFromJPath(ENTITY_TECHNICAL_ROLE_PATH).isEmpty()) {
         return new RedactedHandleObjectToValidate(null, true);
     }

    // Use custom method to properly detect voice tel properties
    boolean hasVoiceTel = hasTechnicalVoiceTelProperty();
    logger.debug("hasTechnicalVoiceTel: {}", hasVoiceTel);
    
    if(!hasVoiceTel) {
        logger.debug("tel voice is not found for technical entity, validating redacted array");
        return validateRedactedArrayForEmptyTelVoice();
    } else {
        logger.info("tel voice is found for technical entity, validating there is no redacted array");
        return validateNotRedactedArrayForTelVoice();
    }
 }

 private RedactedHandleObjectToValidate validateNotRedactedArrayForTelVoice() {
     JSONObject redactedTechPhone = findRedactedTechPhone();

     if(Objects.nonNull(redactedTechPhone)) {
             results.add(RDAPValidationResult.builder()
                     .code(-65104)
                     .value(getResultValue(redactedPointersValue))
                     .message("a redaction of type Tech Phone was found but tech phone was not redacted.")
                     .build());

             return new RedactedHandleObjectToValidate(redactedTechPhone, false);
         }

     return new RedactedHandleObjectToValidate(null, true);
 }

    private RedactedHandleObjectToValidate validateRedactedArrayForEmptyTelVoice() {
        JSONObject redactedTechPhone = findRedactedTechPhone();

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

    private JSONObject findRedactedTechPhone() {
        JSONObject redactedTechPhone = null;
        redactedPointersValue = getPointerFromJPath(REDACTED_PATH);
        for (String redactedJsonPointer : redactedPointersValue) {
            JSONObject redacted = (JSONObject) jsonObject.query(redactedJsonPointer);
            try {
                JSONObject name = (JSONObject) redacted.get("name");
                if (name != null && name.get("type") instanceof String redactedName) {
                    if (redactedName.trim().equalsIgnoreCase(TECH_PHONE_TYPE)) {
                        redactedTechPhone = redacted;
                        break;
                    }
                }
            } catch (Exception e) {
                logger.debug("Skipping malformed redacted object: {}", e.getMessage());
            }
        }
        return redactedTechPhone;
    }

 private boolean validateRedactedProperties(RedactedHandleObjectToValidate redactedHandleObject) {
    Object pathLangValue;

    // If the pathLang property is either absent or is present as a JSON string of “jsonpath” verify prePath
    try {
      logger.debug("Extracting pathLang...");
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
      logger.debug("pathLang is not found");
      return validatePrePathBasedOnPathLang(redactedHandleObject.registryRedacted());
    }
 }

 // Verify that the prePath property is either absent or is present with a valid JSONPath expression.
 private boolean validatePrePathBasedOnPathLang(JSONObject registryRedacted) {
    try {
      var prePathValue = registryRedacted.get("prePath");
      logger.debug("pathPath property is found, so verify value");
      if(prePathValue instanceof String prePath) {
        try {
            var prePathPointer = getPointerFromJPath(prePath);
            logger.debug("prePath pointer with size {}", prePathPointer.size());
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
      logger.debug("prePath property is not found, so validation is true");
    }

    return true;
 }

  // Verify that the method property is either absent or is present as is a JSON string of “removal”.
 private boolean validateMethodProperty(RedactedHandleObjectToValidate redactedHandleObject) {
      try {
        var methodValue = redactedHandleObject.registryRedacted().get("method");
        logger.debug("method property is found, so verify value");
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
        logger.debug("method property is not found, so validation is true");
      }

      return true;
    }
    
    /**
     * Custom method to check if technical entity has tel property with voice type.
     * This method properly handles both string and array type parameters.
     * Fixes the false positive bug where type: ["voice", "work"] was not detected.
     */
    private boolean hasTechnicalVoiceTelProperty() {
        try {
            // Get technical entities
            Set<String> technicalEntities = getPointerFromJPath(ENTITY_TECHNICAL_ROLE_PATH);
            if (technicalEntities.isEmpty()) {
                return false;
            }

            // Check each technical entity for voice tel properties
            for (String entityPointer : technicalEntities) {
                JSONObject entity = (JSONObject) jsonObject.query(entityPointer);
                JSONArray vcardArray = entity.optJSONArray(VCARD_ARRAY);

                if (vcardArray != null && vcardArray.length() > CommonUtils.ONE) {
                    JSONArray vcardProperties = vcardArray.getJSONArray(CommonUtils.ONE);

                    // Iterate through vcard properties to find tel properties
                    for (int i = CommonUtils.ZERO; i < vcardProperties.length(); i++) {
                        try {
                            JSONArray property = vcardProperties.getJSONArray(i);
                            if (property.length() >= 2 && TEL_PROPERTY.equals(property.getString(CommonUtils.ZERO))) {
                                JSONObject params = property.getJSONObject(CommonUtils.ONE);
                                Object type = params.opt("type");

                                if (hasVoiceType(type)) {
                                    return true;
                                }
                            }
                        } catch (Exception e) {
                            // Skip malformed properties, continue searching
                            logger.debug("Skipping malformed vcard property at index {}: {}", i, e.getMessage());
                            continue;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Error checking for technical voice tel property: {}", e.getMessage());
            return false;
        }

        return false;
    }

    /**
     * Check if a type value contains "voice", handling both string and array cases.
     */
    private boolean hasVoiceType(Object type) {
        if (type == null) {
            return false;
        }

        if (type instanceof String) {
            return VOICE_TYPE.equals(type);
        } else if (type instanceof JSONArray) {
            JSONArray typeArray = (JSONArray) type;
            for (int i = CommonUtils.ZERO; i < typeArray.length(); i++) {
                try {
                    if (VOICE_TYPE.equals(typeArray.getString(i))) {
                        return true;
                    }
                } catch (Exception e) {
                    // Skip non-string elements
                    continue;
                }
            }
        }

        return false;
    }
}

