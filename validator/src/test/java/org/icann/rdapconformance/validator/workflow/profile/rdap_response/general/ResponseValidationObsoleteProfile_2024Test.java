package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.testng.annotations.Test;

import java.util.List;

public class ResponseValidationObsoleteProfile_2024Test extends ProfileJsonValidationTestBase {

    public ResponseValidationObsoleteProfile_2024Test() {
        super("/validators/profile/rdapConformance/valid.json",
                "rdapResponseProfile_obsoleteProfile_Validation");
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidationObsoleteProfile_2024(queryContext);
    }

    /**
     * Override base testValidate_ok() because valid.json contains
     * icann_rdap_response_profile_0 which is the forbidden value for -62006.
     */
    @Test
    @Override
    public void testValidate_ok() {
        jsonObject.put("rdapConformance", List.of(
                "rdap_level_0",
                "icann_rdap_technical_implementation_guide_1",
                "icann_rdap_response_profile_1"
        ));
        validate();
    }

    /**
     * Test -62006: rdapConformance contains the obsolete value -> error.
     * valid.json already contains icann_rdap_response_profile_0, no changes needed.
     */
    @Test
    public void ResponseValidationObsoleteProfile_2024_62006() {
        // valid.json already contains icann_rdap_response_profile_0
        validate(-62006,
                "#/rdapConformance:[\"rdap_level_0\",\"icann_rdap_technical_implementation_guide_0\",\"icann_rdap_response_profile_0\"]",
                "The RDAP Conformance data structure includes icann_rdap_response_profile_0, which is obsolete.");
    }

    /**
     * rdapConformance present without the forbidden value -> passes.
     */
    @Test
    public void testConformanceWithoutForbiddenValue_passes() {
        jsonObject.put("rdapConformance", List.of(
                "rdap_level_0",
                "icann_rdap_technical_implementation_guide_1",
                "icann_rdap_response_profile_1"
        ));
        validate();
    }
}