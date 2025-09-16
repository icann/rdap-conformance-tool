package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public class TigValidation1Dot3_2024 extends RDAPConformanceValidation  {
    public TigValidation1Dot3_2024(String rdapResponse, RDAPValidatorResults results, RDAPValidatorConfiguration config) {
        super(rdapResponse, results, config, "icann_rdap_technical_implementation_guide_1",
            -61000,
            "The RDAP Conformance data structure does not include icann_rdap_technical_implementation_guide_1.");
    }

    @Override
    public String getGroupName() {
        return "tigSection_1_3_Validation";
    }
}
