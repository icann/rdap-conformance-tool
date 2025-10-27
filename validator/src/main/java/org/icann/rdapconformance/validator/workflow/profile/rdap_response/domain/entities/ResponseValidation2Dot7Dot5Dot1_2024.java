package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import java.util.Objects;
import java.util.Set;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

    public class ResponseValidation2Dot7Dot5Dot1_2024 extends ProfileJsonValidation {

        public static final String ENTITY_ROLE_PATH = "$.entities[?(@.roles contains 'registrant')]";
        private static final String REDACTED_PATH = "$.redacted[*]";
        private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot7Dot5Dot1_2024.class);

        public ResponseValidation2Dot7Dot5Dot1_2024(QueryContext qctx) {
            super(qctx.getRdapResponseData(), qctx.getResults());
        }

        @Override
        public String getGroupName() {
            return "rdapResponseProfile_2_7_5_1_Validation";
        }

        @Override
        protected boolean doValidate() {
            if (getPointerFromJPath(ENTITY_ROLE_PATH).isEmpty()) {
                logger.debug("no entity has the registrant role, skip validation");
                return true;
            }

            boolean isValid = true;

            JSONObject redactedPhoneExt = null;
            Set<String> redactedPointersValue = getPointerFromJPath(REDACTED_PATH);

            for (String redactedJsonPointer : redactedPointersValue) {
                JSONObject redacted = (JSONObject) jsonObject.query(redactedJsonPointer);
                try {
                    JSONObject name = (JSONObject) redacted.get("name");
                    if (name != null && name.get("type") instanceof String redactedName) {
                        if (redactedName.trim().equalsIgnoreCase("Registrant Phone Ext")) {
                            redactedPhoneExt = redacted;
                            break;
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Skipping malformed redacted object: {}", e.getMessage());
                    continue;
                }
            }

            if (Objects.nonNull(redactedPhoneExt)) {
                Object pathLang = null;
                try {
                    pathLang = redactedPhoneExt.get("pathLang");
                } catch (JSONException e) {
                    logger.debug("pathLang is absent: {}", e.getMessage());
                }

                if (pathLang == null || "jsonpath".equals(pathLang.toString())) {
                    logger.debug("pathLang is either absent or is 'jsonpath'");

                    Object prePath = null;
                    try {
                        prePath = redactedPhoneExt.get("prePath");
                    } catch (JSONException e) {
                        logger.debug("prePath is absent: {}", e.getMessage());
                    }
                    logger.debug("prePath: {}", prePath);

                    if (prePath != null) {
                        // 63800 and 63801 validation
                        isValid = validatePrePath(prePath.toString(), redactedPhoneExt.toString());
                    }
                }

                // 63802 validation
                Object method = null;
                try {
                    method = redactedPhoneExt.get("method");
                } catch (JSONException e) {
                    logger.debug("method is absent: {}", e.getMessage());
                }

                logger.debug("method = {}", method);
                if (method != null && !"removal".equals(method.toString())) {
                    logger.debug("adding 63802, value = {}", redactedPhoneExt);
                    results.add(RDAPValidationResult.builder()
                        .code(-63802)
                        .value(redactedPhoneExt.toString())
                        .message("Registrant Phone Ext redaction method must be removal if present")
                        .build());

                    isValid = false;
                }
            }

            return isValid;
        }



        private boolean validatePrePath(String prePath, String value) {
            if (!isValidJsonPath(prePath)) {
                // prePath is null or not a valid JSONPath
                logger.debug("adding 63800, value = {}", value);
                results.add(RDAPValidationResult.builder()
                    .code(-63800)
                    .value(value)
                    .message("jsonpath is invalid for Registrant Phone Ext")
                    .build());

                return false;
            }

            Set<String> pointers = getPointerFromJPath(prePath);

            if (pointers != null && !pointers.isEmpty()) {
                logger.debug("adding 63801, value = {}", value);
                results.add(RDAPValidationResult.builder()
                    .code(-63801)
                    .value(value)
                    .message("jsonpath must evaluate to a zero set for redaction by removal of Registrant Phone Ext.")
                    .build());

                return false;
            }
            return true;
        }
    }
