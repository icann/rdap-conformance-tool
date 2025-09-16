package org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard;

import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

public class ResponseValidation2Dot7Dot4Dot8_2024 extends ProfileJsonValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot7Dot4Dot8_2024.class);
    public static final String ENTITY_ROLE_PATH = "$.entities[?(@.roles contains 'registrant')]";
    public static final String VCARD_VOICE_PATH = "$.entities[?(@.roles contains 'registrant')].vcardArray[1][?(@[1].type=='voice')]";
    private static final String TEL_PROPERTY = "tel";
    private static final String VOICE_TYPE = "voice";
    private static final String REGISTRANT_PHONE_TYPE = "Registrant Phone";
    private static final String REDACTED_PATH = "$.redacted[*]";
    private Set<String> redactedPointersValue = null;
    private boolean isValid = true;

    public ResponseValidation2Dot7Dot4Dot8_2024(String rdapResponse, RDAPValidatorResults results) {
        super(rdapResponse, results);
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_2_7_4_8_Validation";
    }

    @Override
    protected boolean doValidate() {
        return validateVcardVoiceInTelPropertyObject();
    }

    private boolean validateVcardVoiceInTelPropertyObject() {
        if(getPointerFromJPath(ENTITY_ROLE_PATH).isEmpty()) {
            return true;
        }

        try {
            // Use custom method to find voice tel properties that handles both string and array types
            boolean hasVoiceTel = hasVoiceTelProperty();
            logger.info("hasVoiceTel: {}", hasVoiceTel);

            if(!hasVoiceTel) {
                logger.info("voice tel in vcard does not have values, validate redaction object");
                return validateRedactedArrayForNoVoiceValue();
            }

        } catch (Exception e) {
            logger.info("vcard voice is not found, validations for this case");
            return validateRedactedArrayForNoVoiceValue();
        }

        return true;
    }

    private boolean validateRedactedArrayForNoVoiceValue() {
        JSONObject redactedPhone = null;
        redactedPointersValue = getPointerFromJPath(REDACTED_PATH);
        for (String redactedJsonPointer : redactedPointersValue) {
            JSONObject redacted = (JSONObject) jsonObject.query(redactedJsonPointer);
            JSONObject name = (JSONObject) redacted.get("name");
            try {
                var nameValue = name.get("type");
                if(nameValue instanceof String redactedName) {
                    if(redactedName.trim().equalsIgnoreCase(REGISTRANT_PHONE_TYPE)) {
                        redactedPhone = redacted;
                        break; // Found the Registrant Phone redaction, no need to continue
                    }
                }
            } catch (Exception e) {
                // FIXED: Don't fail immediately when encountering an exception
                // Real-world redacted arrays contain mixed objects:
                // - Some have name.type (e.g., "Registrant Phone", "Registry Domain ID") 
                // - Some have name.description (e.g., "Administrative Contact", "Technical Contact")
                // - The exception occurs when trying to extract "type" from objects that only have "description"
                // We should skip these objects and continue searching, not fail the entire validation
                logger.debug("Redacted object at {} does not have extractable type property, skipping: {}", 
                           redactedJsonPointer, e.getMessage());
                continue; // Continue checking other redacted objects instead of failing
            }
        }

        if(Objects.isNull(redactedPhone)) {
            results.add(RDAPValidationResult.builder()
                    .code(-63700)
                    .value(getResultValue(redactedPointersValue))
                    .message("a redaction of type Registrant Phone is required.")
                    .build());

            return false;
        }

        return validateRedactedProperties(redactedPhone);
    }

    private boolean validateRedactedProperties(JSONObject redactedPhone) {
        if(Objects.isNull(redactedPhone)) {
            logger.info("redactedPhone object is null");
            return true;
        }

        Object pathLangValue;

        // If the pathLang property is either absent or is present as a JSON string of “jsonpath” verify prePath
        try {
            logger.info("Extracting pathLang...");
            pathLangValue = redactedPhone.get("pathLang");
            if(pathLangValue instanceof String pathLang) {
                if (pathLang.trim().equalsIgnoreCase("jsonpath")) {
                    return validatePrePathBasedOnPathLang(redactedPhone);
                }
            }
            return true;
        } catch (Exception e) {
            logger.debug("pathLang is not found due to {}", e.getMessage());
            return validatePrePathBasedOnPathLang(redactedPhone);
        }
    }

    // Verify that the prePath property is either absent or is present with a valid JSONPath expression.
    private boolean validatePrePathBasedOnPathLang(JSONObject redactedPhone) {
        if(Objects.isNull(redactedPhone)) {
            logger.info("redactedPhone object for prePath validations is null");
            return true;
        }

        try {
            var prePathValue = redactedPhone.get("prePath");
            logger.info("prePath property is found, so verify value");
            if(prePathValue instanceof String prePath) {
                try {
                    var prePathPointer = getPointerFromJPath(prePath);
                    logger.info("prePath pointer with size {}", prePathPointer.size());
                    if(!prePathPointer.isEmpty()) {
                        results.add(RDAPValidationResult.builder()
                                .code(-63702)
                                .value(getResultValue(redactedPointersValue))
                                .message("jsonpath must evaluate to a zero set for redaction by removal of Registrant Phone.")
                                .build());
                        isValid = false;
                        return validateMethodProperty(redactedPhone);
                    }
                } catch (Exception e) {
                    // prePath is not a valid JSONPath expression
                    results.add(RDAPValidationResult.builder()
                            .code(-63701)
                            .value(getResultValue(redactedPointersValue))
                            .message("jsonpath is invalid for Registrant Phone.")
                            .build());
                    return false;
                }
            }
        } catch (Exception e) {
            logger.debug("prePath property is not found, no validations defined. Error: {}", e.getMessage());
        }

        return validateMethodProperty(redactedPhone);
    }

    // Verify that the method property is either absent or is present as is a JSON string of “removal”.
    private boolean validateMethodProperty(JSONObject redactedPhone) {
        if(Objects.isNull(redactedPhone)) {
            logger.info("redactedPhone object for method validations is null");
            return true;
        }

        try {
            var methodValue = redactedPhone.get("method");
            logger.info("method property is found, so verify value");
            if(methodValue instanceof String method) {
                if(!method.trim().equalsIgnoreCase("removal")) {
                    results.add(RDAPValidationResult.builder()
                            .code(-63703)
                            .value(getResultValue(redactedPointersValue))
                            .message("Registrant Phone redaction method must be removal if present")
                            .build());
                    isValid = false;
                }
            }
        } catch (Exception e) {
            logger.debug("method property is not found, no validations defined. Error: {}", e.getMessage());
        }

        return isValid;
    }

    /**
     * Custom method to check if registrant entity has tel property with voice type.
     * This method properly handles both string and array type parameters.
     */
    private boolean hasVoiceTelProperty() {
        try {
            // Get registrant entities
            Set<String> registrantEntities = getPointerFromJPath(ENTITY_ROLE_PATH);
            if (registrantEntities.isEmpty()) {
                return false;
            }

            // Check each registrant entity for voice tel properties
            for (String entityPointer : registrantEntities) {
                JSONObject entity = (JSONObject) jsonObject.query(entityPointer);
                JSONArray vcardArray = entity.optJSONArray("vcardArray");

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
            logger.debug("Error checking for voice tel property: {}", e.getMessage());
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

