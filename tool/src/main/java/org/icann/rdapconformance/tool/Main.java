package org.icann.rdapconformance.tool;

import picocli.CommandLine;

public class Main {

  public static void main(String[] args) {
    int exitCode = new CommandLine(new RdapConformanceTool()).execute(args);
    System.exit(exitCode);
  }

}
