package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import com.jayway.jsonpath.JsonPath;
import java.util.Objects;
import java.util.Set;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResponseValidation2Dot7Dot6Dot3_2024 extends ProfileJsonValidation {

    public static final String ENTITY_TECHNICAL_ROLE_PATH = "$.entities[?(@.roles[0]=='technical')]";
    public static final String VCARD_ARRAY_PATH = "$.entities[?(@.roles[0]=='technical')].vcardArray";
    private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot7Dot6Dot3_2024.class);
    private static final String REDACTED_PATH = "$.redacted[*]";
    private boolean emailExists = false;
    private boolean contactUrlExists = false;

    public ResponseValidation2Dot7Dot6Dot3_2024(String rdapResponse, RDAPValidatorResults results) {
        super(rdapResponse, results);
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_2_7_6_3_Validation";
    }

    @Override
    protected boolean doValidate() {
        if(getPointerFromJPath(ENTITY_TECHNICAL_ROLE_PATH).isEmpty()) {
            return true;
        }

        boolean isValid = true;

        JSONObject redactedTechEmail = null;
        Set<String> redactedPointersValue = getPointerFromJPath(REDACTED_PATH);
        for (String redactedJsonPointer : redactedPointersValue) {
            JSONObject redacted = (JSONObject) jsonObject.query(redactedJsonPointer);
            JSONObject name = (JSONObject) redacted.get("name");
            if (name.get("type") instanceof String redactedName) {
                if (redactedName.trim().equalsIgnoreCase("Tech Email")) {
                    redactedTechEmail = redacted;

                    break;
                }
            }
        }

        if (!Objects.isNull(redactedTechEmail)) {
            // 65200 and 65201 validation
            isValid = validateEmailAndContactUri(getPointerFromJPath(VCARD_ARRAY_PATH));

            // 65202 validation
            Object method = null;
            try {
                method = redactedTechEmail.get("method");
            } catch (JSONException e) {
                logger.info("method is absent: {}", e.getMessage());
            }
            logger.info("method = {}", method);
            if (method == null || !"replacementValue".equals(method.toString())) {
                results.add(RDAPValidationResult.builder()
                    .code(-65202)
                    .value(redactedTechEmail.toString())
                    .message("Tech Email redaction method must be replacementValue")
                    .build());

                isValid = false;
            }

            Object pathLang = null;
            try {
                pathLang = redactedTechEmail.get("pathLang");
            } catch (JSONException e) {
                logger.info("pathLang is absent: {}", e.getMessage());
            }

            logger.info("pathLang: {}", pathLang);
            if (pathLang == null || "jsonpath".equals(pathLang.toString())) {
                logger.info("pathLang is either absent or is 'jsonpath'");

                if (emailExists) {
                    // 65203 and 65204 validation
                    Object postPath = null;
                    try {
                        postPath = redactedTechEmail.get("postPath");
                    } catch (JSONException e) {
                        logger.info("postPath is absent: {}", e.getMessage());
                    }
                    logger.info("postPath: {}", postPath);
                    if (postPath != null) {
                        isValid = validatePostPath(postPath.toString(), redactedTechEmail.toString()) && isValid;
                    }
                }

                if (contactUrlExists) {
                    // 65206 validation
                    Object prePath = null;
                    try {
                        prePath = redactedTechEmail.get("prePath");
                    } catch (JSONException e) {
                        logger.info("prePath is absent: {}", e.getMessage());
                    }
                    logger.info("prePath: {}", prePath);
                    if (prePath != null) {
                        isValid = validatePrePath(prePath.toString(), redactedTechEmail.toString()) && isValid;
                    }

                    // 65205 and 65207 validation
                    Object replacementPath = null;
                    try {
                        replacementPath = redactedTechEmail.get("replacementPath");
                    } catch (JSONException e) {
                        logger.info("replacementPath is absent: {}", e.getMessage());
                    }

                    logger.info("replacementPath: {}", replacementPath);
                    if (replacementPath != null) {
                        isValid = validateReplacementPath(replacementPath.toString(), redactedTechEmail.toString()) && isValid;
                    }
                }
            }
        } else {
            logger.info("there is no Tech Email redaction, skip all validations for 2_7_6_3_Validation");
        }

        return isValid;
    }


    private boolean validateEmailAndContactUri(Set<String> vcardArrayPointerValue) {
        boolean isValid = true;

        for (String vcardArrayPointer : vcardArrayPointerValue) {
            logger.info("vcardArrayPointer: {}", vcardArrayPointer);

            JSONArray vcardArray = (JSONArray) jsonObject.query(vcardArrayPointer);

            boolean hasEmail = false;
            boolean hasContactUri = false;

            JSONArray vcard = (JSONArray) vcardArray.get(1);

            for (int i = 0; i < vcard.length(); i++) {
                JSONArray categoryArray = (JSONArray) vcard.get(i);
                String property = categoryArray.get(0).toString();

                if ("email".equals(property)) {
                    hasEmail = true;
                    emailExists = true;
                }

                if ("contact-uri".equals(property)) {
                    hasContactUri = true;
                    contactUrlExists = true;
                }
            }

            if (hasEmail && hasContactUri) {
                logger.info("adding 65200, value = {}", vcardArray);
                results.add(RDAPValidationResult.builder()
                    .code(-65200)
                    .value(vcardArray.toString())
                    .message("a redaction of Tech Email may not have both the email and contact-uri")
                    .build());

                isValid = false;
            }

            if (!hasEmail && !hasContactUri) {
                logger.info("adding 65201, value = {}", vcardArray);
                results.add(RDAPValidationResult.builder()
                    .code(-65201)
                    .value(vcardArray.toString())
                    .message("a redaction of Tech Email must have either the email or contact-uri")
                    .build());

                isValid = false;
            }
        }

        return isValid;
    }

    private boolean validatePostPath(String postPath, String value) {
        if (!isValidJsonPath(postPath)) {
            // postPath is not a valid JSONPath
            logger.info("adding 65203, value = {}", value);
            results.add(RDAPValidationResult.builder()
                .code(-65203)
                .value(value)
                .message("jsonpath is invalid for Tech Email postPath")
                .build());

            return false;
        }

        Set<String> pointers = getPointerFromJPath(postPath);

        if (pointers == null || pointers.isEmpty()) {
            logger.info("adding 65204, value = {}", value);
            results.add(RDAPValidationResult.builder()
                .code(-65204)
                .value(value)
                .message("jsonpath must evaluate to a non-empty set for redaction by replacementValue of Tech Email.")
                .build());

            return false;
        }
        return true;
    }

    private boolean validateReplacementPath(String replacementPath, String value) {
        if (!isValidJsonPath(replacementPath)) {
            // replacementPath is not a valid JSONPath
            logger.info("adding 65205, value = {}", value);
            results.add(RDAPValidationResult.builder()
                .code(-65205)
                .value(value)
                .message("jsonpath is invalid for Tech Email replacementPath")
                .build());

            return false;
        }
        Set<String> pointers = getPointerFromJPath(replacementPath);

        if (pointers == null || pointers.isEmpty()) {
            logger.info("adding 65207, value = {}", value);
            results.add(RDAPValidationResult.builder()
                .code(-65207)
                .value(value)
                .message("jsonpath must evaluate to a non-empty set for redaction by replacementValue of Tech Email in replacementPath")
                .build());

            return false;
        }

        return true;
    }

    private boolean validatePrePath(String prePath, String value) {
        if (!isValidJsonPath(prePath)) {
            // prePath is not a valid JSONPath
            logger.info("adding 65206, value = {}", value);
            results.add(RDAPValidationResult.builder()
                .code(-65206)
                .value(value)
                .message("jsonpath is invalid for Tech Email prePath")
                .build());

            return false;
        }

        return true;
    }
}