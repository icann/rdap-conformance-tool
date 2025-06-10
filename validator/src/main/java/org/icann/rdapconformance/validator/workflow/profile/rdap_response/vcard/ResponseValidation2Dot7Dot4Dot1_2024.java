package org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard;

import org.apache.commons.lang3.StringUtils;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

public class ResponseValidation2Dot7Dot4Dot1_2024 extends ProfileJsonValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot7Dot4Dot1_2024.class);
    public static final String VCARD_FN_PATH = "$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')]";
    public static final String VCARD_PATH = "$.entities[?(@.roles[0]=='registrant')].vcardArray[1]";
    private static final String REDACTED_PATH = "$.redacted[*]";
    private Set<String> vcardFnPointersValue = null;
    private Set<String> vcardPointersValue = null;
    private Set<String> redactedPointersValue = null;

    public ResponseValidation2Dot7Dot4Dot1_2024(String rdapResponse, RDAPValidatorResults results) {
        super(rdapResponse, results);
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_2_7_4_1_Validation";
    }

    @Override
    protected boolean doValidate() {
        return validateVcardFnPropertyObject();
    }

    private boolean validateVcardFnPropertyObject() {
        try {
            vcardFnPointersValue = getPointerFromJPath(VCARD_FN_PATH);
            vcardPointersValue = getPointerFromJPath(VCARD_PATH);
            logger.info("vcardFnPointersValue size: {}", vcardFnPointersValue.size());

            if(vcardFnPointersValue.isEmpty()) {
                logger.info("fn in vcard does not have values, validate redaction object");
                results.add(RDAPValidationResult.builder()
                        .code(-63200)
                        .value(getResultValue(vcardPointersValue))
                        .message("The fn property is required on the vcard for the registrant.")
                        .build());
                return false;
            } else {
                for (String jsonPointer : vcardFnPointersValue) {
                    JSONArray vcardFnArray = (JSONArray) jsonObject.query(jsonPointer);
                    if(vcardFnArray.get(3) instanceof String fnValue) {
                        if(StringUtils.isEmpty(fnValue)) {
                            return validateRedactedArrayForFnValue();
                        }
                    }
                }
            }

            return true;

        } catch (Exception e) {
            logger.info("vcard fn is not found, validations for this case, Error: {}", e.getMessage());
            results.add(RDAPValidationResult.builder()
                    .code(-63200)
                    .value(getResultValue(vcardPointersValue))
                    .message("The fn property is required on the vcard for the registrant.")
                    .build());

            return false;
        }
    }

    private boolean validateRedactedArrayForFnValue() {
        JSONObject redactedRegistrantName = null;
        redactedPointersValue = getPointerFromJPath(REDACTED_PATH);
        for (String redactedJsonPointer : redactedPointersValue) {
            JSONObject redacted = (JSONObject) jsonObject.query(redactedJsonPointer);
            JSONObject name = (JSONObject) redacted.get("name");
            try {
                var nameValue = name.get("type");
                if(nameValue instanceof String redactedName) {
                    if(redactedName.trim().equalsIgnoreCase("Registrant Name")) {
                        redactedRegistrantName = redacted;
                    }
                }
            } catch (Exception e) {
                logger.info("Extract type from name is not possible by {}", e.getMessage());
                results.add(RDAPValidationResult.builder()
                        .code(-63201)
                        .value(getResultValue(redactedPointersValue))
                        .message("a redaction of type Registrant Name is required.")
                        .build());

                return false;
            }

        }

        if(Objects.isNull(redactedRegistrantName)) {
            results.add(RDAPValidationResult.builder()
                    .code(-63201)
                    .value(getResultValue(redactedPointersValue))
                    .message("a redaction of type Registrant Name is required.")
                    .build());

            return false;
        }

        return validateRedactedProperties(redactedRegistrantName);
    }

    private boolean validateRedactedProperties(JSONObject redactedRegistrantName) {
        if(Objects.isNull(redactedRegistrantName)) {
            logger.info("redactedRegistrantName object is null");
            return true;
        }

        Object pathLangValue;

        // If the pathLang property is either absent or is present as a JSON string of “jsonpath” verify postPath
        try {
            logger.info("Extracting pathLang...");
            pathLangValue = redactedRegistrantName.get("pathLang");
            if(pathLangValue instanceof String pathLang) {
                if (pathLang.trim().equalsIgnoreCase("jsonpath")) {
                    return validatePostPathBasedOnPathLang(redactedRegistrantName);
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("pathLang is not found due to {}", e.getMessage());
            return validatePostPathBasedOnPathLang(redactedRegistrantName);
        }
    }

    // Verify that the postPath property is either absent or is present with a valid JSONPath expression.
    private boolean validatePostPathBasedOnPathLang(JSONObject redactedRegistrantName) {
        if(Objects.isNull(redactedRegistrantName)) {
            logger.info("redactedRegistrantName object for postPath validations is null");
            return true;
        }

        try {
            var postPathValue = redactedRegistrantName.get("postPath");
            logger.info("postPath property is found, so verify value");
            if(postPathValue instanceof String postPath) {
                try {
                    var postPathPointer = getPointerFromJPath(postPath);
                    logger.info("postPath pointer with size {}", postPathPointer.size());
                    if(postPathPointer.isEmpty()) {
                        results.add(RDAPValidationResult.builder()
                                .code(-63203)
                                .value(getResultValue(redactedPointersValue))
                                .message("jsonpath must evaluate to non-empty set for redaction by empty value of Registrant Name.")
                                .build());
                        return false;
                    }
                } catch (Exception e) {
                    logger.info("postPath is not a valid JSONPath expression, Error: {}", e.getMessage());
                    results.add(RDAPValidationResult.builder()
                            .code(-63202)
                            .value(getResultValue(redactedPointersValue))
                            .message("jsonpath is invalid for Registrant Name.")
                            .build());
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("postPath property is not found, no validations defined. Error: {}", e.getMessage());
        }

        return validateMethodProperty(redactedRegistrantName);
    }

    // Verify that the method property is either absent or is present as is a JSON string of “emptyValue”.
    private boolean validateMethodProperty(JSONObject redactedRegistrantName) {
        if(Objects.isNull(redactedRegistrantName)) {
            logger.info("redactedPhone object for method validations is null");
            return true;
        }

        try {
            var methodValue = redactedRegistrantName.get("method");
            logger.info("method property is found, so verify value");
            if(methodValue instanceof String method) {
                if(!method.trim().equalsIgnoreCase("emptyValue")) {
                    results.add(RDAPValidationResult.builder()
                            .code(-63204)
                            .value(getResultValue(redactedPointersValue))
                            .message("Registrant Name redaction method must be empytValue.")
                            .build());
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("method property is not found, no validations defined. Error: {}", e.getMessage());
        }

        return true;
    }
}

