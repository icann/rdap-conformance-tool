package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.testng.annotations.Test;

public class ResponseValidation2Dot7Dot5Dot3_2024Test extends ProfileJsonValidationTestBase {
    public ResponseValidation2Dot7Dot5Dot3_2024Test() {
        super("/validators/profile/response_validations/vcard/valid_fax_ext.json",
            "rdapResponseProfile_2_7_5_3_Validation");
    }

    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot7Dot5Dot3_2024(queryContext);
    }

    @Test
    public void test64000() {
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("prePath", "$.store.book.[");
        validate(-64000, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Fax Ext\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\",\"prePath\":\"$.store.book.[\"}",
            "jsonpath is invalid for Registrant Fax Ext");
    }

    @Test
    public void test64001() {
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("prePath", "$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]");
        validate(-64001, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Fax Ext\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\"}",
            "jsonpath must evaluate to a zero set for redaction by removal of Registrant Fax Ext.");
    }

    @Test
    public void test64002() {
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("method", "dummy");
        validate(-64002, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"dummy\",\"name\":{\"type\":\"Registrant Fax Ext\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\",\"prePath\":\"book\"}",
            "Registrant Fax Ext redaction method must be removal if present");
    }

    @Test
    public void testMalformedRedactedArray() throws java.io.IOException {
        // Load malformed JSON that has malformed redacted object at index 0 
        // but we need to create a "Registrant Fax Ext" redaction for this test
        String malformedContent = getResource("/validators/profile/response_validations/vcard/malformed_redacted_test.json");
        jsonObject = new org.json.JSONObject(malformedContent);
        
        // Add a "Registrant Fax Ext" redaction to the test data since malformed_redacted_test.json 
        // is focused on Technical contact redactions
        org.json.JSONObject registrantFaxExtRedaction = new org.json.JSONObject();
        registrantFaxExtRedaction.put("name", new org.json.JSONObject().put("type", "Registrant Fax Ext"));
        registrantFaxExtRedaction.put("prePath", "$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='tel')]");
        registrantFaxExtRedaction.put("method", "removal");
        registrantFaxExtRedaction.put("reason", new org.json.JSONObject().put("description", "Server policy"));
        
        // Add it to the redacted array
        jsonObject.getJSONArray("redacted").put(registrantFaxExtRedaction);
        
        // This should pass validation because "Registrant Fax Ext" redaction exists,
        // even though index 0 has malformed "name": null  
        validate(); // Should NOT generate -64000 error
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

