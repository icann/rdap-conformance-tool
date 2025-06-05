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

public class ResponseValidation2Dot7Dot4Dot3_2024 extends ProfileJsonValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot7Dot6Dot2_2024.class);
    public static final String VCARD_ADDRESS_PATH = "$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')]";
    private static final String REDACTED_PATH = "$.redacted[*]";
    private Set<String> redactedPointersValue = null;

    public ResponseValidation2Dot7Dot4Dot3_2024(String rdapResponse, RDAPValidatorResults results) {
        super(rdapResponse, results);
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_2_7_4_3_Validation";
    }

    @Override
    protected boolean doValidate() {
        return validateVcardStreetAddressPropertyObject();
    }

    private boolean validateVcardStreetAddressPropertyObject() {
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
                if(vcardAddressValuesArray.get(2) instanceof String street) {
                    if(StringUtils.isEmpty(street)) {
                        return validateRedactedArrayForEmptyStreetValue();
                    }
                } else if(vcardAddressValuesArray.get(2) instanceof JSONArray streetArray) {
                    if(streetArray.isEmpty()) {
                        return validateRedactedArrayForEmptyStreetValue();
                    }
                } else {
                    logger.info("street address is not present");
                    results.add(RDAPValidationResult.builder()
                            .code(-63400)
                            .value(getResultValue(vcardAddressPointersValue))
                            .message("The street value of the adr property is required on the vcard for the registrant.")
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

    private boolean validateRedactedArrayForEmptyStreetValue() {
        JSONObject redactedStreet = null;
        redactedPointersValue = getPointerFromJPath(REDACTED_PATH);
        for (String redactedJsonPointer : redactedPointersValue) {
            JSONObject redacted = (JSONObject) jsonObject.query(redactedJsonPointer);
            JSONObject name = (JSONObject) redacted.get("name");
            if(name.get("type") instanceof String redactedName) {
                if(redactedName.trim().equalsIgnoreCase("Registrant Street")) {
                    redactedStreet = redacted;
                }
            }
        }

        if(Objects.isNull(redactedStreet)) {
            results.add(RDAPValidationResult.builder()
                    .code(-63401)
                    .value(getResultValue(redactedPointersValue))
                    .message("a redaction of type Registrant Street is required.")
                    .build());

            return false;
        }

        return validateRedactedProperties(redactedStreet);
    }

    private boolean validateRedactedProperties(JSONObject redactedStreet) {
        if(Objects.isNull(redactedStreet)) {
            logger.info("redactedStreet object is null");
            return true;
        }

        Object pathLangValue;

        // If the pathLang property is either absent or is present as a JSON string of “jsonpath” verify postPath
        try {
            logger.info("Extracting pathLang...");
            pathLangValue = redactedStreet.get("pathLang");
            if(pathLangValue instanceof String pathLang) {
                if (pathLang.trim().equalsIgnoreCase("jsonpath")) {
                    return validatePostPathBasedOnPathLang(redactedStreet);
                }
            }
            return true;
        } catch (Exception e) {
            logger.info("pathLang is not found");
            return validatePostPathBasedOnPathLang(redactedStreet);
        }
    }

    // Verify that the postPath property is present with a valid JSONPath expression.
    private boolean validatePostPathBasedOnPathLang(JSONObject redactedStreet) {
        if(Objects.isNull(redactedStreet)) {
            logger.info("redactedStreet object for postPath validations is null");
            return true;
        }

        try {
            var postPathValue = redactedStreet.get("postPath");
            logger.info("postPath property is found, so verify value");
            if(postPathValue instanceof String postPath) {
                try {
                    var postPathPointer = getPointerFromJPath(postPath);
                    logger.info("postPath pointer with size {}", postPathPointer.size());
                    if(postPathPointer.isEmpty()) {
                        results.add(RDAPValidationResult.builder()
                                .code(-63403)
                                .value(getResultValue(redactedPointersValue))
                                .message("jsonpath must evaluate to non-empty set for redaction by empty value of Registrant Street.")
                                .build());
                        return false;
                    }
                } catch (Exception e) {
                    // postPath is not a valid JSONPath expression
                    results.add(RDAPValidationResult.builder()
                            .code(-63402)
                            .value(getResultValue(redactedPointersValue))
                            .message("jsonpath is invalid for Registrant Street.")
                            .build());
                    return false;
                }
            }
        } catch (Exception e) {
            logger.info("postPath property is not found, so validation is true");
        }

        return validateMethodProperty(redactedStreet);
    }

    // Verify that the method property is present as is a JSON string of “emptyValue”.
    private boolean validateMethodProperty(JSONObject redactedStreet) {
        if(Objects.isNull(redactedStreet)) {
            logger.info("redactedStreet object for method validations is null");
            return true;
        }

        try {
            var methodValue = redactedStreet.get("method");
            logger.info("method property is found, so verify value");
            if(methodValue instanceof String method) {
                if(!method.trim().equalsIgnoreCase("empytValue")) {
                    results.add(RDAPValidationResult.builder()
                            .code(-63404)
                            .value(getResultValue(redactedPointersValue))
                            .message("Registrant Street redaction method must be empytValue.")
                            .build());
                    return false;
                }
            }
        } catch (Exception e) {
            logger.info("method property is not found, so validation is true");
        }

        return true;
    }
}

