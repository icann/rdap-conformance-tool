package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class TigValidation1Dot3Dot1_2024 extends ProfileJsonValidation {

    private static final Logger logger = LoggerFactory.getLogger(TigValidation1Dot3Dot1_2024.class);
    private static final String FORBIDDEN_VALUE = "icann_rdap_technical_implementation_guide_0";
    private static final String JSON_POINTER = "#/rdapConformance";

    private final QueryContext queryContext;

    public TigValidation1Dot3Dot1_2024(QueryContext qctx) {
        super(qctx.getRdapResponseData(), qctx.getResults());
        this.queryContext = qctx;
    }

    @Override
    public String getGroupName() {
        return "tigSection_1_3_1_Validation";
    }

    @Override
    protected boolean doValidate() {
        JSONArray rdapConformance = (JSONArray) new JSONObject(jsonObject.toString())
                .query(JSON_POINTER);

        if (rdapConformance == null) {
            logger.info("The rdapConformance array is missing or null, skipping -61001 check.");
            return true;
        }

        Set<String> values = new HashSet<>();
        rdapConformance.forEach(v -> values.add(v.toString()));

        if (values.contains(FORBIDDEN_VALUE)) {
            results.add(RDAPValidationResult.builder()
                    .code(-61001)
                    .value(getResultValue(JSON_POINTER))
                    .message("The RDAP Conformance data structure includes icann_rdap_technical_implementation_guide_0, which is obsolete.")
                    .build(queryContext));
            return false;
        }

        return true;
    }
}