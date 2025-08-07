package org.icann.rdapconformance.validator;

import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class JpathUtilTest {

    private JpathUtil jpathUtil;
    private JSONObject testJsonObject;
    private String testJsonString;

    @BeforeMethod
    public void setUp() {
        jpathUtil = new JpathUtil();
        
        testJsonObject = new JSONObject();
        testJsonObject.put("name", "example.com");
        testJsonObject.put("status", new String[]{"active", "ok"});
        
        JSONObject contact = new JSONObject();
        contact.put("email", "admin@example.com");
        testJsonObject.put("contact", contact);
        
        testJsonString = testJsonObject.toString();
    }
    
    @Test
    public void testConstructor() {
        JpathUtil util = new JpathUtil();
        
        assertThat(util).isNotNull();
    }
    
    @Test
    public void testExists_JSONObject_ExistingPath() {
        boolean exists = jpathUtil.exists(testJsonObject, "$.name");
        
        assertThat(exists).isTrue();
    }
    
    @Test
    public void testExists_JSONObject_NonExistingPath() {
        boolean exists = jpathUtil.exists(testJsonObject, "$.nonexistent");
        
        assertThat(exists).isFalse();
    }
    
    @Test
    public void testExists_String_ExistingPath() {
        boolean exists = jpathUtil.exists(testJsonString, "$.name");
        
        assertThat(exists).isTrue();
    }
    
    @Test
    public void testExists_String_NonExistingPath() {
        boolean exists = jpathUtil.exists(testJsonString, "$.nonexistent");
        
        assertThat(exists).isFalse();
    }
    
    @Test
    public void testExists_NestedPath() {
        boolean exists = jpathUtil.exists(testJsonObject, "$.contact.email");
        
        assertThat(exists).isTrue();
    }
    
    @Test
    public void testExists_ArrayPath() {
        boolean exists = jpathUtil.exists(testJsonObject, "$.status[0]");
        
        assertThat(exists).isTrue();
    }
    
    @Test
    public void testGetPointerFromJPath_JSONObject() {
        Set<String> pointers = jpathUtil.getPointerFromJPath(testJsonObject, "$.name");
        
        assertThat(pointers).isNotEmpty();
        assertThat(pointers).contains("#/name");
    }
    
    @Test
    public void testGetPointerFromJPath_String() {
        Set<String> pointers = jpathUtil.getPointerFromJPath(testJsonString, "$.name");
        
        assertThat(pointers).isNotEmpty();
        assertThat(pointers).contains("#/name");
    }
    
    @Test
    public void testGetPointerFromJPath_NestedPath() {
        Set<String> pointers = jpathUtil.getPointerFromJPath(testJsonObject, "$.contact.email");
        
        assertThat(pointers).isNotEmpty();
        assertThat(pointers).contains("#/contact/email");
    }
    
    @Test
    public void testGetPointerFromJPath_ArrayPath() {
        Set<String> pointers = jpathUtil.getPointerFromJPath(testJsonObject, "$.status[*]");
        
        assertThat(pointers).isNotEmpty();
        assertThat(pointers).contains("#/status/0");
        assertThat(pointers).contains("#/status/1");
    }
    
    @Test
    public void testGetPointerFromJPath_NonExistingPath() {
        Set<String> pointers = jpathUtil.getPointerFromJPath(testJsonObject, "$.nonexistent");
        
        assertThat(pointers).isEmpty();
    }
    
    @Test
    public void testIsValidJsonPath_ValidPaths() {
        assertThat(jpathUtil.isValidJsonPath("$.name")).isTrue();
        assertThat(jpathUtil.isValidJsonPath("$.contact.email")).isTrue();
        assertThat(jpathUtil.isValidJsonPath("$.status[0]")).isTrue();
        assertThat(jpathUtil.isValidJsonPath("$.status[*]")).isTrue();
        assertThat(jpathUtil.isValidJsonPath("$..name")).isTrue();
    }
    
    @Test
    public void testIsValidJsonPath_InvalidPaths() {
        assertThat(jpathUtil.isValidJsonPath("")).isFalse();
        assertThat(jpathUtil.isValidJsonPath("$.[")).isFalse();
        assertThat(jpathUtil.isValidJsonPath("$..[")).isFalse();
    }
    
    @Test
    public void testIsValidJsonPath_NullPath() {
        assertThat(jpathUtil.isValidJsonPath(null)).isFalse();
    }
    
    @Test
    public void testExists_InvalidJson_ThrowsException() {
        String invalidJson = "{ invalid json }";
        
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> jpathUtil.exists(invalidJson, "$.name"))
            .isInstanceOf(Exception.class);
    }
    
    @Test
    public void testGetPointerFromJPath_InvalidJson_ThrowsException() {
        String invalidJson = "{ invalid json }";
        
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> jpathUtil.getPointerFromJPath(invalidJson, "$.name"))
            .isInstanceOf(Exception.class);
    }
    
    @Test
    public void testExists_EmptyJson() {
        boolean exists = jpathUtil.exists("{}", "$.name");
        
        assertThat(exists).isFalse();
    }
    
    @Test  
    public void testGetPointerFromJPath_EmptyJson() {
        Set<String> pointers = jpathUtil.getPointerFromJPath("{}", "$.name");
        
        assertThat(pointers).isEmpty();
    }
    
    @Test
    public void testExists_ComplexJsonPath() {
        JSONObject complexJson = new JSONObject();
        complexJson.put("items", new JSONObject[]{
            new JSONObject().put("name", "item1"),
            new JSONObject().put("name", "item2")
        });
        
        boolean exists = jpathUtil.exists(complexJson, "$.items[?(@.name == 'item1')]");
        
        assertThat(exists).isTrue();
    }
}