package org.icann.rdapconformance.validator.workflow.profile.rdap_response.entity;

import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

public class ResponseValidationRegistrantHandle_2024Test extends ProfileJsonValidationTestBase {

    static final String handlePointer =
            "#/entities/0:{\"objectClassName\":\"entity\",\"vcardArray\":[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Administrative User\"],[\"org\",{},\"text\",\"Example Inc.\"],[\"adr\",{},\"text\",[\"\",\"Suite 1236\",\"4321 Rue Somewhere\",\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]],[\"email\",{},\"text\",\"administrative.user@example.com\"],[\"tel\",{\"type\":\"voice\"},\"uri\",\"tel:+1-555-555-1236;ext=789\"],[\"tel\",{\"type\":\"fax\"},\"uri\",\"tel:+1-555-555-6321\"]]],\"roles\":[\"registrant\"],\"handle\":\"2138514test\"}";
    static final String namePointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"test\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}";
    static final String pathLangBadPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registry Registrant ID\"},\"prePath\":\"$test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}";
    public ResponseValidationRegistrantHandle_2024Test() {
        super("/validators/profile/response_validations/entity/valid.json",
                "rdapResponseProfile_registrant_handle_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidationRegistrantHandle_2024(
                jsonObject.toString(),
                results,
                datasets);
    }

    @Test
    public void ResponseValidationRegistrationHandle_2024_63100() {
        JSONObject registrantEntity = jsonObject.getJSONArray("entities").getJSONObject(0);

        registrantEntity.put("handle", "2138514test");
        validate(-63100, handlePointer, "The handle of the registrant does not comply with the format (\\w|_){1,80}-\\w{1,8} specified in RFC5730.");
    }

    @Test
    public void ResponseValidationRegistrationHandle_2024_63102() {
        JSONObject registrantEntity = jsonObject.getJSONArray("entities").getJSONObject(0);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        registrantEntity.remove("handle");
        redactedObject.getJSONObject("name").put("type", "test");
        validate(-63102, namePointer, "a redaction of type Registry Registrant ID is required.");
    }

    @Test
    public void ResponseValidationRegistrationHandle_2024_63103() {
        JSONObject registrantEntity = jsonObject.getJSONArray("entities").getJSONObject(0);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        registrantEntity.remove("handle");
        redactedObject.put("prePath", "$test");
        validate(-63103, pathLangBadPointer, "jsonpath is invalid for Registry Registrant ID.");
    }

    @Test
    public void testRealWorldData_IcannOrgResponse_ShouldNotTriggerFalsePositive63102() throws Exception {
        // This test uses real-world RDAP data from icann.org to prevent regression
        // The issue was that mixed redacted objects (some with name.type, some with name.description)
        // caused exceptions that incorrectly triggered -63102 validation failures
        
        // Load real-world data from icann.org RDAP response
        String realWorldContent = getResource("/validators/profile/response_validations/entity/icann_org_registrant_id_real_world.json");
        jsonObject = new JSONObject(realWorldContent);
        
        // The real data has:
        // 1. registrant entity with NO handle property
        // 2. redacted array with mixed objects:
        //    - Objects with name.type (e.g., "Registry Registrant ID", "Registry Domain ID") 
        //    - Objects with name.description (e.g., "Administrative Contact", "Technical Contact")
        // 3. Proper "Registry Registrant ID" redaction object exists at redacted[3]
        
        // Before the fix: Exception thrown when processing objects with name.description
        // caused immediate -63102 failure, preventing discovery of valid "Registry Registrant ID" redaction
        
        // After the fix: Code skips objects that cause exceptions and finds the valid redaction
        // This should pass without any validation errors
        validate();
    }

    @Test
    public void testHandlePresent_ShouldNotTriggerValidations() {
        // Test that when handle property exists, no redaction validations are triggered
        
        JSONObject registrantEntity = jsonObject.getJSONArray("entities").getJSONObject(0);
        registrantEntity.put("handle", "REG123-IANA");
        
        // Expected: Since handle is present, no redaction validations should trigger
        // Test should pass without any validation errors
        validate();
    }

    @Test
    public void testNoRegistrantEntity_ShouldNotTriggerValidations() {
        // Test that when there's no registrant entity, no validations are triggered
        
        JSONArray roles = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("roles");
        roles.put(0, "registrar");
        
        // Expected: Since there's no registrant entity, no handle validations should trigger
        // Test should pass without any validation errors
        validate();
    }

    @Test
    public void testEmptyRedactedArray_ShouldTrigger63102() {
        // Test edge case where redacted array exists but is empty
        
        JSONObject registrantEntity = jsonObject.getJSONArray("entities").getJSONObject(0);
        registrantEntity.remove("handle"); // Remove handle to trigger validation
        
        // Clear the redacted array 
        jsonObject.put("redacted", new JSONArray());
        
        // Expected: Should trigger -63102 because no "Registry Registrant ID" redaction found
        validate(-63102, 
            "",  // Empty redacted array results in empty value
            "a redaction of type Registry Registrant ID is required.");
    }

    @Test
    public void testMixedRedactedObjects_WithValidRegistryRegistrantID_ShouldPass() {
        // Test mixed redacted objects where valid "Registry Registrant ID" exists among mixed objects
        
        JSONObject registrantEntity = jsonObject.getJSONArray("entities").getJSONObject(0);
        registrantEntity.remove("handle"); // Remove handle to trigger redaction validation
        
        // The base test data already has mixed redacted objects and valid "Registry Registrant ID"
        // Expected: Should pass because valid redaction exists despite mixed objects
        validate();
    }

    @Test
    public void testInvalidHandleFormat_ShouldTrigger63100() {
        // Test when handle exists but has invalid format
        
        JSONObject registrantEntity = jsonObject.getJSONArray("entities").getJSONObject(0);
        registrantEntity.put("handle", "invalid_format"); // Invalid format - no dash
        
        // Expected: Should trigger -63100 for invalid handle format
        String expectedValue = "#/entities/0:{\"objectClassName\":\"entity\",\"vcardArray\":[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Administrative User\"],[\"org\",{},\"text\",\"Example Inc.\"],[\"adr\",{},\"text\",[\"\",\"Suite 1236\",\"4321 Rue Somewhere\",\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]],[\"email\",{},\"text\",\"administrative.user@example.com\"],[\"tel\",{\"type\":\"voice\"},\"uri\",\"tel:+1-555-555-1236;ext=789\"],[\"tel\",{\"type\":\"fax\"},\"uri\",\"tel:+1-555-555-6321\"]]],\"roles\":[\"registrant\"],\"handle\":\"invalid_format\"}";
        validate(-63100, expectedValue, "The handle of the registrant does not comply with the format (\\w|_){1,80}-\\w{1,8} specified in RFC5730.");
    }

    @Test
    public void testValidPrePath_ShouldPass() {
        // Test when prePath is valid JSONPath expression and redaction is proper
        
        JSONObject registrantEntity = jsonObject.getJSONArray("entities").getJSONObject(0);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        
        registrantEntity.remove("handle"); // Remove handle to trigger redaction validation
        redactedObject.getJSONObject("name").put("type", "Registry Registrant ID");
        redactedObject.put("prePath", "$.entities[?(@.roles[0]=='registrant')].handle");
        redactedObject.put("pathLang", "jsonpath");
        
        // Expected: Should pass because prePath is valid and redaction is proper
        validate();
    }

    @Test
    public void testMissingPrePathProperty_ShouldPass() {
        // Test when prePath property is missing entirely
        
        JSONObject registrantEntity = jsonObject.getJSONArray("entities").getJSONObject(0);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        
        registrantEntity.remove("handle"); // Remove handle to trigger redaction validation
        redactedObject.getJSONObject("name").put("type", "Registry Registrant ID");
        redactedObject.remove("prePath"); // Remove prePath property completely
        
        // Expected: Should pass because prePath is optional for removal method
        validate();
    }

    @Test
    public void testMissingPathLangProperty_ShouldPass() {
        // Test when pathLang property is missing entirely
        
        JSONObject registrantEntity = jsonObject.getJSONArray("entities").getJSONObject(0);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        
        registrantEntity.remove("handle"); // Remove handle to trigger redaction validation
        redactedObject.getJSONObject("name").put("type", "Registry Registrant ID");
        redactedObject.remove("pathLang"); // Remove pathLang property completely
        
        // Expected: Should pass because pathLang is optional
        validate();
    }

    @Test
    public void testNonStringRedactedName_ShouldSkipObject() {
        // Test when redacted name.type is not a string value
        
        JSONObject registrantEntity = jsonObject.getJSONArray("entities").getJSONObject(0);
        JSONArray redactedArray = jsonObject.getJSONArray("redacted");
        
        registrantEntity.remove("handle"); // Remove handle to trigger redaction validation
        
        // Create redacted object with non-string name.type
        JSONObject badRedactedObject = new JSONObject();
        JSONObject badName = new JSONObject();
        badName.put("type", 123); // Non-string type
        badRedactedObject.put("name", badName);
        
        // Add valid "Registry Registrant ID" redaction after the bad one
        JSONObject goodRedactedObject = new JSONObject();
        JSONObject goodName = new JSONObject();
        goodName.put("type", "Registry Registrant ID");
        goodRedactedObject.put("name", goodName);
        goodRedactedObject.put("prePath", "$.entities[?(@.roles[0]=='registrant')].handle");
        goodRedactedObject.put("pathLang", "jsonpath");
        goodRedactedObject.put("method", "removal");
        
        // Clear and rebuild redacted array
        jsonObject.put("redacted", new JSONArray());
        jsonObject.getJSONArray("redacted").put(badRedactedObject);
        jsonObject.getJSONArray("redacted").put(goodRedactedObject);
        
        // Expected: Should pass because the code skips bad object and finds valid redaction
        validate();
    }

    @Test
    public void testOnlyObjectsWithNameDescription_ShouldTrigger63102() {
        // Test when redacted array only contains objects with name.description (no name.type objects)
        
        JSONObject registrantEntity = jsonObject.getJSONArray("entities").getJSONObject(0);
        registrantEntity.remove("handle"); // Remove handle to trigger redaction validation
        
        // Create redacted objects that only have name.description
        JSONArray newRedactedArray = new JSONArray();
        
        JSONObject adminContact = new JSONObject();
        JSONObject adminName = new JSONObject();
        adminName.put("description", "Administrative Contact");
        adminContact.put("name", adminName);
        adminContact.put("method", "removal");
        
        JSONObject techContact = new JSONObject();
        JSONObject techName = new JSONObject();
        techName.put("description", "Technical Contact");
        techContact.put("name", techName);
        techContact.put("method", "removal");
        
        newRedactedArray.put(adminContact);
        newRedactedArray.put(techContact);
        
        jsonObject.put("redacted", newRedactedArray);
        
        // Expected: Should trigger -63102 because no "Registry Registrant ID" redaction found
        String expectedValue = "#/redacted/0:{\"method\":\"removal\",\"name\":{\"description\":\"Administrative Contact\"}}, #/redacted/1:{\"method\":\"removal\",\"name\":{\"description\":\"Technical Contact\"}}";
        validate(-63102, expectedValue, "a redaction of type Registry Registrant ID is required.");
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
