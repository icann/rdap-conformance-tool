package org.icann.rdapconformance.validator.workflow.profile;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

public class ProfileJsonValidationTest {

    private RDAPValidatorResults mockResults;
    private String validJsonResponse;
    private String invalidJsonResponse;

    @BeforeMethod
    public void setUp() {
        mockResults = mock(RDAPValidatorResults.class);
        
        validJsonResponse = "{\n" +
            "  \"objectClassName\": \"domain\",\n" +
            "  \"ldhName\": \"example.com\",\n" +
            "  \"unicodeName\": \"example.com\",\n" +
            "  \"status\": [\"active\"],\n" +
            "  \"entities\": [\n" +
            "    {\n" +
            "      \"objectClassName\": \"entity\",\n" +
            "      \"handle\": \"REGISTRANT123\",\n" +
            "      \"roles\": [\"registrant\"]\n" +
            "    }\n" +
            "  ],\n" +
            "  \"nameservers\": [\n" +
            "    {\n" +
            "      \"objectClassName\": \"nameserver\",\n" +
            "      \"ldhName\": \"ns1.example.com\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

        invalidJsonResponse = "{ invalid json }";
    }

    @Test
    public void testConstructor_ValidJsonResponse() {
        TestProfileJsonValidation validation = new TestProfileJsonValidation(validJsonResponse, mockResults);
        
        assertThat(validation).isNotNull();
        assertThat(validation.jsonObject).isNotNull();
        assertThat(validation.jsonObject.getString("objectClassName")).isEqualTo("domain");
    }

    @Test
    public void testConstructor_InvalidJsonResponse_ThrowsException() {
        assertThatThrownBy(() -> new TestProfileJsonValidation(invalidJsonResponse, mockResults))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void testConstructor_NullJsonResponse() {
        assertThatThrownBy(() -> new TestProfileJsonValidation(null, mockResults))
            .isInstanceOf(org.json.JSONException.class);
    }

    @Test
    public void testConstructor_EmptyJsonResponse() {
        assertThatThrownBy(() -> new TestProfileJsonValidation("", mockResults))
            .isInstanceOf(org.json.JSONException.class);
    }

    @Test
    public void testExists_ValidPath_ReturnsTrue() {
        TestProfileJsonValidation validation = new TestProfileJsonValidation(validJsonResponse, mockResults);
        
        boolean exists = validation.exists("$.objectClassName");
        
        assertThat(exists).isTrue();
    }

    @Test
    public void testExists_InvalidPath_ReturnsFalse() {
        TestProfileJsonValidation validation = new TestProfileJsonValidation(validJsonResponse, mockResults);
        
        boolean exists = validation.exists("$.nonExistentField");
        
        assertThat(exists).isFalse();
    }

    @Test
    public void testExists_NestedPath_ReturnsTrue() {
        TestProfileJsonValidation validation = new TestProfileJsonValidation(validJsonResponse, mockResults);
        
        boolean exists = validation.exists("$.entities[0].handle");
        
        assertThat(exists).isTrue();
    }

    @Test
    public void testExists_NestedInvalidPath_ReturnsFalse() {
        TestProfileJsonValidation validation = new TestProfileJsonValidation(validJsonResponse, mockResults);
        
        boolean exists = validation.exists("$.entities[0].nonExistentField");
        
        assertThat(exists).isFalse();
    }

    @Test
    public void testGetPointerFromJPath_ValidPath() {
        TestProfileJsonValidation validation = new TestProfileJsonValidation(validJsonResponse, mockResults);
        
        Set<String> pointers = validation.getPointerFromJPath("$.objectClassName");
        
        assertThat(pointers).isNotEmpty();
        assertThat(pointers).contains("#/objectClassName");
    }

    @Test
    public void testGetPointerFromJPath_ArrayPath() {
        TestProfileJsonValidation validation = new TestProfileJsonValidation(validJsonResponse, mockResults);
        
        Set<String> pointers = validation.getPointerFromJPath("$.entities[*].handle");
        
        assertThat(pointers).isNotEmpty();
        assertThat(pointers).contains("#/entities/0/handle");
    }

    @Test
    public void testGetPointerFromJPath_InvalidPath() {
        TestProfileJsonValidation validation = new TestProfileJsonValidation(validJsonResponse, mockResults);
        
        Set<String> pointers = validation.getPointerFromJPath("$.nonExistentField");
        
        assertThat(pointers).isEmpty();
    }

    @Test
    public void testGetPointerFromJPath_WithSpecificJsonObject() {
        TestProfileJsonValidation validation = new TestProfileJsonValidation(validJsonResponse, mockResults);
        
        Set<String> pointers = validation.getPointerFromJPath(validation.jsonObject, "$.ldhName");
        
        assertThat(pointers).isNotEmpty();
        assertThat(pointers).contains("#/ldhName");
    }

    @Test
    public void testGetResultValue_SinglePointer() {
        TestProfileJsonValidation validation = new TestProfileJsonValidation(validJsonResponse, mockResults);
        
        String result = validation.getResultValue("#/objectClassName");
        
        assertThat(result).isEqualTo("#/objectClassName:domain");
    }

    @Test
    public void testGetResultValue_InvalidPointer() {
        TestProfileJsonValidation validation = new TestProfileJsonValidation(validJsonResponse, mockResults);
        
        String result = validation.getResultValue("#/nonExistentField");
        
        // Invalid pointers return the pointer with null value
        assertThat(result).isEqualTo("#/nonExistentField:null");
    }

    @Test
    public void testGetResultValue_SetOfPointers() {
        TestProfileJsonValidation validation = new TestProfileJsonValidation(validJsonResponse, mockResults);
        
        Set<String> pointers = Set.of("#/objectClassName", "#/ldhName");
        String result = validation.getResultValue(pointers);
        
        assertThat(result).contains("#/ldhName:example.com");
        assertThat(result).contains("#/objectClassName:domain");
        assertThat(result).contains(", "); // Should be comma-separated
    }

    @Test
    public void testGetResultValue_EmptySetOfPointers() {
        TestProfileJsonValidation validation = new TestProfileJsonValidation(validJsonResponse, mockResults);
        
        Set<String> emptyPointers = Set.of();
        String result = validation.getResultValue(emptyPointers);
        
        assertThat(result).isEmpty();
    }

    @Test
    public void testIsValidJsonPath_ValidPath() {
        TestProfileJsonValidation validation = new TestProfileJsonValidation(validJsonResponse, mockResults);
        
        boolean isValid = validation.isValidJsonPath("$.objectClassName");
        
        assertThat(isValid).isTrue();
    }

    @Test
    public void testIsValidJsonPath_InvalidPath() {
        TestProfileJsonValidation validation = new TestProfileJsonValidation(validJsonResponse, mockResults);
        
        boolean isValid = validation.isValidJsonPath("invalid path");
        
        assertThat(isValid).isFalse();
    }

    @Test
    public void testIsValidJsonPath_NullPath() {
        TestProfileJsonValidation validation = new TestProfileJsonValidation(validJsonResponse, mockResults);
        
        boolean isValid = validation.isValidJsonPath(null);
        
        assertThat(isValid).isFalse();
    }

    @Test
    public void testIsValidJsonPath_EmptyPath() {
        TestProfileJsonValidation validation = new TestProfileJsonValidation(validJsonResponse, mockResults);
        
        boolean isValid = validation.isValidJsonPath("");
        
        assertThat(isValid).isFalse();
    }

    @Test
    public void testComplexJsonPath_MultipleArrayElements() {
        TestProfileJsonValidation validation = new TestProfileJsonValidation(validJsonResponse, mockResults);
        
        boolean exists = validation.exists("$.entities[*].roles[*]");
        
        assertThat(exists).isTrue();
    }

    @Test
    public void testJsonObjectAccessible() {
        TestProfileJsonValidation validation = new TestProfileJsonValidation(validJsonResponse, mockResults);
        
        // Verify that the jsonObject field is accessible and properly initialized
        assertThat(validation.jsonObject).isNotNull();
        assertThat(validation.jsonObject.has("objectClassName")).isTrue();
        assertThat(validation.jsonObject.getString("objectClassName")).isEqualTo("domain");
    }

    // Test implementation of ProfileJsonValidation for testing purposes
    private static class TestProfileJsonValidation extends ProfileJsonValidation {

        public TestProfileJsonValidation(String rdapResponse, RDAPValidatorResults results) {
            super(rdapResponse, results);
        }

        public String getGroupName() {
            return "TestJsonValidation";
        }

        protected boolean doValidate() throws Exception {
            return true;
        }
    }
}