package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class ResponseValidation1Dot2_5_2024Test extends ProfileJsonValidationTestBase {

    public ResponseValidation1Dot2_5_2024Test() {
        super("/validators/profile/rdapConformance/valid.json",
                "rdapResponseProfile_1_2_5_Validation");
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation1Dot2_5_2024(queryContext);
    }

    @Test
    public void testValid_NoRedactedMember_ShouldPass() {
        jsonObject.remove("redacted");
        updateQueryContextJsonData();
        validateOk(results);
    }

    @Test
    public void testValid_NameWithDescriptionOnly_ShouldPass() {
        jsonObject.put("redacted", new JSONArray()
                .put(new JSONObject().put("name",
                        new JSONObject().put("description", "Registry Domain ID"))));
        updateQueryContextJsonData();
        validateOk(results);
    }

    @Test
    public void testValid_NameWithTypeOnly_ExplicitTest_ShouldPass() {
        jsonObject.put("redacted", new JSONArray()
                .put(new JSONObject().put("name",
                        new JSONObject().put("type", "Registrant Name"))));
        updateQueryContextJsonData();
        validateOk(results);
    }

    @Test
    public void test62003_MissingNameMember_ShouldFail() {
        JSONObject redactedItem = new JSONObject().put("reason",
                new JSONObject().put("description", "Server policy"));
        jsonObject.put("redacted", new JSONArray().put(redactedItem));

        validate(-62003, redactedItem.toString(),
                "The 'name' must be an object with either the strings 'type' or 'description'");
    }

    @Test
    public void test62003_NameIsString_ShouldFail() {
        JSONObject redactedItem = new JSONObject().put("name", "Registrant Name");
        jsonObject.put("redacted", new JSONArray().put(redactedItem));

        validate(-62003, redactedItem.toString(),
                "The 'name' must be an object with either the strings 'type' or 'description'");
    }

    @Test
    public void test62003_NameHasBothTypeAndDescription_ShouldFail() {
        JSONObject redactedItem = new JSONObject().put("name",
                new JSONObject()
                        .put("type", "Registrant Name")
                        .put("description", "Registry Domain ID"));
        jsonObject.put("redacted", new JSONArray().put(redactedItem));

        validate(-62003, redactedItem.toString(),
                "The 'name' must be an object with either the strings 'type' or 'description'");
    }

    @Test
    public void test62003_NameIsEmptyObject_ShouldFail() {
        JSONObject redactedItem = new JSONObject().put("name", new JSONObject());
        jsonObject.put("redacted", new JSONArray().put(redactedItem));

        validate(-62003, redactedItem.toString(),
                "The 'name' must be an object with either the strings 'type' or 'description'");
    }

    @Test
    public void testValid_RedactedNotArray_ShouldSkip() {
        // Not a JSONArray — handled by -62002, this validator skips
        jsonObject.put("redacted", "not an array");
        updateQueryContextJsonData();
        validateOk(results);
    }

    @Test
    public void test62003_NameHasBothKeysOneNonString_ShouldFail() {
        // Both keys present — fails on key exclusivity regardless of value types
        JSONObject redactedItem = new JSONObject().put("name",
                new JSONObject()
                        .put("type", "Registrant Name")
                        .put("description", 123));
        jsonObject.put("redacted", new JSONArray().put(redactedItem));

        validate(-62003, redactedItem.toString(),
                "The 'name' must be an object with either the strings 'type' or 'description'");
    }

    @Test
    public void test62003_NameTypeIsNonString_ShouldFail() {
        // Only "type" present but value is not a string
        JSONObject redactedItem = new JSONObject().put("name",
                new JSONObject().put("type", 123));
        jsonObject.put("redacted", new JSONArray().put(redactedItem));

        validate(-62003, redactedItem.toString(),
                "The 'name' must be an object with either the strings 'type' or 'description'");
    }

    @Test
    public void test62003_NameDescriptionIsNonString_ShouldFail() {
        // Only "description" present but value is not a string
        JSONObject redactedItem = new JSONObject().put("name",
                new JSONObject().put("description", new JSONObject()));
        jsonObject.put("redacted", new JSONArray().put(redactedItem));

        validate(-62003, redactedItem.toString(),
                "The 'name' must be an object with either the strings 'type' or 'description'");
    }
}