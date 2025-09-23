package org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

public class ResponseValidationRegistrantEmail_2024Test extends ProfileJsonValidationTestBase {

    static final String emailPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"test\"},\"prePath\":\"$.test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String pathLangBadPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Email\"},\"prePath\":\"$test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String prePathExistingPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Email\"},\"prePath\":\"$.redacted[*]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String methodPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"test2\",\"name\":{\"type\":\"Registrant Email\"},\"prePath\":\"$.test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";

    public ResponseValidationRegistrantEmail_2024Test() {
        super("/validators/profile/response_validations/vcard/valid_email.json",
                "rdapResponseProfile_Registrant_Email_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
        config = mock(RDAPValidatorConfiguration.class);
    }

    @Override
    public ProfileValidation getProfileValidation() {
        when(config.isGtldRegistry()).thenReturn(true);
        return new ResponseValidationRegistrantEmail_2024(
                jsonObject.toString(),
                results,
                config);
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot8_2024_No_Registrant() {
        when(config.isGtldRegistry()).thenReturn(true);
        JSONArray roles = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("roles");
        roles.put(0, "registrar");
        validate();
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot8_2024_63700() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Remove ALL tel properties from registrant entity to trigger validation
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        // Remove email properties from registrant
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() > 0 && "email".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }

        redactedObject.getJSONObject("name").put("type", "test");
        validate(-65400, emailPointer, "a redaction of type Registrant Email is required.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot8_2024_63701_By_PathLang_NotValid() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Remove ALL tel properties from registrant entity to trigger validation
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        // Remove both email from registrant
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() > 0 && "email".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }

        redactedObject.getJSONObject("name").put("type", "Registrant Email");
        redactedObject.put("prePath", "$test");
        validate(-65401, pathLangBadPointer, "jsonpath is invalid for Registrant Email");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot8_2024_63702_By_MissingPathLang_Bad_PrePath() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Remove ALL tel properties from registrant entity to trigger validation
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        // Remove email properties from registrant
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() > 0 && "email".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }

        redactedObject.getJSONObject("name").put("type", "Registrant Email");
        redactedObject.remove("pathLang");
        redactedObject.put("prePath", "$.redacted[*]");
        validate(-65402, prePathExistingPointer, "jsonpath must evaluate to a zero set for redaction by removal of Registrant Email.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot8_2024_63703_By_Method() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Remove ALL tel properties from registrant entity to trigger validation
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        // Remove email from registrant
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() > 0 && "email".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }

        redactedObject.getJSONObject("name").put("type", "Registrant Email");
        redactedObject.put("method", "test2");
        validate(-65403, methodPointer, "Registrant Email redaction method must be removal if present");
    }

    @Test
    public void testRealWorldData_IcannOrgResponse_ShouldNotTriggerFalsePositive65400() throws Exception {
        when(config.isGtldRegistry()).thenReturn(true);
        // This test uses real-world RDAP data from icann.org to prevent regression
        // The issue was that mixed redacted objects (some with name.type, some with name.description)
        // caused exceptions that incorrectly triggered -63700 validation failures

        // Load real-world data from icann.org RDAP response
        String realWorldContent = getResource("/validators/profile/response_validations/vcard/icann_org_phone_real_world.json");
        jsonObject = new JSONObject(realWorldContent);

        // The real data has:
        // 1. registrant entity with NO email property parameter
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
    public void testEmailTelPresent_ShouldNotTriggerValidations() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test that when email property exists, no redaction validations are triggered

        // The base test data already has a email property, so no modifications needed
        // Expected: Since email is present, no redaction validations should trigger
        // Test should pass without any validation errors
        validate();
    }

    @Test
    public void testNoRegistrantEntity_ShouldNotTriggerValidations() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test that when there's no registrant entity, no validations are triggered

        JSONArray roles = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("roles");
        roles.put(0, "registrar");

        // Expected: Since there's no registrant entity, no phone validations should trigger
        // Test should pass without any validation errors
        validate();
    }

    @Test
    public void testEmptyRedactedArray_ShouldTrigger63700() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test edge case where redacted array exists but is empty

        // Remove ALL tel properties from registrant entity to trigger validation
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);

        // Remove email properties from registrant
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() > 0 && "email".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }

        // Clear the redacted array 
        jsonObject.put("redacted", new JSONArray());

        // Expected: Should trigger -63700 because no "Registrant Email" redaction found
        validate(-65400,
                "",  // Empty redacted array results in empty value
                "a redaction of type Registrant Email is required.");
    }

    @Test
    public void testNoEmailButOtherTelTypes_ShouldTriggerValidations() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test when there are email properties parameter

        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        JSONArray existingEmail = vcardArray.getJSONArray(4); // Get existing tel property// Get parameters object
        existingEmail.remove(0);

        redactedObject.getJSONObject("name").put("type", "Registrant Email");

        // Expected: Since no email property exists, redaction validations should trigger
        // and pass because proper "Registrant Email" redaction is configured
        validate();
    }

    @Test
    public void testInvalidPrePathExists_ShouldTrigger63701() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test when prePath contains invalid JSONPath syntax

        JSONArray email = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        email.remove(0); // Remove email parameter to trigger redaction validation
        redactedObject.put("prePath", "$invalid[syntax"); // Invalid JSONPath

        // Expected: Should trigger -63701 for invalid JSONPath
        redactedObject.getJSONObject("name").put("type", "Registrant Email");
        String expectedValue = "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Email\"},\"prePath\":\"$invalid[syntax\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
        validate(-65401, expectedValue, "jsonpath is invalid for Registrant Email");
    }

    @Test
    public void testValidPrePathWithResults_ShouldTrigger65402() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test when prePath is valid but evaluates to non-empty set (should be empty for removal)

        JSONArray email = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        email.remove(0); // Remove email parameter to trigger redaction validation
        redactedObject.put("prePath", "$.entities[*]"); // Valid JSONPath that returns results
        redactedObject.getJSONObject("name").put("type", "Registrant Email");
        // Expected: Should trigger -65402 because prePath should evaluate to zero set for removal
        String expectedValue = "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Email\"},\"prePath\":\"$.entities[*]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
        validate(-65402, expectedValue, "jsonpath must evaluate to a zero set for redaction by removal of Registrant Email.");
    }


    @Test
    public void testMissingPrePathProperty_ShouldPass() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test when prePath property is missing entirely

        JSONArray email = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        email.remove(0); // Remove email parameter to trigger redaction validation
        redactedObject.remove("prePath"); // Remove prePath property completely
        redactedObject.getJSONObject("name").put("type", "Registrant Email");
        // Expected: Should pass because prePath is optional for removal method
        validate();
    }

    @Test
    public void testInvalidMethod_ShouldTrigger65403() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test when method is not "removal"

        JSONArray email = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        email.remove(0); // Remove email parameter to trigger redaction validation
        redactedObject.put("method", "emptyValue"); // Wrong method for email validation

        redactedObject.getJSONObject("name").put("type", "Registrant Email");
        // Expected: Should trigger -65403 because method must be "removal" for email
        String expectedValue = "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Email\"},\"prePath\":\"$.test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
        validate(-65403, expectedValue, "Registrant Email redaction method must be removal if present");
    }

    @Test
    public void testMissingMethodProperty_ShouldPass() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test when method property is missing entirely

        JSONArray email = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        email.remove(0); // Remove email parameter to trigger redaction validation
        redactedObject.remove("method"); // Remove method property completely
        redactedObject.getJSONObject("name").put("type", "Registrant Email");
        // Expected: Should pass because method is optional (defaults to removal behavior)
        validate();
    }

    @Test
    public void testValidateRedactedArrayForEmailValue_EmailPresentButRedactionFound_ShouldTrigger65404() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test the scenario where email is present in vCard but there's also a redaction for it
        // This should trigger error -65404

        // Keep the existing email in vCard (email is present)
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        // Ensure email property exists (it should already exist in the test data)
        boolean emailExists = false;
        for (int i = 0; i < vcardArray.length(); i++) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (!property.isEmpty() && "email".equals(property.getString(0))) {
                emailExists = true;
                break;
            }
        }

        // If email doesn't exist, add it
        if (!emailExists) {
            JSONArray emailProperty = new JSONArray();
            emailProperty.put("email");
            emailProperty.put(new JSONObject());
            emailProperty.put("text");
            emailProperty.put("test@example.com");
            vcardArray.put(emailProperty);
        }

        // Set up redaction object for Registrant Email
        redactedObject.getJSONObject("name").put("type", "Registrant Email");

        // Expected: Should trigger -65404 because email exists but redaction is also present
        String expectedValue = "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Email\"},\"prePath\":\"$.test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
        validate(-65404, expectedValue, "a redaction of type Registrant Email was found but email was not redacted.");
    }

    @Test
    public void testValidateRedactedArrayForEmailValue_EmailPresentNoRedaction_ShouldPass() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test the scenario where email is present in vCard and no redaction exists for it
        // This should pass (return true)

        // Keep the existing email in vCard (email is present)
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        // Ensure email property exists
        boolean emailExists = false;
        for (int i = 0; i < vcardArray.length(); i++) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (!property.isEmpty() && "email".equals(property.getString(0))) {
                emailExists = true;
                break;
            }
        }

        // If email doesn't exist, add it
        if (!emailExists) {
            JSONArray emailProperty = new JSONArray();
            emailProperty.put("email");
            emailProperty.put(new JSONObject());
            emailProperty.put("text");
            emailProperty.put("test@example.com");
            vcardArray.put(emailProperty);
        }

        // Ensure no redaction object exists for Registrant Email
        redactedObject.getJSONObject("name").put("type", "Other Type");

        // Expected: Should pass because email exists and no redaction for it
        validate();
    }

    @Test
    public void testValidateRedactedProperties_WithPathLangJsonpath_ShouldCallValidatePrePath() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test when pathLang exists and equals "jsonpath" - should call validatePrePathBasedOnPathLang

        // Remove email to trigger redaction validation path
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (!property.isEmpty() && "email".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }

        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Email");
        redactedObject.put("pathLang", "jsonpath");
        redactedObject.put("prePath", "$.nonexistent.path"); // Valid but non-existent path

        // This should trigger the pathLang == "jsonpath" branch and call validatePrePathBasedOnPathLang
        validate();
    }

    @Test
    public void testValidateRedactedProperties_WithPathLangNonJsonpath_ShouldReturnTrue() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test when pathLang exists but doesn't equal "jsonpath" - should return true

        // Remove email to trigger redaction validation path
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (!property.isEmpty() && "email".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }

        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Email");
        redactedObject.put("pathLang", "xpath"); // Different from "jsonpath"

        // This should trigger the pathLang != "jsonpath" branch and return true
        validate();
    }

    @Test
    public void testValidateRedactedProperties_WithPathLangCaseInsensitive_ShouldCallValidatePrePath() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test case-insensitive matching for pathLang "JSONPATH"

        // Remove email to trigger redaction validation path
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (!property.isEmpty() && "email".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }

        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Email");
        redactedObject.put("pathLang", "JSONPATH"); // Uppercase - should still match
        redactedObject.put("prePath", "$.nonexistent.path");

        // This should trigger the pathLang.equalsIgnoreCase("jsonpath") branch
        validate();
    }

    @Test
    public void testValidateRedactedProperties_WithPathLangWhitespace_ShouldCallValidatePrePath() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test pathLang with whitespace - should be trimmed and matched

        // Remove email to trigger redaction validation path
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (!property.isEmpty() && "email".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }

        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Email");
        redactedObject.put("pathLang", "  jsonpath  "); // With whitespace
        redactedObject.put("prePath", "$.nonexistent.path");

        // This should trigger the pathLang.trim().equalsIgnoreCase("jsonpath") branch
        validate();
    }

    @Test
    public void testValidateRedactedProperties_WithNonStringPathLang_ShouldReturnTrue() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test when pathLang exists but is not a string - should return true

        // Remove email to trigger redaction validation path
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (!property.isEmpty() && "email".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }

        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Email");
        redactedObject.put("pathLang", 123); // Non-string value

        // This should not match the instanceof String check and return true
        validate();
    }

    @Test
    public void testValidateRedactedProperties_WithMissingPathLang_ShouldCallValidatePrePath() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test when pathLang is missing - should trigger exception and call validatePrePathBasedOnPathLang

        // Remove email to trigger redaction validation path
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (!property.isEmpty() && "email".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }

        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Email");
        // Don't set pathLang - this should trigger the exception handling
        redactedObject.put("prePath", "$.nonexistent.path");

        // This should trigger the catch block and call validatePrePathBasedOnPathLang
        validate();
    }

    @Test
    public void testValidateRedactedProperties_ExceptionHandling_ShouldCallValidatePrePath() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test exception handling in validateRedactedProperties

        // Remove email to trigger redaction validation path
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (!property.isEmpty() && "email".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }

        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Email");
        // Remove pathLang entirely to trigger exception in redactedEmail.get("pathLang")
        redactedObject.remove("pathLang");

        // This should trigger the exception handling and call validatePrePathBasedOnPathLang
        validate();
    }

    @Test
    public void testValidateMethodProperty_WithValidRemovalMethod_ShouldPass() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test when method property is "removal" - should pass validation

        // Remove email to trigger redaction validation path
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() > 0 && "email".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }

        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Email");
        redactedObject.put("method", "removal"); // Valid method

        // This should pass validation as method is "removal"
        validate();
    }

    @Test
    public void testValidateMethodProperty_WithRemovalCaseInsensitive_ShouldPass() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test case-insensitive matching for method "REMOVAL"

        // Remove email to trigger redaction validation path
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() > 0 && "email".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }

        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Email");
        redactedObject.put("method", "REMOVAL"); // Uppercase - should still match

        // This should pass validation due to case-insensitive comparison
        validate();
    }

    @Test
    public void testValidateMethodProperty_WithRemovalWhitespace_ShouldPass() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test method with whitespace - should be trimmed and matched

        // Remove email to trigger redaction validation path
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() > 0 && "email".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }

        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Email");
        redactedObject.put("method", "  removal  "); // With whitespace

        // This should pass validation due to trim() call
        validate();
    }

    @Test
    public void testValidateMethodProperty_WithInvalidMethod_ShouldTrigger65403() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test when method is not "removal" - should trigger error -65403

        // Remove email to trigger redaction validation path
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() > 0 && "email".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }

        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Email");
        redactedObject.put("method", "invalidMethod"); // Invalid method value

        // Expected: Should trigger -65403 because method is not "removal"
        String expectedValue = "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"invalidMethod\",\"name\":{\"type\":\"Registrant Email\"},\"prePath\":\"$.test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
        validate(-65403, expectedValue, "Registrant Email redaction method must be removal if present");
    }

    @Test
    public void testValidateMethodProperty_WithNonStringMethod_ShouldPass() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test when method exists but is not a string - should pass (skip validation)

        // Remove email to trigger redaction validation path
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() > 0 && "email".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }

        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Email");
        redactedObject.put("method", 456); // Non-string value

        // This should not match the instanceof String check and pass without validation
        validate();
    }

    @Test
    public void testValidateMethodProperty_WithMissingMethod_ShouldPass() {
        when(config.isGtldRegistry()).thenReturn(true);
        // Test when method property is missing entirely - should trigger exception handling and pass

        // Remove email to trigger redaction validation path
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        for (int i = vcardArray.length() - 1; i >= 0; i--) {
            JSONArray property = vcardArray.getJSONArray(i);
            if (property.length() > 0 && "email".equals(property.getString(0))) {
                vcardArray.remove(i);
            }
        }

        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.getJSONObject("name").put("type", "Registrant Email");
        // Don't set method property - this should trigger exception handling
        redactedObject.remove("method"); // Ensure method is not present

        // This should trigger the catch block and pass (method is optional)
        validate();
    }

}
