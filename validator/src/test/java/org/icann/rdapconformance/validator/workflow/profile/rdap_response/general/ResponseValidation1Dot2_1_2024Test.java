package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import java.util.List;
import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidation;
import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidationTest;

public class ResponseValidation1Dot2_1_2024Test extends RDAPConformanceValidationTest {

    public ResponseValidation1Dot2_1_2024Test() {
        super("rdapResponseProfile_1_2_Validation");
    }

    @Override
    public void testValidate_ok() {
        jsonObject.put("rdapConformance", List.of("rdap_level_0", "icann_rdap_technical_implementation_guide_1", "icann_rdap_response_profile_1"));
        validate();
    }

    @Override
    public RDAPConformanceValidation getProfileValidation() {
        return new ResponseValidation1Dot2_1_2024(jsonObject.toString(), results, config);
    }
}
