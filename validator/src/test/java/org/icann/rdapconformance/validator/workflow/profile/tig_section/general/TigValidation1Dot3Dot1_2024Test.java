package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.testng.annotations.Test;

import java.util.List;

public class TigValidation1Dot3Dot1_2024Test extends ProfileJsonValidationTestBase {

    public TigValidation1Dot3Dot1_2024Test() {
        super("/validators/profile/rdapConformance/valid.json",
                "tigSection_1_3_1_Validation");
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new TigValidation1Dot3Dot1_2024(queryContext);
    }

    /**
     * Override base testValidate_ok() because valid.json contains
     * icann_rdap_technical_implementation_guide_0 which is the forbidden value for -61001.
     */
    @Test
    @Override
    public void testValidate_ok() {
        jsonObject.put("rdapConformance", List.of(
                "rdap_level_0",
                "icann_rdap_technical_implementation_guide_1",
                "icann_rdap_response_profile_0"
        ));
        validate();
    }

    /**
     * Test -61001: rdapConformance contains the obsolete value -> error.
     */
    @Test
    public void tigValidation1Dot3Dot1_2024_61001() {
        // valid.json already contains icann_rdap_technical_implementation_guide_0 — no changes needed
        validate(-61001,
                "#/rdapConformance:[\"rdap_level_0\",\"icann_rdap_technical_implementation_guide_0\",\"icann_rdap_response_profile_0\"]",
                "The RDAP Conformance data structure includes icann_rdap_technical_implementation_guide_0, which is obsolete.");
    }

    /**
     * rdapConformance present but does not contain the forbidden value -> passes.
     */
    @Test
    public void testConformanceWithoutForbiddenValue_passes() {
        jsonObject.put("rdapConformance", List.of(
                "rdap_level_0",
                "icann_rdap_technical_implementation_guide_1",
                "icann_rdap_response_profile_0"
        ));
        validate();
    }
}