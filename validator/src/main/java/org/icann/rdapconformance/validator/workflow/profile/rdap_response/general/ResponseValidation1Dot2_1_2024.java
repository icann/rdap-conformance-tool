package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public final class ResponseValidation1Dot2_1_2024 extends RDAPConformanceValidation {
    public ResponseValidation1Dot2_1_2024(String rdapResponse, RDAPValidatorResults results, RDAPValidatorConfiguration config) {
        super(rdapResponse, results, config, "icann_rdap_response_profile_1", -62000,
            "The RDAP Conformance data structure does not include icann_rdap_response_profile_1.");
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_1_2_Validation";
    }
}
