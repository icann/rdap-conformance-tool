package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseValidationObsoleteProfile_2024 extends ProfileJsonValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidationObsoleteProfile_2024.class);
    private static final String FORBIDDEN_VALUE = "icann_rdap_response_profile_0";
    private static final String JSON_POINTER = "#/rdapConformance";

    private final QueryContext queryContext;

    public ResponseValidationObsoleteProfile_2024(QueryContext qctx) {
        super(qctx.getRdapResponseData(), qctx.getResults());
        this.queryContext = qctx;
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_obsoleteProfile_Validation";
    }

    @Override
    protected boolean doValidate() {
        JSONArray rdapConformance = jsonObject.optJSONArray("rdapConformance");

        if (rdapConformance == null) {
            logger.info("The rdapConformance array is missing or null, skipping -62002 check.");
            return true;
        }

        for (int i = 0; i < rdapConformance.length(); i++) {
            if (FORBIDDEN_VALUE.equals(rdapConformance.optString(i))) {
                results.add(RDAPValidationResult.builder()
                        .code(-62002)
                        .value(getResultValue(JSON_POINTER))
                        .message("The RDAP Conformance data structure includes icann_rdap_response_profile_0, which is obsolete.")
                        .build(queryContext));
                return false;
            }
        }

        return true;
    }
}