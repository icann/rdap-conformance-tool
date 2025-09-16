package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test class to verify that malformed redacted array objects don't prevent
 * validation of subsequent valid redacted objects.
 */
public class ResponseValidation2Dot7Dot6Dot2_2024MalformedTest extends ProfileJsonValidationTestBase {

    public ResponseValidation2Dot7Dot6Dot2_2024MalformedTest() {
        super("/validators/profile/response_validations/vcard/malformed_redacted_test.json",
                "rdapResponseProfile_2_7_6_2_Validation");
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot7Dot6Dot2_2024(
                jsonObject.toString(),
                results,
                config);
    }

    @Test
    public void testMalformedRedactedArray_NullName() {
        // This should pass validation because "Tech Phone" redaction exists at index 1, 
        // even though index 0 has malformed "name": null
        validate(); // Should NOT generate -65100 error
    }

    public static class MissingNameTest extends ProfileJsonValidationTestBase {
        public MissingNameTest() {
            super("/validators/profile/response_validations/vcard/malformed_missing_name.json",
                    "rdapResponseProfile_2_7_6_2_Validation");
        }

        @Override
        public ProfileValidation getProfileValidation() {
            return new ResponseValidation2Dot7Dot6Dot2_2024(
                    jsonObject.toString(),
                    results,
                    config);
        }

        @Test
        public void testMalformedRedactedArray_MissingName() {
            // This should pass validation because "Tech Phone" redaction exists at index 1,
            // even though index 0 is missing "name" property entirely
            validate(); // Should NOT generate -65100 error
        }
    }

    public static class NameNotObjectTest extends ProfileJsonValidationTestBase {
        public NameNotObjectTest() {
            super("/validators/profile/response_validations/vcard/malformed_name_string.json",
                    "rdapResponseProfile_2_7_6_2_Validation");
        }

        @Override
        public ProfileValidation getProfileValidation() {
            return new ResponseValidation2Dot7Dot6Dot2_2024(
                    jsonObject.toString(),
                    results,
                    config);
        }

        @Test
        public void testMalformedRedactedArray_NameNotObject() {
            // This should pass validation because "Tech Phone" redaction exists at index 1,
            // even though index 0 has "name" as a string instead of object
            validate(); // Should NOT generate -65100 error
        }
    }

    public static class MissingTypeTest extends ProfileJsonValidationTestBase {
        public MissingTypeTest() {
            super("/validators/profile/response_validations/vcard/malformed_missing_type.json",
                    "rdapResponseProfile_2_7_6_2_Validation");
        }

        @Override
        public ProfileValidation getProfileValidation() {
            return new ResponseValidation2Dot7Dot6Dot2_2024(
                    jsonObject.toString(),
                    results,
                    config);
        }

        @Test
        public void testMalformedRedactedArray_MissingType() {
            // This should pass validation because "Tech Phone" redaction exists at index 1,
            // even though index 0 has empty "name" object without "type"
            validate(); // Should NOT generate -65100 error
        }
    }
}