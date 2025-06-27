package org.icann.rdapconformance.tool;

import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import org.icann.rdapconformance.validator.ToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Model.OptionSpec;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

public static void main(String[] args) {
  RdapConformanceTool tool = new RdapConformanceTool();
  CommandLine commandLine = new CommandLine(tool);

  commandLine.setParameterExceptionHandler((ex, params) -> {
    if (ex instanceof CommandLine.MutuallyExclusiveArgsException) {
      String message = ex.getMessage();

      // Check for duplicate options pattern
      if (message.contains("expected only one match but got") && message.contains("and")) {
        // Extract option name
        String optionName = null;

        if (message.contains("--no-ipv4-queries")) {
          optionName = "--no-ipv4-queries";
          System.err.println("Error: " + optionName + " should be specified only once");
        } else if (message.contains("--no-ipv6-queries")) {
          optionName = "--no-ipv6-queries";
          System.err.println("Error: " + optionName + " should be specified only once");
        } else {
          // Default case - use original message
          System.err.println(message);
        }
      } else {
        // True mutually exclusive case (one of each option)
        System.err.println("Error: --no-ipv4-queries, --no-ipv6-queries are mutually exclusive (specify only one)");
      }
    } else if (ex instanceof CommandLine.OverwrittenOptionException) {
      CommandLine.OverwrittenOptionException overwrittenEx = (CommandLine.OverwrittenOptionException) ex;
      String optionName = "";

      if (overwrittenEx.getOverwritten() instanceof OptionSpec) {
        optionName = ((OptionSpec) overwrittenEx.getOverwritten()).longestName();
      } else {
        optionName = overwrittenEx.getOverwritten().toString();
      }

      System.err.println("Error: " + optionName + " should be specified only once");
    } else if (ex instanceof CommandLine.MaxValuesExceededException) {
      // Handle MaxValuesExceededException which occurs with duplicate options
      String message = ex.getMessage();
      System.out.println("------> Error: " + message);
      String optionName = null;

      if (message.contains("--no-ipv4-queries")) {
        optionName = "--no-ipv4-queries";
      } else if (message.contains("--no-ipv6-queries")) {
        optionName = "--no-ipv6-queries";
      }

      if (optionName != null) {
        System.err.println("Error: " + optionName + " should be specified only once");
      } else {
        System.err.println(message);
      }
    } else {
      System.err.println(ex.getMessage());
    }

    if (!CommandLine.UnmatchedArgumentException.class.isAssignableFrom(ex.getClass())) {
      commandLine.usage(System.err);
    }

    logger.error(ToolResult.BAD_USER_INPUT.getDescription());
    return ToolResult.BAD_USER_INPUT.getCode();
  });

  int exitCode = commandLine.execute(args);
  System.exit(exitCode);
}

}