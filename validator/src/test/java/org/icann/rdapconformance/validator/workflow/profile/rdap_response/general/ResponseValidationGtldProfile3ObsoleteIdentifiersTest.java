package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import java.util.List;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.testng.annotations.Test;

public class ResponseValidationGtldProfile3ObsoleteIdentifiersTest extends ProfileJsonValidationTestBase {

    private static final String MESSAGE =
        "The RDAP Conformance data structure contains a conformance identifier "
            + "from an obsolete gTLD RDAP Profile.";

    public ResponseValidationGtldProfile3ObsoleteIdentifiersTest() {
        super("/validators/profile/rdapConformance/valid.json",
            "gtldProfile3ObsoleteProfile_Validation");
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidationGtldProfile3ObsoleteIdentifiers(queryContext);
    }

    /**
     * Override base testValidate_ok() because valid.json contains obsolete
     * identifiers forbidden under gTLD Profile v3.
     */
    @Test
    @Override
    public void testValidate_ok() {
        jsonObject.put("rdapConformance", List.of("rdap_level_0", "gtldProfile3"));
        validate();
    }

    /**
     * valid.json already contains icann_rdap_technical_implementation_guide_0
     * and icann_rdap_response_profile_0 -> -66001.
     */
    @Test
    public void testObsoleteIdentifiersFromFixture_66001() {
        validate(-66001,
            "#/rdapConformance:[\"rdap_level_0\",\"icann_rdap_technical_implementation_guide_0\",\"icann_rdap_response_profile_0\"]",
            MESSAGE);
    }

    @Test
    public void testTechnicalImplementationGuide1_66001() {
        jsonObject.put("rdapConformance",
            List.of("rdap_level_0", "gtldProfile3", "icann_rdap_technical_implementation_guide_1"));
        validate(-66001,
            "#/rdapConformance:[\"rdap_level_0\",\"gtldProfile3\",\"icann_rdap_technical_implementation_guide_1\"]",
            MESSAGE);
    }

    @Test
    public void testResponseProfile1_66001() {
        jsonObject.put("rdapConformance",
            List.of("rdap_level_0", "gtldProfile3", "icann_rdap_response_profile_1"));
        validate(-66001,
            "#/rdapConformance:[\"rdap_level_0\",\"gtldProfile3\",\"icann_rdap_response_profile_1\"]",
            MESSAGE);
    }

    /**
     * Missing rdapConformance array is out of scope for -66001 (covered by other validations).
     */
    @Test
    public void testMissingRdapConformance_passes() {
        jsonObject.remove("rdapConformance");
        validate();
    }
}
