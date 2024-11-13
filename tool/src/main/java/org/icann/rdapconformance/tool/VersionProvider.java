package org.icann.rdapconformance.tool;

import java.util.Properties;
import picocli.CommandLine.IVersionProvider;

public class VersionProvider implements IVersionProvider {
  public VersionProvider() {
  }

  public String[] getVersion() throws Exception {
    Properties properties = new Properties();
    properties.load(this.getClass().getClassLoader().getResourceAsStream("version.properties"));
    return new String[] { "${COMMAND-FULL-NAME} " + properties.getProperty("tool.version")};
  }
}
