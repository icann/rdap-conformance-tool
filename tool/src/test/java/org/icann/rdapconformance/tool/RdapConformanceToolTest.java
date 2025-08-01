package org.icann.rdapconformance.tool;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Field;

import org.apache.commons.lang3.SystemUtils;
import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.ToolResult;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResultFile;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.icann.rdapconformance.validator.workflow.rdap.file.RDAPFileValidator;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQueryTypeProcessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class RdapConformanceToolTest {

  private RdapConformanceTool tool;
  private Path tempConfigFile;

  private String getUriStrFromConfig(String fileInput) {
    RdapConformanceTool tool = new RdapConformanceTool();
    tool.configurationFile = fileInput;
    return tool.getConfigurationFile().toString();
  }

  @Test
  public void testLinuxPath() {
    assertThat(getUriStrFromConfig("/tmp/test")).endsWith("/tmp/test");
  }

  @Test
  public void testURIPath() {
    assertThat(getUriStrFromConfig("file:/tmp/test")).endsWith("/tmp/test");
  }

  @Test
  public void testWindowsURIPath() {
    assertThat(getUriStrFromConfig("file:/D:/tmp/test")).endsWith("/tmp/test");
  }

  @Test
  public void testURIRemotePath() {
    assertThat(getUriStrFromConfig("http://tmp/test")).endsWith("/tmp/test");
  }

  @Test
  public void testWindowsFilePath() {
    if (SystemUtils.IS_OS_WINDOWS) {
      assertThat(getUriStrFromConfig("D:\\tmp\\test")).endsWith("/tmp/test");
    }
  }

  @BeforeMethod
  public void setUp() throws IOException {
    tool = new RdapConformanceTool();
    tool.uri = URI.create("http://example.com/domain/example.com");

    // Create a temporary file for configuration
    tempConfigFile = Files.createTempFile("config", ".json");
    Files.writeString(tempConfigFile, "{}"); // Empty JSON
    tool.configurationFile = tempConfigFile.toString();
  }

  @AfterMethod
  public void tearDown() throws IOException {
    if (tempConfigFile != null) {
      Files.deleteIfExists(tempConfigFile);
    }
  }

  @Test
  public void testDatasetInitializationFailure() throws Exception {
    // Mock the dataset initialization to return null
    try (MockedStatic<CommonUtils> mockedCommonUtils = Mockito.mockStatic(CommonUtils.class)) {
      mockedCommonUtils.when(() -> CommonUtils.initializeDataSet(any())).thenReturn(null);

      // Call should return dataset unavailable code
      int result = tool.call();
      assertThat(result).isEqualTo(ToolResult.DATASET_UNAVAILABLE.getCode());
    }
  }

  @Test
  public void testConfigFileFailure() throws Exception {
    RDAPDatasetService mockDatasetService = mock(RDAPDatasetService.class);

    try (MockedStatic<CommonUtils> mockedCommonUtils = Mockito.mockStatic(CommonUtils.class)) {
      mockedCommonUtils.when(() -> CommonUtils.initializeDataSet(any())).thenReturn(mockDatasetService);
      mockedCommonUtils.when(() -> CommonUtils.verifyConfigFile(any(), any())).thenReturn(null);

      // Call should return config invalid code
      int result = tool.call();
      assertThat(result).isEqualTo(ToolResult.CONFIG_INVALID.getCode());
    }
  }

  @Test
  public void testQueryTypeFailure() throws Exception {
    RDAPDatasetService mockDatasetService = mock(RDAPDatasetService.class);
    ConfigurationFile mockConfigFile = mock(ConfigurationFile.class);

    RDAPHttpQueryTypeProcessor mockProcessor = mock(RDAPHttpQueryTypeProcessor.class);
    when(mockProcessor.check(any())).thenReturn(false);
    when(mockProcessor.getErrorStatus()).thenReturn(ToolResult.UNSUPPORTED_QUERY);

    try (MockedStatic<CommonUtils> mockedCommonUtils = Mockito.mockStatic(CommonUtils.class);
         MockedStatic<RDAPHttpQueryTypeProcessor> mockedProcessor = Mockito.mockStatic(RDAPHttpQueryTypeProcessor.class)) {

      mockedCommonUtils.when(() -> CommonUtils.initializeDataSet(any())).thenReturn(mockDatasetService);
      mockedCommonUtils.when(() -> CommonUtils.verifyConfigFile(any(), any())).thenReturn(mockConfigFile);
      mockedProcessor.when(() -> RDAPHttpQueryTypeProcessor.getInstance(any())).thenReturn(mockProcessor);

      int result = tool.call();
      assertThat(result).isEqualTo(ToolResult.UNSUPPORTED_QUERY.getCode());
    }
  }

  // TODO: when do we fail on the THIN? Ever?
  @Ignore
  @Test
  public void testThinModelEntityQueryFailure() throws Exception {
    // Setup for thin model with entity query -- this is a MUST
    tool.queryType = RDAPQueryType.ENTITY;

    // Create a tool with the thin model flag set
    RdapConformanceTool thinTool = new TestRdapConformanceTool(true);
    thinTool.uri = URI.create("http://example.com/entity/example");
    thinTool.configurationFile = tempConfigFile.toString();
    thinTool.queryType = RDAPQueryType.ENTITY;

    // Setup mocks for successful path up to thin model check
    RDAPDatasetService mockDatasetService = mock(RDAPDatasetService.class);
    ConfigurationFile mockConfigFile = mock(ConfigurationFile.class);

    RDAPHttpQueryTypeProcessor mockProcessor = mock(RDAPHttpQueryTypeProcessor.class);
    when(mockProcessor.check(any())).thenReturn(true);
    when(mockProcessor.getQueryType()).thenReturn(RDAPQueryType.ENTITY);

    try (MockedStatic<CommonUtils> mockedCommonUtils = Mockito.mockStatic(CommonUtils.class);
         MockedStatic<RDAPHttpQueryTypeProcessor> mockedProcessor = Mockito.mockStatic(RDAPHttpQueryTypeProcessor.class)) {

      mockedCommonUtils.when(() -> CommonUtils.initializeDataSet(any())).thenReturn(mockDatasetService);
      mockedCommonUtils.when(() -> CommonUtils.verifyConfigFile(any(), any())).thenReturn(mockConfigFile);
      mockedProcessor.when(() -> RDAPHttpQueryTypeProcessor.getInstance(any())).thenReturn(mockProcessor);

      int result = thinTool.call();
      assertThat(result).isEqualTo(ToolResult.USES_THIN_MODEL.getCode());
    }
  }

@Test
public void testBuildResultFileFailure() throws Exception {
    // Setup for a file validation scenario where result file build fails
    tool.uri = URI.create("file:///tmp/example.json");
    tool.queryType = RDAPQueryType.DOMAIN;

    // Set required RDAP profile options using reflection
    setMandatoryRdapProfileOptions(tool);

    RDAPValidationResultFile mockResultFile = mock(RDAPValidationResultFile.class);
    RDAPFileValidator mockValidator = mock(RDAPFileValidator.class);
    when(mockResultFile.build()).thenReturn(false);

    // Create a spy of the tool
    RdapConformanceTool spyTool = spy(tool);

    int result = spyTool.validateWithoutNetwork(mockResultFile, mockValidator);
    assertThat(result).isEqualTo(ToolResult.FILE_WRITE_ERROR.getCode());
}

@Test
public void testSettersPreventBothProtocolsDisabled() throws Exception {
    // Test that setters prevent both protocols from being disabled
    tool.setExecuteIPv4Queries(false);
    tool.setExecuteIPv6Queries(false);
    
    // After setting both to false, IPv4 should be automatically re-enabled
    assertThat(tool.isNoIpv4Queries()).isFalse(); // IPv4 is enabled
    assertThat(tool.isNoIpv6Queries()).isTrue();  // IPv6 is disabled
    
    // Test the reverse order
    RdapConformanceTool tool2 = new RdapConformanceTool();
    tool2.uri = URI.create("http://example.com/domain/example.com");
    tool2.configurationFile = tempConfigFile.toString();
    
    tool2.setExecuteIPv6Queries(false);
    tool2.setExecuteIPv4Queries(false);
    
    // After setting both to false, IPv6 should be automatically re-enabled
    assertThat(tool2.isNoIpv4Queries()).isTrue();  // IPv4 is disabled
    assertThat(tool2.isNoIpv6Queries()).isFalse(); // IPv6 is enabled
}

// Helper method to set the mandatory RDAP profile options
private void setMandatoryRdapProfileOptions(RdapConformanceTool tool) throws Exception {
    // Create required nested objects through reflection
    Field dependantRdapProfileGtldField = RdapConformanceTool.class.getDeclaredField("dependantRdapProfileGtld");
    dependantRdapProfileGtldField.setAccessible(true);
    Object dependantRdapProfileGtld = dependantRdapProfileGtldField.get(tool);

    // Get the exclusiveRdapProfile field
    Field exclusiveRdapProfileField = dependantRdapProfileGtld.getClass().getDeclaredField("exclusiveRdapProfile");
    exclusiveRdapProfileField.setAccessible(true);
    Object exclusiveRdapProfile = exclusiveRdapProfileField.get(dependantRdapProfileGtld);

    // Set useRdapProfileFeb2024 to true
    Field dependantRdapProfileField = exclusiveRdapProfile.getClass().getDeclaredField("dependantRdapProfile");
    dependantRdapProfileField.setAccessible(true);
    Object dependantRdapProfile = dependantRdapProfileField.get(exclusiveRdapProfile);

    Field useRdapProfileFeb2024Field = dependantRdapProfile.getClass().getDeclaredField("useRdapProfileFeb2024");
    useRdapProfileFeb2024Field.setAccessible(true);
    useRdapProfileFeb2024Field.set(dependantRdapProfile, true);

    // Set gtldRegistry to true
    Field exclusiveGtldTypeField = exclusiveRdapProfile.getClass().getDeclaredField("exclusiveGtldType");
    exclusiveGtldTypeField.setAccessible(true);
    Object exclusiveGtldType = exclusiveGtldTypeField.get(exclusiveRdapProfile);

    Field dependantRegistryThinField = exclusiveGtldType.getClass().getDeclaredField("dependantRegistryThin");
    dependantRegistryThinField.setAccessible(true);
    Object dependantRegistryThin = dependantRegistryThinField.get(exclusiveGtldType);

    Field gtldRegistryField = dependantRegistryThin.getClass().getDeclaredField("gtldRegistry");
    gtldRegistryField.setAccessible(true);
    gtldRegistryField.set(dependantRegistryThin, true);
}

@Test
public void testGetErrorsWhenNoValidationRun() {
    // Test getErrors when no validation has been run (singleton not initialized properly)
    RDAPValidationResultFile.reset();
    
    List<RDAPValidationResult> errors = tool.getErrors();
    assertThat(errors).isEmpty();
    
    List<RDAPValidationResult> allResults = tool.getAllResults();
    assertThat(allResults).isEmpty();
    
    int errorCount = tool.getErrorCount();
    assertThat(errorCount).isEqualTo(0);
}

@Test
public void testGetErrorsWithMockedResults() throws Exception {
    // Setup mock validation results
    RDAPValidatorResults mockResults = mock(RDAPValidatorResults.class);
    ConfigurationFile mockConfigFile = mock(ConfigurationFile.class);
    
    // Create test validation results
    RDAPValidationResult error1 = RDAPValidationResult.builder()
        .code(-12345)
        .message("Test error 1")
        .value("test-value-1")
        .httpStatusCode(400)
        .queriedURI("https://example.com/test1")
        .build();
        
    RDAPValidationResult error2 = RDAPValidationResult.builder()
        .code(-67890)
        .message("Test error 2")
        .value("test-value-2")
        .httpStatusCode(500)
        .queriedURI("https://example.com/test2")
        .build();
        
    RDAPValidationResult warning = RDAPValidationResult.builder()
        .code(-11111)
        .message("Test warning")
        .value("test-warning-value")
        .httpStatusCode(200)
        .queriedURI("https://example.com/warning")
        .build();
    
    Set<RDAPValidationResult> allTestResults = Set.of(error1, error2, warning);
    when(mockResults.getAll()).thenReturn(allTestResults);
    
    // Configure mock config file to classify results
    when(mockConfigFile.isError(-12345)).thenReturn(true);
    when(mockConfigFile.isError(-67890)).thenReturn(true);
    when(mockConfigFile.isError(-11111)).thenReturn(false);
    when(mockConfigFile.isWarning(-11111)).thenReturn(true);
    when(mockConfigFile.getDefinitionIgnore()).thenReturn(List.of());
    
    // Initialize the RDAPValidationResultFile singleton with mock data
    RDAPValidationResultFile.reset();
    RDAPValidationResultFile resultFile = RDAPValidationResultFile.getInstance();
    resultFile.initialize(mockResults, tool, mockConfigFile, mock(org.icann.rdapconformance.validator.workflow.FileSystem.class));
    
    // Test getErrors - should return only the errors, not warnings
    List<RDAPValidationResult> errors = tool.getErrors();
    assertThat(errors).hasSize(2);
    assertThat(errors).extracting(RDAPValidationResult::getCode).containsExactlyInAnyOrder(-12345, -67890);
    assertThat(errors).extracting(RDAPValidationResult::getMessage).containsExactlyInAnyOrder("Test error 1", "Test error 2");
    
    // Test getAllResults - should return all results
    List<RDAPValidationResult> allResults = tool.getAllResults();
    assertThat(allResults).hasSize(3);
    assertThat(allResults).extracting(RDAPValidationResult::getCode).containsExactlyInAnyOrder(-12345, -67890, -11111);
    
    // Test getErrorCount
    int errorCount = tool.getErrorCount();
    assertThat(errorCount).isEqualTo(2);
}

@Test
public void testGetErrorsWithRealValidationResults() throws Exception {
    // Use real RDAPValidatorResultsImpl for more realistic testing
    RDAPValidatorResultsImpl realResults = RDAPValidatorResultsImpl.getInstance();
    realResults.clear();
    
    // Add some test results
    realResults.add(RDAPValidationResult.builder()
        .code(-46200)
        .message("Handle format violation")
        .value("INVALID-HANDLE")
        .httpStatusCode(200)
        .queriedURI("https://example.com/entity/INVALID")
        .build());
        
    realResults.add(RDAPValidationResult.builder()
        .code(-20900)
        .message("Tel property without voice or fax type")
        .value("tel-property-data")
        .httpStatusCode(200)
        .queriedURI("https://example.com/entity/TEST")
        .build());
    
    // Setup config file mock
    ConfigurationFile mockConfigFile = mock(ConfigurationFile.class);
    when(mockConfigFile.isError(-46200)).thenReturn(true);
    when(mockConfigFile.isError(-20900)).thenReturn(true);
    when(mockConfigFile.isWarning(any(Integer.class))).thenReturn(false);
    when(mockConfigFile.getDefinitionIgnore()).thenReturn(List.of());
    
    // Initialize the RDAPValidationResultFile singleton
    RDAPValidationResultFile.reset();
    RDAPValidationResultFile resultFile = RDAPValidationResultFile.getInstance();
    resultFile.initialize(realResults, tool, mockConfigFile, mock(org.icann.rdapconformance.validator.workflow.FileSystem.class));
    
    // Test the new methods
    List<RDAPValidationResult> errors = tool.getErrors();
    assertThat(errors).hasSize(2);
    
    List<RDAPValidationResult> allResults = tool.getAllResults();
    assertThat(allResults).hasSize(2);
    
    int errorCount = tool.getErrorCount();
    assertThat(errorCount).isEqualTo(2);
    
    // Verify specific error details
    RDAPValidationResult handleError = errors.stream()
        .filter(r -> r.getCode() == -46200)
        .findFirst()
        .orElse(null);
    assertThat(handleError).isNotNull();
    assertThat(handleError.getMessage()).isEqualTo("Handle format violation");
    assertThat(handleError.getValue()).isEqualTo("INVALID-HANDLE");
    assertThat(handleError.getHttpStatusCode()).isEqualTo(200);
    assertThat(handleError.getQueriedURI()).isEqualTo("https://example.com/entity/INVALID");
}

@Test
public void testGetErrorsHandlesExceptionGracefully() {
    // Force an exception by not properly initializing the singleton
    RDAPValidationResultFile.reset();
    
    // The methods should handle exceptions gracefully and return empty/zero values
    List<RDAPValidationResult> errors = tool.getErrors();
    assertThat(errors).isEmpty();
    
    List<RDAPValidationResult> allResults = tool.getAllResults();
    assertThat(allResults).isEmpty();
    
    int errorCount = tool.getErrorCount();
    assertThat(errorCount).isEqualTo(0);
}

@Test
public void testGetErrorsAsJsonWhenNoValidationRun() {
    // Test JSON methods when no validation has been run
    RDAPValidationResultFile.reset();
    
    String errorsJson = tool.getErrorsAsJson();
    assertThat(errorsJson).isEqualTo("[]");
    
    String warningsJson = tool.getWarningsAsJson();
    assertThat(warningsJson).isEqualTo("[]");
    
    String allResultsJson = tool.getAllResultsAsJson();
    assertThat(allResultsJson).contains("\"error\": []");
    assertThat(allResultsJson).contains("\"warning\": []");
    assertThat(allResultsJson).contains("\"ignore\": []");
    assertThat(allResultsJson).contains("\"notes\": []");
}

@Test
public void testJsonMethodsWithMockedResults() throws Exception {
    // Setup mock validation results (reuse setup from previous test)
    RDAPValidatorResults mockResults = mock(RDAPValidatorResults.class);
    ConfigurationFile mockConfigFile = mock(ConfigurationFile.class);
    
    // Create test validation results with same HTTP status to avoid triggering -13018
    RDAPValidationResult error1 = RDAPValidationResult.builder()
        .code(-12345)
        .message("Test error 1")
        .value("test-value-1")
        .httpStatusCode(200)
        .queriedURI("https://example.com/test1")
        .build();
        
    RDAPValidationResult warning1 = RDAPValidationResult.builder()
        .code(-11111)
        .message("Test warning")
        .value("test-warning-value")
        .httpStatusCode(200)
        .queriedURI("https://example.com/warning")
        .build();
    
    Set<RDAPValidationResult> allTestResults = Set.of(error1, warning1);
    when(mockResults.getAll()).thenReturn(allTestResults);
    
    // Configure mock config file to classify results
    when(mockConfigFile.isError(-12345)).thenReturn(true);
    when(mockConfigFile.isError(-11111)).thenReturn(false);
    when(mockConfigFile.isWarning(-11111)).thenReturn(true);
    when(mockConfigFile.getDefinitionIgnore()).thenReturn(List.of());
    when(mockConfigFile.getAlertNotes(-12345)).thenReturn("Error note");
    when(mockConfigFile.getAlertNotes(-11111)).thenReturn("Warning note");
    when(mockConfigFile.getDefinitionNotes()).thenReturn(List.of("General note"));
    
    // Initialize the RDAPValidationResultFile singleton with mock data
    RDAPValidationResultFile.reset();
    RDAPValidationResultFile resultFile = RDAPValidationResultFile.getInstance();
    resultFile.initialize(mockResults, tool, mockConfigFile, mock(org.icann.rdapconformance.validator.workflow.FileSystem.class));
    
    // Test getErrorsAsJson
    String errorsJson = tool.getErrorsAsJson();
    assertThat(errorsJson).contains("-12345");
    assertThat(errorsJson).contains("Test error 1");
    assertThat(errorsJson).contains("test-value-1");
    assertThat(errorsJson).contains("200");
    assertThat(errorsJson).contains("https://example.com/test1");
    assertThat(errorsJson).doesNotContain("-11111"); // Should not contain warnings
    
    // Test getWarningsAsJson
    String warningsJson = tool.getWarningsAsJson();
    assertThat(warningsJson).contains("-11111");
    assertThat(warningsJson).contains("Test warning");
    assertThat(warningsJson).contains("test-warning-value");
    assertThat(warningsJson).doesNotContain("-12345"); // Should not contain errors
    
    // Test getAllResultsAsJson
    String allResultsJson = tool.getAllResultsAsJson();
    assertThat(allResultsJson).contains("\"error\":");
    assertThat(allResultsJson).contains("\"warning\":");
    assertThat(allResultsJson).contains("\"ignore\":");
    assertThat(allResultsJson).contains("\"notes\":");
    assertThat(allResultsJson).contains("-12345"); // Should contain errors
    assertThat(allResultsJson).contains("-11111"); // Should contain warnings
    
    // Verify it's valid JSON by checking basic structure
    assertThat(allResultsJson).startsWith("{");
    assertThat(allResultsJson).endsWith("}");
}


@Test  
public void testJsonMethodsReturnValidJson() throws Exception {
    // Test that JSON methods return properly formatted JSON
    RDAPValidatorResultsImpl realResults = RDAPValidatorResultsImpl.getInstance();
    realResults.clear();
    
    realResults.add(RDAPValidationResult.builder()
        .code(-46200)
        .message("Handle format violation")
        .value("INVALID-HANDLE")
        .httpStatusCode(200)
        .queriedURI("https://example.com/entity/INVALID")
        .build());
    
    ConfigurationFile mockConfigFile = mock(ConfigurationFile.class);
    when(mockConfigFile.isError(-46200)).thenReturn(true);
    when(mockConfigFile.isWarning(any(Integer.class))).thenReturn(false);
    when(mockConfigFile.getDefinitionIgnore()).thenReturn(List.of());
    when(mockConfigFile.getAlertNotes(-46200)).thenReturn("Handle violation note");
    when(mockConfigFile.getDefinitionNotes()).thenReturn(List.of());
    
    RDAPValidationResultFile.reset();
    RDAPValidationResultFile resultFile = RDAPValidationResultFile.getInstance();
    resultFile.initialize(realResults, tool, mockConfigFile, mock(org.icann.rdapconformance.validator.workflow.FileSystem.class));
    
    // Test that all JSON methods return valid JSON strings
    String errorsJson = tool.getErrorsAsJson();
    assertThat(errorsJson).startsWith("[");
    assertThat(errorsJson).endsWith("]");
    assertThat(errorsJson).contains("\"code\": -46200");
    
    String warningsJson = tool.getWarningsAsJson();
    assertThat(warningsJson).isEqualTo("[]"); // No warnings in this test
    
    String allResultsJson = tool.getAllResultsAsJson();
    assertThat(allResultsJson).startsWith("{");
    assertThat(allResultsJson).endsWith("}");
    assertThat(allResultsJson).contains("\"error\":");
    assertThat(allResultsJson).contains("\"warning\":");
}

  // Helper class for thin model testing
  private static class TestRdapConformanceTool extends RdapConformanceTool {
    private final boolean isThin;

    public TestRdapConformanceTool(boolean isThin) {
      this.isThin = isThin;
    }

    @Override
    public boolean isThin() {
      return isThin;
    }
  }
}