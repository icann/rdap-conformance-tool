package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Set;

/**
 * Validates that for every object in the "redacted" array, if a "reason" member
 * is present, it is a JSON object containing only the optional string members
 * "lang", "type", and "description".
 * Error code -62005.
 */
public class ResponseValidation1Dot2_7_2024 extends ProfileJsonValidation {

    private static final int CODE = -62005;
    private static final String MESSAGE = "The 'reason' member is an object and may only contain the optional string 'lang', 'type', and 'description'.";
    private static final Set<String> ALLOWED_REASON_KEYS = Set.of("lang", "type", "description");

    private final QueryContext queryContext;

    public ResponseValidation1Dot2_7_2024(QueryContext qctx) {
        super(qctx.getRdapResponseData(), qctx.getResults());
        this.queryContext = qctx;
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_1_2_7_Validation";
    }

    @Override
    protected boolean doValidate() {
        // Only validate if "redacted" is present in the topmost object
        if (!jsonObject.has("redacted")) {
            return true;
        }

        Object redactedObj = jsonObject.get("redacted");

        // If "redacted" is not a JSONArray, skip — handled by -62002
        if (!(redactedObj instanceof JSONArray redactedArray)) {
            return true;
        }

        boolean isValid = true;

        for (int i = 0; i < redactedArray.length(); i++) {
            Object element = redactedArray.get(i);

            // If element is not a JSONObject, skip — handled by -62002
            if (!(element instanceof JSONObject redactedItem)) {
                continue;
            }

            // "reason" is optional — only validate if present
            if (!redactedItem.has("reason")) {
                continue;
            }

            if (!isValidReason(redactedItem)) {
                results.add(RDAPValidationResult.builder()
                        .code(CODE)
                        .value(redactedItem.toString())
                        .message(MESSAGE)
                        .build(queryContext));
                isValid = false;
            }
        }

        return isValid;
    }

    private boolean isValidReason(JSONObject redactedItem) {
        Object reasonObj = redactedItem.get("reason");

        // "reason" must be a JSONObject
        if (!(reasonObj instanceof JSONObject reason)) {
            return false;
        }

        // Must not contain keys other than "lang", "type", "description"
        for (String key : reason.keySet()) {
            if (!ALLOWED_REASON_KEYS.contains(key)) {
                return false;
            }
        }

        // Each present key's value must be a string
        for (String key : ALLOWED_REASON_KEYS) {
            if (reason.has(key) && !(reason.get(key) instanceof String)) {
                return false;
            }
        }

        return true;
    }
}