package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.icann.rdapconformance.validator.CommonUtils.ONE;

import java.util.Set;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;


public final class ResponseValidation2Dot9Dot1And2Dot9Dot2_2024 extends ProfileJsonValidation {

    public static final String NAMESERVERS_PATH = "$.nameservers[*]";
    public static final String HANDLE_PATH = "$.handle";

    private final RDAPQueryType queryType;

    public ResponseValidation2Dot9Dot1And2Dot9Dot2_2024(String rdapResponse, RDAPValidatorResults results, RDAPQueryType queryType) {
        super(rdapResponse, results);

        this.queryType = queryType;
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_2_9_1_and_2_9_2_Validation";
    }

    @Override
    protected boolean doValidate() {
        boolean isValid = true;
        Set<String> jsonPointers = getPointerFromJPath(NAMESERVERS_PATH);
        for (String jsonPointer : jsonPointers) {
            isValid &= checkHandleForNameServers(jsonPointer);
        }

        return isValid;
    }

    public boolean checkHandleForNameServers(String nameserverJsonPointer) {
        boolean isValid = true;
        JSONObject nameserver = (JSONObject) jsonObject.query(nameserverJsonPointer);
        Set<String> jsonPointers = getPointerFromJPath(nameserver, HANDLE_PATH);
        for (String jsonPointer : jsonPointers) {
            isValid &= validateHandle(nameserverJsonPointer.concat(jsonPointer.substring(ONE)));
        }
        return isValid;
    }

    public boolean validateHandle(String handleJsonPointer) {
        String handle = "";
        Object obj = jsonObject.query(handleJsonPointer);
        if (obj != null) {
            // have to use .toString() instead of cast (String),
            // because if the value is JSONObject.NULL, it won't cast
            // added testValidate_HandleIsNull_AddErrorCode unit test for this
            handle = obj.toString();
        }

        if (handle != null && handle.endsWith("-ICANNRST")) {
            results.add(RDAPValidationResult.builder()
                .code(-47205)
                .value(getResultValue(handleJsonPointer))
                .message(
                    "The globally unique identifier in the domain object handle is using an EPPROID reserved for testing by ICANN.")
                .build());

            return false;
        }

        return true;
    }


    @Override
    public boolean doLaunch() {
        return queryType.equals(RDAPQueryType.DOMAIN);
    }
}