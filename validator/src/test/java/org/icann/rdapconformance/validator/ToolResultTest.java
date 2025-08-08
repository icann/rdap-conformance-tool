package org.icann.rdapconformance.validator;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ToolResultTest {

    @Test
    public void testEnumValues() {
        ToolResult[] values = ToolResult.values();
        
        assertThat(values).hasSize(9);
        assertThat(values).contains(
            ToolResult.SUCCESS,
            ToolResult.CONFIG_INVALID,
            ToolResult.DATASET_UNAVAILABLE,
            ToolResult.UNSUPPORTED_QUERY,
            ToolResult.MIXED_LABEL_FORMAT,
            ToolResult.USES_THIN_MODEL,
            ToolResult.FILE_WRITE_ERROR,
            ToolResult.FILE_READ_ERROR,
            ToolResult.BAD_USER_INPUT
        );
    }
    
    @Test
    public void testSuccess() {
        ToolResult result = ToolResult.SUCCESS;
        
        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getDescription()).contains("A response was available to the tool");
        assertThat(result.getDescription()).contains("application/rdap+JSON");
    }
    
    @Test
    public void testConfigInvalid() {
        ToolResult result = ToolResult.CONFIG_INVALID;
        
        assertThat(result.getCode()).isEqualTo(1);
        assertThat(result.getDescription()).isEqualTo("The configuration definition file is syntactically invalid");
    }
    
    @Test
    public void testDatasetUnavailable() {
        ToolResult result = ToolResult.DATASET_UNAVAILABLE;
        
        assertThat(result.getCode()).isEqualTo(2);
        assertThat(result.getDescription()).isEqualTo("The tool was not able to download a dataset");
    }
    
    @Test
    public void testUnsupportedQuery() {
        ToolResult result = ToolResult.UNSUPPORTED_QUERY;
        
        assertThat(result.getCode()).isEqualTo(3);
        assertThat(result.getDescription()).isEqualTo("The RDAP query is not supported by the tool");
    }
    
    @Test
    public void testMixedLabelFormat() {
        ToolResult result = ToolResult.MIXED_LABEL_FORMAT;
        
        assertThat(result.getCode()).isEqualTo(4);
        assertThat(result.getDescription()).contains("A-labels and U-labels are mixed");
    }
    
    @Test
    public void testUsesThinModel() {
        ToolResult result = ToolResult.USES_THIN_MODEL;
        
        assertThat(result.getCode()).isEqualTo(9);
        assertThat(result.getDescription()).contains("thin model");
    }
    
    @Test
    public void testFileWriteError() {
        ToolResult result = ToolResult.FILE_WRITE_ERROR;
        
        assertThat(result.getCode()).isEqualTo(21);
        assertThat(result.getDescription()).isEqualTo("Failure in writing to results file");
    }
    
    @Test
    public void testFileReadError() {
        ToolResult result = ToolResult.FILE_READ_ERROR;
        
        assertThat(result.getCode()).isEqualTo(22);
        assertThat(result.getDescription()).isEqualTo("Failure in reading from results file");
    }
    
    @Test
    public void testBadUserInput() {
        ToolResult result = ToolResult.BAD_USER_INPUT;
        
        assertThat(result.getCode()).isEqualTo(25);
        assertThat(result.getDescription()).isEqualTo("The user input is invalid");
    }
    
    @Test
    public void testConformanceErrorInterface() {
        ToolResult result = ToolResult.SUCCESS;
        
        assertThat(result).isInstanceOf(ConformanceError.class);
        assertThat(((ConformanceError) result).getCode()).isEqualTo(0);
    }
    
    @Test
    public void testCodeUniqueness() {
        ToolResult[] values = ToolResult.values();
        
        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                assertThat(values[i].getCode())
                    .as("Codes should be unique: %s vs %s", values[i], values[j])
                    .isNotEqualTo(values[j].getCode());
            }
        }
    }
    
    @Test
    public void testValueOf() {
        assertThat(ToolResult.valueOf("SUCCESS"))
            .isEqualTo(ToolResult.SUCCESS);
        assertThat(ToolResult.valueOf("CONFIG_INVALID"))
            .isEqualTo(ToolResult.CONFIG_INVALID);
    }
    
    @Test
    public void testValueOf_InvalidName_ThrowsException() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> ToolResult.valueOf("INVALID"))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void testDescriptionNotEmpty() {
        for (ToolResult result : ToolResult.values()) {
            assertThat(result.getDescription())
                .as("Description should not be empty for %s", result)
                .isNotEmpty();
        }
    }
    
    @Test
    public void testGetters() {
        ToolResult result = ToolResult.SUCCESS;
        
        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getDescription()).isNotNull().isNotEmpty();
    }
}