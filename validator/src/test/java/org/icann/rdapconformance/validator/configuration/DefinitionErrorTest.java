package org.icann.rdapconformance.validator.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefinitionErrorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testConstructor_ValidParameters() {
        DefinitionError error = new DefinitionError(1001, "Test error message");
        
        assertThat(error.getCode()).isEqualTo(1001);
        assertThat(error.getNotes()).isEqualTo("Test error message");
        assertThat(error).isInstanceOf(DefinitionAlert.class);
    }
    
    @Test
    public void testJsonDeserialization_ValidJson() throws Exception {
        String json = "{\"code\": 2001, \"notes\": \"JSON test error\"}";
        
        DefinitionError error = objectMapper.readValue(json, DefinitionError.class);
        
        assertThat(error.getCode()).isEqualTo(2001);
        assertThat(error.getNotes()).isEqualTo("JSON test error");
    }
    
    @Test
    public void testJsonDeserialization_NegativeCode() throws Exception {
        String json = "{\"code\": -1102, \"notes\": \"Negative code error\"}";
        
        DefinitionError error = objectMapper.readValue(json, DefinitionError.class);
        
        assertThat(error.getCode()).isEqualTo(-1102);
        assertThat(error.getNotes()).isEqualTo("Negative code error");
    }
    
    @Test
    public void testJsonDeserialization_EmptyNotes() throws Exception {
        String json = "{\"code\": 2002, \"notes\": \"\"}";
        
        DefinitionError error = objectMapper.readValue(json, DefinitionError.class);
        
        assertThat(error.getCode()).isEqualTo(2002);
        assertThat(error.getNotes()).isEmpty();
    }
    
    @Test
    public void testJsonSerialization() throws Exception {
        DefinitionError error = new DefinitionError(2003, "Serialization test");
        
        String json = objectMapper.writeValueAsString(error);
        
        assertThat(json).contains("\"code\":2003");
        assertThat(json).contains("\"notes\":\"Serialization test\"");
    }
    
    @Test
    public void testJsonDeserialization_MissingRequiredCode() throws Exception {
        String json = "{\"notes\": \"Missing code error\"}";
        
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> objectMapper.readValue(json, DefinitionError.class))
            .isInstanceOf(Exception.class);
    }
    
    @Test
    public void testJsonDeserialization_MissingRequiredNotes() throws Exception {
        String json = "{\"code\": 2004}";
        
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> objectMapper.readValue(json, DefinitionError.class))
            .isInstanceOf(Exception.class);
    }
    
    @Test
    public void testJsonDeserialization_NullNotes() throws Exception {
        String json = "{\"code\": 2005, \"notes\": null}";
        
        DefinitionError error = objectMapper.readValue(json, DefinitionError.class);
        
        assertThat(error.getCode()).isEqualTo(2005);
        assertThat(error.getNotes()).isNull();
    }
    
    @Test
    public void testJsonDeserialization_ExtraFields_ThrowsException() throws Exception {
        String json = "{\"code\": 2006, \"notes\": \"Extra fields test\", \"extra\": \"ignored\"}";
        
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> objectMapper.readValue(json, DefinitionError.class))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Unrecognized field \"extra\"");
    }
    
    @Test
    public void testInheritance() {
        DefinitionError error = new DefinitionError(2007, "Inheritance test");
        
        assertThat(error).isInstanceOf(DefinitionAlert.class);
        assertThat(error.getClass().getSuperclass()).isEqualTo(DefinitionAlert.class);
    }
}