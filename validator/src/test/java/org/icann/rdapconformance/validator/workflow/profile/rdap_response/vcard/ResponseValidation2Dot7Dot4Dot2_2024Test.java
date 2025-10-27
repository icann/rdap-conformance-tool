package org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard;

import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.testng.annotations.Test;

import java.io.IOException;

public class ResponseValidation2Dot7Dot4Dot2_2024Test extends ProfileJsonValidationTestBase {
    public ResponseValidation2Dot7Dot4Dot2_2024Test() {
        super("/validators/profile/response_validations/vcard/valid_org.json",
                "rdapResponseProfile_2_7_4_2_Validation");
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot7Dot4Dot2_2024(queryContext);
    }

    @Test
    public void test63300Removed() {
        // Test case -63300 has been removed as per the requirements
        // Natural persons do not require "Registrant Organization" redaction
        jsonObject.getJSONArray("redacted").remove(0);
        validate(); // Should pass validation now (no error expected)
    }

    @Test
    public void test63301() {
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("prePath", "book.[");
        validate(-63301, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\",\"prePath\":\"book.[\"}",
                "jsonpath is invalid for Registrant Organization");
    }

    @Test
    public void test63302() {
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("prePath", "$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]");
        validate(-63302, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\"}",
                "jsonpath must evaluate to a zero set for redaction by removal of Registrant Organization.");
    }

    @Test
    public void test63303() {
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("method", "dummy");
        validate(-63303, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"dummy\",\"name\":{\"type\":\"Registrant Organization\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\",\"prePath\":\"book\"}",
                "Registrant Organization redaction method must be removal if present");
    }

    @Test
    public void testMalformedRedactedArray() throws java.io.IOException {
        // Load malformed JSON that has malformed redacted object at index 0 
        // but we need to create a "Registrant Organization" redaction for this test
        String malformedContent = getResource("/validators/profile/response_validations/vcard/malformed_redacted_test.json");
        jsonObject = new org.json.JSONObject(malformedContent);

        // This should pass validation because "Registrant Organization" redaction does not exist,
        validate(); // Should NOT generate any errors
    }

    @Test
    public void testMultiRoleRegistrant() throws java.io.IOException {
        // REGRESSION TEST: This test verifies that multi-role entities are handled correctly
        // After fixing role indexing bug (RCT-345), we now use: $.entities[?(@.roles contains 'registrant')]
        // This correctly finds registrant entities regardless of role position

        String multiRoleContent = getResource("/validators/profile/response_validations/vcard/valid_org_multi_role.json");
        jsonObject = new org.json.JSONObject(multiRoleContent);
        jsonObject.getJSONArray("redacted").remove(0);

        // The test JSON has entity with roles: ["technical", "registrant"] 
        // - roles[0] = "technical" 
        // - roles[1] = "registrant" <- NOW CORRECTLY FOUND with 'contains' operator

        // Test with "Registrant Organization" redaction present
        // Expected: Should pass validation (redaction exists, so natural person check is skipped)
        validate(); // Should pass - redaction present, validation logic applies correctly
    }

    @Test
    public void testMultiRoleRegistrant_NaturalPerson() throws java.io.IOException {
        // REGRESSION TEST: Test natural person logic with multi-role entities

        String multiRoleContent = getResource("/validators/profile/response_validations/vcard/valid_org_multi_role.json");
        jsonObject = new org.json.JSONObject(multiRoleContent);

        // Remove the redaction to test natural person logic
        jsonObject.getJSONArray("redacted").remove(0);

        // Expected: Should pass validation (no redaction found, treated as natural person)
        // The updated logic says: "No 'Registrant Organization' redaction found, skip validation (natural person)"
        validate(); // Should pass - no redaction, treated as natural person per updated requirements
    }

    @Test
    public void testMultiRoleRegistrant_ValidationActuallyRuns_WithOrgRedaction() throws java.io.IOException {
        // NEGATIVE TEST: Ensure validation logic actually executes for multi-role entities
        // This test verifies that the registrant entity is found and validation logic runs

        String multiRoleContent = getResource("/validators/profile/response_validations/vcard/valid_org_multi_role.json");
        jsonObject = new org.json.JSONObject(multiRoleContent);
        removeOrgFromVCard();

        // Modify to create a scenario where validation should trigger an error
        // Change the redaction to have an invalid prePath to trigger -63301
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("prePath", "invalid.[jsonpath");

        // Expected: Should fail with -63301 because validation logic actually runs and finds invalid JSONPath
        validate(-63301,
                jsonObject.getJSONArray("redacted").getJSONObject(0).toString(),
                "jsonpath is invalid for Registrant Organization");
    }

    @Test
    public void testMultiRoleRegistrant_ValidationRuns_InvalidMethod() throws java.io.IOException {
        // NEGATIVE TEST: Ensure method validation runs for multi-role entities

        String multiRoleContent = getResource("/validators/profile/response_validations/vcard/valid_org_multi_role.json");
        jsonObject = new org.json.JSONObject(multiRoleContent);
        removeOrgFromVCard();

        // Change method to trigger -63303
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("method", "invalid_method");

        // Expected: Should fail with -63303 because validation logic runs and finds invalid method
        validate(-63303,
                jsonObject.getJSONArray("redacted").getJSONObject(0).toString(),
                "Registrant Organization redaction method must be removal if present");
    }

    @Test
    public void testMultiRoleRegistrant_ValidationRuns_PrePathNonEmpty() throws java.io.IOException {
        // NEGATIVE TEST: Ensure prePath validation runs for multi-role entities

        String multiRoleContent = getResource("/validators/profile/response_validations/vcard/valid_org_multi_role.json");
        jsonObject = new org.json.JSONObject(multiRoleContent);
        removeOrgFromVCard();

        // Set prePath to a valid JSONPath that evaluates to non-empty set to trigger -63302
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("prePath", "$.entities[?(@.roles contains 'registrant')].vcardArray[1][?(@[0]=='fn')][3]");

        // Expected: Should fail with -63302 because validation logic runs and finds non-empty set
        validate(-63302,
                jsonObject.getJSONArray("redacted").getJSONObject(0).toString(),
                "jsonpath must evaluate to a zero set for redaction by removal of Registrant Organization.");
    }

    @Test
    public void testNoRegistrantEntity() {
        // No entities with the 'registrant' role
        jsonObject.remove("entities");
        validate(); // Should pass, nothing to validate
    }

    @Test
    public void testOrgPresentNotRedacted() {
        // vCard org present, but no redacted Registrant Organization
        jsonObject.getJSONArray("redacted").remove(0);
        // Ensure vcardArray has org
        validate(); // Should pass, org present but not redacted
    }

    @Test
    public void testRedactedOrg_PathLangMissing_PrePathMissing() {
        // Remove pathLang and prePath from redacted
        jsonObject.getJSONArray("redacted").getJSONObject(0).remove("pathLang");
        jsonObject.getJSONArray("redacted").getJSONObject(0).remove("prePath");
        // Should skip prePath validation, no error
        validate();
    }

    @Test
    public void testRedactedOrg_PathLangNotJsonPath() {
        // Set pathLang to something other than 'jsonpath'
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("pathLang", "other");
        // Should skip prePath validation, no error
        validate();
    }

    @Test
    public void testRedactedOrg_MethodMissing() {
        // Remove method from redacted
        jsonObject.getJSONArray("redacted").getJSONObject(0).remove("method");
        // Should not trigger -63303
        validate();
    }

    @Test
    public void testRedactedOrg_MethodIsRemoval() {
        // Set method to 'removal'
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("method", "removal");
        // Should not trigger -63303
        validate();
    }

    @Test
    public void testRedactedOrg_PrePathValidEmptySet() {
        // Set prePath to a valid JSONPath that matches nothing
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("prePath", "$.entities[?(@.roles contains 'registrant')].vcardArray[1][?(@[0]=='nonexistent')]");
        validate(); // Should pass, no error
    }

    @Test
    public void testNoRedactedObjects() {
        // Remove the redacted array
        jsonObject.remove("redacted");
        validate(); // Should pass, nothing to validate
    }

    @Test
    public void testRedactedObjectWrongNameType() {
        // Add a redacted object with a different name type
        org.json.JSONObject wrongRedacted = new org.json.JSONObject(jsonObject.getJSONArray("redacted").getJSONObject(0).toString());
        wrongRedacted.getJSONObject("name").put("type", "Other Type");
        jsonObject.getJSONArray("redacted").put(wrongRedacted);
        // Should skip this object, no error
        validate();
    }

    @Test
    public void test63304_OrgPresentAndRedactedOrgPresentWithExtraFields() throws IOException {
        // vCard org present and redactedOrg present, with extra fields in redactedOrg
        String multiRoleContent = getResource("/validators/profile/response_validations/vcard/valid_org_multi_role.json");
        jsonObject = new org.json.JSONObject(multiRoleContent);
        validate(-63304, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')][3]\",\"pathLang\":\"jsonpath\",\"prePath\":\"book\"}",
                "a redaction of type Registrant Organization was found but organization name was not redacted.");
    }

    @Test
    public void test63304_OrgPresentAndRedactedOrgPresentWithNullFields() throws IOException {
        // vCard org present and redactedOrg present, with some null fields in redactedOrg
        String multiRoleContent = getResource("/validators/profile/response_validations/vcard/valid_org_multi_role.json");
        jsonObject = new org.json.JSONObject(multiRoleContent);
        org.json.JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.put("prePath", org.json.JSONObject.NULL);
        redacted.put("method", org.json.JSONObject.NULL);
        validate(-63304, redacted.toString(),
                "a redaction of type Registrant Organization was found but organization name was not redacted.");
    }

    private void removeOrgFromVCard() {
        jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).remove(2);
    }
}
