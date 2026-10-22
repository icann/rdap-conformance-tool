package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidation;

/**
 * gTLD Profile v3 conformance: the rdapConformance data structure must include
 * the "gtldProfile3" identifier. Emits -66000 when missing.
 */
public final class ResponseValidationGtldProfile3 extends RDAPConformanceValidation {

    public ResponseValidationGtldProfile3(QueryContext queryContext) {
        super(queryContext, "gtldProfile3", -66000,
            "The RDAP Conformance data structure does not include gtldProfile3.");
    }

    @Override
    public String getGroupName() {
        return "gtldProfile3Conformance_Validation";
    }
}
