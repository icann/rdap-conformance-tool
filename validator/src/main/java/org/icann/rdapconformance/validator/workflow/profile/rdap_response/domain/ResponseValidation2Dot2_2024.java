package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public final class ResponseValidation2Dot2_2024 extends ProfileJsonValidation {
    private final RDAPQueryType queryType;

    public ResponseValidation2Dot2_2024(QueryContext qctx) {
        super(qctx.getRdapResponseData(), qctx.getResults());

        this.queryType = qctx.getQueryType();
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_2_2_Validation";
    }

    @Override
    protected boolean doValidate() {
        String handle = "";

        Object obj = jsonObject.query("#/handle");
        if (obj != null) {
            // have to use .toString() instead of cast (String),
            // because if the value is JSONObject.NULL, it won't cast
            // added testValidate_HandleIsNull_AddErrorCode unit test for this
            handle = obj.toString();
        }

        if (handle != null && handle.endsWith("-ICANNRST")) {
            results.add(RDAPValidationResult.builder()
                .code(-46205)
                .value(getResultValue("#/handle"))
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
