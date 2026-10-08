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

  @Test
  public void testExtractDuplicatedOptionViaMutuallyExclusiveDuplicates() throws IOException {
    Path tempConfig = Files.createTempFile("config", ".json");
    Files.writeString(tempConfig, "{}");
    try {
      RdapConformanceTool tool = new RdapConformanceTool();
      CommandLine commandLine = new CommandLine(tool);
      // 3 repetitions often triggers the "expected only one match but got" path
      String[] args = {"--config", tempConfig.toString(),
              "--no-ipv4-queries", "--no-ipv4-queries", "--no-ipv4-queries",
              "http://example.com/domain/example.com"};
      String errorMessage = UserInputValidator.parseOptions(args, tool, commandLine);
      assertThat(errorMessage).isNotNull();
    } finally {
      Files.deleteIfExists(tempConfig);
    }
  }

  @Test
  public void testParseOptionsEmptyArgs() {
    RdapConformanceTool tool = new RdapConformanceTool();
    CommandLine commandLine = new CommandLine(tool);
    String errorMessage = UserInputValidator.parseOptions(new String[]{}, tool, commandLine);
    assertThat(errorMessage).isNotNull();
  }

  @Test
  public void testFindDuplicatedOptionViaReflection() throws Exception {
    var m = UserInputValidator.class.getDeclaredMethod("findDuplicatedOption", String[].class);
    m.setAccessible(true);

    assertThat((String) m.invoke(null, (Object) new String[]{"--no-ipv4-queries", "--no-ipv4-queries"}))
            .isEqualTo("--no-ipv4-queries");
    assertThat((String) m.invoke(null, (Object) new String[]{"--no-ipv6-queries", "--no-ipv6-queries"}))
            .isEqualTo("--no-ipv6-queries");
    assertThat((String) m.invoke(null, (Object) new String[]{"--no-ipv4-queries", "--no-ipv6-queries"}))
            .isNull(); // one of each -> no duplicate
    assertThat((String) m.invoke(null, (Object) new String[]{"https://example.com"}))
            .isNull();
  }

  @Test
  public void testExtractDuplicatedOptionViaReflection() throws Exception {
    var m = UserInputValidator.class.getDeclaredMethod("extractDuplicatedOption", String.class);
    m.setAccessible(true);

    // Well-formed picocli message pattern
    assertThat((String) m.invoke(null,
            "expected only one match but got {--x}={--no-ipv4-queries} and {--x}={--no-ipv4-queries}"))
            .isEqualTo("--no-ipv4-queries");

    // No '=' in message -> null branch
    assertThat((String) m.invoke(null, "some message without pattern")).isNull();
  }

  @Test
  public void testParameterExceptionHandlerViaExecute_unknownOption() {
    RdapConformanceTool tool = new RdapConformanceTool();
    CommandLine commandLine = new CommandLine(tool);
    // Register the handler
    UserInputValidator.parseOptions(new String[]{"--bogus"}, tool, commandLine);
    // Now execute() routes through the registered handler (UnmatchedArgumentException path)
    int code = commandLine.execute("--bogus");
    assertThat(code).isNotZero();
  }

  @Test
  public void testParameterExceptionHandlerViaExecute_mutuallyExclusive() throws IOException {
    Path tempConfig = Files.createTempFile("config", ".json");
    Files.writeString(tempConfig, "{}");
    try {
      RdapConformanceTool tool = new RdapConformanceTool();
      CommandLine commandLine = new CommandLine(tool);
      String[] args = {"--config", tempConfig.toString(),
              "--no-ipv4-queries", "--no-ipv6-queries",
              "http://example.com/domain/example.com"};
      UserInputValidator.parseOptions(args, tool, commandLine);
      // non-UnmatchedArgument exception path -> prints usage branch
      int code = commandLine.execute(args);
      assertThat(code).isNotZero();
    } finally {
      Files.deleteIfExists(tempConfig);
    }
  }

  @Test
  public void testHandleParameterExceptionGenericViaReflection() throws Exception {
    var m = UserInputValidator.class.getDeclaredMethod("handleParameterException",
            CommandLine.ParameterException.class, String[].class);
    m.setAccessible(true);

    CommandLine cl = new CommandLine(new RdapConformanceTool());
    CommandLine.ParameterException generic =
            new CommandLine.ParameterException(cl, "some generic parse error");

    String result = (String) m.invoke(null, generic, new String[]{});
    assertThat(result).isEqualTo("some generic parse error");
  }

  @Test
  public void testHandleMaxValuesExceptionFallbacksViaReflection() throws Exception {
    var m = UserInputValidator.class.getDeclaredMethod("handleMaxValuesException",
            CommandLine.MaxValuesExceededException.class, String[].class);
    m.setAccessible(true);
    CommandLine cl = new CommandLine(new RdapConformanceTool());

    // args have no duplicates -> findDuplicatedOption returns null -> message fallback
    var exIpv4 = new CommandLine.MaxValuesExceededException(cl,
            "option --no-ipv4-queries exceeded max values");
    assertThat((String) m.invoke(null, exIpv4, new String[]{}))
            .isEqualTo("Error: --no-ipv4-queries should be specified only once");

    var exIpv6 = new CommandLine.MaxValuesExceededException(cl,
            "option --no-ipv6-queries exceeded max values");
    assertThat((String) m.invoke(null, exIpv6, new String[]{}))
            .isEqualTo("Error: --no-ipv6-queries should be specified only once");

    // message mentions neither option -> raw message fallback
    var exOther = new CommandLine.MaxValuesExceededException(cl, "some other error");
    assertThat((String) m.invoke(null, exOther, new String[]{}))
            .isEqualTo("some other error");
  }

  @Test
  public void testHandleMutuallyExclusiveMessageVariantsViaReflection() throws Exception {
    var m = UserInputValidator.class.getDeclaredMethod("handleMutuallyExclusiveException",
            CommandLine.MutuallyExclusiveArgsException.class);
    m.setAccessible(true);
    CommandLine cl = new CommandLine(new RdapConformanceTool());

    // 1) pattern + "and" but NO '=' -> extractDuplicatedOption returns null -> raw message
    var exNoEquals = new CommandLine.MutuallyExclusiveArgsException(cl,
            "expected only one match but got {--x} and {--y}");
    assertThat((String) m.invoke(null, exNoEquals))
            .isEqualTo("expected only one match but got {--x} and {--y}");

    // 2) pattern WITHOUT "and" -> true mutually-exclusive fallback branch
    var exNoAnd = new CommandLine.MutuallyExclusiveArgsException(cl,
            "some other mutually exclusive error");
    assertThat((String) m.invoke(null, exNoAnd))
            .contains("mutually exclusive");

    // 3) '=' present but no closing '}' after it -> extract returns null
    var exNoBrace = new CommandLine.MutuallyExclusiveArgsException(cl,
            "expected only one match but got x=--no-ipv4-queries and more");
    assertThat((String) m.invoke(null, exNoBrace)).isNotNull();
  }

  @Test
  public void testExtractDuplicatedOptionNoClosingBraceViaReflection() throws Exception {
    var m = UserInputValidator.class.getDeclaredMethod("extractDuplicatedOption", String.class);
    m.setAccessible(true);

    // '}' appears BEFORE '=' -> firstClose < firstEquals -> null branch
    assertThat((String) m.invoke(null, "{closed} then = with no brace after")).isNull();
  }
}