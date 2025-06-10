package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class ResponseValidation2Dot7Dot6Dot1_2024Test extends ProfileJsonValidationTestBase {
    public ResponseValidation2Dot7Dot6Dot1_2024Test() {
        super("/validators/profile/response_validations/vcard/valid_tech_name.json",
            "rdapResponseProfile_2_7_6_1_Validation");
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot7Dot6Dot1_2024(
            jsonObject.toString(),
            results);
    }

    @Test
    public void test65000() {

        jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).remove(2);

        validate(-65000, "[\"vcard\",[[\"email\",{\"type\":\"work\"},\"text\",\"privacy@dnic.JewellaPrivacy.com\"],[\"version\",{},\"text\",\"4.0\"],[\"org\",{},\"text\",\"Example Inc.\"],[\"adr\",{},\"text\",[\"\",\"Suite 1234\",\"4321 Rue Somewhere\",\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]]]]",
            "The fn property is required on the vcard for the technical contact.");
    }

    @Test
    public void test65001() {
        jsonObject.getJSONArray("redacted").getJSONObject(0).getJSONObject("name").put("type", "dummy");

        validate(-65001, "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"dummy\"},\"postPath\":\"$.entities[?(@.roles[0]=='technical')]\"}",
            "a redaction of type Tech Name is required.");
    }

    @Test
    public void test65002() {
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("postPath", "$.store.book.[");
        validate(-65002, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Tech Name\"},\"postPath\":\"$.store.book.[\"}",
            "jsonpath is invalid for Tech Name");
    }

    @Test
    public void test65003() {
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("postPath", "$.store.book[");
        validate(-65003, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Tech Name\"},\"postPath\":\"$.store.book[\"}",
            "jsonpath must evaluate to a non-empty set for redaction by empty value of Tech Name.");
    }

    @Test
    public void test65004() {
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("method", "dummy");
        validate(-65004, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"dummy\",\"name\":{\"type\":\"Tech Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='technical')]\"}",
            "Tech Name redaction method must be emptyValue");
    }
}
