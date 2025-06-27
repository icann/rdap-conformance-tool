package org.icann.rdapconformance.tool;

import org.icann.rdapconformance.validator.ToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;


public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    RdapConformanceTool tool = new RdapConformanceTool();
    String errorMessage = UserInputValidator.parseOptions(args, tool);
    
    if (errorMessage != null) {
      System.err.println(errorMessage);
      logger.error(ToolResult.BAD_USER_INPUT.getDescription());
      System.exit(ToolResult.BAD_USER_INPUT.getCode());
    }
    
    CommandLine commandLine = new CommandLine(tool);
    int exitCode = commandLine.execute(args);
    System.exit(exitCode);
  }
}