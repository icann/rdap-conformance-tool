package org.icann.rdapconformance.validator.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefinitionWarningTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testConstructor_ValidParameters() {
        DefinitionWarning warning = new DefinitionWarning(3001, "Test warning message");
        
        assertThat(warning.getCode()).isEqualTo(3001);
        assertThat(warning.getNotes()).isEqualTo("Test warning message");
        assertThat(warning).isInstanceOf(DefinitionAlert.class);
    }
    
    @Test
    public void testConstructor_NullNotes() {
        DefinitionWarning warning = new DefinitionWarning(3002, null);
        
        assertThat(warning.getCode()).isEqualTo(3002);
        assertThat(warning.getNotes()).isNull();
    }
    
    @Test
    public void testJsonDeserialization_ValidJson() throws Exception {
        String json = "{\"code\": 4001, \"notes\": \"JSON test warning\"}";
        
        DefinitionWarning warning = objectMapper.readValue(json, DefinitionWarning.class);
        
        assertThat(warning.getCode()).isEqualTo(4001);
        assertThat(warning.getNotes()).isEqualTo("JSON test warning");
    }
    
    @Test
    public void testJsonDeserialization_NegativeCode() throws Exception {
        String json = "{\"code\": -2186, \"notes\": \"Negative code warning\"}";
        
        DefinitionWarning warning = objectMapper.readValue(json, DefinitionWarning.class);
        
        assertThat(warning.getCode()).isEqualTo(-2186);
        assertThat(warning.getNotes()).isEqualTo("Negative code warning");
    }
    
    @Test
    public void testJsonDeserialization_WithoutNotes() throws Exception {
        String json = "{\"code\": 4002}";
        
        DefinitionWarning warning = objectMapper.readValue(json, DefinitionWarning.class);
        
        assertThat(warning.getCode()).isEqualTo(4002);
        assertThat(warning.getNotes()).isNull();
    }
    
    @Test
    public void testJsonDeserialization_EmptyNotes() throws Exception {
        String json = "{\"code\": 4003, \"notes\": \"\"}";
        
        DefinitionWarning warning = objectMapper.readValue(json, DefinitionWarning.class);
        
        assertThat(warning.getCode()).isEqualTo(4003);
        assertThat(warning.getNotes()).isEmpty();
    }
    
    @Test
    public void testJsonDeserialization_NullNotes() throws Exception {
        String json = "{\"code\": 4004, \"notes\": null}";
        
        DefinitionWarning warning = objectMapper.readValue(json, DefinitionWarning.class);
        
        assertThat(warning.getCode()).isEqualTo(4004);
        assertThat(warning.getNotes()).isNull();
    }
    
    @Test
    public void testJsonSerialization() throws Exception {
        DefinitionWarning warning = new DefinitionWarning(4005, "Serialization test");
        
        String json = objectMapper.writeValueAsString(warning);
        
        assertThat(json).contains("\"code\":4005");
        assertThat(json).contains("\"notes\":\"Serialization test\"");
    }
    
    @Test
    public void testJsonDeserialization_MissingRequiredCode() throws Exception {
        String json = "{\"notes\": \"Missing code warning\"}";
        
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> objectMapper.readValue(json, DefinitionWarning.class))
            .isInstanceOf(Exception.class);
    }
    
    @Test
    public void testInheritance() {
        DefinitionWarning warning = new DefinitionWarning(4006, "Inheritance test");
        
        assertThat(warning).isInstanceOf(DefinitionAlert.class);
        assertThat(warning.getClass().getSuperclass()).isEqualTo(DefinitionAlert.class);
    }
    
    @Test
    public void testJsonDeserialization_ExtraFields_ThrowsException() throws Exception {
        String json = "{\"code\": 4007, \"notes\": \"Extra fields test\", \"severity\": \"low\"}";
        
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> objectMapper.readValue(json, DefinitionWarning.class))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Unrecognized field \"severity\"");
    }
    
    @Test
    public void testNotesOptional_DifferenceFromError() throws Exception {
        String jsonWarning = "{\"code\": 4008}";
        DefinitionWarning warning = objectMapper.readValue(jsonWarning, DefinitionWarning.class);
        
        assertThat(warning.getCode()).isEqualTo(4008);
        assertThat(warning.getNotes()).isNull();
    }
}