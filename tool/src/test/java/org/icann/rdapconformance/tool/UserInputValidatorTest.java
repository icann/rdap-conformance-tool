package org.icann.rdapconformance.tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;
import picocli.CommandLine;

public class UserInputValidatorTest {

  @Test
  public void testParseOptionsSuccess() throws IOException {
    Path tempConfig = Files.createTempFile("config", ".json");
    Files.writeString(tempConfig, "{}");
    
    try {
      RdapConformanceTool tool = new RdapConformanceTool();
      CommandLine commandLine = new CommandLine(tool);
      String[] args = {"--config", tempConfig.toString(), "http://example.com/domain/example.com"};
      
      String errorMessage = UserInputValidator.parseOptions(args, tool, commandLine);
      assertThat(errorMessage).isNull();
    } finally {
      Files.deleteIfExists(tempConfig);
    }
  }

  @Test
  public void testParseOptionsMutuallyExclusive() throws IOException {
    Path tempConfig = Files.createTempFile("config", ".json");
    Files.writeString(tempConfig, "{}");
    
    try {
      RdapConformanceTool tool = new RdapConformanceTool();
      CommandLine commandLine = new CommandLine(tool);
      String[] args = {"--config", tempConfig.toString(), "--no-ipv4-queries", "--no-ipv6-queries", "http://example.com/domain/example.com"};
      
      String errorMessage = UserInputValidator.parseOptions(args, tool, commandLine);
      assertThat(errorMessage).isEqualTo("Error: --no-ipv4-queries, --no-ipv6-queries are mutually exclusive (specify only one)");
    } finally {
      Files.deleteIfExists(tempConfig);
    }
  }

  @Test
  public void testParseOptionsDuplicateNoIpv4() throws IOException {
    Path tempConfig = Files.createTempFile("config", ".json");
    Files.writeString(tempConfig, "{}");
    
    try {
      RdapConformanceTool tool = new RdapConformanceTool();
      CommandLine commandLine = new CommandLine(tool);
      String[] args = {"--config", tempConfig.toString(), "--no-ipv4-queries", "--no-ipv4-queries", "http://example.com/domain/example.com"};
      
      String errorMessage = UserInputValidator.parseOptions(args, tool, commandLine);
      assertThat(errorMessage).isEqualTo("Error: --no-ipv4-queries should be specified only once");
    } finally {
      Files.deleteIfExists(tempConfig);
    }
  }

  @Test
  public void testParseOptionsDuplicateNoIpv6() throws IOException {
    Path tempConfig = Files.createTempFile("config", ".json");
    Files.writeString(tempConfig, "{}");
    
    try {
      RdapConformanceTool tool = new RdapConformanceTool();
      CommandLine commandLine = new CommandLine(tool);
      String[] args = {"--config", tempConfig.toString(), "--no-ipv6-queries", "--no-ipv6-queries", "http://example.com/domain/example.com"};
      
      String errorMessage = UserInputValidator.parseOptions(args, tool, commandLine);
      assertThat(errorMessage).isEqualTo("Error: --no-ipv6-queries should be specified only once");
    } finally {
      Files.deleteIfExists(tempConfig);
    }
  }

  @Test
  public void testParseOptionsWithInvalidArguments() throws IOException {
    Path tempConfig = Files.createTempFile("config", ".json");
    Files.writeString(tempConfig, "{}");
    
    try {
      RdapConformanceTool tool = new RdapConformanceTool();
      CommandLine commandLine = new CommandLine(tool);
      String[] args = {"--config", tempConfig.toString(), "--invalid-option", "http://example.com/domain/example.com"};
      
      String errorMessage = UserInputValidator.parseOptions(args, tool, commandLine);
      assertThat(errorMessage).isNotNull();
      assertThat(errorMessage).contains("Unknown option");
    } finally {
      Files.deleteIfExists(tempConfig);
    }
  }

  @Test
  public void testParseOptionsWithMissingConfig() {
    RdapConformanceTool tool = new RdapConformanceTool();
    CommandLine commandLine = new CommandLine(tool);
    String[] args = {"http://example.com/domain/example.com"};
    
    String errorMessage = UserInputValidator.parseOptions(args, tool, commandLine);
    assertThat(errorMessage).isNotNull();
    assertThat(errorMessage).contains("Missing required option");
  }

  @Test
  public void testParseOptionsWithMissingURI() throws IOException {
    Path tempConfig = Files.createTempFile("config", ".json");
    Files.writeString(tempConfig, "{}");
    
    try {
      RdapConformanceTool tool = new RdapConformanceTool();
      CommandLine commandLine = new CommandLine(tool);
      String[] args = {"--config", tempConfig.toString()};
      
      String errorMessage = UserInputValidator.parseOptions(args, tool, commandLine);
      assertThat(errorMessage).isNotNull();
      assertThat(errorMessage).contains("Missing required parameter");
    } finally {
      Files.deleteIfExists(tempConfig);
    }
  }

  @Test
  public void testParseOptionsWithValidRdapProfile() throws IOException {
    Path tempConfig = Files.createTempFile("config", ".json");
    Files.writeString(tempConfig, "{}");
    
    try {
      RdapConformanceTool tool = new RdapConformanceTool();
      CommandLine commandLine = new CommandLine(tool);
      String[] args = {"--config", tempConfig.toString(), "--gtld-registry", "--use-rdap-profile-february-2024", "http://example.com/domain/example.com"};
      
      String errorMessage = UserInputValidator.parseOptions(args, tool, commandLine);
      assertThat(errorMessage).isNull();
    } finally {
      Files.deleteIfExists(tempConfig);
    }
  }
}