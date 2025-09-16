package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;

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

    @Test
    public void testMalformedRedactedArray() throws java.io.IOException {
        // Load malformed JSON that has malformed redacted object at index 0 
        // but valid "Tech Name" redaction at index 2
        String malformedContent = getResource("/validators/profile/response_validations/vcard/malformed_redacted_test.json");
        jsonObject = new org.json.JSONObject(malformedContent);
        
        // This should pass validation because "Tech Name" redaction exists at index 2,
        // even though index 0 has malformed "name": null. The fn property is preserved
        // since the "Tech Name" redaction uses emptyValue method, not removal.
        validate(); // Should NOT generate -65001 error
    }

    @Test
    public void testMultiRoleTechnical() throws java.io.IOException {
        // REGRESSION TEST: Verify multi-role entities are handled correctly after RCT-345 fix
        // Changed from @.roles[0]=='technical' to @.roles contains 'technical'
        
        String multiRoleContent = getResource("/validators/profile/response_validations/vcard/valid_org_multi_role.json");
        jsonObject = new org.json.JSONObject(multiRoleContent);
        
        // Test JSON has entity with roles: ["technical", "registrant"]
        // Now correctly found with 'contains' operator regardless of role position
        
        // Should pass validation with multi-role technical entity
        validate(); // Should pass - technical entity correctly found
    }

    @Test
    public void testMultiRoleTechnical_ValidationActuallyRuns_EmptyFn() throws java.io.IOException {
        // NEGATIVE TEST: Ensure validation logic actually executes for multi-role technical entities
        // This test verifies the technical entity is found and fn validation logic runs
        
        String multiRoleContent = getResource("/validators/profile/response_validations/vcard/valid_org_multi_role.json");
        jsonObject = new org.json.JSONObject(multiRoleContent);
        
        // Make fn empty to trigger validation
        JSONArray fnValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(1);
        fnValue.put(3, ""); // Make fn empty
        
        // Remove "Tech Name" redaction to trigger -65001 
        jsonObject.getJSONArray("redacted").getJSONObject(1).getJSONObject("name").put("type", "test");
        
        // Expected: Should fail with -65001 because validation logic actually runs
        validate(-65001, 
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')][3]\",\"pathLang\":\"jsonpath\",\"prePath\":\"book\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"test\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}",
            "a redaction of type Tech Name is required.");
    }

    @Test
    public void testMultiRoleTechnical_ValidationRuns_InvalidPostPath() throws java.io.IOException {
        // NEGATIVE TEST: Ensure postPath validation runs for multi-role technical entities
        
        String multiRoleContent = getResource("/validators/profile/response_validations/vcard/valid_org_multi_role.json");
        jsonObject = new org.json.JSONObject(multiRoleContent);
        
        // Add required Tech Name redaction with invalid postPath
        JSONArray redactedArray = jsonObject.getJSONArray("redacted");
        JSONObject techNameRedaction = new JSONObject();
        techNameRedaction.put("reason", new JSONObject().put("description", "Server policy"));
        techNameRedaction.put("method", "emptyValue");
        techNameRedaction.put("name", new JSONObject().put("type", "Tech Name"));
        techNameRedaction.put("postPath", "$invalid[path"); // Invalid JSONPath
        redactedArray.put(techNameRedaction);
        
        // Make fn empty to trigger validation
        JSONArray fnValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(1);
        fnValue.put(3, ""); // Make fn empty
        
        // Expected: Should fail with -65002 because validation logic runs and finds invalid JSONPath
        validate(-65002, techNameRedaction.toString(), "jsonpath is invalid for Tech Name");
    }
}
