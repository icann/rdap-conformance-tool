package org.icann.rdapconformance.validator.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationFileTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testConstructor_MinimalRequiredFields() {
        ConfigurationFile config = new ConfigurationFile(
            "Test Profile 1.0", null, null, null, null,
            false, false, false, false, false);
        
        assertThat(config.getDefinitionIdentifier()).isEqualTo("Test Profile 1.0");
        assertThat(config.getDefinitionIgnore()).isEmpty();
        assertThat(config.getDefinitionNotes()).isEmpty();
        assertThat(config.isGtldRegistrar()).isFalse();
        assertThat(config.isGtldRegistry()).isFalse();
        assertThat(config.isThinRegistry()).isFalse();
        assertThat(config.isRdapProfileFebruary2019()).isFalse();
        assertThat(config.isRdapProfileFebruary2024()).isFalse();
    }
    
    @Test
    public void testConstructor_AllFields() {
        List<DefinitionError> errors = Arrays.asList(
            new DefinitionError(1001, "Error 1"),
            new DefinitionError(1002, "Error 2")
        );
        List<DefinitionWarning> warnings = Arrays.asList(
            new DefinitionWarning(2001, "Warning 1"),
            new DefinitionWarning(2002, "Warning 2")
        );
        List<Integer> ignores = Arrays.asList(3001, 3002, 3003);
        List<String> notes = Arrays.asList("Note 1", "Note 2");
        
        ConfigurationFile config = new ConfigurationFile(
            "Full Test Profile 1.0", errors, warnings, ignores, notes,
            true, false, true, false, true);
        
        assertThat(config.getDefinitionIdentifier()).isEqualTo("Full Test Profile 1.0");
        assertThat(config.getDefinitionIgnore()).containsExactly(3001, 3002, 3003);
        assertThat(config.getDefinitionNotes()).containsExactly("Note 1", "Note 2");
        assertThat(config.isGtldRegistrar()).isTrue();
        assertThat(config.isGtldRegistry()).isFalse();
        assertThat(config.isThinRegistry()).isTrue();
        assertThat(config.isRdapProfileFebruary2019()).isFalse();
        assertThat(config.isRdapProfileFebruary2024()).isTrue();
    }
    
    @Test
    public void testIsError_WithErrorCodes() {
        List<DefinitionError> errors = Arrays.asList(
            new DefinitionError(1001, "Error 1"),
            new DefinitionError(1002, "Error 2")
        );
        
        ConfigurationFile config = new ConfigurationFile(
            "Error Test Profile", errors, null, null, null,
            false, false, false, false, false);
        
        assertThat(config.isError(1001)).isTrue();
        assertThat(config.isError(1002)).isTrue();
        assertThat(config.isError(1003)).isFalse();
        assertThat(config.isError(2001)).isFalse();
    }
    
    @Test
    public void testIsWarning_WithWarningCodes() {
        List<DefinitionWarning> warnings = Arrays.asList(
            new DefinitionWarning(2001, "Warning 1"),
            new DefinitionWarning(2002, "Warning 2")
        );
        
        ConfigurationFile config = new ConfigurationFile(
            "Warning Test Profile", null, warnings, null, null,
            false, false, false, false, false);
        
        assertThat(config.isWarning(2001)).isTrue();
        assertThat(config.isWarning(2002)).isTrue();
        assertThat(config.isWarning(2003)).isFalse();
        assertThat(config.isWarning(1001)).isFalse();
    }
    
    @Test
    public void testGetAlertNotes_ForErrors() {
        List<DefinitionError> errors = Arrays.asList(
            new DefinitionError(1001, "Specific error message"),
            new DefinitionError(1002, "Another error message")
        );
        
        ConfigurationFile config = new ConfigurationFile(
            "Alert Notes Test", errors, null, null, null,
            false, false, false, false, false);
        
        assertThat(config.getAlertNotes(1001)).isEqualTo("Specific error message");
        assertThat(config.getAlertNotes(1002)).isEqualTo("Another error message");
        assertThat(config.getAlertNotes(1003)).isEmpty();
    }
    
    @Test
    public void testGetAlertNotes_ForWarnings() {
        List<DefinitionWarning> warnings = Arrays.asList(
            new DefinitionWarning(2001, "Specific warning message"),
            new DefinitionWarning(2002, "Another warning message")
        );
        
        ConfigurationFile config = new ConfigurationFile(
            "Alert Notes Test", null, warnings, null, null,
            false, false, false, false, false);
        
        assertThat(config.getAlertNotes(2001)).isEqualTo("Specific warning message");
        assertThat(config.getAlertNotes(2002)).isEqualTo("Another warning message");
        assertThat(config.getAlertNotes(2003)).isEmpty();
    }
    
    @Test
    public void testGetAlertNotes_NonExistentCode() {
        ConfigurationFile config = new ConfigurationFile(
            "Empty Alert Test", null, null, null, null,
            false, false, false, false, false);
        
        assertThat(config.getAlertNotes(9999)).isEmpty();
    }
    
    @Test
    public void testEmptyListIfNull_NullLists() {
        ConfigurationFile config = new ConfigurationFile(
            "Null Lists Test", null, null, null, null,
            false, false, false, false, false);
        
        assertThat(config.getDefinitionIgnore()).isNotNull().isEmpty();
        assertThat(config.getDefinitionNotes()).isNotNull().isEmpty();
    }
    
    @Test
    public void testEmptyListIfNull_EmptyLists() {
        ConfigurationFile config = new ConfigurationFile(
            "Empty Lists Test", Collections.emptyList(), Collections.emptyList(), 
            Collections.emptyList(), Collections.emptyList(),
            false, false, false, false, false);
        
        assertThat(config.getDefinitionIgnore()).isEmpty();
        assertThat(config.getDefinitionNotes()).isEmpty();
    }
    
    @Test
    public void testErrorAndWarningCodes_EmptyWhenNull() {
        ConfigurationFile config = new ConfigurationFile(
            "Empty Codes Test", null, null, null, null,
            false, false, false, false, false);
        
        assertThat(config.isError(1001)).isFalse();
        assertThat(config.isWarning(2001)).isFalse();
    }
    
    @Test
    public void testJsonDeserialization_FullConfiguration() throws Exception {
        String json = "{"
            + "\"definitionIdentifier\": \"JSON Test Profile 1.0\","
            + "\"definitionError\": ["
            + "  {\"code\": 1001, \"notes\": \"JSON Error 1\"},"
            + "  {\"code\": 1002, \"notes\": \"JSON Error 2\"}"
            + "],"
            + "\"definitionWarning\": ["
            + "  {\"code\": 2001, \"notes\": \"JSON Warning 1\"}"
            + "],"
            + "\"definitionIgnore\": [3001, 3002],"
            + "\"definitionNotes\": [\"JSON Note 1\", \"JSON Note 2\"],"
            + "\"gtldRegistrar\": true,"
            + "\"gtldRegistry\": false,"
            + "\"thinRegistry\": false,"
            + "\"rdapProfileFebruary2019\": true,"
            + "\"rdapProfileFebruary2024\": false"
            + "}";
        
        ConfigurationFile config = objectMapper.readValue(json, ConfigurationFile.class);
        
        assertThat(config.getDefinitionIdentifier()).isEqualTo("JSON Test Profile 1.0");
        assertThat(config.isError(1001)).isTrue();
        assertThat(config.isError(1002)).isTrue();
        assertThat(config.isWarning(2001)).isTrue();
        assertThat(config.getDefinitionIgnore()).containsExactly(3001, 3002);
        assertThat(config.getDefinitionNotes()).containsExactly("JSON Note 1", "JSON Note 2");
        assertThat(config.isGtldRegistrar()).isTrue();
        assertThat(config.isGtldRegistry()).isFalse();
        assertThat(config.isRdapProfileFebruary2019()).isTrue();
        assertThat(config.isRdapProfileFebruary2024()).isFalse();
    }
    
    @Test
    public void testBooleanFlags_AllCombinations() {
        ConfigurationFile config1 = new ConfigurationFile(
            "Flags Test 1", null, null, null, null,
            true, true, true, true, true);
        
        assertThat(config1.isGtldRegistrar()).isTrue();
        assertThat(config1.isGtldRegistry()).isTrue();
        assertThat(config1.isThinRegistry()).isTrue();
        assertThat(config1.isRdapProfileFebruary2019()).isTrue();
        assertThat(config1.isRdapProfileFebruary2024()).isTrue();
        
        ConfigurationFile config2 = new ConfigurationFile(
            "Flags Test 2", null, null, null, null,
            false, false, false, false, false);
        
        assertThat(config2.isGtldRegistrar()).isFalse();
        assertThat(config2.isGtldRegistry()).isFalse();
        assertThat(config2.isThinRegistry()).isFalse();
        assertThat(config2.isRdapProfileFebruary2019()).isFalse();
        assertThat(config2.isRdapProfileFebruary2024()).isFalse();
    }
}