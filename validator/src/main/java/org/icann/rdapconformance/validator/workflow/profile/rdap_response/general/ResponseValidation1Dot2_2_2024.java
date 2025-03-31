package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import java.util.Set;
import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public class ResponseValidation1Dot2_2_2024 extends RDAPConformanceValidation {
    public ResponseValidation1Dot2_2_2024(String rdapResponse, RDAPValidatorResults results) {
        super(rdapResponse, results, "redacted", -62001,
            "The RDAP Conformance data structure does not include redacted but\n"
                + "RFC 9537 is being used.");
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_1_2_Validation";
    }

    @Override
    protected boolean doValidate() {
        Set<String> set = getPointerFromJPath("$.redacted");
        if (set != null && !set.isEmpty()) {
            return super.doValidate();
        }

        return true;
    }
}
