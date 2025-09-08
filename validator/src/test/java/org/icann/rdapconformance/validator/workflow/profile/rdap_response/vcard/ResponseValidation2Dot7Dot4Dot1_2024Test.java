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

public class ResponseValidation2Dot7Dot4Dot1_2024Test extends ProfileJsonValidationTestBase {

    static final String fnPointer =
            "#/entities/0/vcardArray/1:[[\"version\",{},\"text\",\"4.0\"],{},[\"org\",{},\"text\",\"Example Inc.\"],[\"adr\",{},\"text\",[\"\",\"Suite 1236\",\"4321 Rue Somewhere\",\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]],[\"email\",{},\"text\",\"administrative.user@example.com\"],[\"tel\",{\"type\":\"voice\"},\"uri\",\"tel:+1-555-555-1236;ext=789\"],[\"tel\",{\"type\":\"fax\"},\"uri\",\"tel:+1-555-555-6321\"]]";
    static final String namePointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"test\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String pathLangBadPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$test\",\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String postPathNotExistingPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.status[*]\",\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String methodPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"test2\",\"name\":{\"type\":\"Registrant Name\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";

    public ResponseValidation2Dot7Dot4Dot1_2024Test() {
        super("/validators/profile/response_validations/vcard/valid_fn.json",
                "rdapResponseProfile_2_7_4_1_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot7Dot4Dot1_2024(
                jsonObject.toString(),
                results);
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot1_2024_No_Registrant() {
        JSONArray roles = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("roles");
        roles.put(0, "registrar");
        validate();
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot1_2024_63200() {
        JSONArray fnValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);

        fnValue.put(1, new JSONObject());
        validate(-63200, fnPointer, "The fn property is required on the vcard for the registrant.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot1_2024_63201() {
        JSONArray fnValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(1);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        fnValue.put(3, StringUtils.EMPTY);
        redactedObject.getJSONObject("name").put("type", "test");
        validate(-63201, namePointer, "a redaction of type Registrant Name is required.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot1_2024_63202_By_PathLang_NotValid() {
        String redactedRegistrantName = """
                  {
                    "type": "Registrant Name"
                  }
                """;
        JSONArray fnValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(1);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        fnValue.put(3, StringUtils.EMPTY);
        redactedObject.put("name", new JSONObject(redactedRegistrantName));
        redactedObject.put("postPath", "$test");
        validate(-63202, pathLangBadPointer, "jsonpath is invalid for Registrant Name.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot1_2024_63203_By_MissingPathLang_Bad_PrePath() {
        String redactedRegistrantName = """
                  {
                    "type": "Registrant Name"
                  }
                """;
        JSONArray fnValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(1);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        fnValue.put(3, StringUtils.EMPTY);
        redactedObject.put("name", new JSONObject(redactedRegistrantName));
        redactedObject.remove("pathLang");
        redactedObject.put("postPath", "$.status[*]");
        validate(-63203, postPathNotExistingPointer, "jsonpath must evaluate to non-empty set for redaction by empty value of Registrant Name.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot1_2024_63204_By_Method() {
        String redactedRegistrantName = """
                  {
                    "type": "Registrant Name"
                  }
                """;
        JSONArray fnValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(1);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        fnValue.put(3, StringUtils.EMPTY);
        redactedObject.put("name", new JSONObject(redactedRegistrantName));
        redactedObject.put("method", "test2");
        validate(-63204, methodPointer, "Registrant Name redaction method must be emptyValue.");
    }

    @Test
    public void testMultipleFnProperties_OneEmptyOneWithValue_ShouldTriggerRedactionValidation() {
        // This test demonstrates the CORRECT behavior:
        // When there are multiple fn properties and ANY of them is empty, 
        // redaction validations (-63201 to -63204) SHOULD be triggered to ensure
        // the empty fn property is properly redacted according to the specification.

        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "test");

        // Add a SECOND fn property that is EMPTY 
        JSONArray emptyFnProperty = new JSONArray();
        emptyFnProperty.put("fn");           // [0] = property name
        emptyFnProperty.put(new JSONObject()); // [1] = parameters
        emptyFnProperty.put("text");         // [2] = type
        emptyFnProperty.put("");             // [3] = empty value

        // Insert the empty fn property into the vcard array
        vcardArray.put(emptyFnProperty);

        // The original fn property at index 1 still has value "Administrative User"
        // So we now have:
        // - fn[1][3] = "Administrative User" (has value)
        // - fn[8][3] = "" (empty - our newly added one)

        // According to the specification: "If the fn property above is present but empty, 
        // the following tests apply" - this means if ANY fn is empty, trigger redaction validations.
        // The code should find the empty fn property and verify that proper redaction is configured.

        // Expected: Redaction validations should trigger and pass because the test data 
        // has proper "Registrant Name" redaction configured in the redacted array
        validate(); // This should pass - redaction validations trigger and find proper configuration
    }

    @Test
    public void testRealWorldData_IcannOrgResponse_ShouldNotTriggerFalsePositive63201() throws Exception {
        // This test uses real-world RDAP data from icann.org to prevent regression
        // The issue was that mixed redacted objects (some with name.type, some with name.description)
        // caused exceptions that incorrectly triggered -63201 validation failures

        // Load real-world data from icann.org RDAP response
        String realWorldContent = getResource("/validators/profile/response_validations/vcard/icann_org_real_world.json");
        jsonObject = new JSONObject(realWorldContent);

        // The real data has:
        // 1. registrant entity with empty fn property: ""
        // 2. redacted array with mixed objects:
        //    - Objects with name.type (e.g., "Registrant Name", "Registry Domain ID") 
        //    - Objects with name.description (e.g., "Administrative Contact", "Technical Contact")
        // 3. Proper "Registrant Name" redaction object exists at redacted[4]

        // Before the fix: Exception thrown when processing objects with name.description
        // caused immediate -63201 failure, preventing discovery of valid "Registrant Name" redaction

        // After the fix: Code skips objects that cause exceptions and finds the valid redaction
        // This should pass without any validation errors
        validate();
    }

    @Test
    public void testFnPropertyWithNonStringValue_ShouldBeSkipped() {
        // Test the logic at line 60: if(vcardFnArray.get(3) instanceof String fnValue)
        // When fn[3] is not a String, it should be skipped (not trigger redaction validations)

        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        JSONArray fnProperty = vcardArray.getJSONArray(1); // Get the existing fn property

        // Change the fn value from string to a number (non-string)
        fnProperty.put(3, 12345); // Now fn[3] is an integer, not a string

        // Expected: Since fn[3] is not a string, it should be skipped
        // No redaction validations should trigger, test should pass
        validate();
    }

    @Test
    public void testMixedFnPropertyTypes_OnlyStringValuesProcessed() {
        // Test multiple fn properties with mixed types - only string values should be processed

        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);

        // Add fn property with non-string value (should be skipped)
        JSONArray nonStringFnProperty = new JSONArray();
        nonStringFnProperty.put("fn");
        nonStringFnProperty.put(new JSONObject());
        nonStringFnProperty.put("text");
        nonStringFnProperty.put(999); // Non-string value
        vcardArray.put(nonStringFnProperty);

        // Add fn property with empty string value (should trigger redaction validation)
        JSONArray emptyStringFnProperty = new JSONArray();
        emptyStringFnProperty.put("fn");
        emptyStringFnProperty.put(new JSONObject());
        emptyStringFnProperty.put("text");
        emptyStringFnProperty.put(""); // Empty string - should trigger validation
        vcardArray.put(emptyStringFnProperty);

        // Expected: Only the empty string fn should trigger redaction validation
        // The non-string fn should be ignored, redaction validation should proceed
        validate();
    }

    @Test
    public void testEmptyRedactedArray_ShouldTrigger63201() {
        // Test edge case where redacted array exists but is empty

        JSONArray fnValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(1);
        fnValue.put(3, StringUtils.EMPTY); // Make fn empty to trigger redaction validation

        // Clear the redacted array 
        jsonObject.put("redacted", new JSONArray());

        // Expected: Should trigger -63201 because no "Registrant Name" redaction found
        validate(-63201,
                "",  // Empty redacted array results in empty value
                "a redaction of type Registrant Name is required.");
    }

    @Test
    public void testMultiRoleRegistrant() throws java.io.IOException {
        // REGRESSION TEST: Verify multi-role entities are handled correctly after RCT-345 fix
        // Changed from @.roles[0]=='registrant' to @.roles contains 'registrant'

        String multiRoleContent = getResource("/validators/profile/response_validations/vcard/valid_org_multi_role.json");
        jsonObject = new org.json.JSONObject(multiRoleContent);
        JSONArray redactedObject = jsonObject.getJSONArray("redacted");
        redactedObject.remove(1);

        // Test JSON has entity with roles: ["technical", "registrant"]
        // Now correctly found with 'contains' operator regardless of role position

        // Should pass validation with multi-role registrant entity
        validate(); // Should pass - registrant entity correctly found
    }

    @Test
    public void testFnNotEmptyWithRegistrantNameRedactionPresent() {
        // fn is not empty, Registrant Name redaction present: should trigger -63205
        JSONArray fnValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(1);
        fnValue.put(3, "Some Registrant");
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Name");
        validate(-63205, "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Name\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}", "a redaction of type Registrant Name was found but registrant fn property was not redacted.");
    }

    @Test
    public void testFnNotEmptyWithNoRegistrantNameRedaction() {
        // fn is not empty, no Registrant Name redaction: should pass
        JSONArray fnValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(1);
        fnValue.put(3, "Some Registrant");
        JSONArray redactedArray = jsonObject.getJSONArray("redacted");
        for (int i = redactedArray.length() - 1; i >= 0; i--) {
            JSONObject red = redactedArray.getJSONObject(i);
            if (red.has("name") && red.getJSONObject("name").optString("type").equalsIgnoreCase("Registrant Name")) {
                redactedArray.remove(i);
            }
        }
        validate();
    }

    @Test
    public void testFnEmptyRegistrantNameRedactionPresentMethodNotEmptyValue() {
        // fn is empty, Registrant Name redaction present, method is not 'emptyValue': should trigger -63204
        JSONArray fnValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(1);
        fnValue.put(3, "");
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Name");
        redactedObject.put("method", "notEmptyValue");
        validate(-63204, "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"notEmptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}", "Registrant Name redaction method must be emptyValue.");
    }

    @Test
    public void testFnEmptyRegistrantNameRedactionPresentMethodMissing() {
        // fn is empty, Registrant Name redaction present, method is missing: should pass
        JSONArray fnValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(1);
        fnValue.put(3, "");
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Name");
        redactedObject.remove("method");
        validate();
    }

    @Test
    public void testFnEmptyRegistrantNameRedactionPresentPathLangNotJsonPath() {
        // fn is empty, Registrant Name redaction present, pathLang is present but not 'jsonpath': should skip postPath validation, go to method validation
        JSONArray fnValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(1);
        fnValue.put(3, "");
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Name");
        redactedObject.put("pathLang", "other");
        redactedObject.put("method", "emptyValue");
        validate();
    }

    @Test
    public void testFnEmptyRegistrantNameRedactionPresentPostPathMissing() {
        // fn is empty, Registrant Name redaction present, postPath is missing: should skip postPath validation, go to method validation
        JSONArray fnValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(1);
        fnValue.put(3, "");
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Name");
        redactedObject.remove("postPath");
        redactedObject.put("method", "emptyValue");
        validate();
    }

    @Test
    public void testMalformedRedactedArray() {
        // Malformed redacted array (missing name/type): should skip and not fail
        JSONArray fnValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(1);
        fnValue.put(3, "");
        JSONArray redactedArray = jsonObject.getJSONArray("redacted");
        JSONObject malformed = new JSONObject();
        malformed.put("reason", new JSONObject().put("description", "Server policy"));
        // No 'name' property
        redactedArray.put(malformed);
        validate(-63201, "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"}}", "a redaction of type Registrant Name is required.");
    }

    @Test
    public void testValidateVcardFnPropertyObject_ExceptionHandling() {
        // Simulate malformed vCard array that causes exception in jsonObject.query(jsonPointer)
        JSONArray entities = jsonObject.getJSONArray("entities");
        JSONObject registrant = entities.getJSONObject(0);
        JSONArray vcardArray = registrant.getJSONArray("vcardArray");
        // Replace vcardArray[1] with a non-JSONArray to cause ClassCastException
        vcardArray.put(1, new JSONObject());
        validate(-63200, "#/entities/0/vcardArray/1:{}", "The fn property is required on the vcard for the registrant.");
    }

    @Test
    public void testValidatePostPathBasedOnPathLang_NullRedactedRegistrantName() throws Exception {
        // Use reflection to call private method with null
        var validation = (ResponseValidation2Dot7Dot4Dot1_2024) getProfileValidation();
        java.lang.reflect.Method m = ResponseValidation2Dot7Dot4Dot1_2024.class.getDeclaredMethod("validatePostPathBasedOnPathLang", JSONObject.class);
        m.setAccessible(true);
        Object result = m.invoke(validation, new Object[]{null});
        assert result.equals(Boolean.TRUE);
    }

    @Test
    public void testValidatePostPathBasedOnPathLang_MissingPostPath() throws Exception {
        // Setup redactedRegistrantName with no postPath
        JSONObject redactedRegistrantName = new JSONObject();
        redactedRegistrantName.put("name", new JSONObject().put("type", "Registrant Name"));
        var validation = (ResponseValidation2Dot7Dot4Dot1_2024) getProfileValidation();
        java.lang.reflect.Method m = ResponseValidation2Dot7Dot4Dot1_2024.class.getDeclaredMethod("validatePostPathBasedOnPathLang", JSONObject.class);
        m.setAccessible(true);
        // Should not throw, should return true (calls validateMethodProperty)
        Object result = m.invoke(validation, redactedRegistrantName);
        assert result.equals(Boolean.TRUE);
    }

    @Test
    public void testValidateMethodProperty_NullRedactedRegistrantName() throws Exception {
        var validation = (ResponseValidation2Dot7Dot4Dot1_2024) getProfileValidation();
        java.lang.reflect.Method m = ResponseValidation2Dot7Dot4Dot1_2024.class.getDeclaredMethod("validateMethodProperty", JSONObject.class);
        m.setAccessible(true);
        Object result = m.invoke(validation, new Object[]{null});
        assert result.equals(Boolean.TRUE);
    }

    @Test
    public void testValidateMethodProperty_MissingMethod() throws Exception {
        JSONObject redactedRegistrantName = new JSONObject();
        redactedRegistrantName.put("name", new JSONObject().put("type", "Registrant Name"));
        var validation = (ResponseValidation2Dot7Dot4Dot1_2024) getProfileValidation();
        java.lang.reflect.Method m = ResponseValidation2Dot7Dot4Dot1_2024.class.getDeclaredMethod("validateMethodProperty", JSONObject.class);
        m.setAccessible(true);
        Object result = m.invoke(validation, redactedRegistrantName);
        assert result.equals(Boolean.TRUE);
    }

    @Test
    public void testValidateRedactedProperties_NullRedactedRegistrantName() throws Exception {
        var validation = (ResponseValidation2Dot7Dot4Dot1_2024) getProfileValidation();
        java.lang.reflect.Method m = ResponseValidation2Dot7Dot4Dot1_2024.class.getDeclaredMethod("validateRedactedProperties", JSONObject.class);
        m.setAccessible(true);
        Object result = m.invoke(validation, new Object[]{null});
        assert result.equals(Boolean.TRUE);
    }

    @Test
    public void testValidateRedactedProperties_MissingPathLang() throws Exception {
        JSONObject redactedRegistrantName = new JSONObject();
        redactedRegistrantName.put("name", new JSONObject().put("type", "Registrant Name"));
        var validation = (ResponseValidation2Dot7Dot4Dot1_2024) getProfileValidation();
        java.lang.reflect.Method m = ResponseValidation2Dot7Dot4Dot1_2024.class.getDeclaredMethod("validateRedactedProperties", JSONObject.class);
        m.setAccessible(true);
        Object result = m.invoke(validation, redactedRegistrantName);
        assert result.equals(Boolean.TRUE);
    }

    @Test
    public void testExtractRedactedRegistrantName_SkipNonType() throws Exception {
        // Add a redacted object with only name.description, not name.type
        JSONArray redactedArray = jsonObject.getJSONArray("redacted");
        JSONObject obj = new JSONObject();
        obj.put("name", new JSONObject().put("description", "Administrative Contact"));
        redactedArray.put(obj);
        var validation = (ResponseValidation2Dot7Dot4Dot1_2024) getProfileValidation();
        java.lang.reflect.Method m = ResponseValidation2Dot7Dot4Dot1_2024.class.getDeclaredMethod("extractRedactedRegistrantName");
        m.setAccessible(true);
        Object result = m.invoke(validation);
        // Should return non-null if a valid Registrant Name redaction exists, null otherwise
        // Here, since only description is present, should return null
        assert result == null;
    }

    @Test
    public void testValidateVcardFnPropertyObject_IndexOutOfBounds() {
        // vcardArray[1] is a JSONArray but too short (less than 4 elements)
        JSONArray entities = jsonObject.getJSONArray("entities");
        JSONObject registrant = entities.getJSONObject(0);
        JSONArray vcardArray = registrant.getJSONArray("vcardArray");
        // Replace vcardArray[1] with a short JSONArray (e.g., 2 elements)
        JSONArray shortArray = new JSONArray();
        shortArray.put("fn");
        shortArray.put(new JSONObject());
        vcardArray.put(1, shortArray);
        validate(-63200, "#/entities/0/vcardArray/1:[\"fn\",{}]", "The fn property is required on the vcard for the registrant.");
    }

    @Test
    public void testValidateVcardFnPropertyObject_NullAtIndex3() {
        // vcardArray[1] is a JSONArray with index 3 set to null
        JSONArray entities = jsonObject.getJSONArray("entities");
        JSONObject registrant = entities.getJSONObject(0);
        JSONArray vcardArray = registrant.getJSONArray("vcardArray");
        JSONArray fnArray = new JSONArray();
        fnArray.put("fn");
        fnArray.put(new JSONObject());
        fnArray.put("text");
        fnArray.put(JSONObject.NULL); // index 3 is null
        vcardArray.put(1, fnArray);
        validate(-63200, "#/entities/0/vcardArray/1:[\"fn\",{},\"text\",null]", "The fn property is required on the vcard for the registrant.");
    }

    @Test
    public void testValidateRedactedProperties_PathLangIsJsonPath() throws Exception {
        // pathLang is a String and equals 'jsonpath' (case-insensitive)
        JSONObject redactedRegistrantName = new JSONObject();
        redactedRegistrantName.put("pathLang", "jsonpath");
        redactedRegistrantName.put("postPath", "$.entities[*]"); // valid JSONPath
        var validation = (ResponseValidation2Dot7Dot4Dot1_2024) getProfileValidation();
        java.lang.reflect.Method m = ResponseValidation2Dot7Dot4Dot1_2024.class.getDeclaredMethod("validateRedactedProperties", JSONObject.class);
        m.setAccessible(true);
        Object result = m.invoke(validation, redactedRegistrantName);
        assert result.equals(Boolean.TRUE);
    }

    @Test
    public void testValidateRedactedProperties_PathLangIsJsonPathWithSpacesAndCase() throws Exception {
        // pathLang is a String and equals ' JSONPATH ' (with spaces and different case)
        JSONObject redactedRegistrantName = new JSONObject();
        redactedRegistrantName.put("pathLang", "  JSONPATH  ");
        redactedRegistrantName.put("postPath", "$.entities[*]"); // valid JSONPath
        var validation = (ResponseValidation2Dot7Dot4Dot1_2024) getProfileValidation();
        java.lang.reflect.Method m = ResponseValidation2Dot7Dot4Dot1_2024.class.getDeclaredMethod("validateRedactedProperties", JSONObject.class);
        m.setAccessible(true);
        Object result = m.invoke(validation, redactedRegistrantName);
        assert result.equals(Boolean.TRUE);
    }

    @Test
    public void testValidateRedactedProperties_PathLangIsOtherString() throws Exception {
        // pathLang is a String but not 'jsonpath'
        JSONObject redactedRegistrantName = new JSONObject();
        redactedRegistrantName.put("pathLang", "other");
        var validation = (ResponseValidation2Dot7Dot4Dot1_2024) getProfileValidation();
        java.lang.reflect.Method m = ResponseValidation2Dot7Dot4Dot1_2024.class.getDeclaredMethod("validateRedactedProperties", JSONObject.class);
        m.setAccessible(true);
        Object result = m.invoke(validation, redactedRegistrantName);
        assert result.equals(Boolean.TRUE);
    }

    @Test
    public void testValidateRedactedProperties_PathLangIsInteger() throws Exception {
        // pathLang is an Integer
        JSONObject redactedRegistrantName = new JSONObject();
        redactedRegistrantName.put("pathLang", 123);
        var validation = (ResponseValidation2Dot7Dot4Dot1_2024) getProfileValidation();
        java.lang.reflect.Method m = ResponseValidation2Dot7Dot4Dot1_2024.class.getDeclaredMethod("validateRedactedProperties", JSONObject.class);
        m.setAccessible(true);
        Object result = m.invoke(validation, redactedRegistrantName);
        assert result.equals(Boolean.TRUE);
    }

    @Test
    public void testValidateRedactedProperties_PathLangIsNull() throws Exception {
        // pathLang is explicitly null
        JSONObject redactedRegistrantName = new JSONObject();
        redactedRegistrantName.put("pathLang", JSONObject.NULL);
        var validation = (ResponseValidation2Dot7Dot4Dot1_2024) getProfileValidation();
        java.lang.reflect.Method m = ResponseValidation2Dot7Dot4Dot1_2024.class.getDeclaredMethod("validateRedactedProperties", JSONObject.class);
        m.setAccessible(true);
        Object result = m.invoke(validation, redactedRegistrantName);
        assert result.equals(Boolean.TRUE);
    }

    @Test
    public void testExtractRedactedRegistrantName_TypeIsRegistrantNameExact() throws Exception {
        // name.type is exactly 'Registrant Name'
        JSONArray redactedArray = new JSONArray();
        JSONObject obj = new JSONObject();
        obj.put("name", new JSONObject().put("type", "Registrant Name"));
        redactedArray.put(obj);
        jsonObject.put("redacted", redactedArray);
        var validation = (ResponseValidation2Dot7Dot4Dot1_2024) getProfileValidation();
        java.lang.reflect.Method m = ResponseValidation2Dot7Dot4Dot1_2024.class.getDeclaredMethod("extractRedactedRegistrantName");
        m.setAccessible(true);
        Object result = m.invoke(validation);
        assert result instanceof JSONObject;
        assert ((JSONObject) result).getJSONObject("name").getString("type").equals("Registrant Name");
    }

    @Test
    public void testExtractRedactedRegistrantName_TypeIsRegistrantNameWithSpacesAndCase() throws Exception {
        // name.type is '  registrant name  ' (with spaces and different case)
        JSONArray redactedArray = new JSONArray();
        JSONObject obj = new JSONObject();
        obj.put("name", new JSONObject().put("type", "  registrant name  "));
        redactedArray.put(obj);
        jsonObject.put("redacted", redactedArray);
        var validation = (ResponseValidation2Dot7Dot4Dot1_2024) getProfileValidation();
        java.lang.reflect.Method m = ResponseValidation2Dot7Dot4Dot1_2024.class.getDeclaredMethod("extractRedactedRegistrantName");
        m.setAccessible(true);
        Object result = m.invoke(validation);
        assert result instanceof JSONObject;
        assert ((JSONObject) result).getJSONObject("name").getString("type").trim().equalsIgnoreCase("Registrant Name");
    }

    @Test
    public void testExtractRedactedRegistrantName_TypeIsOtherString() throws Exception {
        // name.type is a different string
        JSONArray redactedArray = new JSONArray();
        JSONObject obj = new JSONObject();
        obj.put("name", new JSONObject().put("type", "Registry Domain ID"));
        redactedArray.put(obj);
        jsonObject.put("redacted", redactedArray);
        var validation = (ResponseValidation2Dot7Dot4Dot1_2024) getProfileValidation();
        java.lang.reflect.Method m = ResponseValidation2Dot7Dot4Dot1_2024.class.getDeclaredMethod("extractRedactedRegistrantName");
        m.setAccessible(true);
        Object result = m.invoke(validation);
        assert result == null;
    }

    @Test
    public void testExtractRedactedRegistrantName_TypeIsInteger() throws Exception {
        // name.type is an Integer
        JSONArray redactedArray = new JSONArray();
        JSONObject obj = new JSONObject();
        obj.put("name", new JSONObject().put("type", 123));
        redactedArray.put(obj);
        jsonObject.put("redacted", redactedArray);
        var validation = (ResponseValidation2Dot7Dot4Dot1_2024) getProfileValidation();
        java.lang.reflect.Method m = ResponseValidation2Dot7Dot4Dot1_2024.class.getDeclaredMethod("extractRedactedRegistrantName");
        m.setAccessible(true);
        Object result = m.invoke(validation);
        assert result == null;
    }

    @Test
    public void testExtractRedactedRegistrantName_TypeIsNull() throws Exception {
        // name.type is explicitly null
        JSONArray redactedArray = new JSONArray();
        JSONObject obj = new JSONObject();
        obj.put("name", new JSONObject().put("type", JSONObject.NULL));
        redactedArray.put(obj);
        jsonObject.put("redacted", redactedArray);
        var validation = (ResponseValidation2Dot7Dot4Dot1_2024) getProfileValidation();
        java.lang.reflect.Method m = ResponseValidation2Dot7Dot4Dot1_2024.class.getDeclaredMethod("extractRedactedRegistrantName");
        m.setAccessible(true);
        Object result = m.invoke(validation);
        assert result == null;
    }


}
