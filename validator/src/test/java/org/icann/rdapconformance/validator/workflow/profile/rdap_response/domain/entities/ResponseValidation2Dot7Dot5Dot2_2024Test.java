package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.testng.annotations.Test;

public class ResponseValidation2Dot7Dot5Dot2_2024Test extends ProfileJsonValidationTestBase {
    public ResponseValidation2Dot7Dot5Dot2_2024Test() {
        super("/validators/profile/response_validations/vcard/valid_fax.json",
            "rdapResponseProfile_2_7_5_2_Validation");
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot7Dot5Dot2_2024(queryContext);
    }

    @Test
    public void test63900() {
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("prePath", "$.store.book.[");
        validate(-63900, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Fax\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\",\"prePath\":\"$.store.book.[\"}",
            "jsonpath is invalid for Registrant Fax");
    }

    @Test
    public void test63801() {
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("prePath", "$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]");
        validate(-63901, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Fax\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\"}",
            "jsonpath must evaluate to a zero set for redaction by removal of Registrant Fax.");
    }

    @Test
    public void test63902() {
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("method", "dummy");
        validate(-63902, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"dummy\",\"name\":{\"type\":\"Registrant Fax\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\",\"prePath\":\"book\"}",
            "Registrant Fax redaction method must be removal if present");
    }

    @Test
    public void testMalformedRedactedArray() throws java.io.IOException {
        // Load malformed JSON that has malformed redacted object at index 0 
        // but we need to create a "Registrant Fax" redaction for this test
        String malformedContent = getResource("/validators/profile/response_validations/vcard/malformed_redacted_test.json");
        jsonObject = new org.json.JSONObject(malformedContent);
        
        // Add a "Registrant Fax" redaction to the test data since malformed_redacted_test.json 
        // is focused on Technical contact redactions
        org.json.JSONObject registrantFaxRedaction = new org.json.JSONObject();
        registrantFaxRedaction.put("name", new org.json.JSONObject().put("type", "Registrant Fax"));
        registrantFaxRedaction.put("prePath", "$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='tel')]");
        registrantFaxRedaction.put("method", "removal");
        registrantFaxRedaction.put("reason", new org.json.JSONObject().put("description", "Server policy"));
        
        // Add it to the redacted array
        jsonObject.getJSONArray("redacted").put(registrantFaxRedaction);
        
        // This should pass validation because "Registrant Fax" redaction exists,
        // even though index 0 has malformed "name": null  
        validate(); // Should NOT generate -63900 error
    }

    @Test
    public void testMultiRoleRegistrant() throws java.io.IOException {
        // REGRESSION TEST: Verify multi-role entities are handled correctly after RCT-345 fix
        // Changed from @.roles[0]=='registrant' to @.roles contains 'registrant'
        
        String multiRoleContent = getResource("/validators/profile/response_validations/vcard/valid_org_multi_role.json");
        jsonObject = new org.json.JSONObject(multiRoleContent);
        
        // Test JSON has entity with roles: ["technical", "registrant"]
        // Now correctly found with 'contains' operator regardless of role position
        
        // Should pass validation with multi-role registrant entity
        validate(); // Should pass - registrant entity correctly found
    }
}

