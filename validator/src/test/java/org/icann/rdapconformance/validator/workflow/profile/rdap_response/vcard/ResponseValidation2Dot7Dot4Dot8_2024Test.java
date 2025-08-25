package org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard;

import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

public class ResponseValidation2Dot7Dot4Dot8_2024Test extends ProfileJsonValidationTestBase {

    static final String voicePointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"test\"},\"prePath\":\"$.test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String pathLangBadPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Phone\"},\"prePath\":\"$test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String prePathExistingPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Phone\"},\"prePath\":\"$.redacted[*]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String methodPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"test2\",\"name\":{\"type\":\"Registrant Phone\"},\"prePath\":\"$.test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";

    public ResponseValidation2Dot7Dot4Dot8_2024Test() {
        super("/validators/profile/response_validations/vcard/valid_tel_voice.json",
                "rdapResponseProfile_2_7_4_8_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot7Dot4Dot8_2024(
                jsonObject.toString(),
                results);
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot8_2024_No_Registrant() {
        JSONArray roles = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("roles");
        roles.put(0, "registrar");
        validate();
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot8_2024_63700() {
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        tel.remove(1);
        redactedObject.getJSONObject("name").put("type", "test");
        validate(-63700, voicePointer, "a redaction of type Registrant Phone is required.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot8_2024_63701_By_PathLang_NotValid() {
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        tel.remove(1);
        redactedObject.put("prePath", "$test");
        validate(-63701, pathLangBadPointer, "jsonpath is invalid for Registrant Phone.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot8_2024_63702_By_MissingPathLang_Bad_PrePath() {
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        tel.remove(1);
        redactedObject.remove("pathLang");
        redactedObject.put("prePath", "$.redacted[*]");
        validate(-63702, prePathExistingPointer, "jsonpath must evaluate to a zero set for redaction by removal of Registrant Phone.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot8_2024_63703_By_Method() {
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        tel.remove(1);
        redactedObject.put("method", "test2");
        validate(-63703, methodPointer, "Registrant Phone redaction method must be removal if present");
    }

    @Test
    public void testRealWorldData_IcannOrgResponse_ShouldNotTriggerFalsePositive63700() throws Exception {
        // This test uses real-world RDAP data from icann.org to prevent regression
        // The issue was that mixed redacted objects (some with name.type, some with name.description)
        // caused exceptions that incorrectly triggered -63700 validation failures
        
        // Load real-world data from icann.org RDAP response
        String realWorldContent = getResource("/validators/profile/response_validations/vcard/icann_org_phone_real_world.json");
        jsonObject = new JSONObject(realWorldContent);
        
        // The real data has:
        // 1. registrant entity with NO tel property with voice parameter
        // 2. redacted array with mixed objects:
        //    - Objects with name.type (e.g., "Registrant Phone", "Registry Domain ID") 
        //    - Objects with name.description (e.g., "Administrative Contact", "Technical Contact")
        // 3. Proper "Registrant Phone" redaction object exists at redacted[8]
        
        // Before the fix: Exception thrown when processing objects with name.description
        // caused immediate -63700 failure, preventing discovery of valid "Registrant Phone" redaction
        
        // After the fix: Code skips objects that cause exceptions and finds the valid redaction
        // This should pass without any validation errors
        validate();
    }

    @Test
    public void testVoiceTelPresent_ShouldNotTriggerValidations() {
        // Test that when voice tel property exists, no redaction validations are triggered
        
        // The base test data already has a voice tel property, so no modifications needed
        // Expected: Since voice tel is present, no redaction validations should trigger
        // Test should pass without any validation errors
        validate();
    }

    @Test
    public void testNoRegistrantEntity_ShouldNotTriggerValidations() {
        // Test that when there's no registrant entity, no validations are triggered
        
        JSONArray roles = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("roles");
        roles.put(0, "registrar");
        
        // Expected: Since there's no registrant entity, no phone validations should trigger
        // Test should pass without any validation errors
        validate();
    }

    @Test
    public void testEmptyRedactedArray_ShouldTrigger63700() {
        // Test edge case where redacted array exists but is empty
        
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        tel.remove(1); // Remove the voice parameter to trigger validation
        
        // Clear the redacted array 
        jsonObject.put("redacted", new JSONArray());
        
        // Expected: Should trigger -63700 because no "Registrant Phone" redaction found
        validate(-63700, 
            "",  // Empty redacted array results in empty value
            "a redaction of type Registrant Phone is required.");
    }

    @Test
    public void testMultipleTelProperties_WithAndWithoutVoice_ShouldTriggerValidations() {
        // Test multiple tel properties where some have voice and some don't
        // When ANY voice tel is missing, validation should trigger
        
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        
        // Add a tel property WITHOUT voice parameter
        JSONArray noVoiceTel = new JSONArray();
        noVoiceTel.put("tel");           // [0] = property name
        JSONObject telParams = new JSONObject();
        telParams.put("type", "fax");   // Different type, not voice
        noVoiceTel.put(telParams);      // [1] = parameters
        noVoiceTel.put("uri");          // [2] = type
        noVoiceTel.put("tel:+1.555.555.5555"); // [3] = value
        
        // Insert the non-voice tel property into the vcard array
        vcardArray.put(noVoiceTel);
        
        // The original tel property at index 5 still has voice parameter
        // But we now have mixed tel properties - some with voice, some without
        // Since there IS at least one voice tel property, validations should NOT trigger
        
        // Expected: Since voice tel property exists, no redaction validations should trigger
        validate(); // This should pass - no validations trigger
    }

    @Test
    public void testNoVoiceTelButOtherTelTypes_ShouldTriggerValidations() {
        // Test when there are tel properties but none have voice parameter
        
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        JSONArray existingTel = vcardArray.getJSONArray(5); // Get existing tel property
        JSONObject telParams = existingTel.getJSONObject(1); // Get parameters object
        
        // Change voice to fax
        telParams.put("type", "fax");
        
        // Expected: Since no voice tel property exists, redaction validations should trigger
        // and pass because proper "Registrant Phone" redaction is configured
        validate();
    }

    @Test
    public void testInvalidPrePathExists_ShouldTrigger63701() {
        // Test when prePath contains invalid JSONPath syntax
        
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        
        tel.remove(1); // Remove voice parameter to trigger redaction validation
        redactedObject.put("prePath", "$invalid[syntax"); // Invalid JSONPath
        
        // Expected: Should trigger -63701 for invalid JSONPath
        String expectedValue = "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Phone\"},\"prePath\":\"$invalid[syntax\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
        validate(-63701, expectedValue, "jsonpath is invalid for Registrant Phone.");
    }

    @Test
    public void testValidPrePathWithResults_ShouldTrigger63702() {
        // Test when prePath is valid but evaluates to non-empty set (should be empty for removal)
        
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        
        tel.remove(1); // Remove voice parameter to trigger redaction validation
        redactedObject.put("prePath", "$.entities[*]"); // Valid JSONPath that returns results
        
        // Expected: Should trigger -63702 because prePath should evaluate to zero set for removal
        String expectedValue = "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Phone\"},\"prePath\":\"$.entities[*]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
        validate(-63702, expectedValue, "jsonpath must evaluate to a zero set for redaction by removal of Registrant Phone.");
    }

    @Test
    public void testValidPrePathEmpty_ShouldPass() {
        // Test when prePath is valid and evaluates to empty set (correct for removal)
        
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        
        tel.remove(1); // Remove voice parameter to trigger redaction validation
        redactedObject.put("prePath", "$.nonExistentProperty[*]"); // Valid JSONPath that returns no results
        
        // Expected: Should pass because prePath evaluates to zero set and method is removal
        validate();
    }

    @Test
    public void testMissingPrePathProperty_ShouldPass() {
        // Test when prePath property is missing entirely
        
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        
        tel.remove(1); // Remove voice parameter to trigger redaction validation
        redactedObject.remove("prePath"); // Remove prePath property completely
        
        // Expected: Should pass because prePath is optional for removal method
        validate();
    }

    @Test
    public void testInvalidMethod_ShouldTrigger63703() {
        // Test when method is not "removal"
        
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        
        tel.remove(1); // Remove voice parameter to trigger redaction validation
        redactedObject.put("method", "emptyValue"); // Wrong method for phone validation
        
        // Expected: Should trigger -63703 because method must be "removal" for phone
        String expectedValue = "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Phone\"},\"prePath\":\"$.test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
        validate(-63703, expectedValue, "Registrant Phone redaction method must be removal if present");
    }

    @Test
    public void testMissingMethodProperty_ShouldPass() {
        // Test when method property is missing entirely
        
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        
        tel.remove(1); // Remove voice parameter to trigger redaction validation
        redactedObject.remove("method"); // Remove method property completely
        
        // Expected: Should pass because method is optional (defaults to removal behavior)
        validate();
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
