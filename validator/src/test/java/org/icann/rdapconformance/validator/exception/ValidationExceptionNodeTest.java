package org.icann.rdapconformance.validator.exception;

import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValidationExceptionNodeTest {

    private ValidationException mockException;
    private Schema mockSchema;
    private ValidationExceptionNode parentNode;
    private ValidationExceptionNode childNode;

    @BeforeMethod
    public void setUp() {
        mockException = mock(ValidationException.class);
        mockSchema = mock(ObjectSchema.class);
        
        when(mockException.getViolatedSchema()).thenReturn(mockSchema);
        when(mockException.getPointerToViolation()).thenReturn("#/test/path");
        when(mockException.getMessage()).thenReturn("Test validation error");
        when(mockException.getKeyword()).thenReturn("required");
        when(mockException.getSchemaLocation()).thenReturn("classpath://json-schema/test-schema.json");
        when(mockException.getCausingExceptions()).thenReturn(Collections.emptyList());
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("errorCode", -12345);
        properties.put("errorMsg", "Custom error message");
        when(mockSchema.getUnprocessedProperties()).thenReturn(properties);
        
        parentNode = new ValidationExceptionNode(null, mockException);
    }

    @Test
    public void testConstructor() {
        ValidationExceptionNode node = new ValidationExceptionNode(parentNode, mockException);
        
        assertThat(node.getParentException()).isEqualTo(parentNode);
    }

    @Test
    public void testGetParentException() {
        ValidationExceptionNode node = new ValidationExceptionNode(parentNode, mockException);
        
        assertThat(node.getParentException()).isEqualTo(parentNode);
    }

    @Test
    public void testGetChildrenEmpty() {
        List<ValidationExceptionNode> children = parentNode.getChildren();
        
        assertThat(children).isEmpty();
    }

    @Test
    public void testGetChildrenWithCausingExceptions() {
        ValidationException childException = mock(ValidationException.class);
        when(childException.getViolatedSchema()).thenReturn(mockSchema);
        when(childException.getCausingExceptions()).thenReturn(Collections.emptyList());
        
        when(mockException.getCausingExceptions()).thenReturn(List.of(childException));
        
        List<ValidationExceptionNode> children = parentNode.getChildren();
        
        assertThat(children).hasSize(1);
        assertThat(children.get(0).getParentException()).isEqualTo(parentNode);
    }

    @Test
    public void testGetAllExceptionsWithNoChildren() {
        List<ValidationExceptionNode> allExceptions = parentNode.getAllExceptions();
        
        assertThat(allExceptions).hasSize(1);
        assertThat(allExceptions.get(0)).isEqualTo(parentNode);
    }

    @Test
    public void testGetAllExceptionsRecursively() {
        ValidationException childException = mock(ValidationException.class);
        ValidationException grandChildException = mock(ValidationException.class);
        
        when(childException.getViolatedSchema()).thenReturn(mockSchema);
        when(childException.getCausingExceptions()).thenReturn(List.of(grandChildException));
        
        when(grandChildException.getViolatedSchema()).thenReturn(mockSchema);
        when(grandChildException.getCausingExceptions()).thenReturn(Collections.emptyList());
        
        when(mockException.getCausingExceptions()).thenReturn(List.of(childException));
        
        List<ValidationExceptionNode> allExceptions = parentNode.getAllExceptions();
        
        assertThat(allExceptions).hasSize(1); // Only the leaf node (grandchild) should be included
    }

    @Test
    public void testGetPointerToViolation() {
        assertThat(parentNode.getPointerToViolation()).isEqualTo("#/test/path");
    }

    @Test
    public void testGetViolatedSchema() {
        assertThat(parentNode.getViolatedSchema()).isEqualTo(mockSchema);
    }

    @Test
    public void testGetSchemaLocation() {
        assertThat(parentNode.getSchemaLocation()).isEqualTo("test-schema.json");
    }

    @Test
    public void testGetSchemaLocationWhenNull() {
        when(mockException.getSchemaLocation()).thenReturn(null);
        
        assertThat(parentNode.getSchemaLocation()).isEqualTo("the associated");
    }

    @Test
    public void testGetMessage() {
        assertThat(parentNode.getMessage()).isEqualTo("Test validation error");
    }

    @Test
    public void testGetKeyword() {
        assertThat(parentNode.getKeyword()).isEqualTo("required");
    }

    @Test
    public void testGetPropertyFromViolatedSchema() {
        Object errorCode = parentNode.getPropertyFromViolatedSchema("errorCode");
        
        assertThat(errorCode).isEqualTo(-12345);
    }

    @Test
    public void testGetPropertyFromViolatedSchemaWithContainerSchema() {
        // Create a parent with ArraySchema (container schema)
        ValidationException parentException = mock(ValidationException.class);
        ArraySchema parentSchema = mock(ArraySchema.class);
        
        Map<String, Object> parentProperties = new HashMap<>();
        parentProperties.put("errorCode", -99999);
        
        when(parentException.getViolatedSchema()).thenReturn(parentSchema);
        when(parentSchema.getUnprocessedProperties()).thenReturn(parentProperties);
        when(parentException.getCausingExceptions()).thenReturn(Collections.emptyList());
        
        ValidationExceptionNode parentWithContainer = new ValidationExceptionNode(null, parentException);
        
        // Child schema doesn't have the property, so should traverse up
        Map<String, Object> childProperties = new HashMap<>();
        when(mockSchema.getUnprocessedProperties()).thenReturn(childProperties);
        
        ValidationExceptionNode childWithContainerParent = new ValidationExceptionNode(parentWithContainer, mockException);
        
        Object errorCode = childWithContainerParent.getPropertyFromViolatedSchema("errorCode");
        
        assertThat(errorCode).isEqualTo(-99999);
    }

    @Test
    public void testGetErrorCodeFromViolatedSchema() {
        int errorCode = parentNode.getErrorCodeFromViolatedSchema();
        
        assertThat(errorCode).isEqualTo(-12345);
    }

    @Test
    public void testGetMessageWithCustomMessage() {
        String message = parentNode.getMessage("Default message");
        
        assertThat(message).isEqualTo("Custom error message");
    }

    @Test
    public void testGetMessageWithDefaultWhenNoCustomMessage() {
        Map<String, Object> propertiesWithoutErrorMsg = new HashMap<>();
        propertiesWithoutErrorMsg.put("errorCode", -12345);
        when(mockSchema.getUnprocessedProperties()).thenReturn(propertiesWithoutErrorMsg);
        
        String message = parentNode.getMessage("Default message");
        
        assertThat(message).isEqualTo("Default message");
    }

    @Test
    public void testGetMessageWithExceptionHandling() {
        // Force an exception by making getPropertyFromViolatedSchema throw
        Map<String, Object> invalidProperties = new HashMap<>();
        when(mockSchema.getUnprocessedProperties()).thenReturn(invalidProperties);
        
        // This should catch the exception and return the default message
        String message = parentNode.getMessage("Default message");
        
        assertThat(message).isEqualTo("Default message");
    }

    @Test
    public void testContainerSchemaTypes() {
        // Test with CombinedSchema
        ValidationException combinedException = mock(ValidationException.class);
        CombinedSchema combinedSchema = mock(CombinedSchema.class);
        when(combinedException.getViolatedSchema()).thenReturn(combinedSchema);
        when(combinedException.getCausingExceptions()).thenReturn(Collections.emptyList());
        when(combinedSchema.getUnprocessedProperties()).thenReturn(new HashMap<>());
        
        ValidationExceptionNode combinedNode = new ValidationExceptionNode(null, combinedException);
        
        // Test with ReferenceSchema
        ValidationException referenceException = mock(ValidationException.class);
        ReferenceSchema referenceSchema = mock(ReferenceSchema.class);
        when(referenceException.getViolatedSchema()).thenReturn(referenceSchema);
        when(referenceException.getCausingExceptions()).thenReturn(Collections.emptyList());
        when(referenceSchema.getUnprocessedProperties()).thenReturn(new HashMap<>());
        
        ValidationExceptionNode referenceNode = new ValidationExceptionNode(null, referenceException);
        
        // Both should be created successfully
        assertThat(combinedNode).isNotNull();
        assertThat(referenceNode).isNotNull();
    }
}