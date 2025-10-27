package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import java.io.IOException;

import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.EPPRoid;

import org.json.JSONArray;
import org.json.JSONObject;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ResponseValidation2Dot2_1_2024Test extends ProfileJsonValidationTestBase {

    static final String handlePointer =
            "#/handle:2138514test";
    static final String typePointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"test\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.handle\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}";
    static final String pathLangPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registry Domain ID\"},\"pathLang\":\"test\",\"prePath\":\"$.handle\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}";
    static final String pathLangObjectPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registry Domain ID\"},\"pathLang\":{},\"prePath\":\"$.handle\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}";
    static final String pathLangMissingPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registry Domain ID\"},\"prePath\":\"test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}";
    static final String methodPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"test\",\"name\":{\"type\":\"Registry Domain ID\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.handle\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}";

    public ResponseValidation2Dot2_1_2024Test() {
        super("/validators/profile/response_validations/handle/valid.json",
                "rdapResponseProfile_2_2_1_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
    }

    @Override
    public ProfileValidation getProfileValidation() {
        QueryContext domainContext = new QueryContext(
            queryContext.getQueryId(),
            queryContext.getConfig(),
            queryContext.getDatasetService(),
            queryContext.getQuery(),
            queryContext.getResults(),
            RDAPQueryType.DOMAIN
        );
        domainContext.setRdapResponseData(queryContext.getRdapResponseData());
        return new ResponseValidation2Dot2_1_2024(domainContext);
    }

    /**
     * Load a scenario specifically for validation 2.2.1 tests
     * @param scenarioKey The scenario key to load
     */
    private void loadValidation221Scenario(String scenarioKey) throws IOException {
        loadScenario("/validators/profile/response_validations/handle/validation_2_2_1_scenarios.json", scenarioKey);
    }

    @Test
    public void ResponseValidation2Dot2_1_2024_46200() throws IOException {
        loadValidation221Scenario("invalid_handle_format");
        validate(-46200, handlePointer, "The handle in the domain object does not comply with the format "
                + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.");
    }

    @Test
    public void ResponseValidation2Dot2_1_2024_46202() throws IOException {
        loadValidation221Scenario("malformed_redaction_type");
        validate(-46202, typePointer, "a redaction of type Registry Domain ID is required.");
    }

    @Test
    public void ResponseValidation2Dot2_1_2024_46203_By_PathLang() throws IOException {
        loadValidation221Scenario("invalid_pathlang");
        validate(-46203, pathLangPointer, "jsonpath is invalid for Registry Domain ID.");
    }

    @Test
    public void ResponseValidation2Dot2_1_2024_46203_By_PathLangObject() throws IOException {
        loadValidation221Scenario("pathlang_as_object");
        validate(-46203, pathLangObjectPointer, "jsonpath is invalid for Registry Domain ID.");
    }

    @Test
    public void ResponseValidation2Dot2_1_2024_46203_By_MissingPathLang_Bad_PrePath() throws IOException {
        loadValidation221Scenario("missing_pathlang");
        validate(-46203, pathLangMissingPointer, "jsonpath is invalid for Registry Domain ID.");
    }

    @Test
    public void ResponseValidation2Dot2_1_2024_46204_By_Method() throws IOException {
        loadValidation221Scenario("invalid_method");
        validate(-46204, methodPointer, "Registry Domain ID redaction method must be removal if present");
    }

    @Test
    public void ResponseValidation2Dot2_1_2024_46206() throws IOException {
        // Test -46206: Registry Domain ID redaction declared but handle still exists
        loadValidation221Scenario("redaction_consistency_violation");
        
        String expectedPointer = "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registry Domain ID\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.handle\"}";
        
        validate(-46206, expectedPointer, "a redaction of type Registry Domain ID was found but the domain handle was not redacted.");
    }

    @Test
    public void testRealWorldData_IcannOrgResponse_ShouldNotTriggerFalsePositive46202() throws Exception {
        // This test uses real-world RDAP data from icann.org to prevent regression
        // The issue was that mixed redacted objects (some with name.type, some with name.description)
        // caused exceptions that incorrectly triggered -46202 validation failures
        
        // Load real-world data from icann.org RDAP response
        String realWorldContent = getResource("/validators/profile/response_validations/handle/icann_org_domain_id_real_world.json");
        jsonObject = new JSONObject(realWorldContent);
        
        // The real data has:
        // 1. domain object with NO handle property
        // 2. redacted array with mixed objects:
        //    - Objects with name.type (e.g., "Registry Domain ID", "Registrant Phone") 
        //    - Objects with name.description (e.g., "Administrative Contact", "Technical Contact")
        // 3. Proper "Registry Domain ID" redaction object exists at redacted[0]
        
        // Before the fix: Exception thrown when processing objects with name.description
        // caused immediate -46202 failure, preventing discovery of valid "Registry Domain ID" redaction
        
        // After the fix: Code skips objects that cause exceptions and finds the valid redaction
        // This should pass without any validation errors
        validate();
    }

    @Test
    public void testHandlePresent_ShouldNotTriggerValidations() throws IOException {
        // Test that when handle property exists, no redaction validations are triggered
        // (unless there's a Registry Domain ID redaction inconsistency)
        loadValidation221Scenario("handle_present_valid");
        
        // Expected: Since handle is present and no Registry Domain ID redaction exists, no validations trigger
        // Test should pass without any validation errors
        validate();
    }

    @Test
    public void testValidEPPROID_ShouldPass() throws IOException {
        // Test the missing branch: valid EPPROID scenario (line 79)
        loadValidation221Scenario("handle_present_valid");
        
        // This should pass - valid handle format and valid EPPROID
        validate();
    }

    @Test  
    public void testNonStringHandle_ShouldFailValidation() throws IOException {
        // Test the missing branch: non-string handle object scenario (line 66)
        loadValidation221Scenario("malformed_handle_object");
        
        // This should fail validation silently (no specific error message, just returns false)
        // The validation logic returns HandleObjectToValidate with isValid=false but doesn't add error
        QueryContext testContext = new QueryContext(
            queryContext.getQueryId(),
            queryContext.getConfig(),
            queryContext.getDatasetService(),
            queryContext.getQuery(),
            queryContext.getResults(),
            RDAPQueryType.DOMAIN
        );
        testContext.setRdapResponseData(jsonObject.toString());
        ResponseValidation2Dot2_1_2024 validation = new ResponseValidation2Dot2_1_2024(testContext);
        
        boolean result = validation.validate();
        assertThat(result).isFalse(); // Validation should fail
        // No specific error code is expected - just silent failure
    }

    @Test
    public void testEmptyRedactedArray_ShouldTrigger46202() throws IOException {
        // Test edge case where redacted array exists but is empty
        loadValidation221Scenario("empty_redacted_array");
        
        // Expected: Should trigger -46202 because no "Registry Domain ID" redaction found
        validate(-46202, 
            "",  // Empty redacted array results in empty value
            "a redaction of type Registry Domain ID is required.");
    }

    @Test
    public void testMixedRedactedObjects_WithValidRegistryDomainID_ShouldPass() {
        // Test mixed redacted objects where valid "Registry Domain ID" exists among mixed objects
        
        jsonObject.remove("handle"); // Remove handle to trigger redaction validation
        
        // The base test data already has mixed redacted objects and valid "Registry Domain ID"
        // Expected: Should pass because valid redaction exists despite mixed objects
        validate();
    }

    @Test
    public void testInvalidHandleFormat_ShouldTrigger46200() {
        // Test when handle exists but has invalid format

        jsonObject.put("handle", "invalid_format"); // Invalid format - no dash
        jsonObject.remove("redacted"); // Remove redacted array to avoid -46206

        // Expected: Should trigger -46200 for invalid handle format
        validate(-46200, "#/handle:invalid_format", "The handle in the domain object does not comply with the format (\\w|_){1,80}-\\w{1,8} specified in RFC5730.");
    }

    @Test
    public void testValidPrePath_ShouldPass() {
        // Test when prePath is valid and redaction is proper
        
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        
        jsonObject.remove("handle"); // Remove handle to trigger redaction validation
        redactedObject.getJSONObject("name").put("type", "Registry Domain ID");
        redactedObject.put("prePath", "$.handle");
        redactedObject.put("pathLang", "jsonpath");
        redactedObject.put("method", "removal");
        
        // Expected: Should pass because prePath is valid and redaction is proper
        validate();
    }

    @Test
    public void testMissingPrePathProperty_ShouldPass() {
        // Test when prePath property is missing entirely
        
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        
        jsonObject.remove("handle"); // Remove handle to trigger redaction validation
        redactedObject.getJSONObject("name").put("type", "Registry Domain ID");
        redactedObject.remove("prePath"); // Remove prePath property completely
        redactedObject.put("pathLang", "jsonpath");
        redactedObject.put("method", "removal");
        
        // Expected: Should pass because prePath is optional for removal method
        validate();
    }

    @Test
    public void testMissingPathLangProperty_ShouldPass() {
        // Test when pathLang property is missing entirely
        
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        
        jsonObject.remove("handle"); // Remove handle to trigger redaction validation
        redactedObject.getJSONObject("name").put("type", "Registry Domain ID");
        redactedObject.put("prePath", "$.handle");
        redactedObject.remove("pathLang"); // Remove pathLang property completely
        redactedObject.put("method", "removal");
        
        // Expected: Should pass because pathLang is optional
        validate();
    }

    @Test
    public void testNonStringRedactedName_ShouldSkipObject() {
        // Test when redacted name.type is not a string value
        
        jsonObject.remove("handle"); // Remove handle to trigger redaction validation
        
        // Create redacted object with non-string name.type
        JSONObject badRedactedObject = new JSONObject();
        JSONObject badName = new JSONObject();
        badName.put("type", 123); // Non-string type
        badRedactedObject.put("name", badName);
        
        // Add valid "Registry Domain ID" redaction after the bad one
        JSONObject goodRedactedObject = new JSONObject();
        JSONObject goodName = new JSONObject();
        goodName.put("type", "Registry Domain ID");
        goodRedactedObject.put("name", goodName);
        goodRedactedObject.put("prePath", "$.handle");
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
    public void testOnlyObjectsWithNameDescription_ShouldTrigger46202() {
        // Test when redacted array only contains objects with name.description (no name.type objects)
        
        jsonObject.remove("handle"); // Remove handle to trigger redaction validation
        
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
        
        // Expected: Should trigger -46202 because no "Registry Domain ID" redaction found
        String expectedValue = "#/redacted/0:{\"method\":\"removal\",\"name\":{\"description\":\"Administrative Contact\"}}, #/redacted/1:{\"method\":\"removal\",\"name\":{\"description\":\"Technical Contact\"}}";
        validate(-46202, expectedValue, "a redaction of type Registry Domain ID is required.");
    }

    @Test
    public void testInvalidPrePathValue_ShouldTrigger46203() {
        // Test when prePath is not "$.handle"
        
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        
        jsonObject.remove("handle"); // Remove handle to trigger redaction validation
        redactedObject.getJSONObject("name").put("type", "Registry Domain ID");
        redactedObject.put("prePath", "$.invalidPath");
        redactedObject.put("pathLang", "jsonpath");
        redactedObject.put("method", "removal");
        
        // Expected: Should trigger -46203 because prePath must be "$.handle"
        String expectedValue = "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registry Domain ID\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.invalidPath\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}";
        validate(-46203, expectedValue, "jsonpath is invalid for Registry Domain ID.");
    }

    @Test
    public void testInvalidMethodValue_ShouldTrigger46204() {
        // Test when method is not "removal"
        
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        
        jsonObject.remove("handle"); // Remove handle to trigger redaction validation
        redactedObject.getJSONObject("name").put("type", "Registry Domain ID");
        redactedObject.put("prePath", "$.handle");
        redactedObject.put("pathLang", "jsonpath");
        redactedObject.put("method", "emptyValue"); // Wrong method
        
        // Expected: Should trigger -46204 because method must be "removal"
        String expectedValue = "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registry Domain ID\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.handle\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}";
        validate(-46204, expectedValue, "Registry Domain ID redaction method must be removal if present");
    }

    @Test
    public void testMissingMethodProperty_ShouldPass() {
        // Test when method property is missing entirely
        
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        
        jsonObject.remove("handle"); // Remove handle to trigger redaction validation
        redactedObject.getJSONObject("name").put("type", "Registry Domain ID");
        redactedObject.put("prePath", "$.handle");
        redactedObject.put("pathLang", "jsonpath");
        redactedObject.remove("method"); // Remove method property completely
        
        // Expected: Should pass because method is optional (defaults to removal behavior)
        validate();
    }

    @Test
    public void testInvalidEPPROID_ShouldTrigger46201() throws IOException {
        // Test the missing branch: invalid EPPROID scenario (line 79)
        loadValidation221Scenario("invalid_epproid");

        // Mock EPPRoid to return invalid for specific ROID
        EPPRoid eppRoid = datasets.get(EPPRoid.class);
        when(eppRoid.isInvalid("INVALID")).thenReturn(true);

        // Expected: -46201 error for invalid EPPROID
        String expectedPointer = "#/handle:DOM123-INVALID";
        validate(-46201, expectedPointer, "The globally unique identifier in the domain object handle is not registered in EPPROID.");
    }

    // NOTE: The fix for the bug (removing && isValid from line 54) is verified by the fact that
    // testInvalidHandleFormat_ShouldTrigger46200() now requires removing the redacted array.
    // Before the fix, that test would pass with the redacted array present because -46206 was never checked.
    // After the fix, -46206 IS checked even when the handle is invalid, so the test needs the redacted array removed.
    // The scenario "invalid_epproid_with_redaction" in validation_2_2_1_scenarios.json documents the test case.

    @Test
    public void testRedactionConsistencyWithMalformedObjects_ShouldTrigger46206() throws IOException {
        // Test that validateRedactionConsistency properly handles malformed redacted objects
        // This test covers the exception handling at lines 238-241 in ResponseValidation2Dot2_1_2024.java
        // Scenario: Handle is present, redacted array contains malformed objects (missing name.type)
        // alongside valid Registry Domain ID redaction
        loadValidation221Scenario("redaction_consistency_with_malformed_objects");

        // Expected: -46206 error because handle exists but Registry Domain ID redaction is declared
        // The method should skip malformed redacted objects and still find the Registry Domain ID
        String expectedPointer = "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"description\":\"Administrative Contact\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='administrative')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{},\"pathLang\":\"jsonpath\",\"prePath\":\"$.other\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registry Domain ID\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.handle\"}";
        validate(-46206, expectedPointer, "a redaction of type Registry Domain ID was found but the domain handle was not redacted.");
    }
}
