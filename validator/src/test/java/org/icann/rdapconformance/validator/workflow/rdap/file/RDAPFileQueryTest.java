package org.icann.rdapconformance.validator.workflow.rdap.file;

import org.icann.rdapconformance.validator.ConformanceError;
import org.icann.rdapconformance.validator.ToolResult;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RDAPFileQueryTest {

    private RDAPValidatorConfiguration mockConfig;
    private RDAPDatasetService mockDatasetService;
    private RDAPFileQuery fileQuery;

    @BeforeMethod
    public void setUp() {
        mockConfig = mock(RDAPValidatorConfiguration.class);
        mockDatasetService = mock(RDAPDatasetService.class);
        
        when(mockConfig.getUri()).thenReturn(URI.create("file:///tmp/test.json"));
        when(mockConfig.getQueryType()).thenReturn(RDAPQueryType.DOMAIN);
        
        fileQuery = new RDAPFileQuery(mockConfig, mockDatasetService);
    }

    @Test
    public void testConstructor_ValidParameters() {
        assertThat(fileQuery).isNotNull();
    }

    @Test
    public void testGetErrorStatus_ReturnsConfigInvalid() {
        ConformanceError errorStatus = fileQuery.getErrorStatus();
        
        assertThat(errorStatus).isEqualTo(ToolResult.CONFIG_INVALID);
    }

    @Test
    public void testRun_ValidFileUri_ReturnsFalse() {
        // Use a non-existent file path since we don't want tests to depend on actual files
        when(mockConfig.getUri()).thenReturn(URI.create("file:///nonexistent/test.json"));
        fileQuery = new RDAPFileQuery(mockConfig, mockDatasetService);
        
        boolean result = fileQuery.run();
        
        // Should return false since the file doesn't exist
        assertThat(result).isFalse();
    }

    @Test
    public void testRun_InvalidFileUri_ReturnsFalse() {
        when(mockConfig.getUri()).thenReturn(URI.create("file:///nonexistent/file.json"));
        fileQuery = new RDAPFileQuery(mockConfig, mockDatasetService);
        
        boolean result = fileQuery.run();
        
        assertThat(result).isFalse();
        assertThat(fileQuery.getData()).isNull();
    }

    @Test
    public void testValidateStructureByQueryType_AlwaysReturnsTrue() {
        boolean result = fileQuery.validateStructureByQueryType(RDAPQueryType.DOMAIN);
        
        assertThat(result).isTrue();
        
        result = fileQuery.validateStructureByQueryType(RDAPQueryType.ENTITY);
        
        assertThat(result).isTrue();
    }

    @Test
    public void testIsErrorContent_ErrorQueryType_ReturnsTrue() {
        when(mockConfig.getQueryType()).thenReturn(RDAPQueryType.ERROR);
        fileQuery = new RDAPFileQuery(mockConfig, mockDatasetService);
        
        boolean result = fileQuery.isErrorContent();
        
        assertThat(result).isTrue();
    }

    @Test
    public void testIsErrorContent_NonErrorQueryType_ReturnsFalse() {
        when(mockConfig.getQueryType()).thenReturn(RDAPQueryType.DOMAIN);
        fileQuery = new RDAPFileQuery(mockConfig, mockDatasetService);
        
        boolean result = fileQuery.isErrorContent();
        
        assertThat(result).isFalse();
    }

    @Test
    public void testGetData_InitiallyNull() {
        assertThat(fileQuery.getData()).isNull();
    }

    @Test
    public void testGetRawResponse_AlwaysReturnsNull() {
        Object rawResponse = fileQuery.getRawResponse();
        
        assertThat(rawResponse).isNull();
    }

    @Test
    public void testSetErrorStatus_NoException() {
        // Method has TODO comment but should not throw exception
        fileQuery.setErrorStatus(ToolResult.FILE_READ_ERROR);
        
        // No exception expected - method currently does nothing
    }

    @Test
    public void testRun_WithNullUri_ThrowsException() {
        when(mockConfig.getUri()).thenReturn(null);
        fileQuery = new RDAPFileQuery(mockConfig, mockDatasetService);
        
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> fileQuery.run())
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConstructor_NullConfig() {
        RDAPFileQuery query = new RDAPFileQuery(null, mockDatasetService);
        
        assertThat(query).isNotNull();
    }

    @Test
    public void testConstructor_NullDatasetService() {
        RDAPFileQuery query = new RDAPFileQuery(mockConfig, null);
        
        assertThat(query).isNotNull();
    }

    @Test
    public void testIsErrorContent_WithNullConfig_ThrowsException() {
        RDAPFileQuery query = new RDAPFileQuery(null, mockDatasetService);
        
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> query.isErrorContent())
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testRun_WithNullConfig_ThrowsException() {
        RDAPFileQuery query = new RDAPFileQuery(null, mockDatasetService);
        
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> query.run())
            .isInstanceOf(NullPointerException.class);
    }
}