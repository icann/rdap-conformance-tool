package org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard;

import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;

import org.apache.commons.lang3.StringUtils;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

public class ResponseValidation2Dot7Dot4Dot6_2024Test extends ProfileJsonValidationTestBase {

    static final String postalCodePointer =
            "#/entities/0/vcardArray/1/3:[\"adr\",{},\"text\",[\"\",\"Suite 1236\",\"4321 Rue Somewhere\",\"Quebec\",\"QC\",3,\"Canada\"]]";
    static final String namePointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"test\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][5]\",\"pathLang\":\"jsonpath\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String pathLangBadPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Postal Code\"},\"postPath\":\"$test\",\"pathLang\":\"jsonpath\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String postPathNotExistingPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Postal Code\"},\"postPath\":\"$.status[*]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String methodPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"test2\",\"name\":{\"type\":\"Registrant Postal Code\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][5]\",\"pathLang\":\"jsonpath\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";

    public ResponseValidation2Dot7Dot4Dot6_2024Test() {
        super("/validators/profile/response_validations/vcard/valid_address_postal_code.json",
                "rdapResponseProfile_2_7_4_6_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot7Dot4Dot6_2024(
                jsonObject.toString(),
                results,
                config);
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot6_2024_No_Registrant() {
        JSONArray roles = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("roles");
        roles.put(0, "registrar");
        validate();
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot6_2024_63600() {
        JSONArray postalCodeValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);

        postalCodeValue.put(5, 3);
        validate(-63600, postalCodePointer, "The postal code value of the adr property is required on the vcard for the registrant.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot6_2024_63601() {
        JSONArray postalCodeValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        postalCodeValue.put(5, StringUtils.EMPTY);
        redactedObject.getJSONObject("name").put("type", "test");
        validate(-63601, namePointer, "a redaction of type Registrant Postal Code is required.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot6_2024_63602_By_PathLang_NotValid() {
        JSONArray postalCodeValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        postalCodeValue.put(5, StringUtils.EMPTY);
        redactedObject.put("postPath", "$test");
        validate(-63602, pathLangBadPointer, "jsonpath is invalid for Registrant Postal Code.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot6_2024_63603_By_MissingPathLang_Bad_PrePath() {
        JSONArray postalCodeValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        postalCodeValue.put(5, StringUtils.EMPTY);
        redactedObject.remove("pathLang");
        redactedObject.put("postPath", "$.status[*]");
        validate(-63603, postPathNotExistingPointer, "jsonpath must evaluate to non-empty set for redaction by empty value of Registrant Postal Code.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot6_2024_63604_By_Method() {
        JSONArray postalCodeValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        postalCodeValue.put(5, StringUtils.EMPTY);
        redactedObject.put("method", "test2");
        validate(-63604, methodPointer, "Registrant Postal Code redaction method must be emptyValue");
    }

    @Test
    public void testRealWorldData_IcannOrgResponse_ShouldNotTriggerFalsePositive63601() throws Exception {
        // This test uses real-world RDAP data from icann.org to prevent regression
        // The issue was that mixed redacted objects (some with name.type, some with name.description)
        // caused exceptions that incorrectly triggered -63601 validation failures
        
        // Load real-world data from icann.org RDAP response
        String realWorldContent = getResource("/validators/profile/response_validations/vcard/icann_org_postal_code_real_world.json");
        jsonObject = new JSONObject(realWorldContent);
        
        // The real data has:
        // 1. registrant entity with empty postal code at adr[3][5]: ""
        // 2. redacted array with mixed objects:
        //    - Objects with name.type (e.g., "Registrant Postal Code", "Registry Domain ID") 
        //    - Objects with name.description (e.g., "Administrative Contact", "Technical Contact")
        // 3. Proper "Registrant Postal Code" redaction object exists at redacted[7]
        
        // Before the fix: Exception thrown when processing objects with name.description
        // caused immediate -63601 failure, preventing discovery of valid "Registrant Postal Code" redaction
        
        // After the fix: Code skips objects that cause exceptions and finds the valid redaction
        // This should pass without any validation errors
        validate();
    }

    @Test
    public void testMultiplePostalCodeProperties_OneEmptyOneWithValue_ShouldTriggerRedactionValidation() {
        // This test demonstrates the CORRECT behavior:
        // When there are multiple adr properties and ANY postal code is empty, 
        // redaction validations (-63601 to -63604) SHOULD be triggered to ensure
        // the empty postal code is properly redacted according to the specification.
        
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        
        // Add a SECOND adr property with EMPTY postal code
        JSONArray emptyPostalCodeAdr = new JSONArray();
        emptyPostalCodeAdr.put("adr");           // [0] = property name
        emptyPostalCodeAdr.put(new JSONObject()); // [1] = parameters
        emptyPostalCodeAdr.put("text");          // [2] = type
        JSONArray emptyPostalCodeValue = new JSONArray();
        emptyPostalCodeValue.put("");  // [0] = post-office-box
        emptyPostalCodeValue.put("");  // [1] = extended-address  
        emptyPostalCodeValue.put("");  // [2] = street-address
        emptyPostalCodeValue.put("");  // [3] = locality
        emptyPostalCodeValue.put("");  // [4] = region
        emptyPostalCodeValue.put("");  // [5] = postal-code (EMPTY)
        emptyPostalCodeValue.put("");  // [6] = country-name
        emptyPostalCodeAdr.put(emptyPostalCodeValue); // [3] = address array
        
        // Insert the empty postal code adr property into the vcard array
        vcardArray.put(emptyPostalCodeAdr);
        
        // The original adr property at index 3 still has postal code "G1V 2M2"
        // So we now have:
        // - adr[3][3][5] = "G1V 2M2" (has value)
        // - adr[8][3][5] = "" (empty - our newly added one)
        
        // According to the specification: "If the postal code value of the adr property above is present but empty, 
        // the following tests apply" - this means if ANY postal code is empty, trigger redaction validations.
        // The code should find the empty postal code and verify that proper redaction is configured.
        
        // Expected: Redaction validations should trigger and pass because the test data 
        // has proper "Registrant Postal Code" redaction configured in the redacted array
        validate(); // This should pass - redaction validations trigger and find proper configuration
    }

    @Test
    public void testPostalCodeWithNonStringValue_ShouldTrigger63600() {
        // Test the logic at line 55: if(vcardAddressValuesArray.get(5) instanceof String postalCode)
        // When postal code at index 5 is not a String, it should trigger -63600 validation error
        
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        JSONArray adrProperty = vcardArray.getJSONArray(3); // Get the existing adr property
        JSONArray adrValues = adrProperty.getJSONArray(3);  // Get the address values array
        
        // Change the postal code from string to a number (non-string)
        adrValues.put(5, 12345); // Now adr[3][5] is an integer, not a string
        
        // Expected: Since postal code is not a string, it should trigger -63600 error
        // The value will show the modified postal code (12345 instead of original value)
        String expectedPointer = "#/entities/0/vcardArray/1/3:[\"adr\",{},\"text\",[\"\",\"Suite 1236\",\"4321 Rue Somewhere\",\"Quebec\",\"QC\",12345,\"Canada\"]]";
        validate(-63600, expectedPointer, "The postal code value of the adr property is required on the vcard for the registrant.");
    }

    @Test 
    public void testMixedPostalCodeTypes_OnlyStringValuesProcessed() {
        // Test multiple adr properties with mixed postal code types - only string values should be processed
        
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        
        // Add adr property with non-string postal code value (should be skipped)
        JSONArray nonStringPostalCodeAdr = new JSONArray();
        nonStringPostalCodeAdr.put("adr");
        nonStringPostalCodeAdr.put(new JSONObject());
        nonStringPostalCodeAdr.put("text");
        JSONArray nonStringPostalCodeValues = new JSONArray();
        nonStringPostalCodeValues.put("");
        nonStringPostalCodeValues.put("");
        nonStringPostalCodeValues.put("");
        nonStringPostalCodeValues.put("");
        nonStringPostalCodeValues.put("");
        nonStringPostalCodeValues.put(999); // Non-string postal code
        nonStringPostalCodeValues.put("");
        nonStringPostalCodeAdr.put(nonStringPostalCodeValues);
        vcardArray.put(nonStringPostalCodeAdr);
        
        // Add adr property with empty string postal code (should trigger redaction validation)
        JSONArray emptyStringPostalCodeAdr = new JSONArray();
        emptyStringPostalCodeAdr.put("adr");
        emptyStringPostalCodeAdr.put(new JSONObject());
        emptyStringPostalCodeAdr.put("text");
        JSONArray emptyStringPostalCodeValues = new JSONArray();
        emptyStringPostalCodeValues.put("");
        emptyStringPostalCodeValues.put("");
        emptyStringPostalCodeValues.put("");
        emptyStringPostalCodeValues.put("");
        emptyStringPostalCodeValues.put("");
        emptyStringPostalCodeValues.put(""); // Empty string postal code - should trigger validation
        emptyStringPostalCodeValues.put("");
        emptyStringPostalCodeAdr.put(emptyStringPostalCodeValues);
        vcardArray.put(emptyStringPostalCodeAdr);
        
        // Expected: Only the empty string postal code should trigger redaction validation
        // The non-string postal code should be ignored, redaction validation should proceed
        validate();
    }

    @Test
    public void testEmptyRedactedArray_ShouldTrigger63601() {
        // Test edge case where redacted array exists but is empty
        
        JSONArray postalCodeValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);
        postalCodeValue.put(5, StringUtils.EMPTY); // Make postal code empty to trigger redaction validation
        
        // Clear the redacted array 
        jsonObject.put("redacted", new JSONArray());
        
        // Expected: Should trigger -63601 because no "Registrant Postal Code" redaction found
        validate(-63601, 
            "",  // Empty redacted array results in empty value
            "a redaction of type Registrant Postal Code is required.");
    }

    @Test
    public void testPostalCodeNotEmpty_ShouldNotTriggerRedactionValidations() {
        // Test that when postal code has a value, no redaction validations are triggered
        
        JSONArray postalCodeValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);
        postalCodeValue.put(5, "12345"); // Set postal code to non-empty value
        
        // Expected: Since postal code is not empty, no redaction validations should trigger
        // Test should pass without any validation errors
        validate();
    }

    @Test
    public void testNoAdrProperty_ShouldNotTriggerValidations() {
        // Test that when there's no adr property in vcard, no validations are triggered
        
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        
        // Remove the adr property (it's at index 3)
        vcardArray.remove(3);
        
        // Expected: Since there's no adr property, no postal code validations should trigger
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
