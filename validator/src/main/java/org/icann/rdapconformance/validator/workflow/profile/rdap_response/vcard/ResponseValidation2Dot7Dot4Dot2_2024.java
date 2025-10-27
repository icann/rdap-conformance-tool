package org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard;

import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseValidation2Dot7Dot4Dot2_2024 extends ProfileJsonValidation {

    public static final String ENTITY_ROLE_PATH = "$.entities[?(@.roles contains 'registrant')]";
    public static final String VCARD_ORG_PATH = "$.entities[?(@.roles contains 'registrant')].vcardArray[1][?(@[0]=='org')]";
    private static final String REDACTED_PATH = "$.redacted[*]";
    private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot7Dot4Dot2_2024.class);

    public ResponseValidation2Dot7Dot4Dot2_2024(QueryContext qctx) {
        super(qctx.getRdapResponseData(), qctx.getResults());
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_2_7_4_2_Validation";
    }

    @Override
    protected boolean doValidate() {
        if (getPointerFromJPath(ENTITY_ROLE_PATH).isEmpty()) {
            logger.debug("entity with the role of registrant is not present, skip validation");
            return true;
        }

        JSONObject redactedOrg = findRedactedOrganization();
        boolean isValid = true;

        if(CollectionUtils.isNotEmpty(getPointerFromJPath(VCARD_ORG_PATH))) {
            logger.info("Registrant Organization is present, should not be redacted");
            if (Objects.nonNull(redactedOrg)) {
                results.add(RDAPValidationResult.builder()
                    .code(-63304)
                    .value(redactedOrg.toString())
                    .message("a redaction of type Registrant Organization was found but organization name was not redacted.")
                    .build());
                return false;
            }
        }

        if (Objects.isNull(redactedOrg)) {
            logger.debug("No 'Registrant Organization' redaction found, skip validation (natural person)");
            return true;
        } else {
            Object pathLang = null;
            try {
                pathLang = redactedOrg.get("pathLang");
            } catch (JSONException e) {
                logger.debug("pathLang is absent: {}", e.getMessage());
            }

            if (pathLang == null || "jsonpath".equals(pathLang.toString())) {
                logger.debug("pathLang is either absent or is 'jsonpath'");

                Object prePath = null;
                try {
                    prePath = redactedOrg.get("prePath");
                } catch (JSONException e) {
                    logger.debug("prePath is absent: {}", e.getMessage());
                }
                logger.debug("prePath: {}", prePath);

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
                logger.debug("method is absent: {}", e.getMessage());
            }

            logger.debug("method = {}", method);
            if (method != null && !"removal".equals(method.toString())) {
                logger.debug("adding 63303, value = {}", redactedOrg);
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

    private JSONObject findRedactedOrganization() {
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
        return redactedOrg;
    }


    private boolean validatePrePath(String prePath, String value) {
        if (!isValidJsonPath(prePath)) {
            // prePath is null or not a valid JSONPath
            logger.debug("adding 63301, value = {}", value);
            results.add(RDAPValidationResult.builder()
                .code(-63301)
                .value(value)
                .message("jsonpath is invalid for Registrant Organization")
                .build());

            return false;
        }

        Set<String> pointers = getPointerFromJPath(prePath);

        if (pointers != null && !pointers.isEmpty()) {
            logger.debug("adding 63302, value = {}", value);
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
