package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import java.util.Set;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public class StdRdapNameserversSearchValidation_2024  extends ProfileJsonValidation {
    public StdRdapNameserversSearchValidation_2024(String rdapResponse, RDAPValidatorResults results) {
        super(rdapResponse, results);
    }

    @Override
    public String getGroupName() {
        return "stdRdapNameserversSearchValidation";
    }

    @Override
    protected boolean doValidate() {
        boolean result = true;
        String jsonPath = "$.nameserverSearchResults";

        Set<String> pointers = getPointerFromJPath(jsonPath);

        if (pointers.isEmpty()) {
            results.add(RDAPValidationResult.builder()
                .code(-12610)
                .value(jsonObject.toString())
                .message("The nameserverSearchResults structure is required")
                .build());
            return false;
        }

        return result;
    }

}
