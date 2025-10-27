package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import java.util.List;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidation;
import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidationTest;
import org.testng.annotations.Test;

public class TigValidation1Dot3_2024Test extends RDAPConformanceValidationTest {

    protected TigValidation1Dot3_2024Test() {
        super("tigSection_1_3_Validation");
    }

    @Override
    public RDAPConformanceValidation getProfileValidation() {
        return new TigValidation1Dot3_2024(queryContext);
    }

    @Override
    public void testValidate_ok() {
        jsonObject.put("rdapConformance", List.of("rdap_level_0", "icann_rdap_technical_implementation_guide_1","icann_rdap_response_profile_0"));
        validate();
    }
}
