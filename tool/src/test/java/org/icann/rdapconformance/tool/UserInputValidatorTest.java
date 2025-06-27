package org.icann.rdapconformance.tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class UserInputValidatorTest {

  @Test
  public void testParseOptionsSuccess() throws IOException {
    Path tempConfig = Files.createTempFile("config", ".json");
    Files.writeString(tempConfig, "{}");
    
    try {
      RdapConformanceTool tool = new RdapConformanceTool();
      String[] args = {"--config", tempConfig.toString(), "http://example.com/domain/example.com"};
      
      String errorMessage = UserInputValidator.parseOptions(args, tool);
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
      String[] args = {"--config", tempConfig.toString(), "--no-ipv4-queries", "--no-ipv6-queries", "http://example.com/domain/example.com"};
      
      String errorMessage = UserInputValidator.parseOptions(args, tool);
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
      String[] args = {"--config", tempConfig.toString(), "--no-ipv4-queries", "--no-ipv4-queries", "http://example.com/domain/example.com"};
      
      String errorMessage = UserInputValidator.parseOptions(args, tool);
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
      String[] args = {"--config", tempConfig.toString(), "--no-ipv6-queries", "--no-ipv6-queries", "http://example.com/domain/example.com"};
      
      String errorMessage = UserInputValidator.parseOptions(args, tool);
      assertThat(errorMessage).isEqualTo("Error: --no-ipv6-queries should be specified only once");
    } finally {
      Files.deleteIfExists(tempConfig);
    }
  }
}