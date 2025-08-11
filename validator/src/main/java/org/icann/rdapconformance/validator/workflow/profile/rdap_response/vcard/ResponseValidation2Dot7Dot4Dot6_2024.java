package org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard;

import org.apache.commons.lang3.StringUtils;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.general.ResponseValidation2Dot7Dot6Dot2_2024;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

public class ResponseValidation2Dot7Dot4Dot6_2024 extends ProfileJsonValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot7Dot4Dot6_2024.class);
    public static final String VCARD_ADDRESS_PATH = "$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')]";
    public static final String ENTITY_ROLE_PATH = "$.entities[?(@.roles[0]=='registrant')]";
    private static final String REDACTED_PATH = "$.redacted[*]";
    private Set<String> redactedPointersValue = null;

    public ResponseValidation2Dot7Dot4Dot6_2024(String rdapResponse, RDAPValidatorResults results) {
        super(rdapResponse, results);
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_2_7_4_6_Validation";
    }

    @Override
    protected boolean doValidate() {
        return validateVcardPostalCodeAddressPropertyObject();
    }

    private boolean validateVcardPostalCodeAddressPropertyObject() {
        if(getPointerFromJPath(ENTITY_ROLE_PATH).isEmpty()) {
            return true;
        }

        try {
            Set<String> vcardAddressPointersValue = getPointerFromJPath(VCARD_ADDRESS_PATH);
            logger.info("vcardAddressPointersValue size: {}", vcardAddressPointersValue.size());

            if(vcardAddressPointersValue.isEmpty()) {
                logger.info("address in vcard does not have values, no validations");
                return true;
            }

            for (String jsonPointer : vcardAddressPointersValue) {
                JSONArray vcardAddressArray = (JSONArray) jsonObject.query(jsonPointer);
                JSONArray vcardAddressValuesArray = (JSONArray) vcardAddressArray.get(3);
                if(vcardAddressValuesArray.get(5) instanceof String postalCode) {
                    if(StringUtils.isEmpty(postalCode)) {
                        return validateRedactedArrayForEmptyPostalCodeValue();
                    }
                } else {
                    logger.info("postalCode address is not present");
                    results.add(RDAPValidationResult.builder()
                            .code(-63600)
                            .value(getResultValue(vcardAddressPointersValue))
                            .message("The postal code value of the adr property is required on the vcard for the registrant.")
                            .build());
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            logger.info("vcard address is not found, no validations for this case");
        }

        return true;
    }

    private boolean validateRedactedArrayForEmptyPostalCodeValue() {
        JSONObject redactedPostalCode = null;
        redactedPointersValue = getPointerFromJPath(REDACTED_PATH);
        for (String redactedJsonPointer : redactedPointersValue) {
            JSONObject redacted = (JSONObject) jsonObject.query(redactedJsonPointer);
            JSONObject name = (JSONObject) redacted.get("name");
            try {
                var nameValue = name.get("type");
                if(nameValue instanceof String redactedName) {
                    if(redactedName.trim().equalsIgnoreCase("Registrant Postal Code")) {
                        redactedPostalCode = redacted;
                        break; // Found the Registrant Postal Code redaction, no need to continue
                    }
                }
            } catch (Exception e) {
                // FIXED: Don't fail immediately when encountering an exception
                // Real-world redacted arrays contain mixed objects:
                // - Some have name.type (e.g., "Registrant Postal Code", "Registry Domain ID") 
                // - Some have name.description (e.g., "Administrative Contact", "Technical Contact")
                // - The exception occurs when trying to extract "type" from objects that only have "description"
                // We should skip these objects and continue searching, not fail the entire validation
                logger.debug("Redacted object at {} does not have extractable type property, skipping: {}", 
                           redactedJsonPointer, e.getMessage());
                continue; // Continue checking other redacted objects instead of failing
            }
        }

        if(Objects.isNull(redactedPostalCode)) {
            results.add(RDAPValidationResult.builder()
                    .code(-63601)
                    .value(getResultValue(redactedPointersValue))
                    .message("a redaction of type Registrant Postal Code is required.")
                    .build());

            return false;
        }

        return validateRedactedProperties(redactedPostalCode);
    }

    private boolean validateRedactedProperties(JSONObject redactedPostalCode) {
        if(Objects.isNull(redactedPostalCode)) {
            logger.info("redactedPostalCode object is null");
            return true;
        }

        Object pathLangValue;

        // If the pathLang property is either absent or is present as a JSON string of “jsonpath” verify postPath
        try {
            logger.info("Extracting pathLang...");
            pathLangValue = redactedPostalCode.get("pathLang");
            if(pathLangValue instanceof String pathLang) {
                if (pathLang.trim().equalsIgnoreCase("jsonpath")) {
                    return validatePostPathBasedOnPathLang(redactedPostalCode);
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("pathLang is not found due to {}", e.getMessage());
            return validatePostPathBasedOnPathLang(redactedPostalCode);
        }
    }

    // Verify that the postPath property is present with a valid JSONPath expression.
    private boolean validatePostPathBasedOnPathLang(JSONObject redactedPostalCode) {
        if(Objects.isNull(redactedPostalCode)) {
            logger.info("redactedPostalCode object for postPath validations is null");
            return true;
        }

        try {
            var postPathValue = redactedPostalCode.get("postPath");
            logger.info("postPath property is found, so verify value");
            if(postPathValue instanceof String postPath) {
                try {
                    var postPathPointer = getPointerFromJPath(postPath);
                    logger.info("postPath pointer with size {}", postPathPointer.size());
                    if(postPathPointer.isEmpty()) {
                        results.add(RDAPValidationResult.builder()
                                .code(-63603)
                                .value(getResultValue(redactedPointersValue))
                                .message("jsonpath must evaluate to non-empty set for redaction by empty value of Registrant Postal Code.")
                                .build());
                        return false;
                    }
                } catch (Exception e) {
                    // postPath is not a valid JSONPath expression
                    results.add(RDAPValidationResult.builder()
                            .code(-63602)
                            .value(getResultValue(redactedPointersValue))
                            .message("jsonpath is invalid for Registrant Postal Code.")
                            .build());
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("postPath property is not found, no validations defined. Error: {}", e.getMessage());
        }

        return validateMethodProperty(redactedPostalCode);
    }

    // Verify that the method property is present as is a JSON string of “emptyValue”.
    private boolean validateMethodProperty(JSONObject redactedPostalCode) {
        if(Objects.isNull(redactedPostalCode)) {
            logger.info("redactedPostalCode object for method validations is null");
            return true;
        }

        try {
            var methodValue = redactedPostalCode.get("method");
            logger.info("method property is found, so verify value");
            if(methodValue instanceof String method) {
                if(!method.trim().equalsIgnoreCase("emptyValue")) {
                    results.add(RDAPValidationResult.builder()
                            .code(-63604)
                            .value(getResultValue(redactedPointersValue))
                            .message("Registrant Postal Code redaction method must be emptyValue")
                            .build());
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("message property is not found, no validations defined. Error: {}", e.getMessage());
        }

        return true;
    }
}

