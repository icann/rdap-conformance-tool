package org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard;

import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;

/*
    {
      "name": {
        "type": "Registrant Phone"
      },
      "prePath": "$.test",
      "method": "removal",
      "reason": {
        "description": "Server policy"
      }
    },
 */

public class ResponseValidation2Dot7Dot4Dot8_2024Test extends ProfileJsonValidationTestBase {

    static final String voicePointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"test\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String pathLangBadPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Phone\"},\"prePath\":\"$test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String prePathExistingPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Phone\"},\"prePath\":\"$.redacted[*]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String methodPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"test2\",\"name\":{\"type\":\"Registrant Phone\"},\"prePath\":\"$.test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";

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
        // Remove ALL tel properties from registrant entity to trigger validation
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        // Remove both tel properties (voice and fax) from registrant
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() > 0 && "tel".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }

        redactedObject.getJSONObject("name").put("type", "test");
        validate(-63700, voicePointer, "a redaction of type Registrant Phone is required.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot8_2024_63701_By_PathLang_NotValid() {
        // Remove ALL tel properties from registrant entity to trigger validation
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Phone");

        // Remove both tel properties (voice and fax) from registrant
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() > 0 && "tel".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }

        redactedObject.put("prePath", "$test");
        validate(-63701, pathLangBadPointer, "jsonpath is invalid for Registrant Phone.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot8_2024_63702_By_MissingPathLang_Bad_PrePath() {
        // Remove ALL tel properties from registrant entity to trigger validation
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Phone");

        // Remove both tel properties (voice and fax) from registrant
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() > 0 && "tel".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }

        redactedObject.remove("pathLang");
        redactedObject.put("prePath", "$.redacted[*]");
        validate(-63702, prePathExistingPointer, "jsonpath must evaluate to a zero set for redaction by removal of Registrant Phone.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot8_2024_63703_By_Method() {
        // Remove ALL tel properties from registrant entity to trigger validation
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Phone");
        redactedObject.put("prePath", "$.test");

        // Remove both tel properties (voice and fax) from registrant
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() > 0 && "tel".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }

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
        
        // Remove ALL tel properties from registrant entity to trigger validation
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);

        // Remove both tel properties (voice and fax) from registrant
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() > 0 && "tel".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }
        
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
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Phone");
        redactedObject.put("prePath", "$.test");
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
        redactedObject.getJSONObject("name").put("type", "Registrant Phone");
        
        tel.remove(1); // Remove voice parameter to trigger redaction validation
        redactedObject.put("prePath", "$invalid[syntax"); // Invalid JSONPath
        
        // Expected: Should trigger -63701 for invalid JSONPath
        String expectedValue = "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Phone\"},\"prePath\":\"$invalid[syntax\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
        validate(-63701, expectedValue, "jsonpath is invalid for Registrant Phone.");
    }

    @Test
    public void testValidPrePathWithResults_ShouldTrigger63702() {
        // Test when prePath is valid but evaluates to non-empty set (should be empty for removal)
        
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Phone");
        
        tel.remove(1); // Remove voice parameter to trigger redaction validation
        redactedObject.put("prePath", "$.entities[*]"); // Valid JSONPath that returns results
        
        // Expected: Should trigger -63702 because prePath should evaluate to zero set for removal
        String expectedValue = "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Phone\"},\"prePath\":\"$.entities[*]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
        validate(-63702, expectedValue, "jsonpath must evaluate to a zero set for redaction by removal of Registrant Phone.");
    }

    @Test
    public void testValidPrePathEmpty_ShouldPass() {
        // Test when prePath is valid and evaluates to empty set (correct for removal)
        
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Phone");
        
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
        redactedObject.getJSONObject("name").put("type", "Registrant Phone");
        
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
        redactedObject.getJSONObject("name").put("type", "Registrant Phone");
        redactedObject.put("prePath", "$.test");

        tel.remove(1); // Remove voice parameter to trigger redaction validation
        redactedObject.put("method", "emptyValue"); // Wrong method for phone validation
        
        // Expected: Should trigger -63703 because method must be "removal" for phone
        String expectedValue = "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Phone\"},\"prePath\":\"$.test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
        validate(-63703, expectedValue, "Registrant Phone redaction method must be removal if present");
    }

    @Test
    public void testMissingMethodProperty_ShouldPass() {
        // Test when method property is missing entirely
        
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Phone");
        redactedObject.put("prePath", "$.test");
        
        tel.remove(1); // Remove voice parameter to trigger redaction validation
        redactedObject.remove("method"); // Remove method property completely
        
        // Expected: Should pass because method is optional (defaults to removal behavior)
        validate();
    }

    // Array type validation tests - verifying the fix for Jira issue
    @Test
    public void testTypeArrayWithVoiceOnly_NowPassesAfterFix() {
        // Test case: Array with only voice - should now pass after fix
        // This SHOULD pass (voice is present) and NOW DOES pass (custom voice detection)
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject telParams = tel.getJSONObject(1);
        telParams.put("type", Arrays.asList("voice")); // Array with voice

        // After the fix: validation passes because custom method properly detects voice in arrays
        validate(); // Should pass without errors
    }

    @Test
    public void testTypeArrayWithVoiceFirst_NowPassesAfterFix() {
        // Test case: Array with voice first - should now pass after fix
        // This SHOULD pass (voice is present) and NOW DOES pass (custom voice detection)
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject telParams = tel.getJSONObject(1);
        telParams.put("type", Arrays.asList("voice", "work")); // Array with voice first

        // After the fix: validation passes because custom method properly detects voice in arrays
        validate(); // Should pass without errors
    }

    @Test
    public void testTypeArrayWithVoiceSecond_NowPassesAfterFix() {
        // Test case: Array with voice second - should now pass after fix
        // This SHOULD pass (voice is present) and NOW DOES pass (custom voice detection)
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject telParams = tel.getJSONObject(1);
        telParams.put("type", Arrays.asList("work", "voice")); // Array with voice second

        // After the fix: validation passes because custom method properly detects voice in arrays
        validate(); // Should pass without errors
    }

    @Test
    public void testTypeArrayWithMultipleTypes_NowPassesAfterFix() {
        // Test case: Array with multiple types including voice - should now pass after fix
        // This SHOULD pass (voice is present) and NOW DOES pass (custom voice detection)
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject telParams = tel.getJSONObject(1);
        telParams.put("type", Arrays.asList("cell", "voice", "work", "home")); // Array with voice included

        // After the fix: validation passes because custom method properly detects voice in arrays
        validate(); // Should pass without errors
    }

    @Test
    public void testSingleStringFaxType_ShouldTriggerValidation() {
        // Test case: Change ALL tel properties to "fax" - should trigger validation (custom method looks for "voice" only)
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);

        // Change ALL tel properties from voice to fax to trigger validation
        for (int i = 0; i < vcardArray.length(); i++) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() >= 2 && "tel".equals(property.getString(0))) {
                JSONObject telParams = property.getJSONObject(1);
                telParams.put("type", "fax"); // Change all to fax (no voice)
            }
        }

        // Also invalidate the redaction object to ensure -63700 is triggered
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "test"); // Change from "Registrant Phone" to "test"

        // Should trigger -63700 because custom method specifically looks for voice
        validate(-63700, voicePointer, "a redaction of type Registrant Phone is required.");
    }

    @Test
    public void testTypeArrayWithNoVoice_ShouldTriggerValidation() {
        // Test case: Change ALL tel properties to arrays with no voice - should correctly trigger validation
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);

        // Change ALL tel properties to arrays without voice to trigger validation
        for (int i = 0; i < vcardArray.length(); i++) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() >= 2 && "tel".equals(property.getString(0))) {
                JSONObject telParams = property.getJSONObject(1);
                telParams.put("type", Arrays.asList("home", "cell", "work")); // Array without voice or fax
            }
        }

        // Also invalidate the redaction object to ensure -63700 is triggered
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "test"); // Change from "Registrant Phone" to "test"

        // This should trigger validation because no voice is present in any tel property
        validate(-63700, voicePointer, "a redaction of type Registrant Phone is required.");
    }

    // Bad data handling tests
    @Test
    public void testTypeArrayWithNullElements_ShouldNotCrash() {
        // Test case: Array with null elements - should handle gracefully
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject telParams = tel.getJSONObject(1);
        telParams.put("type", Arrays.asList(null, "voice", null));

        validate(); // Should pass - voice found despite nulls
    }

    @Test
    public void testTypeArrayWithMixedElements_ShouldNotCrash() {
        // Test case: Array with mixed non-string elements - should handle gracefully
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject telParams = tel.getJSONObject(1);

        // Mix of different types including valid "voice"
        JSONArray mixedArray = new JSONArray();
        mixedArray.put(123);           // number
        mixedArray.put(true);          // boolean
        mixedArray.put(new JSONObject().put("bad", "data")); // object
        mixedArray.put("voice");       // valid string

        telParams.put("type", mixedArray);
        validate(); // Should pass - voice found despite mixed types
    }

    @Test
    public void testTypeArrayWithOnlyInvalidElements_ShouldTriggerValidation() {
        // Test case: Change ALL tel properties to arrays with only invalid elements - should trigger validation
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);

        JSONArray invalidArray = new JSONArray();
        invalidArray.put(123);
        invalidArray.put(true);
        invalidArray.put(new JSONObject().put("x", "y"));

        // Change ALL tel properties to have bad data arrays
        for (int i = 0; i < vcardArray.length(); i++) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() >= 2 && "tel".equals(property.getString(0))) {
                JSONObject telParams = property.getJSONObject(1);
                telParams.put("type", invalidArray);
            }
        }

        // Also invalidate the redaction object to ensure -63700 is triggered
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "test"); // Change from "Registrant Phone" to "test"

        validate(-63700, voicePointer, "a redaction of type Registrant Phone is required.");
    }

    @Test
    public void testMalformedVcardStructure_ShouldNotCrash() {
        // Test case: Malformed vcard structure - should handle gracefully
        JSONArray entities = jsonObject.getJSONArray("entities");
        JSONObject registrantEntity = entities.getJSONObject(0);

        // Replace vcard array with malformed structure
        JSONArray malformedVcard = new JSONArray();
        malformedVcard.put("vcard");
        malformedVcard.put("not-an-array"); // Should be array

        registrantEntity.put("vcardArray", malformedVcard);

        // Also invalidate the redaction object to ensure -63700 is triggered
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "test"); // Change from "Registrant Phone" to "test"

        validate(-63700, voicePointer, "a redaction of type Registrant Phone is required.");
    }

    @Test
    public void testMissingVcardArray_ShouldNotCrash() {
        // Test case: Entity without vcardArray - should handle gracefully
        JSONArray entities = jsonObject.getJSONArray("entities");
        JSONObject registrantEntity = entities.getJSONObject(0);
        registrantEntity.remove("vcardArray");

        // Also invalidate the redaction object to ensure -63700 is triggered
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "test"); // Change from "Registrant Phone" to "test"

        validate(-63700, voicePointer, "a redaction of type Registrant Phone is required.");
    }

    @Test
    public void testEmptyVcardArray_ShouldNotCrash() {
        // Test case: Empty vcard array - should handle gracefully
        JSONArray entities = jsonObject.getJSONArray("entities");
        JSONObject registrantEntity = entities.getJSONObject(0);
        registrantEntity.put("vcardArray", new JSONArray());

        // Also invalidate the redaction object to ensure -63700 is triggered
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "test"); // Change from "Registrant Phone" to "test"

        validate(-63700, voicePointer, "a redaction of type Registrant Phone is required.");
    }

    @Test
    public void testTelPropertyMissingParameters_ShouldNotCrash() {
        // Test case: Replace ALL tel properties with ones missing parameters objects
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);

        // Replace ALL tel properties with malformed ones (missing parameters)
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() > 0 && "tel".equals(property.getString(0))) {
                JSONArray telWithoutParams = new JSONArray();
                telWithoutParams.put("tel"); // Missing parameters object at index 1
                vcardArray.put(i, telWithoutParams);
            }
        }

        // Also invalidate the redaction object to ensure -63700 is triggered
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "test"); // Change from "Registrant Phone" to "test"

        validate(-63700, voicePointer, "a redaction of type Registrant Phone is required.");
    }

    @Test
    public void testTelParametersAsNonObject_ShouldNotCrash() {
        // Test case: Replace ALL tel properties with ones having bad parameters (string instead of object)
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);

        // Replace ALL tel properties with ones having bad parameters
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() > 0 && "tel".equals(property.getString(0))) {
                JSONArray telBadParams = new JSONArray();
                telBadParams.put("tel");
                telBadParams.put("bad-parameters"); // Should be object, not string
                telBadParams.put("uri");
                telBadParams.put("tel:+1-555-123-4567");
                vcardArray.put(i, telBadParams);
            }
        }

        // Also invalidate the redaction object to ensure -63700 is triggered
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "test"); // Change from "Registrant Phone" to "test"

        validate(-63700, voicePointer, "a redaction of type Registrant Phone is required.");
    }

    @Test
    public void testUnicodeInTypeValue_ShouldHandle() {
        // Test case: Unicode characters in type values
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject telParams = tel.getJSONObject(1);
        telParams.put("type", Arrays.asList("тип", "voice", "声音")); // Mix of languages

        validate(); // Should pass - voice found despite unicode
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

    @Test
    public void testMultiRoleRegistrant_ValidationActuallyRuns_NoVoiceTel() throws java.io.IOException {
        // NEGATIVE TEST: Ensure validation logic actually executes for multi-role entities
        // This test verifies the registrant entity is found and phone validation logic runs
        
        String multiRoleContent = getResource("/validators/profile/response_validations/vcard/valid_org_multi_role.json");
        jsonObject = new org.json.JSONObject(multiRoleContent);
        
        // Remove all tel properties to trigger phone validation
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() > 0 && "tel".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }
        
        // Also remove the "Registrant Phone" redaction to trigger -63700
        jsonObject.getJSONArray("redacted").getJSONObject(0).getJSONObject("name").put("type", "test");
        
        // Expected: Should fail with -63700 because validation logic actually runs
        // Don't check exact value string since it's complex, just verify the error code and message
        validate(-63700, 
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"test\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')][3]\",\"pathLang\":\"jsonpath\",\"prePath\":\"book\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}",
            "a redaction of type Registrant Phone is required.");
    }

    @Test
    public void testRegistrantPhoneRedactedButVoiceTelPresent_ShouldTrigger63704() {
        // Ensure the registrant entity has a tel property with type "voice"
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        // Make sure at least one tel property has type "voice"
        boolean foundVoice = false;
        for (int i = 0; i < vcardArray.length(); i++) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() >= 2 && "tel".equals(property.getString(0))) {
                JSONObject telParams = property.getJSONObject(1);
                telParams.put("type", "voice");
                foundVoice = true;
                break;
            }
        }
        if (!foundVoice) {
            // Add a tel property with type voice if not present
            JSONArray tel = new JSONArray();
            tel.put("tel");
            JSONObject telParams = new JSONObject();
            telParams.put("type", "voice");
            tel.put(telParams);
            tel.put("uri");
            tel.put("tel:+1.555.555.5555");
            vcardArray.put(tel);
        }
        // Ensure the redacted array contains a Registrant Phone redaction object
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Phone");
        // Run validation and assert -63704 is triggered
        validate(-63704,
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}",
            "a redaction of type Registrant Phone was found but the phone was not redacted.");
    }

}
