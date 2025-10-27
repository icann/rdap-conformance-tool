package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import java.util.Set;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

/**
 * 8.8.1.3 & 8.8.1.4
 */
public class ResponseValidation2Dot7Dot1DotXAndRelated3And4_2024 extends
    ResponseValidation2Dot7Dot1DotXAndRelated {

    public ResponseValidation2Dot7Dot1DotXAndRelated3And4_2024(QueryContext qctx) {
        super(qctx.getRdapResponseData(), qctx.getResults(), qctx.getQueryType(), qctx.getConfig());
    }

    @Override
    protected boolean doValidateEntity(String jsonPointer, JSONObject entity) {
        if (isChildOfRegistrar(jsonPointer)) {
            return true;
        }

        Set<String> withRemarkTitleRedactedForPrivacy =
            getPointerFromJPath(entity, "$.remarks[?(@.title == 'REDACTED FOR PRIVACY')]");

        if (withRemarkTitleRedactedForPrivacy.isEmpty()) {
            return this.validateHandle(jsonPointer + "/handle");
        }

        return true;
    }

    private boolean validateHandle(String handleJsonPointer) {
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
                .code(-52106)
                .value(getResultValue(handleJsonPointer))
                .message(
                    "The globally unique identifier in the entity object handle is using an EPPROID reserved for testing by ICANN.")
                .build());
            return false;
        }

        return true;
    }
}
