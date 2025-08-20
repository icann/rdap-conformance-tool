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

public class ResponseValidation2Dot7Dot6Dot1_2024 extends ProfileJsonValidation {

    public static final String ENTITY_TECHNICAL_ROLE_PATH = "$.entities[?(@.roles[0]=='technical')]";
    public static final String VCARD_ARRAY_PATH = "$.entities[?(@.roles[0]=='technical')].vcardArray";
    private static final String REDACTED_PATH = "$.redacted[*]";
    private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot7Dot6Dot1_2024.class);

    public ResponseValidation2Dot7Dot6Dot1_2024(String rdapResponse, RDAPValidatorResults results) {
        super(rdapResponse, results);
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_2_7_6_1_Validation";
    }

    @Override
    protected boolean doValidate() {
        if(getPointerFromJPath(ENTITY_TECHNICAL_ROLE_PATH).isEmpty()) {
            return true;
        }

        boolean isValid = true;
        boolean needCheckRedacted = false;

        for (String vcardArrayPointer : getPointerFromJPath(VCARD_ARRAY_PATH)) {
          JSONArray vcardArray = (JSONArray) jsonObject.query(vcardArrayPointer);

            boolean hasFn = false;
            boolean isFnEmpty = false;

            JSONArray vcard = (JSONArray) vcardArray.get(1);

            for (int i = 0; i < vcard.length(); i++) {
                JSONArray categoryArray = (JSONArray) vcard.get(i);
                String property = categoryArray.get(0).toString();

                if ("fn".equals(property)) {
                    hasFn = true;

                    try {
                        isFnEmpty = categoryArray.get(3).toString().isEmpty();
                    } catch (Exception e) {
                        isFnEmpty = true;
                    }

                    break;
                }
            }

            if (!hasFn) {
                logger.info("adding 65000, value = {}", vcardArray);
                results.add(RDAPValidationResult.builder()
                    .code(-65000)
                    .value(vcardArray.toString())
                    .message("The fn property is required on the vcard for the technical contact.")
                    .build());

                isValid = false;
            }

            if (hasFn && isFnEmpty) {
                needCheckRedacted = true;
            }
        } // end of vCard loop

        if (needCheckRedacted) {
            JSONObject redactedTechName = null;
            Set<String> redactedPointersValue = getPointerFromJPath(REDACTED_PATH);
            for (String redactedJsonPointer : redactedPointersValue) {
                JSONObject redacted = (JSONObject) jsonObject.query(redactedJsonPointer);
                try {
                    JSONObject name = (JSONObject) redacted.get("name");
                    if (name != null && name.get("type") instanceof String redactedName) {
                        if (redactedName.trim().equalsIgnoreCase("Tech Name")) {
                            redactedTechName = redacted;
                            break;
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Skipping malformed redacted object: {}", e.getMessage());
                    continue;
                }
            }

            if (Objects.isNull(redactedTechName)) {
                logger.info("adding 65001, value = {}", getResultValue(redactedPointersValue));
                results.add(RDAPValidationResult.builder()
                    .code(-65001)
                    .value(getResultValue(redactedPointersValue))
                    .message("a redaction of type Tech Name is required.")
                    .build());

                isValid = false;
            } else {
                Object pathLang = null;
                try {
                    pathLang = redactedTechName.get("pathLang");
                } catch (JSONException e) {
                    logger.info("pathLang is absent: {}", e.getMessage());
                }

                if (pathLang == null || "jsonpath".equals(pathLang.toString())) {
                    logger.info("pathLang is either absent or is 'jsonpath'");

                    Object postPath = null;
                    try {
                        postPath = redactedTechName.get("postPath");
                    } catch (JSONException e) {
                        logger.info("postPath is absent: {}", e.getMessage());
                    }
                    logger.info("postPath: {}", postPath);
                    // 65002 and 65003 validation
                    isValid = validatePostPath(postPath, redactedTechName.toString()) && isValid;
                }

                // 65004 validation
                Object method = null;
                try {
                    method = redactedTechName.get("method");
                } catch (JSONException e) {
                    logger.info("method is absent: {}", e.getMessage());
                }

                logger.info("method = {}", method);
                if (method == null || !"emptyValue".equals(method.toString())) {
                    logger.info("adding 65004, value = {}", redactedTechName);
                    results.add(RDAPValidationResult.builder()
                        .code(-65004)
                        .value(redactedTechName.toString())
                        .message("Tech Name redaction method must be emptyValue")
                        .build());

                    isValid = false;
                }
            }
        }

        return isValid;
    }

    private boolean validatePostPath(Object postPath, String value) {
        if (postPath == null || !isValidJsonPath(postPath.toString())) {
            // postPath is null or not a valid JSONPath
            logger.info("adding 65002, value = {}", value);
            results.add(RDAPValidationResult.builder()
                .code(-65002)
                .value(value)
                .message("jsonpath is invalid for Tech Name")
                .build());

            return false;
        }

        Set<String> pointers = getPointerFromJPath(postPath.toString());

        if (pointers == null || pointers.isEmpty()) {
            logger.info("adding 65003, value = {}", value);
            results.add(RDAPValidationResult.builder()
                .code(-65003)
                .value(value)
                .message("jsonpath must evaluate to a non-empty set for redaction by empty value of Tech Name.")
                .build());

            return false;
        }
        return true;
    }
}