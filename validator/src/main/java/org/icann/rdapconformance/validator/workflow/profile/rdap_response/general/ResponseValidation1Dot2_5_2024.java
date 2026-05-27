package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Validates that every object in the "redacted" array has a "name" member,
 * and that "name" is a JSON object containing either "type" or "description"
 * (as JSON strings) but not both.
 * Error code -62003.
 */
public class ResponseValidation1Dot2_5_2024 extends ProfileJsonValidation {

    private static final int CODE = -62003;
    private static final String MESSAGE = "The 'name' must be an object with either the strings 'type' or 'description'";

    private final QueryContext queryContext;

    public ResponseValidation1Dot2_5_2024(QueryContext qctx) {
        super(qctx.getRdapResponseData(), qctx.getResults());
        this.queryContext = qctx;
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_1_2_5_Validation";
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

            if (!isValidNameMember(redactedItem)) {
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

    private boolean isValidNameMember(JSONObject redactedItem) {
        // "name" must be present
        if (!redactedItem.has("name")) {
            return false;
        }

        Object nameObj = redactedItem.get("name");

        // "name" must be a JSONObject
        if (!(nameObj instanceof JSONObject name)) {
            return false;
        }

        boolean hasType = name.has("type") && name.get("type") instanceof String;
        boolean hasDescription = name.has("description") && name.get("description") instanceof String;

        // Must have either "type" or "description" but not both
        return hasType ^ hasDescription;
    }
}