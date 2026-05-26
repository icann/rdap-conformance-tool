package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Validates that the "redacted" JSON member, if present, is an array of JSON objects.
 * Error code -62002.
 */
public class ResponseValidation1Dot2_3_2024 extends ProfileJsonValidation {

    private static final int CODE = -62002;
    private static final String MESSAGE = "The 'redacted' JSON member must be an array of objects.";

    private final QueryContext queryContext;

    public ResponseValidation1Dot2_3_2024(QueryContext qctx) {
        super(qctx.getRdapResponseData(), qctx.getResults());
        this.queryContext = qctx;
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_1_2_3_Validation";
    }

    @Override
    protected boolean doValidate() {
        // Only validate if "redacted" is present in the topmost object
        if (!jsonObject.has("redacted")) {
            return true;
        }

        Object redacted = jsonObject.get("redacted");

        // Must be a JSONArray
        if (!(redacted instanceof JSONArray redactedArray)) {
            results.add(RDAPValidationResult.builder()
                    .code(CODE)
                    .value(redacted.toString())
                    .message(MESSAGE)
                    .build(queryContext));
            return false;
        }

        // Each element of the array must be a JSONObject
        for (int i = 0; i < redactedArray.length(); i++) {
            Object element = redactedArray.get(i);
            if (!(element instanceof JSONObject)) {
                results.add(RDAPValidationResult.builder()
                        .code(CODE)
                        .value(redactedArray.toString())
                        .message(MESSAGE)
                        .build(queryContext));
                return false;
            }
        }

        return true;
    }
}