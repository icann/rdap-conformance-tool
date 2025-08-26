package org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard;

import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;

import org.apache.commons.lang3.StringUtils;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;

public class ResponseValidation2Dot7Dot4Dot4_2024Test extends ProfileJsonValidationTestBase {

    static final String cityPointer =
            "#/entities/0/vcardArray/1/3:[\"adr\",{},\"text\",[\"\",\"Suite 1236\",\"4321 Rue Somewhere\",3,\"QC\",\"G1V 2M2\",\"Canada\"]]";
    static final String namePointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"test\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String pathLangBadPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant City\"},\"postPath\":\"$test\",\"pathLang\":\"jsonpath\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String postPathNotExistingPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant City\"},\"postPath\":\"$.status[*]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String methodPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"test2\",\"name\":{\"type\":\"Registrant City\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";

    public ResponseValidation2Dot7Dot4Dot4_2024Test() {
        super("/validators/profile/response_validations/vcard/valid_address_city.json",
                "rdapResponseProfile_2_7_4_4_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot7Dot4Dot4_2024(
                jsonObject.toString(),
                results);
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot4_2024_No_Registrant() {
        JSONArray roles = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("roles");
        roles.put(0, "registrar");
        validate();
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot4_2024_63500() {
        JSONArray cityValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);

        cityValue.put(3, 3);
        validate(-63500, cityPointer, "The city value of the adr property is required on the vcard for the registrant.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot4_2024_63501() {
        JSONArray cityValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        cityValue.put(3, StringUtils.EMPTY);
        redactedObject.getJSONObject("name").put("type", "test");
        validate(-63501, namePointer, "a redaction of type Registrant City is required.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot4_2024_63502_By_PathLang_NotValid() {
        JSONArray cityValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        cityValue.put(3, StringUtils.EMPTY);
        redactedObject.put("postPath", "$test");
        validate(-63502, pathLangBadPointer, "jsonpath is invalid for Registrant City.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot4_2024_63503_By_MissingPathLang_Bad_PrePath() {
        JSONArray cityValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        cityValue.put(3, StringUtils.EMPTY);
        redactedObject.remove("pathLang");
        redactedObject.put("postPath", "$.status[*]");
        validate(-63503, postPathNotExistingPointer, "jsonpath must evaluate to non-empty set for redaction by empty value of Registrant City.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot4_2024_63504_By_Method() {
        JSONArray cityValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        cityValue.put(3, StringUtils.EMPTY);
        redactedObject.put("method", "test2");
        validate(-63504, methodPointer, "Registrant City redaction method must be emptyValue.");
    }

    @Test
    public void testRealWorldData_IcannOrgResponse_ShouldNotTriggerFalsePositive63501() throws Exception {
        // This test uses real-world RDAP data from icann.org to prevent regression
        // The issue was that mixed redacted objects (some with name.type, some with name.description)
        // caused exceptions that incorrectly triggered -63501 validation failures
        
        // Load real-world data from icann.org RDAP response
        String realWorldContent = getResource("/validators/profile/response_validations/vcard/icann_org_city_real_world.json");
        jsonObject = new JSONObject(realWorldContent);
        
        // The real data has:
        // 1. registrant entity with empty city at adr[3][3]: ""
        // 2. redacted array with mixed objects:
        //    - Objects with name.type (e.g., "Registrant City", "Registry Domain ID") 
        //    - Objects with name.description (e.g., "Administrative Contact", "Technical Contact")
        // 3. Proper "Registrant City" redaction object exists at redacted[6]
        
        // Before the fix: Exception thrown when processing objects with name.description
        // caused immediate -63501 failure, preventing discovery of valid "Registrant City" redaction
        
        // After the fix: Code skips objects that cause exceptions and finds the valid redaction
        // This should pass without any validation errors
        validate();
    }

    @Test
    public void testMultipleCityProperties_OneEmptyOneWithValue_ShouldTriggerRedactionValidation() {
        // This test demonstrates the CORRECT behavior:
        // When there are multiple adr properties and ANY city is empty, 
        // redaction validations (-63501 to -63504) SHOULD be triggered to ensure
        // the empty city is properly redacted according to the specification.
        
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        
        // Add a SECOND adr property with EMPTY city
        JSONArray emptyCityAdr = new JSONArray();
        emptyCityAdr.put("adr");           // [0] = property name
        emptyCityAdr.put(new JSONObject()); // [1] = parameters
        emptyCityAdr.put("text");          // [2] = type
        JSONArray emptyCityValue = new JSONArray();
        emptyCityValue.put("");  // [0] = post-office-box
        emptyCityValue.put("");  // [1] = extended-address  
        emptyCityValue.put("");  // [2] = street-address
        emptyCityValue.put("");  // [3] = locality/city (EMPTY)
        emptyCityValue.put("");  // [4] = region
        emptyCityValue.put("");  // [5] = postal-code
        emptyCityValue.put("");  // [6] = country-name
        emptyCityAdr.put(emptyCityValue); // [3] = address array
        
        // Insert the empty city adr property into the vcard array
        vcardArray.put(emptyCityAdr);
        
        // The original adr property at index 3 still has city "Quebec"
        // So we now have:
        // - adr[3][3][3] = "Quebec" (has value)
        // - adr[8][3][3] = "" (empty - our newly added one)
        
        // According to the specification: "If the city value of the adr property above is present but empty, 
        // the following tests apply" - this means if ANY city is empty, trigger redaction validations.
        // The code should find the empty city and verify that proper redaction is configured.
        
        // Expected: Redaction validations should trigger and pass because the test data 
        // has proper "Registrant City" redaction configured in the redacted array
        validate(); // This should pass - redaction validations trigger and find proper configuration
    }

    @Test
    public void testCityWithNonStringValue_ShouldTrigger63500() {
        // Test the logic at line 54: if(vcardAddressValuesArray.get(3) instanceof String city)
        // When city at index 3 is not a String, it should trigger -63500 validation error
        
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        JSONArray adrProperty = vcardArray.getJSONArray(3); // Get the existing adr property
        JSONArray adrValues = adrProperty.getJSONArray(3);  // Get the address values array
        
        // Change the city from string to a number (non-string)
        adrValues.put(3, 12345); // Now adr[3][3] is an integer, not a string
        
        // Expected: Since city is not a string, it should trigger -63500 error
        // The value will show the modified city (12345 instead of original value)
        String expectedPointer = "#/entities/0/vcardArray/1/3:[\"adr\",{},\"text\",[\"\",\"Suite 1236\",\"4321 Rue Somewhere\",12345,\"QC\",\"G1V 2M2\",\"Canada\"]]";
        validate(-63500, expectedPointer, "The city value of the adr property is required on the vcard for the registrant.");
    }

    @Test 
    public void testMixedCityTypes_OnlyStringValuesProcessed() {
        // Test multiple adr properties with mixed city types - only string values should be processed
        
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        
        // First, modify the EXISTING adr property (at index 3) to have a non-string city
        // This ensures the non-string city is processed first in the validation loop
        JSONArray existingAdrProperty = vcardArray.getJSONArray(3); // Get existing adr property
        JSONArray existingAdrValues = existingAdrProperty.getJSONArray(3); // Get address values array
        existingAdrValues.put(3, 999); // Change city from "Quebec" to integer 999
        
        // Add adr property with empty string city (should be reached after -63500 triggers)
        JSONArray emptyStringCityAdr = new JSONArray();
        emptyStringCityAdr.put("adr");
        emptyStringCityAdr.put(new JSONObject());
        emptyStringCityAdr.put("text");
        JSONArray emptyStringCityValues = new JSONArray();
        emptyStringCityValues.put("");
        emptyStringCityValues.put("");
        emptyStringCityValues.put("");
        emptyStringCityValues.put(""); // Empty string city 
        emptyStringCityValues.put("");
        emptyStringCityValues.put("");
        emptyStringCityValues.put("");
        emptyStringCityAdr.put(emptyStringCityValues);
        vcardArray.put(emptyStringCityAdr);
        
        // Expected: The non-string city should trigger -63500 error first
        // Since validation stops at first error, we expect -63500 for the non-string city
        // The value will contain both adr properties that were found by the JSONPath query
        String expectedValue = "#/entities/0/vcardArray/1/3:[\"adr\",{},\"text\",[\"\",\"Suite 1236\",\"4321 Rue Somewhere\",999,\"QC\",\"G1V 2M2\",\"Canada\"]], #/entities/0/vcardArray/1/7:[\"adr\",{},\"text\",[\"\",\"\",\"\",\"\",\"\",\"\",\"\"]]";
        validate(-63500, expectedValue, "The city value of the adr property is required on the vcard for the registrant.");
    }

    @Test
    public void testEmptyRedactedArray_ShouldTrigger63501() {
        // Test edge case where redacted array exists but is empty
        
        JSONArray cityValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);
        cityValue.put(3, StringUtils.EMPTY); // Make city empty to trigger redaction validation
        
        // Clear the redacted array 
        jsonObject.put("redacted", new JSONArray());
        
        // Expected: Should trigger -63501 because no "Registrant City" redaction found
        validate(-63501, 
            "",  // Empty redacted array results in empty value
            "a redaction of type Registrant City is required.");
    }

    @Test
    public void testCityNotEmpty_ShouldNotTriggerRedactionValidations() {
        // Test that when city has a value, no redaction validations are triggered
        
        JSONArray cityValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);
        cityValue.put(3, "Los Angeles"); // Set city to non-empty value
        
        // Expected: Since city is not empty, no redaction validations should trigger
        // Test should pass without any validation errors
        validate();
    }

    @Test
    public void testNoAdrProperty_ShouldNotTriggerValidations() {
        // Test that when there's no adr property in vcard, no validations are triggered
        
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        
        // Remove the adr property (it's at index 3)
        vcardArray.remove(3);
        
        // Expected: Since there's no adr property, no city validations should trigger
        // Test should pass without any validation errors
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
