package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;
import static org.mockito.Mockito.when;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class ResponseValidation2Dot7Dot6Dot3_2024Test extends ProfileJsonValidationTestBase {
    public ResponseValidation2Dot7Dot6Dot3_2024Test() {
        super("/validators/profile/response_validations/vcard/valid_tech_email.json",
            "rdapResponseProfile_2_7_6_3_Validation");
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot7Dot6Dot3_2024(queryContext);
    }

    @Test
    public void test65200() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());  // empty JSON object
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");

        jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).put(contactUriEntry);

        validate(-65200, "[\"vcard\",[[\"email\",{\"type\":\"work\"},\"text\",\"privacy@dnic.JewellaPrivacy.com\"],[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"\"],[\"org\",{},\"text\",\"Example Inc.\"],[\"adr\",{},\"text\",[\"\",\"Suite 1234\",\"4321 Rue Somewhere\",\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]],[\"contact-uri\",{},\"uri\",\"https://email.example.com/123\"]]]",
            "a redaction of Tech Email may not have both the email and contact-uri");
    }

    @Test
    public void test65201() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).remove(0);
        validate(-65201, "[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"\"],[\"org\",{},\"text\",\"Example Inc.\"],[\"adr\",{},\"text\",[\"\",\"Suite 1234\",\"4321 Rue Somewhere\",\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]]]]",
            "a redaction of Tech Email must have either the email or contact-uri");
    }

    @Test
    public void test65202() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("method", "dummy");
        validate(-65202, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"dummy\",\"name\":{\"type\":\"Tech Email\"}}",
            "Tech Email redaction method must be replacementValue");
    }

    @Test
    public void test65203() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("postPath", "$.store.book.[");
        validate(-65203, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Tech Email\"},\"postPath\":\"$.store.book.[\"}",
            "jsonpath is invalid for Tech Email postPath");
    }

    @Test
    public void test65204() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("postPath", "$.store.book[");
        validate(-65204, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Tech Email\"},\"postPath\":\"$.store.book[\"}",
            "jsonpath must evaluate to a non-empty set for redaction by replacementValue of Tech Email.");
    }

    @Test
    public void test65205() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).remove(0);

        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());  // empty JSON object
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");

        jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).put(contactUriEntry);


        jsonObject.getJSONArray("redacted").getJSONObject(0).put("replacementPath", "$.store.book.[");
        validate(-65205, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Tech Email\"},\"replacementPath\":\"$.store.book.[\"}",
            "jsonpath is invalid for Tech Email replacementPath");
    }

    @Test
    public void test65207() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).remove(0);

        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());  // empty JSON object
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");

        jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).put(contactUriEntry);


        jsonObject.getJSONArray("redacted").getJSONObject(0).put("replacementPath", "$.store.book[");
        validate(-65207, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Tech Email\"},\"replacementPath\":\"$.store.book[\"}",
            "jsonpath must evaluate to a non-empty set for redaction by replacementValue of Tech Email in replacementPath");
    }

    @Test
    public void test65206() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).remove(0);

        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());  // empty JSON object
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");

        jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).put(contactUriEntry);


        jsonObject.getJSONArray("redacted").getJSONObject(0).put("prePath", "$.store.book.[");
        validate(-65206, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Tech Email\"},\"prePath\":\"$.store.book.[\"}",
            "jsonpath is invalid for Tech Email prePath");
    }

    @Test
    public void testMalformedRedactedArray() throws java.io.IOException {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Load malformed JSON that has malformed redacted object at index 0 
        // but valid "Tech Email" redaction at index 3
        String malformedContent = getResource("/validators/profile/response_validations/vcard/malformed_redacted_test.json");
        jsonObject = new org.json.JSONObject(malformedContent);
        
        // Fix the Tech Email redaction to use the correct method for this validation
        // Tech Email validation requires method "replacementValue", not "removal"
        jsonObject.getJSONArray("redacted").getJSONObject(3).put("method", "replacementValue");
        
        // Remove the prePath since replacementValue doesn't use prePath
        jsonObject.getJSONArray("redacted").getJSONObject(3).remove("prePath");
        
        // The key test: This should pass validation because "Tech Email" redaction exists at index 3,
        // even though index 0 has malformed "name": null. Our fix ensures malformed redacted 
        // objects are skipped gracefully.
        validate(); // Should NOT generate any validation errors
    }

    @Test
    public void testMultiRoleTechnical() throws java.io.IOException {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
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
    public void testNoTechnicalEntity() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Remove all entities
        jsonObject.remove("entities");
    }

    @Test
    public void testNoRedactedTechEmail() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Remove all redacted objects
        jsonObject.remove("redacted");
    }

    @Test
    public void testMethodAndPathLangAbsent() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Remove method and pathLang from redacted Tech Email
        JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.remove("method");
        redacted.remove("pathLang");
        validate(-65202, redacted.toString(), "Tech Email redaction method must be replacementValue");
    }

    @Test
    public void testPathLangNotJsonPath() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.put("pathLang", "notjsonpath");
        validate();
    }

    @Test
    public void testValidPostPath() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.put("postPath", "$.entities[*]");
        validate();
    }

    @Test
    public void testValidPrePath() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONArray vcard = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        vcard.remove(0); // Remove email
        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");
        vcard.put(contactUriEntry);
        JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.put("prePath", "$.entities[*]");
        validate();
    }

    @Test
    public void testValidReplacementPath() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONArray vcard = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        vcard.remove(0); // Remove email
        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");
        vcard.put(contactUriEntry);
        JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.put("replacementPath", "$.entities[*]"); // Should be valid and non-empty
        validate();
    }
}
