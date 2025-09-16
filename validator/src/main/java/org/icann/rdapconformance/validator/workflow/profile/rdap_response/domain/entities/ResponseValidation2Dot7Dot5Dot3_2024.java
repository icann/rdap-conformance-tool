package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;


import java.util.Objects;
import java.util.Set;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseValidation2Dot7Dot5Dot3_2024 extends ProfileJsonValidation {

    public static final String ENTITY_ROLE_PATH = "$.entities[?(@.roles contains 'registrant')]";
    private static final String REDACTED_PATH = "$.redacted[*]";
    private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot7Dot5Dot3_2024.class);

    public ResponseValidation2Dot7Dot5Dot3_2024(String rdapResponse, RDAPValidatorResults results) {
        super(rdapResponse, results);
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_2_7_5_3_Validation";
    }

    @Override
    protected boolean doValidate() {
        if (getPointerFromJPath(ENTITY_ROLE_PATH).isEmpty()) {
            logger.info("no entity has the registrant role, skip validation");
            return true;
        }

        boolean isValid = true;

        JSONObject redactedFaxExt = null;
        Set<String> redactedPointersValue = getPointerFromJPath(REDACTED_PATH);

        for (String redactedJsonPointer : redactedPointersValue) {
            JSONObject redacted = (JSONObject) jsonObject.query(redactedJsonPointer);
            try {
                JSONObject name = (JSONObject) redacted.get("name");
                if (name != null && name.get("type") instanceof String redactedName) {
                    if (redactedName.trim().equalsIgnoreCase("Registrant Fax Ext")) {
                        redactedFaxExt = redacted;
                        break;
                    }
                }
            } catch (Exception e) {
                logger.debug("Skipping malformed redacted object: {}", e.getMessage());
                continue;
            }
        }

        if (Objects.nonNull(redactedFaxExt)) {
            Object pathLang = null;
            try {
                pathLang = redactedFaxExt.get("pathLang");
            } catch (JSONException e) {
                logger.info("pathLang is absent: {}", e.getMessage());
            }

            if (pathLang == null || "jsonpath".equals(pathLang.toString())) {
                logger.info("pathLang is either absent or is 'jsonpath'");

                Object prePath = null;
                try {
                    prePath = redactedFaxExt.get("prePath");
                } catch (JSONException e) {
                    logger.info("prePath is absent: {}", e.getMessage());
                }
                logger.info("prePath: {}", prePath);

                if (prePath != null) {
                    // 64000 and 64001 validation
                    isValid = validatePrePath(prePath.toString(), redactedFaxExt.toString());
                }
            }

            // 64002 validation
            Object method = null;
            try {
                method = redactedFaxExt.get("method");
            } catch (JSONException e) {
                logger.info("method is absent: {}", e.getMessage());
            }

            logger.info("method = {}", method);
            if (method != null && !"removal".equals(method.toString())) {
                logger.info("adding 64002, value = {}", redactedFaxExt);
                results.add(RDAPValidationResult.builder()
                    .code(-64002)
                    .value(redactedFaxExt.toString())
                    .message("Registrant Fax Ext redaction method must be removal if present")
                    .build());

                isValid = false;
            }
        }

        return isValid;
    }



    private boolean validatePrePath(String prePath, String value) {
        if (!isValidJsonPath(prePath)) {
            // prePath is null or not a valid JSONPath
            logger.info("adding 64000, value = {}", value);
            results.add(RDAPValidationResult.builder()
                .code(-64000)
                .value(value)
                .message("jsonpath is invalid for Registrant Fax Ext")
                .build());

            return false;
        }

        Set<String> pointers = getPointerFromJPath(prePath);

        if (pointers != null && !pointers.isEmpty()) {
            logger.info("adding 64001, value = {}", value);
            results.add(RDAPValidationResult.builder()
                .code(-64001)
                .value(value)
                .message("jsonpath must evaluate to a zero set for redaction by removal of Registrant Fax Ext.")
                .build());

            return false;
        }
        return true;
    }
}
