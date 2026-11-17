package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.junit.jupiter.api.Test;

/**
 * Verifies that a malformed redacted object (MissingType) does not prevent
 * validation of subsequent valid redacted objects.
 */
public class ResponseValidation2Dot7Dot6Dot2_2024MalformedMissingTypeTest extends ProfileJsonValidationTestBase {
    public ResponseValidation2Dot7Dot6Dot2_2024MalformedMissingTypeTest() {
        super("/validators/profile/response_validations/vcard/malformed_missing_type.json",
                "rdapResponseProfile_2_7_6_2_Validation");
    }

    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot7Dot6Dot2_2024(queryContext);
    }

    @Test
    public void testMalformedRedactedArray_MissingType() {
        // This should pass validation because "Tech Phone" redaction exists at index 1,
        // even though index 0 has empty "name" object without "type"
        validate(); // Should NOT generate -65100 error
    }
}
