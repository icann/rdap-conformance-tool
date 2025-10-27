package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import java.util.Set;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public class StdRdapConformanceValidation_2024 extends ProfileJsonValidation {
    public StdRdapConformanceValidation_2024(QueryContext qctx) {
        super(qctx.getRdapResponseData(), qctx.getResults());
    }

    @Override
    public String getGroupName() {
        return "stdRdapConformanceValidation";
    }

    @Override
    protected boolean doValidate() {
        boolean result = true;
        String jsonPath = "$..rdapConformance";

        Set<String> pointers = getPointerFromJPath(jsonPath);

        if (pointers.isEmpty()) {
            results.add(RDAPValidationResult.builder()
                .code(-10504)
                .value(jsonObject.toString())
                .message("RFC 9083 requires all RDAP responses to have an rdapConformance array.")
                .build());
            return false;
        }

        for (String pointer : pointers) {
            if (!pointer.equalsIgnoreCase("#/rdapConformance") && !pointer.equalsIgnoreCase("/rdapConformance")) {
                results.add(RDAPValidationResult.builder()
                    .code(-10505)
                    .value(pointer)
                    .message("The rdapConformance array must appear only in the top-most of the RDAP response.")
                    .build());
                result = false;
            }
        }

        return result;
    }
}
