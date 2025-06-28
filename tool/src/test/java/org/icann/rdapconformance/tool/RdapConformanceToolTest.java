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
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResultFile;
import org.icann.rdapconformance.validator.workflow.rdap.file.RDAPFileValidator;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQueryTypeProcessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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