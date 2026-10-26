package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import java.util.Set;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gTLD Profile v3 conformance: the rdapConformance data structure must not include
 * any conformance identifier from an obsolete gTLD RDAP Profile. Emits -66001 when
 * any of the forbidden identifiers is present.
 */
public final class ResponseValidationGtldProfile3ObsoleteIdentifiers extends ProfileJsonValidation {

    private static final Logger logger =
        LoggerFactory.getLogger(ResponseValidationGtldProfile3ObsoleteIdentifiers.class);

    private static final Set<String> FORBIDDEN_VALUES = Set.of(
        "icann_rdap_technical_implementation_guide_0",
        "icann_rdap_technical_implementation_guide_1",
        "icann_rdap_response_profile_0",
        "icann_rdap_response_profile_1");

    private static final String JSON_POINTER = "#/rdapConformance";

    private final QueryContext queryContext;

    public ResponseValidationGtldProfile3ObsoleteIdentifiers(QueryContext queryContext) {
        super(queryContext.getRdapResponseData(), queryContext.getResults());
        this.queryContext = queryContext;
    }

    @Override
    public String getGroupName() {
        return "gtldProfile3ObsoleteProfile_Validation";
    }

    @Override
    protected boolean doValidate() {
        JSONArray rdapConformance = jsonObject.optJSONArray("rdapConformance");

        if (rdapConformance == null) {
            logger.debug("The rdapConformance array is missing or null, skipping -66001 check.");
            return true;
        }

        for (int i = 0; i < rdapConformance.length(); i++) {
            if (FORBIDDEN_VALUES.contains(rdapConformance.optString(i))) {
                results.add(RDAPValidationResult.builder()
                    .code(-66001)
                    .value(getResultValue(JSON_POINTER))
                    .message("The RDAP Conformance data structure contains a conformance "
                        + "identifier from an obsolete gTLD RDAP Profile.")
                    .build(queryContext));
                return false;
            }
        }

        return true;
    }
}
