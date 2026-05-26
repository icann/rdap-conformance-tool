package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.testng.annotations.Test;

public class ResponseValidation1Dot2_3_2024Test extends ProfileJsonValidationTestBase {

    public ResponseValidation1Dot2_3_2024Test() {
        super("/validators/profile/rdapConformance/valid.json",
                "rdapResponseProfile_1_2_3_Validation");
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation1Dot2_3_2024(queryContext);
    }

    @Test
    public void testValid_RedactedIsArrayOfObjects_ShouldPass() {
        // Base fixture already has "redacted" as an array of objects — should pass
        validateOk(results);
    }

    @Test
    public void testValid_NoRedactedMember_ShouldPass() {
        // If "redacted" is absent, validation must be skipped
        jsonObject.remove("redacted");
        validateOk(results);
    }

    @Test
    public void test62002_RedactedIsString_ShouldFail() {
        // "redacted" is a plain string, not an array
        jsonObject.put("redacted", "some string");
        validate(-62002, "some string",
                "The 'redacted' JSON member must be an array of objects.");
    }

    @Test
    public void test62002_RedactedIsObject_NotArray_ShouldFail() {
        // "redacted" is a JSON object, not an array
        jsonObject.put("redacted", new org.json.JSONObject("{\"name\":{\"description\":\"test\"}}"));

        validate(-62002, "{\"name\":{\"description\":\"test\"}}",
                "The 'redacted' JSON member must be an array of objects.");
    }

    @Test
    public void test62002_RedactedIsArrayOfStrings_ShouldFail() {
        // "redacted" is an array, but its elements are strings not objects
        jsonObject.put("redacted", new JSONArray().put("item1").put("item2"));

        validate(-62002, "[\"item1\",\"item2\"]",
                "The 'redacted' JSON member must be an array of objects.");
    }

    @Test
    public void test62002_RedactedIsEmptyArray_ShouldPass() {
        // An empty array is technically valid (no elements to object-check)
        jsonObject.put("redacted", new JSONArray());

        validateOk(results);
    }
}