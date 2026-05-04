package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class ResponseValidation1Dot2_4_2024Test extends ProfileJsonValidationTestBase {

    public static final String VCARD_STRUCTURE = "[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Example Registrant\"],[\"adr\",{},\"text\",[\"\",\"\",\"123 Main St\",\"Anytown\",\"CA\",\"12345\",\"\"]]]]";
    public static final String VCARD_STRUCTURE_USA = "[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Example Registrant\"],[\"adr\",{\"cc\":\"USA\"},\"text\",[\"\",\"\",\"123 Main St\",\"Anytown\",\"CA\",\"12345\",\"\"]]]]";
    public static final String VCARD_STRUCTURE_NO_ADR = "[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Example Registrant\"]]]";
    public static final String VCARD_STRUCTURE_ISO = "[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Example Registrant\"],[\"adr\",{\"cc\":\"AA\"},\"text\",[\"\",\"\",\"123 Main St\",\"Anytown\",\"CA\",\"12345\",\"\"]]]]";
    public static final String VCARD_STRUCTURE_NON_ASCII = "[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Example Registrant\"],[\"adr\",{\"cc\":\"ÅÄ\"},\"text\",[\"\",\"\",\"123 Main St\",\"Anytown\",\"CA\",\"12345\",\"\"]]]]";

    public ResponseValidation1Dot2_4_2024Test() {
        super("/validators/profile/response_validations/general/valid_cc.json",
                "rdapResponseProfile_1_2_4_Validation");
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation1Dot2_4_2024(queryContext);
    }

    @Test
    public void testValid_WithCcParameter_ShouldPass() {
        // Base fixture already has a valid "cc": "US" — should pass with no errors
        validateOk(results);
    }

    @Test
    public void test62101_MissingCcParameter_ShouldFail() {
        // Remove the "cc" parameter from the adr property
        jsonObject.getJSONArray("entities")
                .getJSONObject(0)
                .getJSONArray("vcardArray")
                .getJSONArray(1)
                .getJSONArray(2)          // adr entry: ["adr", {"cc":"US"}, ...]
                .put(1, new JSONObject()); // replace params with empty object
        updateQueryContextJsonData();

        validate(-62101, VCARD_STRUCTURE,
                "All jCards MUST have an ISO 3166-1 Alpha 2 cc parameter");
    }

    @Test
    public void test62101_InvalidCcParameter_NotTwoLetters_ShouldFail() {
        // Set cc to an invalid value (too long)
        JSONObject params = new JSONObject();
        params.put("cc", "USA");
        jsonObject.getJSONArray("entities")
                .getJSONObject(0)
                .getJSONArray("vcardArray")
                .getJSONArray(1)
                .getJSONArray(2)
                .put(1, params);
        updateQueryContextJsonData();

        validate(-62101, VCARD_STRUCTURE_USA,
                "All jCards MUST have an ISO 3166-1 Alpha 2 cc parameter");
    }

    @Test
    public void test62101_NoAdrProperty_ShouldFail() {
        // Remove the adr entry entirely from the vcard
        jsonObject.getJSONArray("entities")
                .getJSONObject(0)
                .getJSONArray("vcardArray")
                .getJSONArray(1)
                .remove(2); // remove adr entry
        updateQueryContextJsonData();

        validate(-62101, VCARD_STRUCTURE_NO_ADR,
                "All jCards MUST have an ISO 3166-1 Alpha 2 cc parameter");
    }

    @Test
    public void test62101_InvalidCcParameter_NonExistentCountry_ShouldFail() {
        // "AA" has valid shape (2 uppercase letters) but is not a real ISO 3166-1 alpha-2 code
        JSONObject params = new JSONObject();
        params.put("cc", "AA");
        jsonObject.getJSONArray("entities")
                .getJSONObject(0)
                .getJSONArray("vcardArray")
                .getJSONArray(1)
                .getJSONArray(2)
                .put(1, params);
        updateQueryContextJsonData();

        validate(-62101, VCARD_STRUCTURE_ISO,
                "All jCards MUST have an ISO 3166-1 Alpha 2 cc parameter");
    }

    @Test
    public void test62101_InvalidCcParameter_NonAsciiUppercase_ShouldFail() {
        // "ÅÄ" is 2 uppercase letters but non-ASCII — must be rejected
        JSONObject params = new JSONObject();
        params.put("cc", "ÅÄ");
        jsonObject.getJSONArray("entities")
                .getJSONObject(0)
                .getJSONArray("vcardArray")
                .getJSONArray(1)
                .getJSONArray(2)
                .put(1, params);
        updateQueryContextJsonData();

        validate(-62101, VCARD_STRUCTURE_NON_ASCII,
                "All jCards MUST have an ISO 3166-1 Alpha 2 cc parameter");
    }

    @Test
    public void testValid_NoEntities_ShouldPass() {
        // No entities at all — validation should skip
        jsonObject.remove("entities");
        updateQueryContextJsonData();

        validateOk(results);
    }
}
