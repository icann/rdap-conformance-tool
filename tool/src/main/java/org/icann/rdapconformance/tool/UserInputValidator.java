package org.icann.rdapconformance.tool;

import static org.icann.rdapconformance.validator.CommonUtils.ONE;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import org.icann.rdapconformance.validator.ToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.MaxValuesExceededException;
import picocli.CommandLine.MutuallyExclusiveArgsException;

public class UserInputValidator {
  private static final Logger logger = LoggerFactory.getLogger(UserInputValidator.class);
  private static final String NO_IPV4_QUERIES = "--no-ipv4-queries";
  private static final String NO_IPV6_QUERIES = "--no-ipv6-queries";

  public static String parseOptions(String[] args, RdapConformanceTool tool, CommandLine commandLine) {
    final String[] errorMessage = {null};

    // most of this is because Picoli wasn't detecting the logic and misuses around: --no-ipv(4|6)-queries
    // hence you'll see most of it dedicated to that.
    commandLine.setParameterExceptionHandler((ex, params) -> {
      String error = handleParameterException(ex, params);
      if (error != null) {
        System.err.println(error);
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
      errorMessage[ZERO] = handleParameterException(ex, args);
    }

    return errorMessage[ZERO];
  }

  private static String handleParameterException(CommandLine.ParameterException ex, String[] args) {
    if (ex instanceof CommandLine.MutuallyExclusiveArgsException) {
      return handleMutuallyExclusiveException((MutuallyExclusiveArgsException) ex);
    } else if (ex instanceof CommandLine.MaxValuesExceededException) {
      return handleMaxValuesException((MaxValuesExceededException) ex, args);
    } else {
      return ex.getMessage();
    }
  }

  private static String handleMutuallyExclusiveException(CommandLine.MutuallyExclusiveArgsException ex) {
    String message = ex.getMessage();
    
    // Check for duplicate options pattern
    if (message.contains("expected only one match but got") && message.contains("and")) {
      String duplicatedOption = extractDuplicatedOption(message);
      if (duplicatedOption != null) {
        return "Error: " + duplicatedOption + " should be specified only once";
      }
      return message;
    } else {
      // True mutually exclusive case (one of each option)
      return "Error: --no-ipv4-queries, --no-ipv6-queries are mutually exclusive (specify only one)";
    }
  }

  private static String handleMaxValuesException(CommandLine.MaxValuesExceededException ex, String[] args) {
    String optionName = findDuplicatedOption(args);
    
    if (optionName == null) {
      // Fallback to checking the message if counting didn't work
      String msg = ex.getMessage();
      if (msg.contains(NO_IPV4_QUERIES)) {
        optionName = NO_IPV4_QUERIES;
      } else if (msg.contains(NO_IPV6_QUERIES)) {
        optionName = NO_IPV6_QUERIES;
      }
    }

    if (optionName != null) {
      return "Error: " + optionName + " should be specified only once";
    }
    return ex.getMessage();
  }

  private static String extractDuplicatedOption(String message) {
    // Parse which option was actually duplicated
    // The pattern is: {...}={--option} and {...}={--option}
    int firstEquals = message.indexOf('=');
    int firstClose = message.indexOf('}', firstEquals);

    if (firstEquals > ZERO && firstClose > firstEquals) {
      // Extract the option between = and }
      String duplicatedOption = message.substring(firstEquals + ONE, firstClose).trim();
      // Remove the curly braces if present
      return duplicatedOption.replace("{", "").replace("}", "");
    }
    return null;
  }

  private static String findDuplicatedOption(String[] args) {
    int ipv4Count = ZERO;
    int ipv6Count = ZERO;

    for (String arg : args) {
      if (NO_IPV4_QUERIES.equals(arg)) {
        ipv4Count++;
      } else if (NO_IPV6_QUERIES.equals(arg)) {
        ipv6Count++;
      }
    }

    if (ipv4Count > ONE) {
      return NO_IPV4_QUERIES;
    } else if (ipv6Count > ONE) {
      return NO_IPV6_QUERIES;
    }
    return null;
  }
}