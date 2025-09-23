package org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard;

import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

public class ResponseValidationRegistrantEmail_2024 extends ProfileJsonValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidationRegistrantEmail_2024.class);
    public static final String ENTITY_ROLE_PATH = "$.entities[?(@.roles contains 'registrant')]";
    public static final String VCARD_EMAIL_PATH = "$.entities[?(@.roles contains 'registrant')].vcardArray[1][?(@[0]=='email')]";
    private static final String EMAIL_PROPERTY = "email";
    private static final String REGISTRANT_EMAIL_TYPE = "Registrant Email";
    private static final String REDACTED_PATH = "$.redacted[*]";
    private Set<String> redactedPointersValue = null;
    private boolean isValid = true;
    private final RDAPValidatorConfiguration configuration;

    public ResponseValidationRegistrantEmail_2024(String rdapResponse, RDAPValidatorResults results, RDAPValidatorConfiguration configuration) {
        super(rdapResponse, results);
        this.configuration = configuration;
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_Registrant_Email_Validation";
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
            // Use custom method to find email properties that handles both string and array types
            boolean hasEmail = hasEmailProperty();
            logger.info("hasEmail: {}", hasEmail);

            if(!hasEmail) {
                logger.debug("email in vcard does not have values, validate redaction object needed");
                return validateRedactedArrayForNoEmailValue();
            } else {
                logger.debug("email in vcard has values, no redaction object validation needed");
                return validateRedactedArrayForEmailValue();
            }

        } catch (Exception e) {
            logger.info("vcard email was not able to be extracted due to {}", e.getMessage());
        }

        return true;
    }

    private boolean validateRedactedArrayForEmailValue() {
        JSONObject redactedEmail = extractRedactedEmailObject();
        if(Objects.nonNull(redactedEmail)) {
            results.add(RDAPValidationResult.builder()
                    .code(-65404)
                    .value(getResultValue(redactedPointersValue))
                    .message("a redaction of type Registrant Email was found but email was not redacted.")
                    .build());
            return false;
        }

        return true;
    }

    private boolean validateRedactedArrayForNoEmailValue() {
        JSONObject redactedEmail = extractRedactedEmailObject();
        if(Objects.isNull(redactedEmail)) {
            results.add(RDAPValidationResult.builder()
                    .code(-65400)
                    .value(getResultValue(redactedPointersValue))
                    .message("a redaction of type Registrant Email is required.")
                    .build());

            return false;
        }

        return validateRedactedProperties(redactedEmail);
    }

    private JSONObject extractRedactedEmailObject() {
        JSONObject redactedEmail = null;
        redactedPointersValue = getPointerFromJPath(REDACTED_PATH);
        for (String redactedJsonPointer : redactedPointersValue) {
            JSONObject redacted = (JSONObject) jsonObject.query(redactedJsonPointer);
            JSONObject name = (JSONObject) redacted.get("name");
            try {
                var nameValue = name.get("type");
                if(nameValue instanceof String redactedName) {
                    if(redactedName.trim().equalsIgnoreCase(REGISTRANT_EMAIL_TYPE)) {
                        redactedEmail = redacted;
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
        return redactedEmail;
    }

    private boolean validateRedactedProperties(JSONObject redactedEmail) {
        if(Objects.isNull(redactedEmail)) {
            logger.info("redactedEmail object is null");
            return true;
        }

        Object pathLangValue;

        // if the pathLang property is either absent or is present as a JSON string of “jsonpath”,
        // then verify that the prePath property is either absent or is present with a valid JSONPath expression
        try {
            logger.info("Extracting pathLang...");
            pathLangValue = redactedEmail.get("pathLang");
            if(pathLangValue instanceof String pathLang) {
                if (pathLang.trim().equalsIgnoreCase("jsonpath")) {
                    return validatePrePathBasedOnPathLang(redactedEmail);
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("pathLang is not found due to {}", e.getMessage());
            return validatePrePathBasedOnPathLang(redactedEmail);
        }
    }

    // Verify that the prePath property is either absent or is present with a valid JSONPath expression.
    private boolean validatePrePathBasedOnPathLang(JSONObject redactedEmail) {
        if(Objects.isNull(redactedEmail)) {
            logger.info("redactedEmail object for prePath validations is null");
            return true;
        }

        try {
            var prePathValue = redactedEmail.get("prePath");
            logger.info("prePath property is found, so verify value");
            if(prePathValue instanceof String prePath) {
                if(!isValidJsonPath(prePath)) {
                    logger.info("prePath is not a valid JSONPath expression");
                    results.add(RDAPValidationResult.builder()
                            .code(-65401)
                            .value(getResultValue(redactedPointersValue))
                            .message("jsonpath is invalid for Registrant Email")
                            .build());
                    return false;
                }

                var prePathPointer = getPointerFromJPath(prePath);
                logger.info("prePath pointer with size {}", prePathPointer.size());
                if(!prePathPointer.isEmpty()) {
                    results.add(RDAPValidationResult.builder()
                            .code(-65402)
                            .value(getResultValue(redactedPointersValue))
                            .message("jsonpath must evaluate to a zero set for redaction by removal of Registrant Email.")
                            .build());
                    isValid = false;
                    return validateMethodProperty(redactedEmail);
                }
            }
        } catch (Exception e) {
            logger.error("prePath property is not found, no validations defined. Error: {}", e.getMessage());
        }

        return validateMethodProperty(redactedEmail);
    }

    // Verify that the method property is either absent or is present as is a JSON string of “removal”.
    private boolean validateMethodProperty(JSONObject redactedEmail) {
        if(Objects.isNull(redactedEmail)) {
            logger.info("redactedEmail object for method validations is null");
            return true;
        }

        try {
            var methodValue = redactedEmail.get("method");
            logger.info("method property is found, so verify value");
            if(methodValue instanceof String method) {
                if(!method.trim().equalsIgnoreCase("removal")) {
                    results.add(RDAPValidationResult.builder()
                            .code(-65403)
                            .value(getResultValue(redactedPointersValue))
                            .message("Registrant Email redaction method must be removal if present")
                            .build());
                    isValid = false;
                }
            }
        } catch (Exception e) {
            logger.error("method property is not found, no validations defined. Error: {}", e.getMessage());
        }

        return isValid;
    }

    /**
     * Custom method to check if registrant entity has tel property with voice type.
     * This method properly handles both string and array type parameters.
     */
    private boolean hasEmailProperty() {
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

                    // Iterate through vcard properties to find email properties
                    for (int i = CommonUtils.ZERO; i < vcardProperties.length(); i++) {
                        try {
                            JSONArray property = vcardProperties.getJSONArray(i);
                            if (property.length() >= 2 && EMAIL_PROPERTY.equals(property.getString(CommonUtils.ZERO))) {
                                return true;
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
            logger.error("Error checking for email property: {}", e.getMessage());
            return false;
        }

        return false;
    }

    @Override
    public boolean doLaunch() {
        return configuration.isGtldRegistry();
    }
}

