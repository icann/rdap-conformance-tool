package org.icann.rdapconformance.validator.configuration;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefinitionAlertTest {

    @Test
    public void testConstructor_ValidParameters() {
        DefinitionError alert = new DefinitionError(1001, "Test error message");
        
        assertThat(alert.getCode()).isEqualTo(1001);
        assertThat(alert.getNotes()).isEqualTo("Test error message");
    }
    
    @Test
    public void testConstructor_ZeroCode() {
        DefinitionError alert = new DefinitionError(0, "Zero code message");
        
        assertThat(alert.getCode()).isEqualTo(0);
        assertThat(alert.getNotes()).isEqualTo("Zero code message");
    }
    
    @Test
    public void testConstructor_NegativeCode() {
        DefinitionError alert = new DefinitionError(-2001, "Negative code message");
        
        assertThat(alert.getCode()).isEqualTo(-2001);
        assertThat(alert.getNotes()).isEqualTo("Negative code message");
    }
    
    @Test
    public void testConstructor_EmptyNotes() {
        DefinitionError alert = new DefinitionError(1002, "");
        
        assertThat(alert.getCode()).isEqualTo(1002);
        assertThat(alert.getNotes()).isEmpty();
    }
    
    @Test
    public void testConstructor_NullNotes() {
        DefinitionError alert = new DefinitionError(1003, null);
        
        assertThat(alert.getCode()).isEqualTo(1003);
        assertThat(alert.getNotes()).isNull();
    }
    
    @Test
    public void testGetCode_Immutable() {
        DefinitionError alert = new DefinitionError(1004, "Test message");
        int originalCode = alert.getCode();
        
        assertThat(alert.getCode()).isEqualTo(originalCode);
        assertThat(alert.getCode()).isEqualTo(1004);
    }
    
    @Test
    public void testGetNotes_Immutable() {
        String originalNotes = "Original test message";
        DefinitionError alert = new DefinitionError(1005, originalNotes);
        
        assertThat(alert.getNotes()).isEqualTo(originalNotes);
        assertThat(alert.getNotes()).isSameAs(originalNotes);
    }
    
    @Test
    public void testConstructor_LargeCode() {
        DefinitionError alert = new DefinitionError(Integer.MAX_VALUE, "Max value code");
        
        assertThat(alert.getCode()).isEqualTo(Integer.MAX_VALUE);
        assertThat(alert.getNotes()).isEqualTo("Max value code");
    }
    
    @Test
    public void testConstructor_SmallCode() {
        DefinitionError alert = new DefinitionError(Integer.MIN_VALUE, "Min value code");
        
        assertThat(alert.getCode()).isEqualTo(Integer.MIN_VALUE);
        assertThat(alert.getNotes()).isEqualTo("Min value code");
    }
    
    @Test
    public void testConstructor_LongNotes() {
        String longNotes = "This is a very long notes string that contains lots of text to test how the DefinitionAlert handles long note messages and ensures they are stored correctly without any truncation or modification";
        DefinitionError alert = new DefinitionError(1006, longNotes);
        
        assertThat(alert.getCode()).isEqualTo(1006);
        assertThat(alert.getNotes()).isEqualTo(longNotes);
        assertThat(alert.getNotes()).hasSizeGreaterThan(100);
    }
}