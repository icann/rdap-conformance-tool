package org.icann.rdapconformance.tool;

import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import org.icann.rdapconformance.validator.ToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    RdapConformanceTool tool = new RdapConformanceTool();
    CommandLine commandLine = new CommandLine(tool);

    commandLine.setExitCodeExceptionMapper(exception -> {
      if (exception instanceof ParameterException) {
         logger.error(ToolResult.BAD_USER_INPUT.getDescription());
        return ToolResult.BAD_USER_INPUT.getCode();
      }
      return ZERO; // default is zero, all the others should be handled in RDAPConformanceTool
    });

    int exitCode = commandLine.execute(args);
    System.exit(exitCode);
  }

}
