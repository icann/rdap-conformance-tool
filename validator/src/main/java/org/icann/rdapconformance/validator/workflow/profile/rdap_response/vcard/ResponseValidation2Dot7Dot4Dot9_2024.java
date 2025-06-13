package org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard;

import com.jayway.jsonpath.JsonPath;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

public class ResponseValidation2Dot7Dot4Dot9_2024 extends ProfileJsonValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot7Dot4Dot9_2024.class);
    public static final String VCARD_CONTACT_URI_PATH = "$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='contact-uri')]";
    public static final String VCARD_EMAIL_PATH = "$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='email')]";
    public static final String VCARD_PATH = "$.entities[?(@.roles[0]=='registrant')].vcardArray[1]";
    private static final String REDACTED_PATH = "$.redacted[*]";
    private  Set<String> vcardPointersValue = null;
    private Set<String> redactedPointersValue = null;
    private JSONObject redactedRegistrantEmail = null;

    public ResponseValidation2Dot7Dot4Dot9_2024(String rdapResponse, RDAPValidatorResults results) {
        super(rdapResponse, results);
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_2_7_4_9_Validation";
    }

    @Override
    protected boolean doValidate() {
        return validateRedactedArrayForRegistrantEmailValue();
    }

    private boolean validateRedactedArrayForRegistrantEmailValue() {

        redactedPointersValue = getPointerFromJPath(REDACTED_PATH);
        for (String redactedJsonPointer : redactedPointersValue) {
            JSONObject redacted = (JSONObject) jsonObject.query(redactedJsonPointer);
            JSONObject name = (JSONObject) redacted.get("name");
            try {
                var nameValue = name.get("type");
                if(nameValue instanceof String redactedName) {
                    if(redactedName.trim().equalsIgnoreCase("Registrant Email")) {
                        redactedRegistrantEmail = redacted;
                    }
                }
            } catch (Exception e) {
                logger.info("type property with value “Registrant Email” does not exist in redacted array, no validations");
                return true;
            }
        }

        if(Objects.isNull(redactedRegistrantEmail)) {
            logger.info("redactedRegistrantEmail does not exist in redacted array, no validations");
            return true;
        }

        return validateVCardsNoBothValues();
    }

    private boolean validateVCardsNoBothValues() {
        vcardPointersValue = getPointerFromJPath(VCARD_PATH);
        logger.info("vcardVoicePointersValue size: {}", vcardPointersValue.size());

        for (String jsonPointer : vcardPointersValue) {
            Set<String> titles = new HashSet<>();
            JSONArray vcardArray = (JSONArray) jsonObject.query(jsonPointer);
            var vcardList = convertJsonArrayToList(vcardArray);
            vcardList.forEach(t -> {
                if(t.get(0) instanceof String title) {
                    titles.add(title);
                }
            });

            if(titles.containsAll(Arrays.asList("contact-uri", "email"))) {
                results.add(RDAPValidationResult.builder()
                        .code(-64100)
                        .value(getResultValue(vcardPointersValue))
                        .message("a redaction of Registrant Email may not have both the email and contact-uri")
                        .build());
                return false;
            }
        }

        return validateVCardAtLeastOne();
    }

    private boolean validateVCardAtLeastOne() {
        vcardPointersValue = getPointerFromJPath(VCARD_PATH);
        List<String> titles = new ArrayList<>();
        logger.info("vcardVoicePointersValue size: {}", vcardPointersValue.size());

        for (String jsonPointer : vcardPointersValue) {
            JSONArray vcardArray = (JSONArray) jsonObject.query(jsonPointer);
            var vcardList = convertJsonArrayToList(vcardArray);
            vcardList.forEach(t -> {
                if(t.get(0) instanceof String title) {
                    titles.add(title);
                }
            });

            var atLeastOne = Stream.of("contact-uri", "email").anyMatch(titles::contains);
            if(!atLeastOne) {
                results.add(RDAPValidationResult.builder()
                        .code(-64101)
                        .value(getResultValue(vcardPointersValue))
                        .message("a redaction of Registrant Email must have either the email and contact-uri")
                        .build());
                return false;
            }
        }

        return validateMethodProperty(titles);
    }

    // Verify that the method property is present as is a JSON string of “replacementValue”.
    private boolean validateMethodProperty(List<String> titles) {
        if(Objects.isNull(redactedRegistrantEmail)) {
            logger.info("redactedRegistrantEmail object for method validations is null");
            return true;
        }

        try {
            var methodValue = redactedRegistrantEmail.get("method");
            logger.info("method property is found, so verify value");
            if(methodValue instanceof String method) {
                if(!method.trim().equalsIgnoreCase("replacementValue")) {
                    results.add(RDAPValidationResult.builder()
                            .code(-64102)
                            .value(getResultValue(redactedPointersValue))
                            .message("Registrant Email redaction method must be replacementValue")
                            .build());
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("method property is not found, no validations defined. Error: {}", e.getMessage());
        }

        if(titles.contains("email")) {
            return validateEmailRedactedProperties();
        } else if(titles.contains("contact-uri")) {
            return validateContactRedactedProperties();
        } else {
            return true;
        }
    }

    // If email exists in any VCard, following validations run
    private boolean validateEmailRedactedProperties() {
        if(Objects.isNull(redactedRegistrantEmail)) {
            logger.info("redactedRegistrantEmail object is null");
            return true;
        }

        Object pathLangValue;

        // If the pathLang property is either absent or is present as a JSON string of “jsonpath” verify postPath
        try {
            logger.info("Extracting pathLang...");
            pathLangValue = redactedRegistrantEmail.get("pathLang");
            if(pathLangValue instanceof String pathLang) {
                if (pathLang.trim().equalsIgnoreCase("jsonpath")) {
                    return validatePostPathBasedOnPathLang();
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("pathLang is not found due to {}", e.getMessage());
            return validatePostPathBasedOnPathLang();
        }
    }

    // Verify that the prePath property is either absent or is present with a valid JSONPath expression.
    private boolean validatePostPathBasedOnPathLang() {
        if(Objects.isNull(redactedRegistrantEmail)) {
            logger.info("redactedRegistrantEmail object for postPath validations is null");
            return true;
        }

        try {
            var postPathValue = redactedRegistrantEmail.get("postPath");
            logger.info("postPath property is found, so verify value");
            if(postPathValue instanceof String postPath) {
                try {
                    JsonPath.compile(postPath);
                    var postPathPointer = getPointerFromJPath(postPath);
                    logger.info("postPath pointer with size {}", postPathPointer.size());
                    if(postPathPointer.isEmpty()) {
                        results.add(RDAPValidationResult.builder()
                                .code(-64104)
                                .value(getResultValue(redactedPointersValue))
                                .message("jsonpath must evaluate to a non-empty set for redaction by replacementvalue of Registrant Email.")
                                .build());
                        return false;
                    }
                } catch (Exception e) {
                    // postPath is not a valid JSONPath expression
                    results.add(RDAPValidationResult.builder()
                            .code(-64103)
                            .value(getResultValue(redactedPointersValue))
                            .message("jsonpath is invalid for Registrant Email postPath")
                            .build());
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("postPath property is not found, no validations defined. Error: {}", e.getMessage());
        }

        return true;
    }

    // If contact uri exists in any VCard, following validations run
    private boolean validateContactRedactedProperties() {
        boolean replacementValidations;
        if(Objects.isNull(redactedRegistrantEmail)) {
            logger.info("redactedRegistrantEmail object is null");
            return true;
        }

        Object pathLangValue;

        // If the pathLang property is either absent or is present as a JSON string of “jsonpath” verify replacementPath
        try {
            logger.info("Extracting pathLang...");
            pathLangValue = redactedRegistrantEmail.get("pathLang");
            if(pathLangValue instanceof String pathLang) {
                if (pathLang.trim().equalsIgnoreCase("jsonpath")) {
                    replacementValidations = validateReplacementPathBasedOnPathLang();
                    if(replacementValidations) {
                        return validatePrePathBasedOnPathLang();
                    }
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("pathLang is not found due to {}", e.getMessage());
            replacementValidations =  validateReplacementPathBasedOnPathLang();
            if(replacementValidations) {
                return validatePrePathBasedOnPathLang();
            }
        }


        return replacementValidations;
    }

    // Verify that the prePath property is either absent or is present with a valid JSONPath expression.
    private boolean validateReplacementPathBasedOnPathLang() {
        if(Objects.isNull(redactedRegistrantEmail)) {
            logger.info("redactedRegistrantEmail object for postPath validations is null");
            return true;
        }

        try {
            var replacementPathValue = redactedRegistrantEmail.get("replacementPath");
            logger.info("replacementPath property is found, so verify value");
            if(replacementPathValue instanceof String replacementPath) {
                try {
                    JsonPath.compile(replacementPath);
                    var replacementPathPointer = getPointerFromJPath(replacementPath);
                    logger.info("replacementPath pointer with size {}", replacementPathPointer.size());
                    if(replacementPathPointer.isEmpty()) {
                        results.add(RDAPValidationResult.builder()
                                .code(-64107)
                                .value(getResultValue(redactedPointersValue))
                                .message("jsonpath must evaluate to a non-empty set for redaction by replacementvalue of Registrant Email in replacementPath")
                                .build());
                        return false;
                    }
                } catch (Exception e) {
                    // replacementPath is not a valid JSONPath expression
                    results.add(RDAPValidationResult.builder()
                            .code(-64105)
                            .value(getResultValue(redactedPointersValue))
                            .message("jsonpath is invalid for Registrant Email replacementPath")
                            .build());
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("replacementPath property is not found, no validations defined. Error: {}", e.getMessage());
        }

        return true;
    }

    private boolean validatePrePathBasedOnPathLang() {
        if(Objects.isNull(redactedRegistrantEmail)) {
            logger.info("redactedRegistrantEmail object for prePath validations is null");
            return true;
        }

        try {
            var prePathValue = redactedRegistrantEmail.get("prePath");
            logger.info("prePathValue property is found, so verify value");
            if(prePathValue instanceof String prePath) {
                try {
                    JsonPath.compile(prePath);
                    var prePathPointer = getPointerFromJPath(prePath);
                    logger.info("prePath pointer with size {}", prePathPointer.size());
                } catch (Exception e) {
                    // prePath is not a valid JSONPath expression
                    results.add(RDAPValidationResult.builder()
                            .code(-64106)
                            .value(getResultValue(redactedPointersValue))
                            .message("jsonpath is invalid for Registrant Email prePath")
                            .build());
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("prePath property is not found, no validations defined. Error: {}", e.getMessage());
        }

        return true;
    }

    private List<JSONArray> convertJsonArrayToList(JSONArray jsonArray) {
        List<JSONArray> arrayList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            arrayList.add(jsonArray.getJSONArray(i));
        }

        return arrayList;
    }
}

