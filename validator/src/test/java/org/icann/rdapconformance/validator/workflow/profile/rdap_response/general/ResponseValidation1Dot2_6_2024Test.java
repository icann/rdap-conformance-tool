package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class ResponseValidation1Dot2_6_2024Test extends ProfileJsonValidationTestBase {

    public ResponseValidation1Dot2_6_2024Test() {
        super("/validators/profile/rdapConformance/valid.json",
                "rdapResponseProfile_1_2_6_Validation");
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation1Dot2_6_2024(queryContext);
    }

    @Test
    public void testValid_BaseFixture_ShouldPass() {
        // Base fixture has valid redacted array — should pass
        validateOk(results);
    }

    @Test
    public void testValid_NoRedactedMember_ShouldPass() {
        jsonObject.remove("redacted");
        updateQueryContextJsonData();
        validateOk(results);
    }

    @Test
    public void testValid_AllStringMembers_ShouldPass() {
        jsonObject.put("redacted", new JSONArray()
                .put(new JSONObject()
                        .put("name", new JSONObject().put("description", "Registry Domain ID"))
                        .put("method", "removal")
                        .put("prePath", "$.handle")
                        .put("pathLang", "jsonpath")));
        updateQueryContextJsonData();
        validateOk(results);
    }

    @Test
    public void testValid_OptionalMembersAbsent_ShouldPass() {
        // None of the optional string members are present — should pass
        jsonObject.put("redacted", new JSONArray()
                .put(new JSONObject()
                        .put("name", new JSONObject().put("type", "Registrant Name"))));
        updateQueryContextJsonData();
        validateOk(results);
    }

    @Test
    public void testValid_RedactedNotArray_ShouldSkip() {
        // Not a JSONArray — handled by -62002, this validator skips
        jsonObject.put("redacted", "not an array");
        updateQueryContextJsonData();
        validateOk(results);
    }

    @Test
    public void test62004_MethodIsNotString_ShouldFail() {
        JSONObject redactedItem = new JSONObject()
                .put("name", new JSONObject().put("type", "Registrant Name"))
                .put("method", 123);
        jsonObject.put("redacted", new JSONArray().put(redactedItem));

        validate(-62004, redactedItem.toString(),
                "The members 'postPath', 'pathLang', 'prePath', 'replacementPath', and 'method' must be strings.");
    }

    @Test
    public void test62004_PrePathIsNotString_ShouldFail() {
        JSONObject redactedItem = new JSONObject()
                .put("name", new JSONObject().put("type", "Registrant Name"))
                .put("prePath", new JSONObject());
        jsonObject.put("redacted", new JSONArray().put(redactedItem));

        validate(-62004, redactedItem.toString(),
                "The members 'postPath', 'pathLang', 'prePath', 'replacementPath', and 'method' must be strings.");
    }

    @Test
    public void test62004_PostPathIsNotString_ShouldFail() {
        JSONObject redactedItem = new JSONObject()
                .put("name", new JSONObject().put("type", "Registrant Name"))
                .put("postPath", new JSONArray());
        jsonObject.put("redacted", new JSONArray().put(redactedItem));

        validate(-62004, redactedItem.toString(),
                "The members 'postPath', 'pathLang', 'prePath', 'replacementPath', and 'method' must be strings.");
    }

    @Test
    public void test62004_PathLangIsNotString_ShouldFail() {
        JSONObject redactedItem = new JSONObject()
                .put("name", new JSONObject().put("type", "Registrant Name"))
                .put("pathLang", false);
        jsonObject.put("redacted", new JSONArray().put(redactedItem));

        validate(-62004, redactedItem.toString(),
                "The members 'postPath', 'pathLang', 'prePath', 'replacementPath', and 'method' must be strings.");
    }

    @Test
    public void test62004_ReplacementPathIsNotString_ShouldFail() {
        JSONObject redactedItem = new JSONObject()
                .put("name", new JSONObject().put("type", "Registrant Name"))
                .put("replacementPath", 99.9);
        jsonObject.put("redacted", new JSONArray().put(redactedItem));

        validate(-62004, redactedItem.toString(),
                "The members 'postPath', 'pathLang', 'prePath', 'replacementPath', and 'method' must be strings.");
    }
}