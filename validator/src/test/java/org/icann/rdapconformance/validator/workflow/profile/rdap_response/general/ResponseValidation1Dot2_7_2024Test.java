package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class ResponseValidation1Dot2_7_2024Test extends ProfileJsonValidationTestBase {

    public ResponseValidation1Dot2_7_2024Test() {
        super("/validators/profile/rdapConformance/valid.json",
                "rdapResponseProfile_1_2_7_Validation");
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation1Dot2_7_2024(queryContext);
    }

    @Test
    public void testValid_BaseFixture_ShouldPass() {
        validateOk(results);
    }

    @Test
    public void testValid_NoRedactedMember_ShouldPass() {
        jsonObject.remove("redacted");
        updateQueryContextJsonData();
        validateOk(results);
    }

    @Test
    public void testValid_ReasonAbsent_ShouldPass() {
        // "reason" is optional — object without it is valid
        jsonObject.put("redacted", new JSONArray()
                .put(new JSONObject()
                        .put("name", new JSONObject().put("type", "Registrant Name"))
                        .put("method", "removal")));
        updateQueryContextJsonData();
        validateOk(results);
    }

    @Test
    public void testValid_ReasonWithDescriptionOnly_ShouldPass() {
        JSONObject redactedItem = new JSONObject()
                .put("name", new JSONObject().put("type", "Registrant Name"))
                .put("reason", new JSONObject().put("description", "Server policy"));
        jsonObject.put("redacted", new JSONArray().put(redactedItem));
        updateQueryContextJsonData();
        validateOk(results);
    }

    @Test
    public void testValid_ReasonWithAllAllowedKeys_ShouldPass() {
        JSONObject redactedItem = new JSONObject()
                .put("name", new JSONObject().put("type", "Registrant Name"))
                .put("reason", new JSONObject()
                        .put("lang", "en")
                        .put("type", "Server policy")
                        .put("description", "Data redacted for privacy"));
        jsonObject.put("redacted", new JSONArray().put(redactedItem));
        updateQueryContextJsonData();
        validateOk(results);
    }

    @Test
    public void testValid_RedactedNotArray_ShouldSkip() {
        jsonObject.put("redacted", "not an array");
        updateQueryContextJsonData();
        validateOk(results);
    }

    @Test
    public void test62005_ReasonIsString_ShouldFail() {
        JSONObject redactedItem = new JSONObject()
                .put("name", new JSONObject().put("type", "Registrant Name"))
                .put("reason", "Server policy");
        jsonObject.put("redacted", new JSONArray().put(redactedItem));

        validate(-62005, redactedItem.toString(),
                "The 'reason' member is an object and may only contain the optional string 'lang', 'type', and 'description'.");
    }

    @Test
    public void test62005_ReasonIsArray_ShouldFail() {
        JSONObject redactedItem = new JSONObject()
                .put("name", new JSONObject().put("type", "Registrant Name"))
                .put("reason", new JSONArray().put("Server policy"));
        jsonObject.put("redacted", new JSONArray().put(redactedItem));

        validate(-62005, redactedItem.toString(),
                "The 'reason' member is an object and may only contain the optional string 'lang', 'type', and 'description'.");
    }

    @Test
    public void test62005_ReasonHasUnknownKey_ShouldFail() {
        JSONObject redactedItem = new JSONObject()
                .put("name", new JSONObject().put("type", "Registrant Name"))
                .put("reason", new JSONObject()
                        .put("description", "Server policy")
                        .put("unknownKey", "value"));
        jsonObject.put("redacted", new JSONArray().put(redactedItem));

        validate(-62005, redactedItem.toString(),
                "The 'reason' member is an object and may only contain the optional string 'lang', 'type', and 'description'.");
    }

    @Test
    public void test62005_ReasonDescriptionIsNotString_ShouldFail() {
        JSONObject redactedItem = new JSONObject()
                .put("name", new JSONObject().put("type", "Registrant Name"))
                .put("reason", new JSONObject().put("description", 123));
        jsonObject.put("redacted", new JSONArray().put(redactedItem));

        validate(-62005, redactedItem.toString(),
                "The 'reason' member is an object and may only contain the optional string 'lang', 'type', and 'description'.");
    }

    @Test
    public void test62005_ReasonTypeIsNotString_ShouldFail() {
        JSONObject redactedItem = new JSONObject()
                .put("name", new JSONObject().put("type", "Registrant Name"))
                .put("reason", new JSONObject().put("type", new JSONObject()));
        jsonObject.put("redacted", new JSONArray().put(redactedItem));

        validate(-62005, redactedItem.toString(),
                "The 'reason' member is an object and may only contain the optional string 'lang', 'type', and 'description'.");
    }

    @Test
    public void test62005_ReasonLangIsNotString_ShouldFail() {
        JSONObject redactedItem = new JSONObject()
                .put("name", new JSONObject().put("type", "Registrant Name"))
                .put("reason", new JSONObject().put("lang", false));
        jsonObject.put("redacted", new JSONArray().put(redactedItem));

        validate(-62005, redactedItem.toString(),
                "The 'reason' member is an object and may only contain the optional string 'lang', 'type', and 'description'.");
    }
}