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

public class ResponseValidation2Dot7Dot4Dot3_2024Test extends ProfileJsonValidationTestBase {

    static final String streetPointer =
            "#/entities/0/vcardArray/1/3:[\"adr\",{},\"text\",[\"\",\"Suite 1236\",3,\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]]";
    static final String namePointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}, #/redacted/3:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"test\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String pathLangBadPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}, #/redacted/3:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$test\",\"pathLang\":\"jsonpath\"}";
    static final String postPathNotExistingPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}, #/redacted/3:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.status[*]\"}";
    static final String methodPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}, #/redacted/3:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"test2\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";

    public ResponseValidation2Dot7Dot4Dot3_2024Test() {
        super("/validators/profile/response_validations/vcard/valid.json",
                "rdapResponseProfile_2_7_4_3_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot7Dot4Dot3_2024(
                jsonObject.toString(),
                results);
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot3_2024_No_Registrant() {
        JSONArray roles = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("roles");
        roles.put(0, "registrar");
        validate();
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot3_2024_63400() {
        JSONArray streetValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);

        streetValue.put(2, 3);
        validate(-63400, streetPointer, "The street value of the adr property is required on the vcard for the registrant.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot3_2024_63401() {
        JSONArray streetValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(3);

        streetValue.put(2, StringUtils.EMPTY);
        redactedObject.getJSONObject("name").put("type", "test");
        validate(-63401, namePointer, "a redaction of type Registrant Street is required.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot3_2024_63402_By_PathLang_NotValid() {
        JSONArray streetValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(3);

        streetValue.put(2, StringUtils.EMPTY);
        redactedObject.put("postPath", "$test");
        validate(-63402, pathLangBadPointer, "jsonpath is invalid for Registrant Street.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot3_2024_63403_By_MissingPathLang_Bad_PrePath() {
        JSONArray streetValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(3);

        streetValue.put(2, StringUtils.EMPTY);
        redactedObject.remove("pathLang");
        redactedObject.put("postPath", "$.status[*]");
        validate(-63403, postPathNotExistingPointer, "jsonpath must evaluate to non-empty set for redaction by empty value of Registrant Street.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot3_2024_63404_By_Method() {
        JSONArray streetValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(3);

        streetValue.put(2, StringUtils.EMPTY);
        redactedObject.put("method", "test2");
        validate(-63404, methodPointer, "Registrant Street redaction method must be emptyValue.");
    }

    @Test
    public void testRealWorldData_IcannOrgResponse_ShouldNotTriggerFalsePositive63401() throws Exception {
        // This test uses real-world RDAP data from icann.org to prevent regression
        // The issue was that mixed redacted objects (some with name.type, some with name.description)
        // caused exceptions that incorrectly triggered -63401 validation failures
        
        // Load real-world data from icann.org RDAP response
        String realWorldContent = getResource("/validators/profile/response_validations/vcard/icann_org_street_real_world.json");
        jsonObject = new JSONObject(realWorldContent);
        
        // The real data has:
        // 1. registrant entity with empty street at adr[3][:3]: ["", "", ""]
        // 2. redacted array with mixed objects:
        //    - Objects with name.type (e.g., "Registrant Street", "Registry Domain ID") 
        //    - Objects with name.description (e.g., "Administrative Contact", "Technical Contact")
        // 3. Proper "Registrant Street" redaction object exists at redacted[5]
        
        // Before the fix: Exception thrown when processing objects with name.description
        // caused immediate -63401 failure, preventing discovery of valid "Registrant Street" redaction
        
        // After the fix: Code skips objects that cause exceptions and finds the valid redaction
        // This should pass without any validation errors
        validate();
    }

    @Test
    public void testMultipleStreetProperties_OneEmptyOneWithValue_ShouldTriggerRedactionValidation() {
        // This test demonstrates the CORRECT behavior:
        // When there are multiple adr properties and ANY street is empty, 
        // redaction validations (-63401 to -63404) SHOULD be triggered to ensure
        // the empty street is properly redacted according to the specification.
        
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        
        // Add a SECOND adr property with EMPTY street
        JSONArray emptyStreetAdr = new JSONArray();
        emptyStreetAdr.put("adr");           // [0] = property name
        emptyStreetAdr.put(new JSONObject()); // [1] = parameters
        emptyStreetAdr.put("text");          // [2] = type
        JSONArray emptyStreetValue = new JSONArray();
        emptyStreetValue.put("");  // [0] = post-office-box
        emptyStreetValue.put("");  // [1] = extended-address  
        emptyStreetValue.put("");  // [2] = street-address (EMPTY)
        emptyStreetValue.put("Los Angeles");  // [3] = locality/city
        emptyStreetValue.put("CA");  // [4] = region
        emptyStreetValue.put("90210");  // [5] = postal-code
        emptyStreetValue.put("US");  // [6] = country-name
        emptyStreetAdr.put(emptyStreetValue); // [3] = address array
        
        // Insert the empty street adr property into the vcard array
        vcardArray.put(emptyStreetAdr);
        
        // The original adr property at index 3 still has street "4321 Rue Somewhere"
        // So we now have:
        // - adr[3][3][2] = "4321 Rue Somewhere" (has value)
        // - adr[8][3][2] = "" (empty - our newly added one)
        
        // According to the specification: "If the street value of the adr property above is present but empty, 
        // the following tests apply" - this means if ANY street is empty, trigger redaction validations.
        // The code should find the empty street and verify that proper redaction is configured.
        
        // Expected: Redaction validations should trigger and pass because the test data 
        // has proper "Registrant Street" redaction configured in the redacted array
        validate(); // This should pass - redaction validations trigger and find proper configuration
    }

    @Test
    public void testStreetWithNonStringValue_ShouldTrigger63400() {
        // Test the logic at line 54: if(vcardAddressValuesArray.get(2) instanceof String street)
        // When street at index 2 is not a String, it should trigger -63400 validation error
        
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        JSONArray adrProperty = vcardArray.getJSONArray(3); // Get the existing adr property
        JSONArray adrValues = adrProperty.getJSONArray(3);  // Get the address values array
        
        // Change the street from string to a number (non-string)
        adrValues.put(2, 12345); // Now adr[3][2] is an integer, not a string
        
        // Expected: Since street is not a string, it should trigger -63400 error
        // The value will show the modified street (12345 instead of original value)
        String expectedPointer = "#/entities/0/vcardArray/1/3:[\"adr\",{},\"text\",[\"\",\"Suite 1236\",12345,\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]]";
        validate(-63400, expectedPointer, "The street value of the adr property is required on the vcard for the registrant.");
    }

    @Test 
    public void testMixedStreetTypes_OnlyStringValuesProcessed() {
        // Test multiple adr properties with mixed street types - only string values should be processed
        
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        
        // First, modify the EXISTING adr property (at index 3) to have a non-string street
        // This ensures the non-string street is processed first in the validation loop
        JSONArray existingAdrProperty = vcardArray.getJSONArray(3); // Get existing adr property
        JSONArray existingAdrValues = existingAdrProperty.getJSONArray(3); // Get address values array
        existingAdrValues.put(2, 999); // Change street from "4321 Rue Somewhere" to integer 999
        
        // Add adr property with empty string street (should be reached after -63400 triggers)
        JSONArray emptyStringStreetAdr = new JSONArray();
        emptyStringStreetAdr.put("adr");
        emptyStringStreetAdr.put(new JSONObject());
        emptyStringStreetAdr.put("text");
        JSONArray emptyStringStreetValues = new JSONArray();
        emptyStringStreetValues.put("");
        emptyStringStreetValues.put("");
        emptyStringStreetValues.put(""); // Empty string street 
        emptyStringStreetValues.put("Los Angeles");
        emptyStringStreetValues.put("CA");
        emptyStringStreetValues.put("90210");
        emptyStringStreetValues.put("US");
        emptyStringStreetAdr.put(emptyStringStreetValues);
        vcardArray.put(emptyStringStreetAdr);
        
        // Expected: The non-string street should trigger -63400 error first
        // Since validation stops at first error, we expect -63400 for the non-string street
        // The value will contain both adr properties that were found by the JSONPath query
        String expectedValue = "#/entities/0/vcardArray/1/3:[\"adr\",{},\"text\",[\"\",\"Suite 1236\",999,\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]], #/entities/0/vcardArray/1/7:[\"adr\",{},\"text\",[\"\",\"\",\"\",\"Los Angeles\",\"CA\",\"90210\",\"US\"]]";
        validate(-63400, expectedValue, "The street value of the adr property is required on the vcard for the registrant.");
    }

    @Test
    public void testEmptyRedactedArray_ShouldTrigger63401() {
        // Test edge case where redacted array exists but is empty
        
        JSONArray streetValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);
        streetValue.put(2, StringUtils.EMPTY); // Make street empty to trigger redaction validation
        
        // Clear the redacted array 
        jsonObject.put("redacted", new JSONArray());
        
        // Expected: Should trigger -63401 because no "Registrant Street" redaction found
        validate(-63401, 
            "",  // Empty redacted array results in empty value
            "a redaction of type Registrant Street is required.");
    }

    @Test
    public void testStreetNotEmpty_ShouldNotTriggerRedactionValidations() {
        // Test that when street has a value, no redaction validations are triggered
        
        JSONArray streetValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);
        streetValue.put(2, "123 Main Street"); // Set street to non-empty value
        
        // Expected: Since street is not empty, no redaction validations should trigger
        // Test should pass without any validation errors
        validate();
    }

    @Test
    public void testNoAdrProperty_ShouldNotTriggerValidations() {
        // Test that when there's no adr property in vcard, no validations are triggered
        
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        
        // Remove the adr property (it's at index 3)
        vcardArray.remove(3);
        
        // Expected: Since there's no adr property, no street validations should trigger
        // Test should pass without any validation errors
        validate();
    }

    @Test
    public void testStreetAsArrayType_EmptyArray_ShouldTriggerRedactionValidation() {
        // Test the logic at line 58: if(vcardAddressValuesArray.get(2) instanceof JSONArray streetArray)
        // When street at index 2 is a JSONArray and is empty, should trigger redaction validation
        
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        JSONArray adrProperty = vcardArray.getJSONArray(3); // Get the existing adr property
        JSONArray adrValues = adrProperty.getJSONArray(3);  // Get the address values array
        
        // Change the street from string to an empty array
        adrValues.put(2, new JSONArray()); // Now adr[3][2] is an empty JSONArray
        
        // Expected: Since street array is empty, it should trigger redaction validations
        // and pass because proper "Registrant Street" redaction is configured
        validate();
    }
}
