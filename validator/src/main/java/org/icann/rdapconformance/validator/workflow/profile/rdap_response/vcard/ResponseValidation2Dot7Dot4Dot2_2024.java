package org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard;

import java.util.Objects;
import java.util.Set;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseValidation2Dot7Dot4Dot2_2024 extends ProfileJsonValidation {

    public static final String ENTITY_ROLE_PATH = "$.entities[?(@.roles[0]=='registrant')]";
    public static final String VCARD_ORG_PATH = "$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]";
    private static final String REDACTED_PATH = "$.redacted[*]";
    private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot7Dot4Dot2_2024.class);

    public ResponseValidation2Dot7Dot4Dot2_2024(String rdapResponse, RDAPValidatorResults results) {
        super(rdapResponse, results);
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_2_7_4_2_Validation";
    }

    @Override
    protected boolean doValidate() {
        if (getPointerFromJPath(ENTITY_ROLE_PATH).isEmpty() || (!getPointerFromJPath(VCARD_ORG_PATH).isEmpty())) {
            logger.info("either entity with the role of registrant is not present, or it has org property, skip validation");
            return true;
        }

        boolean isValid = true;

        JSONObject redactedOrg = null;
        Set<String> redactedPointersValue = getPointerFromJPath(REDACTED_PATH);

        for (String redactedJsonPointer : redactedPointersValue) {
            JSONObject redacted = (JSONObject) jsonObject.query(redactedJsonPointer);
            try {
                JSONObject name = (JSONObject) redacted.get("name");
                if (name != null && name.get("type") instanceof String redactedName) {
                    if (redactedName.trim().equalsIgnoreCase("Registrant Organization")) {
                        redactedOrg = redacted;
                        break;
                    }
                }
            } catch (Exception e) {
                logger.debug("Skipping malformed redacted object: {}", e.getMessage());
                continue;
            }
        }

        if (Objects.isNull(redactedOrg)) {
            logger.info("adding 63300, value = {}", getResultValue(redactedPointersValue));
            results.add(RDAPValidationResult.builder()
                .code(-63300)
                .value(getResultValue(redactedPointersValue))
                .message("a redaction of type Registrant Organization is required.")
                .build());

            isValid = false;
        } else {
            Object pathLang = null;
            try {
                pathLang = redactedOrg.get("pathLang");
            } catch (JSONException e) {
                logger.info("pathLang is absent: {}", e.getMessage());
            }

            if (pathLang == null || "jsonpath".equals(pathLang.toString())) {
                logger.info("pathLang is either absent or is 'jsonpath'");

                Object prePath = null;
                try {
                    prePath = redactedOrg.get("prePath");
                } catch (JSONException e) {
                    logger.info("prePath is absent: {}", e.getMessage());
                }
                logger.info("prePath: {}", prePath);

                if (prePath != null) {
                    // 63301 and 63302 validation
                    isValid = validatePrePath(prePath.toString(), redactedOrg.toString());
                }
            }

            // 63303 validation
            Object method = null;
            try {
                method = redactedOrg.get("method");
            } catch (JSONException e) {
                logger.info("method is absent: {}", e.getMessage());
            }

            logger.info("method = {}", method);
            if (method != null && !"removal".equals(method.toString())) {
                logger.info("adding 63303, value = {}", redactedOrg);
                results.add(RDAPValidationResult.builder()
                    .code(-63303)
                    .value(redactedOrg.toString())
                    .message("Registrant Organization redaction method must be removal if present")
                    .build());

                isValid = false;
            }
        }

        return isValid;
}



    private boolean validatePrePath(String prePath, String value) {
        if (!isValidJsonPath(prePath)) {
            // prePath is null or not a valid JSONPath
            logger.info("adding 63301, value = {}", value);
            results.add(RDAPValidationResult.builder()
                .code(-63301)
                .value(value)
                .message("jsonpath is invalid for Registrant Organization")
                .build());

            return false;
        }

        Set<String> pointers = getPointerFromJPath(prePath);

        if (pointers != null && !pointers.isEmpty()) {
            logger.info("adding 63302, value = {}", value);
            results.add(RDAPValidationResult.builder()
                .code(-63302)
                .value(value)
                .message("jsonpath must evaluate to a zero set for redaction by removal of Registrant Organization.")
                .build());

            return false;
        }
        return true;
    }
}
