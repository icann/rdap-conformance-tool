package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Validates that for every object in the "redacted" array, the members
 * "postPath", "pathLang", "prePath", "replacementPath", and "method",
 * if present, are JSON strings.
 * Error code -62004.
 */
public class ResponseValidation1Dot2_6_2024 extends ProfileJsonValidation {

    private static final int CODE = -62004;
    private static final String MESSAGE = "The members 'postPath', 'pathLang', 'prePath', 'replacementPath', and 'method' must be strings.";
    private static final List<String> STRING_MEMBERS = List.of(
            "postPath", "pathLang", "prePath", "replacementPath", "method"
    );

    private final QueryContext queryContext;

    public ResponseValidation1Dot2_6_2024(QueryContext qctx) {
        super(qctx.getRdapResponseData(), qctx.getResults());
        this.queryContext = qctx;
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_1_2_6_Validation";
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

            for (String member : STRING_MEMBERS) {
                if (redactedItem.has(member) && !(redactedItem.get(member) instanceof String)) {
                    results.add(RDAPValidationResult.builder()
                            .code(CODE)
                            .value(redactedItem.toString())
                            .message(MESSAGE)
                            .build(queryContext));
                    isValid = false;
                    break; // one error per redacted object is enough
                }
            }
        }

        return isValid;
    }
}