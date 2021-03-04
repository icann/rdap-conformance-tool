package org.icann.rdap.conformance.tool;

import java.io.File;
import java.util.concurrent.Callable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "rdap-conformance-tool", version = "0.1-alpha", mixinStandardHelpOptions = true)
public class RdapConformanceTool implements Callable<Integer> {

  @Option(names = {"-c", "--config"}, description = "Definition file", required = true)
  File configFile;
  @Option(names = {"--timeout"},
      description = "Timeout for connecting to the server", defaultValue = "20")
  int timeout = 20;
  @Option(names = {"--maximum-redirects"},
      description = "Maximum number of redirects to follow", defaultValue = "3")
  int maxRedirects = 3;
  @Option(names = {"--use-local-datasets"},
      description = "Use locally-persisted datasets", defaultValue = "false")
  boolean useLocalDatasets = false;
  @ArgGroup(exclusive = false)
  DependantRdapProfileGtld dependantRdapProfileGtld;


  @Override
  public Integer call() throws Exception {
    return null;
  }

  static class DependantRdapProfileGtld {

    @Option(names = {"--use-rdap-profile-february-2019"},
        description = "Use RDAP Profile February 2019", defaultValue = "false")
    boolean useRdapProfileFeb2019 = false;
    @ArgGroup(multiplicity = "1")
    ExclusiveGtldType exclusiveGtldType;
  }

  static class ExclusiveGtldType {

    @Option(names = {"--gtld-registrar"},
        description = "Validate the response as coming from a gTLD registrar",
    defaultValue = "false")
    boolean gtldRegistrar = false;
    @ArgGroup(exclusive = false)
    DependantRegistryThin dependantRegistryThin;
  }

  static class DependantRegistryThin {

    @Option(names = {"--gtld-registry"},
        description = "Validate the response as coming from a gTLD registry",
        required = true)
    boolean gtldRegistry;
    @Option(names = {"--thin"},
        description = "The TLD uses the thin model", defaultValue = "false")
    boolean thin = false;
  }
}
