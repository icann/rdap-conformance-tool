package org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

public class ResponseValidation2Dot7Dot4Dot8_2024 extends ProfileJsonValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot7Dot4Dot8_2024.class);
    public static final String ENTITY_ROLE_PATH = "$.entities[?(@.roles[0]=='registrant')]";
    public static final String VCARD_VOICE_PATH = "$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[1].type=='voice')]";
    private static final String REDACTED_PATH = "$.redacted[*]";
    private Set<String> redactedPointersValue = null;

    public ResponseValidation2Dot7Dot4Dot8_2024(String rdapResponse, RDAPValidatorResults results) {
        super(rdapResponse, results);
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_2_7_4_8_Validation";
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
            Set<String> vcardVoicePointersValue = getPointerFromJPath(VCARD_VOICE_PATH);
            logger.info("vcardVoicePointersValue size: {}", vcardVoicePointersValue.size());

            if(vcardVoicePointersValue.isEmpty()) {
                logger.info("voice tel in vcard does not have values, validate redaction object");
                return validateRedactedArrayForNoVoiceValue();
            }

        } catch (Exception e) {
            logger.info("vcard voice is not found, validations for this case");
            return validateRedactedArrayForNoVoiceValue();
        }

        return true;
    }

    private boolean validateRedactedArrayForNoVoiceValue() {
        JSONObject redactedPhone = null;
        redactedPointersValue = getPointerFromJPath(REDACTED_PATH);
        for (String redactedJsonPointer : redactedPointersValue) {
            JSONObject redacted = (JSONObject) jsonObject.query(redactedJsonPointer);
            JSONObject name = (JSONObject) redacted.get("name");
            try {
                var nameValue = name.get("type");
                if(nameValue instanceof String redactedName) {
                    if(redactedName.trim().equalsIgnoreCase("Registrant Phone")) {
                        redactedPhone = redacted;
                    }
                }
            } catch (Exception e) {
                logger.info("Extract type from name is not possible by {}", e.getMessage());
                results.add(RDAPValidationResult.builder()
                        .code(-63700)
                        .value(getResultValue(redactedPointersValue))
                        .message("a redaction of type Registrant Phone is required.")
                        .build());

                return false;
            }
        }

        if(Objects.isNull(redactedPhone)) {
            results.add(RDAPValidationResult.builder()
                    .code(-63700)
                    .value(getResultValue(redactedPointersValue))
                    .message("a redaction of type Registrant Phone is required.")
                    .build());

            return false;
        }

        return validateRedactedProperties(redactedPhone);
    }

    private boolean validateRedactedProperties(JSONObject redactedPhone) {
        if(Objects.isNull(redactedPhone)) {
            logger.info("redactedPhone object is null");
            return true;
        }

        Object pathLangValue;

        // If the pathLang property is either absent or is present as a JSON string of “jsonpath” verify prePath
        try {
            logger.info("Extracting pathLang...");
            pathLangValue = redactedPhone.get("pathLang");
            if(pathLangValue instanceof String pathLang) {
                if (pathLang.trim().equalsIgnoreCase("jsonpath")) {
                    return validatePrePathBasedOnPathLang(redactedPhone);
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("pathLang is not found due to {}", e.getMessage());
            return validatePrePathBasedOnPathLang(redactedPhone);
        }
    }

    // Verify that the prePath property is either absent or is present with a valid JSONPath expression.
    private boolean validatePrePathBasedOnPathLang(JSONObject redactedPhone) {
        if(Objects.isNull(redactedPhone)) {
            logger.info("redactedPhone object for prePath validations is null");
            return true;
        }

        try {
            var prePathValue = redactedPhone.get("prePath");
            logger.info("prePath property is found, so verify value");
            if(prePathValue instanceof String prePath) {
                try {
                    var prePathPointer = getPointerFromJPath(prePath);
                    logger.info("prePath pointer with size {}", prePathPointer.size());
                    if(!prePathPointer.isEmpty()) {
                        results.add(RDAPValidationResult.builder()
                                .code(-63702)
                                .value(getResultValue(redactedPointersValue))
                                .message("jsonpath must evaluate to a zero set for redaction by removal of Registrant Phone.")
                                .build());
                        return false;
                    }
                } catch (Exception e) {
                    // prePath is not a valid JSONPath expression
                    results.add(RDAPValidationResult.builder()
                            .code(-63701)
                            .value(getResultValue(redactedPointersValue))
                            .message("jsonpath is invalid for Registrant Phone.")
                            .build());
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("prePath property is not found, no validations defined. Error: {}", e.getMessage());
        }

        return validateMethodProperty(redactedPhone);
    }

    // Verify that the method property is either absent or is present as is a JSON string of “removal”.
    private boolean validateMethodProperty(JSONObject redactedPhone) {
        if(Objects.isNull(redactedPhone)) {
            logger.info("redactedPhone object for method validations is null");
            return true;
        }

        try {
            var methodValue = redactedPhone.get("method");
            logger.info("method property is found, so verify value");
            if(methodValue instanceof String method) {
                if(!method.trim().equalsIgnoreCase("removal")) {
                    results.add(RDAPValidationResult.builder()
                            .code(-63703)
                            .value(getResultValue(redactedPointersValue))
                            .message("Registrant Phone redaction method must be removal if present")
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

