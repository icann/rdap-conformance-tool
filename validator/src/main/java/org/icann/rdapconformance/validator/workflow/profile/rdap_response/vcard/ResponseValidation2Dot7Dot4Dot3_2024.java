package org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard;

import org.apache.commons.lang3.StringUtils;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

import static org.icann.rdapconformance.validator.CommonUtils.TWO;

public class ResponseValidation2Dot7Dot4Dot3_2024 extends ProfileJsonValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot7Dot4Dot3_2024.class);
    public static final String VCARD_ADDRESS_PATH = "$.entities[?(@.roles contains 'registrant')].vcardArray[1][?(@[0]=='adr')]";
    public static final String ENTITY_ROLE_PATH = "$.entities[?(@.roles contains 'registrant')]";
    private static final String REDACTED_PATH = "$.redacted[*]";
    private Set<String> redactedPointersValue = null;

    private final QueryContext queryContext;

    public ResponseValidation2Dot7Dot4Dot3_2024(QueryContext qctx) {
        super(qctx.getRdapResponseData(), qctx.getResults());
        this.queryContext = qctx;
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
        if(getPointerFromJPath(ENTITY_ROLE_PATH).isEmpty()) {
            return true;
        }

        try {
            Set<String> vcardAddressPointersValue = getPointerFromJPath(VCARD_ADDRESS_PATH);
            logger.debug("vcardAddressPointersValue size: {}", vcardAddressPointersValue.size());

            if(vcardAddressPointersValue.isEmpty()) {
                logger.debug("address in vcard does not have values, no validations");
                return true;
            }

            for (String jsonPointer : vcardAddressPointersValue) {
                JSONArray vcardAddressArray = (JSONArray) jsonObject.query(jsonPointer);
                JSONArray vcardAddressValuesArray = (JSONArray) vcardAddressArray.get(3);
                if(vcardAddressValuesArray.get(TWO) instanceof String street) {
                    if(StringUtils.isEmpty(street)) {
                        return validateRedactedArrayForEmptyStreetValue();
                    } else {
                        return validateRedactedArrayForNonEmptyStreetValue();
                    }
                } else if(vcardAddressValuesArray.get(TWO) instanceof JSONArray streetArray) {
                    if(streetArray.isEmpty()) {
                        return validateRedactedArrayForEmptyStreetValue();
                    } else {
                        return validateRedactedArrayForNonEmptyStreetValue();
                    }
                } else {
                    logger.debug("street address is not present");
                    results.add(RDAPValidationResult.builder()
                            .code(-63400)
                            .value(getResultValue(vcardAddressPointersValue))
                            .message("The street value of the adr property is required on the vcard for the registrant.")
                            .build(queryContext));
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            logger.debug("vcard address is not found, no validations for this case");
        }

        return true;
    }
    
    private boolean validateRedactedArrayForNonEmptyStreetValue() {
        var redactedStreet = extractRegistrantStreet();
        if(Objects.nonNull(redactedStreet)) {
            results.add(RDAPValidationResult.builder()
                    .code(-63405)
                    .value(getResultValue(redactedPointersValue))
                    .message("a redaction of type Registrant Street was found but the registrant street was not redacted.")
                    .build(queryContext));

            return false;
        }

        return true;
    }

    private boolean validateRedactedArrayForEmptyStreetValue() {
        var redactedStreet = extractRegistrantStreet();
        if(Objects.isNull(redactedStreet)) {
            results.add(RDAPValidationResult.builder()
                    .code(-63401)
                    .value(getResultValue(redactedPointersValue))
                    .message("a redaction of type Registrant Street is required.")
                    .build(queryContext));

            return false;
        }

        return validateRedactedProperties(redactedStreet);
    }

    private JSONObject extractRegistrantStreet() {
        JSONObject redactedStreet = null;
        redactedPointersValue = getPointerFromJPath(REDACTED_PATH);
        for (String redactedJsonPointer : redactedPointersValue) {
            JSONObject redacted = (JSONObject) jsonObject.query(redactedJsonPointer);
            JSONObject name = (JSONObject) redacted.get("name");
            try {
                var nameValue = name.get("type");
                if(nameValue instanceof String redactedName) {
                    if(redactedName.trim().equalsIgnoreCase("Registrant Street")) {
                        redactedStreet = redacted;
                        break; // Found the Registrant Street redaction, no need to continue
                    }
                }
            } catch (Exception e) {
                // FIXED: Don't fail immediately when encountering an exception
                // Real-world redacted arrays contain mixed objects:
                // - Some have name.type (e.g., "Registrant Street", "Registry Domain ID")
                // - Some have name.description (e.g., "Administrative Contact", "Technical Contact")
                // - The exception occurs when trying to extract "type" from objects that only have "description"
                // We should skip these objects and continue searching, not fail the entire validation
                logger.debug("Redacted object at {} does not have extractable type property, skipping: {}",
                           redactedJsonPointer, e.getMessage());
                continue; // Continue checking other redacted objects instead of failing
            }
        }
        return redactedStreet;
    }

    private boolean validateRedactedProperties(JSONObject redactedStreet) {
        if(Objects.isNull(redactedStreet)) {
            logger.debug("redactedStreet object is null");
            return true;
        }

        Object pathLangValue;

        // If the pathLang property is either absent or is present as a JSON string of “jsonpath” verify postPath
        try {
            logger.debug("Extracting pathLang...");
            pathLangValue = redactedStreet.get("pathLang");
            if(pathLangValue instanceof String pathLang) {
                if (pathLang.trim().equalsIgnoreCase("jsonpath")) {
                    return validatePostPathBasedOnPathLang(redactedStreet);
                }
            }
            return true;
        } catch (Exception e) {
            logger.debug("pathLang is not found");
            return validatePostPathBasedOnPathLang(redactedStreet);
        }
    }

    // Verify that the postPath property is present with a valid JSONPath expression.
    private boolean validatePostPathBasedOnPathLang(JSONObject redactedStreet) {
        if(Objects.isNull(redactedStreet)) {
            logger.debug("redactedStreet object for postPath validations is null");
            return true;
        }

        try {
            var postPathValue = redactedStreet.get("postPath");
            logger.debug("postPath property is found, so verify value");
            if(postPathValue instanceof String postPath) {
                try {
                    var postPathPointer = getPointerFromJPath(postPath);
                    logger.debug("postPath pointer with size {}", postPathPointer.size());
                    if(postPathPointer.isEmpty()) {
                        results.add(RDAPValidationResult.builder()
                                .code(-63403)
                                .value(getResultValue(redactedPointersValue))
                                .message("jsonpath must evaluate to non-empty set for redaction by empty value of Registrant Street.")
                                .build(queryContext));
                        return false;
                    }
                } catch (Exception e) {
                    // postPath is not a valid JSONPath expression
                    results.add(RDAPValidationResult.builder()
                            .code(-63402)
                            .value(getResultValue(redactedPointersValue))
                            .message("jsonpath is invalid for Registrant Street.")
                            .build(queryContext));
                    return false;
                }
            }
        } catch (Exception e) {
            logger.debug("postPath property is not found, so validation is true");
        }

        return validateMethodProperty(redactedStreet);
    }

    // Verify that the method property is present as is a JSON string of “emptyValue”.
    private boolean validateMethodProperty(JSONObject redactedStreet) {
        if(Objects.isNull(redactedStreet)) {
            logger.debug("redactedStreet object for method validations is null");
            return true;
        }

        try {
            var methodValue = redactedStreet.get("method");
            logger.debug("method property is found, so verify value");
            if(methodValue instanceof String method) {
                if(!method.trim().equalsIgnoreCase("emptyValue")) {
                    results.add(RDAPValidationResult.builder()
                            .code(-63404)
                            .value(getResultValue(redactedPointersValue))
                            .message("Registrant Street redaction method must be emptyValue.")
                            .build(queryContext));
                    return false;
                }
            }
        } catch (Exception e) {
            logger.debug("method property is not found, so validation is true");
        }

        return true;
    }
}

