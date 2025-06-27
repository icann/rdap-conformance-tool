package org.icann.rdapconformance.tool;

import static org.icann.rdapconformance.validator.CommonUtils.ONE;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import org.icann.rdapconformance.validator.ToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

public class UserInputValidator {
  private static final Logger logger = LoggerFactory.getLogger(UserInputValidator.class);

  public static String parseOptions(String[] args, RdapConformanceTool tool, CommandLine commandLine) {
    final String[] errorMessage = {null};

    commandLine.setParameterExceptionHandler((ex, params) -> {
      if (ex instanceof CommandLine.MutuallyExclusiveArgsException) {
        String message = ex.getMessage();

        // Check for duplicate options pattern
        if (message.contains("expected only one match but got") && message.contains("and")) {
          // Parse which option was actually duplicated
          // The pattern is: {...}={--option} and {...}={--option}
          int firstEquals = message.indexOf('=');
          int firstClose = message.indexOf('}', firstEquals);

          String duplicatedOption = null;
          if (firstEquals > ZERO && firstClose > firstEquals) {
            // Extract the option between = and }
            duplicatedOption = message.substring(firstEquals + 1, firstClose).trim();
            // Remove the curly braces if present
            duplicatedOption = duplicatedOption.replace("{", "").replace("}", "");

            System.err.println("Error: " + duplicatedOption + " should be specified only once");
          } else {
            // Fallback to original message if parsing fails
            System.err.println(message);
          }
        } else {
          // True mutually exclusive case (one of each option)
          System.err.println("Error: --no-ipv4-queries, --no-ipv6-queries are mutually exclusive (specify only one)");
        }
      } else if (ex instanceof CommandLine.MaxValuesExceededException) {
        String message = ex.getMessage();
        // Extract option name directly from the exception
        CommandLine.MaxValuesExceededException maxValuesEx = (CommandLine.MaxValuesExceededException) ex;
        String optionName = null;

        // Directly check the command line arguments to determine which option was duplicated
        String[] arguments = params;
        int ipv4Count = ZERO;
        int ipv6Count = ZERO;

        for (String arg : arguments) {
          if ("--no-ipv4-queries".equals(arg)) {
            ipv4Count++;
          } else if ("--no-ipv6-queries".equals(arg)) {
            ipv6Count++;
          }
        }

        if (ipv4Count > ONE) {
          optionName = "--no-ipv4-queries";
        } else if (ipv6Count > ONE) {
          optionName = "--no-ipv6-queries";
        } else {
          // Fallback to checking the message if counting didn't work
          String msg = maxValuesEx.getMessage();
          if (msg.contains("--no-ipv4-queries")) {
            optionName = "--no-ipv4-queries";
          } else if (msg.contains("--no-ipv6-queries")) {
            optionName = "--no-ipv6-queries";
          }
        }

        if (optionName != null) {
          System.err.println("Error: " + optionName + " should be specified only once");
        } else {
          System.err.println(message);
        }
      }

      if (!CommandLine.UnmatchedArgumentException.class.isAssignableFrom(ex.getClass())) {
        commandLine.usage(System.err);
      }

      logger.error(ToolResult.BAD_USER_INPUT.getDescription());
      return ToolResult.BAD_USER_INPUT.getCode();
    });

    try {
      commandLine.parseArgs(args);
    } catch (CommandLine.ParameterException ex) {
      if (ex instanceof CommandLine.MutuallyExclusiveArgsException) {
        String message = ex.getMessage();
        if (message.contains("expected only one match but got") && message.contains("and")) {
          int firstEquals = message.indexOf('=');
          int firstClose = message.indexOf('}', firstEquals);
          String duplicatedOption = null;
          if (firstEquals > ZERO && firstClose > firstEquals) {
            duplicatedOption = message.substring(firstEquals + 1, firstClose).trim();
            duplicatedOption = duplicatedOption.replace("{", "").replace("}", "");
            errorMessage[ZERO] = "Error: " + duplicatedOption + " should be specified only once";
          } else {
            errorMessage[ZERO] = message;
          }
        } else {
          errorMessage[ZERO] = "Error: --no-ipv4-queries, --no-ipv6-queries are mutually exclusive (specify only one)";
        }
      } else if (ex instanceof CommandLine.MaxValuesExceededException) {
        String[] arguments = args;
        int ipv4Count = ZERO;
        int ipv6Count = ZERO;
        for (String arg : arguments) {
          if ("--no-ipv4-queries".equals(arg)) {
            ipv4Count++;
          } else if ("--no-ipv6-queries".equals(arg)) {
            ipv6Count++;
          }
        }
        String optionName = null;
        if (ipv4Count > ONE) {
          optionName = "--no-ipv4-queries";
        } else if (ipv6Count > ONE) {
          optionName = "--no-ipv6-queries";
        } else {
          String msg = ex.getMessage();
          if (msg.contains("--no-ipv4-queries")) {
            optionName = "--no-ipv4-queries";
          } else if (msg.contains("--no-ipv6-queries")) {
            optionName = "--no-ipv6-queries";
          }
        }
        if (optionName != null) {
          errorMessage[ZERO] = "Error: " + optionName + " should be specified only once";
        } else {
          errorMessage[ZERO] = ex.getMessage();
        }
      } else {
        errorMessage[ZERO] = ex.getMessage();
      }
    }

    return errorMessage[ZERO];
  }
}