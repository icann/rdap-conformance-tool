package org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard;

import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
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

public class ResponseValidationTechEmail_2024 extends ProfileJsonValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidationTechEmail_2024.class);
    public static final String ENTITY_ROLE_PATH = "$.entities[?(@.roles contains 'technical')]";
    private static final String EMAIL_PROPERTY = "email";
    private static final String TECH_EMAIL_TYPE = "Tech Email";
    private static final String REDACTED_PATH = "$.redacted[*]";
    private Set<String> redactedPointersValue = null;
    private boolean isValid = true;
    private final RDAPValidatorConfiguration configuration;

    public ResponseValidationTechEmail_2024(QueryContext qctx) {
        super(qctx.getRdapResponseData(), qctx.getResults());
        this.configuration = qctx.getConfig();
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_Tech_Email_Validation";
    }

    @Override
    protected boolean doValidate() {
        return validateVcardEmailPropertyObject();
    }

    @Override
    public boolean doLaunch() {
        return configuration.isGtldRegistry();
    }

    private boolean validateVcardEmailPropertyObject() {
        if(getPointerFromJPath(ENTITY_ROLE_PATH).isEmpty()) {
            return true;
        }

        try {
            // Use custom method to find email properties that handles both string and array types
            boolean hasEmail = hasEmailProperty();
            logger.debug("hasEmail: {}", hasEmail);

            if(!hasEmail) {
                logger.debug("email in vcard does not have values, validate redaction object needed");
                return validateRedactedArrayForNoEmailValue();
            } else {
                logger.debug("email in vcard has values, no redaction object validation needed");
                return validateRedactedArrayForEmailValue();
            }

        } catch (Exception e) {
            logger.debug("vcard email was not able to be extracted due to {}", e.getMessage());
        }

        return true;
    }

    public boolean validateRedactedArrayForEmailValue() {
        JSONObject redactedEmail = extractRedactedEmailObject();
        if(Objects.nonNull(redactedEmail)) {
            // First validate the redaction properties before reporting -65503
            boolean redactionValid = validateRedactedProperties(redactedEmail);

            // Only add -65503 if the redaction properties are valid
            // If redaction properties are invalid, the validateRedactedProperties method will have added appropriate errors
            if (redactionValid) {
                results.add(RDAPValidationResult.builder()
                        .code(-65503)
                        .value(getResultValue(redactedPointersValue))
                        .message("a redaction of type Tech Email was found but email was not redacted.")
                        .build());
                return false;
            }

            // Return false as there were validation errors in redaction properties
            return false;
        }

        return true;
    }

    private boolean validateRedactedArrayForNoEmailValue() {
        JSONObject redactedEmail = extractRedactedEmailObject();
        if(Objects.isNull(redactedEmail)) {
            // For Tech Email, we don't require a redaction if email is not present
            // This differs from Registrant Email which requires redaction
            return true;
        }

        return validateRedactedProperties(redactedEmail);
    }

    public JSONObject extractRedactedEmailObject() {
        JSONObject redactedEmail = null;
        redactedPointersValue = getPointerFromJPath(REDACTED_PATH);
        for (String redactedJsonPointer : redactedPointersValue) {
            JSONObject redacted = (JSONObject) jsonObject.query(redactedJsonPointer);
            JSONObject name = (JSONObject) redacted.get("name");
            try {
                var nameValue = name.get("type");
                if(nameValue instanceof String redactedName) {
                    if(redactedName.trim().equalsIgnoreCase(TECH_EMAIL_TYPE)) {
                        redactedEmail = redacted;
                        break; // Found the Tech Email redaction, no need to continue
                    }
                }
            } catch (Exception e) {
                // FIXED: Don't fail immediately when encountering an exception
                // Real-world redacted arrays contain mixed objects:
                // - Some have name.type (e.g., "Tech Email", "Registry Domain ID")
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

    public boolean validateRedactedProperties(JSONObject redactedEmail) {
        if(Objects.isNull(redactedEmail)) {
            logger.debug("redactedEmail object is null");
            return true;
        }

        Object pathLangValue;

        // if the pathLang property is either absent or is present as a JSON string of "jsonpath",
        // then verify that the prePath property is either absent or is present with a valid JSONPath expression
        try {
            logger.debug("Extracting pathLang...");
            pathLangValue = redactedEmail.get("pathLang");
            if(pathLangValue instanceof String pathLang) {
                if (pathLang.trim().equalsIgnoreCase("jsonpath")) {
                    return validatePrePathBasedOnPathLang(redactedEmail);
                }
            }
            return true;
        } catch (Exception e) {
            logger.debug("pathLang is not found due to {}", e.getMessage());
            return validatePrePathBasedOnPathLang(redactedEmail);
        }
    }

    // Verify that the prePath property is either absent or is present with a valid JSONPath expression.
    public boolean validatePrePathBasedOnPathLang(JSONObject redactedEmail) {
        if(Objects.isNull(redactedEmail)) {
            logger.debug("redactedEmail object for prePath validations is null");
            return true;
        }

        try {
            var prePathValue = redactedEmail.get("prePath");
            logger.debug("prePath property is found, so verify value");
            if(prePathValue instanceof String prePath) {
                if(!isValidJsonPath(prePath)) {
                    logger.debug("prePath is not a valid JSONPath expression");
                    results.add(RDAPValidationResult.builder()
                            .code(-65500)
                            .value(getResultValue(redactedPointersValue))
                            .message("jsonpath is invalid for Tech Email")
                            .build());
                    return false;
                }

                var prePathPointer = getPointerFromJPath(prePath);
                logger.debug("prePath pointer with size {}", prePathPointer.size());
                if(!prePathPointer.isEmpty()) {
                    results.add(RDAPValidationResult.builder()
                            .code(-65501)
                            .value(getResultValue(redactedPointersValue))
                            .message("jsonpath must evaluate to a zero set for redaction by removal of Tech Email.")
                            .build());
                    isValid = false;
                    return validateMethodProperty(redactedEmail);
                }
            }
        } catch (Exception e) {
            logger.debug("prePath property is not found, no validations defined. Error: {}", e.getMessage());
        }

        return validateMethodProperty(redactedEmail);
    }

    // Verify that the method property is either absent or is present as is a JSON string of "removal".
    public boolean validateMethodProperty(JSONObject redactedEmail) {
        if(Objects.isNull(redactedEmail)) {
            logger.debug("redactedEmail object for method validations is null");
            return true;
        }

        try {
            var methodValue = redactedEmail.get("method");
            logger.debug("method property is found, so verify value");
            if(methodValue instanceof String method) {
                if(!method.trim().equalsIgnoreCase("removal")) {
                    results.add(RDAPValidationResult.builder()
                            .code(-65502)
                            .value(getResultValue(redactedPointersValue))
                            .message("Tech Email redaction method must be removal if present")
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
     * Custom method to check if technical entity has email property.
     * This method properly handles both string and array type parameters.
     */
    public boolean hasEmailProperty() {
        try {
            // Get technical entities
            Set<String> technicalEntities = getPointerFromJPath(ENTITY_ROLE_PATH);
            if (technicalEntities.isEmpty()) {
                return false;
            }

            // Check each technical entity with email properties
            for (String entityPointer : technicalEntities) {
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
            logger.debug("Error checking for email property: {}", e.getMessage());
            return false;
        }

        return false;
    }
}